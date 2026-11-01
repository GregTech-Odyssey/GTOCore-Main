package com.gtocore.common.item;

import com.gregtechceu.gtceu.api.item.IComponentItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * 物品上存储的跨维度坐标，统一按 {@link GlobalPos#CODEC} 写入 {@code dimension} / {@code pos}；
 * 旧版物品的 {@code x}/{@code y}/{@code z} 整数坐标只读兼容，写入时清除。
 */
public interface ICoordinateHolder {

    @Nullable
    GlobalPos getStoredGlobalPos(ItemStack stack);

    static @Nullable GlobalPos getGlobalPos(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getItem() instanceof ICoordinateHolder holder) {
            return holder.getStoredGlobalPos(stack);
        }
        if (stack.getItem() instanceof IComponentItem componentItem) {
            for (var component : componentItem.getComponents()) {
                if (component instanceof ICoordinateHolder holder) {
                    var pos = holder.getStoredGlobalPos(stack);
                    if (pos != null) return pos;
                }
            }
        }
        return null;
    }

    /**
     * @param legacyDimensionKey 旧版存放维度 ID 的键
     * @param fallback           旧版未记录维度时使用的维度
     */
    static @Nullable GlobalPos read(@Nullable CompoundTag tag, String legacyDimensionKey, @Nullable ResourceKey<Level> fallback) {
        if (tag == null) return null;
        var result = GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag).result();
        if (result.isPresent()) return result.get();
        var pos = readLegacyPos(tag);
        if (pos == null) return null;
        var dimension = tag.getString(legacyDimensionKey);
        var id = dimension.isEmpty() ? null : ResourceLocation.tryParse(dimension);
        var key = id == null ? fallback : ResourceKey.create(Registries.DIMENSION, id);
        return key == null ? null : GlobalPos.of(key, pos);
    }

    static @Nullable BlockPos readPos(@Nullable CompoundTag tag) {
        if (tag == null) return null;
        var result = GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag).result();
        return result.map(GlobalPos::pos).orElseGet(() -> readLegacyPos(tag));
    }

    static void write(CompoundTag tag, GlobalPos pos) {
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result().ifPresent(t -> tag.merge((CompoundTag) t));
        removeLegacyPos(tag);
    }

    /** 移除坐标及给定的附加键，标签清空后置空 */
    static void clear(ItemStack stack, String... extraKeys) {
        var tag = stack.getTag();
        if (tag == null) return;
        tag.remove("dimension");
        tag.remove("pos");
        removeLegacyPos(tag);
        for (var key : extraKeys) tag.remove(key);
        if (tag.isEmpty()) stack.setTag(null);
    }

    private static @Nullable BlockPos readLegacyPos(CompoundTag tag) {
        return tag.contains("x") && tag.contains("y") && tag.contains("z") ? new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")) : null;
    }

    private static void removeLegacyPos(CompoundTag tag) {
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
    }
}
