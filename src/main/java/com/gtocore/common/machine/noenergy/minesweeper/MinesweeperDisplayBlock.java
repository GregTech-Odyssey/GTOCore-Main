package com.gtocore.common.machine.noenergy.minesweeper;

import com.gtocore.common.data.GTOBlockEntities;
import com.gtocore.common.data.GTOBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 扫雷显示方块：一格一个，铺在核心方块旁边；格子的内容不存在这里——它读核心方块的状态，渲染用的数字直接写在方块状态
 * {@link #STATE} 上（由核心方块在落子后改写），所以两端不用再同步一份数据。
 *
 * <p>
 * 右击翻开、潜行右击插旗，都转发给核心方块处理（{@link MinesweeperCoreBlockEntity#click}）。
 */
public class MinesweeperDisplayBlock extends Block implements EntityBlock {

    /** 显示内容：0 未翻开、1~8 数字、9 空、10 旗、11 雷、12 踩中的雷。 */
    public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 12);

    /** 格子状态（{@link MinesweeperRules} 的取值）→ 方块状态属性的取值。 */
    public static int propertyFor(int cellState) {
        return switch (cellState) {
            case MinesweeperRules.OPEN_EMPTY -> 9;
            case MinesweeperRules.FLAGGED -> 10;
            case MinesweeperRules.BOMB -> 11;
            case MinesweeperRules.DEATH_BOMB -> 12;
            default -> cellState >= MinesweeperRules.NUMBER_MIN ? cellState : 0;
        };
    }

    /** 某个格子状态对应的显示方块状态（铺盘与刷新都用它）。 */
    public static BlockState displayState(int cellState) {
        return GTOBlocks.MINESWEEPER_DISPLAY.get().defaultBlockState().setValue(STATE, propertyFor(cellState));
    }

    public MinesweeperDisplayBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STATE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinesweeperDisplayBlockEntity(GTOBlockEntities.MINESWEEPER_DISPLAY.get(), pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof MinesweeperDisplayBlockEntity display)) return InteractionResult.PASS;
        BlockPos core = display.getCore();
        if (core == null || !level.isLoaded(core)) return InteractionResult.PASS;
        if (level.getBlockEntity(core) instanceof MinesweeperCoreBlockEntity coreEntity &&
                player instanceof ServerPlayer serverPlayer) {
            coreEntity.click(pos, serverPlayer, player.isSecondaryUseActive());
        }
        return InteractionResult.SUCCESS;
    }
}
