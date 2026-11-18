package com.gtocore.integration.ftbquests.emi;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.ftb.mods.ftbxmodcompat.ftbquests.recipemod_common.WrappedQuest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public final class QuestEmiRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 144;
    private static final int SLOT_TOP = 18;
    private static final int MAX_SLOTS = 9;

    private final WrappedQuest quest;
    private final List<EmiIngredient> outputSlots;

    public QuestEmiRecipe(WrappedQuest quest) {
        super(QuestEmiPlugin.QUEST, new ResourceLocation("ftbquests", "/quest/" + Long.toHexString(quest.quest.id)), WIDTH, 74);
        this.quest = quest;
        inputs = group(quest.input);
        outputSlots = group(quest.output);
        for (EmiIngredient slot : outputSlots) {
            outputs.addAll(slot.getEmiStacks());
        }
    }

    private static List<EmiIngredient> group(List<List<ItemStack>> groups) {
        List<EmiIngredient> list = new ObjectArrayList<>(Math.min(groups.size(), MAX_SLOTS));
        for (List<ItemStack> group : groups) {
            if (list.size() == MAX_SLOTS) break;
            List<EmiStack> stacks = new ObjectArrayList<>(group.size());
            for (ItemStack stack : group) {
                if (!stack.isEmpty()) stacks.add(EmiStack.of(stack));
            }
            if (!stacks.isEmpty()) list.add(EmiIngredient.of(stacks));
        }
        return list;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new TitleWidget(quest));
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), i % 3 * 18, SLOT_TOP + i / 3 * 18);
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, SLOT_TOP + 19);
        for (int i = 0; i < outputSlots.size(); i++) {
            widgets.addSlot(outputSlots.get(i), 90 + i % 3 * 18, SLOT_TOP + i / 3 * 18).recipeContext(this);
        }
    }

    private static final class TitleWidget extends Widget {

        private final WrappedQuest quest;
        private final FormattedCharSequence text;
        private final int x;

        private TitleWidget(WrappedQuest quest) {
            this.quest = quest;
            Font font = Minecraft.getInstance().font;
            Component title = quest.quest.getTitle().copy().withStyle(ChatFormatting.UNDERLINE);
            if (font.width(title) > WIDTH) {
                FormattedText cut = font.substrByWidth(title, WIDTH - font.width("..."));
                this.text = Language.getInstance().getVisualOrder(FormattedText.composite(cut, FormattedText.of("...")));
            } else {
                this.text = title.getVisualOrderText();
            }
            this.x = (WIDTH - font.width(text)) / 2;
        }

        @Override
        public Bounds getBounds() {
            return new Bounds(0, 0, WIDTH, 12);
        }

        @Override
        public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
            draw.drawString(Minecraft.getInstance().font, text, x, 2, 0xFF404040, false);
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (button != 0) return false;
            quest.openQuestGui();
            return true;
        }
    }
}
