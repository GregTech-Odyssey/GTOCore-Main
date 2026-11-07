package vazkii.botania.forge.integration.ae2.client;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.api.client.AEKeyRendering;

import vazkii.botania.forge.integration.ae2.ManaKey;
import vazkii.botania.forge.integration.ae2.ManaKeyType;

/** AE2 客户端接线：魔力键在终端里的图标与名称。 */
public final class BotaniaAE2Client {

    private BotaniaAE2Client() {}

    public static void init(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(
                () -> AEKeyRendering.register(ManaKeyType.TYPE, ManaKey.class, new ManaKeyRenderer())));
    }
}
