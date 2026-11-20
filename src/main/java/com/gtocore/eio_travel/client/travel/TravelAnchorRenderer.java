package com.gtocore.eio_travel.client.travel;

import com.gtocore.eio_travel.api.ITravelTarget;
import com.gtocore.eio_travel.client.TravelRenderer;

import com.gtolib.api.client.BlockHighlightMarker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.mojang.blaze3d.vertex.PoseStack;

public interface TravelAnchorRenderer<T extends ITravelTarget> extends TravelRenderer<T> {

    @Override
    default void render(T travelData, LevelRenderer levelRenderer, PoseStack poseStack, double distanceSquared, boolean active) {
        var renderInfo = getRenderInfo(travelData);
        var minecraft = Minecraft.getInstance();
        var camera = minecraft.gameRenderer.getMainCamera();
        var icon = renderInfo.icon() == Items.AIR ? ItemStack.EMPTY : new ItemStack(renderInfo.icon());
        BlockHighlightMarker.render(minecraft, camera, poseStack, travelData.getPos(), distanceSquared, active, renderInfo.name().trim(), icon);
    }

    record RenderInfo(String name, Item icon) {}

    RenderInfo getRenderInfo(T travelData);
}
