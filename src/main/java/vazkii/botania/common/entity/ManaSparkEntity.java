package vazkii.botania.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaEndpoint;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 火花实体：自动附着到下方的魔力池，并把魔力搬运到附近另一个池。
 */
public class ManaSparkEntity extends SparkBaseEntity implements ManaSpark {

    private static final int TRANSFER_INTERVAL = 5;
    private static final long TRANSFER_RATE = 100;

    private SparkUpgradeType upgrade = SparkUpgradeType.NONE;
    private final List<ManaSpark> transfers = new ArrayList<>();
    private SparkAttachable attached;
    private int airTicks;

    public ManaSparkEntity(EntityType<ManaSparkEntity> type, Level level) {
        super(type, level);
    }

    public ManaSparkEntity(Level level) {
        this(BotaniaEntities.MANA_SPARK.get(), level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (attached == null) {
            BlockPos pos = getAttachPos().below();
            if (level().getBlockEntity(pos) instanceof SparkAttachable attachable && attachable.canAttachSpark(ItemStack.EMPTY)) {
                attachable.attachSpark(this);
                attached = attachable;
            }
            if (attached == null && ++airTicks > 40) {
                discard();
                return;
            }
        }
        if (attached != null && tickCount % TRANSFER_INTERVAL == 0) {
            transferMana();
        }
    }

    private void transferMana() {
        if (!(attached instanceof ManaPoolBlockEntity pool) || pool.getCurrentManaLong() <= 0) {
            return;
        }
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -4, -8), origin.offset(8, 4, 8))) {
            if (pos.equals(origin)) {
                continue;
            }
            if (level().getBlockEntity(pos) instanceof ManaPoolBlockEntity other && other != pool) {
                ManaEndpoint.transfer(pool, other, TRANSFER_RATE);
                return;
            }
        }
    }

    /** 玩家持杖时沿火花连线生成光点。 */
    public static void particleBeam(net.minecraft.world.entity.player.Player player,
                                    net.minecraft.world.entity.Entity source, net.minecraft.world.entity.Entity target) {
        if (player == null || source == null || target == null || !player.level().isClientSide) {
            return;
        }
        net.minecraft.world.phys.Vec3 a = source.getBoundingBox().getCenter();
        net.minecraft.world.phys.Vec3 b = target.getBoundingBox().getCenter();
        int steps = 20;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            player.level().addParticle(
                    vazkii.botania.client.fx.SparkleParticleData.sparkle(1F, 0.2F, 1F, 0.2F, 3),
                    a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t, 0, 0, 0);
        }
    }

    @Override
    public SparkAttachable getAttachedTile() {
        return attached;
    }

    @Override
    public ManaReceiver getAttachedManaReceiver() {
        return attached instanceof ManaReceiver receiver ? receiver : null;
    }

    @Override
    public Collection<ManaSpark> getOutgoingTransfers() {
        return transfers;
    }

    @Override
    public void registerTransfer(ManaSpark entity) {
        transfers.add(entity);
    }

    @Override
    public void updateTransfers() {
        transfers.clear();
    }

    @Override
    public SparkUpgradeType getUpgrade() {
        return upgrade;
    }

    @Override
    public void setUpgrade(SparkUpgradeType upgrade) {
        this.upgrade = upgrade;
    }

    @Override
    public boolean areIncomingTransfersDone() {
        return true;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (attached != null) {
            attached = null;
        }
        super.remove(reason);
    }
}
