package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.ExResearchManager;
import com.gtocore.api.research.TeamResearchSavedData;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.api.research.techtree.ui.TechNodeDetails;
import com.gtocore.api.research.techtree.ui.TechTreePage;
import com.gtocore.api.research.ui.RecipeExportTab;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.machine.multiblock.part.IDataAccessHatchMachineAccessor;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.integration.jade.GTOJadePlugin;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.trait.MultiblockTrait;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;
import com.gregtechceu.gtceu.api.gui.factory.MachineSubWindowFactory;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.SubWindowButton;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineSubWindows;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.EURecipeInfo;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DataAccessHatchMachine;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.data.Data;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.vfyjxf.taffy.style.FlexWrap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.GTValues.LuV;
import static com.gtocore.data.techtree.BaseNodes.DataCenterOverclocking;

@DataGeneratorScanned
public class DataCenter extends DataBankMachine implements ICustomRecipeLogicHolder,
                        IEnhancedRecipeLogicMachine,
                        RecipeExportTab.DataItemHolder,
                        ITierCasingMachine,
                        IMultiblockTraitHolder,
                        IWailaDisplayProvider,
                        IMachineSubWindows {

    @SaveToDisk
    @SyncToClient
    private TechNode selectedNode;
    @SaveToDisk
    private UUID researchRequester;
    @SaveToDisk(defaultValue = "0")
    private long cwuBuffer = 0L;
    @SaveToDisk(defaultValue = "0")
    private long cwutCache = 0L;
    private final TierCasingTrait tierCasingTrait;

    private final ReferenceList<NotifiableItemStackHandler> dataAccessHandlers = new ReferenceArrayList<>();

    @SaveToDisk
    private final NotifiableItemStackHandler inpur;
    @SaveToDisk
    private final NotifiableItemStackHandler output;

    public DataCenter(MetaMachineBlockEntity holder) {
        super(holder);
        inpur = new NotifiableItemStackHandler(this, 9, IO.NONE, IO.BOTH);
        output = new NotifiableItemStackHandler(this, 9, IO.NONE, IO.BOTH);
        tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.GLASS_TIER);
    }

    public int getTotalDataSlots() {
        var handlers = getDataAccessHandlers();
        int totalSlots = 0;
        for (var handler : handlers) {
            totalSlots += handler.getSlots();
        }
        return totalSlots;
    }

    private long getCWUInputLimit() {
        var startTier = getCasingTier(GTORecipeDataKeys.GLASS_TIER) - LuV;
        if (startTier < 0) {
            return 0;
        }
        return (long) ((1d + getTotalDataSlots() / 10000d) * (1d + getExistRecipes().size() / 100d) * (16L << (startTier)));
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
            if (cwutCache > 0) {
                cwuBuffer += requestCWU(cwutCache, false);
            }
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

    /**
     * 科技树节点详情里的"启动研究"按钮（两端都会调用）：点一下让本机研究该节点，正在研究时变红，再点取消。
     * 是否正在研究由服务端判定、经按钮所在区块的同步值下发；点击只在服务端执行，研究发起人是点按钮的玩家。
     */
    private void attachResearchButton(UIElement section, TechNode node) {
        var researching = section.addSyncValue(SyncValue.of(() -> selectedNode == node, SyncValue.BOOLEAN, false));
        var button = Button.text(LayoutStyle.AUTO, () -> Component.translatable(researching.getValue() ? LANG_DATA_ACCESS_RESEARCHING : LANG_DATA_ACCESS_LAUNCH_RESEARCH).getString())
                .setVariant(() -> researching.getValue() ? UITheme.ButtonVariant.DANGER : UITheme.ButtonVariant.CONFIRM)
                .setOnServerClick(() -> {
                    var gui = section.getGui();
                    if (gui == null || gui.entityPlayer == null) return;
                    selectedNode = node == selectedNode ? null : node;
                    researchRequester = gui.entityPlayer.getUUID();
                    cwuBuffer = 0L;
                    getRecipeLogic().resetRecipeLogic();
                })
                .bindTooltip(() -> Component.translatable(selectedNode == node ? LANG_DATA_ACCESS_CANCEL_RESEARCH : LANG_DATA_ACCESS_LAUNCH_RESEARCH));
        button.disabled(() -> {
            var gui = section.getGui();
            return gui != null && gui.entityPlayer != null && TechTreeSavedData.isUnlocked(TechTreeSavedData.getTeamUUID(gui.entityPlayer), node);
        }, TechNodeDetails.ALREADY_UNLOCKED);
        section.addChild(button);
    }

    private List<NotifiableItemStackHandler> getDataAccessHandlers() {
        if (!isFormed()) {
            return Collections.emptyList();
        }
        if (dataAccessHandlers.isEmpty()) {
            Arrays.stream(getParts())
                    .filter(DataAccessHatchMachine.class::isInstance)
                    .map(DataAccessHatchMachine.class::cast)
                    .sorted(Comparator.comparingLong(hatch -> hatch.getPos().asLong()))
                    .map(hatch -> ((IDataAccessHatchMachineAccessor) hatch).gtocore$getImportItems())
                    .forEach(dataAccessHandlers::add);
        }
        return dataAccessHandlers;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        dataAccessHandlers.clear();
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        tierCasingTrait.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        tierCasingTrait.onStructureInvalid();
        dataAccessHandlers.clear();
    }

    @Override
    public boolean isActive() {
        return recipeLogic.isWorking();
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (researchRequester == null || selectedNode == null) return null;
        var cwuAvailable = requestCWU(getCWUInputLimit(), true);
        if (TechTreeSavedData.isUnlocked(getOwnerUUID(), DataCenterOverclocking)) {
            var cwuTotalAvailable = requestCWU(Long.MAX_VALUE, true);
            var oc = 0;
            var cwuOC = cwuAvailable;
            while (true) {
                cwuOC *= 3L;
                if (cwuOC > cwuTotalAvailable) {
                    break;
                }
                oc++;
            }
            cwuAvailable <<= oc;
        }
        cwutCache = cwuAvailable;
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
    public void addDisplayText(@NotNull List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, isActive())
                .setWorkingStatusKeys(LANG_DATA_ACCESS_WARN_ENERGY, LANG_DATA_ACCESS_WARN_ENERGY, "gtceu.multiblock.data_bank.providing")
                .addEnergyUsageExactLine(energyUsage)
                .addWorkingStatusLine();
        textList.add(Component.translatable(LANG_DATA_ACCESS_USAGE,
                Component.literal(FormattingUtil.formatNumbers(getExistRecipes().size())).withStyle(ChatFormatting.AQUA),
                Component.literal(FormattingUtil.formatNumbers(getTotalDataSlots())).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));
        if (!isFormed()) return;
        textList.add(Component.translatable(LANG_DATA_ACCESS_MAX_CWU, Component.literal(FormattingUtil.formatNumbers(getCWUInputLimit())).withStyle(ChatFormatting.GREEN))
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

        var unlockContext = TeamResearchSavedData.getOrCreateContext(researchRequester);
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

    @Override
    public Reference2IntMap<TierDataKey> getCasingTiers() {
        return tierCasingTrait.getCasingTiers();
    }

    @Override
    public List<MultiblockTrait> getMultiblockTraits() {
        return new ArrayList<>();
    }

    @Override
    public void appendWailaTooltip(CompoundTag data, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        var nodeBytes = data.getByteArray("node");
        if (nodeBytes.length == 0) return;
        var node = GTOCodecs.TECH_NODE_DATA_CODEC.decode(Data.readData(nodeBytes));
        var ctx = TeamResearchSavedData.getOrCreateContext(getOwnerUUID());
        var capacity = node.getRequirements().getCwuNeeded();
        var storage = ctx.techNodeAccCWU().getLong(node);
        if (ctx.hasScanned(node.getRequirements().getEurekaItem())) {
            storage = (long) (storage + capacity * node.getRequirements().getEurekaProgress());
        }
        IElementHelper helper = iTooltip.getElementHelper();
        iTooltip.add(Component.translatable(LANG_DATA_ACCESS_RESEARCH_PROGRESS, node.getDisplayName().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        iTooltip.add(helper.progress(
                GTOJadePlugin.getProgress(storage, capacity),
                Component.literal(FormattingUtil.formatNumberReadable(storage) + " / " + FormattingUtil.formatNumberReadable(capacity) + " CWU"),
                iTooltip.getElementHelper().progressStyle().color(0xFF006D6A).textColor(-1),
                Util.make(BoxStyle.DEFAULT, style -> style.borderColor = 0xFF555555), true));
    }

    @Override
    public void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
        if (selectedNode != null) {
            data.putByteArray("node", GTOCodecs.TECH_NODE_DATA_CODEC.encode(selectedNode).writeToBytes());
        }
    }

    // ========= 研究（数据访问页用） =========

    /** 取消本机正在进行的研究，与科技树里再次点击"正在研究中"相同（服务端）。 */
    private void cancelResearch(Player player) {
        if (selectedNode == null) return;
        selectedNode = null;
        researchRequester = player.getUUID();
        cwuBuffer = 0L;
        getRecipeLogic().resetRecipeLogic();
    }

    /** 当前研究节点的 CWU 进度（服务端）：研究发起人所在队伍已累计的量，扫描过尤里卡物品时带加成。 */
    private ProgressBar.Progress researchProgress() {
        var node = selectedNode;
        var requester = researchRequester;
        if (node == null || requester == null) return ProgressBar.Progress.EMPTY;
        var requirements = node.getRequirements();
        if (requirements == null) return ProgressBar.Progress.EMPTY;
        var context = TeamResearchSavedData.getOrCreateContext(requester);
        var eureka = requirements.getEurekaItem();
        int bonus = eureka != null && context.hasScanned(eureka) ? Math.round(requirements.getEurekaProgress() * 1000) : 0;
        return new ProgressBar.Progress(context.techNodeAccCWU().getLong(node), requirements.getCwuNeeded(), bonus);
    }

    // ========= 页面标签 =========

    /** 数据访问、配方导出是主窗口顶部的页面标签；科技树是独立窗口（左侧小组件按钮打开，见 {@link #attachConfigurators}）。 */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new DataAccessStorageTab(this));
        sideTabs.attachSubTab(new RecipeExportTab(this));
    }

    /**
     * "数据访问"页：
     *
     * <pre>
     * ┌ 状态面板：状态 / 数据访问仓 / 数据槽位 / 已存配方 / 算力上限 / 当前研究 ┐
     * ├ 研究进度条（研究中才显示）                                         ┤
     * ├ [科技树] [取消研究]                                                ┤
     * └ 数据物品：所有数据访问仓的槽位合在一起，每行 9 格（滚动）            ┘
     * </pre>
     * 
     * 状态由服务端取值下发（统计每秒至多重算一次）；槽位数在打开页面时由服务端决定并随初始数据下发，两端据此建同样多的槽，
     * 结构变化后状态行提示重新打开页面。
     */
    private static final class DataAccessStorageTab implements IFancyUIProvider {

        private final DataCenter machine;

        private DataAccessStorageTab(DataCenter machine) {
            this.machine = machine;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var player = widget.getGui().entityPlayer;
            boolean remote = player.level().isClientSide;
            var stats = new DataAccessStats(machine);
            // 数据物品滚动区的滚动条伸在槽位右侧：玩家背包与槽位对齐
            if (widget instanceof MachineWindow window) window.setInventoryGutter(ScrollerView.SCROLL_BAR_SPACE);
            var page = UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));

            // 数据物品区块：先隐藏，槽建好后有槽才显示（两端都在得知槽数后执行）
            var dataBlock = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP));
            dataBlock.setDisplay(false);
            var slots = new DataSlotGrid(machine, remote, count -> dataBlock.setDisplay(count > 0));

            var status = new StatusPanel();
            status.addLine(LANG_LINE_STATE, () -> Component.translatable(switch (stats.state(slots.count())) {
                case DataAccessStats.UNFORMED -> LANG_STATE_UNFORMED;
                case DataAccessStats.CHANGED -> LANG_STATE_CHANGED;
                case DataAccessStats.NO_HATCH -> LANG_STATE_NO_HATCH;
                default -> LANG_STATE_OK;
            })).level(() -> switch (stats.state(slots.count())) {
                case DataAccessStats.UNFORMED -> StatusLine.Level.ERROR;
                case DataAccessStats.CHANGED, DataAccessStats.NO_HATCH -> StatusLine.Level.WARNING;
                default -> StatusLine.Level.GOOD;
            }).detail(() -> switch (stats.state(slots.count())) {
                case DataAccessStats.UNFORMED -> Component.translatable(LANG_DATA_ACCESS_UNFORMED);
                case DataAccessStats.CHANGED -> Component.translatable(LANG_DATA_ACCESS_REFRESH);
                case DataAccessStats.NO_HATCH -> Component.translatable(LANG_DATA_ACCESS_EMPTY);
                default -> Component.empty();
            });
            status.addLine(LANG_LINE_HATCHES, stats::hatchesText);
            status.addLine(LANG_LINE_SLOTS, stats::slotsText);
            status.addLine(LANG_LINE_RECIPES, stats::recipesText);
            status.addLine(LANG_LINE_MAX_CWU, stats::maxCwuText);
            status.addLine(LANG_LINE_RESEARCH, stats::researchText)
                    .level(() -> {
                        if (machine.selectedNode == null) return StatusLine.Level.NORMAL;
                        return machine.isActive() ? StatusLine.Level.GOOD : StatusLine.Level.WARNING;
                    })
                    .detail(() -> machine.selectedNode != null && !machine.isActive() ? Component.translatable(LANG_RESEARCH_IDLE) : Component.empty())
                    // 点击打开科技树窗口，定位到正在研究的节点并打开它的详情（窗口的初始定位就是它）
                    .onClick(Component.translatable(LANG_RESEARCH_LOCATE).withStyle(ChatFormatting.GRAY), () -> machine.selectedNode != null, clicker -> {
                        if (clicker instanceof ServerPlayer serverPlayer) MachineSubWindowFactory.open(serverPlayer, machine, WINDOW_TECH_TREE);
                    });
            page.addChild(status);

            // 研究：进度条只在研究中显示（服务端判定、下发；同步值挂在父元素上，隐藏的元素自己收不到更新）
            var progress = new ProgressBar(LayoutStyle.AUTO, Component.translatable(LANG_RESEARCH_PROGRESS), UITheme.STATUS_ONLINE, machine::researchProgress);
            boolean researching = !remote && machine.selectedNode != null;
            progress.setDisplay(researching);
            var techTree = Button.translatable(LayoutStyle.AUTO, LANG_TECH_TREE_WINDOW)
                    .setOnServerClick(() -> {
                        if (player instanceof ServerPlayer serverPlayer) MachineSubWindowFactory.open(serverPlayer, machine, WINDOW_TECH_TREE);
                    })
                    .layout(l -> l.flex(1));
            techTree.setHoverTooltips(Component.translatable(LANG_OPEN_TECH_TREE));
            var cancel = Button.translatable(LayoutStyle.AUTO, LANG_CANCEL_BUTTON)
                    .setVariant(UITheme.ButtonVariant.DANGER)
                    .setOnServerClick(() -> machine.cancelResearch(player))
                    .disabled(() -> machine.selectedNode == null, LANG_NO_RESEARCH)
                    .layout(l -> l.flex(1));
            var research = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(progress,
                    UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP)).addChildren(techTree, cancel));
            research.addSyncValue(SyncValue.of(() -> machine.selectedNode != null, SyncValue.BOOLEAN, researching).onChanged(progress::setDisplay));
            page.addChild(research);

            var scroller = new ScrollerView("research.data_access", UISizes.SLOT_ROW_WIDTH, UISizes.SLOT)
                    .adaptiveWidth().adaptiveHeight(DATA_MAX_ROWS * UISizes.SLOT);
            scroller.addScrollViewChild(slots);
            dataBlock.addChildren(TextLine.translatable(LayoutStyle.AUTO, LANG_DATA_ITEMS), scroller);
            page.addChild(dataBlock);
            return page;
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

    /**
     * 数据访问页的统计（服务端，每个打开的页面一份）：成型状态、仓数、槽数、已存配方数、算力上限，以及各行要显示的文字。
     * 已存配方要遍历所有部件，按机器计时每秒至多重算一次；显示文字只在数值变化时重新生成，不在每 tick 的同步里拼字符串。
     */
    private static final class DataAccessStats {

        static final int OK = 0;
        static final int UNFORMED = 1;
        static final int CHANGED = 2;
        static final int NO_HATCH = 3;
        private static final int REFRESH_TICKS = 20;
        private static final Component NONE = Component.literal(NO_VALUE);

        private final DataCenter machine;
        private boolean refreshed;
        private int refreshedAt;
        private boolean formed;
        private int hatches = -1;
        private int slots = -1;
        private int recipes = -1;
        private long maxCwu = -1;
        private Component hatchesText = NONE;
        private Component slotsText = NONE;
        private Component recipesText = NONE;
        private Component maxCwuText = NONE;
        @Nullable
        private TechNode researchNode;
        private Component researchText = NONE;

        private DataAccessStats(DataCenter machine) {
            this.machine = machine;
        }

        private void refresh() {
            int now = machine.getOffsetTimer();
            if (refreshed && now >= refreshedAt && now - refreshedAt < REFRESH_TICKS) return;
            refreshed = true;
            refreshedAt = now;
            formed = machine.isFormed();
            int newHatches = machine.getDataAccessHandlers().size();
            if (newHatches != hatches) hatchesText = Component.literal(FormattingUtil.formatNumbers(hatches = newHatches));
            int newSlots = machine.getTotalDataSlots();
            if (newSlots != slots) slotsText = Component.literal(FormattingUtil.formatNumbers(slots = newSlots));
            int newRecipes = machine.getExistRecipes().size();
            if (newRecipes != recipes) recipesText = Component.literal(FormattingUtil.formatNumbers(recipes = newRecipes));
            long newMaxCwu = formed ? machine.getCWUInputLimit() : 0L;
            if (newMaxCwu != maxCwu) maxCwuText = Component.literal(FormattingUtil.formatNumbers(maxCwu = newMaxCwu) + " CWU/t");
        }

        /** 页面状态；{@code builtSlots} 是打开页面时建的槽数，与现在不同说明结构变了。 */
        int state(int builtSlots) {
            refresh();
            if (!formed) return UNFORMED;
            if (slots != builtSlots) return CHANGED;
            return hatches == 0 ? NO_HATCH : OK;
        }

        Component hatchesText() {
            refresh();
            return hatchesText;
        }

        Component slotsText() {
            refresh();
            return slotsText;
        }

        Component recipesText() {
            refresh();
            return recipesText;
        }

        Component maxCwuText() {
            refresh();
            return maxCwuText;
        }

        /** 当前研究的节点名（节点变了才重新取）。 */
        Component researchText() {
            var node = machine.selectedNode;
            if (node != researchNode) {
                researchNode = node;
                researchText = node == null ? NONE : node.getDisplayName();
            }
            return researchText;
        }
    }

    /**
     * 所有数据访问仓的槽位，每行 9 格。槽数由服务端在建页时决定（{@link #initWidget}），随初始数据先下发槽数，
     * 客户端据此建同样多的槽后再读各槽的数据，两端控件树与容器槽位顺序一致。界面打开期间槽数不变。
     * <ul>
     * <li>服务端的槽直接读写各数据访问仓（{@link CombinedDataAccessHatchHandler}）；客户端的槽用本地镜像
     * （{@link ClientMirror}），内容只由原版容器同步写入，不依赖客户端自己的部件列表。</li>
     * <li>槽里的数据物品显示为其配方主产物（原实现如此）。</li>
     * <li>槽可能上百个：一次建完再排一次布局；只画落在滚动区视口里的槽。</li>
     * </ul>
     */
    private static final class DataSlotGrid extends UIElement {

        private final DataCenter machine;
        private final CombinedDataAccessHatchHandler handler;
        private final boolean remote;
        private final IntConsumer onBuilt;
        private int count = -1;
        /// 正在批量建槽：暂不因每次加槽重排整棵布局树，建完统一排一次
        private boolean building;

        private DataSlotGrid(DataCenter machine, boolean remote, IntConsumer onBuilt) {
            this.machine = machine;
            this.handler = new CombinedDataAccessHatchHandler(machine);
            this.remote = remote;
            this.onBuilt = onBuilt;
            layout(l -> l.row().flexWrap(FlexWrap.WRAP).width(UISizes.SLOT_ROW_WIDTH));
        }

        /** 建页时的槽数（两端一致）。 */
        int count() {
            return Math.max(0, count);
        }

        @Override
        public void initWidget() {
            if (!remote && count < 0) build(machine.getTotalDataSlots());
            super.initWidget();
        }

        private void build(int slots) {
            count = slots;
            ICustomItemStackHandler backing = remote ? new ClientMirror(slots, handler) : handler;
            building = true;
            try {
                for (int i = 0; i < slots; i++) {
                    var slot = new ItemSlot(backing, i, true, true);
                    slot.setItemHook(new DataItemDisplay());
                    addChild(slot);
                }
            } finally {
                building = false;
            }
            recomputeLayout();
            onBuilt.accept(slots);
        }

        @Override
        protected void recomputeLayout() {
            if (!building) super.recomputeLayout();
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            buffer.writeVarInt(count());
            super.writeInitialData(buffer);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            int slots = buffer.readVarInt();
            if (count < 0) build(slots);
            super.readInitialData(buffer);
        }

        /** 只画落在所在滚动区视口里的槽（滚动区只做裁剪，不剔除）。 */
        @Override
        @OnlyIn(Dist.CLIENT)
        protected void drawWidgetsBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            ScrollerView scroller = null;
            for (Widget w = getParent(); w != null; w = w.getParent()) {
                if (w instanceof ScrollerView found) {
                    scroller = found;
                    break;
                }
            }
            if (scroller == null) {
                super.drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                return;
            }
            int top = scroller.getPositionY(), bottom = top + scroller.getSizeHeight();
            for (var widget : widgets) {
                if (!widget.isVisible()) continue;
                int y = widget.getPositionY();
                if (y + widget.getSizeHeight() <= top || y >= bottom) continue;
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.enableBlend();
                if (widget.inAnimate()) widget.getAnimation().drawInBackground(graphics, mouseX, mouseY, partialTicks);
                else widget.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            }
        }
    }

    /**
     * 客户端数据槽的本地镜像：内容由原版容器同步写入。放入判定与上限问本端的数据访问仓（能对上时），
     * 只影响客户端的点击预测，最终以服务端为准。
     */
    private static final class ClientMirror extends CustomItemStackHandler {

        private final CombinedDataAccessHatchHandler local;

        private ClientMirror(int size, CombinedDataAccessHatchHandler local) {
            super(size);
            this.local = local;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot < local.getSlots() ? local.isItemValid(slot, stack) : super.isItemValid(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot < local.getSlots() ? local.getSlotLimit(slot) : super.getSlotLimit(slot);
        }
    }

    /** 槽里数据物品的显示：换成它记录的配方主产物；按输入物品对象缓存，不在每帧里读 NBT、查配方。 */
    private static final class DataItemDisplay implements Function<ItemStack, ItemStack> {

        private ItemStack lastInput = ItemStack.EMPTY;
        private ItemStack lastOutput = ItemStack.EMPTY;

        @Override
        public ItemStack apply(ItemStack stack) {
            if (stack != lastInput) {
                lastInput = stack;
                lastOutput = stack;
                var recipe = ExResearchManager.getRecipeInDataItem(stack);
                if (recipe != null) {
                    var output = ExResearchManager.getMainItemOutput(recipe);
                    if (output != null) lastOutput = output.wrapForDisplayOrFilter();
                }
            }
            return lastOutput;
        }
    }

    // ========= 独立窗口 =========

    /// 独立窗口的键（见 IMachineSubWindows）
    private static final String WINDOW_TECH_TREE = "techtree";

    /**
     * 科技树开独立窗口，由左侧机器小组件里的按钮打开；数据访问、配方导出是主窗口的页面标签（{@link #attachSideTabs}）。
     */
    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        var open = Component.translatable(LANG_OPEN_WINDOW).withStyle(ChatFormatting.GRAY);
        configuratorPanel.attachConfigurators(
                new SubWindowButton(this, WINDOW_TECH_TREE, BaseNodes.MainTree.getIcon(), Component.translatable(LANG_TECH_TREE_WINDOW), open));
    }

    @Override
    public @Nullable ModularUI createSubWindow(String key, Player player) {
        MachineWindow window = switch (key) {
            case WINDOW_TECH_TREE -> TechTreePage.window(new TechTreePage.Options()
                    .extra(this::attachResearchButton)
                    .initialFocus(() -> selectedNode)
                    .researching(() -> selectedNode));
            default -> null;
        };
        if (window == null) return null;
        return new ModularUI(176, 166, this, player).widget(window.setBackToMachine(this));
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

    /// 数据物品网格默认最多显示几行，再多滚动（右下角可拖拽缩放）
    private static final int DATA_MAX_ROWS = 5;
    /** 状态面板里没有值时显示的占位。 */
    private static final String NO_VALUE = "—";
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
    @RegisterLanguage(cn = "正在处理的研究节点：%s", en = "Currently Processing Research Node: %s")
    private static final String LANG_DATA_ACCESS_CURRENT_NODE = "gtocore.machine.data_center.data_access.current_node";
    @RegisterLanguage(cn = "科技树", en = "Tech Tree")
    private static final String LANG_TECH_TREE_WINDOW = "gtocore.machine.data_center.window.tech_tree";
    @RegisterLanguage(cn = "点击打开独立窗口", en = "Click to open in its own window")
    private static final String LANG_OPEN_WINDOW = "gtocore.machine.data_center.window.open";
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
    @RegisterLanguage(cn = "[%s]研究进度：", en = "[%s] Research Progress:")
    private static final String LANG_DATA_ACCESS_RESEARCH_PROGRESS = "gtocore.machine.data_center.data_access.research_progress";
    // 数据访问页：状态面板各行的名称与短状态（行内只剩约 12 个汉字宽，完整说明放悬停）
    @RegisterLanguage(cn = "状态", en = "Status")
    private static final String LANG_LINE_STATE = "gtocore.machine.data_center.data_access.line.state";
    @RegisterLanguage(cn = "正常", en = "Normal")
    private static final String LANG_STATE_OK = "gtocore.machine.data_center.data_access.state.ok";
    @RegisterLanguage(cn = "未成型", en = "Not formed")
    private static final String LANG_STATE_UNFORMED = "gtocore.machine.data_center.data_access.state.unformed";
    @RegisterLanguage(cn = "结构已变化", en = "Structure changed")
    private static final String LANG_STATE_CHANGED = "gtocore.machine.data_center.data_access.state.changed";
    @RegisterLanguage(cn = "无数据访问仓", en = "No hatches")
    private static final String LANG_STATE_NO_HATCH = "gtocore.machine.data_center.data_access.state.no_hatch";
    @RegisterLanguage(cn = "数据访问仓", en = "Data Access Hatches")
    private static final String LANG_LINE_HATCHES = "gtocore.machine.data_center.data_access.line.hatches";
    @RegisterLanguage(cn = "数据槽位", en = "Data slots")
    private static final String LANG_LINE_SLOTS = "gtocore.machine.data_center.data_access.line.slots";
    @RegisterLanguage(cn = "已存配方", en = "Stored recipes")
    private static final String LANG_LINE_RECIPES = "gtocore.machine.data_center.data_access.line.recipes";
    @RegisterLanguage(cn = "算力上限", en = "Max CWU")
    private static final String LANG_LINE_MAX_CWU = "gtocore.machine.data_center.data_access.line.max_cwu";
    @RegisterLanguage(cn = "当前研究", en = "Research")
    private static final String LANG_LINE_RESEARCH = "gtocore.machine.data_center.data_access.line.research";
    @RegisterLanguage(cn = "点击在科技树中定位", en = "Click to locate it in the Tech Tree")
    private static final String LANG_RESEARCH_LOCATE = "gtocore.machine.data_center.data_access.research_locate";
    @RegisterLanguage(cn = "数据中心未在运行，研究暂停", en = "The Data Center is not running; research is paused")
    private static final String LANG_RESEARCH_IDLE = "gtocore.machine.data_center.data_access.research_idle";
    @RegisterLanguage(cn = "研究进度", en = "Progress")
    private static final String LANG_RESEARCH_PROGRESS = "gtocore.machine.data_center.data_access.progress";
    @RegisterLanguage(cn = "打开科技树，选择要研究的节点", en = "Open the Tech Tree to choose a node to research")
    private static final String LANG_OPEN_TECH_TREE = "gtocore.machine.data_center.data_access.open_tech_tree";
    @RegisterLanguage(cn = "取消研究", en = "Cancel")
    private static final String LANG_CANCEL_BUTTON = "gtocore.machine.data_center.data_access.cancel";
    @RegisterLanguage(cn = "当前没有进行中的研究", en = "No research in progress")
    private static final String LANG_NO_RESEARCH = "gtocore.machine.data_center.data_access.no_research";
    @RegisterLanguage(cn = "数据物品", en = "Data Items")
    private static final String LANG_DATA_ITEMS = "gtocore.machine.data_center.data_access.data_items";
}
