package com.gtocore.gametest.timer;

import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.machine.noenergy.RedstoneTimerMachine;

import com.gtolib.redstone.TimerBlockStateLookup;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

@GameTestHolder("gtocore_timer")
@PrefixGameTestTemplate(false)
public final class RedstoneTimerGameTests {

    @GameTest(template = "empty")
    public static void loadedStateBypassesLoadingGetter(GameTestHelper helper) {
        var pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, Blocks.STONE);
        var state = TimerBlockStateLookup.getBlockState(helper.getLevel(), helper.absolutePos(pos), GTOMachines.REDSTONE_TIMER.get(), args -> {
            throw new AssertionError("A loaded timer target should bypass the loading getter");
        });
        helper.assertTrue(state.is(Blocks.STONE), "The fast path reads the current target state");
        helper.setBlock(pos, Blocks.DIRT);
        state = TimerBlockStateLookup.getBlockState(helper.getLevel(), helper.absolutePos(pos), GTOMachines.REDSTONE_TIMER.get(), args -> {
            throw new AssertionError("A loaded timer target should bypass the loading getter");
        });
        helper.assertTrue(state.is(Blocks.DIRT), "The fast path does not cache stale block states");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unavailableChunkKeepsOriginalLookup(GameTestHelper helper) {
        var level = helper.getLevel();
        var target = new BlockPos(1_000_000, 80, 1_000_000);
        helper.assertTrue(level.getChunkSource().getChunkNow(target.getX() >> 4, target.getZ() >> 4) == null, "Target starts unavailable");
        var result = TimerBlockStateLookup.getBlockState(level, target, GTOMachines.REDSTONE_TIMER.get(), args -> {
            helper.assertTrue(args[0] == level && args[1] == target, "Fallback retains the original arguments");
            return Blocks.DIAMOND_BLOCK.defaultBlockState();
        });
        helper.assertTrue(result.is(Blocks.DIAMOND_BLOCK), "Unavailable chunks delegate instead of swallowing the final falling edge");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void otherSourcesKeepOriginalLookup(GameTestHelper helper) {
        var target = helper.absolutePos(new BlockPos(1, 2, 1));
        var result = TimerBlockStateLookup.getBlockState(helper.getLevel(), target, Blocks.STONE, args -> Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.assertTrue(result.is(Blocks.DIAMOND_BLOCK), "Non-timer notifications retain the original getter");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void loadedWireReceivesBothEdges(GameTestHelper helper) throws ReflectiveOperationException {
        var source = new BlockPos(2, 2, 2);
        var wire = source.east();
        var timer = timer(helper, source);
        helper.setBlock(wire.below(), Blocks.STONE);
        helper.setBlock(wire, Blocks.REDSTONE_WIRE);
        output(timer, true);
        helper.assertTrue(helper.getBlockState(wire).getValue(RedStoneWireBlock.POWER) == 15, "Rising edge powers the wire");
        output(timer, false);
        helper.assertTrue(helper.getBlockState(wire).getValue(RedStoneWireBlock.POWER) == 0, "Falling edge clears the wire");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void onlyTorchFaceEmitsSignal(GameTestHelper helper) throws ReflectiveOperationException {
        var timer = timer(helper, new BlockPos(2, 2, 2));
        output(timer, true);
        for (var direction : Direction.values()) {
            helper.assertTrue(timer.getOutputSignal(direction) == (direction == Direction.WEST ? 15 : 0), "Only the torch face emits a weak signal");
            helper.assertTrue(timer.getOutputDirectSignal(direction) == 0, "The timer does not strongly power solid conductors");
        }
        output(timer, false);
        for (var direction : Direction.values()) {
            helper.assertTrue(timer.getOutputSignal(direction) == 0, "Disabling the output clears every direction");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void solidConductorDoesNotSpreadSignal(GameTestHelper helper) throws ReflectiveOperationException {
        var source = new BlockPos(2, 2, 2);
        var front = source.east();
        var wire = front.south();
        var timer = timer(helper, source);
        helper.setBlock(front, Blocks.STONE);
        helper.setBlock(wire.below(), Blocks.STONE);
        helper.setBlock(wire, Blocks.REDSTONE_WIRE);
        var eventPos = helper.absolutePos(front);
        var notifications = new int[1];
        Consumer<BlockEvent.NeighborNotifyEvent> count = event -> {
            if (event.getLevel() == helper.getLevel() && event.getPos().equals(eventPos)) notifications[0]++;
        };
        MinecraftForge.EVENT_BUS.addListener(count);
        try {
            output(timer, true);
            helper.assertTrue(helper.getBlockState(wire).getValue(RedStoneWireBlock.POWER) == 0, "A solid block does not relay the timer output to its neighbors");
            output(timer, false);
            helper.assertTrue(notifications[0] == 0, "Output edges do not fan out notifications around the output block");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(count);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void strengthChangesOnlyTorchFace(GameTestHelper helper) throws ReflectiveOperationException {
        var source = new BlockPos(2, 2, 2);
        var timer = timer(helper, source);
        for (var direction : Direction.Plane.HORIZONTAL) {
            var wire = source.relative(direction);
            helper.setBlock(wire.below(), Blocks.STONE);
            helper.setBlock(wire, Blocks.REDSTONE_WIRE);
        }
        output(timer, true);
        var method = RedstoneTimerMachine.class.getDeclaredMethod("setStrength", int.class);
        method.setAccessible(true);
        method.invoke(timer, 7);
        for (var direction : Direction.Plane.HORIZONTAL) {
            var wire = source.relative(direction);
            helper.assertTrue(helper.getBlockState(wire).getValue(RedStoneWireBlock.POWER) == (direction == Direction.EAST ? 7 : 0), "Strength changes reach only the torch face");
        }
        output(timer, false);
        helper.assertTrue(helper.getBlockState(source.east()).getValue(RedStoneWireBlock.POWER) == 0, "The adjusted output still clears on the falling edge");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rotationClearsOldOutput(GameTestHelper helper) throws ReflectiveOperationException {
        var source = new BlockPos(2, 2, 2);
        var oldWire = source.east();
        var newWire = source.south();
        var timer = timer(helper, source);
        helper.setBlock(oldWire.below(), Blocks.STONE);
        helper.setBlock(newWire.below(), Blocks.STONE);
        helper.setBlock(oldWire, Blocks.REDSTONE_WIRE);
        helper.setBlock(newWire, Blocks.REDSTONE_WIRE);
        set(timer, "initialized", true);
        set(timer, "enabled", true);
        set(timer, "running", true);
        set(timer, "cycleStart", timer.getOffsetTimer() - 20);
        output(timer, true);
        helper.assertTrue(helper.getBlockState(oldWire).getValue(RedStoneWireBlock.POWER) == 15, "Old output starts powered");
        timer.setFrontFacing(Direction.NORTH);
        helper.assertTrue(helper.getBlockState(oldWire).getValue(RedStoneWireBlock.POWER) == 0, "Rotation clears the previous output");
        helper.assertTrue(helper.getBlockState(newWire).getValue(RedStoneWireBlock.POWER) == 15, "Rotation notifies the new output");
        timer.setWorkingEnabled(false);
        helper.assertTrue(helper.getBlockState(newWire).getValue(RedStoneWireBlock.POWER) == 0, "Disabling retains the final falling edge");
        helper.succeed();
    }

    private static RedstoneTimerMachine timer(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, GTOMachines.REDSTONE_TIMER.get());
        var timer = (RedstoneTimerMachine) MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(pos));
        helper.assertTrue(timer != null, "Timer exists");
        timer.setWorkingEnabled(false);
        timer.setFrontFacing(Direction.WEST);
        return timer;
    }

    private static void output(RedstoneTimerMachine timer, boolean on) throws ReflectiveOperationException {
        var method = RedstoneTimerMachine.class.getDeclaredMethod("setOutput", boolean.class);
        method.setAccessible(true);
        method.invoke(timer, on);
    }

    private static void set(RedstoneTimerMachine timer, String name, Object value) throws ReflectiveOperationException {
        var field = RedstoneTimerMachine.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(timer, value);
    }
}
