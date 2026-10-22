package com.gtocore.client.overlay;

import com.gtocore.eio_travel.api.ITravelTarget;
import com.gtocore.eio_travel.api.TravelRegistry;
import com.gtocore.eio_travel.api.TravelTargetType;
import com.gtocore.eio_travel.client.travel.TeammateHeadRenderer;
import com.gtocore.eio_travel.logic.TravelHandler;
import com.gtocore.eio_travel.logic.TravelSavedData;
import com.gtocore.eio_travel.logic.TravelUtils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.suried.gtoutils.spatial.BlockRange;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TravelTargetsClientHandler {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(TravelTargetsClientHandler::renderLevel);
    }

    public static void renderLevel(RenderLevelStageEvent event) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        if (!TravelHandler.canTeleport(player)) {
            return;
        }

        boolean itemTeleport = TravelHandler.canItemTeleport(player);
        TravelSavedData data = TravelSavedData.getTravelData(Minecraft.getInstance().level);
        @Nullable
        ITravelTarget anchorTarget = TravelHandler.getTeleportAnchorTarget(player).orElse(null);
        @Nullable
        Player activeTeammate = TravelHandler.getTeleportTeammateTarget(player, anchorTarget);
        @Nullable
        ITravelTarget activeTarget = activeTeammate == null ? anchorTarget : null;
        var targets = TravelUtils.filterTargets(player, data.getTravelTargetsWithin(player.blockPosition(), BlockRange.sphere(TravelHandler.maxTargetRange() + 2)).stream()).toList();
        for (ITravelTarget target : targets) {
            double range = itemTeleport ? target.getItem2BlockRange() : target.getBlock2BlockRange();
            double distanceSquared = target.getPos().distToCenterSqr(player.position());
            if (range * range < distanceSquared || distanceSquared < TravelHandler.MIN_TELEPORTATION_DISTANCE_SQUARED || TravelUtils.gto$isTeleportPositionAndSurroundingClear(level, target.getPos()).isEmpty()) {
                continue;
            }

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 projectedView = mainCamera.getPosition();
            poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

            boolean active = activeTarget == target;
            TravelRegistry.getRenderer(target).render(target, event.getLevelRenderer(), poseStack, distanceSquared, active);
            poseStack.popPose();
        }

        if (!itemTeleport || !TravelTargetType.TEAMMATE.isEnabled(TravelHandler.getHeldStaff(player))) {
            return;
        }
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        UUID team = TeamUtil.getTeamUUID(player.getUUID());
        for (AbstractClientPlayer other : level.players()) {
            if (!TravelHandler.isTeammateTarget(player, team, other)) {
                continue;
            }
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
            TeammateHeadRenderer.render(other, poseStack, event.getPartialTick(), other.distanceToSqr(player), other == activeTeammate);
            poseStack.popPose();
        }
    }
}
