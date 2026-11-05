package com.gtocore.mixin.xaero;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xaero.map.world.MapDimension;

@Mixin(value = MapDimension.class, remap = false)
public abstract class MapDimensionMixin {

    @Shadow
    @Final
    private ResourceKey<Level> dimId;

    // 非主世界会休眠，且未修改的地形不保证写盘；地图必须独立保存已探索的数据。
    @ModifyReturnValue(method = { "isUsingWorldSave", "isFutureUsingWorldSaveUnsynced" }, at = @At("RETURN"))
    private boolean gtolib$usePersistentMapData(boolean original) {
        return original && dimId == Level.OVERWORLD;
    }
}
