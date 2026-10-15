package com.gtocore;

import com.gtocore.client.ClientProxy;
import com.gtocore.common.CommonProxy;

import com.gtolib.GTOCore;

import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

/**
 * Single Forge entrypoint for the merged GTOdyssey mod (former gtocore + gtolib).
 */
@Mod(GTOCore.MOD_ID)
public final class Core {

    public Core(FMLJavaModLoadingContext context) {
        // Former gtolib @Mod constructor — must run before core proxies.
        GTOCore.bootstrap();
        String buildId = context.getContainer().getModInfo().getOwningFile().getFile().getSecureJar().moduleDataProvider().getManifest().getMainAttributes().getValue("GTO-Build-Id");
        String shownBuildId = buildId == null ? "dev" : buildId;
        Configurator.setLevel("GTO Build", Level.INFO);
        LogManager.getLogger("GTO Build").info("GTO-Build-Id: {}", shownBuildId);
        CrashReportCallables.registerCrashCallable("GTO-Build-Id", () -> shownBuildId);
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        GTMTHINGS_REGISTRATE.registerEventListeners(context.getModEventBus());
    }
}
