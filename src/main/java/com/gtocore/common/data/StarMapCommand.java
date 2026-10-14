package com.gtocore.common.data;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import earth.terrarium.adastra.common.menus.base.PlanetsMenuProvider;
import earth.terrarium.botarium.common.menu.MenuHooks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.UUID;

/**
 * 调试指令：召唤指定等级的火箭让玩家乘坐，等客户端收到乘坐状态后打开选星界面。
 */
@Mod.EventBusSubscriber
public final class StarMapCommand {

    private static final ResourceLocation[] ROCKETS = {
            new ResourceLocation("ad_astra", "tier_1_rocket"),
            new ResourceLocation("ad_astra", "tier_2_rocket"),
            new ResourceLocation("ad_astra", "tier_3_rocket"),
            new ResourceLocation("ad_astra", "tier_4_rocket"),
            new ResourceLocation("ad_astra", "tier_5_rocket"),
            new ResourceLocation("ad_astra", "tier_6_rocket"),
            new ResourceLocation("ad_astra", "tier_7_rocket"),
    };
    private static final int OPEN_DELAY = 10;
    private static final Object2IntOpenHashMap<UUID> PENDING = new Object2IntOpenHashMap<>();

    private StarMapCommand() {}

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("starmap").requires(source -> source.hasPermission(2))
                .then(Commands.argument("tier", IntegerArgumentType.integer(1, ROCKETS.length)).executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    int tier = IntegerArgumentType.getInteger(ctx, "tier");
                    var type = BuiltInRegistries.ENTITY_TYPE.getOptional(ROCKETS[tier - 1]).orElse(null);
                    if (type == null) {
                        ctx.getSource().sendFailure(Component.literal("Rocket entity not found: " + ROCKETS[tier - 1]));
                        return 0;
                    }
                    player.stopRiding();
                    var rocket = type.spawn(player.serverLevel(), player.blockPosition(), MobSpawnType.COMMAND);
                    if (!(rocket instanceof Rocket)) {
                        if (rocket != null) rocket.discard();
                        ctx.getSource().sendFailure(Component.literal("Not a rocket: " + ROCKETS[tier - 1]));
                        return 0;
                    }
                    player.startRiding(rocket, true);
                    PENDING.put(player.getUUID(), OPEN_DELAY);
                    return 1;
                }));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;
        MinecraftServer server = event.getServer();
        var iterator = PENDING.object2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Object2IntMap.Entry<UUID> entry = iterator.next();
            int left = entry.getIntValue() - 1;
            if (left > 0) {
                entry.setValue(left);
                continue;
            }
            var player = server.getPlayerList().getPlayer(entry.getKey());
            iterator.remove();
            if (player != null && player.getVehicle() instanceof Rocket) MenuHooks.openMenu(player, new PlanetsMenuProvider());
        }
    }
}
