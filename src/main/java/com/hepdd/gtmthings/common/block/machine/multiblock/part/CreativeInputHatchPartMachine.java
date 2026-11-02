package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInfiniteSource;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.uipro.elements.PhantomFluidSlot;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeInputHatchPartMachine extends WorkableTieredIOPartMachine implements IDistinctPart {

    private final int SLOT_COUNT = 9;

    public final NotifiableInfiniteSource<AEFluidKey> tank;
    private final int slots;
    @SaveToDisk
    private final KeyInventory<AEFluidKey> creativeTanks;

    @Getter
    @SaveToDisk
    private boolean isDistinct = false;

    // The `Object... args` parameter is necessary in case a superclass needs to pass any args along to createTank().
    // We can't use fields here because those won't be available while createTank() is called.
    public CreativeInputHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        this.slots = SLOT_COUNT;
        this.creativeTanks = KeyInventory.fluids(SLOT_COUNT, 1);
        this.tank = createTank();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected NotifiableInfiniteSource<AEFluidKey> createTank() {
        return new NotifiableInfiniteSource<>(this, creativeTanks, IO.IN, IO.IN, false);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isDistinct) {
            getHandlerUnit().setDistinct(true);
        }
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    @Override
    public Widget createUIWidget() {
        var tankAdapter = new ForgeFluidAdapter(creativeTanks);
        return HatchViews.page(SlotGrid.square(slots, index -> PhantomFluidSlot.of(
                tankAdapter, index,
                () -> tankAdapter.getFluidInTank(index),
                fluid -> setFluid(index, fluid)).xeiPhantom()));
    }

    private void setFluid(int index, @Nullable FluidStack fs) {
        if (index < 0 || index >= creativeTanks.size()) return;
        var key = fs == null ? null : Keys.fluid(fs);
        if (key == null) {
            creativeTanks.set(index, null, 0);
            return;
        }
        for (int i = 0; i < creativeTanks.size(); i++) {
            if (i != index && creativeTanks.keyAt(i) instanceof AEFluidKey other && other.getFluid() == key.getFluid()) return;
        }
        creativeTanks.set(index, key, 1);
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
        getHandlerUnit().setDistinctAndNotify(isDistinct);
    }
}
