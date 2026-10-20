package com.gtocore.integration.emi.planner;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.DecimalField;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.VirtualList;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.emi.emi.planner.ProductionPlanner;
import dev.emi.emi.planner.ProductionPlanner.RecipeChangeKind;
import dev.emi.emi.planner.ProductionPlanner.TargetMode;
import dev.emi.emi.planner.compat.gto.GtoCapabilityAudit;
import dev.emi.emi.screen.ProductionPlannerScreen;
import dev.emi.emi.screen.ProductionPlannerScreen.Flow;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static com.gtocore.integration.emi.planner.PlannerWidgets.*;

final class PlannerReports {

    private PlannerReports() {}

    static UIElement flows(PlannerHost host, String key, String english, List<Flow> flows, @Nullable TargetMode mode) {
        var list = new VirtualList(1, ROW, UISizes.GAP, flows::size, i -> {
            var flow = flows.get(i);
            var name = label(host.session.formatReportRate(flow.stack, flow.displayValue, flow.approximate));
            name.layout(l -> l.flex(1));
            return UIElement.centeredRow(ROW).addChildren(icon(flow.stack, b -> host.flowClick(flow, mode, b)), name);
        });
        var scroll = new ScrollerView("planner." + key, LayoutStyle.AUTO, 2 * ROW).setResizable(false).addScrollViewChild(list);
        var title = text(key + "_prefix", english + "/").copy().append(ProductionPlannerScreen.displaySuffix());
        return section().addChildren(label(title), scroll).layout(l -> l.width(0).flex(1));
    }

    static UIElement targets(PlannerHost host) {
        var panel = section();
        var controls = wrap().addChildren(label(text("targets", "Targets:")),
                button("balance", "BALANCE", () -> host.change(() -> {
                    host.status = ProductionPlanner.balanceLine(host.line()).message();
                })),
                button("clear", "CLEAR", () -> host.change(() -> ProductionPlanner.clearBalanceTarget(host.line()))));
        var power = host.session.calculatePower(host.line());
        controls.addChild(label(new StringBuilder(text("power", "Power").getString()).append(": ")
                .append(host.session.formatSummaryPower(power.averageEUt())).append(power.unknownEntries() > 0 ? " + ?" : "").toString()));
        panel.addChild(controls);
        var targets = host.line().getTargets();
        if (targets.isEmpty()) {
            panel.addChild(label(text("summary.goal_hint", "Click an output to set a target; Ctrl+click an input to set an input target")));
        } else {
            var list = new VirtualList(1, ROW, UISizes.GAP, targets::size, i -> {
                var target = targets.get(i);
                double unit = ProductionPlannerScreen.displayMultiplier();
                var rate = DecimalField.of(UISizes.CONTENT_WIDTH, () -> target.getRate() * unit,
                        value -> host.change(() -> ProductionPlanner.setTargetRate(host.line(), target, value / unit)),
                        0.000001 * unit, 1.0e12 * unit, 0.000001 * unit);
                var name = label(target.getStack().getName());
                name.layout(l -> l.flex(1));
                return UIElement.centeredRow(ROW).addChildren(icon(target.getStack(), b -> {
                    if (b == 0 && Screen.hasControlDown()) host.change(() -> ProductionPlanner.toggleTargetMode(host.line(), target));
                    else host.stackClick(target.getStack(), b);
                }), name, button(Component.literal(target.getMode().label()), () -> host.change(() -> ProductionPlanner.toggleTargetMode(host.line(), target))), rate,
                        button(Component.literal("×"), () -> host.change(() -> ProductionPlanner.removeBalanceTarget(host.line(), target))));
            });
            panel.addChild(new ScrollerView("planner.targets", LayoutStyle.AUTO, Math.min(3, targets.size()) * (ROW + UISizes.GAP))
                    .setResizable(false).addScrollViewChild(list));
        }
        if (!host.line().getBalanceMessage().isBlank()) panel.addChild(label(host.line().getBalanceMessage()));
        return panel;
    }

    static void report(PlannerHost host, UIElement page, int tab) {
        host.heading(page, text("report.title", "Line Report").getString());
        page.addChild(section().addChild(wrap().addChildren(
                button("report.machines", "MACHINES", () -> host.replace(p -> report(host, p, 0))).setSelected(() -> tab == 0),
                button("report.flows", "FLOWS", () -> host.replace(p -> report(host, p, 1))).setSelected(() -> tab == 1),
                button("startup.title", "STARTUP / PRIMING", () -> host.replace(p -> report(host, p, 2))).setSelected(() -> tab == 2),
                button("report.copy", "COPY", () -> {
                    Minecraft.getInstance().keyboardHandler.setClipboard(host.session.buildSummaryText(host.line()));
                    host.status = text("transfer.report_copied", "Line Report copied to clipboard").getString();
                    host.dirty = true;
                }),
                button("report.save", "SAVE", () -> {
                    var path = ProductionPlanner.writeBuildSummaryExportFile(host.session.buildSummaryText(host.line()));
                    host.status = path.isBlank() ? text("transfer.report_save_failed", "Could not save Line Report; use COPY instead").getString() : path;
                    host.dirty = true;
                }))));
        if (tab == 0) {
            var rows = host.session.collectBuildSummary(host.line());
            page.addChild(list("report.machines", 2 * ROW, rows::size, i -> {
                var row = rows.get(i);
                var name = label(new StringBuilder().append(row.machines()).append("× ").append(row.machineName()).toString());
                name.layout(l -> l.flex(1));
                var detail = new StringBuilder(row.voltageName()).append(" | P ").append(row.parallel()).append(" | ").append(row.config())
                        .append(" | ").append(host.session.formatSummaryPower(row.averageEUt())).append(row.unknownPower() ? " + ?" : "")
                        .append(row.provisional() ? " *" : "").toString();
                return section().addChildren(UIElement.centeredRow(ROW).addChildren(icon(row.icon(), b -> host.stackClick(row.icon(), b)), name), label(detail));
            }));
        } else if (tab == 1) {
            var rows = host.session.collectFlowSummary(host.line(), host.session.calculate(host.line()));
            page.addChild(list("report.flows", ROW, rows::size, i -> {
                var row = rows.get(i);
                return UIElement.centeredRow(ROW).addChildren(icon(row.stack(), b -> host.stackClick(row.stack(), b)),
                        label(new StringBuilder(row.section()).append(" | ").append(row.stack().getName().getString()).append(" | ")
                                .append(host.session.formatReportRate(row.stack(), row.rate(), row.approximate())).toString()).layout(l -> l.flex(1)));
            }));
        } else {
            var rows = ProductionPlanner.getStartupResources(host.line());
            page.addChild(list("report.startup", 2 * ROW, rows::size, i -> {
                var row = rows.get(i);
                var detail = new StringBuilder(host.session.startupRoleLabel(row.role())).append(" | MIN ")
                        .append(host.session.formatStartupAmount(row.stack(), row.minimum(), row.approximate())).append(" | BUF ")
                        .append(host.session.formatStartupAmount(row.stack(), row.recommended(), row.approximate())).toString();
                return section().addChildren(UIElement.centeredRow(ROW).addChildren(icon(row.stack(), b -> host.stackClick(row.stack(), b)),
                        label(row.stack().getName()).layout(l -> l.flex(1)), label(detail)), label(host.session.startupRoleDescription(row.role())));
            }));
        }
    }

    static void changes(PlannerHost host, UIElement page) {
        host.heading(page, text("migration.title", "Recipe Changes").getString());
        var changes = ProductionPlanner.getRecipeChanges(host.line());
        page.addChild(button("migration.accept_all", "ACCEPT ALL", () -> host.change(() -> ProductionPlanner.acceptAllRecipeChanges(host.line()))));
        page.addChild(button("migration.line_impact.title", "LINE IMPACT", () -> host.show(p -> textPage(host, p, text("migration.line_impact.title", "LINE IMPACT").getString(), ProductionPlanner.getLineRecipeChangeImpact(host.line())))));
        if (changes.isEmpty()) page.addChild(label(text("migration.none", "No recipe changes detected")));
        page.addChild(list("changes", 2 * ROW, changes::size, i -> {
            var change = changes.get(i);
            var name = label(change.recipeName());
            name.layout(l -> l.flex(1));
            return section().addChildren(UIElement.centeredRow(ROW).addChildren(name,
                    button("ui.details", "DETAILS", () -> host.show(p -> textPage(host, p, change.recipeName(), change.details()))),
                    button("migration.impact.label", "IMPACT", () -> host.show(p -> textPage(host, p, change.recipeName(), ProductionPlanner.getRecipeChangeImpact(host.line(), change)))),
                    button(change.isMissing() ? "ui.remove" : "footer.apply_short", change.isMissing() ? "REMOVE" : "ACCEPT", () -> host.change(() -> {
                        if (change.kind() == RecipeChangeKind.MISSING) ProductionPlanner.removeEntry(host.line(), change.entry());
                        else ProductionPlanner.acceptRecipeChange(host.line(), change.entry());
                    }))), label(change.recipeId()));
        }));
    }

    static void textPage(PlannerHost host, UIElement page, String title, List<String> lines) {
        host.heading(page, title);
        page.addChild(list("text", ROW, lines::size, i -> label(lines.get(i))));
    }

    static Consumer<UIElement> audit(PlannerHost host) {
        var result = GtoCapabilityAudit.run();
        var lines = List.of(result.report().split("\\R"));
        return page -> {
            host.heading(page, result.shortStatus());
            page.addChild(button("report.copy", "COPY", () -> Minecraft.getInstance().keyboardHandler.setClipboard(result.report())));
            page.addChild(list("audit", ROW, lines::size, i -> label(lines.get(i))));
        };
    }
}
