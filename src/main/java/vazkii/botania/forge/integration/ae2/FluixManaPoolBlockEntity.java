package vazkii.botania.forge.integration.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

/**
 * 福鲁伊克斯魔力池：池内魔力即所挂 ME 网络的魔力，读写都走网络存储。
 */
public class FluixManaPoolBlockEntity extends ManaPoolBlockEntity implements IInWorldGridNodeHost, IActionHost {

    private static final IGridNodeListener<FluixManaPoolBlockEntity> LISTENER = (owner, node) -> owner.setChanged();

    private final IManagedGridNode mainNode = GridHelper.createManagedNode(this, LISTENER)
            .setFlags()
            .setIdlePowerUsage(1)
            .setInWorldNode(true)
            .setVisualRepresentation(new ItemStack(vazkii.botania.common.BotaniaBlocks.FLUIX_MANA_POOL.get()));
    private final IActionSource source = IActionSource.ofMachine(this);

    private long shownMana;
    private long shownCapacity;
    private boolean comparatorDirty;
    private int ticks;

    public FluixManaPoolBlockEntity(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.FLUIX_MANA_POOL.get(), pos, state);
    }

    public FluixManaPoolBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
                                    BlockState state) {
        super(type, pos, state);
    }

    public void setOwner(Player player) {
        mainNode.setOwningPlayer(player);
    }

    @Nullable
    private MEStorage storage() {
        IGrid grid = mainNode.getGrid();
        return grid != null && mainNode.isActive() ? grid.getStorageService().getInventory() : null;
    }

    private long stored() {
        MEStorage inventory = storage();
        return inventory == null ? 0 : inventory.extract(ManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, source);
    }

    private long space() {
        MEStorage inventory = storage();
        return inventory == null ? 0 : inventory.insert(ManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, source);
    }

    private static long capacity(long stored, long space) {
        long total = stored + space;
        return total < 0 ? Long.MAX_VALUE : total;
    }

    boolean isOnGrid(@Nullable IGrid grid) {
        return grid != null && mainNode.getGrid() == grid;
    }

    private boolean client() {
        return level != null && level.isClientSide;
    }

    @Override
    public long getCurrentManaLong() {
        return client() ? shownMana : stored();
    }

    @Override
    public long getMaxManaLong() {
        return client() ? shownCapacity : capacity(stored(), space());
    }

    @Override
    public long getAvailableSpaceForManaLong() {
        return client() ? Math.max(0, shownCapacity - shownMana) : space();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(Integer.MAX_VALUE, getCurrentManaLong());
    }

    @Override
    public int getMaxMana() {
        return (int) Math.min(Integer.MAX_VALUE, getMaxManaLong());
    }

    @Override
    public int getAvailableSpaceForMana() {
        return (int) Math.min(Integer.MAX_VALUE, getAvailableSpaceForManaLong());
    }

    @Override
    public boolean isFull() {
        return getAvailableSpaceForManaLong() <= 0;
    }

    @Override
    public void receiveMana(int mana) {
        receiveManaLong(mana);
    }

    @Override
    public void receiveManaLong(long mana) {
        if (mana > 0) {
            addMana(mana, false);
        } else if (mana < 0) {
            removeMana(-mana, false);
        }
    }

    @Override
    public long addMana(long amount, boolean simulate) {
        return move(amount, simulate, true);
    }

    @Override
    public long removeMana(long amount, boolean simulate) {
        return move(amount, simulate, false);
    }

    private long move(long amount, boolean simulate, boolean insert) {
        if (amount <= 0 || client()) {
            return 0;
        }
        IGrid grid = mainNode.getGrid();
        MEStorage inventory = storage();
        if (grid == null || inventory == null) {
            return 0;
        }
        Actionable mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        long moved = insert ? StorageHelper.poweredInsert(grid.getEnergyService(), inventory, ManaKey.KEY, amount, source, mode) : StorageHelper.poweredExtraction(grid.getEnergyService(), inventory, ManaKey.KEY, amount, source, mode);
        if (moved > 0 && !simulate) {
            comparatorDirty = true;
            markDispatchable();
        }
        return Math.max(0, moved);
    }

    public long getComparatorSignal() {
        long max = getMaxManaLong();
        long current = getCurrentManaLong();
        if (max <= 0 || current <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(15, (long) ((double) current / max * 15.0)));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluixManaPoolBlockEntity self) {
        ManaPoolBlockEntity.serverTick(level, pos, state, self);
        if (++self.ticks % 10 != 0) {
            return;
        }
        long stored = self.stored();
        long capacity = capacity(stored, self.space());
        if (capacity != self.shownCapacity || stored != self.shownMana) {
            self.shownMana = stored;
            self.shownCapacity = capacity;
            self.comparatorDirty = true;
            self.markDispatchable();
        }
        if (self.comparatorDirty) {
            self.comparatorDirty = false;
            self.setChanged();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        GridHelper.onFirstTick(this, be -> be.mainNode.create(be.level, be.worldPosition));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        mainNode.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        mainNode.destroy();
    }

    @Override
    protected void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        mainNode.saveToNBT(tag);
        tag.putLong("shownMana", shownMana);
        tag.putLong("shownCapacity", shownCapacity);
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        mainNode.loadFromNBT(tag);
        shownMana = tag.getLong("shownMana");
        shownCapacity = tag.getLong("shownCapacity");
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction dir) {
        return mainNode.getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }
}
