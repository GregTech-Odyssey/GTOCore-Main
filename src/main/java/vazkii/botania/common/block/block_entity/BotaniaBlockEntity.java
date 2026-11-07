package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * 植物魔法方块实体的公共基类。
 *
 * <p>
 * 统一存盘与同步入口：子类实现 {@link #readNBT} / {@link #writeNBT}，
 * {@link #markDispatchable()} 负责标记改动并通知周围客户端。
 * </p>
 */
public abstract class BotaniaBlockEntity extends BlockEntity {

    protected BotaniaBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public final void load(CompoundTag tag) {
        super.load(tag);
        readNBT(tag);
    }

    @Override
    public final void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeNBT(tag);
    }

    /** 从存盘数据读取子类字段。 */
    protected void readNBT(CompoundTag tag) {}

    /** 向存盘数据写入子类字段。 */
    protected void writeNBT(CompoundTag tag) {}

    /** 标记内容已改动，并在服务端通知周围客户端刷新。 */
    public void markDispatchable() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeNBT(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
