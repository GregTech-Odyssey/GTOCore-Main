package com.gtocore.common.wireless.energy.map;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.uipro.window.ScreenHost;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.Nullable;

public final class GridMapUIFactory extends UIFactory<GridMapTarget> {

    public static final GridMapUIFactory INSTANCE = new GridMapUIFactory();

    private GridMapUIFactory() {
        super(GTOCore.id("wireless_grid_map"));
    }

    public static void init() {
        UIFactory.register(INSTANCE);
    }

    public static boolean open(ServerPlayer player, int focusDimRef) {
        return INSTANCE.openUI(new GridMapTarget(focusDimRef, GridMapMode.VIEW), player);
    }

    public static ModularUI createUI(IUIHolder holder, Player player, @Nullable MetaMachine machine, int focusDimRef, GridMapMode mode) {
        return ScreenHost.createUI(new GridMapRoot(new GridMapContext(player, machine, focusDimRef, mode)), holder, player);
    }

    @Override
    protected @Nullable ModularUI createUITemplate(@Nullable GridMapTarget holder, Player player) {
        return holder == null ? null : holder.createUI(player);
    }

    @Override
    protected @Nullable GridMapTarget readHolderFromSyncData(FriendlyByteBuf syncData) {
        return GridMapTarget.CODEC.decode(syncData);
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, GridMapTarget holder) {
        GridMapTarget.CODEC.encode(syncData, holder);
    }
}
