package com.gtocore.api.research.ui;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTree;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.integration.jech.PinYinUtils;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@DataGeneratorScanned
public class RecipeExportTab implements IFancyUIProvider {

    private static final int TAB_WIDTH = 176;
    private static final int TAB_HEIGHT = 144;
    private static final int PAGE_WIDTH = TAB_WIDTH + 8;
    private static final int PAGE_HEIGHT = TAB_HEIGHT + 8;

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

    private final DataItemHolder holder;
    private final TechTreeManager techTree;

    public RecipeExportTab(DataItemHolder holder, TechTreeManager techTree) {
        this.holder = holder;
        this.techTree = techTree;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget fancyMachineUIWidget) {
        return new RecipeExportWidget(holder, techTree);
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
                stack.is(expectedTierItem.getItem()) &&
                !ResearchManager.hasResearchTag(stack);
    }

    private static boolean isSameEntry(@Nullable EntryState first, @Nullable EntryState second) {
        return first != null &&
                second != null &&
                first.nodeName().equals(second.nodeName()) &&
                first.recipeId().equals(second.recipeId());
    }

    private static final class RecipeExportWidget extends WidgetGroup {

        private static final int UPDATE_STATE = 100;
        private static final int SEARCH_FIELD_WIDTH = 102;
        private static final int SEARCH_FIELD_HEIGHT = 14;
        private static final int SWITCH_X = 112;
        private static final int SWITCH_Y = 5;
        private static final int SWITCH_SIZE = 12;
        private static final int RECIPE_PANEL_Y = 22;
        private static final int RECIPE_PANEL_HEIGHT = 52;
        private static final int SLOT_PANEL_Y = RECIPE_PANEL_Y + RECIPE_PANEL_HEIGHT + 8;
        private static final int SLOT_COLUMNS = 9;

        private final DataItemHolder holder;
        private final TechTreeManager techTree;
        private final DraggableScrollableWidgetGroup recipePanel;
        private final WidgetGroup recipeContent;
        private final DraggableScrollableWidgetGroup slotPanel;
        private final WidgetGroup slotContent;
        private String searchText = "";
        private boolean showLockedRewards = false;

        private SyncState currentState = SyncState.EMPTY;
        private SyncState lastSentState = SyncState.EMPTY;

        private RecipeExportWidget(DataItemHolder holder, TechTreeManager techTree) {
            super(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
            this.holder = holder;
            this.techTree = techTree;
            setBackground(GuiTextures.BACKGROUND_INVERSE);

            TextFieldWidget searchField = new TextFieldWidget(4, 4, SEARCH_FIELD_WIDTH, SEARCH_FIELD_HEIGHT, () -> searchText, this::setSearchText);
            searchField.setBackground(GuiTextures.NUMBER_BACKGROUND);
            searchField.setHoverTooltips(Component.translatable(SEARCH_TOOLTIP));
            searchField.setMaxStringLength(64);
            searchField.setClientSideWidget();
            addWidget(searchField);

            SwitchWidget showLockedSwitch = new SwitchWidget(SWITCH_X, SWITCH_Y, SWITCH_SIZE, SWITCH_SIZE, (clickData, result) -> setShowLockedRewards(result));
            showLockedSwitch
                    .setPressed(showLockedRewards)
                    .setBaseTexture(GuiTextures.BUTTON,
                            GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true)
                                    .copy()
                                    .getSubTexture(0.0F, 0.0F, 1.0F, 0.5F)
                                    .scale(0.8F))
                    .setPressedTexture(GuiTextures.BUTTON,
                            GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true)
                                    .copy()
                                    .getSubTexture(0.0F, 0.5F, 1.0F, 0.5F)
                                    .scale(0.8F));
            showLockedSwitch.setHoverTooltips(Component.translatable(SHOW_LOCKED_TOOLTIP));
            showLockedSwitch.setClientSideWidget();
            addWidget(showLockedSwitch);

            LabelWidget showLockedLabel = new LabelWidget(SWITCH_X + SWITCH_SIZE + 4, 7, Component.translatable(SHOW_LOCKED_LABEL));
            showLockedLabel.setHoverTooltips(Component.translatable(SHOW_LOCKED_TOOLTIP));
            showLockedLabel.setClientSideWidget();
            addWidget(showLockedLabel);

            recipePanel = new DraggableScrollableWidgetGroup(4, RECIPE_PANEL_Y, TAB_WIDTH, RECIPE_PANEL_HEIGHT)
                    .setBackground(GuiTextures.DISPLAY)
                    .setYScrollBarWidth(2)
                    .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
            recipeContent = new WidgetGroup(2, 4, TAB_WIDTH - 8, RECIPE_PANEL_HEIGHT - 8);
            recipePanel.addWidget(recipeContent);
            addWidget(recipePanel);

            slotPanel = new DraggableScrollableWidgetGroup(4, SLOT_PANEL_Y, TAB_WIDTH, TAB_HEIGHT - SLOT_PANEL_Y + 4)
                    .setBackground(GuiTextures.DISPLAY)
                    .setYScrollBarWidth(2)
                    .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
            slotContent = new WidgetGroup(2, 2, TAB_WIDTH - 8, TAB_HEIGHT - SLOT_PANEL_Y);
            slotPanel.addWidget(slotContent);
            addWidget(slotPanel);

            rebuildSlotWidgets();
            rebuildRecipeWidgets(0);
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            super.writeInitialData(buffer);
            SyncState state = buildState();
            applyState(state);
            lastSentState = state;
            writeState(buffer, state);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            super.readInitialData(buffer);
            applyState(readState(buffer));
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            syncState();
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == UPDATE_STATE) {
                applyState(readState(buffer));
                return;
            }
            super.readUpdateInfo(id, buffer);
        }

        private void syncState() {
            if (isRemote()) {
                return;
            }
            SyncState state = buildState();
            if (state.equals(lastSentState)) {
                return;
            }
            applyState(state);
            lastSentState = state;
            writeUpdateInfo(UPDATE_STATE, buffer -> writeState(buffer, state));
        }

        private SyncState buildState() {
            List<EntryState> entries = buildEntries();
            EntryState selected = findMatchingEntry(entries, currentState.selected());
            if (selected != null && !selected.unlocked()) {
                selected = null;
            }
            return new SyncState(List.copyOf(entries), selected);
        }

        private void applyState(SyncState state) {
            currentState = state;
            rebuildRecipeWidgets(recipePanel == null ? 0 : recipePanel.getScrollYOffset());
        }

        private void writeState(FriendlyByteBuf buffer, SyncState state) {
            buffer.writeVarInt(state.entries().size());
            for (EntryState entry : state.entries()) {
                writeEntry(buffer, entry);
            }
            buffer.writeBoolean(state.selected() != null);
            if (state.selected() != null) {
                writeEntry(buffer, state.selected());
            }
        }

        private SyncState readState(FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            List<EntryState> entries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                entries.add(readEntry(buffer));
            }
            EntryState selected = buffer.readBoolean() ? readEntry(buffer) : null;
            return new SyncState(List.copyOf(entries), selected);
        }

        private void rebuildRecipeWidgets(int scrollOffset) {
            recipeContent.clearAllWidgets();

            List<EntryState> allEntries = currentState.entries();
            List<EntryState> visibleEntries = getVisibleEntries();

            int effectivreIndex = 0;
            for (EntryState entry : allEntries) {
                int x = (effectivreIndex % SLOT_COLUMNS) * 18;
                int y = (effectivreIndex / SLOT_COLUMNS) * 18;
                var recipeWidget = new RecipeIconWidget(this, entry, x, y);
                recipeContent.addWidget(recipeWidget);
                if (isRemote() && visibleEntries.contains(entry)) {
                    effectivreIndex++;
                } else if (isRemote()) {
                    recipeWidget.setVisible(false);
                }
            }

            int rows = (visibleEntries.size() + SLOT_COLUMNS - 1) / SLOT_COLUMNS;
            recipeContent.setSize(TAB_WIDTH - 8, Math.max(RECIPE_PANEL_HEIGHT - 8, rows * 18));
            recipePanel.setScrollYOffset(scrollOffset);
        }

        private List<EntryState> getVisibleEntries() {
            if (!isRemote()) {
                return currentState.entries();
            }
            String normalizedSearch = searchText.trim().toLowerCase(Locale.ROOT);
            return currentState.entries().stream()
                    .filter(entry -> showLockedRewards || entry.unlocked())
                    .filter(entry -> normalizedSearch.isEmpty() || PinYinUtils.match(entry.searchName().toLowerCase(Locale.ROOT), normalizedSearch))
                    .toList();
        }

        private void rebuildSlotWidgets() {
            slotContent.clearAllWidgets();

            var input = holder.getDataItemStorage();
            var output = holder.getDataOutputStorage();
            int inputRows = Math.max(1, (input.getSlots() + SLOT_COLUMNS - 1) / SLOT_COLUMNS);
            int outputRows = Math.max(1, (output.getSlots() + SLOT_COLUMNS - 1) / SLOT_COLUMNS);

            int inputLabelY = 0;
            int inputSlotY = inputLabelY + 10;
            int outputLabelY = inputSlotY + inputRows * 18 + 4;
            int outputSlotY = outputLabelY + 10;
            int contentHeight = outputSlotY + outputRows * 18;

            slotContent.setSize(TAB_WIDTH - 8, contentHeight);
            slotContent.addWidget(new SlotSectionLabelWidget(0, 0, TAB_WIDTH - 8, contentHeight, inputLabelY, outputLabelY));

            for (int slot = 0; slot < input.getSlots(); slot++) {
                int x = (slot % SLOT_COLUMNS) * 18;
                int y = inputSlotY + (slot / SLOT_COLUMNS) * 18;
                slotContent.addWidget(new SlotWidget(input, slot, x, y, true, true).setBackgroundTexture(GuiTextures.SLOT));
            }

            for (int slot = 0; slot < output.getSlots(); slot++) {
                int x = (slot % SLOT_COLUMNS) * 18;
                int y = outputSlotY + (slot / SLOT_COLUMNS) * 18;
                slotContent.addWidget(new SlotWidget(output, slot, x, y, true, false).setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        private void handleClientRecipeClick(EntryState entry) {
            EntryState matchingEntry = findMatchingEntry(currentState.entries(), entry);
            if (matchingEntry == null || !matchingEntry.unlocked()) {
                return;
            }
            if (!isSameEntry(matchingEntry, currentState.selected())) {
                currentState = new SyncState(currentState.entries(), matchingEntry);
                rebuildRecipeWidgets(recipePanel.getScrollYOffset());
            }
        }

        private void handleServerRecipeClick(EntryState entry) {
            EntryState matchingEntry = findMatchingEntry(currentState.entries(), entry);
            if (matchingEntry == null || !matchingEntry.unlocked()) {
                return;
            }
            if (isSameEntry(matchingEntry, currentState.selected())) {
                ResolvedEntry resolvedEntry = resolveEntry(matchingEntry);
                if (resolvedEntry == null) {
                    return;
                }

                var recipe = resolvedEntry.recipe();
                var tierItem = resolvedEntry.node().getTierItem();
                if (tierItem == null) {
                    return;
                }
                holder.exportSelectedRecipe(tierItem, recipe);
            } else {
                currentState = new SyncState(currentState.entries(), matchingEntry);
            }
            syncState();
        }

        private List<EntryState> buildEntries() {
            Player player = getGuiPlayer();
            Set<String> unlockedNodeNames = new HashSet<>();
            TechTree tree = player == null ? null : TechTreeSavedData.findTree(player, techTree);
            if (tree != null && !tree.isEmpty()) {
                for (TechNode node : tree.getUnlockedNodes()) {
                    unlockedNodeNames.add(node.name);
                }
            }

            List<EntryState> entries = new ArrayList<>();
            for (TechNode node : techTree.getLayout().orderedNodes()) {
                var recipes = node.getRecipes();
                if (recipes.isEmpty()) {
                    continue;
                }

                boolean unlocked = unlockedNodeNames.contains(node.name);
                for (GTRecipeDefinition recipe : recipes) {
                    if (hasRenderableMainOutput(recipe)) {
                        entries.add(new EntryState(node.name, recipe.id.toString(), getMainOutputSearchName(recipe), unlocked));
                    }
                }
            }

            entries.sort(Comparator
                    .comparingInt((EntryState entry) -> entry.unlocked() ? 0 : 1)
                    .thenComparingInt(entry -> {
                        ResolvedEntry resolved = resolveEntry(entry);
                        return resolved == null ? Integer.MAX_VALUE : resolved.node().getTier();
                    })
                    .thenComparing(EntryState::searchName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(EntryState::nodeName)
                    .thenComparing(EntryState::recipeId));
            return entries;
        }

        private void setSearchText(String newText) {
            String normalized = newText == null ? "" : newText;
            if (normalized.equals(searchText)) {
                return;
            }
            searchText = normalized;
            rebuildRecipeWidgets(0);
        }

        private void setShowLockedRewards(boolean showLockedRewards) {
            if (this.showLockedRewards == showLockedRewards) {
                return;
            }
            this.showLockedRewards = showLockedRewards;
            rebuildRecipeWidgets(0);
        }

        private Component getEmptyRecipeText() {
            if (currentState.entries().isEmpty() || (searchText.isBlank() && !showLockedRewards)) {
                return Component.translatable(EMPTY_RECIPES);
            }
            return Component.translatable(FILTER_EMPTY_RECIPES);
        }

        private @Nullable Player getGuiPlayer() {
            return getGui() == null ? null : getGui().entityPlayer;
        }

        private @Nullable EntryState findMatchingEntry(List<EntryState> entries, @Nullable EntryState target) {
            if (target == null) {
                return null;
            }
            return entries.stream()
                    .filter(entry -> isSameEntry(entry, target))
                    .findFirst()
                    .orElse(null);
        }

        private @Nullable ResolvedEntry resolveEntry(EntryState entry) {
            TechNode node = techTree.getNode(entry.nodeName());
            if (node == null) {
                return null;
            }
            for (GTRecipeDefinition recipe : node.getRecipes()) {
                if (entry.recipeId().equals(recipe.id.toString())) {
                    return new ResolvedEntry(node, recipe);
                }
            }
            return null;
        }

        private List<Component> buildTooltip(EntryState entry) {
            ResolvedEntry resolved = resolveEntry(entry);
            if (resolved == null) {
                return List.of(Component.literal(entry.recipeId()));
            }

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(getMainOutputDisplayName(resolved.recipe()).copy().withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.translatable(NODE_TOOLTIP, resolved.node().getDisplayName()).withStyle(ChatFormatting.GRAY));

            var tierItem = resolved.node().getTierItem();
            if (tierItem != null) {
                tooltip.add(Component.translatable(TIER_TOOLTIP, tierItem.getHoverName()).withStyle(ChatFormatting.GRAY));
            }

            if (!entry.unlocked()) {
                tooltip.add(Component.translatable(LOCKED_TOOLTIP).withStyle(ChatFormatting.DARK_GRAY));
                return tooltip;
            }

            tooltip.add(Component.translatable(isSameEntry(currentState.selected(), entry) ? EXPORT_TOOLTIP : SELECT_TOOLTIP)
                    .withStyle(isSameEntry(currentState.selected(), entry) ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            return tooltip;
        }

        private static boolean hasRenderableMainOutput(GTRecipeDefinition recipe) {
            return !getMainItemOutput(recipe).isEmpty() || !getMainFluidOutput(recipe).isEmpty();
        }

        private static @NotNull ItemStack getMainItemOutput(GTRecipeDefinition recipe) {
            if (recipe.itemOutputs.isEmpty()) {
                return ItemStack.EMPTY;
            }
            var ingredient = recipe.itemOutputs.getFirst().inner;
            ItemStack stack = ingredient.getInnerItemStack().copy();
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            stack.setCount(Math.max(1, ingredient.getAmount()));
            return stack;
        }

        private static @NotNull FluidStack getMainFluidOutput(GTRecipeDefinition recipe) {
            if (recipe.fluidOutputs.isEmpty()) {
                return FluidStack.EMPTY;
            }
            return recipe.fluidOutputs.getFirst().inner.getFluidStack();
        }

        private static Component getMainOutputDisplayName(GTRecipeDefinition recipe) {
            ItemStack itemStack = getMainItemOutput(recipe);
            if (!itemStack.isEmpty()) {
                return Component.literal(itemStack.getCount() + "x ").append(itemStack.getHoverName());
            }

            FluidStack fluidStack = getMainFluidOutput(recipe);
            if (!fluidStack.isEmpty()) {
                return Component.literal(fluidStack.getAmount() + "mB ").append(fluidStack.getDisplayName());
            }
            return Component.empty();
        }

        private static String getMainOutputSearchName(GTRecipeDefinition recipe) {
            ItemStack itemStack = getMainItemOutput(recipe);
            if (!itemStack.isEmpty()) {
                return itemStack.getHoverName().getString();
            }

            FluidStack fluidStack = getMainFluidOutput(recipe);
            if (!fluidStack.isEmpty()) {
                return fluidStack.getDisplayName().getString();
            }
            return "";
        }
    }

    private record EntryState(String nodeName, String recipeId, String searchName, boolean unlocked) {}

    private record SyncState(List<EntryState> entries, @Nullable EntryState selected) {

        private static final SyncState EMPTY = new SyncState(List.of(), null);
    }

    private record ResolvedEntry(TechNode node, GTRecipeDefinition recipe) {}

    private static void writeEntry(FriendlyByteBuf buffer, EntryState entry) {
        buffer.writeUtf(entry.nodeName());
        buffer.writeUtf(entry.recipeId());
        buffer.writeUtf(entry.searchName());
        buffer.writeBoolean(entry.unlocked());
    }

    private static EntryState readEntry(FriendlyByteBuf buffer) {
        return new EntryState(buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readBoolean());
    }

    private static final class SlotSectionLabelWidget extends WidgetGroup {

        private SlotSectionLabelWidget(int x, int y, int width, int height, int inputLabelY, int outputLabelY) {
            super(x, y, width, height);
            addWidget(new LabelWidget(0, inputLabelY, Component.translatable(INPUT_LABEL)));
            addWidget(new LabelWidget(0, outputLabelY, Component.translatable(OUTPUT_LABEL)));
        }
    }

    private static final class EmptyTextWidget extends Widget {

        private final Component text;

        private EmptyTextWidget(int x, int y, int width, int height, Component text) {
            super(x, y, width, height);
            this.text = text;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            Position pos = getPosition();
            Size size = getSize();
            Font font = Minecraft.getInstance().font;
            int textWidth = font.width(text);
            int x = pos.x + Math.max(0, (size.width - textWidth) / 2);
            int y = pos.y + Math.max(0, (size.height - font.lineHeight) / 2);
            graphics.drawString(font, text, x, y, 0xFFB9B9C0, false);
        }
    }

    private static final class RecipeIconWidget extends Widget {

        private static final int ACTION_CLICK = 1;

        private final RecipeExportWidget parentWidget;
        private final EntryState entry;

        private RecipeIconWidget(RecipeExportWidget parentWidget, EntryState entry, int x, int y) {
            super(x, y, 18, 18);
            this.parentWidget = parentWidget;
            this.entry = entry;
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id == ACTION_CLICK) {
                parentWidget.handleServerRecipeClick(entry);
                return;
            }
            super.handleClientAction(id, buffer);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            if (!isVisible()) {
                return;
            }
            Position pos = getPosition();
            Size size = getSize();

            GuiTextures.SLOT.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
            if (entry.unlocked() && isSameEntry(parentWidget.currentState.selected(), entry)) {
                DrawerHelper.drawSolidRect(graphics, pos.x + 1, pos.y + 1, size.width - 2, size.height - 2, 0x5539C5BB);
            }

            drawRecipeIcon(graphics, pos.x + 1, pos.y + 1);

            if (!entry.unlocked()) {
                DrawerHelper.drawSolidRect(graphics, pos.x + 1, pos.y + 1, size.width - 2, size.height - 2, 0xAA2B2B2B);
                DrawerHelper.drawBorder(graphics, pos.x, pos.y, size.width, size.height, isMouseOver(mouseX, mouseY) ? 0xFF6A6A6A : 0xFF4C4C4C, 1);
                return;
            }

            if (isSameEntry(parentWidget.currentState.selected(), entry)) {
                DrawerHelper.drawBorder(graphics, pos.x, pos.y, size.width, size.height, 0xFF39C5BB, 1);
            } else if (isMouseOver(mouseX, mouseY)) {
                DrawerHelper.drawBorder(graphics, pos.x, pos.y, size.width, size.height, 0xFFF3F3F3, 1);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (!isMouseOver(mouseX, mouseY) || !isVisible()) {
                return;
            }
            var tooltip = parentWidget.buildTooltip(entry);
            if (!tooltip.isEmpty()) {
                gui.getModularUIGui().setHoverTooltip(tooltip, ItemStack.EMPTY, null, null);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOver(mouseX, mouseY) || button != 0 || !isVisible()) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            if (!entry.unlocked()) {
                return true;
            }
            writeClientAction(ACTION_CLICK, buffer -> {});
            parentWidget.handleClientRecipeClick(entry);
            playButtonClickSound();
            return true;
        }

        @OnlyIn(Dist.CLIENT)
        private void drawRecipeIcon(GuiGraphics graphics, int x, int y) {
            ResolvedEntry resolved = parentWidget.resolveEntry(entry);
            if (resolved == null) {
                graphics.drawString(Minecraft.getInstance().font, "?", x + 5, y + 4, 0xFFFFFFFF, false);
                return;
            }

            ItemStack itemStack = RecipeExportWidget.getMainItemOutput(resolved.recipe());
            if (!itemStack.isEmpty()) {
                var key = AEItemKey.of(itemStack);
                if (key != null) {
                    AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, x, y, key);
                    return;
                }
            }

            FluidStack fluidStack = RecipeExportWidget.getMainFluidOutput(resolved.recipe());
            if (!fluidStack.isEmpty()) {
                var key = AEFluidKey.of(fluidStack);
                if (key != null) {
                    AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, x, y, key);
                    return;
                }
            }

            graphics.drawString(Minecraft.getInstance().font, "?", x + 5, y + 4, 0xFFFFFFFF, false);
        }

        @OnlyIn(Dist.CLIENT)
        private boolean isMouseOver(double mouseX, double mouseY) {
            Position pos = getPosition();
            Size size = getSize();
            return mouseX >= pos.x && mouseX < pos.x + size.width && mouseY >= pos.y && mouseY < pos.y + size.height;
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
    }

    private static ItemStack insertIntoAny(ICustomItemStackHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }
}
