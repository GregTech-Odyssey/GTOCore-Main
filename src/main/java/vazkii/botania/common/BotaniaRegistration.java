package vazkii.botania.common;

import com.gtolib.api.registries.GTORegistration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;

import com.gto.registrate.util.entry.RegistryEntry;

import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.worldgen.MythicBotanyWorldgen;

/**
 * 植物魔法复刻内容的注册器。
 *
 * <p>
 * 各模组内容使用原始命名空间和注册名称，共享植物魔法复刻创造标签；
 * 方块与物品只保留基础行为，外观模型引用原植物魔法的模型资源。
 * </p>
 */
public final class BotaniaRegistration {

    public static final GTORegistration BOTANIA = GTORegistration.BOTANIA;

    public static final RegistryEntry<CreativeModeTab> TAB = BOTANIA
            .defaultCreativeTab("gto_botania", builder -> builder
                    .title(Component.translatable("itemGroup.botania.gto"))
                    .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("botania", "mana_pool"))))
                    .build())
            .register();

    static {
        var tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation("botania", "gto_botania"));
        GTORegistration.MYTHICBOTANY.defaultCreativeTab(tab);
        GTORegistration.EXTRABOTANY.defaultCreativeTab(tab);
    }

    private BotaniaRegistration() {}

    public static void register(IEventBus bus) {
        // 顺序：方块与物品声明 -> 方块实体 -> 生物实体 -> 注册器事件监听。
        BotaniaBlocks.init();
        BotaniaItems.init();
        MythicBotanyBlocks.init();
        MythicBotanyItems.init();
        ExtraBotanyBlocks.init();
        ExtraBotanyItems.init();
        vazkii.botania.client.fx.BotaniaParticles.register(bus);
        vazkii.botania.forge.integration.ae2.BotaniaAE2.init(bus);
        vazkii.botania.common.handler.BotaniaSounds.register(bus);
        vazkii.botania.common.handler.ExtraBotanySounds.register(bus);
        BotaniaBlockEntities.init();
        BotaniaMenus.register(bus);
        vazkii.botania.common.rune.RuneRitualRecipes.register(bus);
        BotaniaEntities.init();
        BOTANIA.registerEventListeners(bus);
        GTORegistration.MYTHICBOTANY.registerEventListeners(bus);
        GTORegistration.EXTRABOTANY.registerEventListeners(bus);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS
                .addListener((net.minecraftforge.event.TickEvent.ServerTickEvent event) -> {
                    if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                        vazkii.botania.common.alfheim.AlfheimPortalHandler.endTick();
                    }
                });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS
                .addListener((net.minecraftforge.event.TickEvent.PlayerTickEvent event) -> {
                    if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && event.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && serverPlayer.tickCount % 20 == 0) {
                        vazkii.botania.common.alfheim.AlfheimTrespass.tick(serverPlayer);
                    }
                });
        bus.addListener(MythicBotanyWorldgen::register);
        bus.addListener(vazkii.botania.common.worldgen.BotaniaWorldgen::register);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS
                .addListener((net.minecraftforge.event.entity.player.EntityItemPickupEvent event) -> {
                    if (vazkii.botania.common.item.FlowerPouchItem.onPickupItem(event.getItem(), event.getEntity())) {
                        event.setCanceled(true);
                    }
                });
    }
}
