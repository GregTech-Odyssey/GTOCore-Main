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
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.uipro.ElementState;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.StackSizeRenderer;

import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.vfyjxf.taffy.style.FlexWrap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
 * │       显示未解锁奖励 ……………… [开关]     │
 * ├ 配方网格（每行 9 格，滚动）             ┤  单击选中，再次单击导出；未解锁的格子叠斜纹（禁止操作）
 * ├ 状态面板：已解锁配方 / 已选配方 / 所需数据物品 ┤
 * ├ [导出研究数据]（整行）                  ┤
 * └ 数据物品输入（9 格） / 数据物品导出（9 格） ┘
 * </pre>
 * 同步：可导出的配方条目在打开界面时由服务端下发一次（{@link RecipeGrid}），之后只下发"是否解锁 / 是否已含有"的位图与选中项；
 * 位图按科技解锁的修改计数或每秒至多重算一次。搜索和"显示未解锁"只影响本端显示，控件树两端一致。
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
    @RegisterLanguage(cn = "显示未解锁奖励", en = "Show Locked")
    private static final String SHOW_LOCKED_LABEL = "gtocore.research.recipe_export_tab.show_locked";
    @RegisterLanguage(cn = "显示未解锁科技节点的奖励（仅预览）", en = "Show rewards from locked research nodes (preview only)")
    private static final String SHOW_LOCKED_TOOLTIP = "gtocore.research.recipe_export_tab.show_locked.tooltip";
    @RegisterLanguage(cn = "请先解锁该科技节点，然后再导出这个奖励", en = "Unlock this research node before exporting this reward")
    private static final String LOCKED_TOOLTIP = "gtocore.research.recipe_export_tab.locked";
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
        var page = UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));

        // 筛选：只影响本端显示，搜索框与开关都是纯客户端控件（两端都建，不参与同步）
        var search = new TextField(LayoutStyle.AUTO, () -> grid.search, grid::setSearch)
                .setPlaceholder(() -> Component.translatable(SEARCH_TOOLTIP));
        search.setHoverTooltips(Component.translatable(SEARCH_TOOLTIP));
        search.getInput().setMaxStringLength(SEARCH_MAX_LENGTH);
        search.setClientSideWidget();
        var showLocked = Switch.of(() -> grid.showLocked, grid::setShowLocked);
        showLocked.setHoverTooltips(Component.translatable(SHOW_LOCKED_TOOLTIP));
        showLocked.setClientSideWidget();
        var showLockedLabel = TextLine.translatable(0, SHOW_LOCKED_LABEL).setColor(UITheme.PANEL_TEXT);
        showLockedLabel.layout(l -> l.flex(1));
        showLockedLabel.setHoverTooltips(Component.translatable(SHOW_LOCKED_TOOLTIP));
        var filter = UIElement.section().addChildren(search,
                UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChildren(showLockedLabel, showLocked));

        // 固定高度、常显滚动条：搜索、切换开关时格子数变化，窗口不跟着伸缩跳动（右下角仍可拖拽缩放）
        var scroller = new ScrollerView("research.recipe_export", UISizes.SLOT_ROW_WIDTH + ScrollerView.SCROLL_BAR_SPACE, GRID_ROWS * UISizes.SLOT)
                .verticalScrollDisplay(ScrollerView.ScrollDisplay.ALWAYS);
        scroller.addScrollViewChild(grid);
        page.addChild(UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(filter, scroller));

        // 当前选择：状态面板 + 紧贴其下的整行导出按钮。没选中时整块禁用，选中了但缺数据物品时按钮禁用（原因不同）
        var status = new StatusPanel();
        status.addLine(LINE_UNLOCKED, grid::unlockedText);
        status.addLine(LINE_SELECTED, grid::selectedName).icon(grid::selectedOutputIcon);
        status.addLine(LINE_DATA_ITEM, grid::selectedDataItemName).icon(grid::selectedDataItemIcon)
                .level(() -> {
                    if (grid.selectedEntry() == null) return StatusLine.Level.NORMAL;
                    return grid.hasSelectedDataItem() ? StatusLine.Level.GOOD : StatusLine.Level.WARNING;
                })
                .detail(() -> grid.selectedEntry() != null && !grid.hasSelectedDataItem() ? Component.translatable(NO_DATA_ITEM) : Component.empty());
        var export = Button.translatable(LayoutStyle.AUTO, EXPORT_BUTTON)
                .setVariant(UITheme.ButtonVariant.CONFIRM)
                .setOnServerClick(grid::exportSelected)
                .disabled(() -> grid.selectedEntry() != null && !grid.hasSelectedDataItem(), NO_DATA_ITEM);
        var selection = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(status, export);
        selection.disabled(() -> grid.selectedEntry() == null, SELECT_FIRST);
        page.addChild(selection);

        // 数据物品：输入可取可放，导出只能取
        page.addChild(UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                TextLine.translatable(LayoutStyle.AUTO, INPUT_LABEL),
                slotGrid(holder.getDataItemStorage(), true),
                TextLine.translatable(LayoutStyle.AUTO, OUTPUT_LABEL),
                slotGrid(holder.getDataOutputStorage(), false)));
        return page;
    }

    /** 每行 9 格的物品槽；槽数取机器的物品栏（两端相同）。 */
    private static UIElement slotGrid(ICustomItemStackHandler handler, boolean canPut) {
        var grid = new UIElement().layout(l -> l.row().flexWrap(FlexWrap.WRAP).width(UISizes.SLOT_ROW_WIDTH));
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            grid.addChild(new ItemSlot(handler, slot, true, canPut));
        }
        return grid;
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

    private static boolean isConvertibleDataItem(ItemStack stack, ItemStack expectedTierItem) {
        return !stack.isEmpty() &&
                stack.is(expectedTierItem.getItem());
    }

    /** 输入物品栏里是否有可写入的该种数据物品（与 {@link DataItemHolder#exportSelectedRecipe} 的判定相同）。 */
    private static boolean hasConvertibleDataItem(ICustomItemStackHandler input, ItemStack tierItem) {
        for (int slot = 0; slot < input.getSlots(); slot++) {
            if (isConvertibleDataItem(input.getStackInSlot(slot), tierItem)) return true;
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

    private static final SyncValue.Codec<BitSet> BITSET = new SyncValue.Codec<>() {

        @Override
        public void write(FriendlyByteBuf buf, BitSet value) {
            buf.writeLongArray(value.toLongArray());
        }

        @Override
        public BitSet read(FriendlyByteBuf buf) {
            return BitSet.valueOf(buf.readLongArray());
        }
    };

    /**
     * 配方网格：一个控件画出全部格子（条目可能上百个，不为每格建控件），点击时把条目下标发给服务端。
     * <ul>
     * <li>条目列表：服务端建页时按科技树顺序收集，随初始数据下发（节点用注册 id，配方用配方 id）；界面打开期间不变，下标两端一致。</li>
     * <li>状态位图（每条两位：已解锁、已含有）与选中项由服务端经 {@link SyncValue} 下发；位图按科技解锁修改计数或每秒至多重算一次。</li>
     * <li>显示顺序、搜索、"显示未解锁"只在客户端计算：已解锁在前，其中未含有的在前。</li>
     * </ul>
     * 选中项是"这一个打开的界面"的状态，存在本控件里（服务端），不进机器字段。
     */
    private static final class RecipeGrid extends UIElement implements IIngredientSlot {

        /// 客户端请求：点击第 n 个条目。避开 WidgetGroup 自用的 1（子控件路由）
        private static final int ACTION_CLICK = 0x5B01;
        /// 位图最多每隔多少 tick 重算一次（"已含有"取决于数据访问仓内容，没有修改计数可用）
        private static final int REFRESH_TICKS = 20;

        private final DataItemHolder holder;
        @Nullable
        private final Player player;
        private final boolean remote;
        private final List<Entry> entries = new ArrayList<>();
        private final SyncValue<BitSet> flags;
        private final SyncValue<Integer> selected;

        // ---- 服务端 ----
        @Nullable
        private BitSet serverFlags;
        private int flagsModCount;
        private int ticksSinceRefresh;
        private int unlockedCount;
        private int unlockedTextCount = -1;
        private Component unlockedText = Component.empty();
        private int selectedIndex = -1;
        /// 已选条目的名称、数据物品，只在选中项变化时重新取
        private int memoIndex = -1;
        private Component memoName = Component.literal(NO_VALUE);
        private ItemStack memoOutput = ItemStack.EMPTY;
        private ItemStack memoTierItem = ItemStack.EMPTY;

        // ---- 客户端 ----
        String search = "";
        boolean showLocked;
        private final IntArrayList visible = new IntArrayList();
        /// 选中项在可见列表里的位置（按选中项缓存，重新筛选后作废）
        private int positionOf = -2;
        private int position = -1;
        private int tooltipIndex = -1;
        private int tooltipSelected = -1;
        @Nullable
        private BitSet tooltipFlags;
        private List<Component> tooltip = Collections.emptyList();

        RecipeGrid(DataItemHolder holder, @Nullable Player player, boolean remote) {
            this.holder = holder;
            this.player = player;
            this.remote = remote;
            layout(l -> l.size(UISizes.SLOT_ROW_WIDTH, UISizes.SLOT));
            this.flags = addSyncValue(SyncValue.of(this::currentFlags, BITSET, new BitSet()).onChanged(value -> refilter()));
            this.selected = addSyncValue(SyncValue.ofInt(this::selectedChecked, -1));
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
                for (int i = start; i < end; i++) buffer.writeResourceLocation(entries.get(i).recipe.id);
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
                    ResourceLocation id = buffer.readResourceLocation();
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
        }

        /** 服务端：该条目所在节点此刻是否已解锁（与位图的判定相同，不走缓存）。 */
        private boolean isUnlockedNow(Entry entry) {
            if (entry.node == null || player == null) return false;
            var tree = TechTreeSavedData.findTree(player, entry.node.getManager());
            return tree != null && tree.getUnlockedNodes().contains(entry.node);
        }

        private boolean serverUnlocked(int index) {
            return currentFlags().get(2 * index);
        }

        /** 选中项（服务端）：选中的条目不再解锁时清除。 */
        private int selectedChecked() {
            if (selectedIndex >= 0 && (selectedIndex >= entries.size() || !serverUnlocked(selectedIndex))) selectedIndex = -1;
            return selectedIndex;
        }

        @Nullable
        Entry selectedEntry() {
            int index = selectedChecked();
            return index < 0 ? null : entries.get(index);
        }

        private void updateMemo() {
            int index = selectedChecked();
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
            if (!recipe.itemOutputs.isEmpty()) return recipe.itemOutputs.getFirst().inner.getInnerItemStack();
            if (!recipe.fluidOutputs.isEmpty()) return FluidUtil.getFilledBucket(recipe.fluidOutputs.getFirst().inner.getFluidStack());
            return ItemStack.EMPTY;
        }

        /** 已选配方主产物的图标（状态行里显示，服务端取值下发）。 */
        ItemStack selectedOutputIcon() {
            updateMemo();
            return memoOutput;
        }

        /** 已选配方所需数据物品的图标。 */
        ItemStack selectedDataItemIcon() {
            updateMemo();
            return memoTierItem;
        }

        Component unlockedText() {
            currentFlags();
            if (unlockedCount != unlockedTextCount) {
                unlockedTextCount = unlockedCount;
                unlockedText = Component.literal(unlockedCount + " / " + entries.size());
            }
            return unlockedText;
        }

        Component selectedName() {
            updateMemo();
            return memoName;
        }

        Component selectedDataItemName() {
            updateMemo();
            return memoTierItem.isEmpty() ? Component.literal(NO_VALUE) : memoTierItem.getHoverName();
        }

        boolean hasSelectedDataItem() {
            updateMemo();
            return !memoTierItem.isEmpty() && hasConvertibleDataItem(holder.getDataItemStorage(), memoTierItem);
        }

        /** 服务端：导出已选配方（导出按钮、再次点击已选格子）。条件不满足时什么也不做。 */
        void exportSelected() {
            var entry = selectedEntry();
            if (entry == null || entry.node == null || entry.recipe == null) return;
            var tierItem = entry.node.getTierItem();
            if (!hasConvertibleDataItem(holder.getDataItemStorage(), tierItem)) return;
            holder.exportSelectedRecipe(tierItem, entry.recipe);
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id != ACTION_CLICK) {
                super.handleClientAction(id, buffer);
                return;
            }
            int index = buffer.readVarInt();
            if (index < 0 || index >= entries.size()) return;
            // 客户端的解锁状态可能滞后：只按最新数据校验被点的这一条
            if (!isUnlockedNow(entries.get(index))) return;
            if (selectedChecked() == index) exportSelected();
            else selectedIndex = index;
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

        void setShowLocked(boolean value) {
            if (showLocked == value) return;
            showLocked = value;
            refilter();
        }

        /** 客户端：按筛选条件重排可见条目，并按行数改高度。 */
        private void refilter() {
            if (!remote) return;
            visible.clear();
            String normalized = search.trim().toLowerCase(Locale.ROOT);
            for (int i = 0; i < entries.size(); i++) {
                if (!showLocked && !unlocked(i)) continue;
                if (!normalized.isEmpty() && !PinYinUtils.match(searchName(entries.get(i)), normalized)) continue;
                visible.add(i);
            }
            // 已解锁在前，其中未含有的在前；同组保持科技树顺序（归并排序是稳定的）
            IntArrays.mergeSort(visible.elements(), 0, visible.size(), (a, b) -> Integer.compare(sortKey(a), sortKey(b)));
            int rows = Math.max(1, (visible.size() + UISizes.SLOTS_PER_ROW - 1) / UISizes.SLOTS_PER_ROW);
            layout(l -> l.height(rows * UISizes.SLOT));
            tooltipIndex = -1;
            positionOf = -2;
        }

        /** 选中项在可见列表里的位置，不可见或没选中为 -1。 */
        private int selectedPosition() {
            int selectedEntry = selected.getValue();
            if (selectedEntry != positionOf) {
                positionOf = selectedEntry;
                position = selectedEntry < 0 ? -1 : visible.indexOf(selectedEntry);
            }
            return position;
        }

        private int sortKey(int index) {
            return (unlocked(index) ? 0 : 2) + (included(index) ? 1 : 0);
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
            if (!isMouseOverElement(mouseX, mouseY) || !inViewport(mouseX, mouseY)) return -1;
            int column = (int) (mouseX - getPositionX()) / UISizes.SLOT, row = (int) (mouseY - getPositionY()) / UISizes.SLOT;
            if (column < 0 || column >= UISizes.SLOTS_PER_ROW || row < 0) return -1;
            int k = row * UISizes.SLOTS_PER_ROW + column;
            return k < visible.size() ? visible.getInt(k) : -1;
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

        /// 一行格子（顶边 y）是否完整落在视口里
        private boolean isRowInViewport(int y) {
            var scroller = scroller();
            return scroller == null || (y >= scroller.getPositionY() && y + UISizes.SLOT <= scroller.getPositionY() + scroller.getSizeHeight());
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            int x0 = getPositionX(), y0 = getPositionY();
            var font = Minecraft.getInstance().font;
            if (visible.isEmpty()) {
                var key = entries.isEmpty() || (search.isBlank() && !showLocked) ? EMPTY_RECIPES : FILTER_EMPTY_RECIPES;
                graphics.drawString(font, UITheme.clip(font, Component.translatable(key).getString(), getSizeWidth()),
                        x0, y0 + (UISizes.SLOT - 8) / 2, UITheme.TEXT_SECONDARY, false);
                return;
            }
            // 只画视口里的行
            var scroller = scroller();
            int top = scroller == null ? Integer.MIN_VALUE : scroller.getPositionY();
            int bottom = scroller == null ? Integer.MAX_VALUE : top + scroller.getSizeHeight();
            int hovered = entryAt(mouseX, mouseY);
            int firstRow = top <= y0 ? 0 : (top - y0) / UISizes.SLOT;
            int from = Math.min(visible.size(), firstRow * UISizes.SLOTS_PER_ROW);
            for (int k = from; k < visible.size(); k++) {
                int x = x0 + k % UISizes.SLOTS_PER_ROW * UISizes.SLOT, y = y0 + k / UISizes.SLOTS_PER_ROW * UISizes.SLOT;
                if (y >= bottom) break;
                int index = visible.getInt(k);
                var entry = entries.get(index);
                UITheme.ITEM_SLOT.draw(graphics, mouseX, mouseY, x, y, UISizes.SLOT, UISizes.SLOT);
                if (entry.output != null) AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, x + 1, y + 1, entry.output);
                else graphics.drawString(font, "?", x + 6, y + 5, UITheme.TEXT, false);
                boolean isUnlocked = unlocked(index);
                if (isUnlocked && included(index)) {
                    StackSizeRenderer.renderSizeLabel(graphics, font, x + 1, y + 17 - font.lineHeight * 0.5f,
                            INCLUDED_LABEL, 0.5f, true, true);
                }
                if (!isUnlocked) {
                    // 未解锁：与禁用控件一致的斜纹（不压暗）
                    UITheme.drawDisabled(graphics, x, y, UISizes.SLOT, UISizes.SLOT);
                } else if (index == hovered) {
                    RenderSystem.colorMask(true, true, true, false);
                    graphics.fill(x + 1, y + 1, x + UISizes.SLOT - 1, y + UISizes.SLOT - 1, 200, UITheme.SLOT_HOVER_OVERLAY);
                    RenderSystem.colorMask(true, true, true, true);
                }
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            // 选中框画在前景层，外扩的 1 像素不被相邻格子盖住；滚出视口时不画
            int k = selectedPosition();
            if (k >= 0) {
                int x = getPositionX() + k % UISizes.SLOTS_PER_ROW * UISizes.SLOT, y = getPositionY() + k / UISizes.SLOTS_PER_ROW * UISizes.SLOT;
                if (isRowInViewport(y)) UITheme.drawSelection(graphics, x, y, UISizes.SLOT, UISizes.SLOT);
            }
            int hovered = entryAt(mouseX, mouseY);
            if (hovered < 0 || gui == null || gui.getModularUIGui() == null) return;
            gui.getModularUIGui().setHoverTooltip(tooltipFor(hovered), ItemStack.EMPTY, null, null);
        }

        /** 悬停提示：产物名、科技节点、所需数据物品，再按状态给操作提示；按（条目、选中项、位图）缓存。 */
        @OnlyIn(Dist.CLIENT)
        private List<Component> tooltipFor(int index) {
            var currentFlags = flags.getValue();
            int currentSelected = selected.getValue();
            if (index == tooltipIndex && currentSelected == tooltipSelected && currentFlags == tooltipFlags) return tooltip;
            tooltipIndex = index;
            tooltipSelected = currentSelected;
            tooltipFlags = currentFlags;
            var entry = entries.get(index);
            var lines = new ArrayList<Component>(6);
            if (entry.node == null || entry.recipe == null) {
                lines.add(Component.literal(NO_VALUE));
                tooltip = lines;
                return tooltip;
            }
            lines.add(ExResearchManager.getMainOutputDisplayName(entry.recipe).copy().withStyle(ChatFormatting.WHITE));
            lines.add(Component.translatable(NODE_TOOLTIP, entry.node.getDisplayName()).withStyle(ChatFormatting.GRAY));
            var tierItem = entry.node.getTierItem();
            if (!tierItem.isEmpty()) lines.add(Component.translatable(TIER_TOOLTIP, tierItem.getHoverName()).withStyle(ChatFormatting.GRAY));
            if (!unlocked(index)) {
                ElementState.appendDisabledLines(lines, Component.translatable(LOCKED_TOOLTIP));
            } else {
                boolean isSelected = index == currentSelected;
                lines.add(Component.translatable(isSelected ? EXPORT_TOOLTIP : SELECT_TOOLTIP)
                        .withStyle(isSelected ? ChatFormatting.AQUA : ChatFormatting.GRAY));
                if (included(index)) lines.add(Component.translatable(INCLUDED_TOOLTIP_DETAIL).withStyle(ChatFormatting.GOLD));
            }
            tooltip = lines;
            return tooltip;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int index = entryAt(mouseX, mouseY);
            if (index < 0 || button != 0) return super.mouseClicked(mouseX, mouseY, button);
            // 未解锁的格子只能预览：吃掉点击，不发请求
            if (!unlocked(index)) return true;
            writeClientAction(ACTION_CLICK, buffer -> buffer.writeVarInt(index));
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

        ICustomItemStackHandler getDataItemStorage();

        ICustomItemStackHandler getDataOutputStorage();

        default void exportSelectedRecipe(ItemStack dataStack, GTRecipeDefinition recipe) {
            ICustomItemStackHandler input = getDataItemStorage();
            ICustomItemStackHandler output = getDataOutputStorage();

            for (int slot = 0; slot < input.getSlots(); slot++) {
                ItemStack stackInSlot = input.getStackInSlot(slot);
                if (!isConvertibleDataItem(stackInSlot, dataStack)) {
                    continue;
                }

                ItemStack exported = stackInSlot.copyWithCount(1);
                ResearchManager.writeResearchToNBT(exported.getOrCreateTag(), recipe.id.toString(), recipe.recipeType);
                ItemStack remainder = insertIntoAny(output, exported, false);
                int inserted = exported.getCount() - remainder.getCount();
                if (inserted <= 0) {
                    continue;
                }
                input.extractItem(slot, inserted, false);
            }
        }

        default Set<GTRecipeDefinition> getExistRecipes() {
            return Collections.emptySet();
        }
    }

    private static ItemStack insertIntoAny(ICustomItemStackHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }
}
