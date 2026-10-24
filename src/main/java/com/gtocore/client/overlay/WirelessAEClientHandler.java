package com.gtocore.client.overlay;

import com.gtocore.client.renderer.RenderHelper;
import com.gtocore.common.data.GTOBlockEntityIndexes;
import com.gtocore.common.item.MEWirelessMachineConfigurator;
import com.gtocore.integration.ae.wireless.WirelessClientCache;
import com.gtocore.integration.ae.wireless.WirelessMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.suried.gtoutils.spatial.BlockRange;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.awt.*;

import static com.gtocore.common.data.GTOMachines.ME_WIRELESS_CONNECTION_MACHINE;
import static com.hepdd.gtmthings.data.CustomMachines.ME_EXPORT_BUFFER;

/**
 * 手持无线机器或配置器时，高亮 64 格内已加入无线网络的机器并标出网络名。
 * <p>
 * 机器的网络 id 来自机器同步到客户端的字段，网络名来自按玩家过滤的 {@link WirelessClientCache}——
 * 只显示本玩家可访问的网络。每帧读取机器的最新网络 id。
 */
@OnlyIn(Dist.CLIENT)
public class WirelessAEClientHandler {

    private static final int RADIUS = 64;
    private static final ReferenceOpenHashSet<MachineDefinition> WIRELESS_MACHINE_DEFINITIONS = new ReferenceOpenHashSet<>();
    private static final ObjectArrayList<MetaMachineBlockEntity> FOUND = new ObjectArrayList<>();

    public static void highlightMachines(Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var level = minecraft.level;
        if (player == null || level == null) return;
        var center = player.blockPosition();
        var held = player.getMainHandItem();
        var emphasized = MEWirelessMachineConfigurator.isConfigurator(held) ? MEWirelessMachineConfigurator.getNetworkId(held) : WirelessClientCache.favorite();
        GTOBlockEntityIndexes.CLIENT_WIRELESS_MACHINES.query(level, center, BlockRange.sphere(RADIUS), null, FOUND);
        try {
            for (int i = 0; i < FOUND.size(); i++) {
                var machineEntity = FOUND.get(i);
                var pos = machineEntity.getBlockPos();
                var id = ((WirelessMachine) machineEntity.getMetaMachine()).getWirelessNetworkId();
                if (id.isEmpty()) continue;
                var name = WirelessClientCache.name(id);
                if (name == null) continue;
                int color = getGridColor(id, id.equals(emphasized));
                RenderHelper.highlightBlock(camera, poseStack, (color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, 4, pos, pos);
                RenderHelper.renderSeeThroughText(camera, poseStack, pos, color, name, bufferSource);
            }
        } finally {
            FOUND.clear();
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
