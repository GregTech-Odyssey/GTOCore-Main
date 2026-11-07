package vazkii.botania.common.block.mana;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.state.BotaniaStateProperties.OptionalDyeColor;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolSpec;

import java.util.List;
import java.util.Optional;

import static vazkii.botania.api.state.BotaniaStateProperties.OPTIONAL_DYE_COLOR;

/**
 * 魔力池方块。四种变体共用一套方块实体，容量由 {@link ManaPoolSpec} 决定。
 */
public class ManaPoolBlock extends Block implements EntityBlock, ManaPoolSpec.Holder {

    private static final VoxelShape NORMAL_SHAPE;
    private static final VoxelShape DILUTED_SHAPE;
    private static final VoxelShape CREATIVE_SHAPE;
    private static final VoxelShape NORMAL_SHAPE_INTERACT;
    private static final VoxelShape DILUTED_SHAPE_INTERACT;
    private static final VoxelShape CREATIVE_SHAPE_INTERACT;

    static {
        NORMAL_SHAPE_INTERACT = box(0, 0, 0, 16, 8, 16);
        DILUTED_SHAPE_INTERACT = box(0, 0, 0, 16, 6, 16);
        CREATIVE_SHAPE_INTERACT = box(0, 0, 0, 16, 10, 16);

        VoxelShape cutout = box(2, 2, 2, 14, 16, 14);
        VoxelShape dilutedCutout = box(1, 1, 1, 15, 6, 15);

        NORMAL_SHAPE = Shapes.join(NORMAL_SHAPE_INTERACT, cutout, BooleanOp.ONLY_FIRST);
        DILUTED_SHAPE = Shapes.join(DILUTED_SHAPE_INTERACT, dilutedCutout, BooleanOp.ONLY_FIRST);
        CREATIVE_SHAPE = Shapes.join(CREATIVE_SHAPE_INTERACT, cutout, BooleanOp.ONLY_FIRST);
    }

    public enum Variant {
        DEFAULT,
        CREATIVE,
        DILUTED,
        FABULOUS
    }

    public final Variant variant;
    private final ManaPoolSpec spec;

    public ManaPoolBlock(Variant variant, Properties properties) {
        this(variant, properties, variant == Variant.DILUTED ? ManaPoolSpec.DILUTED : ManaPoolSpec.POOL);
    }

    public ManaPoolBlock(Variant variant, Properties properties, ManaPoolSpec spec) {
        super(properties);
        this.variant = variant;
        this.spec = spec;
        registerDefaultState(defaultBlockState().setValue(OPTIONAL_DYE_COLOR, OptionalDyeColor.NONE));
    }

    @Override
    public ManaPoolSpec poolSpec() {
        return spec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPTIONAL_DYE_COLOR);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (variant == Variant.CREATIVE) {
            for (int i = 0; i < 2; i++) {
                tooltip.add(Component.translatable("botaniamisc.creativePool" + i).withStyle(ChatFormatting.GRAY));
            }
        } else if (variant == Variant.FABULOUS) {
            tooltip.add(Component.translatable("botaniamisc.fabulousPool0").withStyle(ChatFormatting.GRAY));
        }
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (variant) {
            case DILUTED -> DILUTED_SHAPE;
            case CREATIVE -> CREATIVE_SHAPE;
            case DEFAULT, FABULOUS -> NORMAL_SHAPE;
        };
    }

    @NotNull
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return switch (variant) {
            case DILUTED -> DILUTED_SHAPE_INTERACT;
            case CREATIVE -> CREATIVE_SHAPE_INTERACT;
            case DEFAULT, FABULOUS -> NORMAL_SHAPE_INTERACT;
        };
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player,
                                 @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ManaPoolBlockEntity pool)) {
            return super.use(state, level, pos, player, hand, hit);
        }
        ItemStack stack = player.getItemInHand(hand);

        // 花瓣与蘑菇给池染色，黏土球清除颜色
        Optional<DyeColor> itemColor = colorOf(stack);
        if (itemColor.isPresent()) {
            if (!itemColor.equals(pool.getColor())) {
                pool.setColor(itemColor);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return InteractionResult.PASS;
        }
        if (stack.is(net.minecraft.world.item.Items.CLAY_BALL) && pool.getColor().isPresent()) {
            pool.setColor(Optional.empty());
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        // 潜行右键切换输出模式
        if (player.isShiftKeyDown() && stack.isEmpty()) {
            if (!level.isClientSide) {
                pool.setOutputting(!pool.isOutputtingPower());
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    /** 依物品注册名推断颜色：{@code botania:<color>_petal} 与 {@code botania:<color>_mushroom}。 */
    private static Optional<DyeColor> colorOf(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!"botania".equals(id.getNamespace())) return Optional.empty();
        String path = id.getPath();
        String colorName;
        if (path.endsWith("_petal")) {
            colorName = path.substring(0, path.length() - "_petal".length());
        } else if (path.endsWith("_mushroom")) {
            colorName = path.substring(0, path.length() - "_mushroom".length());
        } else {
            return Optional.empty();
        }
        return Optional.ofNullable(DyeColor.byName(colorName, null));
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ManaPoolBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != BotaniaBlockEntities.MANA_POOL.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> {
            ManaPoolBlockEntity pool = (ManaPoolBlockEntity) be;
            if (tickLevel.isClientSide) {
                ManaPoolBlockEntity.clientTick(tickLevel, pos, tickState, pool);
            } else {
                ManaPoolBlockEntity.serverTick(tickLevel, pos, tickState, pool);
            }
        };
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ManaPoolBlockEntity pool ? ManaPoolBlockEntity.calculateComparatorLevel(pool.getCurrentMana(), pool.getMaxMana()) : 0;
    }
}
