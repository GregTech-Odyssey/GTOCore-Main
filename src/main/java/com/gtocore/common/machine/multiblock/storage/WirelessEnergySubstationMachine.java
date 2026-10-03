package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.api.wireless.energy.EnergyAccount;
import com.gtocore.api.wireless.energy.GridClock;
import com.gtocore.api.wireless.energy.IWirelessGridProvider;
import com.gtocore.api.wireless.energy.ProviderRegistry;
import com.gtocore.api.wireless.energy.WirelessGrid;
import com.gtocore.api.wireless.energy.WirelessText;
import com.gtocore.client.hud.HUDConfigurator;
import com.gtocore.common.block.WirelessEnergyUnitBlock;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.wireless.energy.GridMapEntry;
import com.gtocore.common.wireless.energy.GridReadouts;
import com.gtocore.common.wireless.energy.map.GridSummaryPanel;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.multiblock.NoRecipeLogicMultiblockMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.hepdd.gtmthings.api.capability.IBindable;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@DataGeneratorScanned
public final class WirelessEnergySubstationMachine extends NoRecipeLogicMultiblockMachine implements IWirelessGridProvider, IBindable, ITierCasingMachine, IEnergyInfoProvider, IMachineLife {

    @RegisterLanguage(cn = "本站", en = "This Station")
    private static final String STATION = "gtocore.machine.wireless_energy_substation.station";
    @RegisterLanguage(cn = "本站容量", en = "Station capacity")
    private static final String STATION_CAPACITY = "gtocore.machine.wireless_energy_substation.capacity";
    @RegisterLanguage(cn = "储能单元", en = "Energy units")
    private static final String STATION_UNITS = "gtocore.machine.wireless_energy_substation.units";
    @RegisterLanguage(cn = "本维度节点", en = "Node in this dimension")
    private static final String STATION_NODE = "gtocore.machine.wireless_energy_substation.node";
    @RegisterLanguage(cn = "高于玻璃等级的单元不计入容量", en = "Units above the glass tier add no capacity")
    private static final String OVER_TIER = "gtocore.machine.wireless_energy_substation.over_tier";
    private static final Component OVER_TIER_TEXT = Component.translatable(OVER_TIER);
    private static final Component NONE = Component.empty();

    private final TierCasingTrait tierCasingTrait;
    private final Multimap<Integer, BlockPos> wirelessEnergyUnitPositions = Multimaps.newMultimap(new Int2ObjectOpenHashMap<>(), ObjectOpenHashSet::new);
    private BigInteger stationCapacity = BigInteger.ZERO;
    private Component unitsText = MultiblockPage.NO_VALUE;
    private boolean unitsOverTier;

    public WirelessEnergySubstationMachine(MetaMachineBlockEntity holder) {
        super(holder);
        tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.GLASS_TIER);
    }

    private void register() {
        if (isRemote()) return;
        int tier = getCasingTier(GTORecipeDataKeys.GLASS_TIER);
        var data = getMultiblockState().getMatchContext().get(GTOPredicates.DataKeys.WIRELESS_ENERGY_UNIT);
        BigInteger capacity = BigInteger.ZERO;
        double lossWeight = 0;
        int unitTier = -1;
        wirelessEnergyUnitPositions.clear();
        if (data != null) {
            for (WirelessEnergyUnitBlock.BlockData block : data) {
                if (block.block() == null) {
                    wirelessEnergyUnitPositions.put(0, block.pos());
                    continue;
                }
                if (block.block().getTier() <= tier) {
                    capacity = capacity.add(block.block().getCapacity());
                    lossWeight += block.block().getCapacity().doubleValue() * block.block().getLoss();
                    unitTier = Math.max(unitTier, block.block().getTier());
                }
                wirelessEnergyUnitPositions.put(block.block().getTier(), block.pos());
            }
            data.clear();
        }
        stationCapacity = capacity;
        describeUnits(tier);
        ProviderRegistry.registerTower(this, capacity, lossWeight, unitTier);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        register();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        wirelessEnergyUnitPositions.clear();
        stationCapacity = BigInteger.ZERO;
        unitsText = MultiblockPage.NO_VALUE;
        unitsOverTier = false;
        ProviderRegistry.unregisterLater(this, 20);
    }

    @Override
    public void onMachineRemoved() {
        ProviderRegistry.unregister(this);
    }

    @Override
    public boolean isProvidingWirelessGrid() {
        return isFormed();
    }

    private EnergyAccount account() {
        return WirelessGrid.accountIfPresent(isRemote() ? null : getOwnerUUID());
    }

    @Override
    public UIElement createUIWidget() {
        var invalid = MachineDisplay.page(this);
        var scroller = ScrollerView.page("wireless_energy_substation.page", UISizes.CONTENT_WIDTH).adaptiveWidth();
        scroller.addScrollViewChild(Form.page().addChildren(GridSummaryPanel.of(UISizes.CONTENT_WIDTH, this::getOwnerUUID),
                stationPanel(), GridMapEntry.openButton(this, GridMapEntry.OPEN, GridMapEntry.HINT)));
        var formed = UIElement.column(LayoutStyle.AUTO).addChild(scroller);
        var root = UIElement.column(LayoutStyle.AUTO).addChildren(invalid, formed);
        root.addSyncValue(SyncValue.ofBool(this::isFormed).onChanged(structureFormed -> {
            invalid.setDisplay(!structureFormed);
            formed.setDisplay(structureFormed);
        }));
        return root;
    }

    private UIElement stationPanel() {
        var panel = new StatusPanel(LayoutStyle.AUTO);
        panel.addLine(STATION_CAPACITY, MultiblockPage.cachedRef(() -> stationCapacity, capacity -> Component.literal(FormattingUtil.formatNumbers(capacity) + " EU")));
        panel.addLine(STATION_UNITS, () -> unitsText)
                .bindLevel(() -> unitsOverTier ? Level.WARNING : Level.NORMAL)
                .bindDetail(() -> unitsOverTier ? OVER_TIER_TEXT : NONE);
        panel.addLine(STATION_NODE, MultiblockPage.cached(() -> getOffsetTimer() / 20, second -> nodeText()));
        return UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(TextLine.translatable(LayoutStyle.AUTO, STATION).bindClientColor(UITheme::panelText), panel);
    }

    private Component nodeText() {
        var level = getLevel();
        return level == null ? MultiblockPage.NO_VALUE : WirelessText.plainNode(account().node(level.dimension()));
    }

    private void describeUnits(int casingTier) {
        var tiers = new IntArrayList(wirelessEnergyUnitPositions.keySet());
        tiers.sort(IntComparators.OPPOSITE_COMPARATOR);
        var text = Component.empty();
        boolean overTier = false;
        for (int i = 0; i < tiers.size(); i++) {
            int tier = tiers.getInt(i);
            if (tier < 1 || tier > GTValues.MAX) continue;
            if (!text.getSiblings().isEmpty()) text.append(" ");
            text.append(GTValues.VN[tier] + "×" + wirelessEnergyUnitPositions.get(tier).size());
            overTier |= tier > casingTier;
        }
        unitsText = text.getSiblings().isEmpty() ? MultiblockPage.NO_VALUE : text;
        unitsOverTier = overTier;
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        HUDConfigurator c;
        configuratorPanel.attachConfigurators(
                c = new HUDConfigurator(GTOGuiTextures.HUD_ON, GTOGuiTextures.HUD_OFF));
        if (isRemote()) c.setHudInstance("wireless_energy_hud");
    }

    @Override
    public Reference2IntMap<TierDataKey> getCasingTiers() {
        return tierCasingTrait.getCasingTiers();
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        var account = account();
        if (account.isNone()) return new EnergyInfo(BigInteger.ZERO, BigInteger.ZERO);
        return new EnergyInfo(account.totalCapacity(), account.totalStorage());
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return true;
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public boolean preferTeamName() {
        return true;
    }

    @Override
    public long getInputPerSec() {
        var stats = account().stats();
        return stats == null ? 0 : (long) (stats.nowIn(GridClock.second()) * GridReadouts.TICKS_PER_SECOND);
    }

    @Override
    public long getOutputPerSec() {
        var stats = account().stats();
        return stats == null ? 0 : (long) (stats.nowOut(GridClock.second()) * GridReadouts.TICKS_PER_SECOND);
    }

    public int substituteBlocks(WirelessEnergyUnitBlock block, int count, ServerPlayer player) {
        if (getLevel() == null || wirelessEnergyUnitPositions.isEmpty() || count <= 0) {
            return 0;
        }
        List<WirelessEnergyUnitBlock.BlockData> candidates = new ArrayList<>();
        int tier = block.getTier();
        for (int t = 1; t < tier; t++) {
            var positionsForTier = wirelessEnergyUnitPositions.get(t);
            for (BlockPos pos : positionsForTier) {
                if (pos != null) {
                    candidates.add(new WirelessEnergyUnitBlock.BlockData(WirelessEnergyUnitBlock.get(t), pos));
                }
            }
        }
        if (candidates.isEmpty()) {
            return 0;
        }
        candidates.sort(Comparator.comparingInt((WirelessEnergyUnitBlock.BlockData data) -> data.pos().getY())
                .thenComparingInt(data -> data.pos().getX())
                .thenComparingInt(data -> data.pos().getZ()));
        int numToReplace = Math.min(count, candidates.size());
        List<WirelessEnergyUnitBlock.BlockData> toReplace = candidates.subList(0, numToReplace);
        int successfulCount = 0;
        for (WirelessEnergyUnitBlock.BlockData data : toReplace) {
            BlockPos pos = data.pos();
            var originBlockDrop = data.block();
            if (getLevel().setBlock(pos, block.defaultBlockState(), 11)) {
                successfulCount++;
                if (originBlockDrop != null && !player.getInventory().add(originBlockDrop.asItem().getDefaultInstance())) {
                    player.drop(originBlockDrop.asItem().getDefaultInstance(), false);
                }
            }
        }
        if (successfulCount > 0) {
            this.requestCheck();
        }
        return successfulCount;
    }
}
