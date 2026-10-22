package com.gtocore.common.machine.noenergy.minesweeper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/** 扫雷控制方块的方块实体：只记「自己属于哪个核心方块」。 */
public class MinesweeperControlBlockEntity extends BlockEntity {

    private static final String KEY_CORE = "core";

    private long core;

    public MinesweeperControlBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
