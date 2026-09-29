package com.gtocore.common.machine.noenergy;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.cover.CoverUIs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public class RedstoneTimerMachine extends MetaMachine implements IFancyUIMachine, IControllable {

    public static final int MAX_TICKS = 20 * 60 * 60 * 24;
    public static final int ALWAYS = 0;
    public static final int WITH_SIGNAL = 1;
    public static final int WITHOUT_SIGNAL = 2;
    public static final int VISUAL_RUNNING = 1;
    public static final int VISUAL_LIT = 2;

    private static final int VISUAL_HOLD = 6;
    private static final long[] TICK_STEPS = { 1, 20, 200, 1200 };
    private static final String ELAPSED_TAG = "timerElapsed";

    @RegisterLanguage(cn = "状态", en = "State")
    private static final String LINE_STATE = "gtocore.machine.redstone_timer.line.state";
    @RegisterLanguage(cn = "距下次输出", en = "Next pulse in")
    private static final String LINE_NEXT = "gtocore.machine.redstone_timer.line.next";
    @RegisterLanguage(cn = "本次剩余", en = "Pulse remaining")
    private static final String LINE_REMAINING = "gtocore.machine.redstone_timer.line.remaining";
    @RegisterLanguage(cn = "控制信号", en = "Control signal")
    private static final String LINE_SIGNAL = "gtocore.machine.redstone_timer.line.signal";
    @RegisterLanguage(cn = "输出中", en = "Emitting")
    private static final String STATE_EMITTING = "gtocore.machine.redstone_timer.state.emitting";
    @RegisterLanguage(cn = "间隔中", en = "Waiting")
    private static final String STATE_WAITING = "gtocore.machine.redstone_timer.state.waiting";
    @RegisterLanguage(cn = "已关闭", en = "Disabled")
    private static final String STATE_DISABLED = "gtocore.machine.redstone_timer.state.disabled";
    @RegisterLanguage(cn = "已暂停", en = "Paused")
    private static final String STATE_PAUSED = "gtocore.machine.redstone_timer.state.paused";
    @RegisterLanguage(cn = "等待控制信号", en = "Waiting for a control signal")
    private static final String DETAIL_WAIT_SIGNAL = "gtocore.machine.redstone_timer.detail.wait_signal";
    @RegisterLanguage(cn = "存在控制信号时暂停", en = "Paused while a control signal is present")
    private static final String DETAIL_SIGNAL_PRESENT = "gtocore.machine.redstone_timer.detail.signal_present";
    @RegisterLanguage(cn = "已由电源按钮或软锤关闭", en = "Turned off by the power button or a soft mallet")
    private static final String DETAIL_DISABLED = "gtocore.machine.redstone_timer.detail.disabled";
    @RegisterLanguage(cn = "不使用", en = "Unused")
    private static final String VALUE_UNUSED = "gtocore.machine.redstone_timer.value.unused";
    @RegisterLanguage(cn = "%s t（%s 秒）", en = "%s t (%s s)")
    private static final String VALUE_TICKS = "gtocore.machine.redstone_timer.value.ticks";
    @RegisterLanguage(cn = "周期（t）", en = "Cycle (t)")
    private static final String PROGRESS_CYCLE = "gtocore.machine.redstone_timer.progress.cycle";
    @RegisterLanguage(cn = "前段为间隔，红色段为输出", en = "The first part is the interval; the red part is the pulse")
    private static final String PROGRESS_TOOLTIP = "gtocore.machine.redstone_timer.progress.tooltip";
    @RegisterLanguage(cn = "重新计时", en = "Restart cycle")
    private static final String BUTTON_RESTART = "gtocore.machine.redstone_timer.button.restart";
    @RegisterLanguage(cn = "关闭输出并从间隔起点重新计时", en = "Stop the output and restart from the beginning of the interval")
    private static final String BUTTON_RESTART_TOOLTIP = "gtocore.machine.redstone_timer.button.restart.tooltip";
    @RegisterLanguage(cn = "计时器未运行", en = "The timer is not running")
    private static final String REASON_NOT_RUNNING = "gtocore.machine.redstone_timer.reason.not_running";
    @RegisterLanguage(cn = "计时", en = "Timing")
    private static final String SECTION_TIMING = "gtocore.machine.redstone_timer.section.timing";
    @RegisterLanguage(cn = "间隔（t）", en = "Interval (t)")
    private static final String ROW_INTERVAL = "gtocore.machine.redstone_timer.row.interval";
    @RegisterLanguage(cn = "两次输出之间无信号的时长，不含输出时长；20 t = 1 秒", en = "Time without signal between two pulses, excluding the pulse itself; 20 t = 1 s")
    private static final String ROW_INTERVAL_TOOLTIP = "gtocore.machine.redstone_timer.row.interval.tooltip";
    @RegisterLanguage(cn = "持续（t）", en = "Duration (t)")
    private static final String ROW_DURATION = "gtocore.machine.redstone_timer.row.duration";
    @RegisterLanguage(cn = "每个间隔结束后输出信号的时长", en = "How long the signal is emitted after each interval")
    private static final String ROW_DURATION_TOOLTIP = "gtocore.machine.redstone_timer.row.duration.tooltip";
    @RegisterLanguage(cn = "信号强度", en = "Signal strength")
    private static final String ROW_STRENGTH = "gtocore.machine.redstone_timer.row.strength";
    @RegisterLanguage(cn = "运行", en = "Operation")
    private static final String SECTION_OPERATION = "gtocore.machine.redstone_timer.section.operation";
    @RegisterLanguage(cn = "始终运行", en = "Always run")
    private static final String MODE_ALWAYS = "gtocore.machine.redstone_timer.mode.always";
    @RegisterLanguage(cn = "有控制信号时运行", en = "Run with control signal")
    private static final String MODE_WITH_SIGNAL = "gtocore.machine.redstone_timer.mode.with_signal";
    @RegisterLanguage(cn = "无控制信号时运行", en = "Run without control signal")
    private static final String MODE_WITHOUT_SIGNAL = "gtocore.machine.redstone_timer.mode.without_signal";
    @RegisterLanguage(cn = "控制信号从火把对侧输入", en = "The control signal enters from the side opposite the torch")
    private static final String MODE_TOOLTIP = "gtocore.machine.redstone_timer.mode.tooltip";
    @RegisterLanguage(cn = "暂停时保留进度", en = "Keep progress when paused")
    private static final String ROW_KEEP = "gtocore.machine.redstone_timer.row.keep";
    @RegisterLanguage(cn = "关闭时，恢复运行后从间隔起点开始", en = "When off, the cycle restarts from the beginning of the interval on resume")
    private static final String ROW_KEEP_TOOLTIP = "gtocore.machine.redstone_timer.row.keep.tooltip";
    @RegisterLanguage(cn = "始终运行时不会暂停", en = "Never pauses in Always run mode")
    private static final String REASON_ALWAYS = "gtocore.machine.redstone_timer.reason.always";

    @SaveToDisk(defaultValue = "20")
    private int interval = 20;
    @SaveToDisk(defaultValue = "2")
    private int duration = 2;
    @SaveToDisk(defaultValue = "15")
    private int strength = 15;
    @SaveToDisk(defaultValue = "true")
    private boolean enabled = true;
    @SaveToDisk(defaultValue = "0")
    private int controlMode = ALWAYS;
    @SaveToDisk(defaultValue = "false")
    private boolean keepProgress;
    @SyncToClient(scheduleUpdate = true)
    private int visual;

    @Nullable
    private TickableSubscription tickSubs;
    private boolean initialized;
    private boolean running;
    private boolean output;
    private int inputSignal;
    private int cycleStart;
    private int pausedElapsed;
    private int litUntil;

    public RedstoneTimerMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    private int period() {
        return interval + duration;
    }

    private Direction outputFace() {
        return getFrontFacing().getOpposite();
    }

    private Direction inputFace() {
        return getFrontFacing();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            initialized = false;
            tickSubs = subscribeServerTick(tickSubs, this::tickUpdate);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        cancelTicks();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        if (!initialized || isRemote()) return;
        Level level = getLevel();
        if (level != null && output) {
            var block = getBlockState().getBlock();
            var oldFront = getPos().relative(oldFacing.getOpposite());
            level.neighborChanged(oldFront, block, getPos());
            level.updateNeighborsAtExceptFromFacing(oldFront, block, oldFacing);
        }
        refresh();
        notifyRedstone();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        if (initialized) refresh();
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (forDrop) return;
        int elapsed = currentElapsed();
        if (elapsed > 0) tag.putInt(ELAPSED_TAG, elapsed);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        pausedElapsed = Math.max(0, tag.getInt(ELAPSED_TAG));
    }

    private void tickUpdate() {
        if (!initialized) {
            initialized = true;
            interval = clampTicks(interval);
            duration = clampTicks(duration);
            strength = Math.max(1, Math.min(15, strength));
            pausedElapsed %= period();
            refresh();
            return;
        }
        if (!running) return;
        int now = getOffsetTimer();
        int period = period();
        int elapsed = now - cycleStart;
        if (elapsed < 0) {
            cycleStart = now;
            elapsed = 0;
        }
        int phase = elapsed % period;
        if (elapsed >= period) cycleStart += elapsed - phase;
        boolean on = phase >= interval;
        if (on && !output) litUntil = now - (phase - interval) + Math.max(duration, VISUAL_HOLD);
        setOutput(on);
        int next = on ? period - phase : interval - phase;
        int hold = litUntil - now;
        if (hold > 0 && hold < next) next = hold;
        updateVisual(now);
        schedule(next);
    }

    private static int clampTicks(long value) {
        return (int) Math.max(1, Math.min(MAX_TICKS, value));
    }

    private void schedule(int delay) {
        var subs = tickSubs;
        Level level = getLevel();
        if (subs == null || level == null || level.getServer() == null) return;
        subs.cycle = delay;
        subs.lastTick = level.getServer().getTickCount() + delay;
    }

    private void refresh() {
        if (isRemote()) return;
        inputSignal = readInput();
        boolean shouldRun = shouldRun();
        if (shouldRun && !running) {
            start();
        } else if (!shouldRun && running) {
            stop();
        } else if (running) {
            tickUpdate();
        } else {
            if (!keepProgress || !enabled) pausedElapsed = 0;
            cancelTicks();
            setOutput(false);
            updateVisual(getOffsetTimer());
        }
    }

    private boolean shouldRun() {
        if (!enabled) return false;
        return switch (controlMode) {
            case WITH_SIGNAL -> inputSignal > 0;
            case WITHOUT_SIGNAL -> inputSignal == 0;
            default -> true;
        };
    }

    private void start() {
        running = true;
        cycleStart = getOffsetTimer() - pausedElapsed % period();
        pausedElapsed = 0;
        tickSubs = subscribeServerTick(tickSubs, this::tickUpdate);
        tickUpdate();
    }

    private void stop() {
        pausedElapsed = keepProgress && enabled ? currentElapsed() : 0;
        running = false;
        cancelTicks();
        setOutput(false);
        updateVisual(getOffsetTimer());
    }

    private void cancelTicks() {
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    private int currentElapsed() {
        if (!running) return pausedElapsed;
        int elapsed = getOffsetTimer() - cycleStart;
        return elapsed <= 0 ? 0 : elapsed % period();
    }

    private void setOutput(boolean on) {
        if (output == on) return;
        output = on;
        notifyRedstone();
    }

    private void notifyRedstone() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;
        var block = getBlockState().getBlock();
        var front = getPos().relative(outputFace());
        level.neighborChanged(front, block, getPos());
        level.updateNeighborsAtExceptFromFacing(front, block, outputFace().getOpposite());
    }

    private void updateVisual(int now) {
        int value = 0;
        if (running) value |= VISUAL_RUNNING;
        if (running && (output || now < litUntil)) value |= VISUAL_LIT;
        visual = value;
    }

    private int readInput() {
        if (controlMode == ALWAYS) return 0;
        Level level = getLevel();
        if (level == null) return 0;
        var face = inputFace();
        var pos = getPos().relative(face);
        int signal = level.getSignal(pos, face);
        if (signal >= 15) return signal;
        var state = level.getBlockState(pos);
        return state.is(Blocks.REDSTONE_WIRE) ? Math.max(signal, state.getValue(RedStoneWireBlock.POWER)) : signal;
    }

    public int getVisual() {
        return visual;
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        if (side != null && output && side.getOpposite() == outputFace()) return strength;
        return 0;
    }

    @Override
    public int getOutputDirectSignal(Direction direction) {
        return getOutputSignal(direction);
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return side == outputFace() || side == inputFace();
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public boolean isWorkingEnabled() {
        return enabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (enabled == isWorkingAllowed) return;
        enabled = isWorkingAllowed;
        settingsChanged();
    }

    private void setInterval(long value) {
        interval = clampTicks(value);
        pausedElapsed %= period();
        settingsChanged();
    }

    private void setDuration(long value) {
        duration = clampTicks(value);
        pausedElapsed %= period();
        settingsChanged();
    }

    private void setStrength(long value) {
        strength = (int) Math.max(1, Math.min(15, value));
        onChanged();
        if (output) notifyRedstone();
    }

    private void setControlMode(int mode) {
        if (mode < ALWAYS || mode > WITHOUT_SIGNAL || mode == controlMode) return;
        controlMode = mode;
        settingsChanged();
    }

    private void setKeepProgress(boolean keep) {
        keepProgress = keep;
        onChanged();
    }

    private void settingsChanged() {
        onChanged();
        if (initialized) refresh();
    }

    private void restart() {
        if (!running) return;
        cycleStart = getOffsetTimer();
        litUntil = 0;
        tickUpdate();
    }

    private Component stateText() {
        if (running) return Component.translatable(output ? STATE_EMITTING : STATE_WAITING);
        return Component.translatable(enabled ? STATE_PAUSED : STATE_DISABLED);
    }

    private StatusLine.Level stateLevel() {
        if (running) return StatusLine.Level.GOOD;
        return enabled ? StatusLine.Level.WARNING : StatusLine.Level.NORMAL;
    }

    private Component stateDetail() {
        if (running) return Component.empty();
        if (!enabled) return Component.translatable(DETAIL_DISABLED);
        return Component.translatable(controlMode == WITH_SIGNAL ? DETAIL_WAIT_SIGNAL : DETAIL_SIGNAL_PRESENT);
    }

    private static Component ticks(int ticks) {
        return Component.translatable(VALUE_TICKS, ticks, String.format("%.2f", ticks / 20.0));
    }

    private Component nextText() {
        if (!running) return Component.literal("—");
        int phase = currentElapsed();
        return ticks(phase < interval ? interval - phase : period() - phase + interval);
    }

    private Component remainingText() {
        int phase = currentElapsed();
        if (!running || phase < interval) return Component.literal("—");
        return ticks(period() - phase);
    }

    private Component signalText() {
        if (controlMode == ALWAYS) return Component.translatable(VALUE_UNUSED);
        return Component.literal(Integer.toString(inputSignal));
    }

    private ProgressBar.Progress progress() {
        return new ProgressBar.Progress(currentElapsed(), period(), 0);
    }

    @Override
    public Widget createUIWidget() {
        var status = new StatusPanel();
        status.addLine(LINE_STATE, this::stateText).level(this::stateLevel).detail(this::stateDetail);
        status.addLine(LINE_NEXT, this::nextText);
        status.addLine(LINE_REMAINING, this::remainingText);
        status.addLine(LINE_SIGNAL, this::signalText);
        var cycle = new ProgressBar(LayoutStyle.AUTO, Component.translatable(PROGRESS_CYCLE), UITheme.FLOW_AMBER_LIGHT, this::progress)
                .range(() -> new ProgressBar.Range(interval, period()), UITheme.FLOW_RED_MID);
        cycle.setHoverTooltips(PROGRESS_TOOLTIP);
        var restart = Button.translatable(LayoutStyle.AUTO, BUTTON_RESTART)
                .setOnServerClick(this::restart)
                .disabled(() -> !running, REASON_NOT_RUNNING);
        restart.setHoverTooltips(BUTTON_RESTART_TOOLTIP);

        var timing = CoverUIs.section(SECTION_TIMING).addChildren(
                CoverUIs.inlineNumberRow(ROW_INTERVAL, new NumberField(LayoutStyle.AUTO, () -> interval, this::setInterval,
                        () -> 1, () -> MAX_TICKS, TICK_STEPS), ROW_INTERVAL_TOOLTIP),
                CoverUIs.inlineNumberRow(ROW_DURATION, new NumberField(LayoutStyle.AUTO, () -> duration, this::setDuration,
                        () -> 1, () -> MAX_TICKS, TICK_STEPS), ROW_DURATION_TOOLTIP),
                CoverUIs.inlineNumberRow(ROW_STRENGTH, NumberField.of(LayoutStyle.AUTO, () -> strength, this::setStrength, 1, 15)));

        String[] modes = { MODE_ALWAYS, MODE_WITH_SIGNAL, MODE_WITHOUT_SIGNAL };
        var modeGroup = ButtonGroup.single(modes.length, i -> Component.translatable(modes[i]), () -> controlMode, this::setControlMode);
        modeGroup.setHoverTooltips(MODE_TOOLTIP);
        var operation = CoverUIs.section(SECTION_OPERATION).addChildren(
                modeGroup,
                CoverUIs.controlRow(ROW_KEEP, Switch.of(() -> keepProgress, this::setKeepProgress)
                        .disabled(() -> controlMode == ALWAYS, REASON_ALWAYS), ROW_KEEP_TOOLTIP));

        return CoverUIs.page().addChildren(status, cycle, restart, timing, operation);
    }
}
