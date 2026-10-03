package com.gtocore.common.machine.noenergy;

import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.machine.multiblock.part.ae.IndexedStorages;

import com.gtolib.api.ae2.storage.CellDataStorage;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEItems;
import appeng.items.materials.StorageComponentItem;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * ME 磁盘箱子：ME 磁盘存储器的单方块版本。一个槽放存储组件（1k…256M，最多 {@link #COMPONENT_LIMIT} 个），
 * 组件字节之和就是容量；存储直接用存储访问仓那一套（{@link CellDataStorage} + 数据索引 UUID + 按字节卡容量），
 * 数据索引可以选玩家或机器，显示窗里还有一个「存储转移」按钮，
 * 口径与存储访问仓一致：把网络里其它 ME 存储的内容全部搬进本箱。
 */
@DataGeneratorScanned
public final class MEDiskBoxMachine extends MetaMachine
                                    implements IGridConnectedMachine, MEStorage, IStorageProvider, IStorageMultiblock, IFancyUIMachine, IDropSaveMachine {

    @RegisterLanguage(cn = "存储组件", en = "Storage Component")
    private static final String SLOT_LABEL = "gtocore.machine.me_disk_box.slot";

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor meStorageMonitor = holder.monitorTick(GTOTickTimeMonitors.ME_STORAGE, this::tickUpdate);

    /// 组件槽的数量上限
    public static final int COMPONENT_LIMIT = 64;
    /// 数据索引位置（与 ME 存储器共用同一套文案）
    private static final String MODE = "gtocore.machine.me_storage.mode";
    /// 存储转移的文案与存储访问仓共用，注册在 MachineLang
    private static final String TRANSFER = "gtocore.machine.storage_transfer";
    private static final String TRANSFER_TOOLTIP = "gtocore.machine.storage_transfer.tooltip";
    @RegisterLanguage(cn = "执行", en = "Run")
    private static final String TRANSFER_RUN = "gtocore.machine.me_disk_box.transfer_run";

    @RegisterLanguage(cn = "存储组件：%s 个，容量 %s", en = "Storage components: %s, capacity %s")
    public static final String COMPONENTS = "gtocore.machine.me_disk_box.components";
    @RegisterLanguage(cn = "放存储组件（1k…256M）来提供容量", en = "Put storage components (1k...256M) in to provide capacity")
    public static final String NO_COMPONENTS = "gtocore.machine.me_disk_box.no_components";

    /// 组件槽（1 格，最多 {@link #COMPONENT_LIMIT} 个存储组件）
    @SaveToDisk
    private final NotifiableStackInventory componentStorage;
    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    /// 机器模式下的数据索引；玩家模式用玩家的 UUID
    @SaveToDisk
    @Nullable
    private UUID uuid;
    @SaveToDisk(defaultValue = "false")
    private boolean player;
    @SyncToClient
    private boolean isOnline;
    private final ConditionalSubscriptionHandler tickSubs;

    /// 容量（组件字节之和）
    private double capacity;
    /// 本机存储数据（换数据索引后重新取）
    @Nullable
    private CellDataStorage dataStorage;
    private boolean dirty;
    private boolean observe;

    public MEDiskBoxMachine(MetaMachineBlockEntity holder) {
        super(holder);
        componentStorage = createMachineStorage(null);
        nodeHolder = new GridNodeHolder(this);
        getMainNode().addService(IStorageProvider.class, this);
        tickSubs = new ConditionalSubscriptionHandler(this, meStorageMonitor, 0, () -> true);
    }

    /// 组件槽就是 {@link IStorageMultiblock} 的机器存储槽
    @Override
    public NotifiableStackInventory getMachineStorage() {
        return componentStorage;
    }

    /// 组件槽只收存储组件
    @Override
    public boolean storageFilter(ItemStack stack) {
        return stack.getItem() instanceof StorageComponentItem;
    }

    @Override
    public int getSlotLimit() {
        return COMPONENT_LIMIT;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tickSubs.initialize(getLevel());
        refreshCapacity();
    }

    @Override
    public void onUnload() {
        tickSubs.unsubscribe();
        super.onUnload();
    }

    /// 组件增减：容量立刻跟着变
    @Override
    public void onMachineChanged() {
        if (isRemote()) return;
        refreshCapacity();
        onChanged();
    }

    private void refreshCapacity() {
        long total = 0;
        var storage = componentStorage.storage;
        for (int i = 0, slots = storage.getSlots(); i < slots; i++) {
            var stack = storage.getStackInSlot(i);
            long term = componentBytes(stack);
            if (term < 1) continue;
            total = term > Long.MAX_VALUE / stack.getCount() ? Long.MAX_VALUE : total + term * stack.getCount();
            if (total == Long.MAX_VALUE) break;
        }
        capacity = total;
    }

    /// 组件能提供的字节数；不是组件返回 0
    private static long componentBytes(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageComponentItem component)) return 0;
        return component.getBytes();
    }

    // ==================== 数据索引 ====================

    /// 当前数据索引：玩家模式取玩家的 UUID，机器模式取机器自己的 UUID
    @Nullable
    private UUID dataIndex() {
        return player ? getOwnerUUID() : uuid;
    }

    /// 取本机存储数据：和存储访问仓一样按 UUID 拿 CellDataStorage，拿过一次就缓存
    private CellDataStorage cellStorage() {
        if (dataStorage != null) return dataStorage;
        var index = dataIndex();
        if (index == null || isRemote()) return CellDataStorage.EMPTY;
        dataStorage = CellDataStorage.get(index);
        return dataStorage;
    }

    /// 切换玩家/机器索引：换索引后存储数据要重新取
    private void setPlayer(boolean value) {
        player = value;
        dataStorage = null;
        onChanged();
    }

    /// 机器模式要有自己的 UUID 才能存；没有就现生成一个
    private void ensureIndex() {
        if (!player && uuid == null) uuid = UUID.randomUUID();
    }

    /// 每 20 tick 重算一次已用字节（照抄存储访问仓的 observe 口径）
    private void tickUpdate() {
        var data = cellStorage();
        if (data == CellDataStorage.EMPTY) return;
        var dirty = this.dirty;
        if (dirty) {
            this.dirty = false;
            data.setDirty();
        }
        if (capacity == 0 || !isOnline) return;
        if (dirty || observe) {
            observe = false;
            double totalAmount = 0;
            var map = data.getStoredMap();
            if (map != null) {
                for (var entry : map) {
                    totalAmount += (double) entry.getLongValue() / entry.getKey().getType().getAmountPerByte();
                }
            }
            data.setBytes(totalAmount);
        } else if (getOffsetTimer() % 20 == 7) {
            observe = true;
        }
    }

    // ==================== ME 网络 ====================

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(this, 0);
    }

    @Override
    public Component getDescription() {
        return getDefinition().asItem().getDescription();
    }

    // ==================== 存储（与存储访问仓同口径） ====================

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return capacity > cellStorage().getBytes();
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0) return 0;
        ensureIndex();
        var data = cellStorage();
        if (data == CellDataStorage.EMPTY) return 0;
        double free = capacity - data.getBytes();
        if (amount > free) amount = IndexedStorages.limitByBytes(amount, free, what.getAmountPerByte());
        if (amount < 1) return 0;
        if (mode == Actionable.MODULATE) {
            var map = data.getStoredMap();
            if (map == null) {
                map = new AEKeyMap<>();
                data.setStoredMap(map);
            }
            map.insert(what, amount);
            dirty = true;
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        var data = cellStorage();
        if (data == CellDataStorage.EMPTY) return 0;
        var map = data.getStoredMap();
        if (map == null) return 0;
        if (mode == Actionable.MODULATE) {
            long extracted = map.extract(what, amount);
            if (extracted > 0) dirty = true;
            return extracted;
        }
        return Math.min(amount, map.getAmount(what));
    }

    @Override
    public Object getResourceIdentity() {
        var storage = cellStorage();
        return storage == CellDataStorage.EMPTY ? null : storage;
    }

    @Override
    public void getAvailableStacks(@NotNull KeyCounter out) {
        out.addAll(cellStorage().cache.getAvailableStacksCache());
    }

    @Override
    public KeyCounter getAvailableStacks() {
        return cellStorage().cache.getAvailableStacksCache();
    }

    // ==================== 存储转移 ====================

    /// 存储转移（与存储访问仓同口径）：把网络里其它真实存储的内容搬进本箱，装不下的留在原处
    private void transferFromNetwork() {
        if (isRemote() || !isOnline) return;
        ensureIndex();
        var data = cellStorage();
        if (data == CellDataStorage.EMPTY) return;
        var grid = getMainNode().getGrid();
        if (grid == null) return;
        double used = IndexedStorages.transferFromNetwork(grid, this, this, capacity - data.getBytes(), IActionSource.ofMachine(this));
        data.setBytes(data.getBytes() + used);
        onChanged();
    }

    // ==================== 界面 ====================

    /// 显示窗那一套：主页是状态显示窗，组件槽挂在下方（和通用工厂一样）
    @Override
    public Widget createUIWidget() {
        return MachineDisplay.page(this, this::addDisplayText, controls -> {
            addStorageSlot(controls);
            controls.addChoice(MODE, 2, i -> Component.translatable(i == 0 ? "config.gtceu.option.machines" : "gtceu.ownership.name.player"),
                    () -> player ? 1 : 0, i -> setPlayer(i == 1));
            controls.addServerButton(TRANSFER, TRANSFER_RUN, this::transferFromNetwork, TRANSFER_TOOLTIP);
        });
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return MachineWindow.createUI(this, this, entityPlayer);
    }

    public void addDisplayText(List<Component> textList) {
        var data = cellStorage();
        if (capacity < 1) {
            textList.add(Component.translatable(NO_COMPONENTS).withStyle(ChatFormatting.GRAY));
        }
        textList.add(Component.translatable(COMPONENTS,
                FormattingUtil.formatNumbers(componentStorage.storage.getStackInSlot(0).getCount()),
                NumberUtils.formatDouble(capacity)).withStyle(ChatFormatting.GRAY));
        // 数据索引还没建（机器模式还没存过东西）时按已用 0 算，用量与种类这一行照样显示
        var map = data == CellDataStorage.EMPTY ? null : data.getStoredMap();
        double used = data == CellDataStorage.EMPTY ? 0 : data.getBytes();
        textList.add(Component.translatable("gui.ae2.BytesUsed",
                NumberUtils.numberText(used).append(" / ").append(NumberUtils.formatDouble(capacity)))
                .withStyle(ChatFormatting.GRAY));
        textList.add(Component.literal(String.valueOf(map == null ? 0 : map.size())).withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" ").append(Component.translatable("gui.ae2.Types").withStyle(ChatFormatting.GRAY))));
    }

    // ==================== 拆机保存 ====================

    /// 机器模式的数据索引随物品走；玩家模式本来就是玩家的索引，不需要带
    @Override
    public boolean saveBreak() {
        return uuid != null;
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        if (uuid != null) tag.putUUID("uuid", uuid);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (tag.hasUUID("uuid")) uuid = tag.getUUID("uuid");
        dataStorage = null;
    }

    @Override
    public String getStorageSlotLabel() {
        return SLOT_LABEL;
    }

    @Override
    public ItemStack[] getStorageSlotGhosts() {
        return new ItemStack[] { AEItems.CELL_COMPONENT_1K.stack(), AEItems.CELL_COMPONENT_4K.stack(), AEItems.CELL_COMPONENT_16K.stack(),
                AEItems.CELL_COMPONENT_64K.stack(), AEItems.CELL_COMPONENT_256K.stack() };
    }
}
