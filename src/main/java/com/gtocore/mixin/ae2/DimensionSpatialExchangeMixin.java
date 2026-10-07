package com.gtocore.mixin.ae2;

import com.gtolib.api.dimension.DimensionLifecycle;
import com.gtolib.api.dimension.DimensionManager;

import net.minecraft.server.level.ServerLevel;

import appeng.spatial.SpatialStorageHelper;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

/**
 * AE2 实际区域交换期间同时保活源世界和目的世界，处理结束或异常退出时配对释放租约。
 */
@Mixin(value = SpatialStorageHelper.class, remap = false)
public abstract class DimensionSpatialExchangeMixin {

    @WrapMethod(method = "swapRegions")
    private void gtolib$pinExchange(ServerLevel src, int sx, int sy, int sz, ServerLevel dst, int dx, int dy, int dz,
                                    int sizeX, int sizeY, int sizeZ, Operation<Void> original) {
        var manager = DimensionManager.get(src.getServer());
        try (var source = manager.keepAlive(src.dimension(), DimensionLifecycle.KeepAlive.SPATIAL_EXCHANGE);
                var destination = manager.keepAlive(dst.dimension(), DimensionLifecycle.KeepAlive.SPATIAL_EXCHANGE)) {
            original.call(src, sx, sy, sz, dst, dx, dy, dz, sizeX, sizeY, sizeZ);
        }
    }
}
