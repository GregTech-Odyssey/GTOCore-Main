package vazkii.botania.client;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import vazkii.botania.client.model.FloatingFlowerGeometry;
import vazkii.botania.client.model.PylonModelLayers;
import vazkii.botania.client.model.armor.ArmorModelLayers;
import vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;

/**
 * 复刻内容的客户端接线：方块实体渲染器与模型层。
 */
public final class BotaniaClientSetup {

    private BotaniaClientSetup() {}

    public static void init(IEventBus bus) {
        vazkii.botania.forge.integration.ae2.client.BotaniaAE2Client.init(bus);
        bus.addListener(BotaniaClientSetup::registerRenderers);
        bus.addListener(BotaniaClientSetup::registerLayerDefinitions);
        bus.addListener(BotaniaClientSetup::registerGeometryLoaders);
        bus.addListener(BotaniaColorHandlers::registerBlockColors);
        bus.addListener(BotaniaColorHandlers::registerItemColors);
        bus.addListener(BotaniaClientSetup::clientSetup);
        bus.addListener(BotaniaClientSetup::registerScreens);
        bus.addListener(BotaniaShaders::register);
        bus.addListener(vazkii.botania.client.fx.BotaniaParticles::registerProviders);
    }

    private static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(
                vazkii.botania.common.BotaniaMenus.FLOWER_BAG.get(),
                vazkii.botania.client.gui.bag.FlowerPouchGui::new));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BotaniaBlockEntities.PYLON.get(), PylonBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BotaniaBlockEntities.RETURN_PORTAL.get(),
                vazkii.botania.client.render.block_entity.RenderReturnPortal::new);
        event.registerBlockEntityRenderer(BotaniaBlockEntities.ALF_PORTAL.get(),
                vazkii.botania.client.render.block_entity.AlfheimPortalBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BotaniaBlockEntities.MANA_SPREADER.get(),
                vazkii.botania.client.render.block_entity.ManaSpreaderBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BotaniaBlockEntities.PETAL_APOTHECARY.get(),
                vazkii.botania.client.render.block_entity.PetalApothecaryBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BotaniaBlockEntities.MANA_POOL.get(),
                vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BotaniaBlockEntities.FLUIX_MANA_POOL.get(),
                vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BotaniaBlockEntities.CORPOREA_INDEX.get(),
                vazkii.botania.client.render.block_entity.CorporeaIndexBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BotaniaBlockEntities.LARGE_MANA_POOL.get(),
                vazkii.botania.client.render.block_entity.LargeManaPoolBlockEntityRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        PylonModelLayers.register(event);
        vazkii.botania.client.model.BotaniaModelLayers.register(event);
        ArmorModelLayers.register(event);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(BotaniaRenderLayers::init);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.client.event.RegisterColorHandlersEvent.Block colorEvent) -> com.gtocore.common.block.LaurerilManaPools.Client
                        .registerBlockColors(colorEvent));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.event.TickEvent.ClientTickEvent tickEvent) -> {
                    if (tickEvent.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                        vazkii.botania.client.core.handler.ClientTickHandler
                                .clientTickEnd(net.minecraft.client.Minecraft.getInstance());
                    }
                });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.event.TickEvent.RenderTickEvent renderEvent) -> {
                    if (renderEvent.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                        vazkii.botania.client.core.handler.ClientTickHandler.renderTick(renderEvent.renderTickTime);
                    }
                });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.client.event.RenderGuiEvent.Post guiEvent) -> vazkii.botania.common.alfheim.AlfheimPortalHandler.renderOverlay(guiEvent.getGuiGraphics()));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.event.TickEvent.ClientTickEvent tickEvent) -> {
                    if (tickEvent.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                        var mc = net.minecraft.client.Minecraft.getInstance();
                        vazkii.botania.common.alfheim.AlfheimPortalHandler.clientTick(mc.level, mc.player);
                    }
                });
    }

    private static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("floating_flower", FloatingFlowerGeometry.Loader.INSTANCE);
        event.register("mana_gun", vazkii.botania.forge.client.ForgeManaBlasterModel.Loader.INSTANCE);
    }
}
