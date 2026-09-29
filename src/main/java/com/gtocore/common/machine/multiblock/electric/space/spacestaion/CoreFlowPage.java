package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.api.machine.ILargeSpaceStationMachine;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
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
import com.gregtechceu.gtceu.uipro.styletemplate.WidgetIconAtlas;
import com.gregtechceu.gtceu.uiwidgets.flow.IssueLine;
import com.gregtechceu.gtceu.uiwidgets.flow.IssueView;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeDiagnoser;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeIssue;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import earth.terrarium.adastra.common.registry.ModBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DataGeneratorScanned
final class CoreFlowPage {

    private static final int COLUMN_WIDTH = 60;
    private static final int SIDE_WIDTH = 96;
    private static final int REFRESH_TICKS = 20;
    private static final int READY_TARGET = 10;
    private static final int CYCLE_TICKS = 20;

    @RegisterLanguage(cn = "空间站核心", en = "Station Core")
    private static final String LANG_CORE = "gtocore.machine.space_station.core.flow.core";
    @RegisterLanguage(cn = "全站服务", en = "Station Services")
    private static final String LANG_SERVICES = "gtocore.machine.space_station.core.flow.services";
    @RegisterLanguage(cn = "超净", en = "Clean")
    private static final String LANG_CLEAN = "gtocore.machine.space_station.core.flow.clean";
    @RegisterLanguage(cn = "电梯", en = "Elevator")
    private static final String LANG_ELEVATOR = "gtocore.machine.space_station.core.flow.elevator";
    @RegisterLanguage(cn = "激光", en = "Laser")
    private static final String LANG_LASER = "gtocore.machine.space_station.core.flow.laser";
    @RegisterLanguage(cn = "耗时 ×%s", en = "Time ×%s")
    private static final String LANG_DURATION = "gtocore.machine.space_station.core.flow.duration";
    @RegisterLanguage(cn = "已解锁", en = "Unlocked")
    private static final String LANG_UNLOCKED = "gtocore.machine.space_station.core.flow.unlocked";
    @RegisterLanguage(cn = "未解锁", en = "Locked")
    private static final String LANG_LOCKED = "gtocore.machine.space_station.core.flow.locked";
    @RegisterLanguage(cn = "无", en = "None")
    private static final String LANG_NONE = "gtocore.machine.space_station.core.flow.none";
    @RegisterLanguage(cn = "舱段", en = "Segments")
    private static final String LANG_SEGMENTS = "gtocore.machine.space_station.core.flow.segments";
    @RegisterLanguage(cn = "已接入", en = "Attached")
    private static final String LANG_ATTACHED = "gtocore.machine.space_station.core.flow.attached";
    @RegisterLanguage(cn = "成型", en = "Formed")
    private static final String LANG_FORMED = "gtocore.machine.space_station.core.flow.formed";
    @RegisterLanguage(cn = "连接舱", en = "Connectors")
    private static final String LANG_CONNECTORS = "gtocore.machine.space_station.core.flow.connectors";
    @RegisterLanguage(cn = "功能舱", en = "Functional")
    private static final String LANG_FUNCTIONAL = "gtocore.machine.space_station.core.flow.functional";
    @RegisterLanguage(cn = "点左侧「空间站总览」查看整座空间站并扩建", en = "Use \"Station Overview\" on the left to view and extend the station")
    private static final String LANG_HINT = "gtocore.machine.space_station.core.flow.hint";
    @RegisterLanguage(cn = "舱段未成型：%s", en = "Unformed: %s")
    private static final String LANG_UNFORMED = "gtocore.machine.space_station.core.flow.unformed";
    @RegisterLanguage(cn = "每轮输入与耗电随已接入舱段数增加：%s 份。", en = "Inputs and power per cycle grow with attached segments: %s shares.")
    private static final String LANG_SHARES = "gtocore.machine.space_station.core.flow.shares";
    @RegisterLanguage(cn = "状态", en = "State")
    private static final String LANG_STATE = "gtocore.machine.space_station.core.flow.state";
    @RegisterLanguage(cn = "进度", en = "Progress")
    private static final String LANG_PROGRESS = "gtocore.machine.space_station.core.flow.progress";
    @RegisterLanguage(cn = "就绪度", en = "Readiness")
    private static final String LANG_READINESS = "gtocore.machine.space_station.core.flow.readiness";
    @RegisterLanguage(cn = "电力", en = "Power")
    private static final String LANG_ENERGY = "gtocore.machine.space_station.core.flow.energy";
    @RegisterLanguage(cn = "耗能", en = "Usage")
    private static final String LANG_USAGE = "gtocore.machine.space_station.core.flow.usage";
    @RegisterLanguage(cn = "功率", en = "Input")
    private static final String LANG_POWER = "gtocore.machine.space_station.core.flow.power";
    @RegisterLanguage(cn = "电压", en = "Tier")
    private static final String LANG_VOLTAGE = "gtocore.machine.space_station.core.flow.voltage";
    @RegisterLanguage(cn = "不在太空", en = "Not in Space")
    private static final String LANG_NOT_IN_SPACE = "gtocore.machine.space_station.core.flow.not_in_space";
    @RegisterLanguage(cn = "等待就绪", en = "Warming Up")
    private static final String LANG_WARMING = "gtocore.machine.space_station.core.flow.warming";
    @RegisterLanguage(cn = "就绪", en = "Ready")
    private static final String LANG_READY = "gtocore.machine.space_station.core.flow.ready";

    private CoreFlowPage() {}

    static Widget create(Core core, FancyMachineUIWidget window) {
        boolean remote = window.isRemote();
        var status = new Status(core);
        int inputs = status.diagnoser.inputCount(), outputs = status.diagnoser.outputCount();
        int columns = Math.max(3, inputs);
        int[] widths = new int[columns + 1];
        Arrays.fill(widths, COLUMN_WIDTH);
        widths[columns] = SIDE_WIDTH;
        var chart = new FlowChart(UISizes.FLOW_GUTTER, widths);
        var station = stationNode(chart, core, status, columns);
        for (int i = 0; i < inputs; i++) chart.link(inputNode(chart, status, i, remote), station);
        chart.link(energyNode(chart, status), station);
        var services = servicesNode(chart, status, columns);
        chart.link(station, services).follow(services);
        for (int i = 0; i < outputs; i++) {
            var output = outputNode(chart, status, i, i, remote);
            chart.link(station, output).follow(output);
        }
        var segments = segmentsNode(chart, status, outputs, columns + 1 - outputs);
        chart.link(station, segments).follow(segments);
        return UIElement.column(LayoutStyle.AUTO).addChild(chart.toView(window, false, core.getDefinition().getId().toString()));
    }

    private static FlowNode stationNode(FlowChart chart, Core core, Status status, int columns) {
        var node = chart.node(1, 1, columns - 1).state(status.live(() -> status.station)).detail(status.live(() -> status.stationDetail));
        node.addChildren(FlowParts.header(ItemView.of(core.getDefinition().asStack()), LANG_CORE),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status.live(() -> status.stationView)),
                new ProgressBar(LayoutStyle.AUTO, Component.translatable(LANG_PROGRESS), UITheme.FLOW_CYAN_LIGHT, status.live(() -> status.recipeProgress)),
                new ProgressBar(LayoutStyle.AUTO, Component.translatable(LANG_READINESS), UITheme.STATUS_ONLINE, status.live(() -> status.readiness)).percent());
        return node;
    }

    private static FlowNode inputNode(FlowChart chart, Status status, int index, boolean remote) {
        var node = chart.node(0, index).state(status.live(() -> status.diagnoser.inputIssue(index).state()))
                .detail(status.live(() -> status.diagnoser.inputDetail(index)));
        node.layout(l -> l.justifyContent(AlignContent.CENTER));
        node.addChildren(FlowParts.centered(FlowParts.fluidSlot(remote ? null : status.diagnoser.inputStack(index))),
                TextLine.of(LayoutStyle.AUTO, () -> status.diagnoser.inputName(index)).alignCenter(),
                TextLine.of(LayoutStyle.AUTO, () -> status.diagnoser.inputAmount(index)).alignCenter()
                        .level(status.live(() -> status.diagnoser.inputIssue(index).state().level())),
                new IssueLine(LayoutStyle.AUTO, null, status.live(() -> status.diagnoser.inputIssue(index).view())));
        return node;
    }

    private static FlowNode outputNode(FlowChart chart, Status status, int column, int index, boolean remote) {
        var node = chart.node(2, column).state(status.live(() -> status.diagnoser.outputIssue(index).state()))
                .detail(status.live(() -> status.diagnoser.outputDetail(index)));
        node.layout(l -> l.justifyContent(AlignContent.CENTER));
        node.addChildren(FlowParts.centered(FlowParts.fluidSlot(remote ? null : status.diagnoser.outputStack(index))),
                TextLine.of(LayoutStyle.AUTO, () -> status.diagnoser.outputName(index)).alignCenter(),
                TextLine.of(LayoutStyle.AUTO, () -> status.diagnoser.outputAmount(index)).alignCenter()
                        .level(status.live(() -> status.diagnoser.outputIssue(index).state().level())),
                new IssueLine(LayoutStyle.AUTO, null, status.live(() -> status.diagnoser.outputIssue(index).view())));
        return node;
    }

    private static FlowNode energyNode(FlowChart chart, Status status) {
        var node = chart.node(1, 0).state(status.live(() -> status.diagnoser.energyIssue().state())).detail(status.live(() -> status.energyDetail));
        IGuiTexture icon = UITheme.switching(() -> node.getFlowState() == FlowState.ACTIVE || node.getFlowState() == FlowState.READY,
                new WidgetIconAtlas.PixelExact(WidgetIcons.ENERGY_OFF), new WidgetIconAtlas.PixelExact(WidgetIcons.ENERGY_ON));
        node.addChildren(FlowParts.header(ItemView.of(icon), LANG_ENERGY),
                new IssueLine(LayoutStyle.AUTO, null, status.live(() -> status.diagnoser.energyIssue().view())),
                StatusLine.of(LayoutStyle.AUTO, LANG_USAGE, status.live(() -> status.usageText)),
                StatusLine.of(LayoutStyle.AUTO, LANG_POWER, status.live(() -> status.powerText)),
                StatusLine.of(LayoutStyle.AUTO, LANG_VOLTAGE, status.live(() -> status.voltageText)));
        return node;
    }

    private static FlowNode servicesNode(FlowChart chart, Status status, int column) {
        var node = chart.node(1, column).state(status.live(() -> status.services));
        node.addChildren(FlowParts.header(ItemView.of(new ItemStack(ModBlocks.OXYGEN_DISTRIBUTOR.get())), LANG_SERVICES),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_CLEAN), status.live(() -> status.cleanView)),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_ELEVATOR), status.live(() -> status.elevatorView)),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_LASER), status.live(() -> status.laserView)));
        return node;
    }

    private static FlowNode segmentsNode(FlowChart chart, Status status, int column, int span) {
        var node = chart.node(2, column, span).state(status.live(() -> status.segments)).detail(status.live(() -> status.segmentsDetail));
        node.addChildren(FlowParts.header(ItemView.of(SpaceMultiblock.SPACE_STATION_DOCKING_MODULE.asStack()), LANG_SEGMENTS),
                StatusLine.of(LayoutStyle.AUTO, LANG_ATTACHED, status.live(() -> status.attachedText)),
                StatusLine.of(LayoutStyle.AUTO, LANG_FORMED, status.live(() -> status.formedText))
                        .level(status.live(() -> status.allFormed ? StatusLine.Level.GOOD : StatusLine.Level.ERROR)),
                StatusLine.of(LayoutStyle.AUTO, LANG_CONNECTORS, status.live(() -> status.connectorText)),
                StatusLine.of(LayoutStyle.AUTO, LANG_FUNCTIONAL, status.live(() -> status.functionalText)),
                TextLine.translatable(LayoutStyle.AUTO, LANG_HINT).setColor(UITheme.TEXT_SECONDARY));
        return node;
    }

    private static final class Status extends ThrottledStatus {

        private final Core core;
        private RecipeDiagnoser diagnoser;
        private int shares;
        private FlowState station = FlowState.IDLE;
        private IssueView stationView = RecipeIssue.IDLE.view();
        private List<Component> stationDetail = Collections.emptyList();
        private List<Component> energyDetail = Collections.emptyList();
        private IssueView cleanView = RecipeIssue.IDLE.view();
        private IssueView elevatorView = RecipeIssue.IDLE.view();
        private IssueView laserView = RecipeIssue.IDLE.view();
        private FlowState services = FlowState.IDLE;
        private Component usageText = FlowParts.DASH, powerText = FlowParts.DASH, voltageText = FlowParts.DASH;
        private Component attachedText = FlowParts.DASH, formedText = FlowParts.DASH, connectorText = FlowParts.DASH, functionalText = FlowParts.DASH;
        private boolean allFormed = true;
        private FlowState segments = FlowState.IDLE;
        private List<Component> segmentsDetail = Collections.emptyList();
        private ProgressBar.Progress recipeProgress = new ProgressBar.Progress(0, CYCLE_TICKS, 0);
        private ProgressBar.Progress readiness = new ProgressBar.Progress(0, READY_TARGET, 0);

        private Status(Core core) {
            super(core::getOffsetTimer, REFRESH_TICKS);
            this.core = core;
            this.shares = core.getModules().size() + 1;
            this.diagnoser = new RecipeDiagnoser(core, core.buildCycleRecipe());
        }

        @Override
        protected void onAccess(int now) {
            var logic = core.getRecipeLogic();
            int progress = logic.isActive() && logic.getMaxProgress() > 0 ? logic.getProgress() : 0;
            int max = logic.isActive() && logic.getMaxProgress() > 0 ? logic.getMaxProgress() : CYCLE_TICKS;
            if (progress != recipeProgress.current() || max != recipeProgress.total()) recipeProgress = new ProgressBar.Progress(progress, max, 0);
            int ready = Math.min(READY_TARGET, core.getReadyCount());
            if (ready != readiness.current()) readiness = new ProgressBar.Progress(ready, READY_TARGET, 0);
        }

        @Override
        protected void refresh(int now) {
            var logic = core.getRecipeLogic();
            int count = core.getModules().size() + 1;
            if (count != shares) {
                shares = count;
                diagnoser = new RecipeDiagnoser(core, core.buildCycleRecipe());
            }
            boolean formed = core.isFormed();
            boolean working = logic.isWorking();
            boolean inCycle = working || logic.isWaiting();
            diagnoser.diagnoseFluids(formed, inCycle);
            diagnoser.diagnoseEnergy(formed, working, core.getEnergyContainer(), core.getMaxVoltage(), core.getTier());
            usageText = Component.literal(FormattingUtil.formatNumbers(diagnoser.getEUt()));
            powerText = Component.literal(FormattingUtil.formatNumberReadable(diagnoser.power()));
            voltageText = diagnoser.energyTier() < 0 ? FlowParts.DASH : Component.literal(GTValues.VN[diagnoser.energyTier()]);
            var energyLines = new ArrayList<Component>(diagnoser.energyDetail().size() + 1);
            energyLines.add(Component.translatable(LANG_ENERGY));
            energyLines.addAll(diagnoser.energyDetail());
            energyDetail = energyLines;
            refreshStation(formed, working, inCycle);
            refreshServices(formed);
            refreshSegments();
        }

        private void refreshStation(boolean formed, boolean working, boolean inCycle) {
            IssueView view;
            if (!formed) view = RecipeIssue.UNFORMED.view();
            else if (!core.isInSpace()) view = IssueView.of(RecipeIssue.CONDITION, Component.translatable(LANG_NOT_IN_SPACE));
            else if (working) view = core.getReadyCount() < READY_TARGET ? IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_WARMING)) : RecipeIssue.RUNNING.view();
            else if (inCycle) view = RecipeIssue.WAITING.view();
            else if (!diagnoser.inputsSatisfied()) view = RecipeIssue.INPUT_SHORT.view();
            else if (!diagnoser.energySatisfied()) view = diagnoser.energyIssue().view();
            else view = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_READY));
            stationView = view;
            station = working ? FlowState.ACTIVE : view.issue().state();
            stationDetail = List.of(Component.translatable(LANG_CORE),
                    Component.translatable(view.issue().descriptionKey()).withStyle(RecipeDiagnoser.style(view.issue())),
                    Component.translatable(LANG_SHARES, shares).withStyle(ChatFormatting.GRAY));
        }

        private void refreshServices(boolean formed) {
            var types = core.getTypes();
            cleanView = types.isEmpty() ? IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_NONE)) :
                    IssueView.of(RecipeIssue.OK, Component.translatable(types.iterator().next().getTranslationKey()));
            double multiplier = core.getDurationMultiplier();
            elevatorView = multiplier >= 1 ? IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_NONE)) :
                    IssueView.of(RecipeIssue.OK, Component.translatable(LANG_DURATION, FormattingUtil.formatNumbers(multiplier)));
            laserView = core.hasLaserBoost() ? IssueView.of(RecipeIssue.OK, Component.translatable(LANG_UNLOCKED)) :
                    IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_LOCKED));
            services = !formed ? FlowState.IDLE : !types.isEmpty() || multiplier < 1 ? FlowState.READY : FlowState.IDLE;
        }

        private void refreshSegments() {
            int attached = 0, formedCount = 0, connectors = 0, functional = 0;
            var unformed = new ArrayList<Component>();
            for (ILargeSpaceStationMachine module : core.getModules()) {
                attached++;
                var machine = module.self();
                if (machine.isFormed()) formedCount++;
                else unformed.add(machine.getDefinition().asStack().getHoverName());
                if (StationOverviewAdapter.categoryOf(machine.getDefinition()) == StationOverviewAdapter.CONNECTORS) connectors++;
                else functional++;
            }
            attachedText = Component.literal(String.valueOf(attached));
            formedText = Component.literal(formedCount + " / " + attached);
            connectorText = Component.literal(String.valueOf(connectors));
            functionalText = Component.literal(String.valueOf(functional));
            allFormed = formedCount == attached;
            segments = attached == 0 ? FlowState.IDLE : allFormed ? FlowState.READY : FlowState.WARNING;
            var lines = new ArrayList<Component>();
            lines.add(Component.translatable(LANG_SEGMENTS));
            for (var name : unformed) lines.add(Component.translatable(LANG_UNFORMED, name).withStyle(ChatFormatting.RED));
            lines.add(Component.translatable(LANG_HINT).withStyle(ChatFormatting.GRAY));
            segmentsDetail = lines;
        }
    }
}
