package com.gtocore.client.renderer.entity;

import com.gtocore.client.renderer.GTORenderTypes;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.api.systems.TemperatureApi;

/**
 * 极寒维度（Ad Astra 温度低于 -50°C）中耐寒生物身上叠加的霜纹层。
 */
@OnlyIn(Dist.CLIENT)
public final class FrostCoatLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final TagKey<EntityType<?>> COLD_SURVIVORS = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("ad_astra", "can_survive_extreme_cold"));
    private static final short COLD_THRESHOLD = -50;

    private static Level cachedLevel;
    private static boolean cachedCold;

    private FrostCoatLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            LivingEntityRenderer renderer;
            try {
                renderer = event.getRenderer((EntityType) type);
            } catch (ClassCastException e) {
                continue;
            }
            if (renderer != null) renderer.addLayer(new FrostCoatLayer<>(renderer));
        }
    }

    private static boolean isColdLevel(Level level) {
        if (level != cachedLevel) {
            cachedLevel = level;
            cachedCold = TemperatureApi.API.getTemperature(level) < COLD_THRESHOLD;
        }
        return cachedCold;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || !isColdLevel(entity.level()) || !entity.getType().is(COLD_SURVIVORS)) return;
        var model = getParentModel();
        boolean enderman = entity.getType() == EntityType.ENDERMAN;
        model.renderToBuffer(poseStack, buffer.getBuffer(enderman ? GTORenderTypes.FROST_COAT_ENDERMAN : GTORenderTypes.FROST_COAT), packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
        float wave = Mth.sin((ageInTicks + entity.getId() * 13) * 0.09F);
        float a = 0.2F + 0.8F * Math.max(0, wave);
        float b = 0.2F + 0.8F * Math.max(0, -wave);
        model.renderToBuffer(poseStack, buffer.getBuffer(enderman ? GTORenderTypes.FROST_GLINT_ENDERMAN_A : GTORenderTypes.FROST_GLINT_A), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, a, a, a, 1);
        model.renderToBuffer(poseStack, buffer.getBuffer(enderman ? GTORenderTypes.FROST_GLINT_ENDERMAN_B : GTORenderTypes.FROST_GLINT_B), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, b, b, b, 1);
    }
}
