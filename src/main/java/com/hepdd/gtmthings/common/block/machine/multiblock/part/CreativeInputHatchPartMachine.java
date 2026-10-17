package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInfiniteSource;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
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
        int rowSize = (int) Math.sqrt(slots);
        int colSize = rowSize;
        if (slots == 8) {
            rowSize = 4;
            colSize = 2;
        }

        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);

        int index = 0;
        var tankAdapter = new ForgeFluidAdapter(creativeTanks);
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int finalIndex = index++;
                container.addWidget(new PhantomFluidWidget(
                        tankAdapter, finalIndex,
                        4 + x * 18, 4 + y * 18, 18, 18,
                        () -> tankAdapter.getFluidInTank(finalIndex),
                        fluid -> setFluid(finalIndex, fluid)).setShowAmount(false).setBackground(GuiTextures.FLUID_SLOT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
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
