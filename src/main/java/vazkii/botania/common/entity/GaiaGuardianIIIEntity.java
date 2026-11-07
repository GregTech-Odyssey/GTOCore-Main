package vazkii.botania.common.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.ExtraBotanyItems;
import vazkii.botania.common.item.relic.PandorasBoxItem;

import java.util.UUID;

/**
 * 盖亚守护者 III：额外植物学的高难形态，血量与护甲更高，会周期布设地雷并缴械玩家，
 * 死亡按参与战斗的玩家逐人掉落潘多拉魔盒。
 */
public class GaiaGuardianIIIEntity extends GaiaGuardianEntity {

    public static final float MAX_HP_III = 600F;

    public GaiaGuardianIIIEntity(EntityType<? extends GaiaGuardianIIIEntity> type, Level level) {
        super(type, level);
        this.xpReward = 1000;
    }

    public static AttributeSupplier.Builder createGaiaAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HP_III)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 30.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public static boolean spawn(net.minecraft.world.entity.player.Player player, ItemStack stack,
                                Level level, net.minecraft.core.BlockPos pos) {
        boolean spawned = GaiaGuardianEntity.spawn(player, stack, level, pos, false, BotaniaEntities.GAIA_III.get());
        if (spawned) {
            level.playSound(null, pos, vazkii.botania.common.handler.ExtraBotanySounds.MUSIC_GAIA3.get(),
                    net.minecraft.sounds.SoundSource.RECORDS, 1F, 1F);
        }
        return spawned;
    }

    @Override
    public float getMaxHp() {
        return MAX_HP_III;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || getInvulTime() > 0 || !isAlive()) {
            return;
        }
        if (tickCount % 120 == 0) {
            spawnLandmine();
        }
        if (tickCount % 40 == 0) {
            disarmPlayers();
        }
        if (tickCount % 60 == 0) {
            spawnMissiles();
        }
        if (tickCount % 200 == 0) {
            spawnPixies();
        }
        if (tickCount % 100 == 0) {
            smashBlocks();
        }
        if (tickCount % 20 == 0) {
            cleanEffects();
        }
    }

    private void spawnLandmine() {
        var source = getSource();
        for (int i = 0; i < 2; i++) {
            int roll = level().random.nextInt(100);
            SkullLandMineEntity mine;
            if (roll < 70) {
                mine = new SkullLandMineEntity.Default(level(), this);
            } else if (roll < 90) {
                mine = new SkullLandMineEntity.Danger(level(), this);
            } else {
                mine = new SkullLandMineEntity.Disarm(level(), this);
            }
            double x = source.getX() - 10 + level().random.nextInt(20);
            double y = getY();
            double z = source.getZ() - 10 + level().random.nextInt(20);
            mine.moveTo(x + 0.5, y, z + 0.5);
            mine.summoner = this;
            level().addFreshEntity(mine);
        }
    }

    /** 向每位附近玩家发射一枚追踪魔法弹。 */
    private void spawnMissiles() {
        for (Player player : getPlayersAround()) {
            SkullMissileEntity missile = new SkullMissileEntity(level(), this);
            missile.setPos(getX(), getY() + 2.4, getZ());
            if (missile.findTarget()) {
                level().addFreshEntity(missile);
            }
        }
    }

    /** 在附近生成 1 到 3 只暗精灵。 */
    private void spawnPixies() {
        for (Player player : getPlayersAround()) {
            int count = 1 + level().random.nextInt(3);
            for (int i = 0; i < count; i++) {
                PixieEntity pixie = new PixieEntity(level());
                pixie.setProps(player, this, 1, 8F);
                pixie.moveTo(getX() + (level().random.nextDouble() - 0.5) * 6, getY() + 1,
                        getZ() + (level().random.nextDouble() - 0.5) * 6, 0, 0);
                level().addFreshEntity(pixie);
            }
        }
    }

    /** 随机破坏周围方块，跳过活木、梦木与信标。 */
    private void smashBlocks() {
        for (int i = 0; i < 4; i++) {
            net.minecraft.core.BlockPos pos = blockPosition().offset(level().random.nextInt(11) - 5,
                    level().random.nextInt(5) - 2, level().random.nextInt(11) - 5);
            var state = level().getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level(), pos) < 0) {
                continue;
            }
            if (state.is(vazkii.botania.common.BotaniaBlocks.LIVINGWOOD_LOG.get()) || state.is(vazkii.botania.common.BotaniaBlocks.DREAMWOOD_LOG.get()) || state.is(net.minecraft.world.level.block.Blocks.BEACON)) {
                continue;
            }
            level().destroyBlock(pos, false);
        }
    }

    /** 清掉玩家身上短暂的环境增益效果。 */
    private void cleanEffects() {
        for (Player player : getPlayersAround()) {
            player.getActiveEffects().stream()
                    .filter(effect -> effect.getDuration() < 160)
                    .filter(net.minecraft.world.effect.MobEffectInstance::isAmbient)
                    .filter(effect -> effect.getEffect().getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL)
                    .map(net.minecraft.world.effect.MobEffectInstance::getEffect)
                    .toList()
                    .forEach(player::removeEffect);
        }
    }

    private void disarmPlayers() {
        for (Player player : getPlayersAround()) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty()) {
                continue;
            }
            ItemStack drop = held.copy();
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.drop(drop, true);
            player.hurt(damageSources().mobAttack(this), 4F);
        }
    }

    @Override
    public void die(@NotNull DamageSource source) {
        super.die(source);
        if (level().isClientSide) {
            return;
        }
        // 与原额外植物学一致：每位参与战斗的玩家各得一个潘多拉魔盒，并有 20% 概率获得破损的唱片
        for (UUID uuid : getPlayersWhoAttacked()) {
            if (!(level().getPlayerByUUID(uuid) instanceof ServerPlayer player) || player instanceof FakePlayer) {
                continue;
            }
            ItemStack box = new ItemStack(ExtraBotanyItems.PANDORAS_BOX.get());
            PandorasBoxItem.bindToUUID(box, uuid);
            ItemEntity boxEntity = player.spawnAtLocation(box);
            if (boxEntity != null) {
                boxEntity.setNoPickUpDelay();
            }
            if (level().random.nextFloat() < 0.2F) {
                ItemEntity discEntity = player.spawnAtLocation(new ItemStack(ExtraBotanyItems.MUSIC_DISC_GAIA3.get()));
                if (discEntity != null) {
                    discEntity.setNoPickUpDelay();
                }
            }
        }
    }

    @Override
    protected void dropDeathLoot() {
        // 原额外植物学 III 只掉落潘多拉魔盒与唱片，不掉落盖亚之魂
    }
}
