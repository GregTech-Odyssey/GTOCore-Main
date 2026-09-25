package com.gtocore.client;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechNodeToast;
import com.gtocore.api.research.techtree.ui.TechTreeBrowser;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.AEKey;
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

    public static final NetworkPack OPEN_TECH_TREE_C2S = NetworkPack.registerC2S("openTechTreeC2S", (p, b) -> TechTreeBrowser.open(p, b.readVarInt()));

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

    // 样板产物包里所有 AEKey 的字节预算；超出后其余目的地不再携带产物，保证远低于自定义包 1 MiB 的上限
    private static final int PATTERN_OUTPUTS_BYTE_BUDGET = 512 * 1024;

    public static final NetworkPack SEND_PATTERN_DESTINATION_S2C = NetworkPack.registerS2C("sendPatternDestinationS2C", (p, b) -> {
        var requestId = b.readVarInt();
        var size = b.readVarInt();
        var destinations = new PatternDestination[size];
        for (int i = 0; i < size; i++) {
            var group = PatternContainerGroup.readFromPacket(b);
            var customName = b.readBoolean() ? b.readComponent() : null;
            AEKey providerIcon = null;
            if (b.readBoolean()) {
                providerIcon = AEKey.readKey(b);
                // 未知的 key 类型不会读掉 key 本体，之后的数据已经错位，整包丢弃
                if (providerIcon == null) return;
            }
            var full = b.readBoolean();
            destinations[i] = new PatternDestination(group, customName, providerIcon, full, b.readBoolean());
        }
        Client.patternDestinationReceived(requestId, destinations);
    });

    public static final NetworkPack SEND_PATTERN_OUTPUTS_S2C = NetworkPack.registerS2C("sendPatternOutputsS2C", (p, b) -> {
        var requestId = b.readVarInt();
        var size = b.readVarInt();
        var outputs = new AEKey[size][];
        for (int i = 0; i < size; i++) {
            var keys = new AEKey[b.readInt()];
            for (int j = 0; j < keys.length; j++) {
                keys[j] = AEKey.readKey(b);
                // 同上：数据已错位，整包丢弃
                if (keys[j] == null) return;
            }
            outputs[i] = keys;
        }
        Client.patternOutputsReceived(requestId, outputs);
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

    /**
     * @param requestId 本次目的地列表的编号；客户端发送样板、请求产物时带回，服务端据此拒绝过期的请求
     */
    public static void sendPatternDestination(ServerPlayer player, int requestId, PatternDestination[] destinations) {
        SEND_PATTERN_DESTINATION_S2C.send(buf -> {
            buf.writeVarInt(requestId);
            buf.writeVarInt(destinations.length);
            for (var dest : destinations) {
                dest.group().writeToPacket(buf);
                buf.writeBoolean(dest.customName() != null);
                if (dest.customName() != null) buf.writeComponent(dest.customName());
                buf.writeBoolean(dest.providerIcon() != null);
                if (dest.providerIcon() != null) AEKey.writeKey(buf, dest.providerIcon());
                buf.writeBoolean(dest.full());
                buf.writeBoolean(dest.hasSamePattern());
            }
        }, player);
    }

    /**
     * 按需下发各目的地已有样板的产物（与目的地列表一一对应）。所有产物共用 {@link #PATTERN_OUTPUTS_BYTE_BUDGET}，
     * 超出预算后剩余目的地的产物被截断为空。
     */
    public static void sendPatternOutputs(ServerPlayer player, int requestId, AEKey[][] outputs) {
        SEND_PATTERN_OUTPUTS_S2C.send(buf -> {
            buf.writeVarInt(requestId);
            buf.writeVarInt(outputs.length);
            int budgetEnd = buf.writerIndex() + PATTERN_OUTPUTS_BYTE_BUDGET;
            for (var keys : outputs) {
                // 先占位，写完再回填实际写入的个数
                int countIndex = buf.writerIndex();
                buf.writeInt(0);
                int written = 0;
                for (var key : keys) {
                    if (buf.writerIndex() >= budgetEnd) break;
                    AEKey.writeKey(buf, key);
                    written++;
                }
                buf.setInt(countIndex, written);
            }
        }, player);
    }

    /**
     * @param group          对接机器的分组（图标 + 名称），忽略目的地的普通改名
     * @param customName     目的地被普通改名时的名字，未改名为 null
     * @param providerIcon   目的地本体（样板供应器等）的图标，null 表示不单独显示
     * @param hasSamePattern 目的地（集群则为整个集群）已有与本次样板主产物相同的样板；列表中置灰，仍可发送
     */
    public record PatternDestination(PatternContainerGroup group, @Nullable Component customName, @Nullable AEKey providerIcon,
                                     boolean full, boolean hasSamePattern) {}

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

        public static void patternDestinationReceived(int requestId, PatternDestination[] destinations) {
            if (Minecraft.getInstance().screen instanceof PatternEncodingTermScreen<?> screen) {
                ((IExtendedPatternEncodingTerm) screen).gto$getPatternDestDisplay().open(requestId, destinations);
            }
        }

        public static void patternOutputsReceived(int requestId, AEKey[][] outputs) {
            if (Minecraft.getInstance().screen instanceof PatternEncodingTermScreen<?> screen) {
                ((IExtendedPatternEncodingTerm) screen).gto$getPatternDestDisplay().setOutputs(requestId, outputs);
            }
        }

        public static void addToast(TechNode node, boolean isUnlock) {
            Minecraft.getInstance().getToasts().addToast(new TechNodeToast(node, isUnlock ? TechNodeToast.Type.FINISHED_RESEARCH : TechNodeToast.Type.EUREKA_GAINED));
        }
    }
}
