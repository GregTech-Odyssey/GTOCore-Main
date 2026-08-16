package com.gtocore.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Set;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX;

public class TextureUpdateRequester {

    private final Set<BlockState> states = new ReferenceOpenHashSet<>();

    public void add(Block block) {
        add(block.defaultBlockState());
    }

    public void add(BlockState state) {
        states.add(state);
    }

    public void requestUpdate() {
        PoseStack poseStack = new PoseStack();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, POSITION_TEX);
        poseStack.pushPose();
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();
        for (BlockState state : states) {
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            renderer.renderModelLists(model, state.getBlock().asItem().getDefaultInstance(), LightTexture.FULL_BLOCK, OverlayTexture.NO_OVERLAY, poseStack, bufferBuilder);
        }
        bufferBuilder.end();
        bufferBuilder.discard();
        poseStack.popPose();
    }
}
