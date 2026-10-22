package com.gtocore.client.renderer.item;

import com.gtocore.common.item.armor.HeatInsulationArmorItem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public final class HeatInsulationArmorRenderer implements IClientItemExtensions {

    private static final float RED = (HeatInsulationArmorItem.TEXTURE_TINT >> 16 & 255) / 255.0F;
    private static final float GREEN = (HeatInsulationArmorItem.TEXTURE_TINT >> 8 & 255) / 255.0F;
    private static final float BLUE = (HeatInsulationArmorItem.TEXTURE_TINT & 255) / 255.0F;

    private HumanoidModel<?> originalModel;
    private boolean tintBaseLayer;
    private final Model tintedModel = new Model(RenderType::entityCutoutNoCull) {

        @Override
        public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
            if (tintBaseLayer) {
                tintBaseLayer = false;
                originalModel.renderToBuffer(poseStack, consumer, light, overlay, red * RED, green * GREEN, blue * BLUE, alpha);
            } else {
                // Preserve the colors of subsequent armor trim and enchantment glint passes.
                originalModel.renderToBuffer(poseStack, consumer, light, overlay, red, green, blue, alpha);
            }
        }
    };

    @Override
    public @NotNull Model getGenericArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        originalModel = original;
        tintBaseLayer = true;
        return tintedModel;
    }
}
