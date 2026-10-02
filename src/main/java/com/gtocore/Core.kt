package com.gtocore

import com.gtocore.client.ClientProxy
import com.gtocore.common.CommonProxy
import com.gtolib.GTOCore
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import java.util.function.Supplier

/**
 * Single Forge entrypoint for the merged GTOdyssey mod (former gtocore + gtolib).
 */
@Mod(GTOCore.MOD_ID)
class Core(context: FMLJavaModLoadingContext) {
    init {
        GTOCore.bootstrap()
        DistExecutor.unsafeRunForDist({ Supplier { ClientProxy() } }, { Supplier { CommonProxy() } })
        GTMTRegistration.GTMTHINGS_REGISTRATE.registerEventListeners(context.modEventBus)
    }
}
