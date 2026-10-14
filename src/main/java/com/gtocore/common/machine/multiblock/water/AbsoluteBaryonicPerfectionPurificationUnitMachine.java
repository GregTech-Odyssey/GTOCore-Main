package com.gtocore.common.machine.multiblock.water;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AbsoluteBaryonicPerfectionPurificationUnitMachine extends WaterPurificationUnitMachine {

    private static final List<Item> CATALYST = List.of(
            GTOItems.UP_QUARK_RELEASING_CATALYST.get(),
            GTOItems.DOWN_QUARK_RELEASING_CATALYST.get(),
            GTOItems.BOTTOM_QUARK_RELEASING_CATALYST.get(),
            GTOItems.TOP_QUARK_RELEASING_CATALYST.get(),
            GTOItems.STRANGE_QUARK_RELEASING_CATALYST.get(),
            GTOItems.CHARM_QUARK_RELEASING_CATALYST.get());

    private static final Fluid QUARK_GLUON = GTOMaterials.QuarkGluon.getFluid(FluidStorageKeys.PLASMA);
    private static final Fluid STABLE_BARYONIC_MATTER = GTOMaterials.StableBaryonicMatter.getFluid();

    @SaveToDisk
    @SyncToClient
    private Item catalyst1;
    @SaveToDisk
    @SyncToClient
    private Item catalyst2;

    @SaveToDisk(defaultValue = "0")
    private long inputCount;

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
        textList.add(Component.translatable("gtocore.machine.absolute_baryonic_perfection_purification_unit.items", catalyst1.getDescription(), catalyst2.getDescription()));
        super.addWorkingText(textList);
    }

    @Override
    double getSuccessChance() {
        return successful ? 100 : 0;
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
    public void onWorking() {
        super.onWorking();
        if (getOffsetTimer() % 20 == 0) {
            boolean successful = false;
            for (ItemBusPartMachine bus : busMachines) {
                var inv = bus.getInventory().storage;
                int slots = inv.size();
                for (int i = 0; i < slots; i++) {
                    AEItemKey key = inv.keyAt(i);
                    if (key == null) continue;
                    long count = inv.amountAt(i);
                    if (CATALYST.contains(key.getItem()) && inputFluid(QUARK_GLUON, count * 144L)) {
                        if (i < slots - 1 && key.getItem() == catalyst1) {
                            AEItemKey key1 = inv.keyAt(i + 1);
                            long count1 = inv.amountAt(i + 1);
                            if (key1 != null && inputFluid(QUARK_GLUON, count1 * 144L)) {
                                if (key1.getItem() == catalyst2) {
                                    outputFluid(STABLE_BARYONIC_MATTER, 1000);
                                    successful = true;
                                    this.successful = true;
                                }
                                inv.set(i + 1, null, 0);
                                if (!successful) outputs.add(Keys.toStack(key1, count1));
                            }
                        }
                        if (!successful) outputs.add(Keys.toStack(key, count));
                        inv.set(i, null, 0);
                    }
                }
            }
        }
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        for (ItemStack stack : outputs) {
            var key = AEItemKey.of(stack);
            if (key != null) output(key, stack.getCount());
        }
        outputs.clear();
        if (successful) outputFluid(WaterPurificationPlantMachine.GradePurifiedWater8, inputCount * 9 / 10);
    }

    @Override
    long prepareRecipe(RecipeHandlerUnit unit) {
        eut = 0;
        successful = false;
        inputCount = Math.min(parallel(), unit.getFluidAmount(true, WaterPurificationPlantMachine.GradePurifiedWater7)[0]);
        if (inputCount > 0) {
            recipe = getRecipeBuilder().duration(WaterPurificationPlantMachine.DURATION).inputFluids(WaterPurificationPlantMachine.GradePurifiedWater7, inputCount).buildRawRecipe();
            if (matchRecipe(unit, recipe)) {
                int a = GTValues.RNG.nextInt(6);
                int b;
                do {
                    b = GTValues.RNG.nextInt(6);
                } while (b == a);
                catalyst1 = CATALYST.get(a);
                catalyst2 = CATALYST.get(b);
                calculateVoltage(inputCount);
            }
        }
        return eut;
    }
}
