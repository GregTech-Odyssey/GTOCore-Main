package com.gtocore.common.machine.noenergy.heat;

import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.SimpleNoEnergyMachine;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.GTValues.LV;

public class HeatValveMachine extends SimpleNoEnergyMachine implements IHeatContainerMachine {

    @Getter
    private final IHeatContainer heatContainer;

    /// Whether the valve is open.
    @Getter
    @SyncToClient(notifyUpdate = true)
    private boolean isOpen = true;
    /// Whether the redstone signal is inverted.
    @Getter
    @SaveToDisk(defaultValue = "false")
    private boolean isReverted = false;

    public HeatValveMachine(MetaMachineBlockEntity holder) {
        super(holder, LV, (t) -> 0);
        heatContainer = new ValveHeatContainer();
    }

    @Override
    public boolean isActive() {
        return isOpen;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.enqueueTask(serverLevel, this::updateValveState, 0);
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public void onNeighborChanged(@NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateValveState();
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        return true;
    }

    @Override
    public boolean testHeatCapability(@Nullable Direction side) {
        return true;
    }

    @Override
    public boolean hasAutoOutputFluid() {
        return false;
    }

    @Override
    public boolean hasAutoOutputItem() {
        return false;
    }

    @Override
    protected @NotNull InteractionResult onScrewdriverClick(@NotNull Player playerIn, @NotNull InteractionHand hand, @NotNull Direction gridSide, @NotNull BlockHitResult hitResult) {
        if (!isRemote()) {
            isReverted = !isReverted;
            markAsDirty();
            updateValveState();
            playerIn.displayClientMessage(Component.translatable((isReverted ? "gtocore.machine.sensor.invert.enabled" : "gtocore.machine.sensor.invert.disabled")), true);
            return InteractionResult.SUCCESS;
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    private void updateValveState() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        boolean open = level.hasNeighborSignal(getPos()) == isReverted;
        if (open == isOpen) return;

        isOpen = open;
        markAsDirty();
        markFieldsForSync("isOpen");
    }

    private final class ValveHeatContainer implements IHeatContainer {

        private boolean transferring;
        private int nextSide;

        @Override
        public long acceptHeatFromNetwork(Object sender, Direction inputSide, long heat, double temperature, double heatCapacity, double baseTransferRate, int rateMultiplier) {
            if (!isOpen || transferring || heat <= 0) return 0;

            transferring = true;
            long heatUsed = 0;
            int start = nextSide;
            nextSide = start == 5 ? 0 : start + 1;
            try {
                for (int i = 0; i < 6; i++) {
                    Direction side = GTUtil.DIRECTIONS[(i + start) % 6];
                    if (side == inputSide) continue;

                    IHeatContainer receiver = GTCapabilityHelper.getBlockEntityGTCapability(IHeatContainer.class, getHolder().getNeighborBlockEntity(side), side.getOpposite());
                    if (receiver == null || receiver == this) continue;

                    long remaining = heat - heatUsed;
                    if (remaining <= 0) break;
                    long accepted = receiver.acceptHeatFromNetwork(sender, side.getOpposite(), remaining, temperature, heatCapacity, baseTransferRate, rateMultiplier);
                    if (accepted > 0) heatUsed += accepted;
                }
                return heatUsed;
            } finally {
                transferring = false;
            }
        }

        @Override
        public boolean heatIO(Direction side) {
            return isOpen;
        }

        @Override
        public long getMaxTemperature() {
            return 0;
        }

        @Override
        public double getTemperature() {
            return 0;
        }

        @Override
        public double getHeatCapacity() {
            return 0;
        }

        @Override
        public double getBaseTransferRate() {
            return 0;
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
            return 0;
        }

        @Override
        public void setCurrentHeat(long heat) {}

        @Override
        public long getMaxHeat() {
            return 0;
        }

        @Override
        public long addHeat(long amount, int rateMultiplier, boolean simulate) {
            return 0;
        }

        @Override
        public long removeHeat(long amount, int rateMultiplier, boolean simulate) {
            return 0;
        }

        @Override
        public long addHeatUnrestricted(long amount, boolean simulate) {
            return 0;
        }

        @Override
        public long removeHeatUnrestricted(long amount, boolean simulate) {
            return 0;
        }

        @Override
        public double transferHeatToAdjacent(int rateMultiplier) {
            return 0;
        }

        @Override
        public int getSignal() {
            return 0;
        }
    }
}
