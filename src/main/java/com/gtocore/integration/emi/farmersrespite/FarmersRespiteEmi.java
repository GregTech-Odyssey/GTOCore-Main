package com.gtocore.integration.emi.farmersrespite;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import umpaz.farmersrespite.client.gui.KettleScreen;
import umpaz.farmersrespite.common.block.entity.container.KettleMenu;
import umpaz.farmersrespite.common.crafting.KettlePouringRecipe;
import umpaz.farmersrespite.common.crafting.KettleRecipe;
import umpaz.farmersrespite.common.registry.FRItems;
import umpaz.farmersrespite.common.registry.FRMenuTypes;
import umpaz.farmersrespite.common.registry.FRRecipeTypes;

import java.util.List;

// Farmer's Respite（Umpaz、Probleyes）水壶冲泡页的 EMI 实现，布局参照原模组，代码独立编写
public final class FarmersRespiteEmi implements EmiPlugin {

    private static final ResourceLocation TEXTURE = new ResourceLocation("farmersrespite", "textures/gui/jei/kettle_jei.png");

    public static final EmiRecipeCategory BREWING = new EmiRecipeCategory(new ResourceLocation("farmersrespite", "brewing"), EmiStack.of(FRItems.KETTLE.get())) {

        @Override
        public Component getName() {
            return Component.translatable("farmersrespite.jei.brewing");
        }
    };

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(BREWING);
        registry.addWorkstation(BREWING, EmiStack.of(FRItems.KETTLE.get()));
        var manager = registry.getRecipeManager();
        List<KettlePouringRecipe> pourings = manager.getAllRecipesFor(FRRecipeTypes.KETTLE_POURING.get());
        for (KettleRecipe brew : manager.getAllRecipesFor(FRRecipeTypes.BREWING.get())) {
            FluidStack out = brew.getFluidOut();
            if (out.isEmpty()) {
                registry.addRecipe(new BrewingEmiRecipe(brew, null));
                continue;
            }
            for (KettlePouringRecipe pouring : pourings) {
                if (pouring.getFluid().isSame(out.getFluid())) {
                    registry.addRecipe(new BrewingEmiRecipe(brew, pouring));
                }
            }
        }
        registry.addRecipeHandler(FRMenuTypes.KETTLE.get(), new KettleHandler());
        registry.addStackProvider(KettleScreen.class, FarmersRespiteEmi::kettleFluidAt);
    }

    private static EmiStackInteraction kettleFluidAt(KettleScreen screen, int x, int y) {
        int rx = x - screen.getGuiLeft();
        int ry = y - screen.getGuiTop();
        if (rx < 106 || rx >= 143 || ry < 18 || ry >= 46) return EmiStackInteraction.EMPTY;
        FluidStack fluid = screen.getMenu().kettleTank.getFluid();
        if (fluid.isEmpty()) return EmiStackInteraction.EMPTY;
        return new EmiStackInteraction(ForgeEmiStack.of(fluid));
    }

    private static final class BrewingEmiRecipe extends BasicEmiRecipe {

        private final List<EmiIngredient> items;
        private final EmiStack fluidIn;
        private final EmiStack fluidOut;
        private final EmiStack container;
        private final EmiStack product;
        private final int pourAmount;
        private final int brewTime;
        private final float experience;

        private BrewingEmiRecipe(KettleRecipe brew, @Nullable KettlePouringRecipe pouring) {
            super(BREWING, pouring == null ? brew.getId() : new ResourceLocation(brew.getId().getNamespace(), "/" + brew.getId().getPath() + "/" + pouring.getId().getPath()), 117, 57);
            var ingredients = brew.getIngredients();
            items = new ObjectArrayList<>(ingredients.size());
            for (Ingredient ingredient : ingredients) {
                if (!ingredient.isEmpty()) items.add(EmiIngredient.of(ingredient));
            }
            inputs = items;
            fluidIn = brew.getFluidIn().isEmpty() ? EmiStack.EMPTY : ForgeEmiStack.of(brew.getFluidIn());
            fluidOut = brew.getFluidOut().isEmpty() ? EmiStack.EMPTY : ForgeEmiStack.of(brew.getFluidOut());
            container = pouring == null ? EmiStack.EMPTY : EmiStack.of(pouring.getContainer());
            product = pouring == null ? EmiStack.EMPTY : EmiStack.of(pouring.getOutput());
            pourAmount = pouring == null ? 0 : pouring.getAmount();
            brewTime = brew.getBrewTime();
            experience = brew.getExperience();
            if (!fluidIn.isEmpty()) catalysts.add(fluidIn);
            if (!container.isEmpty()) catalysts.add(container);
            if (!fluidOut.isEmpty()) outputs.add(fluidOut);
            if (!product.isEmpty()) outputs.add(product);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addTexture(TEXTURE, 0, 0, 117, 57, 29, 16);
            widgets.addAnimatedTexture(TEXTURE, 33, 9, 40, 17, 176, 15, 10_000, true, false, false);
            widgets.addTexture(TEXTURE, 13, 40, 17, 15, 176, 0);
            widgets.addTexture(TEXTURE, 53, 2, 8, 11, 176, 62);
            if (experience > 0) widgets.addTexture(TEXTURE, 52, 21, 9, 9, 176, 73);
            for (int i = 0; i < items.size(); i++) {
                widgets.addSlot(items.get(i), 12, 18 * i).drawBack(false);
            }
            if (!fluidIn.isEmpty()) widgets.addTank(fluidIn, 2, 1, 8, 34, 2000).drawBack(false).catalyst(true);
            if (!fluidOut.isEmpty()) widgets.addTank(fluidOut, 75, 0, 41, 32, 2000).drawBack(false).recipeContext(this);
            widgets.addTexture(TEXTURE, 76, 1, 39, 30, 176, 32);
            if (!container.isEmpty()) {
                widgets.addSlot(container, 56, 38).drawBack(false).catalyst(true)
                        .appendTooltip(Component.literal(String.valueOf(pourAmount)).append(Component.translatable("generic.unit.millibuckets")));
            }
            if (!product.isEmpty()) widgets.addSlot(product, 88, 38).drawBack(false).recipeContext(this);
            List<Component> lines = new ObjectArrayList<>(2);
            lines.add(Component.translatable("emi.cooking.time", brewTime / 20f));
            if (experience > 0) lines.add(Component.translatable("emi.cooking.experience", experience));
            widgets.addTooltipText(lines, 34, 2, 38, 29);
        }
    }

    private static final class KettleHandler implements StandardRecipeHandler<KettleMenu> {

        @Override
        public List<Slot> getInputSources(KettleMenu menu) {
            List<Slot> slots = new ObjectArrayList<>(38);
            slots.add(menu.slots.get(0));
            slots.add(menu.slots.get(1));
            for (int i = 4; i < 40; i++) {
                slots.add(menu.slots.get(i));
            }
            return slots;
        }

        @Override
        public List<Slot> getCraftingSlots(KettleMenu menu) {
            List<Slot> slots = new ObjectArrayList<>(2);
            slots.add(menu.slots.get(0));
            slots.add(menu.slots.get(1));
            return slots;
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == BREWING && recipe.supportsRecipeTree();
        }
    }
}
