package com.gtocore.integration.emi.planner;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.DecimalField;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.VirtualList;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.chat.Component;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.planner.ProductionPlanner;
import dev.emi.emi.planner.ProductionPlanner.Entry;
import dev.emi.emi.planner.ProductionPlanner.MachineProfile;
import dev.emi.emi.planner.ProductionPlanner.MachineSettingType;
import dev.emi.emi.screen.ProductionPlannerScreen;
import dev.emi.emi.screen.ProductionPlannerScreen.RateStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.gtocore.integration.emi.planner.PlannerWidgets.*;

final class PlannerEntries {

    private PlannerEntries() {}

    static void entry(PlannerHost host, UIElement page, Entry entry) {
        var recipe = entry.getRecipe();
        host.heading(page, recipe == null ? entry.getRecipeId().toString() : host.session.recipeName(recipe));
        var form = Form.page();
        var scroll = ScrollerView.page("planner.entry", LayoutStyle.AUTO).addScrollViewChild(form);
        scroll.getLayoutStyle().height(0).flex(1);
        page.addChild(scroll);
        form.addChild(section().addChild(wrap().addChildren(
                button(Component.literal(entry.getMachineProfileName()), () -> host.show(new Machines(host, entry))).layout(l -> l.width(UISizes.CONTENT_WIDTH)),
                button("mode.auto", "AUTO", () -> host.change(() -> {
                    if (host.line().isBalanceEnabled()) ProductionPlanner.disableBalance(host.line());
                    if (!ProductionPlanner.setAutomatic(entry, true)) host.status = text("tooltip.duration_set_first", "Set recipe duration before enabling AUTO").getString();
                })).setSelected(entry::isAutomatic),
                button("mode.manual", "MANUAL", () -> host.change(() -> {
                    ProductionPlanner.disableBalance(host.line());
                    ProductionPlanner.setAutomatic(entry, false);
                })).setSelected(() -> !entry.isAutomatic()))));
        form.addChild(titled("header.mach", "Machines", wrap().addChildren(
                NumberField.ofInt(UISizes.CONTENT_WIDTH, entry::getMachines, n -> host.change(() -> ProductionPlanner.setMachines(entry, n)), 1, 1_000_000_000),
                button("common.fixed", "FIXED", () -> host.change(() -> ProductionPlanner.setMachinesFixed(entry, !entry.isMachinesFixed()))).setSelected(entry::isMachinesFixed))));
        form.addChild(titled("header.par", "Parallel", wrap().addChildren(
                NumberField.ofInt(UISizes.CONTENT_WIDTH, entry::getParallel, n -> host.change(() -> ProductionPlanner.setParallel(entry, n)),
                        () -> 1, () -> {
                            int limit = entry.getConfiguredMaxParallel();
                            return limit > 0 ? Math.min(limit, 1_000_000_000) : 1_000_000_000;
                        }),
                button("common.fixed", "FIXED", () -> host.change(() -> ProductionPlanner.setParallelFixed(entry, !entry.isParallelFixed()))).setSelected(entry::isParallelFixed))));
        form.addChild(titled("header.volt", "Voltage", wrap().addChildren(
                button(Component.literal(entry.getVoltageName()), () -> host.show(p -> voltage(host, p, entry)))
                        .clientDisabled(() -> entry.isVoltageFixedByMachine() || entry.getRecipeEUt() <= 0),
                button("ui.reset", "RESET", () -> host.change(() -> ProductionPlanner.resetVoltageToLine(host.line(), entry)))
                        .clientDisabled(entry::isVoltageFixedByMachine),
                button(Component.literal(entry.getOcDisplayLabel()), () -> host.change(() -> ProductionPlanner.cycleOcMode(entry, 1))))));
        double unit = ProductionPlannerScreen.displayMultiplier();
        form.addChild(titled("header.rate", "Rate", DecimalField.of(UISizes.CONTENT_WIDTH,
                () -> host.line().getEffectiveRate(entry) * unit,
                value -> host.change(() -> ProductionPlanner.setRate(entry, value / unit)), 0.01 * unit, 1.0e9 * unit, 0.000001 * unit)
                .clientDisabled(() -> entry.isAutomatic() || host.line().isBalanceEnabled())));
        form.addChild(titled("edit.duration_seconds", "Duration (seconds)", wrap().addChildren(
                DecimalField.of(UISizes.CONTENT_WIDTH, entry::getDurationSeconds,
                        value -> host.change(() -> {
                            ProductionPlanner.setDurationOverrideSeconds(entry, value);
                            if (entry.getDurationTicks() <= 0) ProductionPlanner.setAutomatic(entry, false);
                        }), 0, 1.0e9, 0.00000005),
                button("ui.reset", "RESET", () -> host.change(() -> {
                    ProductionPlanner.clearDurationOverride(entry);
                    if (entry.getDurationTicks() <= 0) ProductionPlanner.setAutomatic(entry, false);
                })))));
        var profile = entry.getMachineProfile();
        if (profile.coilEfficiencyPerTier() > 0) {
            form.addChild(titled("config.coil", "Coil", button(Component.literal(entry.getCoilName()),
                    () -> host.show(p -> coils(host, p, entry)))));
        }
        for (var spec : ProductionPlanner.getMachineSettingSpecs(entry)) {
            var controls = wrap();
            if (spec.type() == MachineSettingType.INTEGER) {
                controls.addChild(NumberField.ofInt(UISizes.CONTENT_WIDTH, () -> entry.getMachineSettingValue(spec),
                        value -> host.change(() -> ProductionPlanner.setMachineSetting(entry, spec, value)), spec.minValue(), spec.maxValue())
                        .setSteps(spec.step(), 8L * spec.step(), 64L * spec.step(), 512L * spec.step()));
            } else {
                controls.addChildren(button(Component.literal("−"), () -> host.change(() -> ProductionPlanner.cycleMachineSetting(entry, spec, -1))),
                        icon(spec.choiceIcon(entry.getMachineSettingValue(spec)), b -> host.change(() -> ProductionPlanner.cycleMachineSetting(entry, spec, b == 1 ? -1 : 1))),
                        button(Component.literal(entry.getMachineSettingDisplayValue(spec)),
                                () -> host.change(() -> ProductionPlanner.cycleMachineSetting(entry, spec, 1))),
                        button(Component.literal("+"), () -> host.change(() -> ProductionPlanner.cycleMachineSetting(entry, spec, 1))));
            }
            var title = spec.labelKey().isBlank() ? Component.literal(spec.englishLabel()) : text(spec.labelKey(), spec.englishLabel());
            var setting = section().addChildren(label(title), controls);
            var help = ProductionPlanner.getMachineSettingDetailLines(entry, spec);
            if (!help.isEmpty()) {
                var tooltips = new Component[help.size()];
                for (int i = 0; i < tooltips.length; i++) tooltips[i] = Component.literal(help.get(i));
                setting.tooltips(tooltips);
            }
            form.addChild(setting);
        }
        var actions = wrap().addChildren(
                button("groups.title", "Groups and Links", () -> host.show(p -> PlannerGroups.assignEntry(host, p, entry))),
                button("header.recipe", "Recipe", () -> { if (recipe != null) EmiApi.displayRecipe(recipe); }),
                button("ui.replace", "REPLACE", () -> host.session.openRecipeReplacement(host.line(), entry)),
                button("ui.remove", "REMOVE", () -> {
                    host.change(() -> ProductionPlanner.removeEntry(host.line(), entry));
                    host.back();
                }));
        if (recipe != null) {
            var output = firstOutput(recipe);
            if (!output.isEmpty() && recipe.getId() != null) {
                var mode = ProductionPlanner.getRecipePathMode(output, recipe.getId());
                actions.addChild(button(Component.literal(mode.label()),
                        () -> host.change(() -> ProductionPlanner.cycleRecipePathMode(output, recipe.getId()))));
            }
            form.addChild(titled("migration.detail.inputs", "Inputs", ingredients(host, recipe, recipe.getInputs(), ProductionPlanner.TargetMode.INPUT)));
            form.addChild(titled("migration.detail.outputs", "Outputs", ingredients(host, recipe, recipe.getOutputs(), ProductionPlanner.TargetMode.OUTPUT)));
            double rate = host.line().getEffectiveRate(entry);
            form.addChild(rateRows(host, "header.inputs_prefix", "Inputs/", host.session.recipeInputs(recipe, rate), ProductionPlanner.TargetMode.INPUT));
            form.addChild(rateRows(host, "header.outputs_prefix", "Outputs/", host.session.recipeOutputs(recipe, rate), ProductionPlanner.TargetMode.OUTPUT));
            var sizing = entry.getMachineSizing(rate);
            var metrics = section().addChildren(
                    label(text("tooltip.duration", "Duration").copy().append(": ").append(Double.toString(entry.getProcessedDurationSeconds())).append(" s")),
                    label(text("report.average_power", "Average power").copy().append(": ")
                            .append(entry.getRecipeEUt() <= 0 ? "?" : host.session.formatSummaryPower(entry.getAveragePowerEUt(rate)))));
            if (sizing.available()) {
                metrics.addChildren(
                        label(text("sizing.required_parallel", "Required effective parallel").copy().append(": ").append(Double.toString(sizing.requiredEffectiveParallel()))),
                        label(text("sizing.recommended", "Recommended setup").copy().append(": ").append(Integer.toString(sizing.machines()))
                                .append(" × ").append(Integer.toString(sizing.parallel()))),
                        label(text("sizing.installed_capacity", "Installed capacity").copy().append(": ")
                                .append(host.session.formatDisplayRate(sizing.capacityRate())).append("/").append(ProductionPlannerScreen.displaySuffix())),
                        label(text(sizing.sufficient() ? "sizing.headroom" : "sizing.shortfall", sizing.sufficient() ? "Headroom" : "CAPACITY SHORTFALL").copy()
                                .append(": ").append(Double.toString(sizing.sufficient() ? sizing.headroomPercent() : sizing.shortfallPercent())).append("%")),
                        label(sizing.note()));
            } else metrics.addChild(label(text("sizing.unavailable_row", "Machine sizing unavailable for this row")));
            form.addChild(metrics);
        }
        form.addChild(section().addChild(actions));
    }

    private static UIElement rateRows(PlannerHost host, String key, String english, List<RateStack> rates, ProductionPlanner.TargetMode mode) {
        var rows = new VirtualList(1, ROW, UISizes.GAP, rates::size, i -> {
            var rate = rates.get(i);
            return UIElement.centeredRow(ROW).addChildren(icon(rate.stack, b -> host.targetClick(rate.stack, mode, b)),
                    label(rate.stack.getName()).layout(l -> l.flex(1)),
                    label(host.session.formatReportRate(rate.stack, rate.amount, rate.approximate)).layout(l -> l.flex(1)));
        });
        var scroll = new ScrollerView("planner.entry." + key, LayoutStyle.AUTO, Math.max(1, Math.min(4, rates.size())) * (ROW + UISizes.GAP))
                .setResizable(false).addScrollViewChild(rows);
        return section().addChildren(label(text(key, english).copy().append(ProductionPlannerScreen.displaySuffix())), scroll);
    }

    private static UIElement ingredients(PlannerHost host, dev.emi.emi.api.recipe.EmiRecipe recipe, List<? extends EmiIngredient> ingredients, ProductionPlanner.TargetMode mode) {
        var row = wrap();
        for (var ingredient : ingredients) {
            if (ingredient == null || ingredient.isEmpty()) continue;
            boolean targetFlow;
            if (mode == ProductionPlanner.TargetMode.INPUT) {
                var role = ProductionPlanner.getInputResourceRole(recipe, ingredient);
                targetFlow = role == ProductionPlanner.ResourceRole.CONSUMED || role == ProductionPlanner.ResourceRole.CONTAINER;
            } else {
                targetFlow = ingredient instanceof dev.emi.emi.api.stack.EmiStack stack && !ProductionPlanner.isStartupOnlyOutput(recipe, stack);
            }
            row.addChild(icon(ingredient, button -> {
                var stacks = ingredient.getEmiStacks();
                if (!stacks.isEmpty()) {
                    if (targetFlow) host.targetClick(stacks.getFirst(), mode, button);
                    else host.stackClick(stacks.getFirst(), button);
                }
            }));
        }
        return section().addChild(row);
    }

    static void voltage(PlannerHost host, UIElement page, Entry entry) {
        host.heading(page, text("header.volt", "Voltage").getString());
        if (entry == null) page.addChild(button("footer.recipe_min", "Recipe minimum", () -> {
            host.change(() -> ProductionPlanner.setLineStandardVoltage(host.line(), -1));
            host.back();
        }));
        int minimum = entry == null ? 0 : Math.max(0, entry.getRecipeTier());
        page.addChild(list("voltage", ROW, () -> ProductionPlanner.maxVoltageTier() - minimum + 1, index -> {
            int tier = index + minimum;
            return button(Component.literal(ProductionPlanner.voltageTierName(tier)), () -> {
                host.change(() -> {
                    if (entry == null) ProductionPlanner.setLineStandardVoltage(host.line(), tier);
                    else ProductionPlanner.setVoltageTier(entry, tier);
                });
                host.back();
            });
        }));
    }

    private static void coils(PlannerHost host, UIElement page, Entry entry) {
        host.heading(page, text("config.coil", "Coil").getString());
        page.addChild(list("coils", ROW, () -> ProductionPlanner.maxCoilTier() + 1, tier -> button(Component.literal(ProductionPlanner.coilTierName(tier)), () -> {
            host.change(() -> ProductionPlanner.setCoilTier(entry, tier));
            host.back();
        })));
    }

    private static final class Machines implements Consumer<UIElement> {

        private final PlannerHost host;
        private final Entry entry;
        private final ObjectArrayList<MachineProfile> filtered = new ObjectArrayList<>();
        private List<MachineProfile> profiles = Collections.emptyList();
        private final ObjectArrayList<String> searchTexts = new ObjectArrayList<>();
        private String query = "";
        private VirtualList list;

        private Machines(PlannerHost host, Entry entry) {
            this.host = host;
            this.entry = entry;
        }

        private void filter() {
            filtered.clear();
            var needle = query.toLowerCase(Locale.ROOT);
            filtered.ensureCapacity(profiles.size());
            for (int i = 0; i < profiles.size(); i++) {
                if (needle.isBlank() || searchTexts.get(i).contains(needle)) filtered.add(profiles.get(i));
            }
            if (list != null) list.refresh();
        }

        @Override
        public void accept(UIElement page) {
            profiles = ProductionPlanner.getCompatibleMachineProfiles(entry);
            searchTexts.clear();
            searchTexts.ensureCapacity(profiles.size());
            for (var profile : profiles) {
                searchTexts.add(new StringBuilder(profile.displayName()).append(' ').append(profile.id()).toString().toLowerCase(Locale.ROOT));
            }
            host.heading(page, text("header.machine", "Machine").getString());
            page.addChild(TextField.of(LayoutStyle.AUTO, () -> query, value -> {
                query = value;
                filter();
            }).setRightClickClear(true));
            filter();
            list = new VirtualList(1, 2 * ROW, UISizes.GAP, filtered::size, index -> {
                var profile = filtered.get(index);
                var name = button(Component.literal(profile.displayName()), () -> {
                    host.change(() -> ProductionPlanner.setMachineProfile(entry, profile.id()));
                    host.back();
                });
                name.layout(l -> l.flex(1));
                return section().addChildren(UIElement.centeredRow(ROW).addChildren(icon(profile.icon(), b -> host.stackClick(profile.icon(), b)), name,
                        button(Component.literal(ProductionPlanner.isPreferredMachine(entry, profile.id()) ? "★" : "☆"),
                                () -> host.change(() -> ProductionPlanner.togglePreferredMachine(entry, profile.id())))),
                        label(host.session.localizedProfileDescription(profile)));
            });
            page.addChild(PlannerWidgets.list("machines", list));
        }
    }
}
