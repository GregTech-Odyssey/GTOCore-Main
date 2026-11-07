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
import vazkii.botania.common.entity.GaiaGuardianIIIEntity;

/** 盖亚守护者 III 渲染：复用玩家模型与噪点着色器。 */
public class GaiaGuardianIIIRenderer
                                     extends HumanoidMobRenderer<GaiaGuardianIIIEntity, HumanoidModel<GaiaGuardianIIIEntity>> {

    public GaiaGuardianIIIRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        ArmorModels.init(context);
    }

    @Override
    public void render(@NotNull GaiaGuardianIIIEntity entity, float yaw, float partialTicks, PoseStack ms,
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
    public ResourceLocation getTextureLocation(@NotNull GaiaGuardianIIIEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() instanceof AbstractClientPlayer player) {
            return player.getSkinTextureLocation();
        }
        return DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
    }

    @Override
    protected boolean isBodyVisible(@NotNull GaiaGuardianIIIEntity entity) {
        return true;
    }

    private static class Model extends HumanoidModel<GaiaGuardianIIIEntity> {

        Model(ModelPart root) {
            super(root, BotaniaShaders::getDopplegangerLayer);
        }
    }
}
