package com.gtocore.common.block;

import com.gtocore.common.data.GTOBlockEntities;
import com.gtocore.common.data.GTOBlocks;

import com.gtolib.GTOCore;
import com.gtolib.utils.RLUtils;
import com.gtolib.utils.register.BlockRegisterUtils;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

import com.gto.registrate.util.entry.BlockEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.mana.LargeManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolSpec;
import vazkii.botania.common.block.mana.LargeManaPoolBlock;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.item.block.LargeManaPoolItem;

/** 秘金魔力池系列：秘金魔力池（魔力池的 8 倍）、秘金大型魔力池（大型魔力池每座的 8 倍）。 */
public final class LaurerilManaPools {

    private static ManaPoolSpec poolSpec() {
        return ManaPoolSpec.POOL.toBuilder()
                .capacity(8 * ManaPoolSpec.POOL.capacity())
                .infusionRate(8 * ManaPoolSpec.POOL.infusionRate()).build();
    }

    private static ManaPoolSpec largeSpec() {
        return ManaPoolSpec.LARGE.toBuilder()
                .capacity(8 * ManaPoolSpec.LARGE.capacity())
                .infusionRate(8 * ManaPoolSpec.LARGE.infusionRate()).build();
    }

    private static final String POOL_ID = "laureril_mana_pool";
    private static final String LARGE_ID = "large_laureril_mana_pool";

    private LaurerilManaPools() {}

    public static final class Pool extends ManaPoolBlockEntity {

        public Pool(BlockPos pos, BlockState state) {
            this(GTOBlockEntities.LAURERIL_MANA_POOL.get(), pos, state);
        }

        public Pool(BlockEntityType<Pool> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    public static final class Large extends LargeManaPoolBlockEntity {

        public Large(BlockPos pos, BlockState state) {
            this(GTOBlockEntities.LARGE_LAURERIL_MANA_POOL.get(), pos, state);
        }

        public Large(BlockEntityType<Large> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    private static final class PoolBlock extends ManaPoolBlock {

        private PoolBlock(Properties builder) {
            super(Variant.DEFAULT, builder, LaurerilManaPools.poolSpec());
        }

        @NotNull
        @Override
        public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
            return new Pool(pos, state);
        }

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                      BlockEntityType<T> type) {
            return (BlockEntityTicker<T>) (l, p, s, be) -> {
                if (be instanceof ManaPoolBlockEntity pool && be.getType() == GTOBlockEntities.LAURERIL_MANA_POOL.get()) {
                    if (level.isClientSide) {
                        ManaPoolBlockEntity.clientTick(l, p, s, pool);
                    } else {
                        ManaPoolBlockEntity.serverTick(l, p, s, pool);
                    }
                }
            };
        }
    }

    private static ModelFile child(BlockModelProvider models, String name, String parent, String... textures) {
        var builder = models.getBuilder("block/" + name)
                .parent(new ModelFile.UncheckedModelFile(RLUtils.bot("block/" + parent)));
        for (int i = 0; i < textures.length; i += 2) {
            builder.texture(textures[i], GTOCore.id("block/" + textures[i + 1]));
        }
        return builder;
    }

    public static BlockEntry<ManaPoolBlock> registerPool() {
        return BlockRegisterUtils.block(POOL_ID, "秘金魔力池", p -> (ManaPoolBlock) new PoolBlock(p))
                .lang("Laureril Mana Pool")
                .properties(p -> p.sound(SoundType.STONE).strength(2, 10).requiresCorrectToolForDrops())
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(),
                        child(prov.models(), POOL_ID, "shapes/mana_pool",
                                "bottom", "laureril_mana_pool_bottom",
                                "inside", "laureril_mana_pool_inside",
                                "side", "laureril_mana_pool_side",
                                "top", "laureril_mana_pool_top")))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .tag(CustomTags.MINEABLE_WITH_WRENCH)
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), GTOCore.id("block/" + POOL_ID)))
                .build()
                .register();
    }

    public static BlockEntry<LargeManaPoolBlock> registerLarge() {
        return BlockRegisterUtils.block(LARGE_ID, "秘金大型魔力池",
                p -> new LargeManaPoolBlock(p, largeSpec(), Large::new))
                .lang("Laureril Large Mana Pool")
                .properties(p -> p.sound(SoundType.STONE).strength(2, 10).requiresCorrectToolForDrops()
                        .pushReaction(PushReaction.BLOCK))
                .blockstate((ctx, prov) -> {
                    String[] tex = { "bottom", "large_laureril_mana_pool_bottom", "top",
                            "large_laureril_mana_pool_top", "side", "large_laureril_mana_pool_side", "particle",
                            "large_laureril_mana_pool_top" };
                    child(prov.models(), LARGE_ID, "large_mana_pool", tex);
                    ModelFile floor = child(prov.models(), LARGE_ID + "_floor", "large_mana_pool_floor", tex);
                    ModelFile rim = child(prov.models(), LARGE_ID + "_rim", "large_mana_pool_rim", tex);
                    ModelFile post = child(prov.models(), LARGE_ID + "_post", "large_mana_pool_post", tex);
                    MultiPartBlockStateBuilder builder = prov.getMultipartBuilder(ctx.get());
                    builder.part().modelFile(floor).addModel().end();
                    BooleanProperty[] sides = { LargeManaPoolBlock.NORTH, LargeManaPoolBlock.EAST,
                            LargeManaPoolBlock.SOUTH, LargeManaPoolBlock.WEST };
                    BooleanProperty[] corners = { LargeManaPoolBlock.NORTH_EAST, LargeManaPoolBlock.SOUTH_EAST,
                            LargeManaPoolBlock.SOUTH_WEST, LargeManaPoolBlock.NORTH_WEST };
                    for (int i = 0; i < 4; i++) {
                        builder.part().modelFile(rim).rotationY(i * 90).addModel().condition(sides[i], false).end();
                    }
                    for (int i = 0; i < 4; i++) {
                        builder.part().modelFile(post).rotationY(i * 90).addModel().useOr()
                                .condition(sides[i], false).condition(sides[(i + 1) % 4], false)
                                .condition(corners[i], false).end();
                    }
                })
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .tag(CustomTags.MINEABLE_WITH_WRENCH)
                .loot((ctx, prov) -> ctx.add(prov, LootTable.lootTable().withPool(LootPool.lootPool()
                        .when(ExplosionCondition.survivesExplosion())
                        .add(LootItem.lootTableItem(prov.asItem())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(prov)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(LargeManaPoolBlock.CORE, true)))))))
                .item(LargeManaPoolItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), GTOCore.id("block/" + LARGE_ID)))
                .build()
                .register();
    }

    public static final class Client {

        private Client() {}

        public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
            event.register((state, level, pos, tintIndex) -> {
                if (tintIndex != 0 || level == null || pos == null) {
                    return -1;
                }
                return level.getBlockEntity(pos) instanceof ManaPoolBlockEntity pool ? pool.getColor().map(net.minecraft.world.item.DyeColor::getFireworkColor).orElse(-1) : -1;
            }, GTOBlocks.LAURERIL_MANA_POOL.get());
            event.register((state, level, pos, tintIndex) -> {
                if (tintIndex != 0 || level == null || pos == null) {
                    return -1;
                }
                return level.getBlockEntity(pos) instanceof LargeManaPoolBlockEntity pool ? pool.getColor().map(net.minecraft.world.item.DyeColor::getFireworkColor).orElse(-1) : -1;
            }, GTOBlocks.LARGE_LAURERIL_MANA_POOL.get());
        }

        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(GTOBlockEntities.LAURERIL_MANA_POOL.get(),
                    vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(GTOBlockEntities.LARGE_LAURERIL_MANA_POOL.get(),
                    vazkii.botania.client.render.block_entity.LargeManaPoolBlockEntityRenderer::new);
        }
    }
}
