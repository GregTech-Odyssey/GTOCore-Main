package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.data.Algae;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemList;
import com.gtocore.common.machine.multiblock.part.ae.widget.AELimitFluidConfigWidget;
import com.gtocore.common.machine.multiblock.part.ae.widget.AELimitItemConfigWidget;

import com.gtolib.api.ae2.storage.BigCellDataStorage;
import com.gtolib.api.ae2.storage.CellDataStorage;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.part.AmountConfigurationPartMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@DataGeneratorScanned
public abstract class StorageAccessPartMachine extends AmountConfigurationPartMachine implements IMachineLife, MEStorage, IGridConnectedMachine, IStorageProvider {

    public static StorageAccessPartMachine create(MetaMachineBlockEntity holder) {
        return new StorageAccessPartMachine.LONG(holder);
    }

    public static StorageAccessPartMachine createBig(MetaMachineBlockEntity holder) {
        return new StorageAccessPartMachine.Big(holder);
    }

    public static StorageAccessPartMachine createIO(MetaMachineBlockEntity holder) {
        return new StorageAccessPartMachine.IO(holder);
    }

    public static StorageAccessPartMachine createAlgae(MetaMachineBlockEntity holder) {
        return new StorageAccessPartMachine.AlgaeAccessHatch(holder);
    }

    public static StorageAccessPartMachine createConfigurable(MetaMachineBlockEntity holder) {
        return new StorageAccessPartMachine.Configurable(holder);
    }

    @RegisterLanguage(cn = "存储转移", en = "Transfer Storage")
    private static final String LANG_TRANSFER = "gtocore.machine.storage_access_hatch.transfer";
    @RegisterLanguage(cn = "把网络里其它 ME 存储的内容全部搬进本仓（本仓装不下的留在原处）", en = "Move everything held by the other ME storages in the network into this hatch (what does not fit stays where it is)")
    private static final String LANG_TRANSFER_TOOLTIP = "gtocore.machine.storage_access_hatch.transfer_tooltip";

    @Setter
    boolean observe;

    int counter;
    @Setter
    boolean check;
    boolean dirty = false;
    boolean transferring;

    @Setter
    @Getter
    @SaveToDisk
    double capacity;
    @Setter
    @Getter
    @SaveToDisk
    boolean isInfinite;
    @SyncToClient
    boolean isOnline;
    @SaveToDisk
    public UUID uuid;

    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    private final ConditionalSubscriptionHandler tickSubs;

    StorageAccessPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.HV, -1000000, 1000000);
        this.nodeHolder = new GridNodeHolder(this);
        getMainNode().addService(IStorageProvider.class, this);
        tickSubs = new ConditionalSubscriptionHandler(this, this::tickUpdate, 0, () -> true);
        current = 0;
    }

    private static final long PRIORITY_MIN = -1000000L;
    private static final long PRIORITY_MAX = 1000000L;

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        if (widget instanceof MachineWindow window) registerPopups(window);
        return MEPartUI.mainPage(this::isOnline, getTitle(), widget, buildPage());
    }

    /** 注册本仓用到的弹出面板；外壳是 {@link MachineWindow} 时才会调用，覆写时先调用 super。 */
    protected void registerPopups(MachineWindow window) {}

    @Override
    public Widget createUIWidget() {
        return buildPage();
    }

    UIElement buildPage() {
        var section = UIElement.section();
        section.addChild(MEPartUI.numberRow("gui.ae2.Priority", MEPatternPartUI.longField(0, this::getCurrent, this::setPriority, PRIORITY_MIN),
                "gui.ae2.PriorityExtractionHint", "gui.ae2.PriorityInsertionHint"));
        addTransferButton(section);
        return MEPartUI.page().addChild(section);
    }

    final void addTransferButton(UIElement section) {
        section.addChild(Button.translatable(UISizes.BUTTON_WIDTH, LANG_TRANSFER)
                .setOnServerClick(this::transferFromNetwork)
                .bindTooltip(() -> Component.translatable(LANG_TRANSFER_TOOLTIP)));
    }

    private void transferFromNetwork() {
        if (isRemote() || uuid == null || !isOnline) return;
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

    abstract long getOwnAmount(AEKey what);

    private void setPriority(long priority) {
        current = Math.clamp(priority, PRIORITY_MIN, PRIORITY_MAX);
        onAmountChange(current);
        onChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tickSubs.initialize(getLevel());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        tickSubs.unsubscribe();
    }

    @Override
    protected long getCurrent() {
        return current;
    }

    @Override
    protected void onAmountChange(long amount) {
        IStorageProvider.requestUpdate(getMainNode());
    }

    abstract void tickUpdate();

    public abstract void setUUID(UUID uuid);

    public abstract int getTypes();

    public abstract double getBytes();

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        if (uuid == null) return;
        storageMounts.mount(this, (int) current);
    }

    @Override
    public Component getDescription() {
        return getDefinition().asItem().getDescription();
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void setOnline(final boolean isOnline) {
        this.isOnline = isOnline;
    }

    @Override
    public boolean isOnline() {
        return this.isOnline;
    }

    private static class LONG extends StorageAccessPartMachine {

        private CellDataStorage dataStorage;

        private LONG(MetaMachineBlockEntity holder) {
            super(holder);
        }

        @Override
        public Object getResourceIdentity() {
            return getCellStorage();
        }

        @Override
        long getOwnAmount(AEKey what) {
            var data = getCellStorage();
            if (data == CellDataStorage.EMPTY) return 0;
            var map = data.getStoredMap();
            return map == null ? 0 : map.getAmount(what);
        }

        @Override
        public void setUUID(UUID uuid) {
            this.uuid = uuid;
            dataStorage = null;
            IStorageProvider.requestUpdate(getMainNode());
        }

        @Override
        public int getTypes() {
            if (dataStorage == null || dataStorage.getStoredMap() == null) return 0;
            return dataStorage.getStoredMap().size();
        }

        @Override
        public double getBytes() {
            if (dataStorage == null) return 0;
            return dataStorage.getBytes();
        }

        void tickUpdate() {
            if (dirty) {
                dirty = false;
                getCellStorage().setDirty();
            }
            if (uuid == null || capacity == 0 || !isOnline) return;
            if (!check) {
                counter++;
                if (counter > 600) {
                    counter = 0;
                    capacity = 0;
                    setUUID(null);
                    isInfinite = false;
                }
            }
            if (observe) {
                observe = false;
                double totalAmount = 0;
                var storage = getCellStorage();
                if (storage == CellDataStorage.EMPTY) return;
                var map = storage.getStoredMap();
                if (map != null) {
                    for (var entry : map) {
                        totalAmount += (double) entry.getLongValue() / entry.getKey().getType().getAmountPerByte();
                    }
                }
                storage.setBytes(totalAmount);
            } else if (!isInfinite && getOffsetTimer() % 20 == 7) {
                observe = true;
            }
        }

        protected CellDataStorage getCellStorage() {
            if (dataStorage != null) return dataStorage;
            if (uuid == null || isRemote()) return CellDataStorage.EMPTY;
            dataStorage = CellDataStorage.get(uuid);
            return dataStorage;
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            return isInfinite || capacity > getCellStorage().getBytes();
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (amount == 0 || uuid == null) return 0;
            var data = getCellStorage();
            if (data == CellDataStorage.EMPTY) return 0;
            if (!isInfinite) {
                amount = (long) Math.min(capacity - data.getBytes(), amount);
            }
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
            if (this.transferring) return 0;
            var data = getCellStorage();
            if (data == CellDataStorage.EMPTY) return 0;
            var map = data.getStoredMap();
            if (map == null) return 0;
            if (mode == Actionable.MODULATE) {
                var extract = map.extract(what, amount);
                if (extract > 0) dirty = true;
                return extract;
            } else {
                return Math.min(amount, map.getAmount(what));
            }
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            var data = getCellStorage();
            if (data == CellDataStorage.EMPTY) return;
            out.addAll(data.cache.getAvailableStacksCache());
        }

        @Override
        public KeyCounter getAvailableStacks() {
            var data = getCellStorage();
            if (data == CellDataStorage.EMPTY) KeyCounter.empty();
            return data.cache.getAvailableStacksCache();
        }
    }

    private static class IO extends LONG implements IControllable {

        @SaveToDisk(defaultValue = "false")
        private boolean isWorkingEnabled;
        @SaveToDisk(defaultValue = "false")
        private boolean export;
        @SaveToDisk(defaultValue = "33554432")
        private long rate = 33554432L;

        private final IActionSource mySrc;
        IFancyConfiguratorButton.Toggle toggle;

        private IO(MetaMachineBlockEntity holder) {
            super(holder);
            mySrc = IActionSource.ofMachine(this);
        }

        /** 页面：传输速率（每次最多搬运多少）。 */
        @Override
        UIElement buildPage() {
            var section = UIElement.section();
            section.addChild(MEPartUI.numberRow(LANG_RATE_SETTING, MEPatternPartUI.longField(0, () -> rate, value -> {
                rate = value;
                onChanged();
            }, 0L)));
            addTransferButton(section);
            return MEPartUI.page().addChild(section);
        }

        @Override
        public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
            super.attachConfigurators(configuratorPanel);
            configuratorPanel.attachConfigurators(toggle = new IFancyConfiguratorButton.Toggle(
                    WidgetIcons.IMPORT,

                    WidgetIcons.EXPORT,
                    () -> export,
                    (cd, b) -> export = b

            ));
            if (isRemote()) {
                toggle.setTooltipsSupplier((export) -> Collections.singletonList(export ?
                        Component.translatable(LANG_EXPORT) :
                        Component.translatable(LANG_IMPORT)));
            }
        }

        @Override
        public boolean isWorkingEnabled() {
            return isWorkingEnabled;
        }

        @Override
        public void setWorkingEnabled(boolean isWorkingAllowed) {
            isWorkingEnabled = isWorkingAllowed;
        }

        /// do not allow insertion when exporting and working enabled
        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return (export && isWorkingEnabled) ? 0 : super.insert(what, amount, mode, source);
        }

        /// do not allow extraction when importing and working enabled
        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            return (!export && isWorkingEnabled) ? 0 : super.extract(what, amount, mode, source);
        }

        @Override
        void tickUpdate() {
            super.tickUpdate();
            if (!this.getMainNode().isActive() || !isWorkingEnabled) {
                return;
            }

            // check if the controller has any other storage parts than this one
            if (this.controllers.isEmpty() || Arrays.stream(getController().getParts()).anyMatch(
                    p -> p instanceof StorageAccessPartMachine && p != this)) {
                isWorkingEnabled = false;
                return;
            }

            var grid = getMainNode().getGrid();
            if (grid == null) {
                isWorkingEnabled = false;
                return;
            }

            transferContents(grid);
        }

        private void transferContents(IGrid grid) {
            var networkInv = grid.getStorageService().getInventory();
            long itemsToMove = rate;

            KeyCounter srcList;
            MEStorage src, destination;
            if (export) {
                src = this;
                srcList = this.getAvailableStacks();
                destination = networkInv;
            } else {
                src = networkInv;
                srcList = grid.getStorageService().getCachedInventory();
                destination = this;
            }

            var energy = grid.getEnergyService();
            boolean didStuff;

            do {
                didStuff = false;

                for (var srcEntry : srcList) {
                    var totalStackSize = srcEntry.getLongValue();
                    if (totalStackSize > 0) {
                        var what = srcEntry.getKey();
                        var possible = destination.insert(what, totalStackSize, Actionable.SIMULATE, this.mySrc);

                        if (possible > 0) {
                            possible = src.extract(what, possible, Actionable.MODULATE, this.mySrc);
                            if (possible > 0) {
                                var inserted = StorageHelper.poweredInsert(energy, destination, what, possible, this.mySrc);

                                if (inserted < possible) {
                                    src.insert(what, possible - inserted, Actionable.MODULATE, this.mySrc);
                                }

                                if (inserted > 0) {
                                    itemsToMove -= Math.max(1, inserted / what.getAmountPerOperation());
                                    didStuff = true;
                                }

                                break;
                            }
                        }
                    }
                }
            } while (itemsToMove > 0 && didStuff);
            isWorkingEnabled = didStuff;
        }
    }

    public static final class Big extends StorageAccessPartMachine {

        private BigCellDataStorage dataStorage;

        private Big(MetaMachineBlockEntity holder) {
            super(holder);
        }

        @Override
        public Object getResourceIdentity() {
            return getCellStorage();
        }

        @Override
        long getOwnAmount(AEKey what) {
            var data = getCellStorage();
            if (data == BigCellDataStorage.EMPTY) return 0;
            var map = data.getStoredMap();
            return map == null ? 0 : map.getLongAmount(what);
        }

        @Override
        public void setUUID(UUID uuid) {
            this.uuid = uuid;
            dataStorage = null;
            IStorageProvider.requestUpdate(getMainNode());
        }

        @Override
        public int getTypes() {
            if (dataStorage == null || dataStorage.getStoredMap() == null) return 0;
            return dataStorage.getStoredMap().size();
        }

        @Override
        public double getBytes() {
            if (dataStorage == null) return 0;
            return dataStorage.getBytes();
        }

        void tickUpdate() {
            if (dirty) {
                dirty = false;
                getCellStorage().setDirty();
            }
            if (uuid == null || capacity == 0 || !isOnline) return;
            if (!check) {
                counter++;
                if (counter > 600) {
                    counter = 0;
                    capacity = 0;
                    setUUID(null);
                    isInfinite = false;
                }
            }
            if (observe) {
                observe = false;
                double totalAmount = 0;
                var data = getCellStorage();
                if (data == BigCellDataStorage.EMPTY) return;
                var map = data.getStoredMap();
                if (map != null) {
                    for (var entry : map) {
                        totalAmount += entry.getValue().doubleValue() / entry.getKey().getType().getAmountPerByte();
                    }
                }
                data.setBytes(totalAmount);
            } else if (!isInfinite && getOffsetTimer() % 20 == 7) {
                observe = true;
            }
        }

        public BigCellDataStorage getCellStorage() {
            if (dataStorage != null) return dataStorage;
            if (uuid == null || isRemote()) return BigCellDataStorage.EMPTY;
            dataStorage = BigCellDataStorage.get(uuid);
            return dataStorage;
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            return isInfinite || capacity > getCellStorage().getBytes();
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (amount == 0 || uuid == null) return 0;
            var data = getCellStorage();
            if (data == BigCellDataStorage.EMPTY) return 0;
            if (!isInfinite) {
                amount = (long) Math.min(capacity - data.getBytes(), amount);
            }
            if (amount < 1) return 0;
            if (mode == Actionable.MODULATE) {
                var map = data.getStoredMap();
                if (map == null) {
                    map = new AEKeyBigMap<>();
                    data.setStoredMap(map);
                }
                map.insert(what, BigInteger.valueOf(amount));
                dirty = true;
            }
            return amount;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (this.transferring) return 0;
            var data = getCellStorage();
            if (data == BigCellDataStorage.EMPTY) return 0;
            var map = data.getStoredMap();
            if (map == null) return 0;
            if (mode == Actionable.MODULATE) {
                amount = map.extractLong(what, amount);
                if (amount > 0) dirty = true;
                return amount;
            } else {
                return Math.min(amount, map.getLongAmount(what));
            }
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            var data = getCellStorage();
            if (data == BigCellDataStorage.EMPTY) return;
            out.addAll(data.cache.getAvailableStacksCache());
        }

        @Override
        public KeyCounter getAvailableStacks() {
            var data = getCellStorage();
            if (data == BigCellDataStorage.EMPTY) KeyCounter.empty();
            return data.cache.getAvailableStacksCache();
        }
    }

    public static final class AlgaeAccessHatch extends LONG {

        private AlgaeAccessHatch(MetaMachineBlockEntity holder) {
            super(holder);
            uuid = UUID.randomUUID();
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEItemKey i && Algae.isAlgae(i))) {
                return 0;
            }
            return super.insert(what, amount, mode, source);
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            return (what instanceof AEItemKey i && Algae.isAlgae(i)) && super.isPreferredStorageFor(what, source);
        }
    }

    @RegisterLanguage(cn = "从ME存储器导出", en = "Export from ME Storage")
    private static final String LANG_EXPORT = "gtocore.machine.part.ae.storage_access.export";
    @RegisterLanguage(cn = "导入到ME存储器", en = "Import to ME Storage")
    private static final String LANG_IMPORT = "gtocore.machine.part.ae.storage_access.import";
    @RegisterLanguage(cn = "导入/导出速率设置", en = "Import/Export Rate Setting")
    private static final String LANG_RATE_SETTING = "gtocore.machine.part.ae.storage_access.rate_setting";

    /**
     * 可配置存储访问仓：在普通访问仓之上加两张按 AEKey 记的表——
     * 输入上限（insert 时按它卡，未配置=不限、-1=禁止存入、N=最多 N）与
     * 输出下限（extract 时按它卡，未配置=不限、-1=禁止取出、N=至少保留 N）。
     * <p>
     * 两张表各有一个启用开关，互不影响；都没开时这个仓就是个普通访问仓，一次表查询都不做。
     * 配置面板是弹出面板，用 81 个物品 + 81 个流体虚拟格：点格子选中数量输入框（网格中央）改数量，
     * 「配置」按钮决定改的是哪张表（点按钮同时打开面板）；数量写 0 会按 -1（禁止）记录。
     */
    @DataGeneratorScanned
    static final class Configurable extends LONG {

        static final int CONFIG_SLOTS = 81;
        static final int CONFIG_COLUMNS = 9;
        static final long FORBIDDEN = -1;
        static final String CONFIG_POPUP = "configurable_storage_limits";
        static final int OUTPUT_ARGUMENT = 1;

        @RegisterLanguage(cn = "输入限制", en = "Input Limit")
        private static final String LANG_INPUT_LIMIT = "gtocore.machine.configurable_storage_access_hatch.input_limit";
        @RegisterLanguage(cn = "启用后按输入表卡住存入：每种最多存多少；未配置=不限、0 记成 -1（禁止存入）、正数=上限", en = "When enabled, the input table caps insertion: how much of each key may be stored; unconfigured = unlimited, 0 is stored as -1 (forbidden), positive = the cap")
        private static final String LANG_INPUT_LIMIT_TOOLTIP = "gtocore.machine.configurable_storage_access_hatch.input_limit_tooltip";
        @RegisterLanguage(cn = "开关：是否按输入表限制存入；关着时不看这张表", en = "Switch: whether the input table restricts insertion; while off the table is not read at all")
        private static final String LANG_INPUT_SWITCH_TOOLTIP = "gtocore.machine.configurable_storage_access_hatch.input_switch_tooltip";
        @RegisterLanguage(cn = "输出限制", en = "Output Limit")
        private static final String LANG_OUTPUT_LIMIT = "gtocore.machine.configurable_storage_access_hatch.output_limit";
        @RegisterLanguage(cn = "启用后按输出表卡住取出：每种至少保留多少，库存不够就不取出；未配置=不限、0 记成 -1（禁止取出）、正数=保留下限", en = "When enabled, the output table floors extraction: how much to keep, nothing comes out below it; unconfigured = unlimited, 0 is stored as -1 (never extract), positive = the floor to keep")
        private static final String LANG_OUTPUT_LIMIT_TOOLTIP = "gtocore.machine.configurable_storage_access_hatch.output_limit_tooltip";
        @RegisterLanguage(cn = "开关：是否按输出表限制取出；关着时不看这张表", en = "Switch: whether the output table restricts extraction; while off the table is not read at all")
        private static final String LANG_OUTPUT_SWITCH_TOOLTIP = "gtocore.machine.configurable_storage_access_hatch.output_switch_tooltip";
        @RegisterLanguage(cn = "配置", en = "Configure")
        private static final String LANG_CONFIG = "gtocore.machine.configurable_storage_access_hatch.config";
        @RegisterLanguage(cn = "在弹出面板里编辑这张表（物品 81 格 + 流体 81 格）；要生效先把左边的开关打开", en = "Edit this table in the popup (81 item + 81 fluid slots); turn on the switch on the left to make it take effect")
        private static final String LANG_CONFIG_TOOLTIP = "gtocore.machine.configurable_storage_access_hatch.config_tooltip";
        @RegisterLanguage(cn = "配置格：点格子放东西、在中间的输入框里改数量；数量写 0 = 禁止（记成 -1）、右键清掉格子 = 不限", en = "Config slots: click a slot to set a key, edit the amount in the middle field; 0 means forbidden (stored as -1), right-click clears the slot (unlimited)")
        private static final String LANG_CONFIG_HINT = "gtocore.machine.configurable_storage_access_hatch.config_hint";
        @RegisterLanguage(cn = "物品格", en = "Item Slots")
        private static final String LANG_CONFIG_ITEMS = "gtocore.machine.configurable_storage_access_hatch.config_items";
        @RegisterLanguage(cn = "流体格", en = "Fluid Slots")
        private static final String LANG_CONFIG_FLUIDS = "gtocore.machine.configurable_storage_access_hatch.config_fluids";

        @SaveToDisk
        private final AEKeyMap<AEKey> inputLimits = new AEKeyMap<>();
        @SaveToDisk
        private final AEKeyMap<AEKey> outputLimits = new AEKeyMap<>();
        @SaveToDisk(defaultValue = "false")
        @SyncToClient
        private boolean inputLimitEnabled;
        @SaveToDisk(defaultValue = "false")
        @SyncToClient
        private boolean outputLimitEnabled;
        @SyncToClient
        private boolean editingOutput;
        @SaveToDisk
        private final ExportOnlyAEItemList itemConfig;
        @SaveToDisk
        private final ExportOnlyAEFluidList fluidConfig;
        private boolean loadingConfig;

        private Configurable(MetaMachineBlockEntity holder) {
            super(holder);
            itemConfig = new ExportOnlyAEItemList(this, CONFIG_SLOTS);
            fluidConfig = new ExportOnlyAEFluidList(this, CONFIG_SLOTS);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            long limit = insertLimit(what);
            if (limit == Long.MAX_VALUE) return super.insert(what, amount, mode, source);
            if (limit < 1) return 0; // -1/0：禁止存入，连表都不用查
            if (amount == 0 || uuid == null) return 0;
            var data = getCellStorage();
            if (data == CellDataStorage.EMPTY) return 0;
            if (!isInfinite) {
                amount = (long) Math.min(capacity - data.getBytes(), amount);
            }
            if (amount < 1) return 0;
            if (mode != Actionable.MODULATE) {
                var storedMap = data.getStoredMap();
                long stored = storedMap == null ? 0 : storedMap.getAmount(what);
                return stored >= limit ? 0 : Math.min(amount, limit - stored);
            }
            var map = data.getStoredMap();
            if (map == null) {
                map = new AEKeyMap<>();
                data.setStoredMap(map);
            }
            long inserted = map.insert(what, amount, limit);
            if (inserted < 1) return 0;
            dirty = true;
            return inserted;
        }

        private long insertLimit(AEKey what) {
            if (!inputLimitEnabled) return Long.MAX_VALUE;
            long limit = inputLimits.getAmount(what);
            if (limit == FORBIDDEN) return 0;
            return limit > 0 ? limit : Long.MAX_VALUE;
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            if (inputLimitEnabled) {
                long limit = inputLimits.getAmount(what);
                if (limit == FORBIDDEN) return false;
                if (limit > 0 && getOwnAmount(what) >= limit) return false;
            }
            return super.isPreferredStorageFor(what, source);
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!outputLimitEnabled) return super.extract(what, amount, mode, source);
            long limit = outputLimits.getAmount(what);
            if (limit == FORBIDDEN) return 0;
            if (limit > 0) {
                long movable = getOwnAmount(what) - limit;
                if (movable < 1) return 0;
                amount = Math.min(amount, movable);
            }
            return super.extract(what, amount, mode, source);
        }

        private AEKeyMap<AEKey> editingLimits() {
            return editingOutput ? outputLimits : inputLimits;
        }

        private void onConfigChanged() {
            if (isRemote() || loadingConfig) return;
            saveConfigToLimits();
            onChanged();
        }

        private void saveConfigToLimits() {
            var limits = editingLimits();
            limits.clear();
            collectConfig(limits, itemConfig.getInventory());
            collectConfig(limits, fluidConfig.getInventory());
        }

        private static void collectConfig(AEKeyMap<AEKey> limits, IConfigurableSlot[] slots) {
            for (var slot : slots) {
                var config = slot.getConfig();
                if (config == null || config.what() == null) continue;
                limits.put(config.what(), config.amount() == 0 ? FORBIDDEN : config.amount());
            }
        }

        private void loadConfigFromLimits() {
            loadingConfig = true;
            try {
                int itemIndex = 0;
                int fluidIndex = 0;
                var itemSlots = itemConfig.getInventory();
                var fluidSlots = fluidConfig.getInventory();
                for (var entry : editingLimits()) {
                    // 表里禁止记的是 -1，格子显示成 0（"0 = 禁止"），写回去时还会记回 -1
                    long amount = Math.max(0, entry.getLongValue());
                    var config = new GenericStack(entry.getKey(), amount);
                    if (entry.getKey() instanceof AEItemKey) {
                        if (itemIndex < itemSlots.length) itemSlots[itemIndex++].setConfig(config);
                    } else if (fluidIndex < fluidSlots.length) {
                        fluidSlots[fluidIndex++].setConfig(config);
                    }
                }
                for (int i = itemIndex; i < itemSlots.length; i++) itemSlots[i].setConfig(null);
                for (int i = fluidIndex; i < fluidSlots.length; i++) fluidSlots[i].setConfig(null);
            } finally {
                loadingConfig = false;
            }
        }

        private void switchLimitMode(boolean output) {
            if (isRemote() || editingOutput == output) return;
            saveConfigToLimits();
            editingOutput = output;
            loadConfigFromLimits();
            onChanged();
        }

        @Override
        UIElement buildPage() {
            var page = super.buildPage();
            var section = UIElement.section();
            section.addChildren(limitRow(false), limitRow(true));
            return page.addChild(section);
        }

        private UIElement limitRow(boolean output) {
            var controls = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter())
                    .addChildren(limitSwitch(output), configButton(output));
            return MEPartUI.controlRow(output ? LANG_OUTPUT_LIMIT : LANG_INPUT_LIMIT, controls,
                    output ? LANG_OUTPUT_LIMIT_TOOLTIP : LANG_INPUT_LIMIT_TOOLTIP);
        }

        private Widget limitSwitch(boolean output) {
            return Switch.of(() -> output ? outputLimitEnabled : inputLimitEnabled, value -> {
                if (output) outputLimitEnabled = value;
                else inputLimitEnabled = value;
                onChanged();
            }).bindTooltip(() -> Component.translatable(output ? LANG_OUTPUT_SWITCH_TOOLTIP : LANG_INPUT_SWITCH_TOOLTIP));
        }

        @Override
        protected void registerPopups(MachineWindow window) {
            window.registerPopup(CONFIG_POPUP, argument -> Popup.of(
                    () -> Component.translatable(argument == OUTPUT_ARGUMENT ? LANG_OUTPUT_LIMIT : LANG_INPUT_LIMIT),
                    this::buildConfigPanel));
        }

        /**
         * 弹出面板内容：和仓库里已有的单槽配置面板一样，每片网格各放一个面板区块
         * （区块标题「物品格」「流体格」，说明行放最前面）。
         */
        private void buildConfigPanel(UIElement column) {
            var hint = MEPatternPartUI.section(column, LANG_CONFIG);
            hint.addChild(TextLine.translatable(LayoutStyle.AUTO, LANG_CONFIG_HINT).setColor(UITheme.PANEL_TEXT));
            MEPatternPartUI.section(column, LANG_CONFIG_ITEMS).addChild(configGrid(true));
            MEPatternPartUI.section(column, LANG_CONFIG_FLUIDS).addChild(configGrid(false));
        }

        /** 一片配置格：物品或流体，每行 {@link #CONFIG_COLUMNS} 格（只有配置槽，没有库存半格）。 */
        private Widget configGrid(boolean item) {
            if (item) {
                return new AELimitItemConfigWidget(0, 0, itemConfig, CONFIG_COLUMNS, this::onConfigChanged);
            }
            return new AELimitFluidConfigWidget(0, 0, fluidConfig, CONFIG_COLUMNS, this::onConfigChanged);
        }

        /** 配置按钮：服务端切到这张表，客户端打开配置面板（已打开时原地换成新表的界面）。 */
        private Button configButton(boolean output) {
            var button = Button.translatable(UISizes.BUTTON_WIDTH, LANG_CONFIG)
                    .setSelected(() -> editingOutput == output)
                    .setOnServerClick(() -> switchLimitMode(output))
                    .bindTooltip(() -> Component.translatable(LANG_CONFIG_TOOLTIP));
            // 用按钮自己（一定挂在窗口里）反查窗口，别用建界面时抓的元素：那个可能已经不在树上
            button.setOnClientClick(() -> {
                var window = MachineWindow.of(button);
                if (window != null) window.openPopup(CONFIG_POPUP, output ? OUTPUT_ARGUMENT : 0);
            });
            return button;
        }
    }
}
