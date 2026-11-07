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
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.common.block.BotaniaFlowerBlock;

/**
 * 产魔花方块基类。
 *
 * <p>
 * 产魔花按固定间隔执行一次 {@link GeneratingFlowerBlockEntity#tickFlower()} 积累魔力，
 * 随后把积攒的魔力送入绑定的魔力发射器。本类不注册任何具体花。
 * </p>
 */
public abstract class GeneratingFlowerBlock extends BotaniaFlowerBlock implements EntityBlock {

    /** 产魔花的工作间隔，与原模组一致为每秒一次。 */
    public static final int TICK_INTERVAL = 20;

    public GeneratingFlowerBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(color, properties);
    }

    /** 由具体花提供对应的方块实体。 */
    protected abstract GeneratingFlowerBlockEntity createFlowerBlockEntity(BlockPos pos, BlockState state);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return createFlowerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> {
            if (be instanceof GeneratingFlowerBlockEntity flower && tickLevel.getGameTime() % TICK_INTERVAL == 0) {
                flower.tickFlower();
                flower.emptyManaIntoCollector();
            }
        };
    }
}
