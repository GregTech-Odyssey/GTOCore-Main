package com.gtocore.common.block;

import com.gtocore.common.data.GTOBlockEntities;

import com.gtolib.GTOCore;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
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
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarSpec;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;
import com.hollingsworth.arsnouveau.common.items.LargeSourceJarItem;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.gtolib.utils.register.BlockRegisterUtils.block;

/** 秘金魔源罐系列：旧版（一千个魔源罐，已弃用）、秘金魔源罐（魔源罐的 8 倍）、秘金大型魔源罐（大型魔源罐的 8 倍）。 */
public final class LaurerilSourceJars {

    public static final SourceJarSpec LEGACY = SourceJarSpec.JAR.toBuilder().capacity(1000 * SourceJarSpec.JAR.capacity()).build();
    public static final SourceJarSpec STANDARD = SourceJarSpec.JAR.toBuilder().capacity(8 * SourceJarSpec.JAR.capacity()).build();
    public static final SourceJarSpec LARGE = SourceJarSpec.LARGE.toBuilder().capacity(8 * SourceJarSpec.LARGE.capacity()).build();
    private static final String[] LARGE_KINDS = { "bottom", "mid", "top", "seam", "empty" };

    private LaurerilSourceJars() {}

    public static final class Legacy extends SourceJarTile {

        public Legacy(BlockPos pos, BlockState state) {
            this(GTOBlockEntities.SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY.get(), pos, state);
        }

        public Legacy(BlockEntityType<Legacy> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    public static final class Standard extends SourceJarTile {

        public Standard(BlockPos pos, BlockState state) {
            this(GTOBlockEntities.STANDARD_LAURERIL_SOURCE_JAR.get(), pos, state);
        }

        public Standard(BlockEntityType<Standard> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    public static final class Large extends LargeSourceJarTile {

        public Large(BlockPos pos, BlockState state) {
            this(GTOBlockEntities.LARGE_LAURERIL_SOURCE_JAR.get(), pos, state);
        }

        public Large(BlockEntityType<Large> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    private static BlockEntry<SourceJar> jar(String id, String cn, String en, SourceJarSpec spec, BiFunction<BlockPos, BlockState, ? extends SourceJarTile> factory,
                                             Supplier<? extends BlockEntityType<?>> type) {
        return block(id, cn, p -> new SourceJar(p, id, spec, factory))
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
        return jar("laureril_source_jar", "秘金魔源罐(旧)", "Laureril Source Jar (Old)", LEGACY, Legacy::new, () -> GTOBlockEntities.SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY.get());
    }

    public static BlockEntry<SourceJar> registerStandard() {
        return jar("standard_laureril_source_jar", "秘金魔源罐", "Laureril Source Jar", STANDARD, Standard::new, () -> GTOBlockEntities.STANDARD_LAURERIL_SOURCE_JAR.get());
    }

    public static BlockEntry<LargeSourceJar> registerLarge() {
        String id = "large_laureril_source_jar";
        return block(id, "秘金大型魔源罐", p -> new LargeSourceJar(p, LARGE, Large::new))
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
                .item(LargeSourceJarItem::new)
                .model((ctx, prov) -> prov.getBuilder(ctx.getName())
                        .parent(new ModelFile.UncheckedModelFile(RLUtils.ars("block/large_source_jar/item")))
                        .texture("jar", GTOCore.id("block/" + id))
                        .texture("particle", GTOCore.id("block/" + id)))
                .build()
                .register();
    }
}
