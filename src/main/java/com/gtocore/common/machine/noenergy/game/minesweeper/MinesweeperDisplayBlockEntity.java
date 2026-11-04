package com.gtocore.common.machine.noenergy.game.minesweeper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.blockentity.FieldDataHolderBlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 扫雷显示方块的方块实体：只记「自己属于哪个核心方块」；盘面状态在核心方块实体里，渲染值在方块状态
 * （见 {@link MinesweeperDisplayBlock#STATE}）。
 *
 * <p>
 * 存盘由 DataSyncLib 负责：{@link #core} 经 {@code @SaveToDisk} 写进 {@code field_save}；未接上核心时为 {@code 0}，
 * 配合 {@code defaultValue = "0"} 不占存档字节。
 */
public class MinesweeperDisplayBlockEntity extends FieldDataHolderBlockEntity {

    /** 旧存档（未走 DataSyncLib 时）写在根上的键，仅用于读档迁移。 */
    private static final String LEGACY_KEY_CORE = "core";

    /** 核心方块的位置（{@code BlockPos#asLong}）；还没接上（刚放下、核心已被拆）时为 {@code 0}。 */
    @SaveToDisk(defaultValue = "0")
    private long core;

    public MinesweeperDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * 旧档迁移：位置原先写在根键 {@code "core"} 上，现在由 {@code field_save} 承载。
     * 只有新字段仍是默认值且根键存有旧值时才回填；新存档不写这个根键，不会误覆盖。
     */
    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (core == 0L && tag.contains(LEGACY_KEY_CORE, Tag.TAG_LONG)) {
            core = tag.getLong(LEGACY_KEY_CORE);
        }
    }

    public void setCore(@Nullable BlockPos core) {
        this.core = core == null ? 0L : core.asLong();
        setChanged();
    }

    public @Nullable BlockPos getCore() {
        return core == 0L ? null : BlockPos.of(core);
    }
}
