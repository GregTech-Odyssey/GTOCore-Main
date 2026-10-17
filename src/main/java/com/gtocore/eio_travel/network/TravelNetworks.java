package com.gtocore.eio_travel.network;

import com.gtocore.common.data.GTOItems;
import com.gtocore.eio_travel.api.ITravelTarget;
import com.gtocore.eio_travel.api.TravelRegistry;
import com.gtocore.eio_travel.logic.TravelHandler;
import com.gtocore.eio_travel.logic.TravelSavedData;

import com.gtolib.api.network.NetworkPack;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import com.google.common.base.Predicates;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;

import java.util.Optional;

public class TravelNetworks {

    private static final NetworkPack ADD_TRAVEL = NetworkPack.registerS2C(
            "eio_travel:add_travel",
            (player, buf) -> TravelRegistry.deserialize(buf.readNbt()).ifPresent(TravelNetworks::handleAddTravel));

    private static final NetworkPack SYNC_TRAVEL = NetworkPack.registerS2C(
            "eio_travel:sync_travel",
            (player, buf) -> {
                CompoundTag nbt = buf.readNbt();
                TravelSavedData travelData = TravelSavedData.getTravelData(GTUtil.getClientLevel());
                travelData.loadNBT(nbt);
            });

    private static final NetworkPack REMOVE_TRAVEL = NetworkPack.registerS2C(
            "eio_travel:remove_travel",
            (player, buf) -> {
                TravelSavedData travelData = TravelSavedData.getTravelData(GTUtil.getClientLevel());
                travelData.removeTravelTargetAt(GTUtil.getClientLevel(), buf.readBlockPos());
            });

    private static final NetworkPack REQUEST_TRAVEL = NetworkPack.registerC2S(
            "eio_travel:request_travel",
            TravelNetworks::handleRequest);

    private static final NetworkPack OPEN_STAFF_UI = NetworkPack.registerC2S(
            "eio_travel:open_staff_ui",
            (player, buf) -> {
                if (player != null && !player.isSpectator() && player.containerMenu == player.inventoryMenu && player.getMainHandItem().is(GTOItems.TRAVEL_STAFF.asItem())) {
                    HeldItemUIFactory.INSTANCE.openUI(player, InteractionHand.MAIN_HAND);
                }
            });

    public static void onServerTravelAdded(ServerLevel level, ITravelTarget target) {
        ADD_TRAVEL.send(b -> b.writeNbt(target.save()), level.players());
    }

    public static void syncTravelData(CompoundTag nbt, ServerPlayer player) {
        SYNC_TRAVEL.send(b -> b.writeNbt(nbt), player);
    }

    public static void syncTravelData(CompoundTag nbt, ServerLevel level) {
        SYNC_TRAVEL.send(b -> b.writeNbt(nbt), level.getPlayers(Predicates.alwaysTrue()));
    }

    public static void onServerTravelRemoved(ServerLevel level, BlockPos pos) {
        REMOVE_TRAVEL.send(b -> b.writeBlockPos(pos), level.players());
    }

    public static void requestStaffUI() {
        OPEN_STAFF_UI.send();
    }

    public static void requestTravel(BlockPos pos) {
        REQUEST_TRAVEL.send(b -> b.writeBlockPos(pos));
    }

    private static void handleAddTravel(ITravelTarget target) {
        TravelSavedData travelData = TravelSavedData.getTravelData(GTUtil.getClientLevel());
        travelData.addTravelTarget(GTUtil.getClientLevel(), target);
    }

    private static void handleRequest(ServerPlayer player, FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();

        if (player == null) {
            return;
        }

        TravelSavedData travelData = TravelSavedData.getTravelData(player.level());
        Optional<ITravelTarget> target = travelData.getTravelTarget(pos);

        // These errors should only ever be triggered if there's some form of desync
        if (!TravelHandler.canBlockTeleport(player)) {
            player.displayClientMessage(Component.nullToEmpty("ERROR: Cannot teleport"), true);
            return;
        }
        if (target.isEmpty()) {
            player.displayClientMessage(Component.nullToEmpty("ERROR: Destination not a valid target"), true);
            return;
        }
        // Eventually change the packet structure to include what teleport method was used so this range can be selected
        // correctly
        int range = Math.max(target.get().getBlock2BlockRange(), target.get().getItem2BlockRange());
        if (pos.distSqr(player.getOnPos()) > range * range) {
            player.displayClientMessage(Component.nullToEmpty("ERROR: Too far"), true);
            return;
        }

        TravelHandler.blockTeleportAround(player.level(), player, target.get().getPos(), false);
    }
}
