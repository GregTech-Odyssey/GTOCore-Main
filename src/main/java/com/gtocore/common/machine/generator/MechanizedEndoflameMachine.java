package com.gtocore.common.machine.generator;

import com.gtocore.utils.ManaUnification;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.mana.feature.IManaMachine;
import com.gtolib.api.machine.mana.trait.NotifiableManaContainer;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.suried.gtoutils.schedule.Task;
import vazkii.botania.api.mana.ManaEndpoint;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@DataGeneratorScanned
public final class MechanizedEndoflameMachine extends TieredMachine implements IManaMachine, IControllable, IFancyUIMachine {

    public static final long MANA_CAPACITY = 100_000L;
    /** 每个燃料 tick 产生的魔力，刻意压低以保持“缓慢”产出。 */
    public static final long MANA_PER_FUEL_TICK = 2L;

    @RegisterLanguage(cn = "消耗熔炉燃料，每个燃料 tick 产生 2 魔力", en = "Consumes furnace fuel: 2 mana per fuel tick")
    public static final Component TIP = Component.translatable("gtocore.machine.mechanized_endoflame.tooltip");
    @RegisterLanguage(cn = "燃料按熔炉燃烧时间逐渐转化为魔力", en = "Fuel is converted into mana over its furnace burn time")
    public static final Component TIP_FUEL = Component.translatable("gtocore.machine.mechanized_endoflame.tooltip.fuel");
    @RegisterLanguage(cn = "从正面输出魔力，可接入魔力管道、魔力池或魔源罐", en = "Outputs mana from the front; works with mana pipes, pools and source jars")
    public static final Component TIP_OUTPUT = Component.translatable("gtocore.machine.mechanized_endoflame.tooltip.output");
    @RegisterLanguage(cn = "魔力缓存", en = "Stored Mana")
    private static final Component LINE_STORED = Component.translatable("gtocore.machine.mechanized_endoflame.stored");
    @RegisterLanguage(cn = "剩余燃烧", en = "Burn Time")
    private static final Component LINE_BURN = Component.translatable("gtocore.machine.mechanized_endoflame.burn");
    @RegisterLanguage(cn = "魔力", en = "Mana")
    private static final Component BAR_MANA = Component.translatable("gtocore.machine.mechanized_endoflame.bar.mana");
    @RegisterLanguage(cn = "运行", en = "Running")
    private static final Component ROW_ENABLED = Component.translatable("gtocore.machine.mechanized_endoflame.enabled");

    @SaveToDisk
    private final NotifiableManaContainer manaContainer;
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> fuelInventory;
    private final ManaEndpoint manaEndpoint;

    @SaveToDisk(defaultValue = "0")
    private int burnTime;
    @SaveToDisk(defaultValue = "true")
    private boolean enabled = true;
    private Task tickTask;

    public MechanizedEndoflameMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier);
        manaContainer = new NotifiableManaContainer(this, IO.OUT, MANA_CAPACITY, MANA_CAPACITY);
        manaEndpoint = ManaUnification.of(manaContainer, 20);
        fuelInventory = NotifiableInventory.items(this, 1, IO.IN);
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
            tickTask = subscribeServerTick(tickTask, this::tick, 0);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    private void tick() {
        if (!enabled) return;
        outputMana();
        if (manaContainer.getCurrentMana() >= MANA_CAPACITY) return;
        if (burnTime <= 0) {
            consumeFuel();
        }
        if (burnTime <= 0) return;
        burnTime--;
        manaContainer.addMana(MANA_PER_FUEL_TICK, 20, false);
    }

    private void consumeFuel() {
        AEItemKey key = fuelInventory.storage.keyAt(0);
        if (key == null) return;
        ItemStack stack = key.getReadOnlyStack();
        int burn = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
        if (burn <= 0) return;
        if (fuelInventory.storage.extract(0, key, 1, false) != 1) return;
        burnTime = burn;
    }

    private void outputMana() {
        long mana = manaContainer.getCurrentMana();
        if (mana <= 0) return;
        var receiver = findManaReceiver(getFrontFacing(), null);
        if (receiver != null && !receiver.isFull()) {
            ManaEndpoint.transfer(manaEndpoint, receiver, mana);
            mana = manaContainer.getCurrentMana();
        }
        if (mana <= 0) return;
        var jar = findSourceJar(getFrontFacing());
        if (jar != null && jar.canAcceptSource()) {
            ManaEndpoint.transfer(manaEndpoint, ManaUnification.of(jar), mana);
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return enabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingAllowed) {
        enabled = workingAllowed;
    }

    @Override
    public Widget createUIWidget() {
        var status = new StatusPanel();
        status.addLine(LINE_STORED,
                () -> Component.literal(FormattingUtil.formatNumbers(manaContainer.getCurrentMana()) + " / " + FormattingUtil.formatNumbers(MANA_CAPACITY)));
        status.addLine(LINE_BURN,
                () -> Component.literal(burnTime + " /t"));
        var manaBar = ProgressBar.of(LayoutStyle.AUTO, BAR_MANA, UITheme.FLOW_CYAN_LIGHT,
                () -> new ProgressBar.Progress(manaContainer.getCurrentMana(), MANA_CAPACITY, 0));
        var fuelRow = UIElement.row(18).addChild(ItemSlot.of(fuelInventory.storage, 0));
        var enabledSwitch = Switch.of(this::isWorkingEnabled, this::setWorkingEnabled);
        return Form.page().addChildren(status, manaBar, fuelRow,
                Form.controlRow(ROW_ENABLED, enabledSwitch));
    }
}
