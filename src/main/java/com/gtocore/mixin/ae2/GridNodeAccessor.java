package com.gtocore.mixin.ae2;

import appeng.api.networking.IGridConnection;
import appeng.me.GridConnection;
import appeng.me.GridNode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = GridNode.class, remap = false)
public interface GridNodeAccessor {

    /** Read-only access to AE's live list; avoids getConnections() allocating a snapshot. */
    @Accessor("connections")
    List<GridConnection> gto$getConnections();

    @Invoker("addConnection")
    void gto$addConnection(IGridConnection connection);
}
