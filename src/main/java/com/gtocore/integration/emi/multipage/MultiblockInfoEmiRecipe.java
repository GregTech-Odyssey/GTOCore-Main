package com.gtocore.integration.emi.multipage;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.OrderItem;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.StructurePattern;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.integration.emi.multipage.StructurePreviewTrigger;
import com.gregtechceu.gtceu.uiwidgets.structure.StructurePlans;
import com.gregtechceu.gtceu.uiwidgets.structure.StructurePreviewWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.emi.ModularForegroundRenderWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class MultiblockInfoEmiRecipe extends ModularEmiRecipe<Widget> {

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(GTCEu.id("multiblock_info"), EmiStack.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE.asItem())) {

        @Override
        public Component getName() {
            return Component.translatable("gtceu.jei.multiblock_info");
        }
    };

    private static final Widget STRUCTURE = new Widget(0, 0, StructurePreviewWidget.WIDTH, StructurePreviewWidget.HEIGHT);

    public final MultiblockMachineDefinition definition;
    private volatile boolean inputsCollected;

    public MultiblockInfoEmiRecipe(MultiblockMachineDefinition definition) {
        super(() -> STRUCTURE);
        this.definition = definition;
        widget = () -> {
            var structure = StructurePattern.of(definition);
            return structure != null ? new StructurePreviewWidget(definition, structure, () -> StructurePreviewTrigger.onShown(definition, structure)) :
                    new Widget(0, 0, StructurePreviewWidget.WIDTH, StructurePreviewWidget.HEIGHT);
        };
    }

    @Override
    public List<EmiIngredient> getInputs() {
        if (!inputsCollected) {
            synchronized (this) {
                if (!inputsCollected) {
                    collectInputs();
                    inputsCollected = true;
                }
            }
        }
        return inputs;
    }

    private void collectInputs() {
        var pattern = definition.getPatternFactory()[0].get();
        if (pattern.predicates != null) {
            Set<Set<Item>> groups = new ObjectOpenHashSet<>();
            for (var predicate : pattern.predicates) {
                addGroups(predicate.common, groups);
                addGroups(predicate.limited, groups);
            }
            for (var group : groups) inputs.add(EmiIngredient.of(group.stream().map(EmiStack::of).toList(), 1));
        }
        var structure = StructurePattern.of(definition);
        var layout = structure == null ? null : structure.layout(structure.defaultValues());
        if (layout == null) return;
        var controller = definition.asItem();
        var parts = new ArrayList<>(StructurePlans.preview(definition, layout).parts());
        parts.sort(Comparator.<ItemStack>comparingInt(stack -> stack.getItem() == controller ? 0 : hasBlockEntity(stack.getItem()) ? 1 : 2)
                .thenComparingInt(stack -> -stack.getCount()));
        for (var stack : parts) {
            if (!stack.isEmpty()) inputs.add(EmiStack.of(stack));
        }
    }

    private static void addGroups(List<SimplePredicate> predicates, Set<Set<Item>> groups) {
        for (var simplePredicate : predicates) {
            if (simplePredicate == null || simplePredicate.candidates == null) continue;
            Set<Item> items = new ReferenceOpenHashSet<>();
            for (var itemStack : simplePredicate.getCandidates()) {
                var item = itemStack.getItem();
                if (item == Items.AIR || item == Items.BARRIER) continue;
                items.add(item);
            }
            if (items.size() > 1) groups.add(items);
        }
    }

    private static boolean hasBlockEntity(Item item) {
        return item instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().hasBlockEntity();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(OrderItem.setTarget(GTOItems.ORDER.asStack(), definition.asStack())));
    }

    @Override
    public List<Widget> getFlatWidgetCollection(Widget widgetIn) {
        return Collections.emptyList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return definition.getId();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var widget = this.widget.get();
        var modular = new ModularWrapper<>(widget);
        modular.setRecipeWidget(0, 0);

        synchronized (CACHE_OPENED) {
            CACHE_OPENED.add(modular);
        }
        widgets.add(new CustomModularEmiRecipe(modular, Collections.emptyList()));
        widgets.add(new ModularForegroundRenderWidget(modular));
        widgets.add(new SlotWidget(EmiStack.of(OrderItem.setTarget(GTOItems.ORDER.asStack(), definition.asStack())), 1000, 1000).recipeContext(this));
    }

    @Override
    public void addTempWidgets(WidgetHolder widgets) {
        if (TEMP_CACHE != null) {
            TEMP_CACHE.modularUI.triggerCloseListeners();
            TEMP_CACHE = null;
        }

        var widget = this.widget.get();
        var modular = new ModularWrapper<>(widget);
        modular.setRecipeWidget(0, 0);
        widgets.add(new CustomModularEmiRecipe(modular, Collections.emptyList()));
        TEMP_CACHE = modular;
    }
}
