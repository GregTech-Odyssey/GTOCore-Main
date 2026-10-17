package com.gtocore.common.machine.dev;

import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeHeatProviderMachine extends MetaMachine implements IUIMachine, IHeatContainerMachine {

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private long confHeat;
    private final CreativeHeatContainer heatContainer = new CreativeHeatContainer();
    @Nullable
    private TickableSubscription transferSubscription;

    public CreativeHeatProviderMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            transferSubscription = subscribeServerTick(transferSubscription, () -> heatContainer.transferHeatToAdjacent(20), 20);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (transferSubscription != null) {
            transferSubscription.unsubscribe();
            transferSubscription = null;
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(140, 95, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 7, "K"))
                .widget(new TextFieldWidget(9, 20, 122, 16, () -> String.valueOf(confHeat), value -> {
                    confHeat = Long.parseLong(value);
                    markAsDirty();
                    requestSync();
                }).setNumbersOnly(0, Long.MAX_VALUE));
    }

    @Override
    public boolean testHeatCapability(@Nullable Direction side) {
        return true;
    }

    @Override
    public IHeatContainer getHeatContainer() {
        return heatContainer;
    }

    private final class CreativeHeatContainer implements IHeatContainer {

        private int nextSide;

        @Override
        public double getTemperature() {
            return confHeat;
        }

        @Override
        public long getMaxTemperature() {
            return Long.MAX_VALUE;
        }

        @Override
        public double getHeatCapacity() {
            return Long.MAX_VALUE;
        }

        @Override
        public double getBaseTransferRate() {
            return Long.MAX_VALUE;
        }

        @Override
        public double getCooldownRate() {
            return 0;
        }

        @Override
        public double getAmbientTemperature() {
            return 0;
        }

        @Override
        public long getCurrentHeat() {
            return confHeat > 0 ? Long.MAX_VALUE : 0;
        }

        @Override
        public void setCurrentHeat(long heat) {}

        @Override
        public long getMaxHeat() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean heatIO(Direction side) {
            return true;
        }

        @Override
        public long addHeat(long amount, int rateMultiplier, boolean simulate) {
            return rateMultiplier > 0 ? addHeatUnrestricted(amount, simulate) : 0;
        }

        @Override
        public long removeHeat(long amount, int rateMultiplier, boolean simulate) {
            return rateMultiplier > 0 ? removeHeatUnrestricted(amount, simulate) : 0;
        }

        @Override
        public long addHeatUnrestricted(long amount, boolean simulate) {
            return Math.max(0, amount);
        }

        @Override
        public long removeHeatUnrestricted(long amount, boolean simulate) {
            return confHeat > 0 ? Math.max(0, amount) : 0;
        }

        @Override
        public double transferHeatToAdjacent(int rateMultiplier) {
            if (confHeat <= 0 || rateMultiplier <= 0) return 0;
            double transferred = 0;
            int start = nextSide;
            nextSide = start == 5 ? 0 : start + 1;
            for (int i = 0; i < 6; i++) {
                Direction side = GTUtil.DIRECTIONS[(i + start) % 6];
                var receiver = GTCapabilityHelper.getBlockEntityGTCapability(IHeatContainer.class, getHolder().getNeighborBlockEntity(side), side.getOpposite());
                if (receiver == null || receiver == this) continue;
                transferred += receiver.acceptHeatFromNetwork(this, side.getOpposite(), getCurrentHeat(), getTemperature(), getHeatCapacity(), getBaseTransferRate(), rateMultiplier);
            }
            return transferred;
        }

        @Override
        public long acceptHeatFromNetwork(Object sender, Direction side, long heat, double temperature, double heatCapacity, double baseTransferRate, int rateMultiplier) {
            if (sender == this || heat <= 0 || rateMultiplier <= 0) return 0;
            var deltaT = temperature - getTemperature();
            if (deltaT <= 0) return 0;
            long balanced = (long) (deltaT * getHeatCapacity() * heatCapacity / (getHeatCapacity() + heatCapacity));
            long maxRate = (long) (Math.min(getBaseTransferRate(), baseTransferRate) * deltaT * rateMultiplier);
            return Math.max(0, Math.min(heat, Math.min(balanced, maxRate)));
        }

        @Override
        public int getSignal() {
            return (int) Math.min(15, 15.0 * getTemperature() / getMaxTemperature());
        }
    }
}
