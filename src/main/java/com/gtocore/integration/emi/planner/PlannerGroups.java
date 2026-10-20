package com.gtocore.integration.emi.planner;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.TextField;

import net.minecraft.network.chat.Component;

import dev.emi.emi.planner.ProductionPlanner;
import dev.emi.emi.planner.ProductionPlanner.Entry;
import dev.emi.emi.planner.ProductionPlanner.Group;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Locale;
import java.util.function.Consumer;

import static com.gtocore.integration.emi.planner.PlannerWidgets.*;

final class PlannerGroups {

    private PlannerGroups() {}

    static void groups(PlannerHost host, UIElement page, Group selected) {
        host.heading(page, selected == null ? text("groups.title", "Groups and Links").getString() : selected.getDisplayName(host.line()));
        var controls = wrap().addChildren(
                button("groups.new", "+ GROUP", () -> host.change(() -> ProductionPlanner.createGroup(host.line(), null))),
                button("groups.child", "+ CHILD", () -> host.change(() -> ProductionPlanner.createGroup(host.line(), selected))),
                button("groups.recipes", "RECIPES", () -> host.show(p -> recipes(host, p, selected))),
                button("groups.links", "LINKS", () -> host.show(p -> links(host, p, selected))));
        if (selected != null) {
            var name = TextField.of(LayoutStyle.AUTO, selected::getName,
                    value -> host.change(() -> ProductionPlanner.renameGroup(host.line(), selected, value))).commitOnSubmit(value -> true);
            page.addChild(name);
            controls.addChildren(
                    button(selected.isCollapsed() ? "groups.expand" : "groups.collapse", selected.isCollapsed() ? "EXPAND" : "COLLAPSE",
                            () -> host.change(() -> ProductionPlanner.setGroupCollapsed(host.line(), selected, !selected.isCollapsed()))),
                    button("ui.move_group", "MOVE TO GROUP", () -> host.show(p -> destinations(host, p, selected, destination -> {
                        host.change(() -> ProductionPlanner.moveGroup(host.line(), selected, destination, Integer.MAX_VALUE));
                        host.back();
                    }))),
                    button(Component.literal("↑"), () -> reorderGroup(host, selected, -1)),
                    button(Component.literal("↓"), () -> reorderGroup(host, selected, 1)),
                    button("groups.delete", "DELETE", () -> {
                        host.change(() -> ProductionPlanner.removeGroup(host.line(), selected));
                        host.back();
                    }));
        }
        page.addChild(section().addChild(controls));
        var children = new ObjectArrayList<Group>(host.line().getGroups().size());
        int parentId = selected == null ? 0 : selected.getId();
        for (var group : host.line().getGroups()) if (group.getParentId() == parentId) children.add(group);
        page.addChild(list("groups", 2 * ROW, children::size, i -> {
            var group = children.get(i);
            var open = button(Component.literal(group.getDisplayName(host.line())), () -> host.show(p -> groups(host, p, group)));
            open.layout(l -> l.flex(1));
            return section().addChild(UIElement.centeredRow(ROW).addChildren(open,
                    button(Component.literal("↑"), () -> reorderGroup(host, group, -1)),
                    button(Component.literal("↓"), () -> reorderGroup(host, group, 1))));
        }));
    }

    private static void reorderGroup(PlannerHost host, Group group, int direction) {
        int index = 0;
        for (var candidate : host.line().getGroups()) {
            if (candidate == group) break;
            if (candidate.getParentId() == group.getParentId()) index++;
        }
        int position = index + direction;
        host.change(() -> ProductionPlanner.moveGroup(host.line(), group, group.getParent(host.line()), position));
    }

    private static void recipes(PlannerHost host, UIElement page, Group group) {
        host.heading(page, text("groups.recipes", "RECIPES").getString());
        var entries = host.line().getEntries();
        page.addChild(list("group.recipes", 2 * ROW, entries::size, i -> {
            var entry = entries.get(i);
            var recipe = entry.getRecipe();
            var name = label(recipe == null ? entry.getRecipeId().toString() : host.session.recipeName(recipe));
            name.layout(l -> l.flex(1));
            return section().addChildren(UIElement.centeredRow(ROW).addChildren(icon(firstOutput(recipe), b -> host.stackClick(firstOutput(recipe), b)), name,
                    button("groups.move_here", "MOVE HERE", () -> host.change(() -> ProductionPlanner.setEntryGroup(host.line(), entry, group)))
                            .clientDisabled(() -> entry.getGroupId() == (group == null ? 0 : group.getId())),
                    button(Component.literal("↑"), () -> host.change(() -> ProductionPlanner.moveEntry(host.line(), entry, i - 1))),
                    button(Component.literal("↓"), () -> host.change(() -> ProductionPlanner.moveEntry(host.line(), entry, i + 1)))),
                    label(host.line().getEntryGroupName(entry)));
        }));
    }

    static void assignEntry(PlannerHost host, UIElement page, Entry entry) {
        destinations(host, page, null, group -> {
            host.change(() -> ProductionPlanner.setEntryGroup(host.line(), entry, group));
            host.back();
        });
    }

    private static void destinations(PlannerHost host, UIElement page, Group moving, Consumer<Group> choose) {
        host.heading(page, text("groups.title", "Groups and Links").getString());
        page.addChild(button("groups.root", "Root", () -> choose.accept(null)));
        var groups = new ObjectArrayList<Group>(host.line().getGroups().size());
        for (var candidate : host.line().getGroups()) {
            boolean allowed = true;
            for (var parent = candidate; parent != null; parent = parent.getParent(host.line())) {
                if (parent == moving) {
                    allowed = false;
                    break;
                }
            }
            if (allowed) groups.add(candidate);
        }
        page.addChild(list("group.destinations", ROW, groups::size,
                i -> button(Component.literal(groups.get(i).getDisplayName(host.line())), () -> choose.accept(groups.get(i)))));
    }

    private static void links(PlannerHost host, UIElement page, Group group) {
        host.heading(page, text("groups.links", "LINKS").getString());
        var candidates = ProductionPlanner.getGroupLinkCandidates(host.line(), group);
        page.addChild(list("group.links", ROW, candidates::size, i -> {
            var stack = candidates.get(i);
            var mode = host.line().getLinkMode(group, stack);
            var name = label(stack.getName());
            name.layout(l -> l.flex(1));
            return UIElement.centeredRow(ROW).addChildren(icon(stack, b -> host.stackClick(stack, b)), name,
                    button(text("groups." + mode.name().toLowerCase(Locale.ROOT), mode.name()), () -> host.change(() -> {
                        boolean rebalance = host.line().isBalanceEnabled();
                        ProductionPlanner.setGroupLinkMode(host.line(), group, stack, group == null ? mode.nextRoot() : mode.nextGroup());
                        if (rebalance) ProductionPlanner.balanceLine(host.line());
                    })));
        }));
    }
}
