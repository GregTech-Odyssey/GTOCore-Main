package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.machine.multiblock.part.ae.StorageAccessPartMachine;

import com.gtolib.api.ae2.storage.BigCellDataStorage;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.api.machine.multiblock.NoRecipeLogicMultiblockMachine;
import com.gtolib.utils.NumberUtils;
import com.gtolib.utils.StringUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.transfer.key.MEStorageProvider;
import com.gregtechceu.gtceu.api.transfer.key.MEStorageProviderIndex;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.utils.BigIntegerUtils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DataGeneratorScanned
public class MEStorageMachine extends NoRecipeLogicMultiblockMachine implements IBindable, IDropSaveMachine, IStorageMultiblock, MEStorageProvider {

    public static final long infinite = 1000000000000L; // 1T
    @RegisterLanguage(en = "Data Index Position", cn = "数据索引位置")
    private static final String MODE = "gtocore.machine.me_storage.mode";
    @SaveToDisk
    private final NotifiableStackInventory machineStorage;
    @SaveToDisk
    private UUID uuid;
    @SaveToDisk(defaultValue = "false")
    protected boolean player = false;
    protected StorageAccessPartMachine accessPartMachine;
    private final List<Reference2ObjectMap.Entry<AEKey, BigInteger>> list = new ArrayList<>();

    public MEStorageMachine(MetaMachineBlockEntity holder) {
        super(holder);
        machineStorage = createMachineStorage(i -> i.getItem() == GTOItems.INFINITE_CELL_COMPONENT.asItem());
        machineStorage.setCapabilityValidator(GTUtil.NEGATIVE);
    }

    @Override
    public void onMachineChanged() {
        unloadContainer();
        loadContainer();
    }

    protected void loadContainer() {
        if (isRemote()) return;
        Level level = getLevel();
        if (level == null) return;
        for (var part : getParts(StorageAccessPartMachine.class)) {
            accessPartMachine = part;
            break;
        }
        if (accessPartMachine == null) return;
        double capacity = getStorageCapacity();
        // 没有容量来源（结构里的核心 / 输入总线里的存储组件）就不绑定访问仓
        if (capacity <= 0) return;
        if (player) {
            accessPartMachine.setUUID(getOwnerUUID());
        } else {
            if (uuid == null) uuid = UUID.randomUUID();
            accessPartMachine.setUUID(uuid);
        }
        accessPartMachine.setCapacity(capacity);
        accessPartMachine.setInfinite(isInfiniteStorage(capacity));
        accessPartMachine.setCheck(true);
    }

    /** 数据索引容量（字节）：默认取结构里 ME 存储核心给的字节数，子类可以换个容量来源。 */
    protected double getStorageCapacity() {
        var core = getMultiblockState().getMatchContext().get(GTOPredicates.DataKeys.ME_STORAGE_CORE);
        return core == null ? 0 : core;
    }

    /** 是否按无限存储处理：默认容量超过 {@link #infinite} 且机器存储里塞了 64 个无限元件。 */
    protected boolean isInfiniteStorage(double capacity) {
        return capacity > infinite && getStorageStack().getCount() == 64;
    }

    protected void unloadContainer() {
        if (accessPartMachine != null) {
            accessPartMachine.setCapacity(0);
            accessPartMachine.setUUID(null);
            accessPartMachine.setCheck(false);
            accessPartMachine.setInfinite(false);
            accessPartMachine = null;
        }
    }

    @Override
    public void onStructureInvalid() {
        unloadContainer();
        MEStorageProviderIndex.remove(getLevel(), this);
        super.onStructureInvalid();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        onMachineChanged();
        // 结构成型后才算"世界里的一个容器"，注册进八叉树索引供合成站/其它设备查找
        MEStorageProviderIndex.add(getLevel(), this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isFormed()) {
            loadContainer();
            MEStorageProviderIndex.add(getLevel(), this);
        }
    }

    @Override
    public void onUnload() {
        unloadContainer();
        MEStorageProviderIndex.remove(getLevel(), this);
        super.onUnload();
    }

    /** 存储本体挂在 ME 存储访问仓上（那东西本身就是 MEStorage）。 */
    @Override
    public appeng.api.storage.MEStorage getStorageCap(net.minecraft.core.Direction side) {
        return accessPartMachine;
    }

    @Override
    public UIElement createUIWidget() {
        var controls = ControlPanel.of(this);
        addStorageSlot(controls);
        controls.addChoice(MODE, 2, i -> Component.translatable(i == 0 ? "config.gtceu.option.machines" : "gtceu.ownership.name.player"),
                () -> player ? 1 : 0, i -> {
                    player = i == 1;
                    onMachineChanged();
                });
        return super.createUIWidget().addChild(controls.build());
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (accessPartMachine != null) {
            textList.add(Component.translatable("gui.ae2.BytesUsed", NumberUtils.numberText(accessPartMachine.getBytes()).append(" / ").append(accessPartMachine.isInfinite() ? StringUtils.full_color("infinity") : NumberUtils.formatDouble(accessPartMachine.getCapacity())).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
            textList.add(Component.literal(String.valueOf(accessPartMachine.getTypes())).withStyle(ChatFormatting.AQUA).append(Component.literal(" ").append(Component.translatable("gui.ae2.Types").withStyle(ChatFormatting.GRAY))));
            if (accessPartMachine instanceof StorageAccessPartMachine.Big machine) {
                var data = machine.getCellStorage();
                if (data == BigCellDataStorage.EMPTY) return;
                var map = data.getStoredMap();
                if (map == null) return;
                list.clear();
                map.reference2ObjectEntrySet().forEach(entry -> {
                    var currentAmount = entry.getValue();
                    if (currentAmount.compareTo(BigIntegerUtils.BIG_INTEGER_MAX_LONG) > 0) {
                        list.add(entry);
                    }
                });
                list.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
                list.forEach(entry -> textList.add(entry.getKey().getDisplayName().copy().append(": ").append(NumberUtils.numberText(entry.getValue().doubleValue())).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return getOwnerUUID();
    }

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
        if (tag.hasUUID("uuid")) {
            uuid = tag.getUUID("uuid");
        }
    }

    @Override
    public NotifiableStackInventory getMachineStorage() {
        return this.machineStorage;
    }

    @Override
    public String getStorageSlotLabel() {
        return GTOItems.INFINITE_CELL_COMPONENT.asItem().getDescriptionId();
    }

    @Override
    public ItemStack[] getStorageSlotGhosts() {
        return new ItemStack[] { GTOItems.INFINITE_CELL_COMPONENT.asStack() };
    }
}
