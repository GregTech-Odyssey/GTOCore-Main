package com.gtocore.client;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechNodeToast;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.common.forge.ServerLangHook;
import com.gtocore.integration.ae.hooks.ICraftAmountMenu;
import com.gtocore.integration.ae.hooks.IExtendedPatternEncodingTerm;

import com.gtolib.api.network.NetworkPack;
import com.gtolib.utils.ServerUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.menu.me.common.MEStorageMenu;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class Message {

    public static void init() {}

    public static final NetworkPack OPEN_CONTAINER_C2S = NetworkPack.registerC2S("openContainerC2S", (p, b) -> tryOpenMetaMachineUI(
            p,
            b.readGlobalPos(),
            b.readOptional(buf -> Direction.from3DDataValue(buf.readByte())).orElse(null)));

    private static void tryOpenMetaMachineUI(ServerPlayer p, GlobalPos globalPos, @Nullable Direction side) {
        Level level = p.getServer().getLevel(globalPos.dimension());
        if (level != null && level.isLoaded(globalPos.pos())) {
            var be = level.getBlockEntity(globalPos.pos());
            if (be instanceof MetaMachineBlockEntity mbe && mbe.getMetaMachine() instanceof IUIMachine uiMachine) {
                uiMachine.tryToOpenUI(p, InteractionHand.MAIN_HAND,
                        new BlockHitResult(globalPos.pos().getCenter(), p.getDirection(), globalPos.pos(), false));
                return;
            }
            if (be instanceof CableBusBlockEntity cbbe && side != null) {
                var part = cbbe.getPart(side);
                if (part != null) {
                    part.onActivate(p, InteractionHand.MAIN_HAND, globalPos.pos().getCenter());
                }
                return;
            }
            if (be instanceof AEBaseBlockEntity aeBase && aeBase.getBlockEntity().getBlockState().getBlock() instanceof AEBaseEntityBlock<?> aeBlock) {
                aeBlock.onActivated(level, globalPos.pos(), p, InteractionHand.MAIN_HAND, ItemStack.EMPTY, new BlockHitResult(globalPos.pos().getCenter(), p.getDirection(), globalPos.pos(), false));
            }
        }
    }

    public static final NetworkPack ORDER_ITEM_C2S = NetworkPack.registerC2S("orderItemC2S", (p, b) -> {
        var containerId = b.readInt();
        if (p.containerMenu.containerId != containerId) return;
        if (!(p.containerMenu instanceof MEStorageMenu menu)) return;
        var keyCounter = new KeyCounter();
        var size = b.readVarInt();
        for (int i = 0; i < size; i++) {
            var stack = GenericStack.readBuffer(b);
            if (stack == null) return;
            keyCounter.add(stack.what(), stack.amount());
        }
        ICraftAmountMenu.open(p, menu.getLocator(), keyCounter, b.readLong());
    });

    public static final NetworkPack SEND_PATTERN_DESTINATION_S2C = NetworkPack.registerS2C("sendPatternDestinationS2C", (p, b) -> {
        var size = b.readVarInt();
        var destinations = new PatternDestination[size];
        for (int i = 0; i < size; i++) {
            destinations[i] = new PatternDestination(PatternContainerGroup.readFromPacket(b), b.readBoolean());
        }
        Client.patternDestinationReceived(destinations);
    });

    public static final NetworkPack SEND_RESEARCH_S2C = NetworkPack.registerS2C("sendResearchS2C", (p, b) -> {
        var isUnlock = b.readBoolean();
        var node = GTOCodecs.TECH_NODE_STREAM_CODEC.decode(b);
        if (node != null) {
            Client.addToast(node, isUnlock);
        }
    });

    public static void sendResearchToast(UUID teamid, TechNode node, boolean isUnlock) {
        var teamManager = FTBTeamsAPI.api().getManager();
        var team = teamManager.getTeamByID(teamid).orElse(teamManager.getTeamForPlayerID(teamid).orElse(null));
        if (team == null) {
            SEND_RESEARCH_S2C.send(buf -> {
                buf.writeBoolean(isUnlock);
                GTOCodecs.TECH_NODE_STREAM_CODEC.encode(buf, node);
            }, ServerUtils.getServer().getPlayerList().getPlayer(teamid));
            return;
        }
        for (var player : team.getMembers()) {
            SEND_RESEARCH_S2C.send(buf -> {
                buf.writeBoolean(isUnlock);
                GTOCodecs.TECH_NODE_STREAM_CODEC.encode(buf, node);
            }, ServerUtils.getServer().getPlayerList().getPlayer(player));
        }
    }

    public static void sendPatternDestination(ServerPlayer player, PatternDestination[] destinations) {
        SEND_PATTERN_DESTINATION_S2C.send(buf -> {
            buf.writeVarInt(destinations.length);
            for (var dest : destinations) {
                dest.group().writeToPacket(buf);
                buf.writeBoolean(dest.full());
            }
        }, player);
    }

    public record PatternDestination(PatternContainerGroup group, boolean full) {}

    public static final NetworkPack serverLangSync = NetworkPack.registerC2S("serverLangSyncC2S", (p, b) -> {
        if (!ServerUtils.isServerLangInitialized()) {
            ServerLangHook.set(ServerUtils.getServer(), b.readUtf());
        }
    });

    public static class Client {

        public static void orderItem(KeyCounter whatToCraft, long initialAmount) {
            ORDER_ITEM_C2S.send(b -> {
                b.writeInt(Minecraft.getInstance().player.containerMenu.containerId);
                b.writeVarInt(whatToCraft.size());
                for (var entry : whatToCraft.genericStackSet()) {
                    GenericStack.writeBuffer(entry, b);
                }
                b.writeLong(initialAmount);
            });
        }

        public static void patternDestinationReceived(PatternDestination[] destinations) {
            if (Minecraft.getInstance().screen instanceof PatternEncodingTermScreen<?> screen) {
                var term = (IExtendedPatternEncodingTerm) screen;
                var listBox = term.gto$getPatternDestDisplay();
                listBox.reset();
                listBox.setVisible(true);
                listBox.setX(200);
                listBox.setY(screen.getYSize() - 100);
                for (int i = 0; i < destinations.length; i++) {
                    var dest = destinations[i];
                    listBox.addPatternContainerGroup(dest.group(), i, dest.full());
                }
            }
        }

        public static void addToast(TechNode node, boolean isUnlock) {
            Minecraft.getInstance().getToasts().addToast(new TechNodeToast(node, isUnlock ? TechNodeToast.Type.FINISHED_RESEARCH : TechNodeToast.Type.EUREKA_GAINED));
        }
    }
}
