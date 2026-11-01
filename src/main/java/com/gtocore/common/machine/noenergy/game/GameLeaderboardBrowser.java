package com.gtocore.common.machine.noenergy.game;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.mojang.brigadier.builder.ArgumentBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * 全部榜单的独立入口：一个只承载 {@link GameLeaderboardsPage} 的窗口，由 {@code /gtocore leaderboard} 打开。
 *
 * <p>
 * 各游戏机自己的排行榜页只列本游戏的榜；聚合榜在这里看，数据直接从三份里程碑存档取，与任何机器无关。
 */
public final class GameLeaderboardBrowser extends UIFactory<GameLeaderboardBrowser.Target> {

    private static final GameLeaderboardBrowser FACTORY = new GameLeaderboardBrowser();

    private GameLeaderboardBrowser() {
        super(GTOCore.id("game_leaderboards"));
    }

    public static void init() {
        UIFactory.register(FACTORY);
    }

    /** {@code /gtocore leaderboard}：打开聚合榜单窗口。 */
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("leaderboard")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(context -> open(context.getSource().getPlayerOrException()) ? 1 : 0);
    }

    private static boolean open(ServerPlayer player) {
        return FACTORY.openUI(new Target(), player);
    }

    @Override
    protected @Nullable ModularUI createUITemplate(@Nullable Target holder, Player player) {
        return holder == null ? null : holder.createUI(player);
    }

    @Override
    protected @Nullable Target readHolderFromSyncData(FriendlyByteBuf syncData) {
        return new Target();
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, Target holder) {}

    public static final class Target implements IUIHolder {

        @Override
        public ModularUI createUI(Player player) {
            return MachineWindow.createUI(new GameLeaderboardsPage(), this, player);
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
