package com.gtocore.common.pipe.muffler;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMufflerConduction extends IMachineFeature {

    boolean isMufflerSource();

    default boolean shouldWorkAsMufflerSource() {
        return false;
    }

    static boolean isConnectedToMufflerPipeNet(IMufflerConduction self) {
        if (!self.isMufflerSource()) return false;
        IMufflerPipeConnection connection = self.getMufflerPipeConnection();
        return connection != null && connection.isConnectedToMufflerNet();
    }

    @Nullable
    static IMufflerConduction getMufflerPipeNetOutput(IMufflerConduction self) {
        if (!self.isMufflerSource()) return null;
        IMufflerPipeConnection connection = self.getMufflerPipeConnection();
        return connection == null ? null : connection.getMufflerOutput();
    }

    @Nullable
    default IMufflerPipeConnection getMufflerPipeConnection() {
        Direction side = self().getFrontFacing();
        return GTCapabilityHelper.getBlockEntityGTCapability(
                IMufflerPipeConnection.class,
                self().holder.getNeighborBlockEntity(side),
                side.getOpposite());
    }

    default boolean testCapability(@Nullable Direction side) {
        return side == null || side == self().getFrontFacing();
    }

    @Override
    default @Nullable <T> Object getGTCapability(@NotNull Class<T> cap, @Nullable Direction side) {
        if (cap == IMufflerConduction.class) {
            if (testCapability(side)) return this;
            return GTCapability.EMPTY;
        }
        return null;
    }
}
