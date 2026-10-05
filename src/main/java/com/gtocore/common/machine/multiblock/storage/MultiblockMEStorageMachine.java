package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.api.ae2.stacks.AEManaKeyHandler;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.block.BlockMap;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Assembly;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Size;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.MEStorageKeyView;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.MEStorage;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.data.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;

@DataGeneratorScanned
public class MultiblockMEStorageMachine extends MultiblockControllerMachine implements MEStorage, IDropSaveMachine, IWailaDisplayProvider {

    public static final int MIN_DEPTH = 2;
    public static final int MAX_DEPTH = 14;

    private static final Predicate<BlockState> PREDICATE = s -> {
        var block = s.getBlock();
        return block == GTBlocks.STEEL_HULL.get() || block == GTOMachines.VAULT_HATCH.get();
    };

    @RegisterLanguage(cn = "左侧宽度", en = "Left Width")
    private static final String LEFT_NAME = "gtocore.multiblock.vault.left";
    @RegisterLanguage(cn = "控制器向左连续的钢机壳/保险库仓数 + 1", en = "Consecutive steel hulls / vault hatches to the left of the controller, plus 1")
    private static final String LEFT_DESC = "gtocore.multiblock.vault.left.desc";
    @RegisterLanguage(cn = "右侧宽度", en = "Right Width")
    private static final String RIGHT_NAME = "gtocore.multiblock.vault.right";
    @RegisterLanguage(cn = "控制器向右连续的钢机壳/保险库仓数 + 1", en = "Consecutive steel hulls / vault hatches to the right of the controller, plus 1")
    private static final String RIGHT_DESC = "gtocore.multiblock.vault.right.desc";
    @RegisterLanguage(cn = "上方高度", en = "Upper Height")
    private static final String UP_NAME = "gtocore.multiblock.vault.up";
    @RegisterLanguage(cn = "控制器向上连续的钢机壳/保险库仓数 + 1", en = "Consecutive steel hulls / vault hatches above the controller, plus 1")
    private static final String UP_DESC = "gtocore.multiblock.vault.up.desc";
    @RegisterLanguage(cn = "下方高度", en = "Lower Height")
    private static final String DOWN_NAME = "gtocore.multiblock.vault.down";
    @RegisterLanguage(cn = "控制器向下连续的钢机壳/保险库仓数 + 1", en = "Consecutive steel hulls / vault hatches below the controller, plus 1")
    private static final String DOWN_DESC = "gtocore.multiblock.vault.down.desc";
    @RegisterLanguage(cn = "深度", en = "Depth")
    private static final String BACK_NAME = "gtocore.multiblock.vault.back";
    @RegisterLanguage(cn = "控制器向后连续的钢机壳/保险库仓/密封机械方块数 + 1", en = "Consecutive steel hulls / vault hatches / hermetic casings behind the controller, plus 1")
    private static final String BACK_DESC = "gtocore.multiblock.vault.back.desc";

    public static final ParamKey LEFT_EDGE = ParamKey.of(LEFT_NAME, LEFT_DESC);
    public static final ParamKey RIGHT_EDGE = ParamKey.of(RIGHT_NAME, RIGHT_DESC);
    public static final ParamKey UP_EDGE = ParamKey.of(UP_NAME, UP_DESC);
    public static final ParamKey DOWN_EDGE = ParamKey.of(DOWN_NAME, DOWN_DESC);
    public static final ParamKey BACK_EDGE = ParamKey.of(BACK_NAME, BACK_DESC);

    private int lDist = 0, rDist = 0, uDist = 0, dDist = 0, bDist = 0;

    @SaveToDisk
    @Getter
    @NotNull
    protected final AEKeyLongMap<AEKey> keyMap = new AEKeyLongMap<>();

    private int cells;
    private long storage;
    @Getter
    private long capacity;

    @Getter
    @NotNull
    private final LongSupplier storageSupplier = () -> storage;
    @Getter
    private final Runnable onChange = this::saveChanges;

    @Nullable
    private final AEKeyType type;
    @Getter
    @Nullable
    private final MEStorageKeyView<AEItemKey> itemView;
    @Getter
    @Nullable
    private final MEStorageKeyView<AEFluidKey> fluidView;
    @Nullable
    private final AEManaKeyHandler manaHandler;

    @NotNull
    private LazyOptional<ManaReceiver> capabilityMana;

    public MultiblockMEStorageMachine(MetaMachineBlockEntity holder, @Nullable AEKeyType type) {
        this(holder, type, type == null);
    }

    protected MultiblockMEStorageMachine(MetaMachineBlockEntity holder, @Nullable AEKeyType type, boolean mana) {
        super(holder);
        this.type = type;
        BooleanSupplier hasRoom = () -> storage < capacity;
        itemView = type == AEKeyTypes.ITEMS || type == null ? new MEStorageKeyView<>(this, AEKeyTypes.ITEMS, keyMap, hasRoom) : null;
        fluidView = type == AEKeyTypes.FLUIDS || type == null ? new MEStorageKeyView<>(this, AEKeyTypes.FLUIDS, keyMap, hasRoom) : null;
        if (mana) {
            manaHandler = new AEManaKeyHandler();
            manaHandler.setMap(keyMap);
            manaHandler.setStorageSupplier(storageSupplier);
            manaHandler.setOnChange(onChange);
            capabilityMana = LazyOptional.of(() -> manaHandler);
        } else {
            manaHandler = null;
            capabilityMana = LazyOptional.empty();
        }
    }

    @Override
    public Object getResourceIdentity() {
        return this;
    }

    @Override
    @Nullable
    public IKeyHandler<AEItemKey> getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return isFormed ? itemView : null;
    }

    @Override
    @Nullable
    public IKeyHandler<AEFluidKey> getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return isFormed ? fluidView : null;
    }

    public static Structure structure(MultiblockMachineDefinition definition) {
        var hatch = blocks(GTOMachines.VAULT_HATCH.get());
        var symbols = Symbols.create()
                .where('C', Predicates.controller(definition))
                .where('W', Predicates.blocks(GTBlocks.STEEL_HULL.get()).or(hatch))
                .where('S', GTOPredicates.hermeticCasing());
        var wallEnd = edge(false);
        return Structure.root(Piece.sized(MultiblockMEStorageMachine::box))
                .symbols(symbols)
                .measure(m -> m.param(LEFT_EDGE).toward(LEFT).until(wallEnd).range(2, MAX_DEPTH + 1))
                .measure(m -> m.param(RIGHT_EDGE).toward(RIGHT).until(wallEnd).range(2, MAX_DEPTH + 1))
                .measure(m -> m.param(UP_EDGE).toward(UP).until(wallEnd).range(2, MAX_DEPTH + 1))
                .measure(m -> m.param(DOWN_EDGE).toward(DOWN).until(wallEnd).range(2, MAX_DEPTH + 1))
                .measure(m -> m.param(BACK_EDGE).toward(BACK).until(edge(true)).range(MIN_DEPTH + 1, MAX_DEPTH + 1))
                .limit(hatch, size -> cells(dimensions(size::get)))
                .build();
    }

    private static TraceabilityPredicate edge(boolean hermetic) {
        return new TraceabilityPredicate(state -> !isWall(state.getBlockState(), hermetic) || state.getPos().distManhattan(state.controllerPos) > MAX_DEPTH, null, null);
    }

    private static boolean isWall(BlockState state, boolean hermetic) {
        if (PREDICATE.test(state)) return true;
        return hermetic && BlockMap.test(state.getBlock(), BlockMap.HERMETIC_CASING);
    }

    public static int[] dimensions(ToIntFunction<ParamKey> values) {
        int left = values.applyAsInt(LEFT_EDGE) - 1;
        int up = values.applyAsInt(UP_EDGE) - 1;
        if (left >= MAX_DEPTH || up >= MAX_DEPTH) return new int[] { 1, 1, 1, 1, MIN_DEPTH };
        int right = Math.min(values.applyAsInt(RIGHT_EDGE) - 1, MAX_DEPTH - left);
        int down = Math.min(values.applyAsInt(DOWN_EDGE) - 1, MAX_DEPTH - up);
        return new int[] { left, right, up, down, values.applyAsInt(BACK_EDGE) - 1 };
    }

    @Nullable
    public static int[] dimensions(@Nullable Assembly assembly) {
        if (assembly == null || !assembly.has(BACK_EDGE)) return null;
        return dimensions(assembly::get);
    }

    public static int cells(int[] dims) {
        return (dims[0] + dims[1] - 1) * (dims[2] + dims[3] - 1) * (dims[4] - 1);
    }

    private static Piece box(Size size) {
        var dims = dimensions(size::get);
        int left = dims[0], right = dims[1], up = dims[2], down = dims[3], back = dims[4];
        int width = left + right + 1;
        int height = up + down + 1;
        var backLayer = new String[height];
        var storageLayer = new String[height];
        var frontLayer = new String[height];
        for (int y = 0; y < height; y++) {
            backLayer[y] = "W".repeat(width);
            var storage = new StringBuilder(width);
            var front = new StringBuilder(width);
            for (int x = 0; x < width; x++) {
                storage.append(x == 0 || x == width - 1 || y == 0 || y == height - 1 ? 'W' : 'S');
                front.append(x == right && y == down ? 'C' : 'W');
            }
            storageLayer[y] = storage.toString();
            frontLayer[y] = front.toString();
        }
        var builder = Piece.start(LEFT, UP, FRONT).aisle(backLayer);
        for (int i = 0; i < back - 1; i++) builder.aisle(storageLayer);
        return builder.aisle(frontLayer).build();
    }

    private boolean updateStructureDimensions() {
        var dims = dimensions(getAssembly());
        if (dims == null) return false;
        lDist = dims[0];
        rDist = dims[1];
        uDist = dims[2];
        dDist = dims[3];
        bDist = dims[4];
        cells = cells(dims);
        return true;
    }

    @Override
    public void onStructureFormed() {
        // 容量要在 super 之前算好：部件（保险库仓）在 super 里绑定处理器时会读当前容量
        updateStructureDimensions();
        refreshCapacity();
        super.onStructureFormed();
        clearDirectionCache();
        notifyNeighborsUpdate();
    }

    /** 按当前结构重算容量并同步给各处理器；容量口径不同时子类覆写 {@link #computeCapacity()}。 */
    protected void refreshCapacity() {
        capacity = Math.max(0, computeCapacity());
        if (manaHandler != null) manaHandler.setCapacity(capacity);
    }

    /** 容量（按 {@link #getCapacityUsage} 计）：密封机械方块数量 × 800 × (密封等级 + 1)。 */
    protected long computeCapacity() {
        return cells * 800L * (getMultiblockState().getMatchContext().getOrDefault(GTORecipeDataKeys.HERMETIC_CASING_TIER, 0) + 1);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        capacity = 0;
        if (manaHandler != null) manaHandler.setCapacity(0);
        clearDirectionCache();
        this.notifyNeighborsUpdate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (manaHandler != null) {
            capabilityMana = LazyOptional.of(() -> manaHandler);
            manaHandler.setPos(getPos());
            manaHandler.setLevel(getLevel());
        }
        double totalAmount = 0;
        for (var e : keyMap) {
            totalAmount += getCapacityUsage(e.getKey().getType(), e.getLongValue());
        }
        this.storage = (long) totalAmount;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (manaHandler != null) manaHandler.setLevel(null);
        capabilityMana.invalidate();
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (tag.get("keymap") instanceof ByteArrayTag byteArrayTag) {
            getFieldDataManager().readFieldFromData(Data.readData(byteArrayTag.getAsByteArray()), 0, "keyMap");
        }
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.putByteArray("keymap", getFieldDataManager().writeFieldToData("keyMap").writeToBytes());
    }

    @Override
    public @Nullable MEStorage getStorageCap(@Nullable Direction side) {
        if (!isFormed) return null;
        return side == null || side == getFrontFacing() ? this : super.getStorageCap(side);
    }

    @Override
    public @Nullable <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!isFormed) return null;
        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            if (side == null || side == getFrontFacing()) {
                return capabilityMana.cast();
            }
            return LazyOptional.empty();
        }
        return null;
    }

    @Override
    public Component getDescription() {
        return getDefinition().asItem().getDescription();
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return capacity > storage;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        var type = what.getType();
        if (!isFormed || (this.type != null && type != this.type)) return 0;
        var amountPerCapacity = getAmountPerCapacity(type);
        amount = Math.min(amountPerCapacity * (capacity - storage), amount);
        if (amount < 1) return 0;
        if (mode == Actionable.MODULATE) {
            keyMap.insert(what, amount);
            saveChanges();
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (mode == Actionable.MODULATE) {
            var extract = keyMap.extract(what, amount);
            if (extract > 0) saveChanges();
            return extract;
        } else {
            return Math.min(amount, keyMap.getAmount(what));
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var map = keyMap;
        if (map.isEmpty()) return;
        out.addAll(map.size(), m -> map.fastForEach(m::insert));
    }

    /** 存储内容变化后标脏并按当前内容重算已用容量；子类写入路径也要调它。 */
    protected void saveChanges() {
        holder.setChanged();
        double totalAmount = 0;
        for (var e : keyMap) {
            totalAmount += getCapacityUsage(e.getKey().getType(), e.getLongValue());
        }
        this.storage = (long) totalAmount;
    }

    @Override
    public void appendWailaTooltip(CompoundTag compoundTag, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        var ints = compoundTag.getIntArray("dimensions");
        if (ints.length != 0) iTooltip.add(Component.translatable("gtceu.multiblock.dimensions.1", ints[0], ints[1], ints[2]));
        var capacity = compoundTag.getLong("capacity");
        var storage = compoundTag.getLong("storage");
        var displayedCapacity = type == null ? FormattingUtil.formatNumbers(capacity) :
                type.formatAmount(capacity * getAmountPerCapacity(type), AmountFormat.FULL);
        iTooltip.add(Component.translatable("gtocore.lang.template.capacity.-990262758", displayedCapacity));
        iTooltip.add(Component.translatable("ae2.gto_extension.craft_used_percent", FormattingUtil.formatNumbers(capacity > 0 ? storage * 100D / capacity : 0)));
    }

    @Override
    public void appendWailaData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        compoundTag.putLong("capacity", capacity);
        compoundTag.putLong("storage", storage);
        if (!isFormed) return;
        compoundTag.putIntArray("dimensions", new int[] { lDist + rDist + 1, uDist + dDist + 1, bDist + 1 });
    }

    private static final AEKeyType ITEM = AEKeyTypes.ITEMS;

    /** 每个内部容量单位可存入的实际数量，与插入上限和 Jade 显示共用。 */
    private static long getAmountPerCapacity(AEKeyType type) {
        return type == ITEM ? 24 : type.getAmountPerByte() / 8;
    }

    private static double getCapacityUsage(AEKeyType type, long amount) {
        if (type == ITEM) return (double) amount / 24;
        return (double) amount * 8 / type.getAmountPerByte();
    }
}
