package com.gtocore.common.machine.trait;

import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferProxyPartMachine;

import com.gtolib.api.machine.trait.ProxyRecipeHandler;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.PlanScratch;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

public final class ProxySlotRecipeHandler {

    private static final ProxyRecipeHandler[] NO_HANDLERS = new ProxyRecipeHandler[0];
    public static final ProxySlotRecipeHandler DEFAULT = new ProxySlotRecipeHandler();
    @Getter
    private final List<RecipeHandlerUnit> proxySlotHandlers;
    private final ProxyRecipeHandler[] handlers;

    private ProxySlotRecipeHandler() {
        proxySlotHandlers = Collections.emptyList();
        handlers = NO_HANDLERS;
    }

    public ProxySlotRecipeHandler(MEPatternBufferProxyPartMachine machine, MEPatternBufferPartMachine patternBuffer) {
        int slots = patternBuffer.getMaxPatternCount();
        var slotHandlers = patternBuffer.internalRecipeHandler.getSlotHandlers();
        var circuit = new ProxyRecipeHandler(machine, patternBuffer.circuitInventorySimulated, true, false);
        var sharedItem = new ProxyRecipeHandler(machine, patternBuffer.shareInventory, true, false);
        var sharedFluid = new ProxyRecipeHandler(machine, patternBuffer.shareTank, false, true);
        var all = new ProxyRecipeHandler[3 + slots * 4];
        all[0] = circuit;
        all[1] = sharedItem;
        all[2] = sharedFluid;
        int h = 3;
        proxySlotHandlers = new ArrayList<>(slots);
        for (int i = 0; i < slots; ++i) {
            var slotRHL = (InternalSlotRecipeHandler.PatternBufferRHL) slotHandlers.get(i);
            var slot = slotRHL.slot;
            var slotHandler = all[h++] = new ProxyRecipeHandler(machine, slotRHL.recipeHandler, true, true);
            var slotCircuit = all[h++] = new ProxyRecipeHandler(machine, slot.circuitInventory, true, false);
            var slotSharedItem = all[h++] = new ProxyRecipeHandler(machine, slot.shareInventory, true, false);
            var slotSharedFluid = all[h++] = new ProxyRecipeHandler(machine, slot.shareTank, false, true);
            proxySlotHandlers.add(new PatternBufferProxyRHL(machine, slot, slotHandler, circuit, slotCircuit, sharedItem, slotSharedItem, sharedFluid, slotSharedFluid));
        }
        handlers = all;
    }

    public void release() {
        for (var handler : handlers) {
            handler.release();
        }
    }

    private static final class PatternBufferProxyRHL extends InternalSlotRecipeHandler.PatternSlotRHL {

        private PatternBufferProxyRHL(MEPatternBufferProxyPartMachine machine, MEPatternBufferPartMachine.InternalSlot slot, ProxyRecipeHandler slotHandler, ProxyRecipeHandler circuit, ProxyRecipeHandler slotCircuit, ProxyRecipeHandler sharedItem, ProxyRecipeHandler slotSharedItem, ProxyRecipeHandler sharedFluid, ProxyRecipeHandler slotSharedFluid) {
            super(slot, machine, slotHandler, circuit, slotCircuit, sharedItem, slotSharedItem, sharedFluid, slotSharedFluid);
        }

        private PatternBufferProxyRHL(MEPatternBufferPartMachine.InternalSlot slot, IRecipeHandler[] handlers) {
            super(slot, null, handlers);
        }

        @Override
        public boolean findRecipe(GTRecipeType recipeType, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle) {
            return !slot.machine.isRemoved() && super.findRecipe(recipeType, canHandle);
        }

        @Override
        public boolean planInputs(GTRecipe recipe, PlanScratch p, long scale, boolean rolled) {
            return !slot.machine.isRemoved() && super.planInputs(recipe, p, scale, rolled);
        }

        @Override
        public boolean consume(KeyIngredient ing, long amount, boolean simulate) {
            return !slot.machine.isRemoved() && super.consume(ing, amount, simulate);
        }

        @Override
        public RecipeHandlerUnit wrapper(Collection<IRecipeHandler> handlers) {
            return new PatternBufferProxyRHL(slot, handlers.toArray(new IRecipeHandler[0]));
        }
    }
}
