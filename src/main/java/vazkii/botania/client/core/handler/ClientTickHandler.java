package vazkii.botania.client.core.handler;

import net.minecraft.client.Minecraft;

/**
 * 客户端计时器：提供游戏内 tick 数与插值时间，供动画着色与渲染使用。
 */
public final class ClientTickHandler {

    private ClientTickHandler() {}

    public static int ticksWithLexicaOpen = 0;
    public static int pageFlipTicks = 0;
    public static int ticksInGame = 0;
    public static float partialTicks = 0;

    public static float total() {
        return ticksInGame + partialTicks;
    }

    public static void renderTick(float renderTickTime) {
        partialTicks = renderTickTime;
    }

    public static void clientTickEnd(Minecraft mc) {
        if (mc.level == null) {
            return;
        }
        if (!mc.isPaused()) {
            ticksInGame++;
            partialTicks = 0;
        }
    }

    public static void notifyPageChange() {
        if (pageFlipTicks == 0) {
            pageFlipTicks = 5;
        }
    }
}
