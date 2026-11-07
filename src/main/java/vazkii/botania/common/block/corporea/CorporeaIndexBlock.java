package vazkii.botania.common.block.corporea;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;

/** 多媒体索引：方块模型为空，由方块实体渲染器绘制旋转的环与核心。 */
public class CorporeaIndexBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = box(2, 2, 2, 14, 14, 14);

    public CorporeaIndexBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                               @NotNull CollisionContext context) {
        return SHAPE;
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CorporeaIndexBlockEntity(pos, state);
    }
}
