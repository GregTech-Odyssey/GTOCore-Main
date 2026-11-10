package com.gtocore.gametest.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uiwidgets.machine.EnergyBufferView;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

@GameTestHolder("gtocore_energy")
@PrefixGameTestTemplate(false)
public final class EnergyBufferGameTests {

    @GameTest(template = "empty")
    public static void assemblyLineRetainsReserveAndShowsOneBar(GameTestHelper helper) throws ReflectiveOperationException {
        var hatchPos = new BlockPos(1, 2, 1);
        helper.setBlock(hatchPos, GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].get());
        var hatch = (EnergyHatchPartMachine) MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(hatchPos));
        var source = hatch.energyContainer;
        long initialEnergy = source.getEnergyCapacity();
        long rate = source.getInputVoltage() * source.getInputAmperage();
        source.setEnergyStored(initialEnergy);
        var original = assemblyLine(helper, new BlockPos(2, 2, 1));
        original.getMultiblockState().matchContext.getParts().add(hatch);
        original.onStructureFormed();
        helper.assertTrue(original.useEnergy(rate, true), "Assembly line sees hatch energy");
        helper.assertTrue(source.getEnergyStored() == initialEnergy && original.getEnergyBuffer() == 0, "Simulation must be read-only");
        helper.assertTrue(original.useEnergy(rate, false), "Assembly line consumes EU");
        helper.assertTrue(source.getEnergyStored() == initialEnergy - rate * 10 && original.getEnergyBuffer() == rate * 9, "GTM controller prefetched real EU");
        helper.assertTrue(countBars(original.createMainPage(null)) == 1, "Ordinary assembly line has exactly one internal energy bar");
        var saved = original.holder.saveWithoutMetadata();
        original.onStructureInvalid();
        helper.assertTrue(original.getEnergyBuffer() == rate * 9, "Invalidating the structure retains EU");
        helper.assertTrue(original.getEnergyBufferDisplayCapacity() >= rate * 9, "Invalid structure does not show a zero capacity with stored EU");
        var restored = assemblyLine(helper, new BlockPos(3, 2, 1));
        restored.holder.load(saved);
        helper.assertTrue(restored.getEnergyBuffer() == rate * 9, "Controller save restores inherited energyBuffer");
        restored.getMultiblockState().matchContext.getParts().add(hatch);
        restored.onStructureFormed();
        helper.assertTrue(restored.getEnergyBuffer() == rate * 9, "Reforming retains reserve");
        source.setEnergyStored(0);
        for (int i = 0; i < 9; i++) helper.assertTrue(restored.useEnergy(rate, false), "Reserve powers the assembly line after disconnection");
        helper.assertTrue(!restored.useEnergy(1, false), "No operation succeeds after both stores are empty");
        restored.onStructureInvalid();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void singleBlockUsesOnlyItsEnergyContainer(GameTestHelper helper) throws ReflectiveOperationException {
        var original = macerator(helper, new BlockPos(1, 2, 1));
        original.getEnergyContainer().setEnergyStored(1000);
        helper.assertTrue(original.useEnergy(32, true), "Single block sees its stored EU");
        helper.assertTrue(original.getEnergyContainer().getEnergyStored() == 1000, "Single-block simulation leaves storage unchanged");
        helper.assertTrue(original.useEnergy(32, false), "Single block consumes EU");
        helper.assertTrue(original.getEnergyContainer().getEnergyStored() == 968, "Single block draws only the requested EU without prefetch");
        helper.assertTrue(countBars(((com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine) original).createMainPage(null)) == 0, "Single block main page has no internal energy bar");
        var saved = original.holder.saveWithoutMetadata();
        var restored = macerator(helper, new BlockPos(2, 2, 1));
        restored.holder.load(saved);
        helper.assertTrue(restored.getEnergyContainer().getEnergyStored() == 968, "Single-block storage survives a real save and load");
        restored.getEnergyContainer().setEnergyStored(0);
        helper.assertTrue(!restored.useEnergy(1, true), "An empty single block has no hidden reserve in simulation");
        helper.assertTrue(!restored.useEnergy(1, false), "An empty single block stops immediately");
        helper.succeed();
    }

    private static WorkableElectricMultiblockMachine assemblyLine(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, GTMultiMachines.ASSEMBLY_LINE.get());
        return (WorkableElectricMultiblockMachine) MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(pos));
    }

    private static TieredEnergyMachine macerator(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, GTMachines.MACERATOR[GTValues.LV].get());
        return (TieredEnergyMachine) MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(pos));
    }

    private static int countBars(Widget widget) throws ReflectiveOperationException {
        int count = 0;
        if (widget instanceof ProgressBar) {
            var label = ProgressBar.class.getDeclaredField("label");
            label.setAccessible(true);
            if (EnergyBufferView.LABEL.equals(label.get(widget))) count++;
        }
        if (widget instanceof WidgetGroup group) {
            for (var child : group.widgets) count += countBars(child);
        }
        return count;
    }
}
