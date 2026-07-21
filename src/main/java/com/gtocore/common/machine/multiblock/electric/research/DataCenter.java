package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.ExResearchManager;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.api.research.ui.RecipeExportTab;
import com.gtocore.api.research.ui.ResearchInfoTab;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.machine.multiblock.part.IDataAccessHatchMachineAccessor;
import com.gtocore.data.recipe.research.AnalyzeData;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.EURecipeInfo;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DataAccessHatchMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@DataGeneratorScanned
public class DataCenter extends DataBankMachine implements ICustomRecipeLogicHolder, IEnhancedRecipeLogicMachine, RecipeExportTab.DataItemHolder {

    @SaveToDisk(saveNull = true)
    @SyncToClient
    private TechNode selectedNode;
    @SaveToDisk(saveNull = true)
    private UUID researchRequester;
    @SaveToDisk
    private long cwuBuffer = 0L;

    @SaveToDisk
    private final NotifiableItemStackHandler inpur;
    @SaveToDisk
    private final NotifiableItemStackHandler output;

    public DataCenter(MetaMachineBlockEntity holder) {
        super(holder);
        inpur = new NotifiableItemStackHandler(this, 4, IO.NONE, IO.BOTH);
        output = new NotifiableItemStackHandler(this, 4, IO.NONE, IO.BOTH);
    }

    public int getTotalDataSlots() {
        var handlers = getDataAccessHandlers();
        int totalSlots = 0;
        for (var handler : handlers) {
            totalSlots += handler.getSlots();
        }
        return totalSlots;
    }

    private int getCWUInputLimit() {
        return getTotalDataSlots() * getExistRecipes().size();
    }

    @Override
    public void tick() {
        if (getRecipeLogic().getLastRecipe() == null) {
            super.tick();
        }
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (recipe != null) {
            var eu = recipe.eut;
            if (eu != 0) {
                if (!this.useEnergy(eu, false)) {
                    setIdleReason(() -> ActionResult.failInsufficientIn(EURecipeInfo.INSTANCE.getName()).reason());
                    return false;
                }
            }
            var cwuAvailable = requestCWU(getCWUInputLimit(), true);
            cwuBuffer += requestCWU(cwuAvailable, false);
        }
        return true;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(output.storage);
        clearInventory(inpur.storage);
    }

    public Set<GTRecipeDefinition> getExistRecipes() {
        return Arrays.stream(getParts())
                .filter(IDataAccessHatchMachineAccessor.class::isInstance)
                .map(IDataAccessHatchMachineAccessor.class::cast)
                .flatMap(i -> i.gtocore$recipes().stream())
                .collect(Collectors.toSet());
    }

    private String getResearchButtonText(@Nullable TechNode node) {
        return Component.translatable(node != null && selectedNode == node ? LANG_DATA_ACCESS_RESEARCHING : LANG_DATA_ACCESS_LAUNCH_RESEARCH).getString();
    }

    private List<NotifiableItemStackHandler> getDataAccessHandlers() {
        if (!isFormed()) {
            return List.of();
        }
        return Arrays.stream(getParts())
                .filter(DataAccessHatchMachine.class::isInstance)
                .map(DataAccessHatchMachine.class::cast)
                .sorted(Comparator.comparingLong(hatch -> hatch.getPos().asLong()))
                .map(hatch -> ((IDataAccessHatchMachineAccessor) hatch).gtocore$getImportItems())
                .toList();
    }

    @Override
    public boolean isActive() {
        return recipeLogic.isWorking();
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (researchRequester == null || selectedNode == null) return null;
        return getRecipeBuilder().EUt(energyUsage * 2L).duration(20).inputFluids(GTMaterials.PCBCoolant, 100).build();
    }

    @Override
    public int getProgress() {
        return getRecipeLogic().getProgress();
    }

    @Override
    public int getMaxProgress() {
        return getRecipeLogic().getMaxProgress();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, isActive())
                .setWorkingStatusKeys(LANG_DATA_ACCESS_WARN_ENERGY, LANG_DATA_ACCESS_WARN_ENERGY, "gtceu.multiblock.data_bank.providing")
                .addEnergyUsageExactLine(energyUsage)
                .addWorkingStatusLine();
        textList.add(Component.translatable(LANG_DATA_ACCESS_USAGE,
                Component.literal(String.valueOf(getExistRecipes().size())).withStyle(ChatFormatting.AQUA),
                Component.literal(String.valueOf(getTotalDataSlots())).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));
        if (!isFormed()) return;
        textList.add(Component.translatable(LANG_DATA_ACCESS_MAX_CWU, Component.literal(String.valueOf(getCWUInputLimit())).withStyle(ChatFormatting.GREEN))
                .withStyle(ChatFormatting.GRAY));
        if (selectedNode != null) {
            textList.add(Component.translatable(LANG_DATA_ACCESS_CURRENT_NODE, selectedNode.getDisplayName().withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        if (cwuBuffer <= 0L) {
            return;
        }
        if (selectedNode == null || researchRequester == null) {
            cwuBuffer = 0L;
            return;
        }

        var unlockContext = TeamResearchSavedDtat.getOrCreateContext(researchRequester);
        unlockContext.addTechNodeAccCWU(selectedNode, cwuBuffer);
        if (TechTreeSavedData.hasNodeMetCWURequirements(researchRequester, selectedNode)) {
            TechTreeSavedData.unlock(researchRequester, selectedNode);
            selectedNode = null;
            researchRequester = null;
            getRecipeLogic().resetRecipeLogic();
        }
        cwuBuffer = 0L;
    }

    @Override
    public ICustomItemStackHandler getDataItemStorage() {
        return inpur;
    }

    @Override
    public ICustomItemStackHandler getDataOutputStorage() {
        return output;
    }

    // ========= UI Tabs =========

    private static final class DataAccessStorageTab implements IFancyUIProvider {

        private final DataCenter machine;
        private final CombinedDataAccessHatchHandler itemHandler;

        private DataAccessStorageTab(DataCenter machine) {
            this.machine = machine;
            this.itemHandler = new CombinedDataAccessHatchHandler(machine);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var group = new WidgetGroup(0, 0, TAB_WIDTH + 8, TAB_HEIGHT + 8);
            var mainGroup = new DraggableScrollableWidgetGroup(4, 4, TAB_WIDTH, TAB_HEIGHT)
                    .setBackground(GuiTextures.DISPLAY)
                    .setYScrollBarWidth(2)
                    .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);

            var handlers = machine.getDataAccessHandlers();
            int totalSlots = machine.getTotalDataSlots();
            int rowCount = (totalSlots + SLOT_COLUMNS - 1) / SLOT_COLUMNS;
            int contentHeight = Math.max(TAB_HEIGHT - 8, SLOT_Y + rowCount * 18);

            var content = new WidgetGroup(2, 4, TAB_WIDTH - 4, contentHeight);
            content.addWidget(new ComponentPanelWidget(0, 0, lines -> {
                if (!machine.isFormed()) {
                    lines.add(Component.translatable(LANG_DATA_ACCESS_UNFORMED));
                    return;
                }
                if (handlers.size() != machine.getDataAccessHandlers().size()) {
                    lines.add(Component.translatable(LANG_DATA_ACCESS_REFRESH));
                    return;
                }
                if (handlers.isEmpty()) {
                    lines.add(Component.translatable(LANG_DATA_ACCESS_EMPTY));
                    return;
                }
                lines.add(Component.translatable(LANG_DATA_ACCESS_SUMMARY, handlers.size(), totalSlots));
            }).setMaxWidthLimit(TAB_WIDTH - 12));

            for (int slot = 0; slot < totalSlots; slot++) {
                int x = slot % SLOT_COLUMNS;
                int y = slot / SLOT_COLUMNS;
                content.addWidget(new SlotWidget(itemHandler, slot, x * 18, SLOT_Y + y * 18, true, true)
                        .setItemHook(i -> {
                            var recipe = ExResearchManager.getRecipeInDataItem(i);
                            if (recipe != null) {
                                var outpur = ExResearchManager.getMainItemOutput(recipe);
                                if (outpur != null) {
                                    return outpur.wrapForDisplayOrFilter();
                                }
                            }
                            return i;
                        })
                        .setBackground(GuiTextures.SLOT));
            }

            mainGroup.addWidget(content);
            group.addWidget(mainGroup);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            return group;
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(ExResearchMachines.BIO_DATA_ACCESS_HATCH.asStack());
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable(LANG_DATA_ACCESS_TITLE));
        }

        @Override
        public Component getTitle() {
            return Component.translatable(LANG_DATA_ACCESS_TITLE);
        }
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new ResearchInfoTab(AnalyzeData.TechTree, (uiWidget, sideTab) -> {
            AtomicReference<ButtonWidget> btnRef = new AtomicReference<>(null);
            var button = new ButtonWidget(4, 4, 64, 20, clickData -> {
                if (clickData.isRemote) {
                    btnRef.get().setButtonTexture(GuiTextures.BUTTON,
                            new TextTexture(Component.translatable(LANG_DATA_ACCESS_LAUNCH_RESEARCH).getString())
                                    .setSupplier(() -> getResearchButtonText(sideTab.getSelectedNode())).setType(TextTexture.TextType.ROLL));
                    return;
                }
                var gui = uiWidget.getGui();
                if (gui == null || gui.entityPlayer == null) {
                    return;
                }
                var node = sideTab.getSelectedNode();
                if (node == null) {
                    return;
                }

                UUID requester = gui.entityPlayer.getUUID();
                boolean selectionChanged = researchRequester == null || !researchRequester.equals(requester);
                selectedNode = node == selectedNode ? null : node;
                researchRequester = requester;

                if (selectionChanged) {
                    cwuBuffer = 0L;
                    getRecipeLogic().resetRecipeLogic();
                }
            }) {

                @Override
                @OnlyIn(Dist.CLIENT)
                protected void drawTooltipTexts(int mouseX, int mouseY) {
                    if (sideTab.getSelectedNode() != null && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this && gui != null && gui.getModularUIGui() != null) {
                        var lines = sideTab.getSelectedNode().getRewardLinesWithHeader();
                        if (selectedNode == sideTab.getSelectedNode()) {
                            lines.add(Component.translatable(LANG_DATA_ACCESS_CANCEL_RESEARCH).withStyle(ChatFormatting.YELLOW));
                        }
                        gui.getModularUIGui().setHoverTooltip(lines, ItemStack.EMPTY, null, null);
                    }
                }
            };
            btnRef.set(button);
            button.setButtonTexture(GuiTextures.BUTTON,
                    new TextTexture(Component.translatable(LANG_DATA_ACCESS_LAUNCH_RESEARCH).getString())
                            .setSupplier(() -> getResearchButtonText(sideTab.getSelectedNode())));
            return button;
        }));
        sideTabs.attachSubTab(new DataAccessStorageTab(this));
        sideTabs.attachSubTab(new RecipeExportTab(this, AnalyzeData.TechTree));
    }

    private record CombinedDataAccessHatchHandler(DataCenter machine) implements ICustomItemStackHandler {

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            var slotRef = findSlot(slot);
            if (slotRef != null) {
                slotRef.handler().setStackInSlot(slotRef.slot(), stack);
            }
        }

        @Override
        public int getSlots() {
            return machine.getTotalDataSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            var slotRef = findSlot(slot);
            return slotRef == null ? ItemStack.EMPTY : slotRef.handler().getStackInSlot(slotRef.slot());
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            var slotRef = findSlot(slot);
            return slotRef == null ? stack : slotRef.handler().insertItem(slotRef.slot(), stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            var slotRef = findSlot(slot);
            return slotRef == null ? ItemStack.EMPTY : slotRef.handler().extractItem(slotRef.slot(), amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            var slotRef = findSlot(slot);
            return slotRef == null ? 0 : slotRef.handler().getSlotLimit(slotRef.slot());
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            var slotRef = findSlot(slot);
            return slotRef != null && slotRef.handler().isItemValid(slotRef.slot(), stack);
        }

        private @Nullable SlotReference findSlot(int slot) {
            if (slot < 0) {
                return null;
            }
            int cursor = slot;
            for (var handler : machine.getDataAccessHandlers()) {
                int slots = handler.getSlots();
                if (cursor < slots) {
                    return new SlotReference(handler, cursor);
                }
                cursor -= slots;
            }
            return null;
        }

        private record SlotReference(ICustomItemStackHandler handler, int slot) {}
    }

    private static final int TAB_WIDTH = 176;
    private static final int TAB_HEIGHT = 144;
    private static final int SLOT_COLUMNS = 9;
    private static final int SLOT_Y = 18;
    @RegisterLanguage(cn = "数据库使用：%s/%s", en = "Database Usage: %s/%s")
    private static final String LANG_DATA_ACCESS_USAGE = "gtocore.machine.data_center.data_access.usage";
    @RegisterLanguage(cn = "数据访问", en = "Data Access")
    private static final String LANG_DATA_ACCESS_TITLE = "gtocore.machine.data_center.data_access.title";
    @RegisterLanguage(cn = "数据中心成型后可访问连接的数据访问仓。", en = "Form the Data Center to access connected Data Access Hatches.")
    private static final String LANG_DATA_ACCESS_UNFORMED = "gtocore.machine.data_center.data_access.unformed";
    @RegisterLanguage(cn = "当前结构中未检测到数据访问仓。", en = "No Data Access Hatch was found in this structure.")
    private static final String LANG_DATA_ACCESS_EMPTY = "gtocore.machine.data_center.data_access.empty";
    @RegisterLanguage(cn = "刷新页面以查看最新数据访问仓信息。", en = "Refresh the page to view the latest Data Access Hatch information.")
    private static final String LANG_DATA_ACCESS_REFRESH = "gtocore.machine.data_center.data_access.refresh";
    @RegisterLanguage(cn = "已连接数据访问仓：%s，总槽位：%s", en = "Connected Data Access Hatches: %s, Total Slots: %s")
    private static final String LANG_DATA_ACCESS_SUMMARY = "gtocore.machine.data_center.data_access.summary";
    @RegisterLanguage(cn = "正在处理的研究节点：%s", en = "Currently Processing Research Node: %s")
    private static final String LANG_DATA_ACCESS_CURRENT_NODE = "gtocore.machine.data_center.data_access.current_node";
    @RegisterLanguage(cn = "启动研究", en = "Launch Research")
    private static final String LANG_DATA_ACCESS_LAUNCH_RESEARCH = "gtocore.machine.data_center.data_access.launch_research";
    @RegisterLanguage(cn = "正在研究中", en = "Research in Progress")
    private static final String LANG_DATA_ACCESS_RESEARCHING = "gtocore.machine.data_center.data_access.researching";
    @RegisterLanguage(cn = "再次点击以取消研究", en = "Click again to cancel research")
    private static final String LANG_DATA_ACCESS_CANCEL_RESEARCH = "gtocore.machine.data_center.data_access.cancel_research";
    @RegisterLanguage(cn = "最大可接受算力：%s CWU/t", en = "Maximum Acceptable CWU: %s CWU/t")
    private static final String LANG_DATA_ACCESS_MAX_CWU = "gtocore.machine.data_center.data_access.max_cwu";
    @RegisterLanguage(cn = "§6警告：未正常运行§r", en = "§6Warning: Not Running Properly§r")
    private static final String LANG_DATA_ACCESS_WARN_ENERGY = "gtocore.machine.data_center.data_access.warn.energy";
}
