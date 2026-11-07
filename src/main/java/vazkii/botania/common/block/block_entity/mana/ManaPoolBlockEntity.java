package vazkii.botania.common.block.block_entity.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.KeyLocked;
import vazkii.botania.api.mana.ManaEndpoint;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.common.BotaniaReplica;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntity;

import java.util.List;
import java.util.Optional;

/**
 * 魔力池方块实体。
 *
 * <p>
 * 移植原模组的魔力存储、注入物品传输、输出模式与钥匙锁定；火花附着与注入配方由后续类接入。
 * 客户端只同步储量与颜色，视觉由方块模型负责。
 * </p>
 */
public class ManaPoolBlockEntity extends BotaniaBlockEntity
                                 implements ManaPool, KeyLocked, SparkAttachable, ManaPoolSpec.Holder {

    public static final int MAX_MANA = 1000000;

    private static final String TAG_MANA = "mana";
    private static final String TAG_OUTPUTTING = "outputting";
    private static final String TAG_CAN_ACCEPT = "canAccept";
    private static final String TAG_CAN_SPARE = "canSpare";
    private static final String TAG_INPUT_KEY = "inputKey";
    private static final String TAG_OUTPUT_KEY = "outputKey";
    private static final String TAG_COLOR = "color";

    private static final int SYNC_INTERVAL = 10;

    private final ManaPoolSpec spec;
    protected long mana;
    private boolean outputting;
    private boolean canAccept = true;
    private boolean canSpare = true;
    private String inputKey = "";
    private String outputKey = "";
    private Optional<DyeColor> color = Optional.empty();

    protected boolean dirty = false;
    private int ticks = 0;

    public ManaPoolBlockEntity(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.MANA_POOL.get(), pos, state);
    }

    public ManaPoolBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.spec = ManaPoolSpec.of(state.getBlock(), ManaPoolSpec.POOL);
    }

    @Override
    public ManaPoolSpec poolSpec() {
        return spec;
    }

    // ==================== 储量 ====================

    @Override
    public int getCurrentMana() {
        return (int) Math.min(Integer.MAX_VALUE, mana);
    }

    @Override
    public long getCurrentManaLong() {
        return mana;
    }

    @Override
    public int getMaxMana() {
        return (int) Math.min(Integer.MAX_VALUE, spec.capacity());
    }

    @Override
    public long getMaxManaLong() {
        return spec.capacity();
    }

    /** 下方是魔力销毁器时视为永不装满。 */
    public boolean isVoidBelow() {
        return level != null && level.getBlockState(worldPosition.below()).getBlock() == BotaniaReplica.BOTANIA.getBlock("mana_void");
    }

    @Override
    public boolean isFull() {
        return !isVoidBelow() && mana >= getMaxManaLong();
    }

    @Override
    public void receiveMana(int mana) {
        receiveManaLong(mana);
    }

    @Override
    public void receiveManaLong(long delta) {
        if (delta > 0) {
            long room = isVoidBelow() ? Long.MAX_VALUE : Math.max(0, getMaxManaLong() - mana);
            delta = Math.min(delta, room);
        }
        if (delta == 0) return;
        setMana(Math.max(0, mana + delta));
    }

    @Override
    public long addMana(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        long room = isVoidBelow() ? amount : Math.max(0, getMaxManaLong() - mana);
        long change = Math.min(amount, room);
        if (change > 0 && !simulate) {
            setMana(mana + change);
        }
        return change;
    }

    @Override
    public long removeMana(long amount, boolean simulate) {
        long change = Math.min(amount, mana);
        if (change <= 0) return 0;
        if (!simulate) {
            setMana(mana - change);
        }
        return change;
    }

    protected void setMana(long value) {
        if (mana == value) return;
        mana = value;
        dirty = true;
    }

    // ==================== 输出与传输 ====================

    @Override
    public boolean isOutputtingPower() {
        return outputting;
    }

    public void setOutputting(boolean outputting) {
        this.outputting = outputting;
        markDispatchable();
    }

    public boolean canAccept() {
        return canAccept;
    }

    public boolean canSpare() {
        return canSpare;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
    }

    // ==================== 颜色与钥匙 ====================

    @Override
    public Optional<DyeColor> getColor() {
        return color;
    }

    @Override
    public void setColor(Optional<DyeColor> color) {
        this.color = color;
        markDispatchable();
    }

    @Override
    public String getInputKey() {
        return inputKey;
    }

    @Override
    public String getOutputKey() {
        return outputKey;
    }

    // ==================== 火花 ====================

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return getAttachedSpark() == null;
    }

    @Override
    public void attachSpark(ManaSpark entity) {}

    @Nullable
    @Override
    public ManaSpark getAttachedSpark() {
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return getCurrentManaLong() < getMaxManaLong();
    }

    @Override
    public int getAvailableSpaceForMana() {
        return (int) Math.min(Integer.MAX_VALUE, getAvailableSpaceForManaLong());
    }

    @Override
    public long getAvailableSpaceForManaLong() {
        if (isVoidBelow()) return Long.MAX_VALUE;
        return Math.max(0, getMaxManaLong() - mana);
    }

    // ==================== 比较器 ====================

    public static int calculateComparatorLevel(int mana, int max) {
        int val = (int) ((double) mana / (double) max * 15.0);
        if (mana > 0) {
            val = Math.max(val, 1);
        }
        return Math.min(val, 15);
    }

    // ==================== tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, ManaPoolBlockEntity self) {
        if (self.dirty && self.ticks % SYNC_INTERVAL == 0) {
            self.markDispatchable();
            self.dirty = false;
        }
        if (self.outputting && self.mana > 0) {
            self.outputToNeighbours();
        }
        self.infuseItems(level, pos);
        self.ticks++;
        if (level instanceof net.minecraft.server.level.ServerLevel server && self.mana > 0 && level.getGameTime() % 6 == 0) {
            server.sendParticles(vazkii.botania.client.fx.WispParticleData.wisp(0.25F, 0.4F, 0.65F, 1F, 1F),
                    pos.getX() + 0.5, pos.getY() + 0.95, pos.getZ() + 0.5, 1, 0.3, 0.05, 0.3, 0.01);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ManaPoolBlockEntity self) {
        self.ticks++;
    }

    /** 处理落在池上的物品注入。 */
    private void infuseItems(Level level, BlockPos pos) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos, pos.offset(1, 1, 1)));
        if (items.isEmpty()) {
            return;
        }
        BlockState catalyst = level.getBlockState(pos.below());
        for (ItemEntity item : items) {
            if (!item.isAlive()) {
                continue;
            }
            if (ManaInfusion.process(level, this, item, catalyst, spec.infusionRate(), pos) > 0) {
                dirty = true;
            }
        }
    }

    /** 输出模式：把魔力推给相邻的非池接收者。 */
    private void outputToNeighbours() {
        if (level == null) return;
        long rate = spec.itemTransferRate();
        for (Direction dir : Direction.values()) {
            if (getCurrentManaLong() <= 0) return;
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            if (!(be instanceof ManaReceiver receiver) || receiver instanceof ManaPool) continue;
            long moved = ManaEndpoint.transfer(this, receiver, rate);
            if (moved > 0) dirty = true;
        }
    }

    // ==================== 存盘 ====================

    @Override
    protected void readNBT(CompoundTag tag) {
        mana = tag.getLong(TAG_MANA);
        outputting = tag.getBoolean(TAG_OUTPUTTING);
        canAccept = !tag.contains(TAG_CAN_ACCEPT) || tag.getBoolean(TAG_CAN_ACCEPT);
        canSpare = !tag.contains(TAG_CAN_SPARE) || tag.getBoolean(TAG_CAN_SPARE);
        inputKey = tag.getString(TAG_INPUT_KEY);
        outputKey = tag.getString(TAG_OUTPUT_KEY);
        color = tag.contains(TAG_COLOR) ? Optional.ofNullable(DyeColor.byName(tag.getString(TAG_COLOR), null)) : Optional.empty();
    }

    @Override
    protected void writeNBT(CompoundTag tag) {
        tag.putLong(TAG_MANA, mana);
        tag.putBoolean(TAG_OUTPUTTING, outputting);
        tag.putBoolean(TAG_CAN_ACCEPT, canAccept);
        tag.putBoolean(TAG_CAN_SPARE, canSpare);
        tag.putString(TAG_INPUT_KEY, inputKey);
        tag.putString(TAG_OUTPUT_KEY, outputKey);
        color.ifPresent(dye -> tag.putString(TAG_COLOR, dye.getName()));
    }

    @NotNull
    @Override
    public String toString() {
        return "ManaPoolBlockEntity{" + worldPosition + ", mana=" + mana + "}";
    }
}
