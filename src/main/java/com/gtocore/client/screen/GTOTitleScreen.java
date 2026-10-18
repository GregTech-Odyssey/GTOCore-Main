package com.gtocore.client.screen;

import com.gtolib.GTOCore;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.internal.BrandingControl;

import com.mojang.blaze3d.systems.RenderSystem;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

/** The title_screen_layout design, using bundled textures and vanilla menu actions. */
@OnlyIn(Dist.CLIENT)
public final class GTOTitleScreen extends TitleScreen {

    private static final ResourceLocation BACKGROUND = GTOCore.id("textures/gui/title/background.png");
    private static final ResourceLocation TITLE = GTOCore.id("textures/gui/title/title.png");
    private static final ResourceLocation LOGO = GTOCore.id("textures/gui/title/logo.png");
    private static final ResourceLocation BUTTON = GTOCore.id("textures/gui/title/button.png");
    private static final ResourceLocation BUTTON_HOVERED = GTOCore.id("textures/gui/title/button_hovered.png");
    private static final String WIKI_URL = "https://gtodyssey.com";
    private static final String LAUNCH_TIME = "Launch took " + ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + "s";
    private static final int BACKGROUND_TEXTURE_WIDTH = 1920;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 1080;
    // Vertical center of the planet in the bundled background, in source-image pixels.
    private static final int BACKGROUND_PLANET_CENTER_Y = 350;
    private static final int MENU_OFFSET_Y = 24;
    private final ArrayList<BrandingLine> branding = new ArrayList<>(3);
    private int titleX;
    private int titleY;
    private int titleWidth;
    private int titleHeight;
    private int backgroundWidth;
    private int backgroundHeight;

    @Override
    protected void init() {
        // Load menu textures while the loading overlay is still opaque, before its crossfade begins.
        var textures = minecraft.getTextureManager();
        textures.getTexture(BACKGROUND);
        textures.getTexture(TITLE);
        textures.getTexture(LOGO);
        textures.getTexture(BUTTON);
        textures.getTexture(BUTTON_HOVERED);
        // Keep the original callbacks, including demo mode and multiplayer restrictions/tooltips.
        super.init();
        Button[] menu = new Button[5];
        Button copyright = null;
        for (var child : children()) {
            if (!(child instanceof Button button)) continue;
            if (button.getMessage().equals(COPYRIGHT_TEXT)) {
                copyright = button;
            } else if (button.getMessage().getContents() instanceof TranslatableContents message) {
                int index = switch (message.getKey()) {
                    case "menu.singleplayer", "menu.playdemo" -> 0;
                    case "menu.multiplayer", "menu.resetdemo" -> 1;
                    case "fml.menu.mods" -> 2;
                    case "menu.options" -> 3;
                    case "menu.quit" -> 4;
                    default -> -1;
                };
                if (index >= 0) menu[index] = button;
            }
        }
        clearWidgets();

        float backgroundScale = Math.max((float) width / BACKGROUND_TEXTURE_WIDTH, (float) height / BACKGROUND_TEXTURE_HEIGHT);
        backgroundWidth = (int) Math.ceil(BACKGROUND_TEXTURE_WIDTH * backgroundScale);
        backgroundHeight = (int) Math.ceil(BACKGROUND_TEXTURE_HEIGHT * backgroundScale);
        int logoSize = Math.max(1, Math.min((int) (height / 4.5), width / 8));
        int headerCenter = Math.round((height - backgroundHeight) / 2 + (float) BACKGROUND_PLANET_CENTER_Y * backgroundHeight / BACKGROUND_TEXTURE_HEIGHT);
        int menuAnchorY = Math.min(width / 10, height / 4) - logoSize / 2;
        int logoX = width / 8;
        int logoY = headerCenter - logoSize / 2;
        titleHeight = logoSize;
        titleWidth = logoSize * 1024 / 355;
        titleX = (width - titleWidth) / 2;
        titleY = headerCenter - titleHeight / 2;
        branding.clear();
        branding.add(new BrandingLine(LAUNCH_TIME, 2, height - 10));
        branding.add(new BrandingLine(ForgeI18n.parseMessage("fml.menu.loadingmods", ModList.get().size()), 2, height - 10 - (font.lineHeight + 1)));
        BrandingControl.forEachAboveCopyrightLine((line, text) -> branding.add(new BrandingLine(text, width - font.width(text), height - (10 + (line + 1) * (font.lineHeight + 1)))));

        var logo = new TextureButton(logoX, logoY, logoSize, logoSize,
                Component.literal("gtodyssey.com"), GTOTitleScreen::openWiki, LOGO, LOGO, false);
        logo.setTooltip(Tooltip.create(Component.literal(WIKI_URL)));
        addRenderableWidget(logo);

        int buttonWidth = Math.min(137, width);
        int buttonHeight = Math.clamp((height - menuAnchorY - logoSize - 24) / 5, 1, 20);
        int buttonX = Math.max(0, Math.min(width - buttonWidth, logoX + (logoSize - buttonWidth) / 2));
        // The source layout anchors the quit button's bottom edge 53 GUI pixels above the bottom.
        int quitY = Math.max(menuAnchorY + logoSize, height - 53 - buttonHeight);
        quitY = Math.min(quitY, height - buttonHeight - 22);
        for (int i = 0; i < menu.length; i++) {
            Button original = menu[i];
            if (original == null) continue;
            int y = Math.min(menuAnchorY + (quitY - menuAnchorY) * (8 + i * 2) / 16, quitY - buttonHeight * (4 - i));
            var button = new TextureButton(buttonX, y + MENU_OFFSET_Y, buttonWidth, buttonHeight,
                    original.getMessage(), pressed -> original.onPress(), BUTTON, BUTTON_HOVERED, true);
            button.active = original.active;
            button.setTooltip(original.getTooltip());
            addRenderableWidget(button);
        }
        if (copyright != null) addRenderableWidget(copyright);
    }

    private static void openWiki(Button button) {
        Util.getPlatform().openUri(WIKI_URL);
    }

    @Override
    public void tick() {
        // This layout hides Realms, so do not start its notification overlay or warning screens.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Dispatch only to visible menu widgets, bypassing TitleScreen's hidden Realms overlay.
        for (var child : children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                setFocused(child);
                if (button == 0) setDragging(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TITLE, titleX, titleY, titleWidth, titleHeight, 0, 0, 4042, 1488, 4042, 1488);
        for (net.minecraft.client.gui.components.Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        RenderSystem.disableBlend();
        for (BrandingLine line : branding) {
            graphics.drawString(font, line.text(), line.x(), line.y(), 0xFFFFFF);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        // ForgeLoadingOverlay supplies its own fading shader alpha; only the overlay should crossfade.
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        // Cover the window without stretching the 16:9 background.
        graphics.blit(BACKGROUND, (width - backgroundWidth) / 2, (height - backgroundHeight) / 2,
                backgroundWidth, backgroundHeight, 0, 0, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
    }

    private record BrandingLine(String text, int x, int y) {}

    private static final class TextureButton extends Button {

        private final ResourceLocation normal;
        private final ResourceLocation hovered;
        private final boolean drawLabel;

        private TextureButton(int x, int y, int width, int height, Component message, OnPress onPress,
                              ResourceLocation normal, ResourceLocation hovered, boolean drawLabel) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.normal = normal;
            this.hovered = hovered;
            this.drawLabel = drawLabel;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            // The complete source texture is stretched, matching FancyMenu's non-nine-slice mode.
            graphics.blit(active && isHoveredOrFocused() ? hovered : normal, getX(), getY(), width, height, 0, 0, 1, 1, 1, 1);
            if (drawLabel) {
                renderString(graphics, Minecraft.getInstance().font, active ? 0xFFFFFF : 0xA0A0A0);
            }
        }
    }
}
