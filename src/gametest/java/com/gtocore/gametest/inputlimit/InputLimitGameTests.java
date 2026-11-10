package com.gtocore.gametest.inputlimit;

import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IInputLimitableMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.datasynclib.GTDataFixer;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyTypes;

import com.gto.datasynclib.blockentity.IDropSave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GameTestHolder("gtocore_inputlimit")
@PrefixGameTestTemplate(false)
public final class InputLimitGameTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputLimitGameTests.class);
    private static final BlockPos MACHINE_POS = new BlockPos(1, 2, 1);

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void newlyPlacedMixedInputMachinesLimitBothInputs(GameTestHelper helper) {
        int checked = 0;
        for (var definition : GTRegistries.MACHINES) {
            if (!mixedInputs(definition)) continue;
            helper.setBlock(MACHINE_POS, definition.get());
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(MACHINE_POS));
            ((IRecipeLogicMachine) machine).setWorkingEnabled(false);
            assertLimited(helper, machine, definition);
            checked++;
        }
        helper.assertTrue(checked > 0, "Mixed input machines were checked");
        LOGGER.info("Verified default anti-clog on {} registered mixed input machines", checked);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void legacyPartialLimitsBecomeConsistentWithoutLosingItems(GameTestHelper helper) {
        for (var definition : GTRegistries.MACHINES) {
            if (!mixedInputs(definition)) continue;
            // Constructors retain the old item=false/fluid=true defaults so omitted disabled data stays valid.
            var original = holder(helper, definition);
            var items = original.getMetaMachine().getItemHandlerCap(null, false);
            helper.assertTrue(items.insert(AEItemKey.of(Items.COAL), 128, false) == 128,
                    definition.getId() + " can reproduce a legacy save with duplicate stacks");
            var saved = original.saveWithoutMetadata();
            var restored = holder(helper, definition);
            restored.load(saved);
            restored.clearRemoved();
            var machine = restored.getMetaMachine();
            var restoredItems = machine.getItemHandlerCap(null, false);
            helper.assertTrue(restoredItems.amountAt(0) == 64 && restoredItems.amountAt(1) == 64,
                    definition.getId() + " retains both existing stacks");
            helper.assertTrue(restoredItems.insert(AEItemKey.of(Items.COAL), 1, true) == 0,
                    definition.getId() + " blocks further duplicate input after loading a legacy save");
            assertConsistent(helper, machine, true, definition);
            var coal = AEItemKey.of(Items.COAL);
            var storage = restoredItems.unrestricted();
            helper.assertTrue(storage.count(coal) == 128,
                    definition.getId() + " counts all existing duplicate stacks");
            helper.assertTrue(storage.extract(coal, 128, true) == 128,
                    definition.getId() + " can simulate extracting all existing duplicates");
            helper.assertTrue(storage.extract(1, coal, 64, false) == 64,
                    definition.getId() + " can empty the last duplicate slot");
            helper.assertTrue(restoredItems.insert(coal, 1, true) == 0,
                    definition.getId() + " still rejects duplicates while the first slot remains full");
            helper.assertTrue(storage.extract(coal, 64, false) == 64 && storage.count(coal) == 0,
                    definition.getId() + " can extract the remaining stack by key");
            restored.setRemoved();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void emptyDisabledLegacySavesStayDisabled(GameTestHelper helper) {
        for (var definition : GTRegistries.MACHINES) {
            if (!mixedInputs(definition)) continue;
            var original = holder(helper, definition);
            ((IInputLimitableMachine) original.getMetaMachine()).setInputLimit(false);
            var saved = original.saveWithoutMetadata();
            var restored = holder(helper, definition);
            restored.load(saved);
            restored.clearRemoved();
            var machine = restored.getMetaMachine();
            assertConsistent(helper, machine, false, definition);
            helper.assertTrue(machine.getItemHandlerCap(null, false).insert(AEItemKey.of(Items.COAL), 128, true) == 128,
                    definition.getId() + " retains the disabled item limit in an empty legacy save");
            restored.setRemoved();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void enabledLimitsSurviveSaveAndLoad(GameTestHelper helper) {
        for (var definition : GTRegistries.MACHINES) {
            if (!mixedInputs(definition)) continue;
            var original = holder(helper, definition);
            original.clearRemoved();
            var saved = original.saveWithoutMetadata();
            original.setRemoved();
            var restored = holder(helper, definition);
            restored.load(saved);
            restored.clearRemoved();
            assertLimited(helper, restored.getMetaMachine(), definition);
            restored.setRemoved();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void togglingControlsItemsAndFluidsTogether(GameTestHelper helper) {
        for (var definition : GTRegistries.MACHINES) {
            if (!mixedInputs(definition)) continue;
            var original = holder(helper, definition);
            original.clearRemoved();
            var machine = original.getMetaMachine();
            var limit = (IInputLimitableMachine) machine;
            limit.setInputLimit(false);
            assertConsistent(helper, machine, false, definition);
            helper.assertTrue(machine.getItemHandlerCap(null, false).insert(AEItemKey.of(Items.COAL), 128, true) == 128,
                    definition.getId() + " accepts duplicate items when disabled");
            var fluids = machine.getFluidHandlerCap(null, false);
            long capacity = fluids.slotLimit(0);
            helper.assertTrue(fluids.insert(AEFluidKey.of(Fluids.WATER), capacity * 2, true) == capacity * 2,
                    definition.getId() + " accepts duplicate fluids when disabled");
            limit.setInputLimit(true);
            assertLimited(helper, machine, definition);
            original.setRemoved();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void legacyMachineItemsKeepEnabledAndDisabledLimitsOnPlacement(GameTestHelper helper) {
        for (var definition : GTRegistries.MACHINES) {
            if (!mixedInputs(definition)) continue;
            var legacy = holder(helper, definition);
            if (!(legacy.getMetaMachine() instanceof WorkableTieredMachine)) continue;
            for (boolean enabled : new boolean[] { true, false }) {
                if (!enabled) ((IInputLimitableMachine) legacy.getMetaMachine()).setInputLimit(false);
                var stack = definition.asStack();
                IDropSave.saveToItem(legacy.getMetaMachine(), stack.getOrCreateTag(), false, GTDataFixer.VERSION);
                helper.setBlock(MACHINE_POS, definition.get());
                var pos = helper.absolutePos(MACHINE_POS);
                var state = helper.getLevel().getBlockState(pos);
                definition.get().setPlacedBy(helper.getLevel(), pos, state, null, stack);
                var machine = MetaMachine.getMachine(helper.getLevel(), pos);
                assertConsistent(helper, machine, enabled, definition);
                helper.assertTrue(machine.getItemHandlerCap(null, false).insert(AEItemKey.of(Items.COAL), 128, true) == (enabled ? 64 : 128),
                        definition.getId() + " applies the saved limit after item placement");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void otherSingleblockMachinesKeepTheirDefaultLimits(GameTestHelper helper) {
        int checked = 0;
        for (var definition : GTRegistries.MACHINES) {
            if (definition instanceof MultiblockMachineDefinition || mixedInputs(definition)) continue;
            var original = holder(helper, definition);
            var machine = original.getMetaMachine();
            if (!(machine instanceof WorkableTieredMachine || machine instanceof SimpleNoEnergyMachine)) continue;
            boolean enabled = ((IInputLimitableMachine) machine).isInputLimit();
            original.clearRemoved();
            assertConsistent(helper, machine, enabled, definition);
            original.setRemoved();
            checked++;
        }
        helper.assertTrue(checked > 0, "Other singleblock defaults were checked");
        LOGGER.info("Verified unchanged anti-clog defaults on {} other singleblock machines", checked);
        helper.succeed();
    }

    private static void assertLimited(GameTestHelper helper, MetaMachine machine, MachineDefinition definition) {
        assertConsistent(helper, machine, true, definition);
        var items = machine.getItemHandlerCap(null, false);
        var coal = AEItemKey.of(Items.COAL);
        helper.assertTrue(items.insert(coal, 128, false) == 64,
                definition.getId() + " accepts only one stack per item when enabled");
        helper.assertTrue(items.insert(1, coal, 1, true) == 0,
                definition.getId() + " rejects duplicate items through slot insertion");
        helper.assertTrue(items.insert(AEItemKey.of(Items.DIRT), 64, true) == 64,
                definition.getId() + " still accepts other items");
        var fluids = machine.getFluidHandlerCap(null, false);
        long capacity = fluids.slotLimit(0);
        helper.assertTrue(fluids.insert(AEFluidKey.of(Fluids.WATER), capacity * 2, false) == capacity,
                definition.getId() + " accepts only one tank per fluid when enabled");
        helper.assertTrue(fluids.insert(AEFluidKey.of(Fluids.LAVA), capacity, true) == capacity,
                definition.getId() + " still accepts other fluids");
    }

    private static void assertConsistent(GameTestHelper helper, MetaMachine machine, boolean enabled, MachineDefinition definition) {
        helper.assertTrue(((IInputLimitableMachine) machine).isInputLimit() == enabled,
                definition.getId() + " reports the expected limit");
        for (var trait : machine.getTraits(NotifiableInventory.class)) {
            if (trait.getHandlerIO() != IO.IN || trait.storage.size() <= 1 || trait.storage.keyType() != AEKeyTypes.ITEMS && trait.storage.keyType() != AEKeyTypes.FLUIDS) continue;
            helper.assertTrue(trait.storage.isUniqueKeys() == enabled,
                    definition.getId() + " keeps all multi-slot input limits consistent");
        }
    }

    private static boolean mixedInputs(MachineDefinition definition) {
        if (definition instanceof MultiblockMachineDefinition) return false;
        var types = definition.getRecipeTypes();
        if (types == null) return false;
        for (var type : types) {
            if (type.getMaxInputs(ItemRecipeInfo.INSTANCE) > 1 && type.getMaxInputs(FluidRecipeInfo.INSTANCE) > 1) return true;
        }
        return false;
    }

    private static MetaMachineBlockEntity holder(GameTestHelper helper, MachineDefinition definition) {
        var holder = (MetaMachineBlockEntity) definition.getBlockEntityType().create(helper.absolutePos(MACHINE_POS), definition.defaultBlockState());
        holder.setLevel(helper.getLevel());
        return holder;
    }
}
