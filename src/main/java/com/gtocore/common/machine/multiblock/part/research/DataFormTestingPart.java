package com.gtocore.common.machine.multiblock.part.research;

import com.gtocore.common.machine.multiblock.electric.research.DataFormTestingPlantMachine;

import com.gtolib.api.machine.part.AmountConfigurationPartMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Getter;
import lombok.Setter;

public class DataFormTestingPart extends AmountConfigurationPartMachine implements IMachineLife, MEStorage, IGridConnectedMachine, IStorageProvider {

    @SaveToDisk
    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);
    @SyncToClient
    @Getter
    @Setter
    private boolean isOnline;

    public DataFormTestingPart(MetaMachineBlockEntity holder) {
        super(holder, GTValues.EV, -1000000, 1000000);
        this.getMainNode().addService(IStorageProvider.class, this);
    }

    @Override
    public void mountInventories(IStorageMounts iStorageMounts) {
        iStorageMounts.mount(this, (int) this.current);
    }

    @Override
    public Widget createUIWidget() {
        return ((WidgetGroup) super.createUIWidget()).addWidget(new LabelWidget(24, -16, () -> "gui.ae2.Priority"));
    }

    @Override
    public Component getDescription() {
        return getDefinition().asItem().getDescription();
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (getController() instanceof DataFormTestingPlantMachine controller) {
            return controller.insert(what, amount, mode);
        }
        return 0;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return getController() instanceof DataFormTestingPlantMachine controller && controller.isPreferredStorageFor();
    }
}
