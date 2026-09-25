package com.gtocore.common.machine.noenergy;

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
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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
import appeng.items.materials.StorageComponentItem;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * ME 磁盘箱子：ME 磁盘存储器的单方块版本。一个槽放 AE2 存储组件（1k…256k，最多 {@link #COMPONENT_LIMIT} 个，
 * 单个组件不超过 {@link #MAX_COMPONENT_BYTES}，1M 及以上的组件不收），组件字节之和就是容量；
 * 存储直接用存储访问仓那一套（{@link CellDataStorage} + 数据索引 UUID + 按字节卡容量），
 * 数据索引可以选玩家或机器（显示窗里点一下切换），显示窗里还有一个「存储转移」按钮，
 * 口径与存储访问仓一致：把网络里其它 ME 存储的内容全部搬进本箱。
 */
@DataGeneratorScanned
public final class MEDiskBoxMachine extends MetaMachine
                                    implements IGridConnectedMachine, MEStorage, IStorageProvider, IStorageMultiblock, IFancyUIMachine, IDropSaveMachine {

    /// 组件槽的数量上限
    public static final int COMPONENT_LIMIT = 64;
    /// 单个组件的容量上限：本机是单方块小箱子，只收 AE2 的 256k 及以下组件（1M 及以上的组件不收）
    public static final long MAX_COMPONENT_BYTES = 256 * 1024L;
    /// 数据索引位置（与 ME 存储器共用同一套文案）
    private static final String MODE = "gtocore.machine.me_storage.mode";
    /// 数据索引开关的按钮键
    private static final String SWITCH = "switch";
    /// 存储转移按钮的键
    private static final String TRANSFER_BUTTON = "transfer";
    /// 存储转移的文案与存储访问仓共用，注册在 MachineLang
    private static final String TRANSFER = "gtocore.machine.storage_transfer";
    private static final String TRANSFER_TOOLTIP = "gtocore.machine.storage_transfer.tooltip";

    @RegisterLanguage(cn = "存储组件：%s 个，容量 %s", en = "Storage components: %s, capacity %s")
    public static final String COMPONENTS = "gtocore.machine.me_disk_box.components";
    @RegisterLanguage(cn = "放 AE2 存储组件（只收 256k 及以下）来提供容量", en = "Put AE2 storage components (256k and below only) in to provide capacity")
    public static final String NO_COMPONENTS = "gtocore.machine.me_disk_box.no_components";

    /// 组件槽（1 格，最多 {@link #COMPONENT_LIMIT} 个存储组件）
    @SaveToDisk
    private final NotifiableItemStackHandler componentStorage;
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
    /// 存储转移进行中：这期间自己的 extract 一律为 0，网络取物只能从别的存储拿
    private boolean transferring;

    public MEDiskBoxMachine(MetaMachineBlockEntity holder) {
        super(holder);
        componentStorage = createMachineStorage(null);
        nodeHolder = new GridNodeHolder(this);
        getMainNode().addService(IStorageProvider.class, this);
        tickSubs = new ConditionalSubscriptionHandler(this, this::tickUpdate, 0, () -> true);
    }

    /// 组件槽就是 {@link IStorageMultiblock} 的机器存储槽
    @Override
    public NotifiableItemStackHandler getMachineStorage() {
        return componentStorage;
    }

    /// 组件槽只收 256k 及以下的 AE2 存储组件
    @Override
    public boolean storageFilter(ItemStack stack) {
        return componentBytes(stack) > 0;
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
        for (int i = 0, slots = componentStorage.getSlots(); i < slots; i++) {
            var stack = componentStorage.getStackInSlot(i);
            long term = componentBytes(stack);
            if (term < 1) continue;
            total = term > Long.MAX_VALUE / stack.getCount() ? Long.MAX_VALUE : total + term * stack.getCount();
            if (total == Long.MAX_VALUE) break;
        }
        capacity = total;
    }

    /// 组件能提供的字节数；不是组件、或者超过 {@link #MAX_COMPONENT_BYTES} 的（1M 及以上）都算 0
    private static long componentBytes(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageComponentItem component)) return 0;
        long bytes = component.getBytes();
        return bytes > MAX_COMPONENT_BYTES ? 0 : bytes;
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
        amount = (long) Math.min(capacity - data.getBytes(), amount);
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
        if (transferring) return 0;
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
    public void getAvailableStacks(@NotNull KeyCounter out) {
        out.addAll(cellStorage().cache.getAvailableStacksCache());
    }

    @Override
    public KeyCounter getAvailableStacks() {
        return cellStorage().cache.getAvailableStacksCache();
    }

    // ==================== 存储转移 ====================

    /// 存储转移（与存储访问仓同口径）：把网络里其它 ME 存储的内容全部搬进本箱，装不下的留在原处
    private void transferFromNetwork() {
        if (isRemote() || !isOnline) return;
        ensureIndex();
        var data = cellStorage();
        if (data == CellDataStorage.EMPTY) return;
        var grid = getMainNode().getGrid();
        if (grid == null) return;
        var network = grid.getStorageService().getInventory();
        var networkContent = new KeyCounter();
        network.getAvailableStacks(networkContent);
        if (networkContent.isEmpty()) return;
        var source = IActionSource.ofMachine(this);
        transferring = true;
        try {
            for (var entry : networkContent) {
                var what = entry.getKey();
                if (what == null) continue;
                // 网络清单里包含本箱自己，减掉自己已有的那部分才只搬别人的
                long want = entry.getLongValue() - getOwnAmount(what);
                if (want < 1) continue;
                long possible = insert(what, want, Actionable.SIMULATE, source);
                if (possible < 1) continue;
                long extracted = network.extract(what, possible, Actionable.MODULATE, source);
                if (extracted < 1) continue;
                long inserted = insert(what, extracted, Actionable.MODULATE, source);
                if (inserted < extracted) network.insert(what, extracted - inserted, Actionable.MODULATE, source);
            }
        } finally {
            transferring = false;
        }
        onChanged();
    }

    /// 本箱自己存了多少
    private long getOwnAmount(AEKey what) {
        var data = cellStorage();
        if (data == CellDataStorage.EMPTY) return 0;
        var map = data.getStoredMap();
        return map == null ? 0 : map.getAmount(what);
    }

    // ==================== 界面 ====================

    /// 显示窗那一套：主页是状态显示窗，组件槽挂在下方（和通用工厂一样）
    @Override
    public Widget createUIWidget() {
        return IStorageMultiblock.super.createUIWidget(MachineDisplay.page(this, this::addDisplayText, this::handleDisplayClick));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new MachineWindow(this));
    }

    public void addDisplayText(List<Component> textList) {
        var data = cellStorage();
        textList.add(Component.translatable(MODE).append(ComponentPanelWidget.withButton(
                Component.literal("[").append(player ? Component.translatable("gtceu.ownership.name.player") :
                        Component.translatable("config.gtceu.option.machines")).append("]"),
                SWITCH)));
        if (capacity < 1) {
            textList.add(Component.translatable(NO_COMPONENTS).withStyle(ChatFormatting.GRAY));
        }
        textList.add(Component.translatable(COMPONENTS,
                FormattingUtil.formatNumbers(componentStorage.getStackInSlot(0).getCount()),
                NumberUtils.formatDouble(capacity)).withStyle(ChatFormatting.GRAY));
        // 数据索引还没建（机器模式还没存过东西）时按已用 0 算，用量与种类这一行照样显示
        var map = data == CellDataStorage.EMPTY ? null : data.getStoredMap();
        double used = data == CellDataStorage.EMPTY ? 0 : data.getBytes();
        textList.add(Component.translatable("gui.ae2.BytesUsed",
                NumberUtils.numberText(used).append(" / ").append(NumberUtils.formatDouble(capacity)))
                .withStyle(ChatFormatting.GRAY));
        textList.add(Component.literal(String.valueOf(map == null ? 0 : map.size())).withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" ").append(Component.translatable("gui.ae2.Types").withStyle(ChatFormatting.GRAY))));
        textList.add(ComponentPanelWidget.withButton(
                Component.literal("[").append(Component.translatable(TRANSFER)).append("]"), TRANSFER_BUTTON)
                .copy().withStyle(ChatFormatting.GRAY)
                .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(TRANSFER_TOOLTIP).withStyle(ChatFormatting.YELLOW)))));
    }

    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (clickData.isRemote) return;
        if (SWITCH.equals(componentData)) {
            setPlayer(!player);
        } else if (TRANSFER_BUTTON.equals(componentData)) {
            transferFromNetwork();
        }
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
}
