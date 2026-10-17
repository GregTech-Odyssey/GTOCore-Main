package com.gtocore.common.data;

import com.gtocore.common.block.BlockMap;
import com.gtocore.common.block.WirelessEnergyTransferUnitBlock;
import com.gtocore.common.block.WirelessEnergyUnitBlock;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.wireless.energy.map.GridMapUIFactory;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.wireless.energy.GridBody;
import com.gtolib.api.wireless.energy.GridDemo;
import com.gtolib.api.wireless.energy.GridView;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Arrays;

/**
 * 开发环境指令：为执行者所在队伍搭建或移除电网演示场景，并打开电网星图。
 */
@DataGeneratorScanned
public final class GridDemoCommand {

    @RegisterLanguage(cn = "电网演示「%s」已就绪：%s 个节点，%s 条线路，%s 个流量端点", en = "Grid demo \"%s\" is ready: %s nodes, %s lines, %s flow endpoints")
    public static final String READY = "gtocore.command.griddemo.ready";
    @RegisterLanguage(cn = "电网演示已移除，本队电网现有 %s 个节点，%s 条线路", en = "Grid demo removed. The team grid now has %s nodes and %s lines")
    public static final String REMOVED = "gtocore.command.griddemo.removed";
    @RegisterLanguage(cn = "未知场景：%s", en = "Unknown scenario: %s")
    public static final String UNKNOWN = "gtocore.command.griddemo.unknown";
    @RegisterLanguage(cn = "无线电网当前不可用", en = "The wireless grid is unavailable")
    public static final String UNAVAILABLE = "gtocore.command.griddemo.unavailable";

    private static final String[] SCENARIOS = Arrays.stream(GridDemo.Scenario.values()).map(GridDemo.Scenario::id).toArray(String[]::new);

    private GridDemoCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("griddemo").requires(source -> source.hasPermission(2))
                .then(Commands.literal("stop").executes(ctx -> run(ctx, GridDemo.Scenario.EMPTY)))
                .then(Commands.literal("open").executes(GridDemoCommand::open))
                .then(Commands.literal("tower")
                        .then(Commands.literal("legacy").then(Commands.argument("layers", IntegerArgumentType.integer(2, 30))
                                .executes(ctx -> legacyTower(ctx, IntegerArgumentType.getInteger(ctx, "layers")))))
                        .then(Commands.literal("box").then(Commands.argument("width", IntegerArgumentType.integer(3, 11))
                                .then(Commands.argument("height", IntegerArgumentType.integer(3, 20))
                                        .then(Commands.argument("depth", IntegerArgumentType.integer(3, 11))
                                                .then(Commands.argument("glass", IntegerArgumentType.integer(GTValues.MV, GTValues.MAX))
                                                        .executes(ctx -> boxTower(ctx, IntegerArgumentType.getInteger(ctx, "width"), IntegerArgumentType.getInteger(ctx, "height"),
                                                                IntegerArgumentType.getInteger(ctx, "depth"), IntegerArgumentType.getInteger(ctx, "glass")))))))))
                .then(Commands.argument("scenario", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SCENARIOS, builder))
                        .executes(ctx -> {
                            var id = StringArgumentType.getString(ctx, "scenario");
                            var scenario = GridDemo.Scenario.of(id);
                            if (scenario != null) return run(ctx, scenario);
                            ctx.getSource().sendFailure(Component.translatable(UNKNOWN, id));
                            return 0;
                        }));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, GridDemo.Scenario scenario) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var result = GridDemo.start(player, scenario);
        if (result == null) {
            ctx.getSource().sendFailure(Component.translatable(UNAVAILABLE));
            return 0;
        }
        var message = scenario == GridDemo.Scenario.EMPTY ? Component.translatable(REMOVED, result.nodes(), result.lines()) :
                Component.translatable(READY, scenario.id(), result.nodes(), result.lines(), result.ports());
        ctx.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static Block glass(int tier) {
        var supplier = BlockMap.GLASSMAP.get(tier);
        return supplier == null ? GTBlocks.CASING_TEMPERED_GLASS.get() : (Block) supplier.get();
    }

    private static Block unit(RandomSource random, int maxTier) {
        int tier = 1 + random.nextInt(Math.max(1, Math.min(maxTier, GTValues.MAX)));
        var unit = random.nextInt(3) == 0 ? WirelessEnergyTransferUnitBlock.get(tier) : WirelessEnergyUnitBlock.get(tier);
        return unit == null ? Blocks.AIR : unit.towerBlock();
    }

    private static void placeController(ServerPlayer player, BlockPos pos, Direction front) {
        player.serverLevel().setBlock(pos, MultiBlockG.WIRELESS_ENERGY_SUBSTATION.get().defaultBlockState(), 3);
        if (MetaMachine.getMachine(player.serverLevel(), pos) instanceof MetaMachine machine) {
            machine.setFrontFacing(front);
            machine.setOwnerUUID(player.getUUID());
        }
    }

    private static int legacyTower(CommandContext<CommandSourceStack> ctx, int layers) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var level = player.serverLevel();
        var back = player.getDirection();
        var front = back.getOpposite();
        var side = front.getClockWise();
        var origin = player.blockPosition().relative(back, 2);
        var steel = GTBlocks.CASING_STEEL_SOLID.get().defaultBlockState();
        var glass = glass(GTValues.IV).defaultBlockState();
        for (int h = 0; h <= layers + 1; h++) {
            for (int a = -3; a <= 3; a++) {
                for (int b = 0; b <= 6; b++) {
                    var pos = origin.relative(side, a).relative(back, b).above(h);
                    boolean edgeA = Math.abs(a) == 3, edgeB = b == 0 || b == 6;
                    BlockState state;
                    if (h == 0 || h == layers + 1 || edgeA && edgeB) state = steel;
                    else if (edgeA || edgeB) state = glass;
                    else state = unit(level.random, GTValues.IV).defaultBlockState();
                    if (h == 0 && a == 0 && b == 0) continue;
                    level.setBlock(pos, state, 3);
                }
            }
        }
        placeController(player, origin, front);
        ctx.getSource().sendSuccess(() -> Component.literal("legacy tower: " + layers + " layers at " + origin.toShortString()), false);
        return 1;
    }

    private static int boxTower(CommandContext<CommandSourceStack> ctx, int requestedWidth, int height, int depth, int glassTier) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var level = player.serverLevel();
        var back = player.getDirection();
        var front = back.getOpposite();
        var side = front.getClockWise();
        var origin = player.blockPosition().relative(back, 2);
        var frame = GTOBlocks.WIRELESS_ENERGY_TOWER_FRAME.get().defaultBlockState();
        var glass = glass(glassTier).defaultBlockState();
        int width = requestedWidth | 1;
        int left = (width - 1) / 2;
        for (int h = 0; h < height; h++) {
            for (int a = 0; a < width; a++) {
                for (int b = 0; b < depth; b++) {
                    int edges = (a == 0 || a == width - 1 ? 1 : 0) + (h == 0 || h == height - 1 ? 1 : 0) + (b == 0 || b == depth - 1 ? 1 : 0);
                    var pos = origin.relative(side, a - left).relative(back, b).above(h);
                    if (a == left && h == 0 && b == 0) continue;
                    var state = edges >= 2 ? frame : edges == 1 ? glass : unit(level.random, glassTier).defaultBlockState();
                    level.setBlock(pos, state, 3);
                }
            }
        }
        placeController(player, origin, front);
        ctx.getSource().sendSuccess(() -> Component.literal("tower " + width + "x" + height + "x" + depth + " at " + origin.toShortString()), false);
        return 1;
    }

    private static int open(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        return GridMapUIFactory.open(player, GridView.dimRef(GridBody.of(player.level().dimension()))) ? 1 : 0;
    }
}
