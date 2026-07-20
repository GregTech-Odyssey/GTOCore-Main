package com.gtocore.mixin.gtm.item;

import com.gtocore.mixin.ftbu.FTBUltimineClientAccessor;

import com.gregtechceu.gtceu.common.item.ColorSprayBehaviour;
import com.gregtechceu.gtceu.common.item.InfiniteSprayCanBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.List;

/**
 * FTB Ultimine chain paint for the infinite spray can.
 * <p>
 * Latency notes (large shapes):
 * <ul>
 *   <li>Client paints FTB highlight shape immediately (same set as server when possible)</li>
 *   <li>Server reuses FTB cache when it still covers the click; otherwise recomputes once</li>
 *   <li>Bulk {@link ColorSprayBehaviour#paintBlocks} skips per-block neighbor updates</li>
 * </ul>
 */
@Mixin(value = InfiniteSprayCanBehaviour.class, remap = false)
public class InfiniteSprayCanBehaviourMixin {

    @Redirect(
              method = "onItemUseFirst",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/common/item/ColorSprayBehaviour;paintArea(Lnet/minecraft/world/item/context/UseOnContext;Lnet/minecraft/world/item/DyeColor;ILcom/gregtechceu/gtceu/common/item/ColorSprayBehaviour$ChargeConsumer;)V"))
    private void gto$paintWithUltimine(UseOnContext context, @Nullable DyeColor color, int maxBlocks,
                                       ColorSprayBehaviour.ChargeConsumer charge) {
        Player player = context.getPlayer();
        if (player == null) {
            ColorSprayBehaviour.paintArea(context, color, maxBlocks, charge);
            return;
        }

        boolean chain = gto$isUltimineHeld(player);
        int chainLimit = Math.max(1, ConfigHolder.INSTANCE.tools.sprayCanChainLength);
        int limit = chain ? chainLimit : 1;

        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(origin);

        // Pipes / AE / IPaintable: GTM BFS is correct.
        if (be != null) {
            ColorSprayBehaviour.paintArea(context, color, limit, charge);
            return;
        }

        if (!chain) {
            ColorSprayBehaviour.paintArea(context, color, 1, charge);
            return;
        }

        // Client: paint the same FTB highlight set immediately (prediction).
        if (level.isClientSide) {
            Collection<BlockPos> clientShape = gto$clientShapeBlocks();
            if (clientShape != null && !clientShape.isEmpty()) {
                ColorSprayBehaviour.paintBlocks(level, clientShape, color, charge);
                return;
            }
            ColorSprayBehaviour.paintArea(context, color, limit, charge);
            return;
        }

        // Server: prefer existing Ultimine cache covering the click (skip expensive recompute).
        if (player instanceof ServerPlayer serverPlayer) {
            FTBUltiminePlayerData data = FTBUltimine.instance.getOrCreatePlayerData(serverPlayer);
            if (!gto$cacheCovers(data, origin)) {
                int max = Math.min(chainLimit, FTBUltimineServerConfig.getMaxBlocks(serverPlayer));
                Direction face = context.getClickedFace();
                data.updateBlocks(serverPlayer, origin, face, false, max);
            }
            if (data.hasCachedPositions()) {
                ColorSprayBehaviour.paintBlocks(level, data.cachedPositions(), color, charge);
                return;
            }
        }

        ColorSprayBehaviour.paintArea(context, color, limit, charge);
    }

    @Unique
    private static boolean gto$isUltimineHeld(Player player) {
        if (player instanceof ServerPlayer) {
            return FTBUltimine.instance.getOrCreatePlayerData(player).isPressed();
        }
        if (player.level().isClientSide && FTBUltimineClient.keyBinding != null) {
            return FTBUltimineClient.keyBinding.isDown();
        }
        return false;
    }

    @Unique
    private static boolean gto$cacheCovers(FTBUltiminePlayerData data, BlockPos origin) {
        if (!data.hasCachedPositions()) {
            return false;
        }
        for (BlockPos pos : data.cachedPositions()) {
            if (origin.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    @Nullable
    private static Collection<BlockPos> gto$clientShapeBlocks() {
        if (!(FTBUltimine.instance.proxy instanceof FTBUltimineClient client)) {
            return null;
        }
        List<BlockPos> shape = ((FTBUltimineClientAccessor) (Object) client).gto$getShapeBlocks();
        if (shape == null || shape.isEmpty()) {
            return null;
        }
        return shape;
    }
}
