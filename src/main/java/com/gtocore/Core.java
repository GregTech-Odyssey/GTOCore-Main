package com.gtocore;

import com.gtocore.client.ClientProxy;
import com.gtocore.common.CommonProxy;

import com.gtolib.GTOCore;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

/**
 * Single Forge entrypoint for the merged GTOdyssey mod (former gtocore + gtolib).
 */
@Mod(GTOCore.MOD_ID)
public final class Core {

    public Core(FMLJavaModLoadingContext context) {
        // Former gtolib @Mod constructor — must run before core proxies.
        GTOCore.bootstrap();
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        GTMTHINGS_REGISTRATE.registerEventListeners(context.getModEventBus());
    }
}
