package com.gtocore.common.machine.multiblock.part.voiding;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.AEKeyFilter;
import appeng.hooks.IUnique;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public final class FilterVoidOutputHatchPartMachine extends VoidOutputHatchPartMachine implements AEKeyFilter {

    @Getter
    @SaveToDisk
    private final KeyInventory<AEFluidKey> tanks = KeyInventory.fluids(81, 1);

    @Setter
    @Getter
    @SaveToDisk(defaultValue = "false")
    public boolean reverse;

    private final IntOpenHashSet ids = new IntOpenHashSet();

    public FilterVoidOutputHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MV);
    }

    @Override
    public boolean matches(AEKey key) {
        if (!(key instanceof AEFluidKey fluidKey)) return false;
        var id = ((IUnique) fluidKey.getFluid()).ae2$getUid();
        if (reverse) {
            return !ids.contains(id);
        } else {
            return ids.contains(id);
        }
    }

    private void onSlotChanged() {
        ids.clear();
        for (int i = 0; i < tanks.size(); i++) {
            var key = tanks.keyAt(i);
            if (key != null) {
                ids.add(((IUnique) key.getFluid()).ae2$getUid());
            }
        }
        if (ids.isEmpty()) {
            handler.setFilter(null);
        } else {
            handler.setFilter(this);
        }
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onSlotChanged();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators((new IFancyConfiguratorButton.Toggle(WidgetIcons.BLACKLIST, WidgetIcons.WHITELIST, this::isReverse, (clickData, pressed) -> this.setReverse(pressed))).setTooltipsSupplier((pressed) -> List.of(Component.translatable("gui.ae2wtlib.switch").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)).append(Component.translatable(pressed ? "gui.ae2wtlib.whitelist" : "gui.ae2wtlib.blacklist")))));
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = 9;
        int colSize = 9;
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        var adapter = new ForgeFluidAdapter(tanks);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int finalIndex = index++;
                container.addWidget(new PhantomFluidWidget(adapter, finalIndex, 4 + x * 18, 4 + y * 18, 18, 18, () -> adapter.getFluidInTank(finalIndex), (fluid -> tanks.set(finalIndex, Keys.fluid(fluid), 1000))).setChangeListener(this::onSlotChanged).setShowAmount(false).setBackground(GuiTextures.FLUID_SLOT));
            }
        }
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}
