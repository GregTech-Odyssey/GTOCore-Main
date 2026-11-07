package vazkii.botania.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaReceiver;

/**
 * 魔力弹：沿直线飞行，命中魔力接收者时注入魔力，命中其它方块或超时后消失。
 */
public class ManaBurstEntity extends Entity {

    private static final EntityDataAccessor<Integer> MANA = SynchedEntityData.defineId(ManaBurstEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(ManaBurstEntity.class,
            EntityDataSerializers.INT);

    private int ticksExisted;

    public ManaBurstEntity(EntityType<ManaBurstEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public ManaBurstEntity(Level level, double x, double y, double z, double mx, double my, double mz) {
        this(BotaniaEntities.MANA_BURST.get(), level);
        setPos(x, y, z);
        setDeltaMovement(mx, my, mz);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MANA, 0);
        entityData.define(COLOR, 0xFFFFFF);
    }

    public int getMana() {
        return entityData.get(MANA);
    }

    public void setMana(int mana) {
        entityData.set(MANA, mana);
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    public void setColor(int color) {
        entityData.set(COLOR, color);
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (ticksExisted > 100 || getY() < level().getMinBuildHeight() - 10) {
            discard();
            return;
        }
        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        if (!level().isClientSide) {
            BlockPos pos = blockPosition();
            var state = level().getBlockState(pos);
            if (!state.isAir() && !state.getCollisionShape(level(), pos).isEmpty()) {
                var blockEntity = level().getBlockEntity(pos);
                ManaReceiver receiver = blockEntity == null ? null : blockEntity.getCapability(vazkii.botania.api.BotaniaForgeCapabilities.MANA_RECEIVER, null).orElse(null);
                if (receiver != null && receiver.canReceiveManaFromBursts()) {
                    long added = receiver.addMana(getMana(), false);
                    setMana((int) Math.max(0, getMana() - added));
                    if (getMana() <= 0) {
                        discard();
                    }
                } else {
                    discard();
                }
            }
        }
        setDeltaMovement(motion.x, motion.y - 0.03, motion.z);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setMana(tag.getInt("mana"));
        setColor(tag.getInt("color"));
        ticksExisted = tag.getInt("ticksExisted");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("mana", getMana());
        tag.putInt("color", getColor());
        tag.putInt("ticksExisted", ticksExisted);
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
