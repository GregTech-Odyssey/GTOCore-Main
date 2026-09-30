package com.gtocore.mixin.appbot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import appbot.client.ManaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ManaRenderer.class, remap = false)
public abstract class ManaRendererMixin {

    @Unique
    private static final ResourceLocation gto$MANA_WATER = new ResourceLocation("botania", "block/mana_water");

    @Shadow
    private TextureAtlasSprite waterSprite;

    @Overwrite
    private void lazyInitSprite() {
        waterSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(gto$MANA_WATER);
    }
}
