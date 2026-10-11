package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.api.wireless.energy.EnergyAccount;
import com.gtocore.api.wireless.energy.GridClock;
import com.gtocore.api.wireless.energy.IWirelessGridProvider;
import com.gtocore.api.wireless.energy.Provider;
import com.gtocore.api.wireless.energy.ProviderRegistry;
import com.gtocore.api.wireless.energy.WirelessGrid;
import com.gtocore.api.wireless.energy.WirelessText;
import com.gtocore.client.hud.HUDConfigurator;
import com.gtocore.common.block.BlockMap;
import com.gtocore.common.block.WirelessEnergyTowerUnit;
import com.gtocore.common.block.WirelessEnergyTransferUnitBlock;
import com.gtocore.common.block.WirelessEnergyUnitBlock;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.wireless.energy.GridMapEntry;
import com.gtocore.common.wireless.energy.GridReadouts;
import com.gtocore.common.wireless.energy.map.GridMapMode;
import com.gtocore.common.wireless.energy.map.GridSummaryPanel;

import com.gtolib.GTOCore;
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
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IMachineSubWindows;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Size;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GTBlocks;
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
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.ToIntFunction;

import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;

/**
 * 无线能源塔：宽为 3~11 的奇数、深 3~11、高 3~20 的盒子，棱为框架、六面为同级玻璃、内部放存储与传输单元，控制器在正面底边正中。
 * 成型后按玻璃等级向所在维度登记容量与同档吞吐。
 */
@DataGeneratorScanned
public final class WirelessEnergySubstationMachine extends NoRecipeLogicMultiblockMachine implements IWirelessGridProvider, IBindable, ITierCasingMachine, IEnergyInfoProvider, IMachineLife, IMachineSubWindows {

    public static final int MIN_SIZE = 3;
    public static final int MAX_WIDTH = 11;
    public static final int MAX_HEIGHT = 20;

    @RegisterLanguage(cn = "左侧宽度", en = "Left Width")
    private static final String LEFT_NAME = "gtocore.multiblock.wireless_energy_substation.left";
    @RegisterLanguage(cn = "控制器向左连续的框架数 + 1", en = "Consecutive frames to the left of the controller, plus 1")
    private static final String LEFT_DESC = "gtocore.multiblock.wireless_energy_substation.left.desc";
    @RegisterLanguage(cn = "右侧宽度", en = "Right Width")
    private static final String RIGHT_NAME = "gtocore.multiblock.wireless_energy_substation.right";
    @RegisterLanguage(cn = "控制器向右连续的框架数 + 1", en = "Consecutive frames to the right of the controller, plus 1")
    private static final String RIGHT_DESC = "gtocore.multiblock.wireless_energy_substation.right.desc";
    @RegisterLanguage(cn = "高度", en = "Height")
    private static final String UP_NAME = "gtocore.multiblock.wireless_energy_substation.up";
    @RegisterLanguage(cn = "从底边到顶边的格数", en = "Blocks from the bottom edge to the top edge")
    private static final String UP_DESC = "gtocore.multiblock.wireless_energy_substation.up.desc";
    @RegisterLanguage(cn = "深度", en = "Depth")
    private static final String BACK_NAME = "gtocore.multiblock.wireless_energy_substation.back";
    @RegisterLanguage(cn = "从正面到背面的格数", en = "Blocks from the front face to the back face")
    private static final String BACK_DESC = "gtocore.multiblock.wireless_energy_substation.back.desc";
    @RegisterLanguage(cn = "宽度", en = "Width")
    private static final String WIDTH_NAME = "gtocore.multiblock.wireless_energy_substation.width";
    @RegisterLanguage(cn = "左右方向的格数，控制器位于正面底边正中，因此只能为奇数", en = "Blocks from side to side; the controller sits at the middle of the front bottom edge, so the width is always odd")
    private static final String WIDTH_DESC = "gtocore.multiblock.wireless_energy_substation.width.desc";

    public static final ParamKey LEFT_EDGE = ParamKey.of(LEFT_NAME, LEFT_DESC);
    public static final ParamKey RIGHT_EDGE = ParamKey.of(RIGHT_NAME, RIGHT_DESC);
    public static final ParamKey UP_EDGE = ParamKey.of(UP_NAME, UP_DESC);
    public static final ParamKey BACK_EDGE = ParamKey.of(BACK_NAME, BACK_DESC);
    public static final ParamKey WIDTH = ParamKey.of(WIDTH_NAME, WIDTH_DESC);

    @RegisterLanguage(cn = "本站", en = "This Station")
    private static final String STATION = "gtocore.machine.wireless_energy_substation.station";
    @RegisterLanguage(cn = "本站容量", en = "Station capacity")
    private static final String STATION_CAPACITY = "gtocore.machine.wireless_energy_substation.capacity";
    @RegisterLanguage(cn = "存储单元", en = "Storage units")
    private static final String STATION_UNITS = "gtocore.machine.wireless_energy_substation.units";
    @RegisterLanguage(cn = "传输单元", en = "Transfer units")
    private static final String STATION_TRANSFER_UNITS = "gtocore.machine.wireless_energy_substation.transfer_units";
    @RegisterLanguage(cn = "本站吞吐", en = "Station throughput")
    private static final String STATION_THROUGHPUT = "gtocore.machine.wireless_energy_substation.throughput";
    @RegisterLanguage(cn = "输入、输出各 %s EU/t", en = "%s EU/t each for input and output")
    private static final String STATION_THROUGHPUT_VALUE = "gtocore.machine.wireless_energy_substation.throughput.value";
    @RegisterLanguage(cn = "本维度同档输出", en = "Same-tier output here")
    private static final String BANK_OUTPUT = "gtocore.machine.wireless_energy_substation.bank_output";
    @RegisterLanguage(cn = "本维度同档输入", en = "Same-tier input here")
    private static final String BANK_INPUT = "gtocore.machine.wireless_energy_substation.bank_input";
    @RegisterLanguage(cn = "%s / %s EU/t", en = "%s / %s EU/t")
    private static final String BANK_VALUE = "gtocore.machine.wireless_energy_substation.bank_value";
    @RegisterLanguage(cn = "本维度节点", en = "Node in this dimension")
    private static final String STATION_NODE = "gtocore.machine.wireless_energy_substation.node";
    @RegisterLanguage(cn = "高于玻璃等级的单元不生效", en = "Units above the glass tier have no effect")
    private static final String OVER_TIER = "gtocore.machine.wireless_energy_substation.over_tier";
    private static final Component OVER_TIER_TEXT = Component.translatable(OVER_TIER);
    private static final Component NONE = Component.empty();

    private final TierCasingTrait tierCasingTrait;
    private final Multimap<Integer, BlockPos> storagePositions = Multimaps.newMultimap(new Int2ObjectOpenHashMap<>(), ObjectOpenHashSet::new);
    private final Multimap<Integer, BlockPos> transferPositions = Multimaps.newMultimap(new Int2ObjectOpenHashMap<>(), ObjectOpenHashSet::new);
    private Component unitsText = MultiblockPage.NO_VALUE;
    private Component transferText = MultiblockPage.NO_VALUE;
    private boolean unitsOverTier;
    private int towerTier = -1;
    private long throughput;

    public WirelessEnergySubstationMachine(MetaMachineBlockEntity holder) {
        super(holder);
        tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.GLASS_TIER);
    }

    public static Structure structure(MultiblockMachineDefinition definition) {
        var symbols = Symbols.create()
                .where('C', Predicates.controller(definition))
                .where('F', Predicates.blocks(GTOBlocks.WIRELESS_ENERGY_TOWER_FRAME.get()))
                .where('G', GTOPredicates.glass())
                .where('U', Predicates.air().or(GTOPredicates.wirelessEnergyUnit()));
        var sideEnd = new TraceabilityPredicate(WirelessEnergySubstationMachine::isSideEnd, null, null);
        var backEnd = new TraceabilityPredicate(WirelessEnergySubstationMachine::isPastBack, null, null);
        return Structure.root(Piece.sized(WirelessEnergySubstationMachine::box))
                .symbols(symbols)
                .measure(m -> m.param(LEFT_EDGE).toward(LEFT).until(sideEnd).range(1, MAX_WIDTH).preset(2))
                .measure(m -> m.param(RIGHT_EDGE).toward(RIGHT).until(sideEnd).range(1, MAX_WIDTH).preset(2))
                .measure(m -> m.param(UP_EDGE).toward(UP).until(sideEnd).range(MIN_SIZE, MAX_HEIGHT))
                .measure(m -> m.param(BACK_EDGE).toward(BACK).until(backEnd).range(MIN_SIZE, MAX_WIDTH))
                .symmetric(WIDTH, LEFT_EDGE, RIGHT_EDGE, -1, MIN_SIZE, MAX_WIDTH)
                .build();
    }

    private static boolean isShell(BlockState state) {
        var block = state.getBlock();
        return block == GTOBlocks.WIRELESS_ENERGY_TOWER_FRAME.get() || BlockMap.test(block, BlockMap.GLASSMAP);
    }

    private static boolean isSideEnd(MultiblockState state) {
        if (tooFar(state)) return true;
        var block = state.getBlockState().getBlock();
        if (block == GTOBlocks.WIRELESS_ENERGY_TOWER_FRAME.get()) return false;
        if (!BlockMap.test(block, BlockMap.GLASSMAP)) return true;
        var pos = state.getPos();
        var controller = state.controllerPos;
        var previous = pos.offset(Integer.signum(controller.getX() - pos.getX()), Integer.signum(controller.getY() - pos.getY()), Integer.signum(controller.getZ() - pos.getZ()));
        return !previous.equals(controller) && state.world.isLoaded(previous) && state.world.getBlockState(previous).getBlock() == GTOBlocks.WIRELESS_ENERGY_TOWER_FRAME.get();
    }

    private static boolean tooFar(MultiblockState state) {
        return state.getPos().distManhattan(state.controllerPos) > MAX_HEIGHT + MAX_WIDTH * 2;
    }

    private static boolean isPastBack(MultiblockState state) {
        if (tooFar(state)) return true;
        if (isShell(state.getBlockState())) return false;
        var previous = state.getPos().relative(state.controller.self().getFrontFacing());
        return !previous.equals(state.controllerPos) && state.world.isLoaded(previous) && isShell(state.world.getBlockState(previous));
    }

    public static int[] dimensions(ToIntFunction<ParamKey> values) {
        return new int[] { values.applyAsInt(LEFT_EDGE) - 1, values.applyAsInt(RIGHT_EDGE) - 1, values.applyAsInt(UP_EDGE) - 1, 0, values.applyAsInt(BACK_EDGE) };
    }

    private static Piece box(Size size) {
        var dims = dimensions(size::get);
        int right = dims[1], down = dims[3], depth = dims[4];
        int width = dims[0] + right + 1;
        int height = dims[2] + down + 1;
        var builder = Piece.start(LEFT, UP, FRONT);
        for (int z = 0; z < depth; z++) {
            var layer = new String[height];
            for (int y = 0; y < height; y++) {
                var row = new StringBuilder(width);
                for (int x = 0; x < width; x++) {
                    int edges = (x == 0 || x == width - 1 ? 1 : 0) + (y == 0 || y == height - 1 ? 1 : 0) + (z == 0 || z == depth - 1 ? 1 : 0);
                    if (z == depth - 1 && x == right && y == down) row.append('C');
                    else row.append(edges >= 2 ? 'F' : edges == 1 ? 'G' : 'U');
                }
                layer[y] = row.toString();
            }
            builder.aisle(layer);
        }
        return builder.build();
    }

    private void register() {
        if (isRemote()) return;
        int glassTier = getCasingTier(GTORecipeDataKeys.GLASS_TIER);
        var data = getMultiblockState().getMatchContext().get(GTOPredicates.DataKeys.WIRELESS_ENERGY_UNIT);
        var counts = new int[GTValues.MAX + 1];
        long total = 0;
        storagePositions.clear();
        transferPositions.clear();
        if (data != null) {
            for (var entry : data) {
                var unit = entry.unit();
                if (unit == null) continue;
                int tier = unit.getTier();
                boolean storage = unit instanceof WirelessEnergyUnitBlock;
                (storage ? storagePositions : transferPositions).put(tier, entry.pos());
                if (tier > glassTier) continue;
                if (storage) counts[tier]++;
                long t = unit.getThroughput();
                total = total > Long.MAX_VALUE - t ? Long.MAX_VALUE : total + t;
            }
            data.clear();
        }
        var units = new ArrayList<Provider.Unit>();
        for (int t = 1; t <= GTValues.MAX; t++) {
            if (counts[t] == 0) continue;
            var unit = WirelessEnergyUnitBlock.get(t);
            if (unit != null) units.add(new Provider.Unit(unit.isGridScaled() ? t : Provider.Unit.FIXED, counts[t], unit.getCapacity(), unit.getLoss()));
        }
        towerTier = Math.min(glassTier, GTValues.MAX);
        throughput = total;
        unitsText = describe(storagePositions);
        transferText = describe(transferPositions);
        unitsOverTier = overTier(storagePositions, glassTier) || overTier(transferPositions, glassTier);
        ProviderRegistry.registerTower(this, units, towerTier, throughput);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        register();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        storagePositions.clear();
        transferPositions.clear();
        unitsText = MultiblockPage.NO_VALUE;
        transferText = MultiblockPage.NO_VALUE;
        unitsOverTier = false;
        towerTier = -1;
        throughput = 0;
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
        panel.addLine(STATION_CAPACITY, MultiblockPage.cachedRef(() -> ProviderRegistry.towerCapacity(this), capacity -> Component.literal(FormattingUtil.formatNumbers(capacity) + " EU")));
        panel.addLine(STATION_UNITS, () -> unitsText)
                .bindLevel(() -> unitsOverTier ? Level.WARNING : Level.NORMAL)
                .bindDetail(() -> unitsOverTier ? OVER_TIER_TEXT : NONE);
        panel.addLine(STATION_TRANSFER_UNITS, () -> transferText);
        panel.addLine(STATION_THROUGHPUT, MultiblockPage.cached(() -> throughput, value -> Component.translatable(STATION_THROUGHPUT_VALUE, FormattingUtil.formatNumbers(value))));
        panel.addLine(BANK_OUTPUT, MultiblockPage.cached(() -> getOffsetTimer() / 20, second -> bankText(true)));
        panel.addLine(BANK_INPUT, MultiblockPage.cached(() -> getOffsetTimer() / 20, second -> bankText(false)));
        panel.addLine(STATION_NODE, MultiblockPage.cached(() -> getOffsetTimer() / 20, second -> nodeText()));
        return UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(TextLine.translatable(LayoutStyle.AUTO, STATION).bindClientColor(UITheme::panelText), panel);
    }

    private Component nodeText() {
        var level = getLevel();
        return level == null ? MultiblockPage.NO_VALUE : WirelessText.plainNode(account().node(level.dimension()));
    }

    private Component bankText(boolean output) {
        var level = getLevel();
        if (level == null || towerTier < 0) return MultiblockPage.NO_VALUE;
        var node = account().node(level.dimension());
        return output ? Component.translatable(BANK_VALUE, FormattingUtil.formatNumbers(node.outputUsed(towerTier)), FormattingUtil.formatNumbers(node.outputCapacity(towerTier))) :
                Component.translatable(BANK_VALUE, FormattingUtil.formatNumbers(node.inputUsed(towerTier)), FormattingUtil.formatNumbers(node.inputCapacity(towerTier)));
    }

    @Override
    public @Nullable ModularUI createSubWindow(String key, Player player) {
        return GridMapEntry.window(this, key, player, GridMapMode.VIEW);
    }

    private static Component describe(Multimap<Integer, BlockPos> positions) {
        var tiers = new IntArrayList(positions.keySet());
        tiers.sort(IntComparators.OPPOSITE_COMPARATOR);
        var text = Component.empty();
        for (int i = 0; i < tiers.size(); i++) {
            int tier = tiers.getInt(i);
            if (tier < 1 || tier > GTValues.MAX) continue;
            if (!text.getSiblings().isEmpty()) text.append(" ");
            text.append(GTValues.VN[tier] + "×" + positions.get(tier).size());
        }
        return text.getSiblings().isEmpty() ? MultiblockPage.NO_VALUE : text;
    }

    private static boolean overTier(Multimap<Integer, BlockPos> positions, int casingTier) {
        for (int tier : positions.keySet()) {
            if (tier > casingTier) return true;
        }
        return false;
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

    public int substituteBlocks(WirelessEnergyTowerUnit unit, int count, ServerPlayer player) {
        var positions = unit instanceof WirelessEnergyTransferUnitBlock ? transferPositions : storagePositions;
        if (getLevel() == null || positions.isEmpty() || count <= 0) {
            return 0;
        }
        List<WirelessEnergyTowerUnit.BlockData> candidates = new ArrayList<>();
        int tier = unit.getTier();
        for (int t = 1; t < tier; t++) {
            var old = unit instanceof WirelessEnergyTransferUnitBlock ? WirelessEnergyTransferUnitBlock.get(t) : WirelessEnergyUnitBlock.get(t);
            for (BlockPos pos : positions.get(t)) {
                if (pos != null) candidates.add(new WirelessEnergyTowerUnit.BlockData(old, pos));
            }
        }
        if (candidates.isEmpty()) {
            return 0;
        }
        candidates.sort(Comparator.comparingInt((WirelessEnergyTowerUnit.BlockData data) -> data.pos().getY())
                .thenComparingInt(data -> data.pos().getX())
                .thenComparingInt(data -> data.pos().getZ()));
        int numToReplace = Math.min(count, candidates.size());
        int successfulCount = 0;
        for (var data : candidates.subList(0, numToReplace)) {
            if (getLevel().setBlock(data.pos(), unit.towerBlock().defaultBlockState(), 11)) {
                successfulCount++;
                var origin = data.unit();
                if (origin != null && !player.getInventory().add(origin.towerBlock().asItem().getDefaultInstance())) {
                    player.drop(origin.towerBlock().asItem().getDefaultInstance(), false);
                }
            }
        }
        if (successfulCount > 0) {
            this.requestCheck();
        }
        return successfulCount;
    }

    @Override
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) TaskHandler.enqueueTask(serverLevel, () -> convertLegacyTower(0), 1);
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static final int LEGACY_RADIUS = 3, LEGACY_DEPTH = 6, LEGACY_RETRIES = 30;

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private void convertLegacyTower(int attempt) {
        if (isRemoved() || !(getLevel() instanceof ServerLevel level)) return;
        var front = getFrontFacing();
        if (front.getAxis() == Direction.Axis.Y) return;
        var side = front.getClockWise();
        var back = front.getOpposite();
        var origin = getPos();
        var steel = GTBlocks.CASING_STEEL_SOLID.get();
        if (!level.isLoaded(origin.relative(side)) || level.getBlockState(origin.relative(side)).getBlock() != steel) return;
        for (int a = -LEGACY_RADIUS; a <= LEGACY_RADIUS; a += LEGACY_RADIUS * 2) {
            for (int b = 0; b <= LEGACY_DEPTH; b += LEGACY_DEPTH) {
                if (!level.isLoaded(origin.relative(side, a).relative(back, b))) {
                    if (attempt < LEGACY_RETRIES) TaskHandler.enqueueTask(level, () -> convertLegacyTower(attempt + 1), 40);
                    return;
                }
            }
        }
        int layers = legacyLayers(level, origin, side, back, steel);
        if (layers < 0) return;
        var glass = level.getBlockState(origin.above());
        int height = Math.min(layers + 2, MAX_HEIGHT);

        var frame = GTOBlocks.WIRELESS_ENERGY_TOWER_FRAME.get().defaultBlockState();
        var drops = new Object2IntOpenHashMap<Item>();
        for (int h = 0; h < height; h++) {
            for (int a = -LEGACY_RADIUS; a <= LEGACY_RADIUS; a++) {
                for (int b = 0; b <= LEGACY_DEPTH; b++) {
                    if (h == 0 && a == 0 && b == 0) continue;
                    int edges = (Math.abs(a) == LEGACY_RADIUS ? 1 : 0) + (b == 0 || b == LEGACY_DEPTH ? 1 : 0) + (h == 0 || h == height - 1 ? 1 : 0);
                    if (edges == 0) continue;
                    var pos = origin.relative(side, a).relative(back, b).above(h);
                    var state = level.getBlockState(pos);
                    var target = edges >= 2 ? frame : glass;
                    if (state == target) continue;
                    if (state.getBlock() instanceof WirelessEnergyTowerUnit unit) drops.addTo(unit.towerBlock().asItem(), 1);
                    level.setBlock(pos, target, 3);
                }
            }
        }
        var dropAt = origin.relative(front);
        for (var entry : drops.object2IntEntrySet()) {
            var item = entry.getKey();
            for (int left = entry.getIntValue(); left > 0;) {
                int count = Math.min(left, item.getMaxStackSize());
                Block.popResource(level, dropAt, new ItemStack(item, count));
                left -= count;
            }
        }
        GTOCore.LOGGER.info("[无线电网] 旧版无线能源塔 {} 已原地转换为新结构（{} 层储能，高 {}）", origin, layers, height);
        requestCheck();
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static int legacyLayers(ServerLevel level, BlockPos origin, Direction side, Direction back, Block steel) {
        for (int a = -LEGACY_RADIUS; a <= LEGACY_RADIUS; a++) {
            for (int b = 0; b <= LEGACY_DEPTH; b++) {
                if ((a != 0 || b != 0) && level.getBlockState(origin.relative(side, a).relative(back, b)).getBlock() != steel) return -1;
            }
        }
        var glass = level.getBlockState(origin.above()).getBlock();
        if (!BlockMap.test(glass, BlockMap.GLASSMAP)) return -1;
        int layers = 0;
        for (int h = 1; h <= 31; h++) {
            var above = origin.above(h);
            if (!level.isLoaded(above)) return -1;
            var block = level.getBlockState(above).getBlock();
            if (block == steel) break;
            if (block != glass) return -1;
            for (int a = -LEGACY_RADIUS; a <= LEGACY_RADIUS; a += LEGACY_RADIUS * 2) {
                for (int b = 0; b <= LEGACY_DEPTH; b += LEGACY_DEPTH) {
                    if (level.getBlockState(origin.relative(side, a).relative(back, b).above(h)).getBlock() != steel) return -1;
                }
            }
            layers = h;
        }
        if (layers < 1) return -1;
        var roof = origin.above(layers + 1);
        for (int a = -LEGACY_RADIUS; a <= LEGACY_RADIUS; a++) {
            for (int b = 0; b <= LEGACY_DEPTH; b++) {
                if (level.getBlockState(roof.relative(side, a).relative(back, b)).getBlock() != steel) return -1;
            }
        }
        return layers;
    }
}
