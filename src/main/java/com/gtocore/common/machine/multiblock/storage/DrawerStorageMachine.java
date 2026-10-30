package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.MEStorageHatch;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.api.machine.storage.DrawerStorageCapacity;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.AmountFormat;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import lombok.Getter;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;

/**
 * 抽屉存储器：主机槽放超级箱或超级缸，输入总线放同类箱缸与超级存储升级。
 * <ul>
 * <li>主机槽（一格、最多 {@link #CONTROLLER_LIMIT} 个）决定能存哪一大类：超级箱只能存物品、超级缸只能存流体，
 * 并按「等级 × 数量」给容量额外加成；</li>
 * <li>可存种类数 = 与主机同类别的箱缸格数 × 数量（普通=1、1x2=2、2x2=4）；</li>
 * <li>每种类容量 = √(密封等级 × 主机等级 × 主机数量) × 同类箱缸最低容量 ÷ 同类箱缸最大格数 × √(同类箱缸总数量) × 升级倍率。</li>
 * <li>升级倍率为所有升级倍率的乘积按同类箱缸数量均分，每个箱缸最多计入 4 个升级。</li>
 * <li>显示窗里可以开关「溢出销毁」：开启后，超过每种类容量或种类已满时放不下的内容会被直接销毁，
 * 调用方不会把放不下的部分收回去。</li>
 * </ul>
 */
@DataGeneratorScanned
public final class DrawerStorageMachine extends MultiblockMEStorageMachine implements IStorageMultiblock, IFancyUIMachine, IDisplayUIMachine {

    @RegisterLanguage(cn = "超级箱 / 超级缸", en = "Super Chest / Super Tank")
    private static final String SLOT_LABEL = "gtocore.machine.drawer_storage.slot";

    /// 主机槽（超级箱/缸）的数量上限
    public static final int CONTROLLER_LIMIT = 64;

    @RegisterLanguage(cn = "种类：%s / %s", en = "Types: %s / %s")
    public static final String TYPES = "gtocore.machine.drawer_storage.types";
    @RegisterLanguage(cn = "主机：%s ×%s（等级 %s）", en = "Controller: %s x%s (tier %s)")
    public static final String CONTROLLER = "gtocore.machine.drawer_storage.controller";
    @RegisterLanguage(cn = "主机槽里放超级箱（只能存物品）或超级缸（只能存流体），最多 " + CONTROLLER_LIMIT + " 个", en = "Put a super chest (items only) or a super tank (fluids only) into the controller slot, up to " + CONTROLLER_LIMIT)
    public static final String NO_CONTROLLER = "gtocore.machine.drawer_storage.no_controller";
    @RegisterLanguage(cn = "容量加成：√(主机 %s × 密封 %s) = ×%s", en = "Capacity bonus: sqrt(controller %s x hermetic %s) = x%s")
    public static final String BONUS = "gtocore.machine.drawer_storage.bonus";
    @RegisterLanguage(cn = "输入箱缸 %s 个：最大格数 %s，数量收益 ×%s", en = "Input storage units: %s, maximum slots %s, count bonus x%s")
    public static final String STORAGE_COUNT = "gtocore.machine.drawer_storage.storage_count";
    @RegisterLanguage(cn = "升级 %s 个：按 %s 个箱缸均分，每个最多计入 4 个，倍率 ×%s", en = "Upgrades: %s, shared over %s storage units, at most 4 per unit, multiplier x%s")
    public static final String UPGRADES = "gtocore.machine.drawer_storage.upgrades";
    @RegisterLanguage(cn = "超级箱 %s 种：每种类容量 %s", en = "Super chests %s types: %s per type")
    public static final String ITEM_DRAWERS = "gtocore.machine.drawer_storage.item_drawers";
    @RegisterLanguage(cn = "超级缸 %s 种：每种类容量 %s", en = "Super tanks %s types: %s per type")
    public static final String FLUID_DRAWERS = "gtocore.machine.drawer_storage.fluid_drawers";
    @RegisterLanguage(cn = "输入总线放与主机同类的超级箱/缸：普通、1x2、2x2 每个分别提供 1、2、4 种", en = "Put super chests/tanks matching the controller into the input buses: normal, 1x2 and 2x2 units provide 1, 2 and 4 types each")
    public static final String NO_DRAWER = "gtocore.machine.drawer_storage.no_drawer";
    @RegisterLanguage(cn = "溢出销毁：%s", en = "Overflow voiding: %s")
    public static final String OVERFLOW = "gtocore.machine.drawer_storage.overflow";
    @RegisterLanguage(cn = "开启后，放不下的内容（超过每种类容量，或种类已满放不进新种类）会被直接销毁，调用方不会收回", en = "When enabled, anything that does not fit (over the per-type capacity, or no free type for a new key) is voided instead of being returned")
    public static final String OVERFLOW_TOOLTIP = "gtocore.machine.drawer_storage.overflow.tooltip";
    @RegisterLanguage(cn = "溢出销毁", en = "Overflow Voiding")
    private static final String OVERFLOW_LABEL = "gtocore.machine.drawer_storage.overflow.label";

    /// 主机槽（超级箱/缸）：一格，最多 {@link #CONTROLLER_LIMIT} 个
    @SaveToDisk
    @Getter
    private final NotifiableStackInventory machineStorage;
    /// 可存种类数：同类箱缸的格数 × 数量
    @Getter
    private int types;
    /// 每种类容量
    @Getter
    private long perTypeCapacity;
    /// 主机槽决定的大类：true = 只能存流体
    @Getter
    private boolean fluidKind;
    /// 输入总线中同类箱缸的总数量
    @Getter
    private long drawerCount;
    /// 同类箱缸的最大格数，与最低容量分别取值
    private int drawerSlots = 1;
    @Getter
    private long upgradeCount;
    @Getter
    private double upgradeMultiplier = 1;
    /// 主机等级 × 数量
    @Getter
    private double controllerMultiplier;
    @Getter
    private double controllerLevel;
    @Getter
    private int hermeticLevel = 1;
    /// 溢出销毁：放不下的内容直接销毁，不退回调用方
    @SaveToDisk(defaultValue = "false")
    @Getter
    private boolean voidOverflow;

    /// 输入总线：放超级箱/缸与超级存储升级
    private final StorageBusListener drawerBuses = new StorageBusListener();

    public DrawerStorageMachine(MetaMachineBlockEntity holder) {
        super(holder, null, false);
        machineStorage = createMachineStorage(null);
        machineStorage.setCapabilityValidator(GTUtil.NEGATIVE);
    }

    /// 主机槽只收超级箱 / 超级缸
    @Override
    public boolean storageFilter(ItemStack stack) {
        return DrawerStorageCapacity.get(stack.getItem()) != null;
    }

    @Override
    public int getSlotLimit() {
        return CONTROLLER_LIMIT;
    }

    /// 不设总容量：只按种类卡每一种的上限
    @Override
    protected long computeCapacity() {
        refreshDrawers();
        return Long.MAX_VALUE;
    }

    /// 重扫输入总线里的箱缸/升级与主机槽，算出种类数与每种类容量
    private void refreshDrawers() {
        hermeticLevel = getMultiblockState().getMatchContext().getOrDefault(GTORecipeDataKeys.HERMETIC_CASING_TIER, 0) + 1;
        var controller = getStorageStack();
        var definition = DrawerStorageCapacity.get(controller.getItem());
        fluidKind = definition != null && definition.fluid();
        controllerLevel = definition == null ? 0 : definition.controllerLevel();
        controllerMultiplier = controllerLevel * controller.getCount();

        long minimumCapacity = Long.MAX_VALUE;
        int maximumSlots = 1;
        int types = 0;
        long drawers = 0;
        long upgrades = 0;
        double upgradeLog2 = 0;
        for (var bus : drawerBuses.buses()) {
            var inv = bus.storage;
            for (int i = 0, slots = inv.size(); i < slots; i++) {
                var key = inv.keyAt(i);
                if (key == null) continue;
                long count = inv.amountAt(i);
                if (count < 1) continue;
                var item = key.getItem();
                var storage = DrawerStorageCapacity.get(item);
                if (storage != null) {
                    if (storage.fluid() != fluidKind) continue;
                    types = Keys.saturatedInt(types + (long) storage.slots() * Keys.saturatedInt(count));
                    drawers = Keys.add(drawers, count);
                    minimumCapacity = Math.min(minimumCapacity, storage.capacity());
                    maximumSlots = Math.max(maximumSlots, storage.slots());
                } else {
                    double log2 = DrawerStorageCapacity.upgradeLog2(item);
                    if (log2 <= 0) continue;
                    upgrades = Keys.add(upgrades, count);
                    upgradeLog2 += count * log2;
                }
            }
        }
        this.types = types;
        this.drawerCount = drawers;
        this.drawerSlots = maximumSlots;
        this.upgradeCount = upgrades;
        this.upgradeMultiplier = DrawerStorageCapacity.averageUpgrade(upgradeLog2, upgrades, drawers);
        this.perTypeCapacity = DrawerStorageCapacity.capacityOf(minimumCapacity, maximumSlots, drawers,
                hermeticLevel, controllerMultiplier, upgradeMultiplier);
    }

    /// 主机决定的大类：超级箱只收物品、超级缸只收流体
    private boolean accepts(AEKey what) {
        return fluidKind ? what instanceof AEFluidKey : what instanceof AEItemKey;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!isFormed || amount < 1 || types < 1 || perTypeCapacity < 1 || !accepts(what)) return 0;
        var map = keyMap;
        long stored = map.getAmount(what);
        int used = map.size();
        if (used > types) return 0;
        if (stored == 0 && used >= types) return voidOverflow ? amount : 0;
        if (mode == Actionable.SIMULATE) {
            return voidOverflow ? amount : Math.clamp(perTypeCapacity - stored, 0, amount);
        }
        long inserted = map.insert(what, amount, perTypeCapacity);
        if (inserted > 0) saveChanges();
        // 溢出销毁：放不下的部分也照单收下（多的直接销毁），调用方不会再把它们退回来
        return voidOverflow ? amount : inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (mode != Actionable.MODULATE) return Math.min(amount, keyMap.getAmount(what));
        long extracted = keyMap.extract(what, amount);
        if (extracted > 0) saveChanges();
        return extracted;
    }

    @Override
    protected void saveChanges() {
        holder.setChanged();
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (!isFormed || types < 1 || perTypeCapacity < 1 || !accepts(what)) return false;
        var map = getKeyMap();
        long stored = map.getAmount(what);
        int used = map.size();
        return used <= types && stored < perTypeCapacity && (stored > 0 || used < types);
    }

    @Override
    public void onStructureFormed() {
        // 箱缸总线要先挂上，容量里要用
        drawerBuses.bind(getMultiblockState().getMatchContext(), this, this::onDrawerBusChanged);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        drawerBuses.unbind();
        types = 0;
        drawerCount = 0;
        drawerSlots = 1;
        upgradeCount = 0;
        upgradeMultiplier = 1;
        perTypeCapacity = 0;
        controllerMultiplier = 0;
        controllerLevel = 0;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        drawerBuses.unbind();
    }

    /// 主机槽里的超级箱/缸换了（{@link IStorageMultiblock} 的槽变更回调）
    @Override
    public void onMachineChanged() {
        if (isRemote() || !isFormed) return;
        refreshCapacity();
        refreshHatches();
        onChanged();
    }

    private void onDrawerBusChanged() {
        if (isRemote() || !isFormed) return;
        long oldCapacity = perTypeCapacity;
        int oldTypes = types;
        long oldDrawers = drawerCount;
        int oldSlots = drawerSlots;
        long oldUpgrades = upgradeCount;
        double oldMultiplier = upgradeMultiplier;
        refreshCapacity();
        if (oldCapacity == perTypeCapacity && oldTypes == types && oldDrawers == drawerCount && oldSlots == drawerSlots &&
                oldUpgrades == upgradeCount && oldMultiplier == upgradeMultiplier)
            return;
        refreshHatches();
        onChanged();
    }

    /// 容量变了，让结构里的保险库仓重新读一遍
    private void refreshHatches() {
        for (var part : getParts(MEStorageHatch.class)) {
            part.refreshStorageBinding();
        }
    }

    // ==================== 界面 ====================

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return isFormed && IFancyUIMachine.super.shouldOpenUI(player, hand, hit);
    }

    /// 显示窗那一套：主页是机器的状态显示窗，主机槽（超级箱/缸）跟在下方（和通用工厂一样）
    @Override
    public UIElement createUIWidget() {
        var controls = ControlPanel.of(this);
        addStorageSlot(controls);
        controls.addToggle(OVERFLOW_LABEL, () -> voidOverflow, value -> voidOverflow = value, OVERFLOW_TOOLTIP);
        return MachineDisplay.page(this).addChild(controls.build());
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return MachineWindow.createUI(this, this, entityPlayer);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        var controller = getStorageStack();
        if (controller.isEmpty()) {
            textList.add(Component.translatable(NO_CONTROLLER).withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(Component.translatable(CONTROLLER, controller.getHoverName(),
                    FormattingUtil.formatNumbers(controller.getCount()),
                    NumberUtils.formatDouble(controllerLevel)).withStyle(ChatFormatting.GRAY));
            textList.add(Component.translatable(BONUS,
                    NumberUtils.formatDouble(controllerMultiplier),
                    FormattingUtil.formatNumbers(hermeticLevel),
                    NumberUtils.formatDouble(capacityBonus())).withStyle(ChatFormatting.GRAY));
        }
        textList.add(Component.translatable(STORAGE_COUNT, FormattingUtil.formatNumbers(drawerCount),
                FormattingUtil.formatNumbers(drawerSlots), NumberUtils.formatDouble(Math.sqrt(drawerCount)))
                .withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable(UPGRADES, FormattingUtil.formatNumbers(upgradeCount),
                FormattingUtil.formatNumbers(drawerCount), NumberUtils.formatDouble(upgradeMultiplier)).withStyle(ChatFormatting.GRAY));
        if (types < 1) {
            textList.add(Component.translatable(NO_DRAWER).withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(Component.translatable(fluidKind ? FLUID_DRAWERS : ITEM_DRAWERS,
                    FormattingUtil.formatNumbers(types),
                    formatCapacity(fluidKind, perTypeCapacity)).withStyle(ChatFormatting.GRAY));
        }
        int used = getKeyMap().size();
        textList.add(Component.translatable(TYPES,
                FormattingUtil.formatNumbers(used),
                FormattingUtil.formatNumbers(types)).withStyle(used > types ? ChatFormatting.RED : ChatFormatting.GRAY));
    }

    private static String formatCapacity(boolean fluid, long capacity) {
        return fluid ? AEKeyTypes.FLUIDS.formatAmount(capacity, AmountFormat.FULL) : FormattingUtil.formatNumbers(capacity);
    }

    /// 主机与密封的总加成开根号（显示用）
    private double capacityBonus() {
        return Math.sqrt(Math.max(0, hermeticLevel) * Math.max(0, controllerMultiplier));
    }

    @Override
    public void appendWailaData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        compoundTag.putInt("types", types);
        compoundTag.putInt("usedTypes", getKeyMap().size());
        compoundTag.putLong("perTypeCapacity", perTypeCapacity);
        compoundTag.putLong("drawerCount", drawerCount);
        compoundTag.putInt("drawerSlots", drawerSlots);
        compoundTag.putLong("upgradeCount", upgradeCount);
        compoundTag.putDouble("upgradeMultiplier", upgradeMultiplier);
        compoundTag.putDouble("controllerMultiplier", controllerMultiplier);
        compoundTag.putInt("hermeticLevel", hermeticLevel);
        compoundTag.putBoolean("fluidKind", fluidKind);
        compoundTag.putBoolean("voidOverflow", voidOverflow);
    }

    @Override
    public void appendWailaTooltip(CompoundTag compoundTag, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        int usedTypes = compoundTag.getInt("usedTypes");
        int typeLimit = compoundTag.getInt("types");
        var typeLine = Component.translatable(TYPES, FormattingUtil.formatNumbers(usedTypes), FormattingUtil.formatNumbers(typeLimit));
        iTooltip.add(usedTypes > typeLimit ? typeLine.withStyle(ChatFormatting.RED) : typeLine);
        boolean fluid = compoundTag.getBoolean("fluidKind");
        iTooltip.add(Component.translatable(fluid ? FLUID_DRAWERS : ITEM_DRAWERS,
                FormattingUtil.formatNumbers(compoundTag.getInt("types")),
                formatCapacity(fluid, compoundTag.getLong("perTypeCapacity"))));
        long drawerCount = compoundTag.getLong("drawerCount");
        int drawerSlots = Math.max(1, compoundTag.getInt("drawerSlots"));
        iTooltip.add(Component.translatable(STORAGE_COUNT,
                FormattingUtil.formatNumbers(drawerCount),
                FormattingUtil.formatNumbers(drawerSlots), NumberUtils.formatDouble(Math.sqrt(drawerCount))));
        iTooltip.add(Component.translatable(UPGRADES, FormattingUtil.formatNumbers(compoundTag.getLong("upgradeCount")),
                FormattingUtil.formatNumbers(drawerCount), NumberUtils.formatDouble(compoundTag.getDouble("upgradeMultiplier"))));
        iTooltip.add(Component.translatable(BONUS,
                NumberUtils.formatDouble(compoundTag.getDouble("controllerMultiplier")),
                FormattingUtil.formatNumbers(compoundTag.getInt("hermeticLevel")),
                NumberUtils.formatDouble(Math.sqrt(Math.max(0, compoundTag.getInt("hermeticLevel")) *
                        Math.max(0, compoundTag.getDouble("controllerMultiplier"))))));
        iTooltip.add(Component.translatable(OVERFLOW,
                Component.translatable(compoundTag.getBoolean("voidOverflow") ? "gtocore.machine.on" : "gtocore.machine.off")));
    }

    @Override
    public String getStorageSlotLabel() {
        return SLOT_LABEL;
    }

    @Override
    public ItemStack[] getStorageSlotGhosts() {
        return new ItemStack[] { GTMachines.SUPER_CHEST[GTValues.LV].asStack(), GTMachines.SUPER_TANK[GTValues.LV].asStack() };
    }
}
