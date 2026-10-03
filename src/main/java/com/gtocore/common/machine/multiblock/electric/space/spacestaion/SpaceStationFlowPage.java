package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.common.data.machines.GTOMachineProtocols;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemView;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
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
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeDiagnoser;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeIssue;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import earth.terrarium.adastra.common.registry.ModBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTMaterials.DistilledWater;

@DataGeneratorScanned
public final class SpaceStationFlowPage {

    private static final int COLUMN_WIDTH = 60;
    private static final int ENVIRONMENT_WIDTH = 84;
    private static final int REFRESH_TICKS = 20;
    private static final int PROGRESS_TICKS = 4;
    private static final int READY_TARGET = 10;
    private static final int LABEL_WIDTH = 24;

    @RegisterLanguage(cn = "空间站运行", en = "Station Operation")
    private static final String LANG_STATION = "gtocore.machine.space_station.flow.station";
    @RegisterLanguage(cn = "状态", en = "State")
    private static final String LANG_STATE = "gtocore.machine.space_station.flow.state";
    @RegisterLanguage(cn = "舱内环境", en = "Environment")
    private static final String LANG_ENVIRONMENT = "gtocore.machine.space_station.flow.environment";
    @RegisterLanguage(cn = "供氧", en = "Oxygen")
    private static final String LANG_OXYGEN = "gtocore.machine.space_station.flow.oxygen";
    @RegisterLanguage(cn = "温度", en = "Temp")
    private static final String LANG_TEMPERATURE = "gtocore.machine.space_station.flow.temperature";
    @RegisterLanguage(cn = "超净", en = "Clean")
    private static final String LANG_CLEANROOM = "gtocore.machine.space_station.flow.cleanroom";
    @RegisterLanguage(cn = "作业", en = "Work")
    private static final String LANG_WORKSPACE = "gtocore.machine.space_station.flow.workspace";
    @RegisterLanguage(cn = "空间", en = "Volume")
    private static final String LANG_VOLUME = "gtocore.machine.space_station.flow.volume";
    @RegisterLanguage(cn = "已供氧", en = "Supplied")
    private static final String LANG_OXYGEN_ON = "gtocore.machine.space_station.flow.oxygen_on";
    @RegisterLanguage(cn = "待供氧", en = "Pending")
    private static final String LANG_OXYGEN_PENDING = "gtocore.machine.space_station.flow.oxygen_pending";
    @RegisterLanguage(cn = "未供氧", en = "None")
    private static final String LANG_OXYGEN_OFF = "gtocore.machine.space_station.flow.oxygen_off";
    @RegisterLanguage(cn = "舒适", en = "Comfort")
    private static final String LANG_TEMPERATURE_ON = "gtocore.machine.space_station.flow.temperature_on";
    @RegisterLanguage(cn = "待调节", en = "Pending")
    private static final String LANG_TEMPERATURE_PENDING = "gtocore.machine.space_station.flow.temperature_pending";
    @RegisterLanguage(cn = "未调节", en = "Unset")
    private static final String LANG_TEMPERATURE_OFF = "gtocore.machine.space_station.flow.temperature_off";
    @RegisterLanguage(cn = "%s 台", en = "%s")
    private static final String LANG_MACHINE_COUNT = "gtocore.machine.space_station.flow.machine_count";
    @RegisterLanguage(cn = "无机器", en = "None")
    private static final String LANG_NO_MACHINE = "gtocore.machine.space_station.flow.no_machine";
    @RegisterLanguage(cn = "%s 格", en = "%s")
    private static final String LANG_BLOCK_COUNT = "gtocore.machine.space_station.flow.block_count";
    @RegisterLanguage(cn = "供氧与温度：覆盖 %s 格。运行时每 20 秒刷新，就绪度降为 0 时撤除。", en = "Oxygen and temperature: %s blocks covered. Refreshed every 20 seconds while running; removed when readiness drops to 0.")
    private static final String LANG_ENV_OXYGEN_DETAIL = "gtocore.machine.space_station.flow.env_oxygen_detail";
    @RegisterLanguage(cn = "超净环境：%s，就绪度达到 100%% 时生效。", en = "Cleanroom: %s, effective at 100%% readiness.")
    private static final String LANG_ENV_CLEAN_DETAIL = "gtocore.machine.space_station.flow.env_clean_detail";
    @RegisterLanguage(cn = "舱内机器：%s 台，工作中 %s 台；就绪度达到 100%% 后可在太空中工作。", en = "Machines inside: %s, %s working; they can work in space at 100%% readiness.")
    private static final String LANG_ENV_WORK_DETAIL = "gtocore.machine.space_station.flow.env_work_detail";
    @RegisterLanguage(cn = "就绪度已达 100%%，超净环境与太空作业生效。", en = "Readiness is at 100%%; the cleanroom and space work are in effect.")
    private static final String LANG_READINESS_REACHED = "gtocore.machine.space_station.flow.readiness_reached";
    @RegisterLanguage(cn = "就绪度未达 100%%，超净环境与太空作业暂不生效。", en = "Readiness is below 100%%; the cleanroom and space work are not yet in effect.")
    private static final String LANG_READINESS_PENDING = "gtocore.machine.space_station.flow.readiness_pending";
    @RegisterLanguage(cn = "舱内空间：%s 格。", en = "Interior volume: %s blocks.")
    private static final String LANG_ENV_VOLUME_DETAIL = "gtocore.machine.space_station.flow.env_volume_detail";
    @RegisterLanguage(cn = "进度", en = "Progress")
    private static final String LANG_PROGRESS = "gtocore.machine.space_station.flow.progress";
    @RegisterLanguage(cn = "就绪度", en = "Readiness")
    private static final String LANG_READINESS = "gtocore.machine.space_station.flow.readiness";
    @RegisterLanguage(cn = "运行时每秒提升 10%%，停机后逐渐下降；达到 100%% 后舱内机器方可工作。", en = "Rises by 10%% per second while running and falls gradually after stopping. Machines inside the station work only at 100%%.")
    private static final String LANG_READINESS_HINT = "gtocore.machine.space_station.flow.readiness.hint";
    @RegisterLanguage(cn = "电力", en = "Power")
    private static final String LANG_ENERGY = "gtocore.machine.space_station.flow.energy";
    @RegisterLanguage(cn = "耗能", en = "Usage")
    private static final String LANG_USAGE = "gtocore.machine.space_station.flow.usage";
    @RegisterLanguage(cn = "功率", en = "Input")
    private static final String LANG_POWER = "gtocore.machine.space_station.flow.power";
    @RegisterLanguage(cn = "电压", en = "Tier")
    private static final String LANG_VOLTAGE = "gtocore.machine.space_station.flow.voltage";
    @RegisterLanguage(cn = "要求", en = "Needs")
    private static final String LANG_REQUIRED_TIER = "gtocore.machine.space_station.flow.required_tier";
    @RegisterLanguage(cn = "蒸馏水供给", en = "Water Supply")
    private static final String LANG_WATER = "gtocore.machine.space_station.flow.water_supply";
    @RegisterLanguage(cn = "每仓", en = "Each")
    private static final String LANG_PER_HATCH = "gtocore.machine.space_station.flow.per_hatch";
    @RegisterLanguage(cn = "每仓每秒供水量（mB）", en = "Supply per hatch per second (mB)")
    private static final String LANG_PER_HATCH_TOOLTIP = "gtocore.machine.space_station.flow.per_hatch.tooltip";
    @RegisterLanguage(cn = "供水仓", en = "Hatches")
    private static final String LANG_HATCHES = "gtocore.machine.space_station.flow.hatches";
    @RegisterLanguage(cn = "每秒", en = "Per Second")
    private static final String LANG_PER_SECOND = "gtocore.machine.space_station.flow.per_second";
    @RegisterLanguage(cn = "帆板", en = "Sails")
    private static final String LANG_SAILS = "gtocore.machine.space_station.flow.sails";
    @RegisterLanguage(cn = "光伏帆板：已成型 %s 台，帆板接口共 %s 个。", en = "Photovoltaic sails: %s formed, %s sail docks in total.")
    private static final String LANG_SAILS_DETAIL = "gtocore.machine.space_station.flow.sails_detail";
    @RegisterLanguage(cn = "点左侧「探索者号空间站总览」可在空位上安装光伏帆板。", en = "Use \"Explorer Station Overview\" on the left to install sails at free docks.")
    private static final String LANG_SAILS_HINT = "gtocore.machine.space_station.flow.sails_hint";
    @RegisterLanguage(cn = "、", en = ", ")
    private static final String LANG_SEPARATOR = "gtocore.machine.space_station.flow.separator";

    @RegisterLanguage(cn = "不在太空", en = "Not in Space")
    private static final String LANG_NOT_IN_SPACE = "gtocore.machine.space_station.flow.not_in_space";
    @RegisterLanguage(cn = "空间站仅在太空（轨道维度）中运行。", en = "The space station operates only in space (orbit dimensions).")
    private static final String LANG_NOT_IN_SPACE_DESC = "gtocore.machine.space_station.flow.not_in_space.desc";
    @RegisterLanguage(cn = "等待就绪", en = "Warming Up")
    private static final String LANG_WARMING = "gtocore.machine.space_station.flow.warming";
    @RegisterLanguage(cn = "持续运行 %s 秒后就绪度达到 100%%，舱内机器方可工作。", en = "Readiness reaches 100%% after %s more seconds of operation; machines inside the station work only then.")
    private static final String LANG_WARMING_DESC = "gtocore.machine.space_station.flow.warming.desc";
    @RegisterLanguage(cn = "就绪", en = "Ready")
    private static final String LANG_READY = "gtocore.machine.space_station.flow.ready";
    @RegisterLanguage(cn = "缺少：%s", en = "Missing: %s")
    private static final String LANG_MISSING = "gtocore.machine.space_station.flow.missing";

    @RegisterLanguage(cn = "无供水仓", en = "No Hatch")
    private static final String LANG_WATER_NO_HATCH = "gtocore.machine.space_station.flow.water_no_hatch";
    @RegisterLanguage(cn = "对接接口处没有供水仓。", en = "No supply hatch is installed at the docking ports.")
    private static final String LANG_WATER_NO_HATCH_DESC = "gtocore.machine.space_station.flow.water_no_hatch.desc";
    @RegisterLanguage(cn = "供水停用", en = "Supply Off")
    private static final String LANG_WATER_OFF = "gtocore.machine.space_station.flow.water_off";
    @RegisterLanguage(cn = "每仓供水量为 0，已停止供水。", en = "The supply per hatch is 0; supply is stopped.")
    private static final String LANG_WATER_OFF_DESC = "gtocore.machine.space_station.flow.water_off.desc";
    @RegisterLanguage(cn = "待机", en = "Standby")
    private static final String LANG_WATER_IDLE = "gtocore.machine.space_station.flow.water_idle";
    @RegisterLanguage(cn = "空间站未运行，暂停供水。", en = "Supply pauses while the station is not running.")
    private static final String LANG_WATER_IDLE_DESC = "gtocore.machine.space_station.flow.water_idle.desc";
    @RegisterLanguage(cn = "供水仓满", en = "Hatches Full")
    private static final String LANG_WATER_FULL = "gtocore.machine.space_station.flow.water_full";
    @RegisterLanguage(cn = "供水仓已满，暂时无法供水。", en = "The supply hatches are full; supply is on hold.")
    private static final String LANG_WATER_FULL_DESC = "gtocore.machine.space_station.flow.water_full.desc";
    @RegisterLanguage(cn = "缺蒸馏水", en = "Water Short")
    private static final String LANG_WATER_SHORT = "gtocore.machine.space_station.flow.water_short";
    @RegisterLanguage(cn = "输入仓中的蒸馏水不足，部分或全部供水仓未获供给。", en = "Distilled Water in the input hatches is insufficient; some or all supply hatches receive nothing.")
    private static final String LANG_WATER_SHORT_DESC = "gtocore.machine.space_station.flow.water_short.desc";
    @RegisterLanguage(cn = "供水中", en = "Supplying")
    private static final String LANG_WATER_ON = "gtocore.machine.space_station.flow.water_on";
    @RegisterLanguage(cn = "正在向各供水仓输出蒸馏水。", en = "Distilled Water is being sent to each supply hatch.")
    private static final String LANG_WATER_ON_DESC = "gtocore.machine.space_station.flow.water_on.desc";
    @RegisterLanguage(cn = "运行时每秒从空间站输入仓扣除蒸馏水，输出到对接接口处的各供水仓，供给光伏帆板控制器。", en = "While running, Distilled Water is drawn from the station's input hatches every second and sent to each supply hatch at the docking ports for the Photovoltaic Sail Controllers.")
    private static final String LANG_WATER_DESC = "gtocore.machine.space_station.flow.water_desc";
    @RegisterLanguage(cn = "供水仓：%s（可接收 %s）", en = "Supply hatches: %s (accepting %s)")
    private static final String LANG_WATER_HATCHES = "gtocore.machine.space_station.flow.water_hatches";
    @RegisterLanguage(cn = "每秒供水：%s（计划 %s）", en = "Supplied per second: %s (planned %s)")
    private static final String LANG_WATER_RATE = "gtocore.machine.space_station.flow.water_rate";
    @RegisterLanguage(cn = "输入蒸馏水：%s（每轮配方需要 %s）", en = "Distilled Water in inputs: %s (%s per cycle)")
    private static final String LANG_WATER_STOCK = "gtocore.machine.space_station.flow.water_stock";
    @RegisterLanguage(cn = "每秒供水扣除：%s", en = "Drawn for supply per second: %s")
    private static final String LANG_WATER_DRAW = "gtocore.machine.space_station.flow.water_draw";

    private SpaceStationFlowPage() {}

    static Widget create(SimpleSpaceStationMachine machine, FancyMachineUIWidget window) {
        boolean remote = window.isRemote();
        var status = new Status(machine, machine.roundRecipe());
        var diagnoser = status.diagnoser;
        int inputs = diagnoser.inputCount(), outputs = diagnoser.outputCount();
        int columns = Math.max(3, Math.max(inputs, outputs + 1));
        int[] widths = new int[columns + 1];
        Arrays.fill(widths, COLUMN_WIDTH);
        widths[columns] = ENVIRONMENT_WIDTH;
        var chart = new FlowChart(UISizes.FLOW_GUTTER_WIDTH, widths);
        var station = stationNode(chart, machine, status, columns);
        var environment = environmentNode(chart, status, columns);
        chart.link(station, environment).follow(environment);
        var inputNodes = new FlowNode[inputs];
        for (int i = 0; i < inputs; i++) {
            inputNodes[i] = inputNode(chart, status, i, remote);
            chart.link(inputNodes[i], station);
        }
        chart.link(energyNode(chart, status), station);
        var water = waterNode(chart, machine, status, columns - outputs, remote);
        chart.link(station, water).follow(water);
        for (int i = 0; i < outputs; i++) {
            var output = outputNode(chart, status, columns - outputs + i, i, remote);
            chart.link(station, output).follow(output);
        }
        if (status.waterIndex >= 0) chart.link(inputNodes[status.waterIndex], water).follow(water);
        return UIElement.column(LayoutStyle.AUTO).addChild(chart.toView(window, false, machine.getDefinition().getId().toString()));
    }

    private static FlowNode stationNode(FlowChart chart, SimpleSpaceStationMachine machine, Status status, int columns) {
        var node = chart.node(1, 1, columns - 1).bindState(status.live(() -> status.station)).bindDetail(status.live(() -> status.stationDetail));
        node.addChildren(FlowParts.header(ItemView.of(machine.getDefinition().asStack()), LANG_STATION),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status.live(() -> status.stationView)),
                ProgressBar.of(LayoutStyle.AUTO, Component.translatable(LANG_PROGRESS), UITheme.FLOW_CYAN_LIGHT, status.live(() -> status.recipeProgress)),
                ProgressBar.of(LayoutStyle.AUTO, Component.translatable(LANG_READINESS), UITheme.STATUS_ONLINE, status.live(() -> status.readiness))
                        .percent().bindDetail(() -> Status.READINESS_HINT));
        return node;
    }

    private static FlowNode inputNode(FlowChart chart, Status status, int index, boolean remote) {
        var diagnoser = status.diagnoser;
        var node = chart.node(0, index).bindState(status.live(() -> diagnoser.inputIssue(index).state())).bindDetail(status.live(() -> status.inputDetails.get(index)));
        return FlowParts.slotBody(node, FlowParts.fluidSlot(remote ? null : diagnoser.inputStack(index)),
                TextLine.of(LayoutStyle.AUTO, () -> diagnoser.inputName(index)), () -> diagnoser.inputAmount(index),
                status.live(() -> diagnoser.inputIssue(index).state().getLevel()), status.live(() -> diagnoser.inputIssue(index).view()));
    }

    private static FlowNode energyNode(FlowChart chart, Status status) {
        var diagnoser = status.diagnoser;
        var node = chart.node(1, 0).bindState(status.live(() -> diagnoser.energyIssue().state())).bindDetail(status.live(() -> status.energyDetail));
        node.addChildren(FlowParts.header(ItemView.of(FlowParts.energyIcon(node)), LANG_ENERGY), new IssueLine(LayoutStyle.AUTO, null, status.live(() -> diagnoser.energyIssue().view())),
                StatusLine.of(LayoutStyle.AUTO, LANG_USAGE, () -> status.usageText),
                StatusLine.of(LayoutStyle.AUTO, LANG_POWER, status.live(() -> status.powerText))
                        .bindLevel(status.live(() -> diagnoser.energyIssue() == RecipeIssue.LOW_POWER ? Level.ERROR : Level.NORMAL)),
                StatusLine.of(LayoutStyle.AUTO, LANG_VOLTAGE, status.live(() -> status.voltageText)).bindLevel(status.live(status::voltageLevel)),
                StatusLine.of(LayoutStyle.AUTO, LANG_REQUIRED_TIER, () -> status.requiredTierText),
                ProgressBar.of(LayoutStyle.AUTO, Component.empty(), UITheme.FLOW_CYAN_LIGHT, status.live(() -> status.buffer)).percent());
        return node;
    }

    private static FlowNode environmentNode(FlowChart chart, Status status, int column) {
        var node = chart.node(1, column).bindState(status.live(() -> status.environment)).bindDetail(status.live(() -> status.environmentDetail));
        node.addChildren(FlowParts.header(ItemView.of(new ItemStack(ModBlocks.OXYGEN_DISTRIBUTOR.get())), LANG_ENVIRONMENT),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_OXYGEN), status.live(() -> status.oxygenView)),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_TEMPERATURE), status.live(() -> status.temperatureView)),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_CLEANROOM), status.live(() -> status.cleanroomView)),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_WORKSPACE), status.live(() -> status.workspaceView)),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_VOLUME), status.live(() -> status.volumeView)));
        return node;
    }

    private static FlowNode outputNode(FlowChart chart, Status status, int column, int index, boolean remote) {
        var diagnoser = status.diagnoser;
        var node = chart.node(2, column).bindState(status.live(() -> diagnoser.outputIssue(index).state())).bindDetail(status.live(() -> diagnoser.outputDetail(index)));
        return FlowParts.slotBody(node, FlowParts.fluidSlot(remote ? null : diagnoser.outputStack(index)),
                TextLine.of(LayoutStyle.AUTO, () -> diagnoser.outputName(index)), () -> diagnoser.outputAmount(index),
                status.live(() -> diagnoser.outputIssue(index).state().getLevel()), status.live(() -> diagnoser.outputIssue(index).view()));
    }

    private static FlowNode waterNode(FlowChart chart, SimpleSpaceStationMachine machine, Status status, int span, boolean remote) {
        var node = chart.node(2, 0, span).bindState(status.live(() -> status.waterState)).bindDetail(status.live(() -> status.waterDetail));
        var header = UIElement.centeredRow(UISizes.SLOT_SIZE);
        header.addChildren(FlowParts.fluidSlot(remote ? null : DistilledWater.getFluid(1)), TextLine.translatable(0, LANG_WATER).layout(l -> l.flex(1)));
        int fieldWidth = chart.widthFor(span) - 2 * UISizes.FLOW_NODE_PADDING - LABEL_WIDTH - UISizes.GAP;
        var field = NumberField.ofInt(fieldWidth, machine::getWaterAmountPerHatch, machine::setWaterAmountPerHatch, 0, SimpleSpaceStationMachine.MAX_WATER_PER_HATCH);
        field.setHoverTooltips(Component.translatable(LANG_PER_HATCH_TOOLTIP));
        var amountRow = UIElement.centeredRow(UISizes.CONTROL_HEIGHT)
                .addChildren(TextLine.translatable(LABEL_WIDTH, LANG_PER_HATCH).bindClientColor(UITheme::textSecondary), field);
        node.addChildren(header,
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status.live(() -> status.waterView)),
                amountRow,
                StatusLine.of(LayoutStyle.AUTO, LANG_HATCHES, status.live(() -> status.hatchesText)),
                StatusLine.of(LayoutStyle.AUTO, LANG_SAILS, status.live(() -> status.sailsText))
                        .bindLevel(status.live(() -> status.sailsFormed > 0 ? Level.GOOD : Level.NORMAL)),
                StatusLine.of(LayoutStyle.AUTO, LANG_PER_SECOND, status.live(() -> status.rateText)).bindLevel(status.live(() -> status.waterState.getLevel())));
        return node;
    }

    private static Component ratio(int current, int total) {
        return Component.literal(current + " / " + total);
    }

    private static final class Status extends ThrottledStatus {

        private static final Component READINESS_HINT = Component.translatable(LANG_READINESS_HINT);

        private final SimpleSpaceStationMachine machine;
        private final RecipeDiagnoser diagnoser;
        private final int duration;
        private final int waterIndex;
        private final Fluid water = DistilledWater.getFluid();
        private final Component usageText;
        private final Component requiredTierText;
        private final IRecipeHandler.KeyVisitor waterCollector;
        private long[] unitWater = new long[0];
        private long[] unitScratch = new long[0];
        private int collectingUnit;

        private boolean progressRefreshed;
        private int progressAt;
        private FlowState station = FlowState.IDLE;
        private IssueView stationView = RecipeIssue.IDLE.view();
        private List<Component> stationDetail = Collections.emptyList();
        private FlowState waterState = FlowState.IDLE;
        private IssueView waterView = RecipeIssue.IDLE.view();
        private List<Component> waterDetail = Collections.emptyList();
        private List<Component> energyDetail = Collections.emptyList();
        private final List<List<Component>> inputDetails;
        private FlowState environment = FlowState.IDLE;
        private IssueView oxygenView = RecipeIssue.OFFLINE.view();
        private IssueView temperatureView = RecipeIssue.OFFLINE.view();
        private IssueView cleanroomView = RecipeIssue.OFFLINE.view();
        private IssueView workspaceView = RecipeIssue.OFFLINE.view();
        private IssueView volumeView = RecipeIssue.OFFLINE.view();
        private List<Component> environmentDetail = Collections.emptyList();
        private Component powerText = FlowParts.DASH;
        private Component voltageText = FlowParts.DASH;
        private Component hatchesText = FlowParts.DASH;
        private Component sailsText = FlowParts.DASH;
        private int sailsFormed = -1, sailDocks = -1;
        private Component rateText = FlowParts.DASH;
        private long shownPower = -1;
        private int shownTier = -2;
        private int shownHatches = -1, shownAccepting = -1;
        private long shownRate = -1;
        private long waterDraw;
        private ProgressBar.Progress recipeProgress = ProgressBar.Progress.EMPTY;
        private ProgressBar.Progress readiness = ProgressBar.Progress.EMPTY;
        private ProgressBar.Progress buffer = ProgressBar.Progress.EMPTY;
        private int shownProgress = -1, shownMaxProgress = -1, shownReady = -1;
        private long shownStored = -1, shownCapacity = -1;

        private Status(SimpleSpaceStationMachine machine, GTRecipeDefinition recipe) {
            super(machine::getOffsetTimer, REFRESH_TICKS);
            this.machine = machine;
            this.diagnoser = new RecipeDiagnoser(machine, recipe);
            this.duration = recipe.duration;
            this.waterIndex = diagnoser.findInput(DistilledWater.getFluid(1));
            this.usageText = Component.literal(FormattingUtil.formatNumbers(diagnoser.getEUt()));
            this.requiredTierText = Component.literal(GTValues.VN[diagnoser.getTier()]);
            this.inputDetails = new ArrayList<>(Collections.nCopies(diagnoser.inputCount(), Collections.emptyList()));
            this.waterCollector = (key, amount) -> {
                if (key instanceof AEFluidKey fluidKey && fluidKey.getFluid() == water) {
                    long sum = unitWater[collectingUnit] + amount;
                    unitWater[collectingUnit] = sum < 0 ? Long.MAX_VALUE : sum;
                }
                return false;
            };
        }

        private Level voltageLevel() {
            if (diagnoser.energyTier() < 0) return Level.NORMAL;
            return diagnoser.energyIssue() == RecipeIssue.LOW_VOLTAGE ? Level.ERROR : Level.GOOD;
        }

        @Override
        protected void onAccess(int now) {
            if (!progressRefreshed || now < progressAt || now - progressAt >= PROGRESS_TICKS) {
                progressRefreshed = true;
                progressAt = now;
                refreshProgress();
            }
        }

        @Override
        protected void refresh(int now) {
            refreshStates();
        }

        private void refreshProgress() {
            var logic = machine.getRecipeLogic();
            int progress = 0, max = duration;
            if (logic.isActive() && logic.getMaxProgress() > 0) {
                progress = logic.getProgress();
                max = logic.getMaxProgress();
            }
            if (progress != shownProgress || max != shownMaxProgress) {
                shownProgress = progress;
                shownMaxProgress = max;
                recipeProgress = new ProgressBar.Progress(progress, max, 0);
            }
            int ready = Math.min(READY_TARGET, machine.getReadyCount());
            if (ready != shownReady) {
                shownReady = ready;
                readiness = new ProgressBar.Progress(ready, READY_TARGET, 0);
            }
            var container = machine.getEnergyContainer();
            long stored = container.getEnergyStored(), capacity = container.getEnergyCapacity();
            if (stored != shownStored || capacity != shownCapacity) {
                shownStored = stored;
                shownCapacity = capacity;
                buffer = capacity > 0 ? new ProgressBar.Progress(stored, capacity, 0) : new ProgressBar.Progress(0, 1, 0);
            }
        }

        private void refreshStates() {
            boolean formed = machine.isFormed();
            var logic = machine.getRecipeLogic();
            boolean working = logic.isWorking();
            boolean inCycle = working || logic.isWaiting();
            diagnoser.diagnoseFluids(formed, inCycle);
            diagnoser.diagnoseEnergy(formed, working, machine.getEnergyContainer(), machine.getMaxVoltage(), machine.getTier());
            refreshEnergyTexts();
            var energyLines = new ArrayList<Component>(diagnoser.energyDetail().size() + 1);
            energyLines.add(Component.translatable(LANG_ENERGY));
            energyLines.addAll(diagnoser.energyDetail());
            energyDetail = energyLines;
            refreshWater(formed, working);
            refreshInputDetails();
            refreshStation(formed, working, inCycle);
            refreshEnvironment(formed, working);
        }

        private void refreshEnergyTexts() {
            long power = diagnoser.power();
            if (power != shownPower) {
                shownPower = power;
                powerText = Component.literal(FormattingUtil.formatNumberReadable(power));
            }
            int tier = diagnoser.energyTier();
            if (tier != shownTier) {
                shownTier = tier;
                voltageText = tier < 0 ? FlowParts.DASH : Component.literal(GTValues.VN[tier]);
            }
        }

        private void refreshInputDetails() {
            for (int i = 0; i < diagnoser.inputCount(); i++) {
                var detail = diagnoser.inputDetail(i);
                if (i == waterIndex && waterDraw > 0) {
                    var lines = new ArrayList<Component>(detail.size() + 1);
                    lines.addAll(detail);
                    lines.add(Component.translatable(LANG_WATER_DRAW, RecipeDiagnoser.amount(waterDraw)).withStyle(ChatFormatting.GRAY));
                    detail = lines;
                }
                inputDetails.set(i, detail);
            }
        }

        private void refreshStation(boolean formed, boolean working, boolean inCycle) {
            var logic = machine.getRecipeLogic();
            IssueView view;
            Component sentence;
            if (!formed) {
                view = RecipeIssue.UNFORMED.view();
                sentence = null;
            } else if (!machine.isInSpace()) {
                view = IssueView.of(RecipeIssue.CONDITION, Component.translatable(LANG_NOT_IN_SPACE));
                sentence = Component.translatable(LANG_NOT_IN_SPACE_DESC);
            } else if (!logic.isWorkingEnabled()) {
                view = RecipeIssue.DISABLED.view();
                sentence = null;
            } else if (working) {
                int ready = machine.getReadyCount();
                if (ready < READY_TARGET) {
                    view = IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_WARMING));
                    sentence = Component.translatable(LANG_WARMING_DESC, READY_TARGET - ready);
                } else {
                    view = RecipeIssue.RUNNING.view();
                    sentence = null;
                }
            } else if (inCycle) {
                view = RecipeIssue.WAITING.view();
                sentence = null;
            } else if (!diagnoser.inputsSatisfied()) {
                view = firstInputProblem();
                sentence = null;
            } else if (!diagnoser.energySatisfied()) {
                view = diagnoser.energyIssue().view();
                sentence = null;
            } else if (!diagnoser.outputsFit()) {
                view = firstOutputProblem();
                sentence = null;
            } else {
                view = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_READY));
                sentence = null;
            }
            stationView = view;
            station = working ? FlowState.ACTIVE : view.issue().state();
            var lines = new ArrayList<Component>(4);
            lines.add(Component.translatable(LANG_STATION));
            var style = RecipeDiagnoser.style(view.issue());
            lines.add((sentence != null ? sentence.copy() : Component.translatable(view.issue().descriptionKey())).withStyle(style));
            if (formed && !diagnoser.inputsSatisfied()) lines.add(Component.translatable(LANG_MISSING, missingNames()).withStyle(style));
            stationDetail = lines;
        }

        private IssueView firstInputProblem() {
            for (int i = 0; i < diagnoser.inputCount(); i++) {
                if (diagnoser.inputIssue(i).isProblem()) return diagnoser.inputIssue(i).view();
            }
            return RecipeIssue.INPUT_SHORT.view();
        }

        private IssueView firstOutputProblem() {
            for (int i = 0; i < diagnoser.outputCount(); i++) {
                if (diagnoser.outputIssue(i).isProblem()) return diagnoser.outputIssue(i).view();
            }
            return RecipeIssue.OUTPUT_FULL.view();
        }

        private MutableComponent missingNames() {
            var names = Component.empty();
            boolean first = true;
            for (int i = 0; i < diagnoser.inputCount(); i++) {
                if (diagnoser.available(i) >= diagnoser.need(i)) continue;
                if (!first) names.append(Component.translatable(LANG_SEPARATOR));
                names.append(diagnoser.inputName(i));
                first = false;
            }
            return names;
        }

        private void refreshWater(boolean formed, boolean working) {
            var hatches = formed ? machine.getSupplyHatches() : null;
            int total = hatches == null ? 0 : hatches.size();
            int amount = machine.getWaterAmountPerHatch();
            int accepting = 0, supplied = 0;
            long stock = 0;
            if (formed) stock = collectWater();
            if (total > 0 && amount > 0) {
                for (int h = 0; h < total; h++) {
                    if (hatches.get(h).simulateOutputFluid(water, amount)) accepting++;
                }
                supplied = simulateSupply(accepting, amount);
            }
            waterDraw = working ? (long) supplied * amount : 0;
            IssueView view;
            String sentence;
            if (!formed) {
                view = RecipeIssue.UNFORMED.view();
                sentence = RecipeIssue.UNFORMED.descriptionKey();
            } else if (total == 0) {
                view = IssueView.of(RecipeIssue.NO_OUTPUT_HATCH, Component.translatable(LANG_WATER_NO_HATCH));
                sentence = LANG_WATER_NO_HATCH_DESC;
            } else if (amount <= 0) {
                view = IssueView.of(RecipeIssue.DISABLED, Component.translatable(LANG_WATER_OFF));
                sentence = LANG_WATER_OFF_DESC;
            } else if (!working) {
                view = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_WATER_IDLE));
                sentence = LANG_WATER_IDLE_DESC;
            } else if (accepting == 0) {
                view = IssueView.of(RecipeIssue.OUTPUT_FULL, Component.translatable(LANG_WATER_FULL), FlowState.WARNING);
                sentence = LANG_WATER_FULL_DESC;
            } else if (supplied < accepting) {
                view = IssueView.of(RecipeIssue.INPUT_SHORT, Component.translatable(LANG_WATER_SHORT));
                sentence = LANG_WATER_SHORT_DESC;
            } else {
                view = IssueView.of(RecipeIssue.RUNNING, Component.translatable(LANG_WATER_ON));
                sentence = LANG_WATER_ON_DESC;
            }
            if (!view.equals(waterView)) waterView = view;
            var issue = view.issue();
            if (issue == RecipeIssue.INPUT_SHORT && supplied > 0) waterState = FlowState.WARNING;
            else waterState = view.state();
            if (total != shownHatches || accepting != shownAccepting) {
                shownHatches = total;
                shownAccepting = accepting;
                hatchesText = ratio(accepting, total);
            }
            long rate = working ? (long) supplied * amount : (long) accepting * amount;
            if (rate != shownRate) {
                shownRate = rate;
                rateText = RecipeDiagnoser.amount(rate);
            }
            int docks = 0, sails = 0;
            var assembly = formed ? machine.getAssembly() : null;
            if (assembly != null) {
                docks = assembly.ports(GTOMachineProtocols.PHOTOVOLTAIC_SAIL).size();
                for (var sail : assembly.machines(GTOMachineProtocols.PHOTOVOLTAIC_SAIL)) {
                    if (sail instanceof MultiblockControllerMachine controller && controller.isFormed()) sails++;
                }
            }
            if (sails != sailsFormed || docks != sailDocks) {
                sailsFormed = sails;
                sailDocks = docks;
                sailsText = ratio(sails, docks);
            }
            var lines = new ArrayList<Component>(8);
            lines.add(Component.translatable(LANG_WATER));
            lines.add(Component.translatable(LANG_WATER_DESC).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(LANG_WATER_HATCHES, total, accepting).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(LANG_WATER_RATE, RecipeDiagnoser.amount((long) supplied * amount), RecipeDiagnoser.amount((long) accepting * amount)).withStyle(ChatFormatting.GRAY));
            if (waterIndex >= 0) {
                lines.add(Component.translatable(LANG_WATER_STOCK, RecipeDiagnoser.amount(stock), diagnoser.inputAmount(waterIndex)).withStyle(ChatFormatting.GRAY));
            }
            lines.add(Component.translatable(sentence).withStyle(RecipeDiagnoser.style(issue)));
            lines.add(Component.translatable(LANG_SAILS_DETAIL, sails, docks).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(LANG_SAILS_HINT).withStyle(ChatFormatting.GRAY));
            waterDetail = lines;
        }

        private void refreshEnvironment(boolean formed, boolean working) {
            if (!formed) {
                environment = FlowState.IDLE;
                oxygenView = temperatureView = cleanroomView = workspaceView = volumeView = RecipeIssue.OFFLINE.view();
                environmentDetail = List.of(Component.translatable(LANG_ENVIRONMENT), Component.translatable(RecipeIssue.OFFLINE.descriptionKey()).withStyle(ChatFormatting.GRAY));
                return;
            }
            boolean ready = machine.isWorkspaceReady();
            int oxygenated = machine.getOxygenatedBlockCount();
            if (oxygenated > 0) {
                oxygenView = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_OXYGEN_ON));
                temperatureView = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_TEMPERATURE_ON));
            } else if (working) {
                oxygenView = IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_OXYGEN_PENDING));
                temperatureView = IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_TEMPERATURE_PENDING));
            } else {
                oxygenView = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_OXYGEN_OFF));
                temperatureView = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_TEMPERATURE_OFF));
            }
            var types = machine.getTypes();
            Component typeName = types.isEmpty() ? FlowParts.DASH : Component.translatable(types.iterator().next().getTranslationKey());
            cleanroomView = ready ? IssueView.of(RecipeIssue.OK, typeName) : IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_WARMING));
            var machines = machine.getSpaceMachines();
            int total = 0, busy = 0;
            if (machines != null) {
                for (var inner : machines) {
                    total++;
                    if (inner.getRecipeLogic().isWorking()) busy++;
                }
            }
            if (total == 0) workspaceView = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_NO_MACHINE));
            else if (ready) workspaceView = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_MACHINE_COUNT, total));
            else workspaceView = IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_WARMING));
            int volume = machine.getInnerVolume();
            volumeView = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_BLOCK_COUNT, FormattingUtil.formatNumbers(volume)));
            environment = ready ? working ? FlowState.ACTIVE : FlowState.READY : working ? FlowState.WARNING : FlowState.IDLE;
            environmentDetail = List.of(Component.translatable(LANG_ENVIRONMENT),
                    Component.translatable(LANG_ENV_OXYGEN_DETAIL, FormattingUtil.formatNumbers(oxygenated)).withStyle(ChatFormatting.GRAY),
                    Component.translatable(LANG_ENV_CLEAN_DETAIL, typeName).withStyle(ChatFormatting.GRAY),
                    Component.translatable(LANG_ENV_WORK_DETAIL, total, busy).withStyle(ChatFormatting.GRAY),
                    Component.translatable(LANG_ENV_VOLUME_DETAIL, FormattingUtil.formatNumbers(volume)).withStyle(ChatFormatting.GRAY),
                    Component.translatable(ready ? LANG_READINESS_REACHED : LANG_READINESS_PENDING).withStyle(ready ? ChatFormatting.GREEN : ChatFormatting.GOLD));
        }

        private long collectWater() {
            var units = machine.getInputUnits();
            int count = units.size();
            if (unitWater.length < count) {
                unitWater = new long[count];
                unitScratch = new long[count];
            }
            long total = 0;
            for (int u = 0; u < count; u++) {
                unitWater[u] = 0;
                collectingUnit = u;
                units.get(u).forEachKey(AEKeyType.fluids(), true, waterCollector);
                total += unitWater[u];
            }
            return total < 0 ? Long.MAX_VALUE : total;
        }

        private int simulateSupply(int hatches, int amount) {
            int count = machine.getInputUnits().size();
            System.arraycopy(unitWater, 0, unitScratch, 0, Math.min(count, unitWater.length));
            int supplied = 0;
            for (int h = 0; h < hatches; h++) {
                boolean found = false;
                for (int u = 0; u < count && !found; u++) {
                    if (unitScratch[u] >= amount) {
                        unitScratch[u] -= amount;
                        found = true;
                    }
                }
                if (!found) break;
                supplied++;
            }
            return supplied;
        }
    }
}
