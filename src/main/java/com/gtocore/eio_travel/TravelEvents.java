package com.gtocore.eio_travel;

import com.gtocore.common.data.GTOItems;
import com.gtocore.eio_travel.logic.TravelHandler;
import com.gtocore.eio_travel.logic.TravelSavedData;
import com.gtocore.eio_travel.network.TravelNetworks;

import com.gtolib.api.scope.GTOScopes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.suried.gtoutils.schedule.Scope;
import com.suried.gtoutils.schedule.ScopeKey;

public class TravelEvents {

    private static final ScopeKey<Scope.Trigger> RESYNC = ScopeKey.of("eio_travel_resync");

    public static void init() {
        // 两端启动时就注册旅行网络通道，避免专用服务器客户端在登录同步到达时尚未加载 TravelNetworks。
        TravelNetworks.init();
        MinecraftForge.EVENT_BUS.addListener(TravelEvents::onLeftClickBlock);
    }

    public static void install() {
        GTOScopes.PRESENCE.install("eio_travel", (scope, player) -> TravelNetworks.syncTravelData(TravelSavedData.getTravelData(player.serverLevel()).save(new CompoundTag()), player));
        GTOScopes.LEVEL.install("eio_travel", (scope, level) -> scope.provide(RESYNC, scope.trigger(0, () -> {
            if (!level.players().isEmpty()) TravelNetworks.syncTravelData(TravelSavedData.getTravelData(level).save(new CompoundTag()), level);
        })));
    }

    public static void requireResync(ServerLevel level) {
        Scope.Trigger resync = GTOScopes.LEVEL.lookup(level, RESYNC);
        if (resync != null) resync.fire();
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isHeldUiStaff(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** 旅行手杖与维度权杖都用左键打开手持物品界面，左键方块时不能顺带挖掘。 */
    private static boolean isHeldUiStaff(Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.is(GTOItems.TRAVEL_STAFF.asItem()) || stack.is(GTOItems.DIMENSION_SCEPTER.asItem());
    }

    public static class Client {

        private static long LAST_STAFF_UI_REQUEST = 0;
        private static boolean LAST_JUMPING = false;
        private static boolean LAST_SNEAKING = false;
        private static int JUMP_COOLDOWN = 0;

        public static void init() {
            MinecraftForge.EVENT_BUS.addListener(Client::onAttackKey);
            MinecraftForge.EVENT_BUS.addListener(Client::movementInputUpdate);
            MinecraftForge.EVENT_BUS.addListener(Client::emptyClick);
            MinecraftForge.EVENT_BUS.addListener(Client::blockClick);
            MinecraftForge.EVENT_BUS.addListener(Client::itemClick);
        }

        public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
            Player player = Minecraft.getInstance().player;
            if (!event.isAttack() || player == null || !isHeldUiStaff(player)) {
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
