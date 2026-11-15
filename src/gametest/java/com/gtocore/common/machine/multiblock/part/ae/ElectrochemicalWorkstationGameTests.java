package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.machine.multiblock.electric.ElectrochemicalMachine;
import com.gtocore.common.machine.multiblock.electric.ElectrochemicalWorkstation;
import com.gtocore.common.machine.multiblock.part.SemipermeableMembraneHatch;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.machine.trait.MultiblockTrait;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

import java.util.Collections;

/** Real recipe lifecycle with fixed power and output space; inputs use same-colored pattern and ME fluid parts. */
@GameTestHolder("gtocore")
@PrefixGameTestTemplate(false)
public final class ElectrochemicalWorkstationGameTests {

    private static final int CYCLES = 20;

    @GameTest(template = "empty", batch = "electrochemical_pattern_mode", timeoutTicks = 100)
    public static void platingPatternOverridesElectrolyzerInCombinedMode(GameTestHelper helper) throws ReflectiveOperationException {
        repeatPlating(helper, GTORecipeTypes.ELECTROLYZER_RECIPES, null);
    }

    @GameTest(template = "empty", batch = "electrochemical_buffer_mode", timeoutTicks = 100)
    public static void explicitPlatingBufferOverridesElectrolyzer(GameTestHelper helper) throws ReflectiveOperationException {
        repeatPlating(helper, GTORecipeTypes.ELECTROLYZER_RECIPES, GTORecipeTypes.ELECTROPLATING_RECIPES);
    }

    @GameTest(template = "empty", batch = "electrochemical_machine_mode", timeoutTicks = 100)
    public static void platingMachineWorksWithCombinedBuffer(GameTestHelper helper) throws ReflectiveOperationException {
        repeatPlating(helper, GTORecipeTypes.ELECTROPLATING_RECIPES, null);
    }

    private static void repeatPlating(GameTestHelper helper, GTRecipeType machineType, GTRecipeType bufferType) throws ReflectiveOperationException {
        var controller = place(helper, new BlockPos(0, 2, 0), MultiBlockH.GIANT_ELECTROCHEMICAL_WORKSTATION);
        var machine = (ElectrochemicalWorkstation) controller.getMetaMachine();
        var buffer = (MEPatternBufferPartMachine) place(helper, new BlockPos(2, 2, 0), GTAEMachines.ME_PATTERN_BUFFER).getMetaMachine();
        var fluid = (MEInputHatchPartMachine) place(helper, new BlockPos(4, 2, 0), GTAEMachines.FLUID_IMPORT_HATCH_ME).getMetaMachine();
        var membrane = (SemipermeableMembraneHatch) place(helper, new BlockPos(6, 2, 0), GTOMachines.SEMIPERMEABLE_MEMBRANE_HATCH).getMetaMachine();

        // Isolate mode selection from structure formation, power delivery and output capacity.
        setField(WorkableElectricMultiblockMachine.class, machine, "tier", 12);
        setField(WorkableElectricMultiblockMachine.class, machine, "energyContainer", new EnergyContainerList() {

            @Override
            public long getOverclockVoltage() {
                return 32768;
            }

            @Override
            public long getEnergyStored() {
                return Long.MAX_VALUE / 4;
            }
        });
        setField(ElectricMultiblockMachine.class, machine, "mtraits", new MultiblockTrait[0]);
        setField(ElectrochemicalMachine.class, machine, "membraneHatch", membrane);
        machine.setVoidingMode(IVoidable.VoidingMode.VOID_BOTH);
        machine.setRecipeType(machineType);
        buffer.setRecipeType(bufferType);
        membrane.getInventory().storage.setStackInSlot(0, ChemicalHelper.get(GTOTagPrefix.ION_MEMBRANE, GTMaterials.Polytetrafluoroethylene));
        buffer.setPaintingColor(0xFF0000);
        fluid.setPaintingColor(0xFF0000);

        var tanks = fluid.aeFluidHandler.getInventory();
        var water = new GenericStack(AEFluidKey.of(GTMaterials.Water.getFluid()), 100000);
        var acid = new GenericStack(AEFluidKey.of(GTOMaterials.TrisHydrochlorideSolution.getFluid()), 100000);
        tanks[0].setConfig(water);
        tanks[0].setStock(water);
        tanks[1].setConfig(acid);
        tanks[1].setStock(acid);
        for (var unit : buffer.getRecipeHandlers()) machine.addHandlerList(unit);
        machine.addHandlerList(RecipeHandlerUnit.of(IO.IN, fluid, Collections.singletonList(fluid.aeFluidHandler)));
        machine.arrangeHandlerList();

        var rhodium = AEItemKey.of(ChemicalHelper.getItem(TagPrefix.plate, GTMaterials.Rhodium));
        var palladium = AEItemKey.of(ChemicalHelper.getItem(TagPrefix.plate, GTMaterials.Palladium));
        var ammonium = AEItemKey.of(ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.AmmoniumChloride));
        var output = AEItemKey.of(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium));
        var definition = RecipeBuilder.get(RecipeBuilder.getTypeID(GTCEu.id("rhodium_plated_palladium_ingot"), GTORecipeTypes.ELECTROPLATING_RECIPES));
        helper.assertTrue(definition != null, "Plating half reaction must be registered");
        var pattern = PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] { new GenericStack(rhodium, 2), new GenericStack(palladium, 6), new GenericStack(ammonium, 2) },
                new GenericStack[] { new GenericStack(output, 8) });
        pattern.getOrCreateTag().putString("recipe", definition.id.toString());
        buffer.getPatternInventory().setStackInSlot(0, pattern);
        buffer.onPatternChange(0);
        var slot = buffer.getInternalInventory()[0];
        helper.assertTrue(slot.recipe == definition, "Pattern must load its plating recipe binding");

        for (int cycle = 0; cycle < CYCLES; cycle++) {
            int number = cycle;
            helper.runAtTickTime(5 + cycle * 2, () -> {
                slot.itemInventory.insert(rhodium, 2, false);
                slot.itemInventory.insert(palladium, 6, false);
                slot.itemInventory.insert(ammonium, 2, false);
                helper.assertTrue(machine.getRecipeLogic().findAndHandleRecipe(), "Plating cycle " + number + " must start");
                helper.assertTrue(slot.itemInventory.isEmpty(), "Plating cycle " + number + " must consume both half reactions' inputs");
                helper.assertTrue(membrane.membrane().getDamageValue() == number + 1, "Membrane wear must advance on every cycle");
                machine.getRecipeLogic().onRecipeFinish();
                if (number == CYCLES - 1) helper.succeed();
            });
        }
    }

    private static MetaMachineBlockEntity place(GameTestHelper helper, BlockPos pos, MachineDefinition definition) {
        helper.setBlock(pos, definition.get().defaultBlockState());
        return (MetaMachineBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static void setField(Class<?> owner, Object target, String name, Object value) throws ReflectiveOperationException {
        var field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
