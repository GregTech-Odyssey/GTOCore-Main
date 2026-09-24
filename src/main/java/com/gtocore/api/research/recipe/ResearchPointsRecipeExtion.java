package com.gtocore.api.research.recipe;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchPoints;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;
import com.gtocore.utils.GuiHelper;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.recipesearch.IntLongMap;
import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;
import org.jetbrains.annotations.NotNull;

@DataGeneratorScanned
public class ResearchPointsRecipeExtion extends RecipeExtension<ResearchPoints> {

    public static final ResearchPointsRecipeExtion INSTANCE = new ResearchPointsRecipeExtion("research_points");

    public ResearchPointsRecipeExtion(String name) {
        super(name, GTOCodecs.RESEARCH_POINTS_SYNC_CODEC, false);
    }

    @Override
    public boolean handleOutput(@NotNull IRecipeHandlerHolder holder, @NotNull GTRecipe recipe, boolean simulate) {
        if (!simulate) {
            var points = recipe.data.getData(INSTANCE);
            if (points != null) {
                for (var it = points.reference2LongEntrySet().fastIterator(); it.hasNext();) {
                    var entry = it.next();
                    var researchTag = entry.getKey();
                    var amount = entry.getLongValue();
                    if (holder instanceof IResearchPointsOperation machine) {
                        machine.addResearchData(researchTag, amount);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {}

    @Override
    public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
        return 0;
    }

    @Override
    public void setParallel(GTRecipe recipe, long parallel) {}

    /** 配方页：一句"可获得的研究点数"，每种研究点一个展示槽（悬停看数量，可在 EMI 里查看）。 */
    @Override
    public void appendInfo(GTRecipeDefinition recipe, RecipeInfoBuilder info) {
        var points = recipe.data.getData(INSTANCE);
        if (points == null) return;
        info.sentence(() -> Component.translatable(RESEARCH_POINTS));
        for (var it = points.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            var researchTag = entry.getKey();
            var amount = entry.getLongValue();
            info.slot(() -> new ResearchTagSlot(new ResearchTagEmiStack(researchTag).setAmount(amount)));
        }
    }

    /** 研究点展示槽：标准物品槽底图，画研究点图标，悬停提示与 EMI 快捷键都作用于该研究点。 */
    private static final class ResearchTagSlot extends ImageWidget implements IIngredientSlot {

        private final EmiStack tag;

        private ResearchTagSlot(EmiStack tag) {
            super(0, 0, UISizes.SLOT, UISizes.SLOT, UITheme.ITEM_SLOT);
            this.tag = tag;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
            return isMouseOverElement(mouseX, mouseY) ? tag : null;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void drawTooltipTexts(int mouseX, int mouseY) {
            if (isMouseOverElement(mouseX, mouseY) && gui != null && gui.getModularUIGui() != null) {
                gui.getModularUIGui().setHoverTooltip(tag.getTooltipText(), ItemStack.EMPTY, null, null);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOverElement(mouseX, mouseY)) {
                return EmiScreenManager.stackInteraction(new EmiStackInteraction(tag, null, true), bind -> bind.matchesMouse(button));
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (isMouseOverElement(GuiHelper.getRealMouseX(), GuiHelper.getRealMouseY())) {
                return EmiScreenManager.stackInteraction(new EmiStackInteraction(tag, null, true), bind -> bind.matchesKey(keyCode, scanCode));
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            tag.render(graphics, getPositionX() + 1, getPositionY() + 1, partialTicks, 3);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (isMouseOverElement(mouseX, mouseY)) {
                graphics.fill(getPositionX() + 1, getPositionY() + 1, getPositionX() + 17, getPositionY() + 17, 0x80FFFFFF);
            }
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @RegisterLanguage(cn = "可获得的研究点数", en = "Research Points")
    public static String RESEARCH_POINTS = "gtocore.recipe.extension.research_points";
}
