package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.client.Message;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.Nullable;

public final class TechTreeBrowser extends UIFactory<TechTreeBrowser.Target> {

    private static final TechTreeBrowser FACTORY = new TechTreeBrowser();

    private TechTreeBrowser() {
        super(GTOCore.id("tech_tree_browser"));
    }

    public static void init() {
        UIFactory.register(FACTORY);
    }

    public static void request(TechNode node) {
        Message.OPEN_TECH_TREE_C2S.send(buf -> buf.writeVarInt(TechTreeView.encodeNode(node)));
    }

    public static void open(ServerPlayer player, int code) {
        var node = TechTreeView.decodeNode(code);
        if (node != null) FACTORY.openUI(new Target(node), player);
    }

    @Override
    protected @Nullable ModularUI createUITemplate(@Nullable Target holder, Player player) {
        return holder == null ? null : holder.createUI(player);
    }

    @Override
    protected @Nullable Target readHolderFromSyncData(FriendlyByteBuf syncData) {
        var node = TechTreeView.decodeNode(syncData.readVarInt());
        return node == null ? null : new Target(node);
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, Target holder) {
        syncData.writeVarInt(TechTreeView.encodeNode(holder.node));
    }

    public static final class Target implements IUIHolder {

        private final TechNode node;

        private Target(TechNode node) {
            this.node = node;
        }

        @Override
        public ModularUI createUI(Player player) {
            return new ModularUI(176, 166, this, player)
                    .widget(TechTreePage.window(new TechTreePage.Options().initialFocus(() -> node)));
        }

        @Override
        public boolean isInvalid() {
            return false;
        }

        @Override
        public boolean isRemote() {
            return GTCEu.isClientThread();
        }

        @Override
        public void markAsDirty() {}
    }
}
