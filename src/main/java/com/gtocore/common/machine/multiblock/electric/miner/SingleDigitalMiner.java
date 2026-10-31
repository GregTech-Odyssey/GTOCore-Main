package com.gtocore.common.machine.multiblock.electric.miner;

import com.gtocore.client.forge.ForgeClientEvent;

import com.gtolib.api.machine.feature.IDigitalMiner;
import com.gtolib.api.machine.impl.DigitalMinerLogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageAccess;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SingleDigitalMiner extends SimpleTieredMachine implements IDigitalMiner, IDataInfoProvider {
    // modify from gtmt

    private static final int SLOT_COLUMNS = 9;
    @SaveToDisk
    protected final StackInventory filterInventory;
    private final int maximumRadius;
    @Nullable
    protected TickableSubscription autoOutputSubs;

    /** 复用父类 SimpleTieredMachine 的 autoOutputMonitor（同一个 entry 只会有一个监控器）。 */
    @Nullable
    protected ISubscription exportItemSubs, energySubs;
    @Getter
    protected ItemFilter itemFilter;
    private final long energyPerTick;
    // miner property
    @Getter
    @Setter
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int minerRadius;
    @Getter
    @Setter
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int minHeight;
    @Getter
    @Setter
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int maxHeight;
    @SaveToDisk(defaultValue = "0")
    private int silkLevel;

    //////////////////////////////////////
    // ***** Initialization ******//

    public SingleDigitalMiner(MetaMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction, args);
        this.energyPerTick = GTValues.VEX[tier - 1];
        this.filterInventory = createFilterItemHandler();
        this.silkLevel = 0;
        this.minHeight = 0;
        this.maxHeight = 64;
        this.maximumRadius = (int) (8 * Math.pow(2, tier));
        this.minerRadius = maximumRadius;
    }

    /// ///////////////////////////////////

    protected StackInventory createFilterItemHandler() {
        var transfer = new StackInventory();
        transfer.setFilter(
                item -> item.is(GTItems.ITEM_FILTER.asItem()) || item.is(GTItems.TAG_FILTER.asItem()));
        transfer.setOnContentsChanged(() -> {
            if (getLevel() != null && !isRemote()) filterChange();
        });
        return transfer;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new DigitalMinerLogic(this);
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(exportItems.storage);
        clearInventory(filterInventory);
    }

    public AABB getMinerArea() {
        var pos = this.getPos();
        return new AABB(pos.getX() - minerRadius, maxHeight, pos.getZ() - minerRadius, pos.getX() + minerRadius, minHeight, pos.getZ() + minerRadius);
    }

    @Override
    public IDigitalMiner.MinerConfig getMinerConfig() {
        return new MinerConfig(getMinerArea(), silkLevel > 0 ? energyPerTick * 4 : energyPerTick, 20, (int) Math.pow(2, tier - 1), silkLevel, itemFilter, null, FluidMode.Ignore);
    }

    @Override
    public DigitalMinerLogic getRecipeLogic() {
        return (DigitalMinerLogic) super.getRecipeLogic();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            filterChange();
            if (getLevel() instanceof ServerLevel serverLevel) {
                TaskHandler.enqueueTask(serverLevel, this::updateAutoOutputSubscription);
            }
            exportItemSubs = exportItems.addChangedListener(this::updateAutoOutputSubscription);
        }
    }

    //////////////////////////////////////
    // ********** LOGIC **********//

    @Override
    public void onUnload() {
        super.onUnload();
        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }

        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
    }

    /// ///////////////////////////////////
    protected void updateAutoOutputSubscription() {
        if (!exportItems.isEmpty() && holder.blockEntityDirectionCache.hasAdjacentTarget(getLevel(), getPos(), getFrontFacing(), AEKeyTypes.ITEMS, StorageAccess.INSERT)) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, autoOutputMonitor, 20);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void autoOutput() {
        exportItems.exportToNearby(getFrontFacing());
        updateAutoOutputSubscription();
    }

    @Override
    public boolean drainInput(boolean simulate) {
        long cost = silkLevel > 0 ? energyPerTick * 4 : energyPerTick;
        long resultEnergy = energyContainer.getEnergyStored() - cost;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(cost);
            return true;
        }
        return false;
    }

    @Override
    public Widget createUIWidget() {
        var page = MachineDisplay.page(this, this::addDisplayText, this::addControls);
        page.addChild(SlotGrid.of(SLOT_COLUMNS, exportItems.storage.size(), i -> ItemSlot.of(exportItems.storage, i, true, false)));
        return page;
    }

    private void addControls(ControlPanel controls) {
        controls.add(MinerControls.filterAndSilk(this, filterInventory, () -> silkLevel != 0, this::setSilk));
        controls.addInt(MinerControls.RADIUS, this::getMinerRadius, value -> {
            minerRadius = value;
            getRecipeLogic().resetArea(true);
        }, 1, maximumRadius);
        controls.add(MinerControls.pair(MinerControls.cell(this, MIN_HEIGHT, this::getMinHeight, this::setMinHeight, this::minBuildHeight, this::maxBuildHeight),
                MinerControls.cell(this, MAX_HEIGHT, this::getMaxHeight, this::setMaxHeight, this::minBuildHeight, this::maxBuildHeight)));
        var reset = MinerControls.resetButton(this, this::resetRecipe);
        controls.add(UIElement.row(UISizes.CONTROL_HEIGHT).addChild(reset));
    }

    private int minBuildHeight() {
        var level = getLevel();
        return level == null ? -64 : level.getMinBuildHeight();
    }

    private int maxBuildHeight() {
        var level = getLevel();
        return level == null ? 320 : level.getMaxBuildHeight();
    }

    private void resetRecipe() {
        setWorkingEnabled(false);
        getRecipeLogic().resetRecipeLogic();
    }

    private void filterChange() {
        this.itemFilter = null;
        if (!filterInventory.getStackInSlot(0).isEmpty())
            this.itemFilter = ItemFilter.loadFilter(filterInventory.getStackInSlot(0));
        resetRecipe();
    }

    private void setSilk(boolean silk) {
        silkLevel = silk ? 1 : 0;
        resetRecipe();
    }

    private void addDisplayText(List<Component> textList) {
        textList.add(Component.translatable("gtocore.digital_miner.to_be_mined").append(String.valueOf(getRecipeLogic().getOreAmount())));
        if (getRecipeLogic().isDone())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done").withStyle(ChatFormatting.GREEN));
        else if (getRecipeLogic().isWorking())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.working").withStyle(ChatFormatting.GOLD));
        else if (!this.isWorkingEnabled())
            textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        if (getRecipeLogic().isInventoryFull())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull").withStyle(ChatFormatting.RED));
        if (!drainInput(true))
            textList.add(Component.translatable("gtceu.multiblock.large_miner.needspower").withStyle(ChatFormatting.RED));
    }

    //////////////////////////////////////
    // ******* Interaction *******//

    /// ///////////////////////////////////
    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (isRemote()) return InteractionResult.SUCCESS;

        if (!this.isActive()) {
            int currentRadius = minerRadius;
            if (currentRadius == 1)
                minerRadius = this.maximumRadius;
            else if (playerIn.isShiftKeyDown())
                minerRadius = Math.max(1, Math.round(currentRadius / 2.0f));
            else
                minerRadius = Math.max(1, currentRadius - 1);

            getRecipeLogic().resetArea(true);

            int workingArea = minerRadius;
            playerIn.sendSystemMessage(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        } else {
            playerIn.sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            int workingArea = minerRadius;
            return Collections.singletonList(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        }
        return new ArrayList<>();
    }

    @Nullable
    private ForgeClientEvent.HighlightNeed need;

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                WidgetIcons.HIGHLIGHT, WidgetIcons.HIGHLIGHT, () -> false,
                (clickData, pressed) -> {
                    if (clickData.isRemote && this.self().getLevel() != null) {
                        if (need != null && ForgeClientEvent.CUstomHighlightNeeds.containsKey(need)) {
                            ForgeClientEvent.CUstomHighlightNeeds.removeInt(need);
                            need = null;
                            return;
                        }
                        need = new ForgeClientEvent.HighlightNeed(
                                getPos().east(minerRadius).north(minerRadius).atY(maxHeight),
                                getPos().west(minerRadius).south(minerRadius).atY(minHeight),
                                ChatFormatting.WHITE.getColor());
                        ForgeClientEvent.CUstomHighlightNeeds.computeIfAbsent(
                                need, k -> 20 * 10);
                    }
                })
                .setTooltipsSupplier(pressed -> Collections.singletonList(Component.translatable(SHOW_RANGE_TOOLTIP))));
    }
}
