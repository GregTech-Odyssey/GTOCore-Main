package com.gtocore.client.overlay;

import com.gtocore.client.renderer.RenderHelper;
import com.gtocore.common.item.MEWirelessMachineConfigurator;
import com.gtocore.integration.ae.wireless.WirelessClientCache;
import com.gtocore.integration.ae.wireless.WirelessMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.gtocore.common.data.GTOMachines.ME_WIRELESS_CONNECTION_MACHINE;
import static com.hepdd.gtmthings.data.CustomMachines.ME_EXPORT_BUFFER;

/**
 * 手持无线机器或配置器时，高亮 64 格内已加入无线网络的机器并标出网络名。
 * <p>
 * 机器的网络 id 来自机器同步到客户端的字段，网络名来自按玩家过滤的 {@link WirelessClientCache}——
 * 只显示本玩家可访问的网络。每 {@link #SCAN_INTERVAL} tick 重新扫描一次附近已加载区块的方块实体。
 */
@OnlyIn(Dist.CLIENT)
public class WirelessAEClientHandler {

    private static final int RADIUS = 64;
    private static final int SCAN_INTERVAL = 10;
    private static final ReferenceSet<MachineDefinition> WIRELESS_MACHINE_DEFINITIONS = new ReferenceOpenHashSet<>();

    private record Marker(BlockPos pos, String networkId) {}

    private static List<Marker> markers = List.of();
    private static int lastScan = Integer.MIN_VALUE;

    public static void highlightMachines(Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var level = minecraft.level;
        if (player == null || level == null) return;
        int now = GTValues.CLIENT_TIME;
        if (now - lastScan >= SCAN_INTERVAL || now < lastScan) {
            markers = scan(level, player.blockPosition());
            lastScan = now;
        }
        var held = player.getMainHandItem();
        var emphasized = MEWirelessMachineConfigurator.isConfigurator(held) ? MEWirelessMachineConfigurator.getNetworkId(held) : WirelessClientCache.favorite();
        for (var marker : markers) {
            var name = WirelessClientCache.name(marker.networkId());
            if (name == null) continue;
            var color = getGridColor(marker.networkId(), marker.networkId().equals(emphasized));
            RenderHelper.highlightBlock(camera, poseStack, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 4, marker.pos(), marker.pos());
            RenderHelper.renderSeeThroughText(camera, poseStack, marker.pos(), color.getRGB(), name, bufferSource);
        }
    }

    private static List<Marker> scan(ClientLevel level, BlockPos center) {
        var result = new ArrayList<Marker>();
        int chunkRadius = (RADIUS >> 4) + 1;
        int centerX = center.getX() >> 4, centerZ = center.getZ() >> 4;
        for (int cx = centerX - chunkRadius; cx <= centerX + chunkRadius; cx++) {
            for (int cz = centerZ - chunkRadius; cz <= centerZ + chunkRadius; cz++) {
                var chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (var blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof MetaMachineBlockEntity machineEntity) ||
                            !(machineEntity.getMetaMachine() instanceof WirelessMachine machine)) {
                        continue;
                    }
                    var id = machine.getWirelessNetworkId();
                    var pos = blockEntity.getBlockPos();
                    if (!id.isEmpty() && pos.distSqr(center) <= RADIUS * RADIUS) result.add(new Marker(pos, id));
                }
            }
        }
        return result;
    }

    public static boolean shouldHighlight() {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        var heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof MetaMachineItem item && WIRELESS_MACHINE_DEFINITIONS.contains(item.getDefinition())) return true;
        return MEWirelessMachineConfigurator.isConfigurator(heldItem);
    }

    private static Color getGridColor(String networkId, boolean emphasized) {
        float hue = Math.floorMod(networkId.hashCode(), 360) / 360f;
        float wave = (float) Math.sin(System.currentTimeMillis() / 200.0);
        float brightness = 0.8f + (emphasized ? wave * 0.2f : 0);
        float saturation = 0.4f + (emphasized ? -wave * 0.4f : 0.4f);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    static {
        WIRELESS_MACHINE_DEFINITIONS.add(ME_EXPORT_BUFFER);
        WIRELESS_MACHINE_DEFINITIONS.add(ME_WIRELESS_CONNECTION_MACHINE);
    }
}
