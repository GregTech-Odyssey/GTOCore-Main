package com.gtocore.api.research.recipe;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchPoints;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.fast.recipesearch.IntLongMap;
import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public class ResearchPointsRecipeExtion extends RecipeExtension<ResearchPoints> {

    public static final ResearchPointsRecipeExtion INSTANCE = new ResearchPointsRecipeExtion("research_points");

    public ResearchPointsRecipeExtion(String name) {
        super(name, GTOCodecs.RESEARCH_POINTS_SYNC_CODEC, false);
    }

    @Override
    public boolean handle(IO io, @NotNull IRecipeHandlerHolder holder, @Nullable RecipeHandlerUnit unit, @NotNull GTRecipe recipe, boolean simulate) {
        if (io == IO.OUT && !simulate) {
            var points = recipe.data.getData(INSTANCE);
            if (points != null) {
                for (var entry : points.reference2LongEntrySet()) {
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

    @Override
    public void addInfo(GTRecipeDefinition recipe, WidgetGroup group, int xOffset, MutableInt yOffset) {
        var points = recipe.data.getData(INSTANCE);
        if (points != null) {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10), Component.translatable(RESEARCH_POINTS)));
            yOffset.add(10);
            for (var entry : points.reference2LongEntrySet()) {
                var researchTag = entry.getKey();
                var amount = entry.getLongValue();
                int finalXOffset = 3 - xOffset;
                var tag = new ResearchTagEmiStack(researchTag).setAmount(amount);
                class ResearchTagDisplay extends ImageWidget implements IIngredientSlot {

                    ResearchTagDisplay() {
                        super(finalXOffset, yOffset.getValue(), 18, 18, GuiTextures.SLOT);
                    }

                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public Object getXEIIngredientOverMouse(double v, double v1) {
                        if (isMouseOverElement(v, v1)) {
                            return tag;
                        }
                        return null;
                    }

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
                            return EmiScreenManager.stackInteraction(new EmiStackInteraction(tag, null, true),
                                    bind -> bind.matchesMouse(button));
                        }
                        return super.mouseClicked(mouseX, mouseY, button);
                    }

                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                        var mc = Minecraft.getInstance();
                        var mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
                        var mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
                        if (isMouseOverElement(mouseX, mouseY)) {
                            return EmiScreenManager.stackInteraction(new EmiStackInteraction(tag, null, true),
                                    bind -> bind.matchesKey(keyCode, scanCode));
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
                var d = new ResearchTagDisplay();
                d.setBackground(GuiTextures.SLOT);
                d.setSize(18, 18);
                group.addWidget(d);
                xOffset += 18;
            }
            yOffset.add(10);
        }
    }

    @Override
    public int getInfoHeight(GTRecipeDefinition recipe) {
        return 30 + 18;
    }

    @RegisterLanguage(cn = "可获得的研究点数", en = "Research Points")
    public static String RESEARCH_POINTS = "gtocore.recipe.extension.research_points";
}
