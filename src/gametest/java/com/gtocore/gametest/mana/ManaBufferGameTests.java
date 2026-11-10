package com.gtocore.gametest.mana;

import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.ManaMultiBlock;
import com.gtocore.common.machine.mana.part.ManaHatchPartMachine;

import com.gtolib.api.machine.mana.trait.ManaTrait;
import com.gtolib.api.misc.BufferedManaContainerList;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("gtocore_mana")
@PrefixGameTestTemplate(false)
public final class ManaBufferGameTests {

    @GameTest(template = "empty")
    public static void controllerSaveRetainsWithdrawnMana(GameTestHelper helper) {
        var hatchPos = new BlockPos(1, 2, 1);
        helper.setBlock(hatchPos, ManaMachine.MANA_INPUT_HATCH[GTValues.LV].get());
        var hatch = (ManaHatchPartMachine) MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(hatchPos));
        var source = hatch.getManaContainer();
        source.setCurrentMana(source.getMaxMana());
        long rate = source.getMaxIORate();

        var original = controller(helper, new BlockPos(2, 2, 1));
        var originalTrait = original.getTraits(ManaTrait.class)[0];
        var cached = new BufferedManaContainerList(originalTrait.getManaBuffer(), source);
        helper.assertTrue(cached.removeMana(rate, 1, false) == rate, "The machine consumes one tick of mana");
        helper.assertTrue(cached.getBufferedMana() == rate * 9, "Prefetch leaves nine ticks in the controller");
        var saved = original.holder.saveWithoutMetadata();

        var restored = controller(helper, new BlockPos(3, 2, 1));
        restored.holder.load(saved);
        var restoredTrait = restored.getTraits(ManaTrait.class)[0];
        var reloaded = new BufferedManaContainerList(restoredTrait.getManaBuffer(), source);
        helper.assertTrue(reloaded.getBufferedMana() == rate * 9, "The real controller save restores the nested reserve");
        helper.assertTrue(reloaded.getCurrentMana() == source.getMaxMana() - rate, "Hatch and controller mana remain conserved");
        restoredTrait.onStructureInvalid();
        reloaded = new BufferedManaContainerList(restoredTrait.getManaBuffer(), source);
        helper.assertTrue(reloaded.getBufferedMana() == rate * 9, "Structure invalidation retains withdrawn mana");
        source.setCurrentMana(0);
        helper.assertTrue(reloaded.removeManaUnrestricted(rate * 10, false) == rate * 9, "The restored reserve can be consumed exactly once");
        helper.assertTrue(reloaded.removeMana(rate, 1, false) == 0, "An empty hatch and empty reserve stop consumption");
        helper.runAfterDelay(2, () -> {
            var controllerChunk = helper.getLevel().getChunkAt(original.getPos());
            controllerChunk.setUnsaved(false);
            helper.assertTrue(cached.removeMana(rate, 1, false) == rate, "A cached tick succeeds without hatch access");
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(controllerChunk.isUnsaved(), "Cache consumption marks the controller chunk for saving");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty")
    public static void distributorSaveRetainsWithdrawnMana(GameTestHelper helper) {
        var hatchPos = new BlockPos(1, 2, 1);
        helper.setBlock(hatchPos, ManaMachine.MANA_INPUT_HATCH[GTValues.LV].get());
        var hatch = (ManaHatchPartMachine) MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(hatchPos));
        var source = hatch.getManaContainer();
        source.setCurrentMana(source.getMaxMana());
        helper.setBlock(new BlockPos(2, 2, 1), ManaMultiBlock.BASE_MANA_DISTRIBUTOR.get());
        var original = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 1)));
        var cached = new BufferedManaContainerList(original.getTraits(ManaTrait.class)[0].getManaBuffer(), source);
        helper.assertTrue(cached.removeMana(1, 20, false) == 1, "Distributor supplies the requested mana");
        long reserve = cached.getBufferedMana();
        helper.assertTrue(reserve > 0, "Small withdrawals leave a distributor reserve");
        var saved = original.holder.saveWithoutMetadata();
        helper.setBlock(new BlockPos(3, 2, 1), ManaMultiBlock.BASE_MANA_DISTRIBUTOR.get());
        var restored = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 1)));
        restored.holder.load(saved);
        var reloaded = new BufferedManaContainerList(restored.getTraits(ManaTrait.class)[0].getManaBuffer(), source);
        helper.assertTrue(reloaded.getBufferedMana() == reserve, "Distributor save restores all withdrawn mana");
        helper.assertTrue(reloaded.getCurrentMana() == source.getMaxMana() - 1, "Distributor reload conserves total mana");
        source.setCurrentMana(0);
        helper.assertTrue(reloaded.removeManaUnrestricted(Long.MAX_VALUE, false) == reserve, "Restored distributor reserve can be consumed once");
        helper.assertTrue(reloaded.removeMana(1, 20, false) == 0, "Empty distributor and source stop transfer");
        helper.succeed();
    }

    private static MetaMachine controller(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, ManaMultiBlock.MANA_INFUSER.get());
        return MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(pos));
    }
}
