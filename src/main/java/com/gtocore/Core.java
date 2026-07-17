package com.gtocore;

import com.gtocore.client.ClientProxy;
import com.gtocore.common.CommonProxy;

import com.gtolib.GTOCore;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

/**
 * Single Forge entrypoint for the merged GTOdyssey mod (former gtocore + gtolib).
 */
@Mod(GTOCore.MOD_ID)
public final class Core {

    public Core() {
        // Former gtolib @Mod constructor — must run before core proxies.
        GTOCore.bootstrap();
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }
}
