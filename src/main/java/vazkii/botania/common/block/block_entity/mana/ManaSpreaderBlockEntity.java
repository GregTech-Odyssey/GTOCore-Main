package vazkii.botania.common.block.block_entity.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaEndpoint;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntity;
import vazkii.botania.common.entity.ManaBurstEntity;

/**
 * 魔力发射器：从相邻魔力池补充魔力，右键时沿玩家视线发射一颗魔力弹。
 */
public class ManaSpreaderBlockEntity extends BotaniaBlockEntity implements ManaReceiver {

    private static final String TAG_MANA = "mana";
    private static final long MAX_MANA = 1000;

    private long mana;

    public ManaSpreaderBlockEntity(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.MANA_SPREADER.get(), pos, state);
    }

    public ManaSpreaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public long getMana() {
        return mana;
    }

    /** 沿给定方向发射魔力弹，成功后清空自身储量。 */
    public boolean shoot(Vec3 direction) {
        if (level == null || mana <= 0) {
            return false;
        }
        Vec3 dir = direction.normalize().scale(0.4);
        ManaBurstEntity burst = new ManaBurstEntity(level, worldPosition.getX() + 0.5 + dir.x,
                worldPosition.getY() + 0.5 + dir.y, worldPosition.getZ() + 0.5 + dir.z, dir.x, dir.y, dir.z);
        burst.setMana((int) Math.min(Integer.MAX_VALUE, mana));
        burst.setColor(0xFFFFFF);
        level.addFreshEntity(burst);
        mana = 0;
        markDispatchable();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ManaSpreaderBlockEntity self) {
        if (self.mana >= MAX_MANA) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(pos.relative(dir)) instanceof ManaPoolBlockEntity pool) {
                ManaEndpoint.transfer(pool, self, MAX_MANA - self.mana);
                break;
            }
        }
    }

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(Integer.MAX_VALUE, mana);
    }

    @Override
    public long getCurrentManaLong() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return mana >= MAX_MANA;
    }

    @Override
    public void receiveMana(int amount) {
        if (amount > 0) {
            mana = Math.min(MAX_MANA, mana + amount);
            markDispatchable();
        }
    }

    @Override
    public long addMana(long amount, boolean simulate) {
        if (amount <= 0 || isFull()) {
            return 0;
        }
        long change = Math.min(amount, MAX_MANA - mana);
        if (!simulate) {
            mana += change;
            markDispatchable();
        }
        return change;
    }

    @Override
    public long removeMana(long amount, boolean simulate) {
        long change = Math.min(amount, mana);
        if (!simulate) {
            mana -= change;
            markDispatchable();
        }
        return change;
    }

    public long getAvailableSpaceForManaLong() {
        return MAX_MANA - mana;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return false;
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        mana = tag.getLong(TAG_MANA);
    }

    @Override
    protected void writeNBT(CompoundTag tag) {
        tag.putLong(TAG_MANA, mana);
    }
}
