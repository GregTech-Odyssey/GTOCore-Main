package com.gtocore.eio_travel;

import com.gtocore.common.data.GTOItems;
import com.gtocore.eio_travel.logic.TravelHandler;
import com.gtocore.eio_travel.logic.TravelSavedData;
import com.gtocore.eio_travel.network.TravelNetworks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber
public class TravelEvents {

    @Nullable
    public static Runnable syncTask = null;

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().getMainHandItem().is(GTOItems.TRAVEL_STAFF.asItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            TravelNetworks.syncTravelData(TravelSavedData.getTravelData(serverPlayer.level()).save(new CompoundTag()), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            TravelNetworks.syncTravelData(TravelSavedData.getTravelData(serverPlayer.level()).save(new CompoundTag()), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && syncTask != null) {
            syncTask.run();
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class Client {

        private static long LAST_STAFF_UI_REQUEST = 0;
        private static boolean LAST_JUMPING = false;
        private static boolean LAST_SNEAKING = false;
        private static int JUMP_COOLDOWN = 0;

        @SubscribeEvent
        public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
            Player player = Minecraft.getInstance().player;
            if (!event.isAttack() || player == null || !player.getMainHandItem().is(GTOItems.TRAVEL_STAFF.asItem())) {
                return;
            }
            event.setCanceled(true);
            event.setSwingHand(false);
            long now = player.level().getGameTime();
            if (now - LAST_STAFF_UI_REQUEST >= 10) {
                LAST_STAFF_UI_REQUEST = now;
                TravelNetworks.requestStaffUI();
            }
        }

        @SubscribeEvent
        public static void movementInputUpdate(MovementInputUpdateEvent event) {
            Input input = event.getInput();
            Player player = event.getEntity();
            boolean isNewJump = input.jumping && !LAST_JUMPING;
            LAST_JUMPING = input.jumping;
            boolean isNewCrouch = input.shiftKeyDown && !LAST_SNEAKING;
            LAST_SNEAKING = input.shiftKeyDown;

            if (!player.onGround() || !TravelHandler.canBlockTeleport(player)) {
                JUMP_COOLDOWN = 0;
                return;
            }
            if (isNewJump) {
                boolean success = TravelHandler.blockElevatorTeleport(player.level(), player, Direction.UP, true);
                if (!success) {
                    success = TravelHandler.blockTeleport(player.level(), player, true);
                }
                if (success) {
                    JUMP_COOLDOWN = 7;
                } else {
                    JUMP_COOLDOWN = 0;
                }
            } else if (isNewCrouch) {
                boolean success = TravelHandler.blockElevatorTeleport(player.level(), player, Direction.DOWN, true);
                if (!success) {
                    TravelHandler.blockTeleport(player.level(), player, true);
                }
            }

            if (JUMP_COOLDOWN > 0) {
                JUMP_COOLDOWN -= 1;
                input.jumping = false;
            }
        }

        @SubscribeEvent
        public static void emptyClick(PlayerInteractEvent.RightClickEmpty event) {
            Player player = event.getEntity();
            // Credit to castcrafter/travel_anchors
            if (TravelHandler.canBlockTeleport(player) && !player.isShiftKeyDown() && event.getHand() == InteractionHand.MAIN_HAND && event
                    .getEntity()
                    .getItemInHand(InteractionHand.OFF_HAND)
                    .isEmpty() && event.getItemStack().isEmpty()) {
                if (TravelHandler.blockTeleport(event.getLevel(), event.getEntity(), true)) {
                    player.swing(event.getHand(), true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }

        @SubscribeEvent
        public static void blockClick(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();
            if (!TravelHandler.canBlockTeleport(player) || TravelHandler.isAimingAtTeammate(player)) {
                return;
            }
            if (TravelHandler.blockTeleport(event.getLevel(), event.getEntity(), true)) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void itemClick(PlayerInteractEvent.RightClickItem event) {
            Player player = event.getEntity();
            if (!TravelHandler.canBlockTeleport(player) || TravelHandler.isAimingAtTeammate(player)) {
                return;
            }
            if (TravelHandler.blockTeleport(event.getLevel(), event.getEntity(), true)) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}
