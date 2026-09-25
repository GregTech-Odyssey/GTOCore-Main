package com.gtocore.common.machine.noenergy;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
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
import net.minecraft.nbt.ByteArrayTag;
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
import appeng.items.materials.StorageComponentItem;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.data.Data;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ME 磁盘箱子：ME 磁盘存储器的单方块版本。一个槽放 AE2 存储组件（1k…256k，最多 {@link #COMPONENT_LIMIT} 个），
 * 组件字节之和就是容量；存储本身的机制与存储访问仓一致（挂进 ME 网络当一个存储器、按字节卡容量、
 * 物品 1 个 1 字节的近似口径也照抄），内容随物品走、组件拆机时掉出来。
 */
@DataGeneratorScanned
public final class MEDiskBoxMachine extends MetaMachine
                                    implements IGridConnectedMachine, MEStorage, IStorageProvider, IStorageMultiblock, IFancyUIMachine, IDropSaveMachine {

    /// 组件槽的数量上限
    public static final int COMPONENT_LIMIT = 64;

    @RegisterLanguage(cn = "存储组件：%s 个，容量 %s", en = "Storage components: %s, capacity %s")
    public static final String COMPONENTS = "gtocore.machine.me_disk_box.components";
    @RegisterLanguage(cn = "放 AE2 存储组件（1k…256k）来提供容量", en = "Put AE2 storage components (1k...256k) in to provide capacity")
    public static final String NO_COMPONENTS = "gtocore.machine.me_disk_box.no_components";

    /// 箱子里存的内容（随物品走）
    @SaveToDisk
    @NotNull
    private final AEKeyMap<AEKey> keyMap = new AEKeyMap<>();
    /// 组件槽（1 格，最多 {@link #COMPONENT_LIMIT} 个存储组件）
    @SaveToDisk
    private final NotifiableItemStackHandler componentStorage;
    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    @SyncToClient
    private boolean isOnline;
    /// 容量（组件字节之和）与已用字节
    private double capacity;
    private double bytes;

    public MEDiskBoxMachine(MetaMachineBlockEntity holder) {
        super(holder);
        componentStorage = createMachineStorage(null);
        nodeHolder = new GridNodeHolder(this);
        getMainNode().addService(IStorageProvider.class, this);
    }

    /// 组件槽就是 {@link IStorageMultiblock} 的机器存储槽
    @Override
    public NotifiableItemStackHandler getMachineStorage() {
        return componentStorage;
    }

    /// 组件槽只收 AE2 存储组件
    @Override
    public boolean storageFilter(ItemStack stack) {
        return stack.getItem() instanceof StorageComponentItem;
    }

    @Override
    public int getSlotLimit() {
        return COMPONENT_LIMIT;
    }

    /// 组件增减：容量立刻跟着变
    @Override
    public void onMachineChanged() {
        if (isRemote()) return;
        refreshCapacity();
        onChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshCapacity();
    }

    private void refreshCapacity() {
        long total = 0;
        for (int i = 0, slots = componentStorage.getSlots(); i < slots; i++) {
            var stack = componentStorage.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof StorageComponentItem component)) continue;
            long term = component.getBytes();
            total = term > Long.MAX_VALUE / stack.getCount() ? Long.MAX_VALUE : total + term * stack.getCount();
            if (total == Long.MAX_VALUE) break;
        }
        capacity = total;
        recalcBytes();
    }

    /// 已用字节：与存储访问仓的 observe 口径一致（按 AEKeyType 的 amountPerByte 折算）
    private void recalcBytes() {
        double total = 0;
        for (var entry : keyMap) {
            total += (double) entry.getLongValue() / entry.getKey().getType().getAmountPerByte();
        }
        bytes = total;
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
        return capacity > bytes;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount < 1 || capacity <= bytes) return 0;
        long space = (long) Math.min(capacity - bytes, amount);
        if (space < 1) return 0;
        if (mode == Actionable.MODULATE) {
            keyMap.insert(what, space);
            recalcBytes();
            onChanged();
        }
        return space;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (mode != Actionable.MODULATE) return Math.min(amount, keyMap.getAmount(what));
        long extracted = keyMap.extract(what, amount);
        if (extracted > 0) {
            recalcBytes();
            onChanged();
        }
        return extracted;
    }

    @Override
    public void getAvailableStacks(@NotNull KeyCounter out) {
        if (keyMap.isEmpty()) return;
        out.addAll(keyMap.size(), m -> keyMap.fastForEach(m::insert));
    }

    @Override
    public KeyCounter getAvailableStacks() {
        var counter = new KeyCounter();
        getAvailableStacks(counter);
        return counter;
    }

    // ==================== 界面 ====================

    /// 显示窗那一套：主页是状态显示窗，组件槽挂在下方（和通用工厂一样）
    @Override
    public Widget createUIWidget() {
        return IStorageMultiblock.super.createUIWidget(MachineDisplay.page(this, this::addDisplayText, null));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new MachineWindow(this));
    }

    /// 显示窗里的内容（单方块机器不是多方块，走 MachineDisplay 的文本回调）
    public void addDisplayText(List<Component> textList) {
        if (capacity < 1) {
            textList.add(Component.translatable(NO_COMPONENTS).withStyle(ChatFormatting.GRAY));
        }
        textList.add(Component.translatable(COMPONENTS,
                FormattingUtil.formatNumbers(componentStorage.getStackInSlot(0).getCount()),
                NumberUtils.formatDouble(capacity)).withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable("gui.ae2.BytesUsed",
                NumberUtils.numberText(bytes).append(" / ").append(NumberUtils.formatDouble(capacity)))
                .withStyle(ChatFormatting.GRAY));
        textList.add(Component.literal(String.valueOf(keyMap.size())).withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" ").append(Component.translatable("gui.ae2.Types").withStyle(ChatFormatting.GRAY))));
    }

    // ==================== 拆机保存 ====================

    @Override
    public boolean saveBreak() {
        return true;
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.putByteArray("keymap", getFieldDataManager().writeFieldToData("keyMap").writeToBytes());
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (tag.get("keymap") instanceof ByteArrayTag byteArrayTag) {
            getFieldDataManager().readFieldFromData(Data.readData(byteArrayTag.getAsByteArray()), 0, "keyMap");
        }
        recalcBytes();
    }
}
