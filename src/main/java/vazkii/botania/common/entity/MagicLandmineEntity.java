package vazkii.botania.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.fx.WispParticleData;

import java.util.List;

/** 魔法地雷：倒计时结束后造成伤害与负面效果，子类可改变范围与爆炸行为。 */
public class MagicLandmineEntity extends Entity {

    public GaiaGuardianEntity summoner;

    public MagicLandmineEntity(EntityType<? extends MagicLandmineEntity> type, Level world) {
        super(type, world);
    }

    protected int getFuse() {
        return 55;
    }

    public int getExplodeCountDown() {
        return Math.max(0, getFuse() - tickCount);
    }

    protected float getLandmineRadius() {
        return getBbWidth() / 2;
    }

    protected void particleTick() {
        float range = getLandmineRadius();
        for (int i = 0; i < 6; i++) {
            WispParticleData data = WispParticleData.wisp(0.4F, getParticleRed(), getParticleGreen(), getParticleBlue(), 1F);
            level().addParticle(data, getX() - range + Math.random() * range * 2, getY(),
                    getZ() - range + Math.random() * range * 2, 0, 0.015F, 0);
        }
    }

    protected float getParticleRed() {
        return 0.2F;
    }

    protected float getParticleGreen() {
        return 0F;
    }

    protected float getParticleBlue() {
        return 0.2F;
    }

    protected List<Player> getVictimPlayers() {
        return level().getEntitiesOfClass(Player.class, getBoundingBox());
    }

    protected void explode() {
        level().playSound(null, getX(), getY(), getZ(),
                vazkii.botania.common.handler.BotaniaSounds.gaiaTrap.get(), net.minecraft.sounds.SoundSource.HOSTILE,
                1F, 1F);
        float m = 0.35F;
        for (int i = 0; i < 25; i++) {
            WispParticleData data = WispParticleData.wisp(0.5F, 0.2F, 0.4F, 0.2F);
            level().addParticle(data, getX(), getY() + 1, getZ(), (float) (Math.random() - 0.5F) * m,
                    (float) (Math.random() - 0.5F) * m, (float) (Math.random() - 0.5F) * m);
        }
        if (!level().isClientSide) {
            for (Player player : getVictimPlayers()) {
                player.hurt(damageSources().indirectMagic(this, summoner), 10);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0));
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2));
            }
        }
    }

    @Override
    public void tick() {
        setDeltaMovement(Vec3.ZERO);
        super.tick();
        particleTick();
        if (tickCount >= getFuse()) {
            explode();
            discard();
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
