package vazkii.botania.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 悬浮花模型。
 *
 * <p>
 * 与原模组一致：把「花」按 0.5 缩放并上移 0.2，与浮空岛模型合并烘焙；物品侧按两个渲染层分别绘制。
 * 由于复刻方块没有浮空花方块实体，岛类型固定为草岛。
 * </p>
 */
public class FloatingFlowerGeometry implements IUnbakedGeometry<FloatingFlowerGeometry> {

    private static final ResourceLocation ISLAND = new ResourceLocation("botania", "block/islands/island_grass");
    private static final Transformation FLOWER_TRANSFORM = new Transformation(
            new Vector3f(0F, 0.2F, 0F), null, new Vector3f(0.5F, 0.5F, 0.5F), null);

    private final UnbakedModel flower;
    @Nullable
    private UnbakedModel island;

    public FloatingFlowerGeometry(UnbakedModel flower) {
        this.flower = flower;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        flower.resolveParents(modelGetter);
        island = modelGetter.apply(ISLAND);
        if (island != null) {
            island.resolveParents(modelGetter);
        }
    }

    @Nullable
    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides,
                           ResourceLocation name) {
        Transformation rotation = FLOWER_TRANSFORM.compose(transform.getRotation());
        ModelState flowerState = new ModelState() {

            @Override
            public Transformation getRotation() {
                return rotation;
            }

            @Override
            public boolean isUvLocked() {
                return transform.isUvLocked();
            }
        };
        UnbakedModel islandModel = island != null ? island : baker.getModel(ISLAND);
        BakedModel bakedFlower = flower.bake(baker, spriteGetter, flowerState, name);
        BakedModel bakedIsland = islandModel == null ? null : islandModel.bake(baker, spriteGetter, transform, name);
        if (bakedFlower == null || bakedIsland == null) {
            return null;
        }
        return new Baked(bakedFlower, bakedIsland);
    }

    /** 合并后的模型：花与岛各自保留自己的渲染类型。 */
    public static class Baked implements BakedModel {

        private final BakedModel flower;
        private final BakedModel island;

        Baked(BakedModel flower, BakedModel island) {
            this.flower = flower;
            this.island = island;
        }

        @NotNull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable net.minecraft.core.Direction side,
                                        @NotNull RandomSource rand) {
            return getQuads(state, side, rand, ModelData.EMPTY, null);
        }

        @NotNull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable net.minecraft.core.Direction side,
                                        @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
            List<BakedQuad> flowerQuads = flower.getQuads(state, side, rand, ModelData.EMPTY, renderType);
            List<BakedQuad> islandQuads = island.getQuads(state, side, rand, ModelData.EMPTY, renderType);
            List<BakedQuad> merged = new ArrayList<>(flowerQuads.size() + islandQuads.size());
            merged.addAll(flowerQuads);
            merged.addAll(islandQuads);
            return merged;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return flower.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return flower.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return flower.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @NotNull
        @Override
        public TextureAtlasSprite getParticleIcon() {
            return flower.getParticleIcon();
        }

        @NotNull
        @Override
        public ItemOverrides getOverrides() {
            return flower.getOverrides();
        }

        @NotNull
        @Override
        public ItemTransforms getTransforms() {
            return flower.getTransforms();
        }

        @NotNull
        @Override
        public List<BakedModel> getRenderPasses(@NotNull ItemStack stack, boolean fabulous) {
            return List.of(flower, island);
        }

        @NotNull
        @Override
        public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                                 @NotNull ModelData data) {
            return ChunkRenderTypeSet.union(
                    BakedModel.super.getRenderTypes(state, rand, data),
                    flower.getRenderTypes(state, rand, data),
                    island.getRenderTypes(state, rand, data));
        }
    }

    /** 几何加载器入口，注册名 {@code floating_flower}。 */
    public enum Loader implements IGeometryLoader<FloatingFlowerGeometry> {

        INSTANCE;

        @NotNull
        @Override
        public FloatingFlowerGeometry read(@NotNull JsonObject json, @NotNull JsonDeserializationContext context) {
            BlockModel flower = context.deserialize(json.getAsJsonObject("flower"), BlockModel.class);
            return new FloatingFlowerGeometry(flower);
        }
    }
}
