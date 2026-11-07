package vazkii.botania.forge.client;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.model.ManaBlasterBakedModel;

import java.util.function.Function;

/**
 * 魔力脉冲枪模型加载器：从模型 JSON 读取无弹夹/带弹夹两个底模。
 */
public class ForgeManaBlasterModel implements IUnbakedGeometry<ForgeManaBlasterModel> {

    private final ResourceLocation gunNoClip, gunClip;

    public ForgeManaBlasterModel(ResourceLocation gunNoClip, ResourceLocation gunClip) {
        this.gunNoClip = gunNoClip;
        this.gunClip = gunClip;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        modelGetter.apply(this.gunNoClip).resolveParents(modelGetter);
        modelGetter.apply(this.gunClip).resolveParents(modelGetter);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        Transformation transform = context.getRootTransform();
        ModelState state = new ModelState() {

            @NotNull
            @Override
            public Transformation getRotation() {
                return transform;
            }
        };
        return ManaBlasterBakedModel.create(baker, this.gunNoClip, this.gunClip, state);
    }

    public enum Loader implements IGeometryLoader<ForgeManaBlasterModel> {

        INSTANCE;

        @NotNull
        @Override
        public ForgeManaBlasterModel read(JsonObject json, JsonDeserializationContext deserializationContext) {
            return new ForgeManaBlasterModel(
                    new ResourceLocation(GsonHelper.getAsString(json, "gun_noclip")),
                    new ResourceLocation(GsonHelper.getAsString(json, "gun_clip")));
        }
    }
}
