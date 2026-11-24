package com.gtocore.api.research.ui;

import com.gtocore.api.research.ExResearchManager;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTree;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.integration.emi.research.EmiResearchHelper;
import com.gtocore.integration.jech.PinYinUtils;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.transfer.forge.MenuItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.StackSizeRenderer;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.datasynclib.util.ByteBufCodecExtends;

import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 配方导出页（数据中心的页面标签、数据导出机的主页）：把已解锁科技节点的配方写进数据物品。
 *
 * <pre>
 * ┌ 区块：[搜索配方主产物 ………………]         ┐
 * ├ 配方网格（每行 9 格，滚动）             ┤  单击选中，再次单击导出；数据中心可用 Shift 选择范围
 * ├ 状态面板：已解锁配方 / 已选配方 / 所需数据物品 ┤
 * ├ [导出研究数据]（整行）                  ┤
 * └ 数据物品输入（9 格） / 数据物品导出（9 格） ┘
 * </pre>
 * 
 * 同步：可导出的配方条目在打开界面时由服务端下发一次（{@link RecipeGrid}），之后只下发"是否解锁 / 是否已含有"的位图与选中项；
 * 位图按科技解锁的修改计数或每秒至多重算一次。搜索只影响本端显示，控件树两端一致。
 * 选中、导出都由服务端按最新的解锁状态校验后执行，导出本身仍是 {@link DataItemHolder#exportSelectedRecipe}。
 */
@DataGeneratorScanned
public class RecipeExportTab implements IFancyUIProvider {

    @RegisterLanguage(cn = "配方导出", en = "Recipe Export")
    public static final String TAB_NAME = "gtocore.research.recipe_export_tab";
    @RegisterLanguage(cn = "未找到已解锁科技对应的配方导出项", en = "No exportable recipes are available for unlocked research.")
    private static final String EMPTY_RECIPES = "gtocore.research.recipe_export_tab.empty";
    @RegisterLanguage(cn = "数据物品输入", en = "Data Item Input")
    private static final String INPUT_LABEL = "gtocore.research.recipe_export_tab.input";
    @RegisterLanguage(cn = "数据物品导出", en = "Data Item Output")
    private static final String OUTPUT_LABEL = "gtocore.research.recipe_export_tab.output";
    @RegisterLanguage(cn = "科技节点：%s", en = "Research Node: %s")
    private static final String NODE_TOOLTIP = "gtocore.research.recipe_export_tab.node";
    @RegisterLanguage(cn = "所需数据物品：%s", en = "Required Data Item: %s")
    private static final String TIER_TOOLTIP = "gtocore.research.recipe_export_tab.tier";
    @RegisterLanguage(cn = "单击选中该配方", en = "Click to select this recipe")
    private static final String SELECT_TOOLTIP = "gtocore.research.recipe_export_tab.select";
    @RegisterLanguage(cn = "再次点击即可导出研究数据", en = "Click again to export research data")
    private static final String EXPORT_TOOLTIP = "gtocore.research.recipe_export_tab.export";
    @RegisterLanguage(cn = "搜索配方主产物", en = "Search recipe outputs")
    private static final String SEARCH_TOOLTIP = "gtocore.research.recipe_export_tab.search";
    private static final Component SEARCH_TOOLTIP_C = Component.translatable(SEARCH_TOOLTIP);
    @RegisterLanguage(cn = "没有找到匹配的配方奖励", en = "No matching recipe rewards were found.")
    private static final String FILTER_EMPTY_RECIPES = "gtocore.research.recipe_export_tab.filter_empty";
    @RegisterLanguage(cn = "已含有", en = "Included")
    private static final String INCLUDED_TOOLTIP = "gtocore.research.recipe_export_tab.included";
    @RegisterLanguage(cn = "该配方数据已包含在数据库中", en = "This recipe data is already included in the database.")
    private static final String INCLUDED_TOOLTIP_DETAIL = "gtocore.research.recipe_export_tab.included.detail";
    @RegisterLanguage(cn = "已解锁配方", en = "Unlocked recipes")
    private static final String LINE_UNLOCKED = "gtocore.research.recipe_export_tab.line.unlocked";
    @RegisterLanguage(cn = "已选配方", en = "Selected")
    private static final String LINE_SELECTED = "gtocore.research.recipe_export_tab.line.selected";
    @RegisterLanguage(cn = "所需数据物品", en = "Data item")
    private static final String LINE_DATA_ITEM = "gtocore.research.recipe_export_tab.line.data_item";
    @RegisterLanguage(cn = "输入槽中没有所需的数据物品", en = "The required data item is not in the input slots")
    private static final String NO_DATA_ITEM = "gtocore.research.recipe_export_tab.no_data_item";
    @RegisterLanguage(cn = "请先在上方选择一个已解锁的配方", en = "Select an unlocked recipe above first")
    private static final String SELECT_FIRST = "gtocore.research.recipe_export_tab.select_first";
    @RegisterLanguage(cn = "导出研究数据", en = "Export Research Data")
    private static final String EXPORT_BUTTON = "gtocore.research.recipe_export_tab.export_button";
    @RegisterLanguage(cn = "存入数据中心", en = "Store in Data Center")
    private static final Component STORE_BUTTON = Component.translatable("gtocore.research.recipe_export_tab.store_button");
    @RegisterLanguage(cn = "导出到缓冲区或背包", en = "Export to Buffer / Inventory")
    private static final Component EXTERNAL_EXPORT_BUTTON = Component.translatable("gtocore.research.recipe_export_tab.external_export_button");
    @RegisterLanguage(cn = "剩余 %s 个空槽", en = "Free slots: %s")
    private static final String FREE_SLOTS_TOOLTIP = "gtocore.research.recipe_export_tab.free_slots";
    @RegisterLanguage(cn = "数据中心已满", en = "Data Center is full")
    private static final Component FULL_TOOLTIP = Component.translatable("gtocore.research.recipe_export_tab.full");
    @RegisterLanguage(cn = "按住 Shift 点击可选择范围", en = "Shift-click to select a range")
    private static final Component RANGE_TOOLTIP = Component.translatable("gtocore.research.recipe_export_tab.range").withStyle(ChatFormatting.GRAY);
    @RegisterLanguage(cn = "%s 个配方", en = "%s recipes")
    private static final String SELECTED_COUNT = "gtocore.research.recipe_export_tab.selected_count";
    @RegisterLanguage(cn = "按配方而定", en = "Varies by recipe")
    private static final Component BATCH_DATA_ITEM = Component.translatable("gtocore.research.recipe_export_tab.batch_data_item");

    private static final Component INPUT_LABEL_C = Component.translatable(INPUT_LABEL);
    private static final Component OUTPUT_LABEL_C = Component.translatable(OUTPUT_LABEL);
    private static final Component LINE_UNLOCKED_C = Component.translatable(LINE_UNLOCKED);
    private static final Component LINE_SELECTED_C = Component.translatable(LINE_SELECTED);
    private static final Component LINE_DATA_ITEM_C = Component.translatable(LINE_DATA_ITEM);
    private static final Component NO_DATA_ITEM_TIP = Component.translatable(NO_DATA_ITEM);
    private static final Component SELECT_FIRST_TIP = Component.translatable(SELECT_FIRST);
    private static final Component EXPORT_BUTTON_LABEL = Component.translatable(EXPORT_BUTTON);

    /** 状态面板里没有值时显示的占位。 */
    private static final String NO_VALUE = "—";
    /// "已含有"角标（格子里的小字）
    private static final Component INCLUDED_LABEL = Component.translatable(INCLUDED_TOOLTIP);
    /// 配方网格默认显示几行，再多滚动（右下角可拖拽缩放）
    private static final int GRID_ROWS = 4;
    /// 搜索框最大长度
    private static final int SEARCH_MAX_LENGTH = 64;

    private final DataItemHolder holder;

    public RecipeExportTab(DataItemHolder holder) {
        this.holder = holder;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var player = widget.getGui().entityPlayer;
        var grid = new RecipeGrid(holder, player, player.level().isClientSide);
        // 配方网格右侧常显滚动条：玩家背包与网格、数据物品槽对齐，只有滚动条伸出来
        if (widget instanceof MachineWindow window) window.setInventoryGutter(ScrollerView.SCROLL_BAR_SPACE);
        var page = Form.page();

        // 筛选：只影响本端显示，搜索框是纯客户端控件（两端都建，不参与同步）
        var search = TextField.of(LayoutStyle.AUTO, () -> grid.search, grid::setSearch)
                .bindClientPlaceholder(() -> SEARCH_TOOLTIP_C);
        search.tooltips(SEARCH_TOOLTIP_C);
        search.getInput().setMaxStringLength(SEARCH_MAX_LENGTH);
        search.setClientSideWidget();
        var filter = UIElement.section().addChild(search);

        // 固定高度、常显滚动条：搜索时格子数变化，窗口不跟着伸缩跳动（右下角仍可拖拽缩放）
        var scroller = new ScrollerView("research.recipe_export", UISizes.SLOT_ROW_WIDTH + ScrollerView.SCROLL_BAR_SPACE, GRID_ROWS * UISizes.SLOT_SIZE)
                .setVerticalScrollDisplay(ScrollerView.ScrollDisplay.ALWAYS);
        scroller.addScrollViewChild(grid);
        page.addChild(UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(filter, scroller));

        // 当前选择：状态面板 + 紧贴其下的整行导出按钮。没选中时整块禁用，选中了但缺数据物品时按钮禁用（原因不同）
        var status = new StatusPanel();
        status.addLine(LINE_UNLOCKED_C, grid::getUnlockedText);
        status.addLine(LINE_SELECTED_C, grid::getSelectedName).bindIcon(grid::getSelectedOutputIcon);
        status.addLine(LINE_DATA_ITEM_C, grid::getSelectedDataItemName).bindIcon(grid::getSelectedDataItemIcon)
                .bindLevel(() -> {
                    if (!grid.hasSelection()) return Level.NORMAL;
                    return grid.hasSelectedDataItem() ? Level.GOOD : Level.WARNING;
                })
                .bindDetail(() -> grid.hasSelection() && !grid.hasSelectedDataItem() ? NO_DATA_ITEM_TIP : Component.empty());
        var export = Button.text(LayoutStyle.AUTO, EXPORT_BUTTON_LABEL)
                .setVariant(UITheme.ButtonVariant.CONFIRM)
                .setOnServerClick(grid::exportSelected)
                .disabled(() -> grid.hasSelection() && !grid.hasSelectedDataItem(), NO_DATA_ITEM_TIP);
        if (holder.supportsBatchExport()) {
            export.tooltips(FULL_TOOLTIP);
            var freeSlots = export.addSyncValue(SyncValue.ofInt(holder::getFreeInternalExportSlots, 0)
                    .onChanged(count -> export.tooltips(count > 0 ? Component.translatable(FREE_SLOTS_TOOLTIP, count) : FULL_TOOLTIP)));
            export.bindClientText(() -> (freeSlots.getValue() > 0 ? STORE_BUTTON : EXTERNAL_EXPORT_BUTTON).getString());
        }
        var selection = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(status, export);
        selection.disabled(() -> !grid.hasSelection(), SELECT_FIRST_TIP);
        page.addChild(selection);

        // 数据物品：输入可取可放，导出只能取
        page.addChild(UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                TextLine.constant(LayoutStyle.AUTO, INPUT_LABEL_C),
                slotGrid(holder.getDataItemStorage(), true),
                TextLine.constant(LayoutStyle.AUTO, OUTPUT_LABEL_C),
                slotGrid(holder.getDataOutputStorage(), false)));
        return page;
    }

    /** 每行 9 格的物品槽；槽数取机器的物品栏（两端相同）。 */
    private static UIElement slotGrid(KeyInventory<AEItemKey> inventory, boolean canPut) {
        var handler = new MenuItemAdapter(inventory);
        return SlotGrid.of(UISizes.SLOTS_PER_ROW, inventory.size(), slot -> ItemSlot.of(handler, slot, true, canPut));
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTItems.TOOL_DATA_ORB.asItem());
    }

    @Override
    public Component getTitle() {
        return Component.translatable(TAB_NAME);
    }

    @Override
    public List<Component> getTabTooltips() {
        return Collections.singletonList(Component.translatable(TAB_NAME));
    }

    private static boolean isConvertibleDataItem(@Nullable AEItemKey key, ItemStack expectedTierItem) {
        return key != null && key.getItem() == expectedTierItem.getItem();
    }

    /** 输入物品栏里是否有可写入的该种数据物品（与 {@link DataItemHolder#exportSelectedRecipe} 的判定相同）。 */
    private static boolean hasConvertibleDataItem(KeyInventory<AEItemKey> input, ItemStack tierItem) {
        if (tierItem.isEmpty()) return false;
        for (int slot = 0; slot < input.size(); slot++) {
            if (isConvertibleDataItem(input.keyAt(slot), tierItem)) return true;
        }
        return false;
    }

    // ==================== 配方网格 ====================

    /** 一个可导出的配方：所属节点与配方（客户端解析不到时为 null，仍占位以保证两端下标一致）。 */
    private static final class Entry {

        @Nullable
        final TechNode node;
        @Nullable
        final GTRecipeDefinition recipe;
        /// 以下只在客户端使用
        @Nullable
        AEKey output;
        @Nullable
        String searchName;

        Entry(@Nullable TechNode node, @Nullable GTRecipeDefinition recipe) {
            this.node = node;
            this.recipe = recipe;
        }
    }

    private static final StreamCodec<FriendlyByteBuf, BitSet> BITSET = StreamCodec.convert(ByteBufCodecs.LONG_ARRAY, BitSet::toLongArray, BitSet::valueOf);
    private static final int[] NO_SELECTION = new int[0];

    /**
     * 配方网格：一个控件画出全部格子（条目可能上百个，不为每格建控件），点击时把条目下标发给服务端。
     * <ul>
     * <li>条目列表：服务端建页时按科技树顺序收集，随初始数据下发（节点用注册 id，配方用配方 id）；界面打开期间不变，下标两端一致。</li>
     * <li>状态位图（每条两位：已解锁、已含有）与选中项由服务端经 {@link SyncValue} 下发；位图按科技解锁修改计数或每秒至多重算一次。</li>
     * <li>显示顺序、搜索只在客户端计算：未含有的在前。</li>
     * </ul>
     * 选中项是"这一个打开的界面"的状态，存在本控件里（服务端），不进机器字段。
     */
    private static final class RecipeGrid extends UIElement implements IIngredientSlot {

        /// 位图最多每隔多少 tick 重算一次（"已含有"取决于数据访问仓内容，没有修改计数可用）
        private static final int REFRESH_TICKS = 20;

        private final DataItemHolder holder;
        @Nullable
        private final Player player;
        private final boolean remote;
        private final List<Entry> entries = new ArrayList<>();
        private final SyncValue<BitSet> flags;
        private final RPC<Integer> click;
        @Nullable
        private final RPC<int[]> rangeClick;

        // ---- 服务端 ----
        @Nullable
        private BitSet serverFlags;
        private int flagsModCount;
        private int ticksSinceRefresh;
        private int unlockedCount;
        private int unlockedTextCount = -1;
        private Component unlockedText = Component.empty();
        private int[] selection = NO_SELECTION;
        private int[] availabilitySelection = NO_SELECTION;
        private int availabilityVersion = -1;
        private boolean selectedDataAvailable;
        private int selectedTextCount;
        private Component selectedCountText = Component.empty();
        /// 已选条目的名称、数据物品，只在选中项变化时重新取
        private int memoIndex = -1;
        private Component memoName = Component.literal(NO_VALUE);
        private ItemStack memoOutput = ItemStack.EMPTY;
        private ItemStack memoTierItem = ItemStack.EMPTY;

        // ---- 客户端 ----
        String search = "";
        private final IntArrayList visible = new IntArrayList();
        private final BitSet selectedSlots = new BitSet();
        private int anchor = -1;
        private int clientSelectionSize;
        private int tooltipIndex = -1;
        @Nullable
        private BitSet tooltipFlags;
        private List<Component> tooltip = Collections.emptyList();

        RecipeGrid(DataItemHolder holder, @Nullable Player player, boolean remote) {
            this.holder = holder;
            this.player = player;
            this.remote = remote;
            layout(l -> l.size(UISizes.SLOT_ROW_WIDTH, UISizes.SLOT_SIZE));
            this.flags = addSyncValue(SyncValue.of(this::currentFlags, BITSET, new BitSet()).onChanged(value -> refilter()));
            addSyncValue(SyncValue.of(() -> selection, ByteBufCodecs.VAR_INT_ARRAY, NO_SELECTION).onChanged(this::updateSelectedSlots));
            this.click = addRPC(ByteBufCodecs.VAR_INT, (clicker, index) -> clickEntry(index))
                    .validate(index -> index >= 0 && index < entries.size());
            this.rangeClick = holder.supportsBatchExport() ? addRPC(ByteBufCodecs.VAR_INT_ARRAY, (clicker, indices) -> selectRange(indices))
                    .validate(indices -> indices.length > 0 && indices.length <= entries.size()) : null;
            if (!remote) collectEntries();
        }

        /** 服务端：收集所有带可显示主产物的节点配方（与原实现同一顺序）。 */
        private void collectEntries() {
            for (var manager : TechTreeManager.getManagers()) {
                for (TechNode node : manager.getLayout().orderedNodes()) {
                    for (GTRecipeDefinition recipe : node.getRecipes()) {
                        if (ExResearchManager.hasRenderableMainOutput(recipe)) entries.add(new Entry(node, recipe));
                    }
                }
            }
        }

        // ==================== 同步 ====================

        /** 条目按节点分组下发：[组数] × ([节点] [配方数] × [配方 id])。 */
        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            int groups = 0;
            for (int i = 0; i < entries.size(); i++) {
                if (i == 0 || entries.get(i).node != entries.get(i - 1).node) groups++;
            }
            buffer.writeVarInt(groups);
            int start = 0;
            while (start < entries.size()) {
                var node = entries.get(start).node;
                int end = start;
                while (end < entries.size() && entries.get(end).node == node) end++;
                GTOCodecs.TECH_NODE_STREAM_CODEC.encode(buffer, node);
                buffer.writeVarInt(end - start);
                for (int i = start; i < end; i++) ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.encode(buffer, entries.get(i).recipe.id);
                start = end;
            }
            super.writeInitialData(buffer);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            entries.clear();
            int groups = buffer.readVarInt();
            for (int g = 0; g < groups; g++) {
                TechNode node = GTOCodecs.TECH_NODE_STREAM_CODEC.decode(buffer);
                int count = buffer.readVarInt();
                for (int i = 0; i < count; i++) {
                    ResourceLocation id = ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.decode(buffer);
                    var recipe = findRecipe(node, id);
                    var entry = new Entry(node, recipe);
                    if (recipe != null) entry.output = ExResearchManager.getMainItemOutput(recipe);
                    entries.add(entry);
                }
            }
            super.readInitialData(buffer);
            refilter();
        }

        @Nullable
        private static GTRecipeDefinition findRecipe(@Nullable TechNode node, ResourceLocation id) {
            if (node == null) return null;
            for (var recipe : node.getRecipes()) {
                if (id.equals(recipe.id)) return recipe;
            }
            return null;
        }

        @Override
        public void detectAndSendChanges() {
            if (!remote) ticksSinceRefresh++;
            super.detectAndSendChanges();
        }

        // ==================== 服务端：状态 ====================

        /** 状态位图：第 2i 位已解锁，第 2i+1 位已含有。按解锁修改计数或每秒至多重算一次。 */
        private BitSet currentFlags() {
            int modCount = TechTreeSavedData.getModCount();
            if (serverFlags == null || modCount != flagsModCount || ticksSinceRefresh >= REFRESH_TICKS) {
                refreshFlags(modCount);
            }
            return serverFlags;
        }

        private void refreshFlags(int modCount) {
            flagsModCount = modCount;
            ticksSinceRefresh = 0;
            var bits = new BitSet(entries.size() * 2);
            Set<GTRecipeDefinition> exist = holder.getExistRecipes();
            TechTreeManager lastManager = null;
            TechTree tree = null;
            int unlocked = 0;
            for (int i = 0; i < entries.size(); i++) {
                var entry = entries.get(i);
                if (entry.node == null) continue;
                var manager = entry.node.getManager();
                if (manager != lastManager) {
                    lastManager = manager;
                    tree = player == null ? null : TechTreeSavedData.findTree(player, manager);
                }
                if (tree != null && tree.getUnlockedNodes().contains(entry.node)) {
                    bits.set(2 * i);
                    unlocked++;
                }
                if (exist.contains(entry.recipe)) bits.set(2 * i + 1);
            }
            unlockedCount = unlocked;
            // 内容没变时沿用旧对象，同步值比较时直接相等
            if (!bits.equals(serverFlags)) serverFlags = bits;
            pruneSelection(bits);
        }

        /** 服务端：该条目所在节点此刻是否已解锁（与位图的判定相同，不走缓存）。 */
        private boolean isUnlockedNow(Entry entry) {
            if (entry.node == null || player == null) return false;
            var tree = TechTreeSavedData.findTree(player, entry.node.getManager());
            return tree != null && tree.getUnlockedNodes().contains(entry.node);
        }

        private void pruneSelection(BitSet bits) {
            int count = 0;
            for (int index : selection) {
                if (bits.get(2 * index)) count++;
            }
            if (count == selection.length) return;
            var valid = count == 0 ? NO_SELECTION : new int[count];
            int slot = 0;
            for (int index : selection) {
                if (bits.get(2 * index)) valid[slot++] = index;
            }
            selection = valid;
        }

        boolean hasSelection() {
            return selection.length != 0;
        }

        private void updateMemo() {
            int index = selection.length == 1 ? selection[0] : -1;
            if (index == memoIndex) return;
            memoIndex = index;
            var entry = index < 0 ? null : entries.get(index);
            if (entry == null || entry.node == null || entry.recipe == null) {
                memoName = Component.literal(NO_VALUE);
                memoOutput = ItemStack.EMPTY;
                memoTierItem = ItemStack.EMPTY;
            } else {
                memoName = ExResearchManager.getMainOutputDisplayName(entry.recipe);
                memoOutput = mainOutputIcon(entry.recipe);
                memoTierItem = entry.node.getTierItem();
            }
        }

        /** 配方主产物的图标：物品本身，流体用装满它的桶（没有桶的流体不显示）。 */
        private static ItemStack mainOutputIcon(GTRecipeDefinition recipe) {
            if (!recipe.itemOutputs.isEmpty()) {
                return recipe.itemOutputs.ingredient(0).displayKey() instanceof AEItemKey key ? key.toStack() : ItemStack.EMPTY;
            }
            if (!recipe.fluidOutputs.isEmpty()) {
                return recipe.fluidOutputs.ingredient(0).displayKey() instanceof AEFluidKey key ? FluidUtil.getFilledBucket(Keys.toFluidStack(key, recipe.fluidOutputs.amount(0))) : ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }

        /** 已选配方主产物的图标（状态行里显示，服务端取值下发）。 */
        ItemStack getSelectedOutputIcon() {
            updateMemo();
            return memoOutput;
        }

        /** 已选配方所需数据物品的图标。 */
        ItemStack getSelectedDataItemIcon() {
            updateMemo();
            return memoTierItem;
        }

        Component getUnlockedText() {
            currentFlags();
            if (unlockedCount != unlockedTextCount) {
                unlockedTextCount = unlockedCount;
                unlockedText = Component.literal(unlockedCount + " / " + entries.size());
            }
            return unlockedText;
        }

        Component getSelectedName() {
            if (selection.length > 1) {
                if (selectedTextCount != selection.length) {
                    selectedTextCount = selection.length;
                    selectedCountText = Component.translatable(SELECTED_COUNT, selectedTextCount);
                }
                return selectedCountText;
            }
            updateMemo();
            return memoName;
        }

        Component getSelectedDataItemName() {
            if (selection.length > 1) return BATCH_DATA_ITEM;
            updateMemo();
            return memoTierItem.isEmpty() ? Component.literal(NO_VALUE) : memoTierItem.getHoverName();
        }

        boolean hasSelectedDataItem() {
            var input = holder.getDataItemStorage();
            int version = input.version();
            if (selection != availabilitySelection || version != availabilityVersion) {
                availabilitySelection = selection;
                availabilityVersion = version;
                selectedDataAvailable = false;
                for (int index : selection) {
                    if (hasConvertibleDataItem(input, entries.get(index).node.getTierItem())) {
                        selectedDataAvailable = true;
                        break;
                    }
                }
            }
            return selectedDataAvailable;
        }

        /** 服务端：导出已选配方（导出按钮、再次点击已选格子）。条件不满足时什么也不做。 */
        void exportSelected() {
            boolean batch = selection.length > 1;
            for (int index : selection) {
                var entry = entries.get(index);
                if (entry.recipe == null || !isUnlockedNow(entry)) continue;
                var tierItem = entry.node.getTierItem();
                if (hasConvertibleDataItem(holder.getDataItemStorage(), tierItem)) {
                    holder.exportSelectedRecipe(tierItem, entry.recipe, player, batch);
                }
            }
        }

        private void clickEntry(int index) {
            // 客户端的解锁状态可能滞后：只按最新数据校验被点的这一条
            if (!isUnlockedNow(entries.get(index))) return;
            if (selection.length == 1 && selection[0] == index) exportSelected();
            else selection = new int[] { index };
        }

        private void selectRange(int[] indices) {
            var seen = new BitSet(entries.size());
            for (int index : indices) {
                if (index < 0 || index >= entries.size() || seen.get(index) || !isUnlockedNow(entries.get(index))) return;
                seen.set(index);
            }
            if (!Arrays.equals(selection, indices)) selection = indices;
        }

        // ==================== 客户端：显示 ====================

        private boolean unlocked(int index) {
            return flags.getValue().get(2 * index);
        }

        private boolean included(int index) {
            return flags.getValue().get(2 * index + 1);
        }

        void setSearch(String text) {
            String normalized = text == null ? "" : text;
            if (normalized.equals(search)) return;
            search = normalized;
            refilter();
        }

        /** 客户端：按筛选条件重排可见条目，并按行数改高度。 */
        private void refilter() {
            if (!remote) return;
            visible.clear();
            String normalized = search.trim().toLowerCase(Locale.ROOT);
            for (int i = 0; i < entries.size(); i++) {
                if (!unlocked(i)) continue;
                if (!normalized.isEmpty() && !PinYinUtils.match(searchName(entries.get(i)), normalized)) continue;
                visible.add(i);
            }
            // 未含有的在前；同组保持科技树顺序（归并排序是稳定的）
            IntArrays.mergeSort(visible.elements(), 0, visible.size(), (a, b) -> Boolean.compare(included(a), included(b)));
            int rows = Math.max(1, (visible.size() + UISizes.SLOTS_PER_ROW - 1) / UISizes.SLOTS_PER_ROW);
            layout(l -> l.height(rows * UISizes.SLOT_SIZE));
            tooltipIndex = -1;
        }

        private void updateSelectedSlots(int[] indices) {
            if (!remote) return;
            selectedSlots.clear();
            for (int index : indices) selectedSlots.set(index);
            clientSelectionSize = indices.length;
            tooltipIndex = -1;
        }

        private static String searchName(Entry entry) {
            if (entry.searchName == null) {
                entry.searchName = entry.recipe == null ? "" :
                        ExResearchManager.getMainOutputDisplayName(entry.recipe).getString().toLowerCase(Locale.ROOT);
            }
            return entry.searchName;
        }

        /** 鼠标下的条目下标（视口外、空格为 -1）。 */
        private int entryAt(double mouseX, double mouseY) {
            int position = visiblePositionAt(mouseX, mouseY);
            return position < 0 ? -1 : visible.getInt(position);
        }

        private int visiblePositionAt(double mouseX, double mouseY) {
            if (!isMouseOverElement(mouseX, mouseY) || !inViewport(mouseX, mouseY)) return -1;
            int column = (int) (mouseX - getPositionX()) / UISizes.SLOT_SIZE, row = (int) (mouseY - getPositionY()) / UISizes.SLOT_SIZE;
            if (column < 0 || column >= UISizes.SLOTS_PER_ROW || row < 0) return -1;
            int k = row * UISizes.SLOTS_PER_ROW + column;
            return k < visible.size() ? k : -1;
        }

        /// 所在的滚动区（网格总放在滚动区里，找不到时按不裁剪处理）
        @Nullable
        private ScrollerView scroller() {
            for (Widget w = getParent(); w != null; w = w.getParent()) {
                if (w instanceof ScrollerView scroller) return scroller;
            }
            return null;
        }

        /// 鼠标是否在滚动区视口里（被裁掉的格子不响应悬停和点击）
        private boolean inViewport(double mouseX, double mouseY) {
            var scroller = scroller();
            return scroller == null || scroller.isMouseOverElement(mouseX, mouseY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            int x0 = getPositionX(), y0 = getPositionY();
            var font = Minecraft.getInstance().font;
            if (visible.isEmpty()) {
                var key = search.isBlank() ? EMPTY_RECIPES : FILTER_EMPTY_RECIPES;
                graphics.drawString(font, UIText.fit(Component.translatable(key).getString(), getSizeWidth()),
                        x0, UIText.centerY(y0, UISizes.SLOT_SIZE), UITheme.TEXT_SECONDARY, false);
                return;
            }
            // 只画视口里的行
            var scroller = scroller();
            int top = scroller == null ? Integer.MIN_VALUE : scroller.getPositionY();
            int bottom = scroller == null ? Integer.MAX_VALUE : top + scroller.getSizeHeight();
            int hovered = entryAt(mouseX, mouseY);
            int firstRow = top <= y0 ? 0 : (top - y0) / UISizes.SLOT_SIZE;
            int from = Math.min(visible.size(), firstRow * UISizes.SLOTS_PER_ROW);
            for (int k = from; k < visible.size(); k++) {
                int x = x0 + k % UISizes.SLOTS_PER_ROW * UISizes.SLOT_SIZE, y = y0 + k / UISizes.SLOTS_PER_ROW * UISizes.SLOT_SIZE;
                if (y >= bottom) break;
                int index = visible.getInt(k);
                var entry = entries.get(index);
                UITheme.ITEM_SLOT.draw(graphics, mouseX, mouseY, x, y, UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
                if (entry.output != null) AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, x + 1, y + 1, entry.output);
                else graphics.drawString(font, "?", x + 6, y + 5, UITheme.TEXT, false);
                if (included(index)) {
                    StackSizeRenderer.renderSizeLabel(graphics, font, x + 1, y + 17 - font.lineHeight * 0.5f,
                            INCLUDED_LABEL, 0.5f, true, true);
                }
                if (index == hovered) UIDraw.hoverOverlay(graphics, x, y, UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            // 选中框画在前景层，外扩的 1 像素不被相邻格子盖住；滚出视口时不画
            if (!selectedSlots.isEmpty()) {
                int x0 = getPositionX(), y0 = getPositionY();
                var scroller = scroller();
                int top = scroller == null ? y0 : scroller.getPositionY();
                int bottom = scroller == null ? Integer.MAX_VALUE : top + scroller.getSizeHeight();
                int firstRow = top <= y0 ? 0 : Math.ceilDiv(top - y0, UISizes.SLOT_SIZE);
                for (int k = firstRow * UISizes.SLOTS_PER_ROW; k < visible.size(); k++) {
                    int y = y0 + k / UISizes.SLOTS_PER_ROW * UISizes.SLOT_SIZE;
                    if (y + UISizes.SLOT_SIZE > bottom) break;
                    if (selectedSlots.get(visible.getInt(k))) {
                        int x = x0 + k % UISizes.SLOTS_PER_ROW * UISizes.SLOT_SIZE;
                        UIDraw.selectionFrame(graphics, x, y, UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
                    }
                }
            }
            int hovered = entryAt(mouseX, mouseY);
            if (hovered < 0 || gui == null || gui.getModularUIGui() == null) return;
            gui.getModularUIGui().setHoverTooltip(tooltipFor(hovered), ItemStack.EMPTY, null, null);
        }

        /** 悬停提示：产物名、科技节点、所需数据物品，再按状态给操作提示；按（条目、选中项、位图）缓存。 */
        @OnlyIn(Dist.CLIENT)
        private List<Component> tooltipFor(int index) {
            var currentFlags = flags.getValue();
            if (index == tooltipIndex && currentFlags == tooltipFlags) return tooltip;
            tooltipIndex = index;
            tooltipFlags = currentFlags;
            var entry = entries.get(index);
            var lines = new ArrayList<Component>(7);
            if (entry.node == null || entry.recipe == null) {
                lines.add(Component.literal(NO_VALUE));
                tooltip = lines;
                return tooltip;
            }
            lines.add(ExResearchManager.getMainOutputDisplayName(entry.recipe).copy().withStyle(ChatFormatting.WHITE));
            lines.add(Component.translatable(NODE_TOOLTIP, entry.node.getDisplayName()).withStyle(ChatFormatting.GRAY));
            var tierItem = entry.node.getTierItem();
            if (!tierItem.isEmpty()) lines.add(Component.translatable(TIER_TOOLTIP, tierItem.getHoverName()).withStyle(ChatFormatting.GRAY));
            boolean isSelected = clientSelectionSize == 1 && selectedSlots.get(index);
            lines.add(Component.translatable(isSelected ? EXPORT_TOOLTIP : SELECT_TOOLTIP)
                    .withStyle(isSelected ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            if (rangeClick != null) lines.add(RANGE_TOOLTIP);
            if (included(index)) lines.add(Component.translatable(INCLUDED_TOOLTIP_DETAIL).withStyle(ChatFormatting.GOLD));
            tooltip = lines;
            return tooltip;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int position = visiblePositionAt(mouseX, mouseY);
            if (position < 0 || button != 0) return super.mouseClicked(mouseX, mouseY, button);
            int index = visible.getInt(position);
            if (rangeClick != null && Screen.hasShiftDown()) {
                int start = visible.indexOf(anchor);
                if (start < 0) {
                    anchor = index;
                    start = position;
                }
                int from = Math.min(start, position), to = Math.max(start, position);
                rangeClick.send(Arrays.copyOfRange(visible.elements(), from, to + 1));
            } else {
                anchor = index;
                click.send(index);
            }
            playButtonClickSound();
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public @Nullable Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
            int index = entryAt(mouseX, mouseY);
            if (index < 0) return null;
            var output = entries.get(index).output;
            return output == null ? null : EmiResearchHelper.toEmiStack(output);
        }
    }

    public interface DataItemHolder {

        KeyInventory<AEItemKey> getDataItemStorage();

        KeyInventory<AEItemKey> getDataOutputStorage();

        void exportSelectedRecipe(ItemStack dataStack, GTRecipeDefinition recipe, @Nullable Player player, boolean batch);

        default boolean supportsBatchExport() {
            return false;
        }

        default int getFreeInternalExportSlots() {
            return 0;
        }

        default Set<GTRecipeDefinition> getExistRecipes() {
            return Collections.emptySet();
        }
    }
}
