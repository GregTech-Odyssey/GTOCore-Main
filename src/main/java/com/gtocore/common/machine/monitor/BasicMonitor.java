package com.gtocore.common.machine.monitor;

import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.uipro.elements.Form;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.hepdd.gtmthings.api.capability.IBindable;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BasicMonitor extends MetaMachine implements IBindable, IMachineLife, IMonitor, IFancyUIMachine {

    private TickableSubscription tickSubscription;

    @Getter
    @SaveToDisk
    @SyncToClient
    private CompoundTag screenLayout = new CompoundTag();

    public BasicMonitor(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() != null && !getLevel().isClientSide) {
            Manager.addBlock(this);
        } else {
            tickSubscription = subscribeClientTick(tickSubscription, this::clientTick, 10);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (getLevel() != null && !getLevel().isClientSide) {
            Manager.removeBlock(this);
        }
        tickSubscription = ITickSubscription.unsubscribe(tickSubscription);
    }

    @OnlyIn(Dist.CLIENT)
    protected void clientTick() {
        Manager.updateAllNetworkDisplayMachines(getLevel());
    }

    @Override
    public @Nullable UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public void onPaintingColorChanged(int color) {
        super.onPaintingColorChanged(color);
        if (getLevel() != null && !getLevel().isClientSide) {
            Manager.removeBlock(getBlockState(), getPos(), getLevel());
            Manager.addBlock(getBlockState(), getPos(), getLevel(), color);
        }
    }

    public void setScreenLayout(CompoundTag layout) {
        screenLayout = layout.copy();
        onChanged();
        requestSync();
    }

    @Override
    public Widget createUIWidget() {
        return MonitorEditor.create(this);
    }

    @Override
    public void attachSideTabs(TabsWidget tabs) {
        tabs.setMainTab(this);
        var network = Manager.networkAt(this);
        if (network == null || getLevel() == null) return;
        for (var provider : network.providers(getLevel())) {
            tabs.attachSubTab(new ConfigurationPage(provider));
        }
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        var network = Manager.networkAt(this);
        if (network == null || getLevel() == null) return Collections.emptyList();
        var providers = network.providers(getLevel());
        var pages = new ArrayList<IFancyUIProvider>(providers.size());
        for (var provider : providers) pages.add(new ConfigurationPage(provider));
        return pages;
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel panel) {}

    @Override
    public boolean windowHasPlayerInventory() {
        return true;
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    private record ConfigurationPage(IInformationProvider provider) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            return provider instanceof AbstractInfoProviderMonitor monitor ? monitor.createConfigurationPage() : Form.page();
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(((MetaMachine) provider).getDefinition().asItem());
        }

        @Override
        public Component getTitle() {
            return Component.translatable(((MetaMachine) provider).getDefinition().getDescriptionId());
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }
    }
}
