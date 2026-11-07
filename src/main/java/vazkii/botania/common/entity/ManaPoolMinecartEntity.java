package vazkii.botania.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.BotaniaBlocks;
import vazkii.botania.common.BotaniaItems;

/**
 * 魔力池矿车：沿铁轨移动，可携带并搬运魔力。
 */
public class ManaPoolMinecartEntity extends AbstractMinecart {

    private static final int MAX_MANA = 1_000_000;
    private static final int PARTICLE_COLOR = 0x40E0D0;
    private static final String TAG_MANA = "mana";
    private static final EntityDataAccessor<Integer> MANA = SynchedEntityData.defineId(ManaPoolMinecartEntity.class,
            EntityDataSerializers.INT);

    public ManaPoolMinecartEntity(EntityType<ManaPoolMinecartEntity> type, Level level) {
        super(type, level);
    }

    public ManaPoolMinecartEntity(Level level, double x, double y, double z) {
        super(BotaniaEntities.POOL_MINECART.get(), level, x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MANA, 0);
    }

    @NotNull
    @Override
    public BlockState getDisplayBlockState() {
        return BotaniaBlocks.MANA_POOL.get().defaultBlockState();
    }

    @NotNull
    @Override
    public AbstractMinecart.Type getMinecartType() {
        return Type.RIDEABLE;
    }

    @Override
    protected boolean canAddPassenger(@NotNull net.minecraft.world.entity.Entity passenger) {
        return false;
    }

    @Override
    protected void applyNaturalSlowdown() {
        float factor = 0.98F;
        setDeltaMovement(getDeltaMovement().multiply(factor, 0, factor));
    }

    @NotNull
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(BotaniaItems.POOL_MINECART.get());
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 8;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && Math.random() > 1F - (double) getMana() / MAX_MANA * 0.1) {
            float red = (PARTICLE_COLOR >> 16 & 0xFF) / 255F;
            float green = (PARTICLE_COLOR >> 8 & 0xFF) / 255F;
            float blue = (PARTICLE_COLOR & 0xFF) / 255F;
            double x = Mth.floor(getX());
            double y = Mth.floor(getY());
            double z = Mth.floor(getZ());
            WispParticleData data = WispParticleData.wisp((float) Math.random() / 3F, red, green, blue, 2F);
            level().addParticle(data, x + 0.3 + Math.random() * 0.5, y + 0.85 + Math.random() * 0.25, z + Math.random(),
                    0, (float) Math.random() / 25F, 0);
        }
    }

    @Override
    public void moveAlongTrack(@NotNull BlockPos pos, @NotNull BlockState state) {
        super.moveAlongTrack(pos, state);
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos pumpPos = pos.relative(dir);
            if (!level().getBlockState(pumpPos).is(BotaniaBlocks.PUMP.get()) || !(level().getBlockEntity(pumpPos) instanceof vazkii.botania.common.block.block_entity.mana.ManaPumpBlockEntity pump)) {
                continue;
            }
            BlockPos poolPos = pumpPos.relative(dir);
            if (!(level().getBlockEntity(poolPos) instanceof vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity pool)) {
                continue;
            }
            if (pump.hasRedstone) {
                continue;
            }
            var pumpDir = level().getBlockState(pumpPos)
                    .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            if (pumpDir == dir) {
                long transfer = Math.min(10_000L, pool.getCurrentManaLong());
                long taken = transfer > 0 ? pool.removeMana(transfer, false) : 0;
                if (taken > 0) {
                    setMana((int) Math.min(MAX_MANA, getMana() + taken));
                    pump.setActive(true);
                }
            } else if (pumpDir == dir.getOpposite() && !pool.isFull()) {
                long transfer = Math.min(10_000L, getMana());
                long given = transfer > 0 ? pool.addMana(transfer, false) : 0;
                if (given > 0) {
                    setMana(getMana() - (int) given);
                    pump.setActive(true);
                }
            }
            pump.hasCart = true;
            pump.hasCartOnTop = true;
            pump.comparator = Mth.floor((float) getMana() / MAX_MANA * 15);
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_MANA, getMana());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setMana(tag.getInt(TAG_MANA));
    }

    @NotNull
    @Override
    public Item getDropItem() {
        return BotaniaItems.POOL_MINECART.get();
    }

    @Override
    public int getComparatorLevel() {
        return Mth.floor((float) getMana() / MAX_MANA * 15);
    }

    public int getMana() {
        return entityData.get(MANA);
    }

    public void setMana(int mana) {
        entityData.set(MANA, Mth.clamp(mana, 0, MAX_MANA));
    }
}
