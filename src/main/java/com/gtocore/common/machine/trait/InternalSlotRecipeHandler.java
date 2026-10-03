package com.gtocore.common.machine.trait;

import com.gtocore.common.machine.multiblock.part.ae.AbstractRecipeInternalSlot;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IFilteredHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.PlanScratch;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;

import appeng.api.stacks.AEKeyType;

import com.gto.recipesearch.IntLongMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

@Getter
public final class InternalSlotRecipeHandler {

    private final List<RecipeHandlerUnit> slotHandlers;

    public InternalSlotRecipeHandler(MEPatternBufferPartMachine buffer, MEPatternBufferPartMachine.InternalSlot[] slots) {
        this.slotHandlers = new ArrayList<>(slots.length);
        for (MEPatternBufferPartMachine.InternalSlot slot : slots) {
            slotHandlers.add(PatternBufferRHL.of(slot, buffer));
        }
    }

    public abstract static class AbstractRHL<S extends AbstractRecipeInternalSlot> extends RecipeHandlerUnit {

        protected final S slot;

        protected AbstractRHL(S slot, IMultiPart part, IRecipeHandler... handlers) {
            super(IO.IN, part, handlers);
            this.isDistinct = true;
            this.slot = slot;
            this.priority = IFilteredHandler.HIGH;
        }

        @Override
        public void refreshPriority() {
            this.priority = IFilteredHandler.HIGH;
        }

        protected abstract @Nullable GTRecipeDefinition getCachedRecipe();

        protected abstract void clearCachedRecipe();

        protected boolean isCachedRecipeAvailable(GTRecipeDefinition recipe) {
            return false;
        }

        protected abstract @Nullable GTRecipeType getEffectiveRecipeType(GTRecipeType recipeType);

        protected abstract void onRecipeHandled(GTRecipe recipe);

        @Override
        public abstract RecipeHandlerUnit wrapper(Collection<IRecipeHandler> handlers);

        @Override
        public boolean findRecipe(GTRecipeType recipeType, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle) {
            if (slot.isEmpty()) return false;
            var cachedRecipe = getCachedRecipe();
            if (cachedRecipe != null) {
                if (canHandle.test(this, cachedRecipe)) {
                    return true;
                } else if (isCachedRecipeAvailable(cachedRecipe)) {
                    // 缺电/输出满等暂时失败时保留缓存，也不按当前机器模式重搜，否则会串到别的配方
                    return false;
                } else {
                    clearCachedRecipe();
                }
            }
            recipeType = getEffectiveRecipeType(recipeType);
            if (recipeType == null) return false;
            var map = this.getSearchMap(recipeType);
            if (map.isEmpty()) return false;
            return recipeType.search(this, map, canHandle);
        }

        @Override
        public boolean planInputs(GTRecipe recipe, PlanScratch p, long scale, boolean rolled) {
            if (!recipe.itemInputs.isEmpty() && slot.isEmpty()) return false;
            return super.planInputs(recipe, p, scale, rolled);
        }

        @Override
        public void onCommitted(GTRecipe recipe) {
            super.onCommitted(recipe);
            onRecipeHandled(recipe);
        }
    }

    static class PatternSlotRHL extends AbstractRHL<MEPatternBufferPartMachine.InternalSlot> {

        PatternSlotRHL(MEPatternBufferPartMachine.InternalSlot slot, IMultiPart part, IRecipeHandler... handlers) {
            super(slot, part, handlers);
        }

        private PatternSlotRHL(IRecipeHandler handler, MEPatternBufferPartMachine.InternalSlot slot, MEPatternBufferPartMachine buffer) {
            super(slot, buffer, handler, slot.circuitInventory, slot.shareInventory, slot.shareTank, buffer.circuitInventorySimulated, buffer.shareInventory, buffer.shareTank);
        }

        @Override
        protected @Nullable GTRecipeDefinition getCachedRecipe() {
            return slot.recipe;
        }

        @Override
        protected void clearCachedRecipe() {
            slot.setRecipe(null);
        }

        @Override
        protected boolean isCachedRecipeAvailable(GTRecipeDefinition recipe) {
            return slot.machine.isRecipeTypeUsable(recipe.recipeType);
        }

        @Override
        protected GTRecipeType getEffectiveRecipeType(GTRecipeType recipeType) {
            final var type = slot.machine.getEffectiveRecipeType();
            if (type != null && type != recipeType) {
                return type;
            }
            return recipeType;
        }

        @Override
        protected void onRecipeHandled(GTRecipe recipe) {
            slot.setRecipe(recipe.definition);
        }

        @Override
        public RecipeHandlerUnit wrapper(Collection<IRecipeHandler> handlers) {
            return new PatternSlotRHL(slot, null, handlers.toArray(new IRecipeHandler[0]));
        }
    }

    final static class PatternBufferRHL extends PatternSlotRHL {

        final SlotRecipeHandler recipeHandler;

        private static PatternBufferRHL of(MEPatternBufferPartMachine.InternalSlot slot, MEPatternBufferPartMachine buffer) {
            return new PatternBufferRHL(new SlotRecipeHandler(buffer, slot), slot, buffer);
        }

        private PatternBufferRHL(SlotRecipeHandler handler, MEPatternBufferPartMachine.InternalSlot slot, MEPatternBufferPartMachine buffer) {
            super(handler, slot, buffer);
            recipeHandler = handler;
        }
    }

    final static class SlotRecipeHandler extends NotifiableRecipeHandlerTrait {

        final MEPatternBufferPartMachine.InternalSlot slot;
        @Nullable
        private GTRecipeType searchType;
        private int searchGeneration;

        private SlotRecipeHandler(MEPatternBufferPartMachine buffer, MEPatternBufferPartMachine.InternalSlot slot) {
            super(buffer);
            this.slot = slot;
            slot.setOnContentsChanged(this::notifyListeners);
        }

        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public @Nullable KeyInventory<?> storage(AEKeyType type) {
            if (type == AEKeyType.items()) return slot.itemInventory;
            if (type == AEKeyType.fluids()) return slot.fluidInventory;
            return null;
        }

        @Override
        public IntLongMap getSearchMap(@NotNull GTRecipeType type) {
            int generation = GTRecipeType.searchGeneration();
            if (slot.isContentsChanged() || searchType != type || searchGeneration != generation) {
                searchType = type;
                searchGeneration = generation;
                var map = slot.ingredientMap;
                map.clear();
                fill(type, slot.fluidInventory, map);
                fill(type, slot.itemInventory, map);
            }
            return slot.ingredientMap;
        }

        private static void fill(GTRecipeType type, KeyInventory<?> inventory, IntLongMap map) {
            int size = inventory.size();
            for (int i = 0; i < size; i++) {
                long amount = inventory.amountAt(i);
                if (amount > 0) type.convertKey(inventory.rawKeyAt(i), amount, map);
            }
        }

        @Override
        public boolean isOnlyRecipe() {
            return true;
        }
    }
}
