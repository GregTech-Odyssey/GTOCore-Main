package com.gtocore.common.data;

import com.gtocore.api.wireless.energy.GridBody;
import com.gtocore.api.wireless.energy.GridDemo;
import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.common.wireless.energy.map.GridMapUIFactory;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

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

    private static int open(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        return GridMapUIFactory.open(player, GridView.dimRef(GridBody.of(player.level().dimension()))) ? 1 : 0;
    }
}
