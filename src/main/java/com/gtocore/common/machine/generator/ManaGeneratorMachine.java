package com.gtocore.common.machine.generator;

import com.gtocore.utils.ManaUnification;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.mana.feature.IManaMachine;
import com.gtolib.api.machine.mana.trait.NotifiableManaContainer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import vazkii.botania.api.mana.ManaEndpoint;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@DataGeneratorScanned
public final class ManaGeneratorMachine extends TieredEnergyMachine implements IManaMachine, IControllable, IFancyUIMachine {

    public static final long MANA_CAPACITY = 100000;
    /** 电力换魔力的比例：1 EU 换 4 魔力。 */
    private static final long MANA_PER_EU = 4;

    @RegisterLanguage(cn = "魔力缓存", en = "Stored Mana")
    private static final String LINE_STORED = "gtocore.machine.mana_generator.stored";
    @RegisterLanguage(cn = "魔力产出", en = "Mana Production")
    private static final String LINE_PRODUCTION = "gtocore.machine.mana_generator.production";
    @RegisterLanguage(cn = "电力缓存", en = "Stored Energy")
    private static final String LINE_ENERGY = "gtocore.machine.mana_generator.energy";
    @RegisterLanguage(cn = "魔力", en = "Mana")
    private static final String BAR_MANA = "gtocore.machine.mana_generator.bar.mana";
    @RegisterLanguage(cn = "电力", en = "Energy")
    private static final String BAR_ENERGY = "gtocore.machine.mana_generator.bar.energy";
    @RegisterLanguage(cn = "运行", en = "Running")
    private static final String ROW_ENABLED = "gtocore.machine.mana_generator.row.enabled";

    @SaveToDisk
    private final NotifiableManaContainer manaContainer;
    private final ManaEndpoint manaEndpoint;
    private final long energyPerTick;
    private final long manaPerTick;
    @SaveToDisk(defaultValue = "true")
    private boolean enabled = true;
    private TickableSubscription tickSubs;

    public ManaGeneratorMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier);
        energyPerTick = GTValues.V[tier];
        manaPerTick = manaPerTick(tier);
        manaContainer = new NotifiableManaContainer(this, IO.OUT, MANA_CAPACITY, MANA_CAPACITY);
        manaEndpoint = ManaUnification.of(manaContainer, 20);
    }

    /** 某档次的每 tick 魔力产出（= 该档位电压 × 换算比）。 */
    public static long manaPerTick(int tier) {
        return GTValues.V[tier] * MANA_PER_EU;
    }

    @Override
    public IManaContainer getManaContainer() {
        return manaContainer;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubs = subscribeServerTick(tickSubs, this::tickUpdate, 20);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    private void tickUpdate() {
        if (!enabled) return;
        outputMana();
        generateMana();
    }

    private void outputMana() {
        Level level = getLevel();
        if (level == null) return;
        long mana = manaContainer.getCurrentMana();
        if (mana < 1) return;
        var facing = getFrontFacing();
        var receiver = findManaReceiver(facing, null);
        if (receiver != null && !receiver.isFull()) {
            ManaEndpoint.transfer(manaEndpoint, receiver, mana);
        }
        mana = manaContainer.getCurrentMana();
        if (mana < 1) return;
        var jarTile = findSourceJar(facing);
        if (jarTile == null || !jarTile.canAcceptSource()) return;
        ManaEndpoint.transfer(manaEndpoint, ManaUnification.of(jarTile), mana);
    }

    private void generateMana() {
        long space = manaContainer.getMaxMana() - manaContainer.getCurrentMana();
        if (space <= 0) return;
        long ticks = Math.min(20L, energyContainer.getEnergyStored() / energyPerTick);
        if (ticks <= 0) return;
        ticks = Math.min(ticks, space / manaPerTick);
        if (ticks <= 0) return;
        energyContainer.removeEnergy(ticks * energyPerTick);
        manaContainer.addMana(ticks * manaPerTick, 20, false);
    }

    @Override
    public boolean isWorkingEnabled() {
        return enabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        enabled = isWorkingAllowed;
    }

    private Component storedText() {
        return Component.literal(FormattingUtil.formatNumbers(manaContainer.getCurrentMana()) + " / " + FormattingUtil.formatNumbers(MANA_CAPACITY));
    }

    private Component productionText() {
        return Component.literal(FormattingUtil.formatNumbers(enabled ? manaPerTick : 0) + " /t");
    }

    private Component energyText() {
        return Component.literal(FormattingUtil.formatNumbers(energyContainer.getEnergyStored()) + " / " + FormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()) + " EU");
    }

    @Override
    public Widget createUIWidget() {
        var status = new StatusPanel();
        status.addLine(LINE_STORED, this::storedText);
        status.addLine(LINE_PRODUCTION, this::productionText);
        status.addLine(LINE_ENERGY, this::energyText);
        var manaBar = ProgressBar.of(LayoutStyle.AUTO, Component.translatable(BAR_MANA), UITheme.FLOW_CYAN_LIGHT,
                () -> new ProgressBar.Progress(manaContainer.getCurrentMana(), MANA_CAPACITY, 0));
        var energyBar = ProgressBar.of(LayoutStyle.AUTO, Component.translatable(BAR_ENERGY), UITheme.FLOW_AMBER_LIGHT,
                () -> new ProgressBar.Progress(energyContainer.getEnergyStored(), energyContainer.getEnergyCapacity(), 0)).percent();
        var enabledSwitch = Switch.of(this::isWorkingEnabled, this::setWorkingEnabled);
        return Form.page().addChildren(status, manaBar, energyBar, Form.controlRow(ROW_ENABLED, enabledSwitch));
    }
}
