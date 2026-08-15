package com.gtocore.common.machine.multiblock.part.research.computer;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.machine.multiblock.electric.SupercomputingCenterMachine;
import com.gtocore.common.machine.multiblock.part.research.SimpleResearchTagPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.HPCAMachine;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import lombok.Getter;
import lombok.Setter;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.VA;

public class ComputationalDataHolderMachine extends SimpleResearchTagPartMachine
                                            implements IHPCAComponentHatch {

    @Getter
    @Setter
    @SaveToDisk
    @SyncToClient(notifyUpdate = true)
    private boolean damaged;

    private TickableSubscription subscription;

    public ComputationalDataHolderMachine(MetaMachineBlockEntity holder) {
        super(holder, 1024L, ResearchTag.COMPUTATION);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = subscribeServerTick(subscription, () -> {
            if (isDamaged()) {
                return;
            }
            int data = 0;
            if (getController() instanceof HPCAMachine hcpa) {
                data = (int) Math.sqrt(hcpa.getHpcaHandler().getMaxCWUt() + hcpa.getHpcaHandler().getCachedCWUt());
            } else if (getController() instanceof SupercomputingCenterMachine supercomputingCenter) {
                data = (int) Math.sqrt(supercomputingCenter.getAdjustedMaxCWU() + supercomputingCenter.getCacheCWUt());
            }
            addData(data);
        }, 100);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public int getUpkeepEUt() {
        return VA[EV] * 2;
    }

    @Override
    public boolean canBeDamaged() {
        return true;
    }

    @Override
    public boolean isBridge() {
        return false;
    }

    @Override
    public void addData(double amount) {
        if (isDamaged()) {
            return;
        }
        super.addData(amount);
    }

    @Override
    public ResourceTexture getComponentIcon() {
        return GTOGuiTextures.COMPUTATION_RESEARCH_TAG_COMPONENT;
    }
}
