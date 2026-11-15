package com.gtocore.client;

import com.gtocore.integration.Mods;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.quest.client.book.QuestBookScreen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.ui.CustomClickEvent;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class QuestBookEntries {

    @RegisterLanguage(cn = "打开 GTO 任务书", en = "Open GTO Quest Book")
    static final String KEY_NAME = "key.gtocore.open_quest_book";
    @RegisterLanguage(cn = "GTO 任务书", en = "GTO Quest Book")
    static final String BUTTON_NAME = "sidebar_button.gtocore.quest_book";
    @RegisterLanguage(cn = "本任务书处于测试阶段", en = "This quest book is in testing")
    static final String TIP_TESTING = "gtocore.quest_book.sidebar.testing";
    @RegisterLanguage(cn = "若出现异常，请改用原任务书", en = "If issues occur, use the original quest book instead")
    static final String TIP_FALLBACK = "gtocore.quest_book.sidebar.fallback";

    public static final KeyMapping KEY = new KeyMapping(KEY_NAME,
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.keybinding.gtocore");

    private static final ResourceLocation BUTTON = GTOCore.id("quest_book");
    private static final ResourceLocation CLICK = GTOCore.id("open_quest_book");

    private QuestBookEntries() {}

    static void init() {
        KeyMappingRegistry.register(KEY);
        if (Mods.FTBLIBRARY.isLoaded()) Sidebar.init();
    }

    public static void tick(Minecraft mc) {
        while (KEY.consumeClick()) {
            if (mc.screen == null && mc.player != null) QuestBookScreen.open(-1);
        }
    }

    private static final class Sidebar {

        private static void init() {
            CustomClickEvent.EVENT.register(event -> {
                if (!CLICK.equals(event.id())) return EventResult.pass();
                QuestBookScreen.open(-1);
                return EventResult.interruptFalse();
            });
            SidebarButtonCreatedEvent.EVENT.register(event -> {
                var button = event.getButton();
                if (!BUTTON.equals(button.getId())) return;
                button.setTooltipHandler(lines -> {
                    lines.add(I18n.get(TIP_TESTING));
                    lines.add(I18n.get(TIP_FALLBACK));
                });
            });
        }
    }
}
