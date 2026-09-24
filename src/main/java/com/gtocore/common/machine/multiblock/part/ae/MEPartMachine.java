package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.integration.ae.wireless.WirelessMachine;

import com.gtolib.api.machine.feature.IMEPartMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * ME 部件机器基类（ME 总线、样板供应器等），可加入 GTO 无线 ME 网络（{@link WirelessMachine}）。
 * <p>
 * 存档键：所有 {@link SaveToDisk} 字段都显式写明 {@code key}，取值与改写成 Java 前的 Kotlin 属性名一致，
 * 字段改名不影响已有存档；新增字段请使用新键，不要复用这些键。
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MEPartMachine extends WorkableTieredIOPartMachine implements WirelessMachine, IMEPartMachine, IDistinctPart, IMachineLife {

    /// ME 输入总线/仓的配置格数：界面每行 9 格、两行（2026-09 由 16 改为 18）。
    /// 旧存档是 16 格：配置/库存数组按下标读入、多出的格子留空（DataSyncLib 数组读取取两者较短的长度），无需额外迁移；
    /// 数据棒复制的旧配置同理，缺的键视为空
    public static final int CONFIG_SIZE = 18;

    // ==================== AE2 Grid ====================
    @SaveToDisk(key = "nodeHolder")
    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);

    @SyncToClient
    private boolean onlineField;

    private final IActionSource actionSourceField = IActionSource.ofMachine(() -> nodeHolder.getMainNode().getNode());

    @SaveToDisk(key = "distinctField", defaultValue = "false")
    protected boolean distinctField;

    /// 是否所有面都可连接 AE 线缆（剪线钳切换）；否则只有正面可连
    @SaveToDisk(key = "isAllFacing", defaultValue = "false")
    private boolean allFacing;

    // ==================== WirelessMachine - Persisted State ====================
    @SaveToDisk(key = "_connectedNetworkId", defaultValue = "")
    @SyncToClient
    private String connectedNetworkId = "";

    protected MEPartMachine(MetaMachineBlockEntity holder, IO io) {
        super(holder, GTValues.LuV, io);
    }

    @Override
    public @Nullable ICustomItemStackHandler getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Override
    public @Nullable ICustomFluidStackHandler getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Override
    public int tintColor(int index) {
        return index == 9 ? getRealColor() : -1;
    }

    // ==================== 剪线钳切换连接面 ====================

    @Override
    public Pair<GTToolType, InteractionResult> onToolClick(Set<GTToolType> toolType, ItemStack itemStack, UseOnContext context) {
        var result = super.onToolClick(toolType, itemStack, context);
        if (result.getSecond() == InteractionResult.PASS && toolType.contains(GTToolType.WIRE_CUTTER)) {
            var player = context.getPlayer();
            if (player == null) return result;
            return Pair.of(GTToolType.WIRE_CUTTER, onWireCutterClick(player, context.getHand()));
        }
        return result;
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held, Set<GTToolType> toolTypes) {
        return super.shouldRenderGrid(player, pos, state, held, toolTypes) || toolTypes.contains(GTToolType.WIRE_CUTTER);
    }

    private InteractionResult onWireCutterClick(Player player, InteractionHand hand) {
        player.swing(hand);
        if (allFacing) {
            getMainNode().setExposedOnSides(EnumSet.of(getFrontFacing()));
            if (isRemote()) player.displayClientMessage(Component.translatable("gtocore.me_front"), true);
            allFacing = false;
        } else {
            getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
            if (isRemote()) player.displayClientMessage(Component.translatable("gtocore.me_any"), true);
            allFacing = true;
        }
        return InteractionResult.CONSUME;
    }

    public boolean isAllFacing() {
        return allFacing;
    }

    public void setAllFacing(boolean allFacing) {
        this.allFacing = allFacing;
    }

    // ==================== WirelessMachine ====================

    @Override
    public String getWirelessNetworkId() {
        return connectedNetworkId;
    }

    @Override
    public void setWirelessNetworkId(String id) {
        this.connectedNetworkId = id;
    }

    // ==================== 旧存档迁移 ====================

    /**
     * LDLib 时代用 {@code @Persisted wirelessMachinePersisted} 保存无线连接，NBT 形如
     * {@code wirelessMachinePersisted: {gridName: "...", beSet: true/false}}。
     * 字段读取前先把旧的 {@code gridName} 搬进 {@link #connectedNetworkId}，已放置的机器不会丢连接。
     */
    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (connectedNetworkId.isEmpty() && tag.contains("wirelessMachinePersisted")) {
            var oldGridName = tag.getCompound("wirelessMachinePersisted").getString("gridName");
            if (!oldGridName.isEmpty()) connectedNetworkId = oldGridName;
        }
    }

    // ==================== Lifecycle ====================

    @Override
    public void onLoad() {
        super.onLoad();
        if (allFacing) getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        if (isRemote()) return;
        onWirelessLoad();
        var handlerUnit = getHandlerUnit();
        handlerUnit.isDistinct = distinctField;
        handlerUnit.color = getPaintingColor();
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        IMachineLife.super.onMachinePlaced(player, stack);
        onWirelessPlaced(player, stack);
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    @Override
    public boolean isDistinct() {
        return distinctField;
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        this.distinctField = isDistinct;
        getHandlerUnit().setDistinctAndNotify(isDistinct);
    }

    public boolean getOnlineField() {
        return onlineField;
    }

    public void setOnlineField(boolean onlineField) {
        this.onlineField = onlineField;
    }

    public IActionSource getActionSourceField() {
        return actionSourceField;
    }

    @Override
    public void setOnline(boolean isOnline) {
        this.onlineField = isOnline;
    }

    @Override
    public boolean isOnline() {
        return onlineField;
    }

    @Override
    public IActionSource getActionSource() {
        return actionSourceField;
    }

    // ==================== GUI ====================

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer).widget(new MachineWindow(this));
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(getWirelessUIProvider());
    }
}
