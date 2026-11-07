package vazkii.botania.common.block.block_entity.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.mana.LargeManaPoolBlock;

import java.util.List;

/**
 * 大型魔力池方块实体。
 *
 * <p>
 * 每个单元只保存自己的魔力，对外则以整个并池网络为单位：储量、容量与剩余空间都按连通单元汇总，
 * 注入与抽取在本单元不足时按顺序在相邻单元之间搬运，等效于原模组的合并池。
 * </p>
 */
public class LargeManaPoolBlockEntity extends ManaPoolBlockEntity {

    private List<LargeManaPoolBlockEntity> cachedNetwork;
    private long cachedTick = -1;

    public LargeManaPoolBlockEntity(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.LARGE_MANA_POOL.get(), pos, state);
    }

    public LargeManaPoolBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static final String TAG_CORE_X = "coreX";
    private static final String TAG_CORE_Z = "coreZ";

    /** 本单元相对 3×3 核心的偏移。 */
    private int coreDx;
    private int coreDz;

    /** 记录本单元相对 3×3 核心的偏移，并同步给客户端用于并池读数。 */
    public void setCoreOffset(int dx, int dz) {
        this.coreDx = dx;
        this.coreDz = dz;
        setChanged();
        markDispatchable();
    }

    public boolean isCore() {
        return coreDx == 0 && coreDz == 0;
    }

    /** 本单元所属 3×3 的核心位置。 */
    public BlockPos getCorePos() {
        return worldPosition.offset(coreDx, 0, coreDz);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // 旧存档的单元没有记录核心偏移：若自身不是核心，就近认领一个完整的核心
        if (level != null && !level.isClientSide && coreDx == 0 && coreDz == 0 && !level.getBlockState(worldPosition).getValue(LargeManaPoolBlock.CORE)) {
            repairCoreOffset();
        }
    }

    private void repairCoreOffset() {
        BlockState selfState = level.getBlockState(worldPosition);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos candidate = worldPosition.offset(dx, 0, dz);
                if (!level.isLoaded(candidate)) {
                    continue;
                }
                BlockState state = level.getBlockState(candidate);
                if (state.is(selfState.getBlock()) && state.getValue(LargeManaPoolBlock.CORE)) {
                    setCoreOffset(dx, dz);
                    return;
                }
            }
        }
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        coreDx = tag.getInt(TAG_CORE_X);
        coreDz = tag.getInt(TAG_CORE_Z);
    }

    @Override
    protected void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        tag.putInt(TAG_CORE_X, coreDx);
        tag.putInt(TAG_CORE_Z, coreDz);
    }

    public long unitCapacity() {
        ManaPoolSpec spec = poolSpec();
        return Math.max(1L, spec.capacity() / Math.max(1, spec.cells()));
    }

    long unitStored() {
        return mana;
    }

    long unitRoom() {
        return Math.max(0, unitCapacity() - mana);
    }

    void unitAdd(long amount) {
        if (amount > 0) {
            setMana(mana + amount);
        }
    }

    long unitRemove(long amount) {
        long taken = Math.min(amount, mana);
        if (taken > 0) {
            setMana(mana - taken);
        }
        return taken;
    }

    /** 与本单元连通的并池单元，按世界时间缓存，避免每 tick 重复遍历。 */
    List<LargeManaPoolBlockEntity> network() {
        if (level == null) {
            return List.of(this);
        }
        long now = level.getGameTime();
        if (cachedNetwork == null || cachedTick != now) {
            cachedNetwork = LargeManaPoolNetwork.collect(level, worldPosition);
            if (cachedNetwork.isEmpty()) {
                cachedNetwork = List.of(this);
            }
            cachedTick = now;
        }
        return cachedNetwork;
    }

    /** 整个并池网络的储量。 */
    public long getStoredMana() {
        return LargeManaPoolNetwork.stored(network());
    }

    /** 整个并池网络的容量。 */
    public long getStoredCapacity() {
        return LargeManaPoolNetwork.capacity(network());
    }

    /** 并池的单元数量。 */
    public int getUnits() {
        return LargeManaPoolNetwork.units(network());
    }

    /** 储量占比，用于展示。 */
    public float getFillFraction() {
        long capacity = getStoredCapacity();
        return capacity <= 0 ? 0F : Math.min(1F, (float) getStoredMana() / capacity);
    }

    @Override
    public long getCurrentManaLong() {
        return LargeManaPoolNetwork.stored(network());
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(Integer.MAX_VALUE, getCurrentManaLong());
    }

    @Override
    public long getMaxManaLong() {
        return LargeManaPoolNetwork.capacity(network());
    }

    @Override
    public int getMaxMana() {
        return (int) Math.min(Integer.MAX_VALUE, getMaxManaLong());
    }

    @Override
    public boolean isFull() {
        return LargeManaPoolNetwork.room(network()) <= 0;
    }

    @Override
    public long getAvailableSpaceForManaLong() {
        return LargeManaPoolNetwork.room(network());
    }

    @Override
    public int getAvailableSpaceForMana() {
        return (int) Math.min(Integer.MAX_VALUE, getAvailableSpaceForManaLong());
    }

    @Override
    public void receiveMana(int mana) {
        receiveManaLong(mana);
    }

    @Override
    public void receiveManaLong(long delta) {
        if (delta == 0) {
            return;
        }
        List<LargeManaPoolBlockEntity> units = network();
        if (delta > 0) {
            long remaining = Math.min(delta, LargeManaPoolNetwork.room(units));
            for (int i = 0; i < units.size() && remaining > 0; i++) {
                LargeManaPoolBlockEntity unit = units.get(i);
                long add = Math.min(remaining, unit.unitRoom());
                unit.unitAdd(add);
                remaining -= add;
            }
        } else {
            long remaining = -delta;
            for (int i = 0; i < units.size() && remaining > 0; i++) {
                remaining -= units.get(i).unitRemove(remaining);
            }
        }
    }

    @Override
    public long addMana(long amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        long change = Math.min(amount, getAvailableSpaceForManaLong());
        if (change > 0 && !simulate) {
            receiveManaLong(change);
        }
        return change;
    }

    @Override
    public long removeMana(long amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        long change = Math.min(amount, getCurrentManaLong());
        if (change > 0 && !simulate) {
            receiveManaLong(-change);
        }
        return change;
    }
}
