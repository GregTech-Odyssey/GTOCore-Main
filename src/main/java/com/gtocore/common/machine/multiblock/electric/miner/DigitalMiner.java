package com.gtocore.common.machine.multiblock.electric.miner;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.IDigitalMiner;
import com.gtolib.api.machine.impl.DigitalMinerLogic;
import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.ItemFilterBehaviour;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.uiwidgets.prospector.ProspectorMapView;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalMiner extends TierCasingMultiblockMachine implements IDigitalMiner {

    @RegisterLanguage(cn = "矿脉地图", en = "Ore Map")
    private static final String MAP = "gtocore.digital_miner.map";
    private static final int MAX_MAP_CHUNKS = 25;

    @SaveToDisk
    protected final StackInventory filterInventory;
    @SaveToDisk
    public IDigitalMiner.FluidMode fluidMode = IDigitalMiner.FluidMode.Harvest;
    @Nullable
    protected ISubscription energySubs;
    protected Filter<?, ?> itemFilter;
    protected Filter<?, ?> fluidFilter;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int xRadialLength;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int zRadialLength;
    @Getter
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int xOffset;
    @Getter
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int zOffset;
    @Setter
    @Getter
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int minHeight;
    @Setter
    @Getter
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int maxHeight;

    // ===================== 逻辑相关方法 =====================
    @Getter
    @SaveToDisk(defaultValue = "0")
    private int silkLevel;
    @SyncToClient
    private long energyPerTickBase = 0L;
    @Getter
    @SyncToClient
    private int parallelMining = 0;
    @SyncToClient
    private int prospectorRadius;
    @SyncToClient
    @SaveToDisk(defaultValue = "1")
    private int maxRadius = 1;
    // ===================== Getter/Setter =====================
    @Getter
    @SyncToClient
    @SaveToDisk(defaultValue = "false")
    private boolean showRange = false;
    @Getter
    private long energyPerTick;

    public DigitalMiner(MetaMachineBlockEntity holder) {
        super(holder, GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
        this.filterInventory = createFilterItemHandler();
        this.silkLevel = 0;
        this.minHeight = 0;
        this.maxHeight = 64;
        this.xRadialLength = 1;
        this.zRadialLength = 1;
        this.xOffset = 0;
        this.zOffset = 0;
    }

    // ===================== 交互相关方法 =====================

    protected StackInventory createFilterItemHandler() {
        var transfer = new StackInventory();
        transfer.setFilter(
                item -> item.getItem() instanceof ComponentItem componentItem &&
                        componentItem.getComponents().stream().anyMatch(c -> c instanceof ItemFilterBehaviour));
        transfer.setOnContentsChanged(() -> {
            if (getLevel() != null && !isRemote()) filterChange();
        });
        return transfer;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new DigitalMinerLogic(this);
    }

    // ===================== 生命周期相关 =====================
    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            filterChange();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        tier = Math.min(getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER), tier);
        this.energyPerTickBase = (int) Math.pow(4, getTier()) * 2L;
        this.energyPerTick = energyPerTickBase * (silkLevel == 0 ? 1 : 4);
        this.parallelMining = (int) Math.min(4096, 4 * Math.pow(2, getTier()));
        this.maxRadius = (int) Math.min(8 * Math.pow(2, getTier()), 128);
        this.prospectorRadius = Math.min(getTier() / 2 + 1, 6);
        resetRecipe();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.parallelMining = 0;
        resetRecipe();
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(filterInventory);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (isWorkingAllowed && getRecipeLogic().isDone()) getRecipeLogic().resetRecipeLogic();
        super.setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    public MinerConfig getMinerConfig() {
        return new MinerConfig(getMinerArea(), energyPerTick, getSpeed(), parallelMining, silkLevel, itemFilter, fluidFilter, fluidMode);
    }

    private void resetRecipe() {
        setWorkingEnabled(false);
        getRecipeLogic().resetRecipeLogic();
    }

    private void filterChange() {
        this.itemFilter = null;
        this.fluidFilter = null;
        var stack = filterInventory.getStackInSlot(0);
        if (!stack.isEmpty()) {
            if (stack.is(GTItems.TAG_FLUID_FILTER.asItem()) || stack.is(GTItems.FLUID_FILTER.asItem()))
                this.fluidFilter = FluidFilter.loadFilter(stack);
            else this.itemFilter = ItemFilter.loadFilter(filterInventory.getStackInSlot(0));
        }
        resetRecipe();
    }

    private void setSilk(boolean silk) {
        silkLevel = silk ? 1 : 0;
        energyPerTick = energyPerTickBase * (silk ? 4 : 1);
        resetRecipe();
    }

    public boolean drainInput(boolean simulate) {
        var energyContainer = getEnergyContainer();
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    @Override
    public void addControls(ControlPanel controls) {
        super.addControls(controls);
        controls.add(MinerControls.filterAndSilk(this, filterInventory, () -> silkLevel != 0, this::setSilk));
        var modes = IDigitalMiner.FluidMode.values();
        var fluid = ButtonGroup.single(modes.length, i -> fluidModeTitle(modes[i]), () -> fluidMode.ordinal(), i -> {
            if (i < 0 || i >= modes.length) return;
            fluidMode = modes[i];
            resetRecipe();
            onChanged();
        }).compact();
        fluid.layout(l -> l.flex(1));
        controls.add(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(Form.fieldLabel(MinerControls.FLUID), fluid));
        controls.add(MinerControls.pair(MinerControls.cell(this, X_OFFSET, this::getXOffset, v -> xOffset = v, () -> -maxRadius, () -> maxRadius),
                MinerControls.cell(this, Z_OFFSET, this::getZOffset, v -> zOffset = v, () -> -maxRadius, () -> maxRadius)));
        controls.add(MinerControls.pair(MinerControls.cell(this, XRADIAL_LENGTH, this::getMinerXRadius, this::setMinerXRadius, () -> 0, () -> maxRadius * 2 + 1),
                MinerControls.cell(this, ZRADIAL_LENGTH, this::getMinerZRadius, this::setMinerZRadius, () -> 0, () -> maxRadius * 2 + 1)));
        controls.add(MinerControls.pair(MinerControls.cell(this, MIN_HEIGHT, this::getMinHeight, this::setMinHeight, this::minBuildHeight, this::maxBuildHeight),
                MinerControls.cell(this, MAX_HEIGHT, this::getMaxHeight, this::setMaxHeight, this::minBuildHeight, this::maxBuildHeight)));
        controls.add(MinerControls.switchAndButton(this, SHOW_RANGE, SHOW_RANGE_TOOLTIP, () -> showRange, value -> showRange = value,
                MinerControls.resetButton(this, this::resetRecipe)));
    }

    private static Component fluidModeTitle(IDigitalMiner.FluidMode mode) {
        return Component.translatable(switch (mode) {
            case Harvest -> "gtocore.digital_miner.fluid_mode.title.Harvest";
            case Ignore -> "gtocore.digital_miner.fluid_mode.title.Ignore";
            case Void -> "gtocore.digital_miner.fluid_mode.title.Void";
        });
    }

    private int minBuildHeight() {
        var level = getLevel();
        return level == null ? -64 : level.getMinBuildHeight();
    }

    private int maxBuildHeight() {
        var level = getLevel();
        return level == null ? 320 : level.getMaxBuildHeight();
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable(TO_BE_MINED).append(String.valueOf(getRecipeLogic().getOreAmount())));
        if (!isFormed()) return;
        if (getRecipeLogic().isDone())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done").withStyle(ChatFormatting.GREEN));
        if (getRecipeLogic().isInventoryFull())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull").withStyle(ChatFormatting.RED));
        if (!drainInput(true))
            textList.add(Component.translatable("gtceu.multiblock.large_miner.needspower").withStyle(ChatFormatting.RED));
        if (MultiblockPage.isScreenText()) return;
        textList.add(Component.translatable(PARALLEL, parallelMining));
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new MapTab());
    }

    private final class MapTab implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var modes = new ProspectorMode<?>[] { ProspectorMode.ORE };
            var view = new ProspectorMapView((MachineWindow) widget, Math.max(1, prospectorRadius), modes, () -> 0, () -> false,
                    ProspectorMapView.Options.machine(getPos(), mapChunks(), new RangePicker()));
            return ScrollerView.page("digital_miner.map", UISizes.CONTENT_WIDTH).addScrollViewChild(view);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(GTItems.PROSPECTOR_LV.asStack());
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable(MAP));
        }

        @Override
        public Component getTitle() {
            return Component.translatable(MAP);
        }
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return side == getFrontFacing() || side == Direction.DOWN || super.canConnectRedstone(side);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        if (getLevel().hasNeighborSignal(fromPos)) {
            setWorkingEnabled(true);
        }
    }

    public int getMinerXRadius() {
        return xRadialLength;
    }

    public void setMinerXRadius(int minerRadius) {
        this.xRadialLength = minerRadius;
    }

    public int getMinerZRadius() {
        return zRadialLength;
    }

    public void setMinerZRadius(int minerRadius) {
        this.zRadialLength = minerRadius;
    }

    @Override
    public DigitalMinerLogic getRecipeLogic() {
        return (DigitalMinerLogic) super.getRecipeLogic();
    }

    public int getSpeed() {
        return 40;
    }

    public AABB getMinerArea() {
        if (xRadialLength > maxRadius * 2 + 1) xRadialLength = maxRadius * 2 + 1;
        if (zRadialLength > maxRadius * 2 + 1) zRadialLength = maxRadius * 2 + 1;
        if (minHeight > maxHeight) {
            int temp = minHeight;
            minHeight = maxHeight;
            maxHeight = temp;
        }
        BlockPos pos = getPos();
        BlockPos pos1 = pos.offset(xOffset, 0, zOffset).atY(minHeight);
        BlockPos pos2 = pos1.offset((xRadialLength), 0, (zRadialLength)).atY(maxHeight);
        return new AABB(pos1, pos2);
    }

    private int mapChunks() {
        int half = (3 * maxRadius + 16) / 16;
        return Math.min(2 * half + 1, MAX_MAP_CHUNKS);
    }

    private final class RangePicker implements ProspectorMapView.RegionPicker {

        private ProspectorMapView.Region cached = ProspectorMapView.Region.NONE;

        @Override
        public ProspectorMapView.Region current() {
            var pos = getPos();
            int x = pos.getX() + xOffset, z = pos.getZ() + zOffset;
            var region = cached;
            if (region.minX() != x || region.minZ() != z || region.maxX() != x + xRadialLength || region.maxZ() != z + zRadialLength) {
                cached = region = new ProspectorMapView.Region(x, z, x + xRadialLength, z + zRadialLength);
            }
            return region;
        }

        @Override
        public void pick(Player player, ProspectorMapView.Region region) {
            var pos = getPos();
            int maxLength = maxRadius * 2 + 1;
            xOffset = Mth.clamp(region.minX() - pos.getX(), -maxRadius, maxRadius);
            zOffset = Mth.clamp(region.minZ() - pos.getZ(), -maxRadius, maxRadius);
            xRadialLength = Mth.clamp(region.maxX() - region.minX(), 0, maxLength);
            zRadialLength = Mth.clamp(region.maxZ() - region.minZ(), 0, maxLength);
            resetRecipe();
            onChanged();
        }
    }
}
