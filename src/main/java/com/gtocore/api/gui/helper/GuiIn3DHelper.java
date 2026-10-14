package com.gtocore.api.gui.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
public final class GuiIn3DHelper {

    public static void renderIn3D(PoseStack poseStack, BiConsumer<GuiGraphics, PoseStack> renderFunction) {
        if (renderFunction == null) {
            return;
        }
        var guiGraphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        var guiPose = guiGraphics.pose();

        guiPose.pushPose();
        guiPose.mulPoseMatrix(poseStack.last().pose());
        renderFunction.accept(guiGraphics, guiPose);
        guiPose.popPose();
    }
}
