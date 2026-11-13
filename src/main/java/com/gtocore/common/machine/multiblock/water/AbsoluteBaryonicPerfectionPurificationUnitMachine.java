package com.gtocore.common.machine.multiblock.water;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AbsoluteBaryonicPerfectionPurificationUnitMachine extends WaterPurificationUnitMachine {

    private static final List<Item> CATALYST = List.of(
            GTOItems.UP_QUARK_RELEASING_CATALYST.get(),
            GTOItems.DOWN_QUARK_RELEASING_CATALYST.get(),
            GTOItems.STRANGE_QUARK_RELEASING_CATALYST.get(),
            GTOItems.CHARM_QUARK_RELEASING_CATALYST.get(),
            GTOItems.BOTTOM_QUARK_RELEASING_CATALYST.get(),
            GTOItems.TOP_QUARK_RELEASING_CATALYST.get());

    private static final Fluid QUARK_GLUON = GTOMaterials.QuarkGluon.getFluid(FluidStorageKeys.PLASMA);
    private static final Fluid STABLE_BARYONIC_MATTER = GTOMaterials.StableBaryonicMatter.getFluid();

    @SaveToDisk
    @SyncToClient
    private Item catalyst1;
    @SaveToDisk
    @SyncToClient
    private Item catalyst2;

    @SaveToDisk
    private Item lastCatalyst;

    @SaveToDisk(defaultValue = "0")
    private long inputCount;

    @SaveToDisk(defaultValue = "0")
    private long pendingMatter;

    @SaveToDisk(defaultValue = "0")
    private long pendingWater;

    @SaveToDisk(defaultValue = "false")
    private boolean successful;

    @SaveToDisk
    private final List<ItemStack> outputs = new ArrayList<>();

    private final List<ItemBusPartMachine> busMachines = new ArrayList<>();

    public AbsoluteBaryonicPerfectionPurificationUnitMachine(MetaMachineBlockEntity holder) {
        super(holder, 128);
    }

    @Override
    void addWorkingText(List<Component> textList) {
        if (catalyst1 != null && catalyst2 != null) textList.add(Component.translatable("gtocore.machine.absolute_baryonic_perfection_purification_unit.items", catalyst1.getDescription(), catalyst2.getDescription()));
        super.addWorkingText(textList);
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (!getRecipeLogic().isWorking() && getRecipeLogic().getLastRecipe() != null) addWorkingText(textList);
    }

    @Override
    double getSuccessChance() {
        return successful ? 100 : 0;
    }

    private int signal() {
        if (successful || catalyst1 == null || catalyst2 == null || getRecipeLogic().getLastRecipe() == null) return 0;
        int a = CATALYST.indexOf(catalyst1);
        int b = CATALYST.indexOf(catalyst2);
        int low = Math.min(a, b);
        return low * 5 - low * (low - 1) / 2 + Math.abs(a - b);
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        int signal = signal();
        return signal > 0 ? signal : super.getOutputSignal(side);
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return true;
    }

    @Override
    public void onPartScan(IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof ItemBusPartMachine itemBusPart) {
            IO io = itemBusPart.getInventory().getHandlerIO();
            if (io == IO.IN || io == IO.BOTH) {
                busMachines.add(itemBusPart);
            }
        }
    }

    @Override
    public void onStructureFormed() {
        busMachines.clear();
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        busMachines.clear();
    }

    @Override
    void onLogicReset() {
        if (isRemote() || catalyst1 == null && catalyst2 == null && lastCatalyst == null) return;
        catalyst1 = null;
        catalyst2 = null;
        lastCatalyst = null;
        updateSignal();
        onChanged();
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        var level = getLevel();
        if (level == null) return;
        for (ItemStack stack : outputs) Block.popResource(level, getPos(), stack);
        outputs.clear();
    }

    @Override
    public void beforeWorking(RecipeHandlerUnit unit, GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        flush();
        int a = GTValues.RNG.nextInt(6);
        int b = GTValues.RNG.nextInt(5);
        if (b >= a) b++;
        catalyst1 = CATALYST.get(a);
        catalyst2 = CATALYST.get(b);
        lastCatalyst = null;
        successful = false;
        updateSignal();
        onChanged();
    }

    @Override
    public void onWorking() {
        super.onWorking();
        if (successful || getOffsetTimer() % 20 != 0 || getRecipeLogic().getLastRecipe() == null) return;
        if (scan()) onChanged();
    }

    private boolean scan() {
        boolean changed = false;
        for (ItemBusPartMachine bus : busMachines) {
            var inv = bus.getInventory().storage;
            for (int i = 0, slots = inv.size(); i < slots; i++) {
                AEItemKey key = inv.keyAt(i);
                if (key == null) continue;
                Item item = key.getItem();
                if (!CATALYST.contains(item)) continue;
                long count = inv.amountAt(i);
                boolean pair = isPair(item);
                if (!inputFluid(QUARK_GLUON, pair ? 144L : count * 144L)) return changed;
                if (pair) {
                    inv.set(i, count > 1 ? key : null, count - 1);
                    takeLast();
                    outputs.add(new ItemStack(GTOItems.UNALIGNED_QUARK_RELEASING_CATALYST.get(), 2));
                    pendingMatter += 1000;
                    lastCatalyst = null;
                    successful = true;
                    flushMatter();
                    updateSignal();
                    return true;
                }
                inv.set(i, null, 0);
                outputs.add(Keys.toStack(key, count));
                lastCatalyst = item;
                changed = true;
            }
        }
        return changed;
    }

    private boolean isPair(Item item) {
        return lastCatalyst != null && (lastCatalyst == catalyst1 && item == catalyst2 || lastCatalyst == catalyst2 && item == catalyst1);
    }

    private void takeLast() {
        for (int i = outputs.size() - 1; i >= 0; i--) {
            ItemStack stack = outputs.get(i);
            if (stack.getItem() != lastCatalyst) continue;
            stack.shrink(1);
            if (stack.isEmpty()) outputs.remove(i);
            return;
        }
    }

    private void flushMatter() {
        if (pendingMatter > 0 && outputFluid(STABLE_BARYONIC_MATTER, pendingMatter)) pendingMatter = 0;
    }

    private void flush() {
        if (pendingWater > 0 && outputFluid(WaterPurificationPlantMachine.GradePurifiedWater8, pendingWater)) pendingWater = 0;
        flushMatter();
        outputs.removeIf(stack -> {
            var key = AEItemKey.of(stack);
            return key == null || output(key, stack.getCount());
        });
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        if (successful) pendingWater += inputCount * 9 / 10;
        successful = false;
        catalyst1 = null;
        catalyst2 = null;
        lastCatalyst = null;
        flush();
        updateSignal();
        onChanged();
    }

    @Override
    long prepareRecipe(RecipeHandlerUnit unit) {
        eut = 0;
        inputCount = Math.min(parallel(), unit.getFluidAmount(true, WaterPurificationPlantMachine.GradePurifiedWater7)[0]);
        if (inputCount > 0) {
            recipe = getRecipeBuilder().duration(WaterPurificationPlantMachine.DURATION).inputFluids(WaterPurificationPlantMachine.GradePurifiedWater7, inputCount).buildRawRecipe();
            if (matchRecipe(unit, recipe)) calculateVoltage(inputCount);
        }
        return eut;
    }
}
