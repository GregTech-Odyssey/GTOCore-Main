package com.gtocore.common.pipe.steam;

import com.gtocore.common.blockentity.SteamPipeBlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public final class SteamNetHandler implements MEStorage {

    private final SteamPipeBlockEntity pipe;
    @Getter
    private final Direction facing;

    public SteamNetHandler(SteamPipeBlockEntity pipe, @NotNull Direction facing) {
        this.pipe = pipe;
        this.facing = facing;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return pipe.insert(what, amount, mode, facing);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {}

    @Override
    public KeyCounter getAvailableStacks() {
        return KeyCounter.empty();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(pipe.getBlockState().getBlock().getDescriptionId());
    }
}
