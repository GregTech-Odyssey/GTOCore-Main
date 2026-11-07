package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.block.PylonBlock;

/**
 * 水晶方块实体。
 *
 * <p>
 * 保留原模组的激活状态与中心坐标，供盖亚召唤与传送门联动使用；渲染动画由客户端渲染器驱动。
 * </p>
 */
public class PylonBlockEntity extends BotaniaBlockEntity {

    private static final String TAG_ACTIVATED = "activated";
    private static final String TAG_CENTER_X = "centerX";
    private static final String TAG_CENTER_Y = "centerY";
    private static final String TAG_CENTER_Z = "centerZ";

    private boolean activated = false;
    private BlockPos centerPos = BlockPos.ZERO;
    private int ticks = 0;

    public PylonBlockEntity(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.PYLON.get(), pos, state);
    }

    public PylonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(worldPosition).inflate(1.0, 2.0, 1.0);
    }

    public PylonBlock.Variant variant() {
        return getBlockState().getBlock() instanceof PylonBlock pylon ? pylon.variant : PylonBlock.Variant.MANA;
    }

    public boolean isActivated() {
        return activated;
    }

    public BlockPos getCenterPos() {
        return centerPos;
    }

    public void setActivated(boolean activated, BlockPos centerPos) {
        this.activated = activated;
        if (centerPos != null) {
            this.centerPos = centerPos;
        }
        markDispatchable();
    }

    public static void commonTick(Level level, BlockPos pos, BlockState state, PylonBlockEntity self) {
        ++self.ticks;
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        activated = tag.getBoolean(TAG_ACTIVATED);
        centerPos = new BlockPos(tag.getInt(TAG_CENTER_X), tag.getInt(TAG_CENTER_Y), tag.getInt(TAG_CENTER_Z));
    }

    @Override
    protected void writeNBT(CompoundTag tag) {
        tag.putBoolean(TAG_ACTIVATED, activated);
        tag.putInt(TAG_CENTER_X, centerPos.getX());
        tag.putInt(TAG_CENTER_Y, centerPos.getY());
        tag.putInt(TAG_CENTER_Z, centerPos.getZ());
    }
}
