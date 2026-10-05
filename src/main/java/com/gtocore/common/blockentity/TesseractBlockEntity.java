package com.gtocore.common.blockentity;

import com.gtocore.common.machine.tesseract.TesseractCapCache;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TesseractBlockEntity extends MetaMachineBlockEntity {

    public TesseractBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    private TesseractCapCache.Holder tesseract() {
        return (TesseractCapCache.Holder) metaMachine;
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if ((cap == ForgeCapabilities.ITEM_HANDLER || cap == ForgeCapabilities.FLUID_HANDLER || cap == Capabilities.STORAGE) && tesseract().isCalled()) return LazyOptional.empty();
        return super.getCapability(cap, side);
    }

    @Override
    public @Nullable MEStorage getMEStorage(@Nullable Direction side) {
        return tesseract().isCalled() ? null : super.getMEStorage(side);
    }
}
