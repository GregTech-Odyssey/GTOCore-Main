package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.PylonBlockEntity;
import vazkii.botania.common.entity.GaiaGuardianEntity;

/**
 * 水晶方块。三种变体共用一套方块实体与渲染器，颜色与附魔加成由变体决定。
 */
public class PylonBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 21, 14);

    public enum Variant {

        MANA(8f, 0.5f, 0.5f, 1f),
        NATURA(15f, 0.5f, 1f, 0.5f),
        GAIA(15f, 1f, 0.5f, 1f),
        ALFSTEEL(15f, 1f, 0.7f, 0.2f);

        public final float enchantPowerBonus;
        public final float r;
        public final float g;
        public final float b;

        Variant(float enchantPowerBonus, float r, float g, float b) {
            this.enchantPowerBonus = enchantPowerBonus;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public final Variant variant;

    public PylonBlock(@NotNull Variant variant, BlockBehaviour.Properties properties) {
        super(properties);
        this.variant = variant;
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PylonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != BotaniaBlockEntities.PYLON.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> PylonBlockEntity.commonTick(tickLevel, pos, tickState, (PylonBlockEntity) be);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return variant.enchantPowerBonus;
    }

    /** 盖亚水晶：手持泰拉钢锭或盖亚之魂右键召唤盖亚守护者。 */
    @NotNull
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull net.minecraft.world.level.Level level,
                            @NotNull BlockPos pos, @NotNull net.minecraft.util.RandomSource random) {
        if (random.nextInt(4) != 0) {
            return;
        }
        level.addParticle(
                vazkii.botania.client.fx.SparkleParticleData.sparkle(random.nextFloat() * 0.5F + 0.5F, variant.r,
                        variant.g, variant.b, 4),
                pos.getX() + 0.25 + random.nextDouble() * 0.5, pos.getY() + 0.4 + random.nextDouble() * 0.6,
                pos.getZ() + 0.25 + random.nextDouble() * 0.5, 0, 0.01, 0);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player,
                                 @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (variant != Variant.GAIA || level.isClientSide) {
            return super.use(state, level, pos, player, hand, hit);
        }
        ItemStack stack = player.getItemInHand(hand);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if ("extrabotany".equals(id.getNamespace()) && "nature_orb".equals(id.getPath())) {
            return vazkii.botania.common.entity.GaiaGuardianIIIEntity.spawn(player, stack, level, pos) ? InteractionResult.sidedSuccess(false) : InteractionResult.FAIL;
        }
        if (!"botania".equals(id.getNamespace())) {
            return super.use(state, level, pos, player, hand, hit);
        }
        boolean gaiaIngot = "gaia_ingot".equals(id.getPath());
        if (!gaiaIngot && !"terrasteel_ingot".equals(id.getPath())) {
            return super.use(state, level, pos, player, hand, hit);
        }
        return GaiaGuardianEntity.spawn(player, stack, level, pos, gaiaIngot) ? InteractionResult.sidedSuccess(false) : InteractionResult.FAIL;
    }
}
