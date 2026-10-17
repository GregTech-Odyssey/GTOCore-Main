package com.gtocore.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;

import java.util.ArrayList;
import java.util.List;

/** Resolves server-sent quest IDs with the client's localized FTB Quests data. */
@OnlyIn(Dist.CLIENT)
public final class QuestTradeClientIntegration {

    private static final String QUEST_ID_KEY = "gtocore.trade.quest_id";

    private QuestTradeClientIntegration() {}

    public static ArrayList<Component> resolveQuestTitles(List<Component> lines) {
        ArrayList<Component> resolved = new ArrayList<>(lines.size());
        for (Component line : lines) {
            resolved.add(resolveQuestTitles(line));
        }
        return resolved;
    }

    private static MutableComponent resolveQuestTitles(Component component) {
        MutableComponent resolved;
        if (component.getContents() instanceof TranslatableContents translatable) {
            resolved = resolveQuestTitle(translatable);
            if (resolved == null) {
                Object[] arguments = translatable.getArgs().clone();
                for (int i = 0; i < arguments.length; i++) {
                    if (arguments[i] instanceof Component argument) {
                        arguments[i] = resolveQuestTitles(argument);
                    }
                }
                resolved = translatable.getFallback() == null ? Component.translatable(translatable.getKey(), arguments) :
                        Component.translatableWithFallback(translatable.getKey(), translatable.getFallback(), arguments);
            }
        } else {
            resolved = component.plainCopy();
        }

        resolved.withStyle(component.getStyle());
        for (Component sibling : component.getSiblings()) {
            resolved.append(resolveQuestTitles(sibling));
        }
        return resolved;
    }

    private static MutableComponent resolveQuestTitle(TranslatableContents translatable) {
        if (!QUEST_ID_KEY.equals(translatable.getKey()) || translatable.getArgs().length != 1 || ClientQuestFile.INSTANCE == null) {
            return null;
        }
        try {
            long questId = Long.parseUnsignedLong(String.valueOf(translatable.getArgs()[0]), 16);
            var quest = ClientQuestFile.INSTANCE.getQuest(questId);
            if (quest != null) {
                Component title = quest.getTitle();
                if (!title.getString().isBlank()) return title.copy();
            }
        } catch (NumberFormatException ignored) {
            // Leave malformed or unrelated quest-ID components unchanged.
        }
        return null;
    }
}
