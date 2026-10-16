package com.gtocore.common.weather;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public final class WeatherCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("weather").requires(source -> source.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (var entry : WeatherSystem.profile(context.getSource().getLevel().dimension())
                                    .entries()) {
                                builder.suggest(entry.weather().id());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> set(context.getSource(),
                                WeatherTypes.REGISTRY.get(StringArgumentType.getString(context, "type")), -1))
                        .then(Commands.argument("duration", TimeArgument.time(1))
                                .executes(context -> set(context.getSource(),
                                        WeatherTypes.REGISTRY.get(StringArgumentType.getString(context, "type")),
                                        IntegerArgumentType.getInteger(context, "duration")))));
    }

    public static int setVanilla(CommandSourceStack source, int ticks, boolean rain, boolean thunder) {
        return set(source, WeatherSystem.profile(source.getLevel().dimension()).vanillaWeather(rain, thunder), ticks) ==
                0 ? 0 : ticks;
    }

    private static int set(CommandSourceStack source, WeatherType weather, int ticks) {
        var profile = WeatherSystem.profile(source.getLevel().dimension());
        var entry = weather == null ? null : profile.find(weather);
        if (entry == null) {
            source.sendFailure(Component.translatable("gtocore.weather.unsupported"));
            return 0;
        }

        if (ticks < 0) {
            ticks = entry.duration() - entry.variation() + source.getLevel().random.nextInt(entry.variation() * 2 + 1);
        }

        WeatherSystem.get(source.getServer()).change(source.getLevel(), weather, ticks);
        var effective = WeatherSystem.current(source.getLevel());
        source.sendSuccess(
                () -> effective == weather ? Component.translatable("gtocore.weather.changed", weather.displayName()) :
                        Component.translatable("gtocore.weather.stellar_override", weather.displayName(),
                                effective.displayName()),
                true);
        return 1;
    }

    private WeatherCommands() {}
}
