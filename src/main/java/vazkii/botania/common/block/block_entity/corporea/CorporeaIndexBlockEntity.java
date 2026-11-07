package vazkii.botania.common.block.block_entity.corporea;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.block.block_entity.BotaniaBlockEntity;

/** 多媒体索引的方块实体：渲染由 {@code CorporeaIndexBlockEntityRenderer} 完成。 */
public class CorporeaIndexBlockEntity extends BotaniaBlockEntity {

    public CorporeaIndexBlockEntity(BlockPos pos, BlockState state) {
        this(vazkii.botania.common.block.block_entity.BotaniaBlockEntities.CORPOREA_INDEX.get(), pos, state);
    }

    public CorporeaIndexBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
