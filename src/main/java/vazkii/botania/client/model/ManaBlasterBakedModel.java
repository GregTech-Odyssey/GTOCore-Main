package vazkii.botania.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.api.mana.ControlLensItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 魔力脉冲枪的模型：按是否装弹夹、装了哪块透镜选择对应模型，并把透镜叠在枪身上。
 */
public class ManaBlasterBakedModel implements BakedModel {

    private final BakedModel originalModel;

    private static final String TAG_LENS = "lens";
    private static final String TAG_CLIP = "clip";
    private static final String TAG_CLIP_POS = "clipPos";
    private static final int CLIP_SLOTS = 6;

    // key 是（透镜物品, 是否装弹夹）；没有透镜时物品为 null
    private final Map<Pair<Item, Boolean>, BakedModel> models;

    private ManaBlasterBakedModel(Map<Pair<Item, Boolean>, BakedModel> models) {
        this.originalModel = models.get(Pair.of(null, false));
        this.models = models;
    }

    public static ManaBlasterBakedModel create(ModelBaker baker, ResourceLocation gunNoClip, ResourceLocation gunClip,
                                               ModelState state) {
        BakedModel gunNoClipModel = Preconditions.checkNotNull(baker.bake(gunNoClip, state));
        BakedModel gunClipModel = Preconditions.checkNotNull(baker.bake(gunClip, state));
        Map<Pair<Item, Boolean>, BakedModel> models = new HashMap<>();
        models.put(Pair.of(null, false), gunNoClipModel);
        models.put(Pair.of(null, true), gunClipModel);

        for (var item : BuiltInRegistries.ITEM) {
            var lens = item.getDefaultInstance();
            if (isValidLens(lens)) {
                models.put(Pair.of(item, false), new CompositeBakedModel(baker, lens, gunNoClipModel));
                models.put(Pair.of(item, true), new CompositeBakedModel(baker, lens, gunClipModel));
            }
        }
        return new ManaBlasterBakedModel(models);
    }

    public static boolean hasClip(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean(TAG_CLIP);
    }

    @Nullable
    private static CompoundTag getTag(ItemStack stack) {
        return stack.getTag();
    }

    public static ItemStack getLens(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        if (hasClip(stack)) {
            int pos = Math.max(0, Math.min(CLIP_SLOTS - 1, tag.getInt(TAG_CLIP_POS)));
            CompoundTag cmp = tag.getCompound(TAG_LENS + pos);
            return cmp.isEmpty() ? ItemStack.EMPTY : ItemStack.of(cmp);
        }
        CompoundTag cmp = tag.getCompound(TAG_LENS);
        return cmp.isEmpty() ? ItemStack.EMPTY : ItemStack.of(cmp);
    }

    public static boolean isValidLens(ItemStack lens) {
        var item = lens.getItem();
        if (!(item instanceof BasicLensItem)) {
            return false;
        }
        return !(item instanceof ControlLensItem control && control.isControlLens(lens));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return originalModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return originalModel.isCustomRenderer();
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return originalModel.getParticleIcon();
    }

    @NotNull
    @Override
    public ItemTransforms getTransforms() {
        return originalModel.getTransforms();
    }

    private final ItemOverrides itemHandler = new ItemOverrides() {

        @NotNull
        @Override
        public BakedModel resolve(@NotNull BakedModel model, @NotNull ItemStack stack, @Nullable ClientLevel worldIn,
                                  @Nullable LivingEntity entityIn, int seed) {
            ItemStack lens = getLens(stack);
            Pair<Item, Boolean> key = Pair.of(lens.isEmpty() ? null : lens.getItem(), hasClip(stack));
            return models.getOrDefault(key, Minecraft.getInstance().getModelManager().getMissingModel());
        }
    };

    @NotNull
    @Override
    public ItemOverrides getOverrides() {
        return itemHandler;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return originalModel.getQuads(state, side, rand);
    }

    private static class CompositeBakedModel implements BakedModel {

        private final BakedModel originalModel;
        private final BakedModel lensModel;

        CompositeBakedModel(ModelBaker baker, ItemStack lens, BakedModel gun) {
            this.originalModel = gun;
            ResourceLocation lensId = BuiltInRegistries.ITEM.getKey(lens.getItem());
            ModelState transform = new ModelState() {

                @Override
                public Transformation getRotation() {
                    return new Transformation(new Vector3f(-0.4F, 0.2F, 0.0F), com.mojang.math.Axis.YP.rotationDegrees(90),
                            new Vector3f(0.625F, 0.625F, 0.625F), null);
                }
            };
            this.lensModel = baker.bake(new ModelResourceLocation(lensId, "inventory"), transform);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return originalModel.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return originalModel.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return originalModel.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return originalModel.isCustomRenderer();
        }

        @NotNull
        @Override
        public TextureAtlasSprite getParticleIcon() {
            return originalModel.getParticleIcon();
        }

        @NotNull
        @Override
        public ItemTransforms getTransforms() {
            return originalModel.getTransforms();
        }

        @NotNull
        @Override
        public ItemOverrides getOverrides() {
            return this.originalModel.getOverrides();
        }

        @NotNull
        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction face, @NotNull RandomSource rand) {
            List<BakedQuad> gunQuads = this.originalModel.getQuads(state, face, rand);
            List<BakedQuad> lensQuads = this.lensModel.getQuads(state, face, rand);
            List<BakedQuad> ret = new ArrayList<>(gunQuads.size() + lensQuads.size());
            ret.addAll(gunQuads);
            ret.addAll(lensQuads);
            return ret;
        }
    }
}
