package com.gtocore.mixin.ae2;

import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.utils.ServerUtils;

import net.minecraft.server.level.ServerLevel;

import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.spatial.SpatialStorageWorldData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * GTO 生命周期适配：AE2 空间存储元数据查询只打开 SavedData，实际方块操作才显式加载空间世界。
 */
@Mixin(value = SpatialStoragePlotManager.class, remap = false)
public abstract class DimensionSpatialStorageMixin {

    /**
     * 查询空间区域分配元数据，复用管理器的运行或休眠 SavedData 存储。
     *
     * @return 空间存储元数据
     * @author GTO
     * @reason 元数据查询不得唤醒空间存储地形。
     */
    @Overwrite
    private SpatialStorageWorldData getWorldData() {
        return DimensionManager.get(ServerUtils.getServer()).metadataStorage(SpatialStorageDimensionIds.WORLD_ID)
                .computeIfAbsent(SpatialStorageWorldData::load, SpatialStorageWorldData::new,
                        SpatialStorageWorldData.ID);
    }

    /**
     * 为实际空间方块操作显式加载空间世界。
     *
     * @return 完成初始化的空间存储世界
     * @author GTO
     * @reason 实际空间交换需要运行世界，与元数据查询分别处理。
     */
    @Overwrite
    public ServerLevel getLevel() {
        return DimensionManager.get(ServerUtils.getServer()).loadNow(SpatialStorageDimensionIds.WORLD_ID);
    }
}
