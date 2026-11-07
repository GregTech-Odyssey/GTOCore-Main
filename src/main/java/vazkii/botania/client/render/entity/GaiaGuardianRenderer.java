package vazkii.botania.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.BotaniaShaders;
import vazkii.botania.client.model.armor.ArmorModels;
import vazkii.botania.common.entity.GaiaGuardianEntity;

/**
 * 盖亚守护者渲染器。
 *
 * <p>
 * 与原模组一致：使用玩家模型与自定义噪点着色器，生成保护期间噪点更强、形态更破碎；
 * 纹理取当前视角玩家的皮肤，没有视角玩家时用默认皮肤。
 * </p>
 */
public class GaiaGuardianRenderer extends HumanoidMobRenderer<GaiaGuardianEntity, HumanoidModel<GaiaGuardianEntity>> {

    public GaiaGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        ArmorModels.init(context);
    }

    @Override
    public void render(@NotNull GaiaGuardianEntity entity, float yaw, float partialTicks, PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light) {
        ShaderInstance shader = BotaniaShaders.doppleganger();
        if (shader != null) {
            int invulTime = entity.getInvulTime();
            float grainIntensity;
            float disfiguration;
            if (invulTime > 0) {
                grainIntensity = invulTime > 20 ? 1F : invulTime * 0.05F;
                disfiguration = grainIntensity * 0.3F;
            } else {
                disfiguration = (0.025F + entity.hurtTime * ((1F - 0.15F) / 20F)) / 2F;
                grainIntensity = 0.05F + entity.hurtTime * ((1F - 0.15F) / 10F);
            }
            shader.safeGetUniform("BotaniaGrainIntensity").set(grainIntensity);
            shader.safeGetUniform("BotaniaDisfiguration").set(disfiguration);
        }
        super.render(entity, yaw, partialTicks, ms, buffers, light);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull GaiaGuardianEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() instanceof AbstractClientPlayer player) {
            return player.getSkinTextureLocation();
        }
        return DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
    }

    @Override
    protected boolean isBodyVisible(@NotNull GaiaGuardianEntity entity) {
        return true;
    }

    /** 用盖亚噪点层渲染的玩家模型。 */
    private static class Model extends HumanoidModel<GaiaGuardianEntity> {

        Model(ModelPart root) {
            super(root, BotaniaShaders::getDopplegangerLayer);
        }
    }
}
