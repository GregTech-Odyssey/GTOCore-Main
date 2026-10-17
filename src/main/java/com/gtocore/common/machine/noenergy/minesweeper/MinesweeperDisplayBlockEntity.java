package com.gtocore.common.machine.noenergy.minesweeper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * 扫雷显示方块的方块实体：只记「自己属于哪个核心方块」，格子内容不存在这里——盘面状态由核心方块实体持有，
 * 渲染用的数字直接放进方块状态（见 {@link MinesweeperDisplayBlock#STATE}）。
 */
public class MinesweeperDisplayBlockEntity extends BlockEntity {

    private static final String KEY_CORE = "core";

    /** 核心方块的位置；还没接上（刚放下、核心已被拆）时为 {@code 0}。 */
    private long core;

    public MinesweeperDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setCore(@Nullable BlockPos core) {
        this.core = core == null ? 0L : core.asLong();
        setChanged();
    }

    public @Nullable BlockPos getCore() {
        return core == 0L ? null : BlockPos.of(core);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (core != 0L) tag.putLong(KEY_CORE, core);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        core = tag.getLong(KEY_CORE);
    }
}
