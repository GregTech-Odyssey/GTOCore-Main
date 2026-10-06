package com.gtocore.mixin.mc.client;

import com.gtocore.client.renderer.fx.SolarStormFX;
import com.gtocore.client.renderer.item.ItemCountRenderer;
import com.gtocore.common.weather.WeatherSystem;
import com.gtocore.common.weather.WeatherTypes;
import com.gtocore.utils.StxckUtil;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Unique
    private Biome.Precipitation gto$precipitation;

    @ModifyExpressionValue(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;hasPrecipitation()Z"))
    private boolean gto$planetHasPrecipitation(boolean original) {
        return gto$precipitation != null || original;
    }

    @ModifyExpressionValue(method = { "renderSnowAndRain", "tickRain" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    private Biome.Precipitation gto$planetPrecipitation(Biome.Precipitation original) {
        return gto$precipitation == null ? original : gto$precipitation;
    }

    @Inject(method = { "renderSnowAndRain", "tickRain" }, at = @At("HEAD"), cancellable = true)
    private void replaceSolarRain(CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        gto$precipitation = null;
        if (level == null) return;
        var weather = ILevel.getCapability(level, WeatherSystem.CLIENT_WEATHER);
        if (weather == WeatherTypes.SNOW) gto$precipitation = Biome.Precipitation.SNOW;
        else if (weather == WeatherTypes.ACID_RAIN || weather == WeatherTypes.METHANE_RAIN ||
                (weather == WeatherTypes.THUNDER && level.dimension() != Level.OVERWORLD))
            gto$precipitation = Biome.Precipitation.RAIN;
        if (SolarStormFX.isSolarSurface(level)) ci.cancel();
    }

    @ModifyExpressionValue(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"))
    private float preserveSolarSkyBrightness(float rain) {
        return SolarStormFX.isSolarSurface(Minecraft.getInstance().level) ? 0.0F : rain;
    }

    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void renderItemCount(Entity entity, double x, double y, double z, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        var entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (entityRenderDispatcher.camera == null) return;
        if (entity instanceof ItemEntity itemEntity) {
            var maxDistance = StxckUtil.getMinItemCountRenderDistance();
            if (entityRenderDispatcher.distanceToSqr(entity) > maxDistance * maxDistance) return;
            var offset = entityRenderDispatcher.getRenderer(entity).getRenderOffset(entity, partialTicks);
            var light = entityRenderDispatcher.getPackedLightCoords(entity, partialTicks);
            var nx = Mth.lerp(partialTicks, entity.xOld, entity.getX()) - x + offset.x();
            var ny = Mth.lerp(partialTicks, entity.yOld, entity.getY()) - y + offset.y();
            var nz = Mth.lerp(partialTicks, entity.zOld, entity.getZ()) - z + offset.z();
            poseStack.pushPose();
            poseStack.translate(nx, ny, nz);
            ItemCountRenderer.renderItemCount(itemEntity, poseStack, bufferSource, light, entityRenderDispatcher);
            poseStack.popPose();
        }
    }
}
