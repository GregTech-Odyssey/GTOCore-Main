package com.gtocore.common.machine.mana;

import com.gtocore.utils.ManaUnification;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.mana.feature.IManaMachine;
import com.gtolib.api.machine.mana.trait.NotifiableManaContainer;
import com.gtolib.utils.MathUtil;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 超级魔力缸：单个方块存下 long 上限的魔力，并自动把存量输出给相邻的魔力接收方（魔力池、魔力机器、魔力管道）与魔源容器。
 * <p>
 * 本身实现 {@link ManaPool}，所以魔力管道会把它当魔力池直接抽取；两个方向都按 {@link ManaUnification} 的
 * 4 魔源 = 1 魔力折算，输出魔源时只送 4 的整数倍，扣掉的魔力与送出的魔源严格等价。
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@DataGeneratorScanned
public final class SuperManaTankMachine extends MetaMachine implements IManaMachine, ManaPool, ISourceTile, IControllable, IFancyUIMachine {

    /** 内部缓存上限：long 最大值。 */
    public static final long MAX_MANA = Long.MAX_VALUE;
    /** 每分钟输出一次（20 tick）。 */
    private static final int OUTPUT_INTERVAL = 20;
    /** 未知魔力接收方按这个上限估算空位。 */
    private static final int UNKNOWN_RECEIVER_CAPACITY = 10000000;

    @RegisterLanguage(cn = "魔力存量", en = "Stored Mana")
    private static final String LINE_MANA = "gtocore.machine.super_mana_tank.mana";
    @RegisterLanguage(cn = "可折魔源", en = "Convertible Source")
    private static final String LINE_SOURCE = "gtocore.machine.super_mana_tank.source";
    @RegisterLanguage(cn = "自动输出", en = "Auto Output")
    private static final String ROW_OUTPUT = "gtocore.machine.super_mana_tank.output";

    @SaveToDisk
    private final NotifiableManaContainer manaContainer;
    @SaveToDisk(defaultValue = "true")
    private boolean autoOutput = true;
    @Nullable
    private TickableSubscription outputSubs;

    public SuperManaTankMachine(MetaMachineBlockEntity holder) {
        super(holder);
        // 上限给 long 最大值，但每秒 IO 上限只给 int 上限：容器算 rateMultiplier * maxIORate 时会溢出
        manaContainer = new NotifiableManaContainer(this, IO.BOTH, MAX_MANA, Integer.MAX_VALUE);
    }

    @Override
    public @NotNull IManaContainer getManaContainer() {
        return manaContainer;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public boolean isOutputtingPower() {
        return autoOutput;
    }

    /** Botania 的上限是 int，这里按 int 上限报告（真实存量仍按 long 记）。 */
    @Override
    public int getMaxMana() {
        return MathUtil.saturatedCast(MAX_MANA);
    }

    @Override
    public Optional<DyeColor> getColor() {
        return Optional.empty();
    }

    @Override
    public void setColor(Optional<DyeColor> color) {}

    //////////////////////////////////////
    // ********** 自动输出 **********//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            outputSubs = subscribeServerTick(outputSubs, this::tickOutput, OUTPUT_INTERVAL);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (outputSubs != null) {
            outputSubs.unsubscribe();
            outputSubs = null;
        }
    }

    private void tickOutput() {
        if (!autoOutput || manaContainer.getCurrentMana() <= 0) return;
        Level level = getLevel();
        if (level == null) return;
        for (var facing : GTUtil.DIRECTIONS) {
            if (manaContainer.getCurrentMana() <= 0) return;
            // 邻居缓存：方块状态与方块实体都直接取缓存，不再按坐标查找
            var be = holder.getNeighborBlockEntity(facing);
            if (be == null) continue;
            var receiver = XplatAbstractions.INSTANCE.findManaReceiver(level, getPos().relative(facing),
                    holder.getNeighborBlockState(facing), be, facing.getOpposite());
            if (receiver != null) {
                outputMana(receiver);
            } else if (be instanceof ISourceTile sourceTile) {
                outputSource(sourceTile);
            }
        }
    }

    private void outputMana(ManaReceiver receiver) {
        if (receiver == this || receiver.isFull() || !receiver.canReceiveManaFromBursts()) return;
        long space = getMaxMana(receiver) - receiver.getCurrentMana();
        long send = Math.min(manaContainer.getCurrentMana(), space);
        if (send <= 0) return;
        int amount = MathUtil.saturatedCast(send);
        receiver.receiveMana(amount);
        manaContainer.removeManaUnrestricted(amount, false);
    }

    /** 输出魔源只送 4 的整数倍，这样扣掉的魔力与送出的魔源严格等价。 */
    private void outputSource(ISourceTile sourceTile) {
        if (sourceTile == this || !sourceTile.canAcceptSource()) return;
        long space = sourceTile.getMaxSource() - sourceTile.getSource();
        if (space <= 0) return;
        long source = Math.min(space, ManaUnification.manaToSource(manaContainer.getCurrentMana()));
        source -= source % ManaUnification.SOURCE_PER_MANA;
        if (source <= 0) return;
        int amount = MathUtil.saturatedCast(source);
        sourceTile.addSource(amount);
        manaContainer.removeManaUnrestricted(ManaUnification.sourceToMana(amount), false);
    }

    private static int getMaxMana(ManaReceiver receiver) {
        if (receiver instanceof ManaCollector collector) return collector.getMaxMana();
        if (receiver instanceof ManaPool pool) return pool.getMaxMana();
        if (receiver instanceof SparkAttachable attachable) return attachable.getAvailableSpaceForMana();
        return receiver.isFull() ? 0 : UNKNOWN_RECEIVER_CAPACITY;
    }

    //////////////////////////////////////
    // ********** 魔源容纳 **********//
    //////////////////////////////////////

    @Override
    public int getTransferRate() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canAcceptSource() {
        return manaContainer.getCurrentMana() < MAX_MANA;
    }

    @Override
    public int getSource() {
        return MathUtil.saturatedCast(ManaUnification.manaToSource(manaContainer.getCurrentMana()));
    }

    @Override
    public int getMaxSource() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setMaxSource(int maxSource) {}

    @Override
    public int setSource(int source) {
        manaContainer.setCurrentMana(ManaUnification.sourceToMana(source));
        return getSource();
    }

    @Override
    public int addSource(int source) {
        if (source > 0) manaContainer.addManaUnrestricted(ManaUnification.sourceToMana(source), false);
        return getSource();
    }

    /** 向上取整扣魔力：手出去的魔源绝不会比扣掉的魔力更值钱，不会被套利。 */
    @Override
    public int removeSource(int amount) {
        if (amount > 0) {
            long mana = (amount + ManaUnification.SOURCE_PER_MANA - 1L) / ManaUnification.SOURCE_PER_MANA;
            manaContainer.removeManaUnrestricted(mana, false);
        }
        return getSource();
    }

    //////////////////////////////////////
    // ********** 控制与界面 **********//
    //////////////////////////////////////

    @Override
    public boolean isWorkingEnabled() {
        return autoOutput;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        autoOutput = isWorkingAllowed;
    }

    private Component manaText() {
        return Component.literal(FormattingUtil.formatNumbers(manaContainer.getCurrentMana()) + " / ∞");
    }

    private Component sourceText() {
        return Component.literal(FormattingUtil.formatNumbers(ManaUnification.manaToSource(manaContainer.getCurrentMana())));
    }

    @Override
    public Widget createUIWidget() {
        var status = new StatusPanel();
        status.addLine(LINE_MANA, this::manaText);
        status.addLine(LINE_SOURCE, this::sourceText);
        var outputSwitch = Switch.of(this::isWorkingEnabled, this::setWorkingEnabled);
        return Form.page().addChildren(status, Form.controlRow(ROW_OUTPUT, outputSwitch));
    }
}
