package com.gtocore.common.block;

import com.gtocore.common.data.GTOBlockEntities;

import com.gtolib.GTOCore;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

import com.gto.registrate.util.entry.BlockEntry;
import com.hollingsworth.arsnouveau.common.block.LargeSourceJar;
import com.hollingsworth.arsnouveau.common.block.SourceJar;
import com.hollingsworth.arsnouveau.common.block.tile.LargeSourceJarTile;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;
import com.hollingsworth.arsnouveau.common.items.LargeSourceJarItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.gtolib.utils.register.BlockRegisterUtils.block;

/** 秘金魔源罐系列：旧版（一千个魔源罐，已弃用）、秘金魔源罐（魔源罐的 8 倍）、秘金大型魔源罐（大型魔源罐的 8 倍）。 */
public final class LaurerilSourceJars {

    public static final long LEGACY_CAPACITY = 10_000_000L;
    public static final long STANDARD_CAPACITY = 8 * 10_000L;
    public static final long LARGE_UNIT_CAPACITY = 8 * LargeSourceJarTile.UNIT_CAPACITY;
    private static final String[] LARGE_KINDS = { "bottom", "mid", "top", "seam", "empty" };

    private LaurerilSourceJars() {}

    public static final class Legacy extends SourceJarTile {

        public Legacy(BlockPos pos, BlockState state) {
            this(GTOBlockEntities.SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY.get(), pos, state);
        }

        public Legacy(BlockEntityType<Legacy> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public int getMaxSource() {
            return (int) LEGACY_CAPACITY;
        }
    }

    public static final class Standard extends SourceJarTile {

        public Standard(BlockPos pos, BlockState state) {
            this(GTOBlockEntities.STANDARD_LAURERIL_SOURCE_JAR.get(), pos, state);
        }

        public Standard(BlockEntityType<Standard> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public int getMaxSource() {
            return (int) STANDARD_CAPACITY;
        }
    }

    public static final class Large extends LargeSourceJarTile {

        public Large(BlockPos pos, BlockState state) {
            this(GTOBlockEntities.LARGE_LAURERIL_SOURCE_JAR.get(), pos, state);
        }

        public Large(BlockEntityType<Large> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public long unitCapacity() {
            return LARGE_UNIT_CAPACITY;
        }
    }

    private static SourceJar jarBlock(Block.Properties properties, String name, long capacity, BiFunction<BlockPos, BlockState, ? extends SourceJarTile> factory) {
        return new SourceJar(properties, name) {

            @Override
            public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                return factory.apply(pos, state);
            }

            @Override
            public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
                if (stack.getTag() == null) return;
                long source = stack.getTag().getCompound("BlockEntityTag").getLong("source");
                tooltip.add(Tips.stat(Component.translatable("ars_nouveau.source_jar.fullness", FormattingUtil.formatNumber2Places(source * 100D / capacity))));
            }
        };
    }

    private static BlockEntry<SourceJar> jar(String id, String cn, String en, long capacity, BiFunction<BlockPos, BlockState, ? extends SourceJarTile> factory,
                                             Supplier<? extends BlockEntityType<?>> type) {
        return block(id, cn, p -> jarBlock(p, id, capacity, factory))
                .lang(en)
                .properties(props -> props
                        .sound(SoundType.STONE)
                        .strength(50, 2000)
                        .requiresCorrectToolForDrops()
                        .mapColor(MapColor.COLOR_BLACK)
                        .instrument(NoteBlockInstrument.BASEDRUM))
                .blockstate((ctx, prov) -> {
                    prov.getVariantBuilder(ctx.get()).forAllStates(state -> {
                        int fill = Math.min(state.getValue(SourceJar.fill) * 10, 100);
                        return ConfiguredModel.builder().modelFile(prov.models().singleTexture(id + fill, RLUtils.ars("block/source_jar/source_jar" + fill), "2", GTOCore.id("block/laureril_source_jar"))).build();
                    });
                    prov.models().singleTexture(id, RLUtils.ars("block/source_jar/source_jar0"), "2", GTOCore.id("block/laureril_source_jar"));
                })
                .tag(CustomTags.MINEABLE_WITH_WRENCH)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .loot((ctx, prov) -> ctx.add(prov, LootTable.lootTable().withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(prov.asItem())
                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy("source", "BlockEntityTag.source"))
                                .apply(SetContainerContents.setContents(type.get()).withEntry(DynamicLoot.dynamicEntry(RLUtils.mc("contents"))))))))
                .item()
                .build()
                .register();
    }

    public static BlockEntry<SourceJar> registerLegacy() {
        return jar("laureril_source_jar", "秘金魔源罐(旧)", "Laureril Source Jar (Old)", LEGACY_CAPACITY, Legacy::new, () -> GTOBlockEntities.SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY.get());
    }

    public static BlockEntry<SourceJar> registerStandard() {
        return jar("standard_laureril_source_jar", "秘金魔源罐", "Laureril Source Jar", STANDARD_CAPACITY, Standard::new, () -> GTOBlockEntities.STANDARD_LAURERIL_SOURCE_JAR.get());
    }

    public static BlockEntry<LargeSourceJar> registerLarge() {
        String id = "large_laureril_source_jar";
        return block(id, "秘金大型魔源罐", p -> new LargeSourceJar(p, Large::new))
                .lang("Laureril Large Source Jar")
                .initialProperties(() -> Blocks.GLASS)
                .properties(p -> p.sound(SoundType.STONE).strength(50, 2000).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_BLACK).noOcclusion()
                        .pushReaction(PushReaction.BLOCK).isViewBlocking((s, l, pos) -> false).isSuffocating((s, l, pos) -> false))
                .blockstate((ctx, prov) -> {
                    for (String kind : LARGE_KINDS) {
                        prov.models().getBuilder("block/" + id + "/" + kind)
                                .parent(new ModelFile.UncheckedModelFile(RLUtils.ars("block/large_source_jar/" + kind)))
                                .texture("jar", GTOCore.id("block/" + id))
                                .texture("particle", GTOCore.id("block/" + id))
                                .renderType("translucent");
                    }
                    prov.getVariantBuilder(ctx.get()).forAllStates(state -> {
                        int y = state.getValue(LargeSourceJar.PART_Y);
                        boolean joined = state.getValue(LargeSourceJar.JOINED);
                        boolean center = state.getValue(LargeSourceJar.PART_X) == 1 && state.getValue(LargeSourceJar.PART_Z) == 1;
                        String kind = !center ? "empty" : y == 1 || (y == 2 && joined) ? "mid" : y == 0 ? (joined ? "seam" : "bottom") : "top";
                        return ConfiguredModel.builder().modelFile(new ModelFile.UncheckedModelFile(GTOCore.id("block/" + id + "/" + kind))).build();
                    });
                })
                .tag(CustomTags.MINEABLE_WITH_WRENCH)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .loot((ctx, prov) -> ctx.add(prov, LootTable.lootTable()))
                .item((b, p) -> new LargeSourceJarItem(b, p, () -> LARGE_UNIT_CAPACITY))
                .model((ctx, prov) -> prov.getBuilder(ctx.getName())
                        .parent(new ModelFile.UncheckedModelFile(RLUtils.ars("block/large_source_jar/item")))
                        .texture("jar", GTOCore.id("block/" + id))
                        .texture("particle", GTOCore.id("block/" + id)))
                .build()
                .register();
    }
}
