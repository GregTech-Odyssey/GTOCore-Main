package com.gtocore.common.data;

import net.minecraft.client.renderer.RenderType;

import com.gto.registrate.ClientEvent;

import static com.gtocore.common.data.GTOMachines.*;

public class ClientData {

    public static void init() {
        // Initialize client-side data here
        ClientEvent.setBlockRenderLayer(ADJUSTABLE_SEMI_REFLECTOR, RenderType::translucent);
        ClientEvent.setBlockRenderLayer(BEAM_REDIRECTOR, RenderType::translucent);
        ClientEvent.setBlockRenderLayer(EXCITATION_CRYSTAL, RenderType::translucent);
        ClientEvent.setBlockRenderLayer(RAY_BEAM_POLARIZER, RenderType::translucent);
    }
}
