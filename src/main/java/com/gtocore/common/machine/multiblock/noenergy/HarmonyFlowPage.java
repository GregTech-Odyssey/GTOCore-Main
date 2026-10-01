package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.issue.DiagnosisResult;
import com.gregtechceu.gtceu.api.machine.issue.MachineDiagnosis;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemView;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.flow.FlowChart;
import com.gregtechceu.gtceu.uipro.flow.FlowNode;
import com.gregtechceu.gtceu.uipro.flow.FlowParts;
import com.gregtechceu.gtceu.uipro.flow.FlowState;
import com.gregtechceu.gtceu.uipro.flow.ThrottledStatus;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.flow.IssueLine;
import com.gregtechceu.gtceu.uiwidgets.flow.IssueView;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeIssue;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DataGeneratorScanned
public final class HarmonyFlowPage {

    private static final int SIDE_WIDTH = 104;
    private static final int CORE_WIDTH = 124;
    private static final int REFRESH_TICKS = 20;
    private static final int PROGRESS_TICKS = 4;

    @RegisterLanguage(cn = "氢储量", en = "Hydrogen Reserve")
    private static final String LANG_HYDROGEN = "gtocore.machine.eye_of_harmony.flow.hydrogen";
    @RegisterLanguage(cn = "氦储量", en = "Helium Reserve")
    private static final String LANG_HELIUM = "gtocore.machine.eye_of_harmony.flow.helium";
    @RegisterLanguage(cn = "物料输入", en = "Material Input")
    private static final String LANG_INPUT = "gtocore.machine.eye_of_harmony.flow.input";
    @RegisterLanguage(cn = "超频设定", en = "Overclock")
    private static final String LANG_OVERCLOCK = "gtocore.machine.eye_of_harmony.flow.overclock";
    @RegisterLanguage(cn = "宇宙模拟", en = "Cosmos Simulation")
    private static final String LANG_RECIPE = "gtocore.machine.eye_of_harmony.flow.recipe";
    @RegisterLanguage(cn = "无线电网", en = "Wireless Grid")
    private static final String LANG_ENERGY = "gtocore.machine.eye_of_harmony.flow.energy";
    @RegisterLanguage(cn = "模拟等级", en = "Simulation Tier")
    private static final String LANG_TIER = "gtocore.machine.eye_of_harmony.flow.tier";
    @RegisterLanguage(cn = "产物输出", en = "Product Output")
    private static final String LANG_OUTPUT = "gtocore.machine.eye_of_harmony.flow.output";

    @RegisterLanguage(cn = "状态", en = "State")
    private static final String LANG_STATE = "gtocore.machine.eye_of_harmony.flow.state";
    @RegisterLanguage(cn = "储量", en = "Stored")
    private static final String LANG_STOCK = "gtocore.machine.eye_of_harmony.flow.stock";
    @RegisterLanguage(cn = "可供", en = "Covers")
    private static final String LANG_RUNS = "gtocore.machine.eye_of_harmony.flow.runs";
    @RegisterLanguage(cn = "电路", en = "Circuit")
    private static final String LANG_CIRCUIT = "gtocore.machine.eye_of_harmony.flow.circuit";
    @RegisterLanguage(cn = "耗时", en = "Duration")
    private static final String LANG_DURATION = "gtocore.machine.eye_of_harmony.flow.duration";
    @RegisterLanguage(cn = "耗能倍率", en = "Energy")
    private static final String LANG_ENERGY_FACTOR = "gtocore.machine.eye_of_harmony.flow.energy_factor";
    @RegisterLanguage(cn = "最低耗能", en = "Minimum")
    private static final String LANG_STARTUP = "gtocore.machine.eye_of_harmony.flow.startup";
    @RegisterLanguage(cn = "进度", en = "Progress")
    private static final String LANG_PROGRESS = "gtocore.machine.eye_of_harmony.flow.progress";
    @RegisterLanguage(cn = "配方等级", en = "Recipe Tier")
    private static final String LANG_RECIPE_TIER = "gtocore.machine.eye_of_harmony.flow.recipe_tier";
    @RegisterLanguage(cn = "本次耗能", en = "This Run")
    private static final String LANG_RUN_ENERGY = "gtocore.machine.eye_of_harmony.flow.run_energy";
    @RegisterLanguage(cn = "所有者", en = "Owner")
    private static final String LANG_OWNER = "gtocore.machine.eye_of_harmony.flow.owner";
    @RegisterLanguage(cn = "储能", en = "Stored")
    private static final String LANG_STORED = "gtocore.machine.eye_of_harmony.flow.stored";
    @RegisterLanguage(cn = "升级进度", en = "Advance")
    private static final String LANG_ADVANCE = "gtocore.machine.eye_of_harmony.flow.advance";
    @RegisterLanguage(cn = "可运行", en = "Runs")
    private static final String LANG_ALLOWED = "gtocore.machine.eye_of_harmony.flow.allowed";
    @RegisterLanguage(cn = "输入单元", en = "Units")
    private static final String LANG_INPUT_UNITS = "gtocore.machine.eye_of_harmony.flow.input_units";
    @RegisterLanguage(cn = "输出单元", en = "Units")
    private static final String LANG_OUTPUT_UNITS = "gtocore.machine.eye_of_harmony.flow.output_units";

    @RegisterLanguage(cn = "%s 次", en = "%s runs")
    private static final String LANG_TIMES = "gtocore.machine.eye_of_harmony.flow.times";
    @RegisterLanguage(cn = "编程电路 %s", en = "Programmed %s")
    private static final String LANG_CIRCUIT_VALUE = "gtocore.machine.eye_of_harmony.flow.circuit_value";
    @RegisterLanguage(cn = "×1/%s", en = "×1/%s")
    private static final String LANG_FRACTION = "gtocore.machine.eye_of_harmony.flow.fraction";
    @RegisterLanguage(cn = "等级 %s", en = "Tier %s")
    private static final String LANG_TIER_VALUE = "gtocore.machine.eye_of_harmony.flow.tier_value";
    @RegisterLanguage(cn = "等级 ≤ %s", en = "Tier ≤ %s")
    private static final String LANG_TIER_MIN = "gtocore.machine.eye_of_harmony.flow.tier_min";

    @RegisterLanguage(cn = "充足", en = "Sufficient")
    private static final String LANG_FLUID_OK = "gtocore.machine.eye_of_harmony.flow.fluid_ok";
    @RegisterLanguage(cn = "不足", en = "Insufficient")
    private static final String LANG_FLUID_SHORT = "gtocore.machine.eye_of_harmony.flow.fluid_short";
    @RegisterLanguage(cn = "储量不低于 1,024 KB，可供下一次配方启动。", en = "The reserve holds at least 1,024 KB, enough for the next recipe start.")
    private static final String LANG_FLUID_OK_DESC = "gtocore.machine.eye_of_harmony.flow.fluid_ok.desc";
    @RegisterLanguage(cn = "储量低于 1,024 KB，配方无法启动。补充至输入仓后，每秒自动抽入。", en = "The reserve is below 1,024 KB; no recipe can start. Fluid added to the input hatches is drawn in every second.")
    private static final String LANG_FLUID_SHORT_DESC = "gtocore.machine.eye_of_harmony.flow.fluid_short.desc";
    @RegisterLanguage(cn = "每秒将输入仓中的全部%s抽入内部储量，储量没有上限。", en = "Every second, all %s in the input hatches is drawn into the internal reserve, which has no limit.")
    private static final String LANG_FLUID_RULE_DRAW = "gtocore.machine.eye_of_harmony.flow.fluid_rule_draw";
    @RegisterLanguage(cn = "每次启动配方从储量中扣除 1,024 KB。", en = "Each recipe start takes 1,024 KB from the reserve.")
    private static final String LANG_FLUID_RULE_COST = "gtocore.machine.eye_of_harmony.flow.fluid_rule_cost";
    @RegisterLanguage(cn = "当前储量：%s", en = "Current reserve: %s")
    private static final String LANG_FLUID_CURRENT = "gtocore.machine.eye_of_harmony.flow.fluid_current";

    @RegisterLanguage(cn = "未设置", en = "Not Set")
    private static final String LANG_OC_NONE = "gtocore.machine.eye_of_harmony.flow.oc_none";
    @RegisterLanguage(cn = "输入单元中没有编程电路 1~4，配方无法启动。", en = "No programmed circuit 1~4 is in the input units; no recipe can start.")
    private static final String LANG_OC_NONE_DESC = "gtocore.machine.eye_of_harmony.flow.oc_none.desc";
    @RegisterLanguage(cn = "不超频", en = "No Overclock")
    private static final String LANG_OC_ZERO = "gtocore.machine.eye_of_harmony.flow.oc_zero";
    @RegisterLanguage(cn = "超频 %s 次", en = "%s Overclocks")
    private static final String LANG_OC_COUNT = "gtocore.machine.eye_of_harmony.flow.oc_count";
    @RegisterLanguage(cn = "耗时与启动耗能按当前电路编号计算。", en = "Duration and startup energy follow the current circuit number.")
    private static final String LANG_OC_SET_DESC = "gtocore.machine.eye_of_harmony.flow.oc_set.desc";
    @RegisterLanguage(cn = "编程电路 1~4 对应超频 0~3 次；输入单元中同时存在多个时，取编号最大者。", en = "Programmed circuits 1~4 give 0~3 overclocks; if several are present in the input units, the highest number applies.")
    private static final String LANG_OC_RULE_CIRCUIT = "gtocore.machine.eye_of_harmony.flow.oc_rule_circuit";
    @RegisterLanguage(cn = "每次超频：耗时 ×1/2，启动耗能 ×8，平均功率 ×16。", en = "Each overclock: duration ×1/2, startup energy ×8, average power ×16.")
    private static final String LANG_OC_RULE_EFFECT = "gtocore.machine.eye_of_harmony.flow.oc_rule_effect";

    @RegisterLanguage(cn = "无所有者", en = "No Owner")
    private static final String LANG_NO_OWNER = "gtocore.machine.eye_of_harmony.flow.no_owner";
    @RegisterLanguage(cn = "机器没有所有者，无法连接无线电网。", en = "The machine has no owner and cannot reach a wireless grid.")
    private static final String LANG_NO_OWNER_DESC = "gtocore.machine.eye_of_harmony.flow.no_owner.desc";
    @RegisterLanguage(cn = "储能不足", en = "Insufficient")
    private static final String LANG_EU_SHORT = "gtocore.machine.eye_of_harmony.flow.eu_short";
    @RegisterLanguage(cn = "电网储能高于最低启动耗能；高等级配方倍率更大，所需能量更多。", en = "The grid holds more than the minimum startup energy; higher-tier recipes use a larger multiplier and need more.")
    private static final String LANG_EU_READY_DESC = "gtocore.machine.eye_of_harmony.flow.eu_ready.desc";
    @RegisterLanguage(cn = "配方启动时，一次性从所有者的无线电网扣除启动耗能；运行期间不再耗电。", en = "When a recipe starts, the startup energy is taken from the owner's wireless grid at once; nothing more is drawn while it runs.")
    private static final String LANG_EU_RULE_DRAW = "gtocore.machine.eye_of_harmony.flow.eu_rule_draw";
    @RegisterLanguage(cn = "启动耗能 = 5,277,655,810,867,200 EU × 2^(3 × 电路编号 − 1) × 配方倍率", en = "Startup energy = 5,277,655,810,867,200 EU × 2^(3 × circuit − 1) × recipe multiplier")
    private static final String LANG_EU_RULE_FORMULA = "gtocore.machine.eye_of_harmony.flow.eu_rule_formula";
    @RegisterLanguage(cn = "配方倍率 = (配方等级 − 1) × 4，至少为 1；等级 10 的配方为 36 倍。", en = "Recipe multiplier = (recipe tier − 1) × 4, at least 1; a tier 10 recipe is 36×.")
    private static final String LANG_EU_RULE_MULTIPLIER = "gtocore.machine.eye_of_harmony.flow.eu_rule_multiplier";
    @RegisterLanguage(cn = "电网储能须大于启动耗能，配方才能启动。", en = "The grid must hold more than the startup energy for a recipe to start.")
    private static final String LANG_EU_RULE_STRICT = "gtocore.machine.eye_of_harmony.flow.eu_rule_strict";
    @RegisterLanguage(cn = "当前电路下的最低启动耗能（配方倍率 1）：%s EU", en = "Minimum startup energy for the current circuit (multiplier 1): %s EU")
    private static final String LANG_EU_CURRENT = "gtocore.machine.eye_of_harmony.flow.eu_current";

    @RegisterLanguage(cn = "启动条件：机器有所有者，且已设置编程电路；", en = "Start conditions: the machine has an owner and a programmed circuit is set;")
    private static final String LANG_RECIPE_RULE_START = "gtocore.machine.eye_of_harmony.flow.recipe_rule_start";
    @RegisterLanguage(cn = "氢、氦储量各不低于 1,024 KB，无线电网储能大于启动耗能。", en = "hydrogen and helium reserves each hold at least 1,024 KB, and the wireless grid holds more than the startup energy.")
    private static final String LANG_RECIPE_RULE_START_2 = "gtocore.machine.eye_of_harmony.flow.recipe_rule_start_2";
    @RegisterLanguage(cn = "启动时扣除氢、氦各 1,024 KB，并从无线电网扣除启动耗能。", en = "At start, 1,024 KB each of hydrogen and helium are consumed and the startup energy is taken from the wireless grid.")
    private static final String LANG_RECIPE_RULE_COST = "gtocore.machine.eye_of_harmony.flow.recipe_rule_cost";
    @RegisterLanguage(cn = "只能运行等级不高于模拟等级的配方。", en = "Only recipes whose tier does not exceed the simulation tier can run.")
    private static final String LANG_RECIPE_RULE_TIER = "gtocore.machine.eye_of_harmony.flow.recipe_rule_tier";

    @RegisterLanguage(cn = "计数中", en = "Counting")
    private static final String LANG_TIER_COUNTING = "gtocore.machine.eye_of_harmony.flow.tier_counting";
    @RegisterLanguage(cn = "运行中的配方与模拟等级相同，已在启动时计入升级进度。", en = "The running recipe matches the simulation tier and was counted toward the advance when it started.")
    private static final String LANG_TIER_COUNTING_DESC = "gtocore.machine.eye_of_harmony.flow.tier_counting.desc";
    @RegisterLanguage(cn = "运行与模拟等级相同的配方以累计升级进度。", en = "Run recipes of the same tier as the simulation tier to build up the advance.")
    private static final String LANG_TIER_IDLE_DESC = "gtocore.machine.eye_of_harmony.flow.tier_idle.desc";
    @RegisterLanguage(cn = "每启动一次与模拟等级相同的配方，计数 1 次。", en = "Each start of a recipe at the simulation tier counts once.")
    private static final String LANG_TIER_RULE_COUNT = "gtocore.machine.eye_of_harmony.flow.tier_rule_count";
    @RegisterLanguage(cn = "累计 %s 次（17 + 4 × 等级）后，等级提升 1 级并重新计数。", en = "After %s counts (17 + 4 × tier), the tier rises by 1 and the count resets.")
    private static final String LANG_TIER_RULE_ADVANCE = "gtocore.machine.eye_of_harmony.flow.tier_rule_advance";
    @RegisterLanguage(cn = "启动更低等级的配方不计数。", en = "Starting a lower-tier recipe does not count.")
    private static final String LANG_TIER_RULE_HIGHER = "gtocore.machine.eye_of_harmony.flow.tier_rule_higher";
    @RegisterLanguage(cn = "距下一级还需 %s 次。", en = "%s more to the next tier.")
    private static final String LANG_TIER_REMAINING = "gtocore.machine.eye_of_harmony.flow.tier_remaining";

    @RegisterLanguage(cn = "已投料", en = "Loaded")
    private static final String LANG_INPUT_LOADED = "gtocore.machine.eye_of_harmony.flow.input_loaded";
    @RegisterLanguage(cn = "待匹配", en = "Waiting")
    private static final String LANG_INPUT_WAITING = "gtocore.machine.eye_of_harmony.flow.input_waiting";
    @RegisterLanguage(cn = "输入宇宙素及配方所需物品，匹配对应的宇宙模拟配方。", en = "Supply Cosmic Element and the items a recipe requires to match a cosmos simulation recipe.")
    private static final String LANG_INPUT_RULE = "gtocore.machine.eye_of_harmony.flow.input_rule";
    @RegisterLanguage(cn = "待产出", en = "Standby")
    private static final String LANG_OUTPUT_IDLE = "gtocore.machine.eye_of_harmony.flow.output_idle";
    @RegisterLanguage(cn = "结构中没有输出仓，产物将全部销毁。", en = "The structure has no output hatch; all products will be voided.")
    private static final String LANG_OUTPUT_VOID_ALL_DESC = "gtocore.machine.eye_of_harmony.flow.output_void_all_desc";

    private HarmonyFlowPage() {}

    static Widget create(HarmonyMachine machine, FancyMachineUIWidget window) {
        boolean remote = window.isRemote();
        var status = new Status(machine);
        var chart = new FlowChart(0, SIDE_WIDTH, CORE_WIDTH, SIDE_WIDTH);
        var hydrogen = fluidNode(chart, 0, remote, HarmonyMachine.HYDROGEN, LANG_HYDROGEN, status.hydrogen);
        var input = inputNode(chart, status);
        var helium = fluidNode(chart, 2, remote, HarmonyMachine.HELIUM, LANG_HELIUM, status.helium);
        var overclock = overclockNode(chart, status);
        var recipe = recipeNode(chart, machine, status);
        var energy = energyNode(chart, status);
        var tier = tierNode(chart, status);
        var output = outputNode(chart, status);
        chart.link(hydrogen, recipe);
        chart.link(input, recipe);
        chart.link(helium, recipe);
        chart.link(overclock, recipe);
        chart.link(energy, recipe);
        chart.link(recipe, tier).follow(tier);
        chart.link(recipe, output).follow(output);
        return UIElement.column(LayoutStyle.AUTO).addChild(chart.toView(window, false, machine.getDefinition().getId().toString()));
    }

    private static FlowNode fluidNode(FlowChart chart, int column, boolean remote, Fluid fluid, String title, Reserve reserve) {
        var node = chart.node(0, column).bindState(reserve::state).bindDetail(reserve::detail);
        var header = UIElement.centeredRow(UISizes.SLOT_SIZE);
        header.addChildren(FlowParts.fluidSlot(remote ? null : new FluidStack(fluid, 1)), TextLine.translatable(0, title).layout(l -> l.flex(1)));
        node.addChildren(header,
                new IssueLine(LayoutStyle.AUTO, null, reserve::view),
                ProgressBar.of(LayoutStyle.AUTO, Component.translatable(LANG_STOCK), UITheme.FLOW_CYAN_LIGHT, reserve::progress).currentOnly().setUnit("B"),
                StatusLine.of(LayoutStyle.AUTO, LANG_RUNS, reserve::runsText).bindLevel(reserve::runsLevel));
        return node;
    }

    private static FlowNode inputNode(FlowChart chart, Status status) {
        var node = chart.node(0, 1).bindState(status::inputState).bindDetail(status::inputDetail);
        node.addChildren(FlowParts.header(ItemView.of(GTMachines.ITEM_IMPORT_BUS[GTValues.LV].asStack()), LANG_INPUT),
                new IssueLine(LayoutStyle.AUTO, null, status::inputView),
                StatusLine.of(LayoutStyle.AUTO, LANG_INPUT_UNITS, status::inputUnitsText));
        return node;
    }

    private static FlowNode overclockNode(FlowChart chart, Status status) {
        var node = chart.node(1, 0).bindState(status::overclockState).bindDetail(status::overclockDetail);
        node.addChildren(FlowParts.header(ItemView.of(GTItems.PROGRAMMED_CIRCUIT.asStack()), LANG_OVERCLOCK),
                new IssueLine(LayoutStyle.AUTO, null, status::overclockView),
                StatusLine.of(LayoutStyle.AUTO, LANG_CIRCUIT, status::circuitText),
                StatusLine.of(LayoutStyle.AUTO, LANG_DURATION, status::durationText),
                StatusLine.of(LayoutStyle.AUTO, LANG_ENERGY_FACTOR, status::energyFactorText));
        return node;
    }

    private static FlowNode recipeNode(FlowChart chart, HarmonyMachine machine, Status status) {
        var node = chart.node(1, 1).bindState(status::recipeState).bindDetail(status::recipeDetail);
        node.addChildren(FlowParts.header(ItemView.of(machine.getDefinition().asStack()), LANG_RECIPE),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status::recipeView),
                ProgressBar.of(LayoutStyle.AUTO, Component.translatable(LANG_PROGRESS), UITheme.FLOW_CYAN_LIGHT, status::progress),
                StatusLine.of(LayoutStyle.AUTO, LANG_RECIPE_TIER, status::recipeTierText),
                StatusLine.of(LayoutStyle.AUTO, LANG_RUN_ENERGY, status::runEnergyText));
        return node;
    }

    private static FlowNode energyNode(FlowChart chart, Status status) {
        var node = chart.node(1, 2).bindState(status::energyState).bindDetail(status::energyDetail);
        node.addChildren(FlowParts.header(ItemView.of(FlowParts.energyIcon(node)), LANG_ENERGY),
                new IssueLine(LayoutStyle.AUTO, null, status::energyView),
                StatusLine.of(LayoutStyle.AUTO, LANG_OWNER, status::ownerText),
                StatusLine.of(LayoutStyle.AUTO, LANG_STORED, status::storedText).bindLevel(status::storedLevel),
                StatusLine.of(LayoutStyle.AUTO, LANG_STARTUP, status::startupText));
        return node;
    }

    private static FlowNode tierNode(FlowChart chart, Status status) {
        var node = chart.node(2, 0).bindState(status::tierState).bindDetail(status::tierDetail);
        node.addChildren(FlowParts.header(ItemView.of(new ItemStack(Items.EXPERIENCE_BOTTLE)), LANG_TIER),
                new IssueLine(LayoutStyle.AUTO, null, status::tierView),
                ProgressBar.of(LayoutStyle.AUTO, Component.translatable(LANG_ADVANCE), UITheme.STATUS_ONLINE, status::advance),
                StatusLine.of(LayoutStyle.AUTO, LANG_ALLOWED, status::allowedText));
        return node;
    }

    private static FlowNode outputNode(FlowChart chart, Status status) {
        var node = chart.node(2, 1).bindState(status::outputState).bindDetail(status::outputDetail);
        node.addChildren(FlowParts.header(ItemView.of(GTMachines.ITEM_EXPORT_BUS[GTValues.LV].asStack()), LANG_OUTPUT),
                new IssueLine(LayoutStyle.AUTO, null, status::outputView),
                StatusLine.of(LayoutStyle.AUTO, LANG_OUTPUT_UNITS, status::outputUnitsText));
        return node;
    }

    private static Component eu(BigInteger energy) {
        return Component.literal(FormattingUtil.formatNumberReadable(energy.doubleValue(), false, FormattingUtil.DECIMAL_FORMAT_1F, " EU"));
    }

    private static Component gray(Component component) {
        return component.copy().withStyle(ChatFormatting.GRAY);
    }

    private static IssueView labeled(IssueView view, Component label) {
        return new IssueView(view.issue(), label, view.stateOverride(), view.source());
    }

    private static final class Reserve {

        private final HarmonyMachine machine;
        private final boolean hydrogen;
        private final IdleReason shortage;
        private final Component fluidName;
        private final Status status;
        private IssueView view = RecipeIssue.OFFLINE.view();
        private List<Component> detail = Collections.emptyList();
        private ProgressBar.Progress progress = ProgressBar.Progress.EMPTY;
        private Component runsText = Status.NONE;
        private Level runsLevel = Level.NORMAL;
        private long shown = -1;

        private Reserve(HarmonyMachine machine, boolean hydrogen, Status status) {
            this.machine = machine;
            this.hydrogen = hydrogen;
            this.shortage = hydrogen ? IdleReason.HYDROGEN_RESERVE_SHORT : IdleReason.HELIUM_RESERVE_SHORT;
            this.status = status;
            this.fluidName = new FluidStack(hydrogen ? HarmonyMachine.HYDROGEN : HarmonyMachine.HELIUM, 1).getDisplayName();
        }

        long amount() {
            return hydrogen ? machine.getHydrogen() : machine.getHelium();
        }

        IssueView view() {
            status.update();
            return view;
        }

        FlowState state() {
            status.update();
            return view.state();
        }

        List<Component> detail() {
            status.update();
            return detail;
        }

        ProgressBar.Progress progress() {
            status.update();
            return progress;
        }

        Component runsText() {
            status.update();
            return runsText;
        }

        Level runsLevel() {
            status.update();
            return runsLevel;
        }

        private void refresh(boolean formed, DiagnosisResult result) {
            long amount = amount();
            boolean enough = !result.has(shortage.type());
            String description;
            if (!formed) {
                view = RecipeIssue.OFFLINE.view();
                description = null;
            } else if (enough) {
                view = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_FLUID_OK));
                description = LANG_FLUID_OK_DESC;
            } else {
                view = labeled(IssueView.of(result, shortage.type(), RecipeIssue.INPUT_SHORT.view()), Component.translatable(LANG_FLUID_SHORT));
                description = LANG_FLUID_SHORT_DESC;
            }
            if (amount != shown) {
                shown = amount;
                progress = new ProgressBar.Progress(amount / 1000, HarmonyMachine.FLUID_PER_RUN / 1000, 0);
            }
            runsText = formed ? Component.translatable(LANG_TIMES, FormattingUtil.formatNumbers(amount / HarmonyMachine.FLUID_PER_RUN)) : Status.NONE;
            runsLevel = !formed ? Level.NORMAL : enough ? Level.GOOD : Level.ERROR;
            detail = List.of(Component.translatable(hydrogen ? LANG_HYDROGEN : LANG_HELIUM),
                    gray(Component.translatable(LANG_FLUID_CURRENT, FormattingUtil.formatBuckets(amount))),
                    gray(Component.translatable(LANG_FLUID_RULE_DRAW, fluidName)),
                    gray(Component.translatable(LANG_FLUID_RULE_COST)),
                    FlowIssueViews.sentence(view, description));
        }
    }

    private static final class Status extends ThrottledStatus {

        private static final Component NONE = Component.literal("—");

        private final HarmonyMachine machine;
        private final Reserve hydrogen;
        private final Reserve helium;

        private boolean progressRefreshed;
        private int progressAt;
        private int shownProgress = -1, shownMaxProgress = -1;
        private int shownCount = -1, shownNeeded = -1;

        private ProgressBar.Progress progress = ProgressBar.Progress.EMPTY;
        private ProgressBar.Progress advance = ProgressBar.Progress.EMPTY;

        private IssueView inputView = RecipeIssue.OFFLINE.view();
        private List<Component> inputDetail = Collections.emptyList();
        private Component inputUnitsText = NONE;
        private IssueView outputView = RecipeIssue.OFFLINE.view();
        private List<Component> outputDetail = Collections.emptyList();
        private Component outputUnitsText = NONE;

        private IssueView overclockView = RecipeIssue.OFFLINE.view();
        private List<Component> overclockDetail = Collections.emptyList();
        private Component circuitText = NONE, durationText = NONE, energyFactorText = NONE, startupText = NONE;

        private IssueView recipeView = RecipeIssue.OFFLINE.view();
        private FlowState recipeState = FlowState.IDLE;
        private List<Component> recipeDetail = Collections.emptyList();
        private Component recipeTierText = NONE, runEnergyText = NONE;

        private IssueView energyView = RecipeIssue.OFFLINE.view();
        private List<Component> energyDetail = Collections.emptyList();
        private Component ownerText = NONE, storedText = NONE;
        private Level storedLevel = Level.NORMAL;

        private IssueView tierView = RecipeIssue.OFFLINE.view();
        private List<Component> tierDetail = Collections.emptyList();
        private Component allowedText = NONE;

        private Status(HarmonyMachine machine) {
            super(machine::getOffsetTimer, REFRESH_TICKS);
            this.machine = machine;
            this.hydrogen = new Reserve(machine, true, this);
            this.helium = new Reserve(machine, false, this);
        }

        IssueView inputView() {
            update();
            return inputView;
        }

        FlowState inputState() {
            update();
            return inputView.state();
        }

        List<Component> inputDetail() {
            update();
            return inputDetail;
        }

        Component inputUnitsText() {
            update();
            return inputUnitsText;
        }

        IssueView outputView() {
            update();
            return outputView;
        }

        FlowState outputState() {
            update();
            return outputView.state();
        }

        List<Component> outputDetail() {
            update();
            return outputDetail;
        }

        Component outputUnitsText() {
            update();
            return outputUnitsText;
        }

        IssueView overclockView() {
            update();
            return overclockView;
        }

        FlowState overclockState() {
            update();
            return overclockView.state();
        }

        List<Component> overclockDetail() {
            update();
            return overclockDetail;
        }

        Component circuitText() {
            update();
            return circuitText;
        }

        Component durationText() {
            update();
            return durationText;
        }

        Component energyFactorText() {
            update();
            return energyFactorText;
        }

        Component startupText() {
            update();
            return startupText;
        }

        IssueView recipeView() {
            update();
            return recipeView;
        }

        FlowState recipeState() {
            update();
            return recipeState;
        }

        List<Component> recipeDetail() {
            update();
            return recipeDetail;
        }

        ProgressBar.Progress progress() {
            update();
            return progress;
        }

        Component recipeTierText() {
            update();
            return recipeTierText;
        }

        Component runEnergyText() {
            update();
            return runEnergyText;
        }

        IssueView energyView() {
            update();
            return energyView;
        }

        FlowState energyState() {
            update();
            return energyView.state();
        }

        List<Component> energyDetail() {
            update();
            return energyDetail;
        }

        Component ownerText() {
            update();
            return ownerText;
        }

        Component storedText() {
            update();
            return storedText;
        }

        Level storedLevel() {
            update();
            return storedLevel;
        }

        IssueView tierView() {
            update();
            return tierView;
        }

        FlowState tierState() {
            update();
            return tierView.state();
        }

        List<Component> tierDetail() {
            update();
            return tierDetail;
        }

        ProgressBar.Progress advance() {
            update();
            return advance;
        }

        Component allowedText() {
            update();
            return allowedText;
        }

        @Override
        protected void onAccess(int now) {
            if (progressRefreshed && now >= progressAt && now - progressAt < PROGRESS_TICKS) return;
            progressRefreshed = true;
            progressAt = now;
            var logic = machine.getRecipeLogic();
            int value = 0, max = 0;
            if (logic.isActive() && logic.getMaxProgress() > 0) {
                value = logic.getProgress();
                max = logic.getMaxProgress();
            }
            if (value != shownProgress || max != shownMaxProgress) {
                shownProgress = value;
                shownMaxProgress = max;
                progress = max > 0 ? new ProgressBar.Progress(value, max, 0) : new ProgressBar.Progress(0, 1, 0);
            }
        }

        @Override
        protected void refresh(int now) {
            boolean formed = machine.isFormed();
            var logic = machine.getRecipeLogic();
            boolean working = logic.isWorking();
            var result = MachineDiagnosis.of(machine);
            var running = logic.isActive() ? logic.getLastRecipe() : null;
            var target = result.recipe();
            boolean hasRecipe = running != null || target != null;
            int recipeTier = running != null ? running.data.getInt(GTORecipeDataKeys.TIER) : target != null ? target.data.getInt(GTORecipeDataKeys.TIER) : 0;
            int oc = machine.getOverclock();
            boolean owned = machine.hasOwner();
            var container = owned ? machine.getWirelessEnergyContainer() : null;
            BigInteger stored = container != null ? container.getStorage() : BigInteger.ZERO;
            BigInteger minimum = machine.getStartupEnergy();
            hydrogen.refresh(formed, result);
            helium.refresh(formed, result);
            refreshInput(formed, working || logic.isWaiting(), result);
            refreshOutput(formed, working, result);
            refreshOverclock(formed, oc, minimum, result);
            refreshEnergy(formed, owned, oc, stored, minimum, result);
            refreshRecipe(formed, working, hasRecipe, recipeTier, oc, result);
            refreshTier(formed, working, working && recipeTier == machine.getTier(), result);
        }

        private void refreshInput(boolean formed, boolean active, DiagnosisResult result) {
            int units = formed ? machine.getInputUnits().size() : 0;
            IssueView problem = formed && units > 0 && !active ? FlowIssueViews.inputProblem(result) : null;
            if (!formed) inputView = RecipeIssue.OFFLINE.view();
            else if (units == 0) inputView = RecipeIssue.NO_INPUT_HATCH.view();
            else if (active) inputView = IssueView.of(RecipeIssue.RUNNING, Component.translatable(LANG_INPUT_LOADED));
            else if (problem != null) inputView = problem;
            else inputView = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_INPUT_WAITING));
            inputUnitsText = formed ? Component.literal(Integer.toString(units)) : NONE;
            inputDetail = List.of(Component.translatable(LANG_INPUT), gray(Component.translatable(LANG_INPUT_RULE)), FlowIssueViews.sentence(inputView, null));
        }

        private void refreshOutput(boolean formed, boolean working, DiagnosisResult result) {
            int units = formed ? machine.getOutputUnits().size() : 0;
            String description = null;
            if (!formed) {
                outputView = RecipeIssue.OFFLINE.view();
            } else {
                var view = FlowIssueViews.output(result, machine, working, units);
                outputView = view != null ? view : IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_OUTPUT_IDLE));
                if (outputView.issue() == RecipeIssue.OUTPUT_VOIDED && units == 0) description = LANG_OUTPUT_VOID_ALL_DESC;
            }
            outputUnitsText = formed ? Component.literal(Integer.toString(units)) : NONE;
            outputDetail = List.of(Component.translatable(LANG_OUTPUT), FlowIssueViews.sentence(outputView, description));
        }

        private void refreshOverclock(boolean formed, int oc, BigInteger minimum, DiagnosisResult result) {
            String description;
            if (!formed) {
                overclockView = RecipeIssue.OFFLINE.view();
                description = null;
            } else if (oc <= 0) {
                overclockView = labeled(IssueView.of(result, IdleReason.SET_CIRCUIT.type(), RecipeIssue.CONDITION.view()), Component.translatable(LANG_OC_NONE));
                description = LANG_OC_NONE_DESC;
            } else {
                var label = oc <= 1 ? Component.translatable(LANG_OC_ZERO) : Component.translatable(LANG_OC_COUNT, oc - 1);
                overclockView = IssueView.of(RecipeIssue.OK, label);
                description = LANG_OC_SET_DESC;
            }
            boolean set = formed && oc > 0;
            circuitText = set ? Component.translatable(LANG_CIRCUIT_VALUE, oc) : NONE;
            durationText = !set ? NONE : oc == 1 ? Component.literal("×1") : Component.translatable(LANG_FRACTION, 1 << (oc - 1));
            energyFactorText = set ? Component.literal("×" + (1 << 3 * (oc - 1))) : NONE;
            startupText = set ? eu(minimum) : NONE;
            overclockDetail = List.of(Component.translatable(LANG_OVERCLOCK),
                    gray(Component.translatable(LANG_OC_RULE_CIRCUIT)),
                    gray(Component.translatable(LANG_OC_RULE_EFFECT)),
                    FlowIssueViews.sentence(overclockView, description));
        }

        private void refreshEnergy(boolean formed, boolean owned, int oc, BigInteger stored, BigInteger minimum, DiagnosisResult result) {
            String description;
            boolean gridShort = result.has(IdleReason.HARMONY_GRID_SHORT.type());
            if (!formed) {
                energyView = RecipeIssue.OFFLINE.view();
                description = null;
            } else if (result.has(IdleReason.NO_OWNER.type())) {
                energyView = labeled(IssueView.of(result, IdleReason.NO_OWNER.type(), RecipeIssue.CONDITION.view()), Component.translatable(LANG_NO_OWNER));
                description = LANG_NO_OWNER_DESC;
            } else if (oc <= 0) {
                energyView = RecipeIssue.IDLE.view();
                description = LANG_OC_NONE_DESC;
            } else if (gridShort) {
                energyView = labeled(IssueView.of(result, IdleReason.HARMONY_GRID_SHORT.type(), RecipeIssue.LOW_POWER.view()), Component.translatable(LANG_EU_SHORT));
                description = null;
            } else {
                energyView = RecipeIssue.ENERGY_READY.view();
                description = LANG_EU_READY_DESC;
            }
            ownerText = formed && owned ? TeamUtil.getName(machine.getLevel(), machine.getUUID()) : NONE;
            storedText = formed && owned ? eu(stored) : NONE;
            storedLevel = !formed || !owned || oc == 0 ? Level.NORMAL : gridShort ? Level.ERROR : Level.GOOD;
            var lines = new ArrayList<Component>(7);
            lines.add(Component.translatable(LANG_ENERGY));
            lines.add(gray(Component.translatable(LANG_EU_RULE_DRAW)));
            lines.add(gray(Component.translatable(LANG_EU_RULE_FORMULA)));
            lines.add(gray(Component.translatable(LANG_EU_RULE_MULTIPLIER)));
            lines.add(gray(Component.translatable(LANG_EU_RULE_STRICT)));
            if (formed && oc > 0) lines.add(gray(Component.translatable(LANG_EU_CURRENT, FormattingUtil.formatNumbers(minimum))));
            lines.add(FlowIssueViews.sentence(energyView, description));
            energyDetail = lines;
        }

        private void refreshRecipe(boolean formed, boolean working, boolean hasRecipe, int recipeTier, int oc, DiagnosisResult result) {
            var logic = machine.getRecipeLogic();
            if (!formed) recipeView = RecipeIssue.UNFORMED.view();
            else if (!logic.isWorkingEnabled()) recipeView = RecipeIssue.DISABLED.view();
            else if (working) recipeView = RecipeIssue.RUNNING.view();
            else recipeView = IssueView.primary(result, logic.isWaiting() ? RecipeIssue.WAITING.view() : RecipeIssue.IDLE.view());
            recipeState = working ? FlowState.ACTIVE : recipeView.state();
            recipeTierText = formed && hasRecipe ? Component.translatable(LANG_TIER_VALUE, recipeTier) : NONE;
            runEnergyText = formed && hasRecipe && oc > 0 ? eu(HarmonyMachine.recipeEnergy(oc, recipeTier)) : NONE;
            var lines = new ArrayList<Component>(6);
            lines.add(Component.translatable(LANG_RECIPE));
            lines.add(gray(Component.translatable(LANG_RECIPE_RULE_START)));
            lines.add(gray(Component.translatable(LANG_RECIPE_RULE_START_2)));
            lines.add(gray(Component.translatable(LANG_RECIPE_RULE_COST)));
            lines.add(gray(Component.translatable(LANG_RECIPE_RULE_TIER)));
            lines.add(FlowIssueViews.sentence(recipeView, null));
            recipeDetail = lines;
        }

        private void refreshTier(boolean formed, boolean working, boolean counting, DiagnosisResult result) {
            int tier = machine.getTier();
            int needed = HarmonyMachine.runsToAdvance(tier);
            int count = machine.getTierCount();
            if (count != shownCount || needed != shownNeeded) {
                shownCount = count;
                shownNeeded = needed;
                advance = new ProgressBar.Progress(count, needed, 0);
            }
            var label = Component.translatable(LANG_TIER_VALUE, tier);
            String description = counting ? LANG_TIER_COUNTING_DESC : LANG_TIER_IDLE_DESC;
            if (!formed) {
                tierView = RecipeIssue.OFFLINE.view();
            } else if (counting) {
                tierView = IssueView.of(RecipeIssue.RUNNING, label);
            } else if (!working && result.has(IdleReason.SIMULATION_TIER.type())) {
                tierView = labeled(IssueView.of(result, IdleReason.SIMULATION_TIER.type(), RecipeIssue.CONDITION.view()), label);
                description = null;
            } else {
                tierView = IssueView.of(RecipeIssue.OK, label);
            }
            allowedText = formed ? Component.translatable(LANG_TIER_MIN, tier) : NONE;
            tierDetail = List.of(Component.translatable(LANG_TIER),
                    gray(Component.translatable(LANG_RECIPE_RULE_TIER)),
                    gray(Component.translatable(LANG_TIER_RULE_COUNT)),
                    gray(Component.translatable(LANG_TIER_RULE_ADVANCE, needed)),
                    gray(Component.translatable(LANG_TIER_RULE_HIGHER)),
                    gray(Component.translatable(LANG_TIER_REMAINING, Math.max(0, needed - count))),
                    FlowIssueViews.sentence(tierView, description));
        }
    }
}
