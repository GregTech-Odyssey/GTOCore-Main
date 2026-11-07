package vazkii.botania.common.alfheim;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 精灵门停留计时：在门内停留满 120 tick 后放行。 */
public final class AlfheimPortalHandler {

    private static final int PORTAL_TIME = 120;

    private static final Set<ServerPlayer> IN_PORTAL = new HashSet<>();
    private static final Set<ServerPlayer> PORTAL_BLOCKED = new HashSet<>();
    private static final Map<ServerPlayer, Integer> TIMES_IN_PORTAL = new HashMap<>();

    private AlfheimPortalHandler() {}

    /** 玩家位于门内时每 tick 调用，累计到阈值返回 true。 */
    public static boolean setInPortal(Level level, Player player) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        IN_PORTAL.add(serverPlayer);
        if (PORTAL_BLOCKED.contains(serverPlayer) || player.isOnPortalCooldown()) {
            return false;
        }
        int timeInPortal = TIMES_IN_PORTAL.merge(serverPlayer, 1, Integer::sum);
        if (timeInPortal >= PORTAL_TIME) {
            PORTAL_BLOCKED.add(serverPlayer);
            return true;
        }
        return false;
    }

    public static boolean shouldCheck(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return !TIMES_IN_PORTAL.isEmpty() || !PORTAL_BLOCKED.isEmpty() || serverLevel.getServer().getTickCount() % 4 == 3;
    }

    // ==================== 客户端

    public static int clientInPortalTime;

    /** 客户端每 tick 根据玩家所在方块更新门内计时。 */
    public static void clientTick(net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.world.entity.player.Player player) {
        if (level == null || player == null) {
            return;
        }
        var state = level.getBlockState(player.blockPosition());
        boolean inPortal = state.is(vazkii.botania.common.BotaniaBlocks.ALFHEIM_PORTAL.get()) && state.getValue(vazkii.botania.common.block.ReplicaVisualBlocks.PORTAL_STATE) != vazkii.botania.common.block.ReplicaVisualBlocks.PortalState.OFF;
        clientInPortalTime = inPortal ? Math.min(120, clientInPortalTime + 1) : Math.max(0, clientInPortalTime - 2);
    }

    /** 客户端绘制全屏旋涡，透明度随计时上升。 */
    public static void renderOverlay(net.minecraft.client.gui.GuiGraphics gui) {
        if (clientInPortalTime <= 0) {
            return;
        }
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) {
            return;
        }
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        float scale = Math.max(width, height) / 48F;
        gui.pose().pushPose();
        gui.pose().translate(width < height ? (height - width) / -2 : 0, width > height ? (width - height) / -2 : 0, 0);
        gui.pose().scale(scale, scale, 1);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        float alpha = net.minecraft.util.Mth.clamp((clientInPortalTime + mc.getFrameTime()) / 120F, 0.05F, 0.8F);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1, 1, 1, alpha);
        var sprite = mc.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS)
                .apply(new net.minecraft.resources.ResourceLocation("botania", "block/alfheim_portal_swirl"));
        gui.blit(0, 0, 0, 48, 48, sprite);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1, 1, 1, 1);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        gui.pose().popPose();
    }

    /** 每 tick 结束时清理已离开门的玩家状态。 */
    public static void endTick() {
        IN_PORTAL.clear();
        TIMES_IN_PORTAL.keySet().removeIf(player -> !IN_PORTAL.contains(player));
        PORTAL_BLOCKED.removeIf(player -> !player.isOnPortalCooldown() && !IN_PORTAL.contains(player));
    }
}
