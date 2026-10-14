package com.gtocore.common.machine.multiblock.electric.space;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.machines.GTOMachineProtocols;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.MachineProtocol;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
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
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeDiagnoser;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeIssue;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@DataGeneratorScanned
final class ElevatorFlowPage {

    private static final int COLUMN_WIDTH = 76;
    private static final int SIDE_WIDTH = 104;
    private static final int REFRESH_TICKS = 20;

    @RegisterLanguage(cn = "电梯运行", en = "Elevator Run")
    private static final String LANG_RUN = "gtocore.machine.space_elevator.flow.run";
    @RegisterLanguage(cn = "已装满", en = "Full")
    private static final String LANG_SPOOL_FULL = "gtocore.machine.space_elevator.flow.spool_full";
    @RegisterLanguage(cn = "补充中", en = "Refilling")
    private static final String LANG_SPOOL_FILLING = "gtocore.machine.space_elevator.flow.spool_filling";
    @RegisterLanguage(cn = "缺 %s 个", en = "%s missing")
    private static final String LANG_SPOOL_MISSING = "gtocore.machine.space_elevator.flow.spool_missing";
    @RegisterLanguage(cn = "%s EU/t", en = "%s EU/t")
    private static final String LANG_EUT = "gtocore.machine.space_elevator.flow.eut";
    @RegisterLanguage(cn = "电力", en = "Power")
    private static final String LANG_ENERGY = "gtocore.machine.space_elevator.flow.energy";
    @RegisterLanguage(cn = "电压", en = "Tier")
    private static final String LANG_VOLTAGE = "gtocore.machine.space_elevator.flow.voltage";
    @RegisterLanguage(cn = "状态", en = "State")
    private static final String LANG_STATE = "gtocore.machine.space_elevator.flow.state";
    @RegisterLanguage(cn = "进度", en = "Progress")
    private static final String LANG_PROGRESS = "gtocore.machine.space_elevator.flow.progress";
    @RegisterLanguage(cn = "动力模块", en = "Power Module")
    private static final String LANG_POWER_MODULE = "gtocore.machine.space_elevator.flow.power_module";
    @RegisterLanguage(cn = "算力", en = "Computation")
    private static final String LANG_COMPUTATION = "gtocore.machine.space_elevator.flow.computation";
    @RegisterLanguage(cn = "%s CWU/t", en = "%s CWU/t")
    private static final String LANG_CWU = "gtocore.machine.space_elevator.flow.cwu";
    @RegisterLanguage(cn = "需要 %s 及以上电压", en = "Needs %s or higher")
    private static final String LANG_LOW_TIER = "gtocore.machine.space_elevator.flow.low_tier";
    @RegisterLanguage(cn = "太空电梯运行需要 %s 及以上电压；电压越高，模块耗时越短，算力需求越高。", en = "The elevator needs %s or higher; a higher tier shortens module durations and raises the computation demand.")
    private static final String LANG_TIER_DESC = "gtocore.machine.space_elevator.flow.tier_desc";
    @RegisterLanguage(cn = "每轮运行 20 秒，需要 %s 电力与 %s 算力。模块仅在太空电梯运行时工作。", en = "Each cycle lasts 20 seconds and needs %s power and %s computation. Modules only work while the elevator runs.")
    private static final String LANG_CORE_DESC = "gtocore.machine.space_elevator.flow.core_desc";
    @RegisterLanguage(cn = "就绪", en = "Ready")
    private static final String LANG_READY = "gtocore.machine.space_elevator.flow.ready";
    @RegisterLanguage(cn = "纳米管线轴", en = "Nanotube Spools")
    private static final String LANG_SPOOL = "gtocore.machine.space_elevator.flow.spool";
    @RegisterLanguage(cn = "运行时从输入总线补充纳米管线轴，装满 %s 个后显示电梯缆绳。", en = "While running, nanotube spools are taken from the input bus; the elevator cable appears once %s are stored.")
    private static final String LANG_SPOOL_DESC = "gtocore.machine.space_elevator.flow.spool_desc";
    @RegisterLanguage(cn = "轨道空间站", en = "Orbital Station")
    private static final String LANG_ORBIT = "gtocore.machine.space_elevator.flow.orbit";
    @RegisterLanguage(cn = "已连接", en = "Linked")
    private static final String LANG_LINKED = "gtocore.machine.space_elevator.flow.linked";
    @RegisterLanguage(cn = "未连接", en = "Not Linked")
    private static final String LANG_UNLINKED = "gtocore.machine.space_elevator.flow.unlinked";
    @RegisterLanguage(cn = "倍率", en = "Multiplier")
    private static final String LANG_MULTIPLIER = "gtocore.machine.space_elevator.flow.multiplier";
    @RegisterLanguage(cn = "当前星球轨道上的空间站装有太空电梯连接舱且就绪时自动连接：模块耗时进一步降低，算力需求变为 %s 倍。", en = "Links automatically to a ready Space Elevator Connector Module on a station in this planet's orbit: module durations drop further and the computation demand becomes %s times.")
    private static final String LANG_ORBIT_DESC = "gtocore.machine.space_elevator.flow.orbit_desc";
    @RegisterLanguage(cn = "太空电梯模块", en = "Elevator Modules")
    private static final String LANG_MODULES = "gtocore.machine.space_elevator.flow.modules";
    @RegisterLanguage(cn = "巨型模块", en = "Mega Modules")
    private static final String LANG_MEGA = "gtocore.machine.space_elevator.flow.mega";
    @RegisterLanguage(cn = "已安装", en = "Installed")
    private static final String LANG_INSTALLED = "gtocore.machine.space_elevator.flow.installed";
    @RegisterLanguage(cn = "已接入", en = "Linked")
    private static final String LANG_ATTACHED = "gtocore.machine.space_elevator.flow.attached";
    @RegisterLanguage(cn = "并行", en = "Parallel")
    private static final String LANG_PARALLEL = "gtocore.machine.space_elevator.flow.parallel";
    @RegisterLanguage(cn = "耗时", en = "Duration")
    private static final String LANG_DURATION = "gtocore.machine.space_elevator.flow.duration";
    @RegisterLanguage(cn = "点左侧「总览」查看整座结构并在空接口上安装模块", en = "Use \"Overview\" on the left to view the structure and install modules at free ports")
    private static final String LANG_HINT = "gtocore.machine.space_elevator.flow.hint";
    @RegisterLanguage(cn = "在左侧「总览」中安装模块", en = "Install modules in \"Overview\"")
    private static final String LANG_HINT_SHORT = "gtocore.machine.space_elevator.flow.hint_short";
    @RegisterLanguage(cn = "模块并行上限为 %s^(动力模块等级-1)，耗时倍率随太空电梯电压提高而降低。", en = "Module parallel limit is %s^(power module tier - 1); the duration multiplier falls as the elevator tier rises.")
    private static final String LANG_MODULE_DESC = "gtocore.machine.space_elevator.flow.module_desc";
    @RegisterLanguage(cn = "未成型或未接入：%s", en = "Unformed or not linked: %s")
    private static final String LANG_DETACHED = "gtocore.machine.space_elevator.flow.detached";
    @RegisterLanguage(cn = "太空电梯装配线的并行上限为 2^(动力模块等级-1)。", en = "The Space Elevator Assembly Line has a parallel limit of 2^(power module tier - 1).")
    private static final String LANG_ASSEMBLY_LINE = "gtocore.machine.space_elevator.flow.assembly_line";

    private ElevatorFlowPage() {}

    static Widget create(SpaceElevatorMachine machine, FancyMachineUIWidget window) {
        boolean road = machine instanceof SuperSpaceElevatorMachine;
        var status = new Status(machine, road);
        var chart = new FlowChart(UISizes.FLOW_GUTTER_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH, SIDE_WIDTH);
        var energy = inputNode(chart, 0, node -> ItemView.of(FlowParts.energyIcon(node)), LANG_ENERGY,
                status.live(() -> status.diagnoser.energyIssue().state()), status.live(() -> status.energyDetail),
                status.live(() -> status.usageText), status.live(() -> status.usageLevel), status.live(() -> status.diagnoser.energyIssue().view()));
        var computation = inputNode(chart, 1, node -> ItemView.of(GTResearchMachines.COMPUTATION_HATCH_RECEIVER.asStack()), LANG_COMPUTATION,
                status.live(() -> status.core), status.live(() -> status.coreDetail), status.live(() -> status.computationText), () -> Level.NORMAL,
                status.live(() -> status.computationView));
        var spool = inputNode(chart, 2, node -> ItemView.of(GTOItems.NANOTUBE_SPOOL.asStack()), LANG_SPOOL, status.live(() -> status.spool),
                status.live(() -> status.spoolDetail), status.live(() -> status.spoolText), status.live(() -> status.spoolLevel), status.live(() -> status.spoolView));
        var core = coreNode(chart, machine, status);
        var orbit = orbitNode(chart, status);
        chart.link(energy, core);
        chart.link(computation, core);
        chart.link(spool, core);
        chart.link(orbit, core);
        var modules = moduleNode(chart, status, status.normal, 0, road ? 2 : 4, LANG_MODULES, MultiBlockD.ASSEMBLER_MODULE.asStack());
        chart.link(core, modules).follow(modules);
        if (road) {
            var mega = moduleNode(chart, status, status.mega, 2, 2, LANG_MEGA, MultiBlockH.MEGA_ASSEMBLER.asStack());
            chart.link(core, mega).follow(mega);
        }
        return UIElement.column(LayoutStyle.AUTO).addChild(chart.toView(window, false, machine.getDefinition().getId().toString()));
    }

    private static FlowNode inputNode(FlowChart chart, int column, Function<FlowNode, Widget> icon, String name, Supplier<FlowState> state,
                                      Supplier<List<Component>> detail, Supplier<Component> value, Supplier<Level> level, Supplier<IssueView> issue) {
        var node = chart.node(0, column).bindState(state).bindDetail(detail);
        return FlowParts.slotBody(node, icon.apply(node), TextLine.translatable(LayoutStyle.AUTO, name), value, level, issue);
    }

    private static FlowNode orbitNode(FlowChart chart, Status status) {
        var node = chart.node(1, 3).bindState(status.live(() -> status.orbit)).bindDetail(status.live(() -> status.orbitDetail));
        node.addChildren(FlowParts.header(ItemView.of(SpaceMultiblock.SPACE_ELEVATOR_CONNECTOR_MODULE.asStack()), LANG_ORBIT),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status.live(() -> status.orbitView)),
                StatusLine.of(LayoutStyle.AUTO, LANG_MULTIPLIER, status.live(() -> status.orbitText)));
        return node;
    }

    private static FlowNode coreNode(FlowChart chart, SpaceElevatorMachine machine, Status status) {
        var node = chart.node(1, 0, 3).bindState(status.live(() -> status.core)).bindDetail(status.live(() -> status.coreDetail));
        node.addChildren(FlowParts.header(ItemView.of(machine.getDefinition().asStack()), LANG_RUN),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status.live(() -> status.coreView)),
                StatusLine.of(LayoutStyle.AUTO, LANG_VOLTAGE, status.live(() -> status.voltageText))
                        .bindLevel(status.live(() -> status.voltageOk ? Level.GOOD : Level.ERROR)),
                StatusLine.of(LayoutStyle.AUTO, LANG_POWER_MODULE, status.live(() -> status.powerModuleText)),
                ProgressBar.of(LayoutStyle.AUTO, Component.translatable(LANG_PROGRESS), UITheme.FLOW_CYAN_LIGHT, status.live(() -> status.progress)));
        return node;
    }

    private static FlowNode moduleNode(FlowChart chart, Status status, Group group, int column, int span, String title, ItemStack icon) {
        var node = chart.node(2, column, span).bindState(status.live(() -> group.state)).bindDetail(status.live(() -> group.detail));
        node.addChildren(FlowParts.header(ItemView.of(icon), title),
                StatusLine.of(LayoutStyle.AUTO, LANG_INSTALLED, status.live(() -> group.installedText)),
                StatusLine.of(LayoutStyle.AUTO, LANG_ATTACHED, status.live(() -> group.attachedText))
                        .bindLevel(status.live(() -> group.allAttached ? Level.GOOD : Level.ERROR)),
                StatusLine.of(LayoutStyle.AUTO, LANG_PARALLEL, status.live(() -> group.parallelText)),
                StatusLine.of(LayoutStyle.AUTO, LANG_DURATION, status.live(() -> group.durationText)));
        if (!group.mega) node.addChild(TextLine.translatable(LayoutStyle.AUTO, LANG_HINT_SHORT).setColor(UITheme.TEXT_SECONDARY));
        return node;
    }

    private static Component times(String value) {
        return Component.literal("×" + value);
    }

    private static Component ratio(int current, int total) {
        return Component.literal(current + " / " + total);
    }

    private static final class Group {

        private final MachineProtocol protocol;
        private final boolean road;
        private final boolean mega;
        private FlowState state = FlowState.IDLE;
        private List<Component> detail = Collections.emptyList();
        private Component installedText = FlowParts.DASH, attachedText = FlowParts.DASH, parallelText = FlowParts.DASH, durationText = FlowParts.DASH;
        private boolean allAttached = true;

        Group(MachineProtocol protocol, boolean road, boolean mega) {
            this.protocol = protocol;
            this.road = road;
            this.mega = mega;
        }

        void refresh(SpaceElevatorMachine machine, boolean running, int tier, int powerTier, double multiplier) {
            var assembly = machine.isFormed() ? machine.getAssembly() : null;
            var level = machine.getLevel();
            int ports = 0, installed = 0, attached = 0;
            var names = new ArrayList<Component>();
            if (assembly != null && level != null) {
                for (var port : assembly.ports(protocol)) {
                    ports++;
                    if (!level.isLoaded(port) || !(MetaMachine.getMachine(level, port) instanceof SpaceElevatorModuleMachine module)) continue;
                    installed++;
                    if (module.isFormed() && module.getController() == machine) attached++;
                    else names.add(module.getDefinition().asStack().getHoverName());
                }
            }
            installedText = ratio(installed, ports);
            attachedText = Component.literal(String.valueOf(attached));
            allAttached = attached == installed;
            parallelText = running && powerTier > 0 ? times(FormattingUtil.formatNumbers(SpaceElevatorModuleMachine.parallelLimit(road, powerTier))) : FlowParts.DASH;
            durationText = running && tier > GTValues.ZPM ?
                    times(FormattingUtil.formatNumber2Places(SpaceElevatorModuleMachine.durationMultiplier(multiplier, tier, road))) : FlowParts.DASH;
            state = installed == 0 ? FlowState.IDLE : !allAttached ? FlowState.WARNING : running ? FlowState.ACTIVE : FlowState.READY;
            var lines = new ArrayList<Component>(names.size() + 4);
            lines.add(Component.translatable(mega ? LANG_MEGA : LANG_MODULES));
            lines.add(Component.translatable(LANG_MODULE_DESC, SpaceElevatorModuleMachine.parallelBase(road)).withStyle(ChatFormatting.GRAY));
            if (mega) lines.add(Component.translatable(LANG_ASSEMBLY_LINE).withStyle(ChatFormatting.GRAY));
            for (var name : names) lines.add(Component.translatable(LANG_DETACHED, name).withStyle(ChatFormatting.RED));
            lines.add(Component.translatable(LANG_HINT).withStyle(ChatFormatting.GRAY));
            detail = lines;
        }
    }

    private static final class Status extends ThrottledStatus {

        private final SpaceElevatorMachine machine;
        private final boolean road;
        final Group normal;
        final Group mega;
        private RecipeDiagnoser diagnoser;
        private int diagnoserTier = -1;
        private FlowState core = FlowState.IDLE;
        private IssueView coreView = RecipeIssue.IDLE.view();
        private List<Component> coreDetail = Collections.emptyList();
        private List<Component> energyDetail = Collections.emptyList();
        private Component usageText = FlowParts.DASH, voltageText = FlowParts.DASH;
        private boolean voltageOk;
        private Component powerModuleText = FlowParts.DASH, computationText = FlowParts.DASH;
        private Component spoolText = FlowParts.DASH;
        private IssueView spoolView = RecipeIssue.IDLE.view();
        private Level spoolLevel = Level.NORMAL;
        private Level usageLevel = Level.NORMAL;
        private IssueView computationView = RecipeIssue.IDLE.view();
        private FlowState spool = FlowState.IDLE;
        private List<Component> spoolDetail = Collections.emptyList();
        private FlowState orbit = FlowState.IDLE;
        private IssueView orbitView = RecipeIssue.IDLE.view();
        private Component orbitText = FlowParts.DASH;
        private List<Component> orbitDetail = Collections.emptyList();
        private ProgressBar.Progress progress = ProgressBar.Progress.EMPTY;

        Status(SpaceElevatorMachine machine, boolean road) {
            super(machine::getOffsetTimer, REFRESH_TICKS);
            this.machine = machine;
            this.road = road;
            this.normal = new Group(GTOMachineProtocols.SPACE_ELEVATOR_MODULE, road, false);
            this.mega = new Group(GTOMachineProtocols.MEGA_SPACE_ELEVATOR_MODULE, true, true);
            this.diagnoser = diagnoser(SpaceElevatorMachine.REQUIRED_TIER);
        }

        private RecipeDiagnoser diagnoser(int tier) {
            diagnoserTier = tier;
            return new RecipeDiagnoser(machine, machine.getRecipeBuilder().duration(SpaceElevatorMachine.CYCLE_TICKS).EUt(GTValues.VA[tier]).build());
        }

        @Override
        protected void onAccess(int now) {
            var logic = machine.getRecipeLogic();
            int current = logic.isActive() && logic.getMaxProgress() > 0 ? logic.getProgress() : 0;
            int max = logic.isActive() && logic.getMaxProgress() > 0 ? logic.getMaxProgress() : SpaceElevatorMachine.CYCLE_TICKS;
            if (current != progress.current() || max != progress.total()) progress = new ProgressBar.Progress(current, max, 0);
        }

        @Override
        protected void refresh(int now) {
            var logic = machine.getRecipeLogic();
            boolean formed = machine.isFormed();
            boolean working = logic.isWorking();
            int tier = machine.getTier();
            int planned = Math.max(SpaceElevatorMachine.REQUIRED_TIER, Math.min(GTValues.MAX, tier));
            if (planned != diagnoserTier) diagnoser = diagnoser(planned);
            diagnoser.diagnoseEnergy(formed, working, machine.getEnergyContainer(), machine.getMaxVoltage(), tier);
            usageText = Component.translatable(LANG_EUT, FormattingUtil.formatNumbers(diagnoser.getEUt()));
            usageLevel = formed && diagnoser.energyIssue().isProblem() ? Level.ERROR : Level.NORMAL;
            computationView = working ? RecipeIssue.RUNNING.view() : RecipeIssue.IDLE.view();
            voltageOk = formed && tier >= SpaceElevatorMachine.REQUIRED_TIER;
            voltageText = formed ? Component.literal(GTValues.VN[Math.max(0, Math.min(GTValues.MAX, tier))]) : FlowParts.DASH;
            var energyLines = new ArrayList<Component>(diagnoser.energyDetail().size() + 2);
            energyLines.add(Component.translatable(LANG_ENERGY));
            energyLines.addAll(diagnoser.energyDetail());
            energyLines.add(Component.translatable(LANG_TIER_DESC, GTValues.VN[SpaceElevatorMachine.REQUIRED_TIER]).withStyle(ChatFormatting.GRAY));
            energyDetail = energyLines;
            int powerTier = formed ? machine.getCasingTier(GTORecipeDataKeys.POWER_MODULE_TIER) : 0;
            powerModuleText = powerTier > 0 ? Component.literal("MK " + powerTier) : FlowParts.DASH;
            long cwu = SpaceElevatorMachine.computationDemand(planned, machine.exCWUt());
            computationText = Component.translatable(LANG_CWU, FormattingUtil.formatNumbers(cwu));
            refreshCore(formed, working, tier, cwu, logic.isWaiting() ? null : working ? null : logic.getIdleReason());
            refreshSpool(formed);
            refreshOrbit(formed);
            var link = machine.getNetMachineCache();
            double multiplier = link == null ? 1.0 : link.getDurationMultiplier();
            int moduleTier = working ? tier : 0;
            normal.refresh(machine, working, moduleTier, powerTier, multiplier);
            if (road) mega.refresh(machine, working, moduleTier, powerTier, multiplier);
        }

        private void refreshCore(boolean formed, boolean working, int tier, long cwu, Component reason) {
            IssueView view;
            if (!formed) view = RecipeIssue.UNFORMED.view();
            else if (tier < SpaceElevatorMachine.REQUIRED_TIER) {
                view = IssueView.of(RecipeIssue.LOW_VOLTAGE, Component.translatable(LANG_LOW_TIER, GTValues.VN[SpaceElevatorMachine.REQUIRED_TIER]));
            } else if (working) view = RecipeIssue.RUNNING.view();
            else if (machine.getRecipeLogic().isWaiting()) view = RecipeIssue.WAITING.view();
            else if (!diagnoser.energySatisfied()) view = diagnoser.energyIssue().view();
            else if (reason != null) view = IssueView.of(RecipeIssue.CONDITION, reason);
            else view = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_READY));
            coreView = view;
            core = working ? FlowState.ACTIVE : view.issue().state();
            var lines = new ArrayList<Component>(4);
            lines.add(machine.getDefinition().asStack().getHoverName());
            lines.add((reason != null && !working ? reason.copy() : Component.translatable(view.issue().descriptionKey())).withStyle(RecipeDiagnoser.style(view.issue())));
            lines.add(Component.translatable(LANG_CORE_DESC, Component.literal(FormattingUtil.formatNumbers(diagnoser.getEUt()) + " EU/t"),
                    Component.translatable(LANG_CWU, FormattingUtil.formatNumbers(cwu))).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(LANG_TIER_DESC, GTValues.VN[SpaceElevatorMachine.REQUIRED_TIER]).withStyle(ChatFormatting.GRAY));
            coreDetail = lines;
        }

        private void refreshSpool(boolean formed) {
            int count = machine.getSpoolCount(), max = machine.getMaxSpoolCount();
            spoolText = ratio(count, max);
            spool = !formed ? FlowState.IDLE : count >= max ? FlowState.READY : FlowState.WARNING;
            spoolLevel = count >= max ? Level.GOOD : Level.NORMAL;
            spoolView = count >= max ? IssueView.of(RecipeIssue.OK, Component.translatable(LANG_SPOOL_FULL)) :
                    machine.getRecipeLogic().isWorking() ? IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_SPOOL_FILLING)) :
                            IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_SPOOL_MISSING, max - count));
            spoolDetail = List.of(Component.translatable(LANG_SPOOL), Component.translatable(LANG_SPOOL_DESC, max).withStyle(ChatFormatting.GRAY));
        }

        private void refreshOrbit(boolean formed) {
            var link = machine.getNetMachineCache();
            if (link != null) {
                orbitView = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_LINKED));
                orbitText = times(FormattingUtil.formatNumber2Places(Math.sqrt(link.getDurationMultiplier())));
                orbit = formed ? FlowState.READY : FlowState.IDLE;
            } else {
                orbitView = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_UNLINKED));
                orbitText = FlowParts.DASH;
                orbit = FlowState.IDLE;
            }
            orbitDetail = List.of(Component.translatable(LANG_ORBIT),
                    Component.translatable(LANG_ORBIT_DESC, FormattingUtil.formatNumber2Places(SpaceElevatorMachine.linkedComputationFactor()))
                            .withStyle(ChatFormatting.GRAY));
        }
    }
}
