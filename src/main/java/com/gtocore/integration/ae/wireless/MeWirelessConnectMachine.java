package com.gtocore.integration.ae.wireless;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

/**
 * ME 无线连接机：放在 AE 线缆旁，把所在 ME 网络接入一个无线网络（或从无线网络取得 ME 网络）。
 * 同一无线网络里的所有机器经网络的虚拟 hub 并入同一个 AE 网格，不再区分源节点/子节点。
 * <p>
 * 存档键：{@code _connectedNetworkId} 沿用旧版（Kotlin 实现）不变；旧版的 {@code _nodeType} 不再读写（DataSyncLib 忽略多余键）。
 */
public class MeWirelessConnectMachine extends MetaMachine implements WirelessMachine, IMachineLife, IFancyUIMachine {

    private final GridNodeHolder gridHolder = new GridNodeHolder(this);

    @SyncToClient
    private boolean gridOnline;

    @SaveToDisk(key = "_connectedNetworkId", defaultValue = "")
    @SyncToClient
    private String wirelessNetworkId = "";

    public MeWirelessConnectMachine(MetaMachineBlockEntity holder) {
        super(holder);
        // 连接机不占频道（只作致密线缆承载）
        getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
    }

    // ==================== AE ====================

    @Override
    public boolean isOnline() {
        return gridOnline;
    }

    @Override
    public void setOnline(boolean online) {
        gridOnline = online;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return gridHolder.getMainNode();
    }

    // ==================== 无线 ====================

    @Override
    public String getWirelessNetworkId() {
        return wirelessNetworkId;
    }

    @Override
    public void setWirelessNetworkId(String id) {
        wirelessNetworkId = id;
    }

    /**
     * 最早一代存档（LDLib {@code @Persisted}）把网络存在根 NBT 的 {@code wirelessMachinePersisted.gridName}。
     * 此方法先于字段读取执行；新存档没有这个键，已有 {@code _connectedNetworkId} 时字段读取会覆盖这里的值。
     */
    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (wirelessNetworkId.isEmpty() && tag.contains("wirelessMachinePersisted")) {
            var gridName = tag.getCompound("wirelessMachinePersisted").getString("gridName");
            if (!gridName.isEmpty()) wirelessNetworkId = gridName;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onWirelessLoad();
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        IMachineLife.super.onMachinePlaced(player, stack);
        onWirelessPlaced(player, stack);
    }

    // ==================== 界面 ====================

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(UISizes.WINDOW_WIDTH, UISizes.WINDOW_WIDTH, this, entityPlayer).widget(new MachineWindow(this));
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return WirelessMachineUI.createPage(this, widget);
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
    }
}
