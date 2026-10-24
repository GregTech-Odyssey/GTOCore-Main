package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.CircuitHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInfiniteSource;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uipro.elements.ae.PhantomAEKeySlot;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeInputDualHatchPartMachine extends WorkableTieredIOPartMachine implements IDistinctPart {

    private static final int GRID_SIZE = 6;
    public static final int SLOT_COUNT = GRID_SIZE * GRID_SIZE;

    @Getter
    @SaveToDisk
    private final NotifiableInfiniteSource<AEItemKey> inventory;
    @Getter
    @SaveToDisk
    private final NotifiableInfiniteSource<AEFluidKey> tank;
    @Getter
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> circuitInventory;

    @Getter
    @SaveToDisk(defaultValue = "false")
    private boolean isDistinct;

    public CreativeInputDualHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        inventory = new NotifiableInfiniteSource<>(this, KeyInventory.items(SLOT_COUNT).setUniqueKeys(true), IO.IN, IO.IN, false);
        tank = new NotifiableInfiniteSource<>(this, KeyInventory.fluids(SLOT_COUNT, 1).setUniqueKeys(true), IO.IN, IO.IN, false);
        circuitInventory = CircuitHandler.create(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getHandlerUnit().setDistinct(isDistinct);
    }

    @Override
    public void setDistinct(boolean distinct) {
        isDistinct = distinct;
        getHandlerUnit().setDistinctAndNotify(distinct);
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IDistinctPart.super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
    }

    @Override
    public Widget createUIWidget() {
        return Form.page().addChild(UIElement.centeredRow(GRID_SIZE * UISizes.SLOT_SIZE)
                .addChildren(
                        UIElement.flexSpacer(),
                        SlotGrid.of(GRID_SIZE, SLOT_COUNT, index -> PhantomAEKeySlot.of(
                                () -> getKey(index), key -> setKey(index, key), key -> acceptsKey(index, key)).xeiPhantom()),
                        UIElement.flexSpacer()));
    }

    @Nullable
    private AEKey getKey(int index) {
        var item = inventory.config.keyAt(index);
        return item == null ? tank.config.keyAt(index) : item;
    }

    private boolean acceptsKey(int index, AEKey key) {
        KeyInventory<?> config;
        if (key instanceof AEItemKey) config = inventory.config;
        else if (key instanceof AEFluidKey) config = tank.config;
        else return false;
        int existing = config.indexOf(key);
        return existing < 0 || existing == index;
    }

    private void setKey(int index, @Nullable AEKey key) {
        if (getKey(index) == key) return;
        // 两种输入按同一索引共享一个幽灵槽，切换类型时清除原来的配置。
        inventory.config.set(index, key instanceof AEItemKey ? key : null, 1);
        tank.config.set(index, key instanceof AEFluidKey ? key : null, 1);
    }
}
