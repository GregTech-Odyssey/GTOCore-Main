package com.gtocore.common.machine.multiblock.part;

import com.gtocore.api.gui.configurators.MultiMachineModeFancyConfigurator;
import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.api.annotation.DataGeneratorScanned;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyTankConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.CircuitHandler;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.PlanScratch;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine;
import com.gregtechceu.gtceu.uipro.elements.FluidSlot;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.recipesearch.IntLongMap;
import com.hepdd.gtmthings.api.machine.IProgrammableMachine;
import com.hepdd.gtmthings.common.item.VirtualProviderData;
import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

@DataGeneratorScanned
public final class ProgrammableHatchPartMachine extends DualHatchPartMachine implements IProgrammableMachine {

    @SaveToDisk
    @SyncToClient
    private final ArrayList<GTRecipeType> recipeTypes = new ArrayList<>();
    @SaveToDisk
    @SyncToClient
    private GTRecipeType recipeType = null;

    @SaveToDisk
    private final ProgrammableFluidHandler fluidTank = new ProgrammableFluidHandler(this);

    public ProgrammableHatchPartMachine(MetaMachineBlockEntity holder, int tier, IO io, Object... args) {
        super(holder, tier, io, args);
    }

    @Override
    protected @NotNull NotifiableInventory<AEItemKey> createInventory(Object @NotNull... args) {
        return NotifiableInventory.items(this, getInventorySize(), io)
                .setFilter(key -> !(key instanceof AEItemKey itemKey && isConfiguredVirtualProvider(itemKey.getReadOnlyStack())));
    }

    public static boolean isConfiguredVirtualProvider(ItemStack stack) {
        if (!stack.is(CustomItems.VIRTUAL_ITEM_PROVIDER.get()) &&
                !stack.is(CustomItems.VIRTUAL_FLUID_PROVIDER.get()))
            return false;
        return VirtualProviderData.hasData(stack);
    }

    @Override
    protected @NotNull NotifiableInventory<AEItemKey> createCircuitItemHandler(Object... args) {
        if (args.length > 0 && args[0] instanceof IO io && io == IO.IN) {
            return new ProgrammableCircuitHandler(this);
        } else {
            return NotifiableInventory.empty(this, AEKeyType.items());
        }
    }

    @Override
    public RecipeHandlerUnit getHandlerUnit() {
        var list = getRecipeHandlerUnit();
        if (list == null) {
            List<IRecipeHandler> handlers = new ArrayList<>();
            IO handlerIO = null;
            for (var trait : self().getTraits()) {
                if (trait instanceof IRecipeHandlerTrait rht && rht.isAvailable() && rht.getHandlerIO() != IO.NONE) {
                    if (handlerIO == null) handlerIO = rht.getHandlerIO();
                    handlers.add(rht);
                }
            }

            if (handlers.isEmpty()) {
                list = RecipeHandlerUnit.NO_DATA;
                setRecipeHandlerUnit(list);
            } else {
                list = new ProgrammableRHL(handlerIO, this, handlers);
                setRecipeHandlerUnit(list);
            }
        }
        return list;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (recipeType == GTORecipeTypes.DUMMY_RECIPES || recipeType == GTORecipeTypes.HATCH_COMBINED) {
            recipeType = null;
        }
        MultiMachineModeFancyConfigurator.verify(recipeTypes, recipeType, () -> recipeType = null);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new MultiMachineModeFancyConfigurator(recipeTypes, recipeType, this::setRecipeType));
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new FancyTankConfigurator(fluidTank.storage, Component.translatable("gui.gtceu.share_tank.title")) {

            @Override
            public Widget createConfigurator() {
                var adapter = new ForgeFluidAdapter(fluidTank.clearOnDrainView);
                return SlotGrid.square(1, i -> FluidSlot.of(adapter, i, true, true));
            }
        });
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        this.recipeTypes.clear();
        this.recipeTypes.addAll(MultiMachineModeFancyConfigurator.extractRecipeTypes(this.getController()));
        MultiMachineModeFancyConfigurator.verify(recipeTypes, recipeType, () -> recipeType = null);
    }

    @Override
    public void setAvailableRecipeTypes(@NotNull GTRecipeType[] types) {
        this.recipeTypes.clear();
        this.recipeTypes.addAll(Arrays.asList(types));
        MultiMachineModeFancyConfigurator.verify(recipeTypes, recipeType, () -> recipeType = null);
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        this.recipeTypes.clear();
    }

    public void setRecipeType(GTRecipeType type) {
        if (type != recipeType) {
            recipeType = type;
            for (var c : getControllers()) {
                if (c instanceof IRecipeLogicMachine machine) {
                    machine.getRecipeLogic().markLastRecipeDirty();
                    machine.getRecipeLogic().updateTickSubscription();
                }
            }
        }
    }

    public @Nullable GTRecipeType getRecipeType() {
        return recipeType;
    }

    @Override
    public boolean swapIO() {
        // Programmable hatches should not be able to swap IO
        return false;
    }

    @Override
    public boolean isProgrammable() {
        return true;
    }

    @Override
    public void setProgrammable(boolean programmable) {}

    private static final class ProgrammableFluidHandler extends NotifiableInventory<AEFluidKey> {

        private final IKeyHandler<AEFluidKey> clearOnDrainView = new ClearOnDrainView(storage);

        public ProgrammableFluidHandler(MetaMachine machine) {
            super(machine, KeyInventory.fluids(1, 1000), IO.IN, IO.NONE);
        }

        @Override
        public boolean isNotConsumable() {
            return true;
        }

        @Override
        public @Nullable KeyInventory<?> storage(AEKeyType type) {
            return null;
        }

        @Override
        public boolean handlesFluids() {
            return true;
        }

        @Override
        public long available(AEKeyType type, KeyIngredient ingredient) {
            if (type != AEKeyType.fluids() || storage.amountAt(0) <= 0) return 0;
            return ingredient.test(storage.uidAt(0), storage.rawKeyAt(0)) ? Long.MAX_VALUE : 0;
        }

        @Override
        public long reserveInput(PlanScratch plan, int member, AEKeyType type, int entry, KeyIngredient ingredient, long need, boolean consume) {
            return !consume && available(type, ingredient) > 0 ? need : 0;
        }

        @Override
        public boolean forEachKey(AEKeyType type, KeyVisitor visitor) {
            if (type != AEKeyType.fluids()) return false;
            long amount = storage.amountAt(0);
            return amount > 0 && visitor.visit(storage.rawKeyAt(0), amount);
        }

        @Override
        public void addToSearchMap(IntLongMap target, @NotNull GTRecipeType type) {
            getSearchMap(type).setTo(target);
        }

        private void setVirtualFluid(@Nullable AEFluidKey key, long amount) {
            storage.set(0, key, amount);
        }
    }

    private static final class ClearOnDrainView implements IKeyHandler<AEFluidKey> {

        private final KeyInventory<AEFluidKey> tank;

        private ClearOnDrainView(KeyInventory<AEFluidKey> tank) {
            this.tank = tank;
        }

        @Override
        public AEKeyType keyType() {
            return AEKeyType.fluids();
        }

        @Override
        public int size() {
            return tank.size();
        }

        @Override
        public @Nullable AEFluidKey keyAt(int slot) {
            return tank.keyAt(slot);
        }

        @Override
        public long amountAt(int slot) {
            return tank.amountAt(slot);
        }

        @Override
        public long slotLimit(int slot) {
            return tank.slotLimit(slot);
        }

        @Override
        public long insert(int slot, AEFluidKey key, long amount, boolean simulate) {
            return 0;
        }

        @Override
        public long extract(int slot, AEFluidKey key, long amount, boolean simulate) {
            if (amount <= 0 || tank.amountAt(slot) <= 0) return 0;
            if (simulate) return amount;
            tank.set(slot, null, 0);
            return 0;
        }

        @Override
        public long insert(AEFluidKey key, long amount, boolean simulate) {
            return 0;
        }

        @Override
        public long extract(AEFluidKey key, long amount, boolean simulate) {
            for (int i = 0; i < tank.size(); i++) {
                if (tank.amountAt(i) > 0) return extract(i, key, amount, simulate);
            }
            return 0;
        }
    }

    public static class ProgrammableCircuitHandler extends CircuitHandler {

        private static final Item VIRTUAL_ITEM_PROVIDER = CustomItems.VIRTUAL_ITEM_PROVIDER.asItem();
        private static final Item VIRTUAL_FLUID_PROVIDER = CustomItems.VIRTUAL_FLUID_PROVIDER.asItem();
        private final IProgrammableMachine programmable;
        @Nullable
        private final ProgrammableHatchPartMachine part;

        public ProgrammableCircuitHandler(MetaMachine machine) {
            super(machine, IO.IN);
            this.programmable = (IProgrammableMachine) machine;
            this.part = machine instanceof ProgrammableHatchPartMachine partMachine ? partMachine : null;
        }

        @Override
        public void addToSearchMap(IntLongMap target, @NotNull GTRecipeType type) {
            getSearchMap(type).setTo(target);
        }

        @Override
        public long insert(int slot, AEItemKey key, long amount, boolean simulate) {
            return 0;
        }

        @Override
        public long insert(AEItemKey key, long amount, boolean simulate) {
            if (amount <= 0 || !canCapInput() || !programmable.isProgrammable()) return 0;
            var item = key.getItem();
            if (item == VIRTUAL_ITEM_PROVIDER && VirtualProviderData.hasData(key.getReadOnlyStack())) {
                if (!simulate) {
                    var virtual = VirtualProviderData.getVirtualItem(key.getReadOnlyStack());
                    storage.set(0, Keys.item(virtual), virtual.getCount());
                }
                return 1;
            } else if (part != null && item == VIRTUAL_FLUID_PROVIDER && VirtualProviderData.hasData(key.getReadOnlyStack())) {
                if (!simulate) {
                    var virtual = VirtualProviderData.getVirtualFluid(key.getReadOnlyStack());
                    part.fluidTank.setVirtualFluid(Keys.fluid(virtual), virtual.getAmount());
                }
                return 1;
            }
            return 0;
        }
    }

    private static class ProgrammableRHL extends RecipeHandlerUnit {

        private final ProgrammableHatchPartMachine part;

        private ProgrammableRHL(IO handlerIO, ProgrammableHatchPartMachine part, Collection<IRecipeHandler> handlers) {
            super(handlerIO, part, handlers.toArray(new IRecipeHandler[0]));
            this.part = part;
            this.priority = 10000;
        }

        @Override
        public void refreshPriority() {
            this.priority = 10000;
        }

        @Override
        public RecipeHandlerUnit wrapper(Collection<IRecipeHandler> handlers) {
            return new ProgrammableRHL(IO.IN, part, handlers);
        }

        @Override
        public boolean findRecipe(GTRecipeType recipeType, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle) {
            final var type = part.recipeType;
            if (type != null && type != recipeType) {
                recipeType = type;
            }
            var map = this.getSearchMap(recipeType);
            if (map.isEmpty()) return false;
            return recipeType.search(this, map, canHandle);
        }
    }
}
