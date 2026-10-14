package com.gtocore.client.overlay;

import com.gtocore.client.renderer.RenderHelper;
import com.gtocore.common.item.MEWirelessMachineConfigurator;
import com.gtocore.integration.ae.wireless.WirelessClientCache;
import com.gtocore.integration.ae.wireless.WirelessMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.datastream.DataComponentKey;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.awt.*;

import static com.gtocore.common.data.GTOMachines.ME_WIRELESS_CONNECTION_MACHINE;
import static com.hepdd.gtmthings.data.CustomMachines.ME_EXPORT_BUFFER;

/**
 * 手持无线机器或配置器时，高亮 64 格内已加入无线网络的机器并标出网络名。
 * <p>
 * 机器的网络 id 来自机器同步到客户端的字段，网络名来自按玩家过滤的 {@link WirelessClientCache}——
 * 只显示本玩家可访问的网络。每 {@link #SCAN_INTERVAL} tick 重新扫描一次附近已加载区块的方块实体，
 * 首次渲染或跨区块移动时立即扫描。缓存绑定客户端 Level，随世界卸载释放；每帧读取机器的最新网络 id。
 */
@OnlyIn(Dist.CLIENT)
public class WirelessAEClientHandler {

    private static final int RADIUS = 64;
    private static final int SCAN_INTERVAL = 10;
    private static final ReferenceOpenHashSet<MachineDefinition> WIRELESS_MACHINE_DEFINITIONS = new ReferenceOpenHashSet<>();
    private static final DataComponentKey<MachineCache> CACHE_KEY = DataComponentKey.createNoCodec("gtocore_wireless_highlight_cache");

    public static void highlightMachines(Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var level = minecraft.level;
        if (player == null || level == null) return;
        var cache = ILevel.getCapability(level, CACHE_KEY);
        if (cache == null) {
            cache = new MachineCache();
            ILevel.setCapability(level, CACHE_KEY, cache);
        }
        var center = player.blockPosition();
        int now = GTValues.CLIENT_TIME;
        if (cache.center == null || (long) now - cache.lastScan >= SCAN_INTERVAL || now < cache.lastScan ||
                center.getX() >> 4 != cache.center.getX() >> 4 || center.getZ() >> 4 != cache.center.getZ() >> 4) {
            cache.scan(level, center, now);
        }
        var held = player.getMainHandItem();
        var emphasized = MEWirelessMachineConfigurator.isConfigurator(held) ? MEWirelessMachineConfigurator.getNetworkId(held) : WirelessClientCache.favorite();
        for (int i = 0; i < cache.machines.size(); i++) {
            var machineEntity = cache.machines.get(i);
            if (machineEntity.isRemoved()) continue;
            var pos = machineEntity.getBlockPos();
            if (pos.distSqr(center) > RADIUS * RADIUS) continue;
            var id = ((WirelessMachine) machineEntity.getMetaMachine()).getWirelessNetworkId();
            if (id.isEmpty()) continue;
            var name = WirelessClientCache.name(id);
            if (name == null) continue;
            int color = getGridColor(id, id.equals(emphasized));
            RenderHelper.highlightBlock(camera, poseStack, (color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, 4, pos, pos);
            RenderHelper.renderSeeThroughText(camera, poseStack, pos, color, name, bufferSource);
        }
    }

    private static final class MachineCache {

        private final ObjectArrayList<MetaMachineBlockEntity> machines = new ObjectArrayList<>();
        private BlockPos center;
        private int lastScan;

        private void scan(ClientLevel level, BlockPos center, int now) {
            machines.clear();
            int chunkRadius = (RADIUS >> 4) + 1;
            int centerX = center.getX() >> 4, centerZ = center.getZ() >> 4;
            for (int cx = centerX - chunkRadius; cx <= centerX + chunkRadius; cx++) {
                for (int cz = centerZ - chunkRadius; cz <= centerZ + chunkRadius; cz++) {
                    var chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;
                    for (var blockEntity : chunk.getBlockEntities().values()) {
                        if (blockEntity instanceof MetaMachineBlockEntity machineEntity &&
                                machineEntity.getMetaMachine() instanceof WirelessMachine) {
                            machines.add(machineEntity);
                        }
                    }
                }
            }
            this.center = center;
            lastScan = now;
        }
    }

    public static boolean shouldHighlight() {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        var heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof MetaMachineItem item && WIRELESS_MACHINE_DEFINITIONS.contains(item.getDefinition())) return true;
        return MEWirelessMachineConfigurator.isConfigurator(heldItem);
    }

    private static int getGridColor(String networkId, boolean emphasized) {
        float hue = Math.floorMod(networkId.hashCode(), 360) / 360f;
        float wave = (float) Math.sin(System.currentTimeMillis() / 200.0);
        float brightness = 0.8f + (emphasized ? wave * 0.2f : 0);
        float saturation = 0.4f + (emphasized ? -wave * 0.4f : 0.4f);
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    static {
        WIRELESS_MACHINE_DEFINITIONS.add(ME_EXPORT_BUFFER);
        WIRELESS_MACHINE_DEFINITIONS.add(ME_WIRELESS_CONNECTION_MACHINE);
    }
}
