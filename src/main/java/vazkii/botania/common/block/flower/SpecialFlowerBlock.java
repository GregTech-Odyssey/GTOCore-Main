package vazkii.botania.common.block.flower;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.BotaniaFlowerBlock;

/**
 * 特殊花方块基类。
 *
 * <p>
 * 特殊花由方块实体自行决定工作节奏，基类只把每 tick 转交给
 * {@link SpecialFlowerBlockEntity#commonTick} 并交给子类创建实体。
 * 本类不注册任何具体花。
 * </p>
 */
public abstract class SpecialFlowerBlock extends BotaniaFlowerBlock implements EntityBlock {

    public SpecialFlowerBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(color, properties);
    }

    /** 由具体花提供对应的方块实体。 */
    protected abstract SpecialFlowerBlockEntity createFlowerBlockEntity(BlockPos pos, BlockState state);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return createFlowerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> {
            if (be instanceof SpecialFlowerBlockEntity flower) {
                SpecialFlowerBlockEntity.commonTick(tickLevel, pos, tickState, flower);
            }
        };
    }
}
