package vazkii.botania.common.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 头颅地雷：盖亚守护者 III 的三种变体，蓝雷高伤、红雷更痛、绿雷缴械。
 */
public abstract class SkullLandMineEntity extends MagicLandmineEntity {

    public static final float LANDMINE_WIDTH = 5F;
    private static final ResourceLocation DEFAULT_SKULL = new ResourceLocation("extrabotany",
            "textures/entity/skull_missile/skull_missile.png");

    public SkullLandMineEntity(EntityType<? extends SkullLandMineEntity> type, Level level) {
        super(type, level);
    }

    public abstract ResourceLocation getTexture();

    @Override
    protected float getLandmineRadius() {
        return LANDMINE_WIDTH / 2;
    }

    @Override
    protected List<Player> getVictimPlayers() {
        return level().getEntitiesOfClass(Player.class,
                new net.minecraft.world.phys.AABB(getX(), getY(), getZ(), getX(), getY(), getZ())
                        .inflate(LANDMINE_WIDTH / 2));
    }

    protected static ResourceLocation defaultTexture() {
        return DEFAULT_SKULL;
    }

    public static class Default extends SkullLandMineEntity {

        private static final ResourceLocation SKULL = new ResourceLocation("extrabotany",
                "textures/entity/skull_landmine/blue.png");

        public Default(EntityType<? extends Default> type, Level level) {
            super(type, level);
        }

        public Default(Level level, net.minecraft.world.entity.Entity owner) {
            super(BotaniaEntities.SKULL_LANDMINE_BLUE.get(), level);
            if (owner instanceof GaiaGuardianEntity gaia) {
                this.summoner = gaia;
            }
        }

        @Override
        public ResourceLocation getTexture() {
            return SKULL;
        }

        @Override
        protected float getParticleRed() {
            return 0.2F;
        }

        @Override
        protected float getParticleBlue() {
            return 0.6F;
        }
    }

    public static class Danger extends SkullLandMineEntity {

        private static final ResourceLocation SKULL = new ResourceLocation("extrabotany",
                "textures/entity/skull_landmine/red.png");

        public Danger(EntityType<? extends Danger> type, Level level) {
            super(type, level);
        }

        public Danger(Level level, net.minecraft.world.entity.Entity owner) {
            super(BotaniaEntities.SKULL_LANDMINE_RED.get(), level);
            if (owner instanceof GaiaGuardianEntity gaia) {
                this.summoner = gaia;
            }
        }

        @Override
        public ResourceLocation getTexture() {
            return SKULL;
        }

        @Override
        protected float getParticleRed() {
            return 0.6F;
        }

        @Override
        protected float getParticleGreen() {
            return 0F;
        }

        @Override
        protected float getParticleBlue() {
            return 0.2F;
        }
    }

    public static class Disarm extends SkullLandMineEntity {

        private static final ResourceLocation SKULL = new ResourceLocation("extrabotany",
                "textures/entity/skull_landmine/green.png");

        public Disarm(EntityType<? extends Disarm> type, Level level) {
            super(type, level);
        }

        public Disarm(Level level, net.minecraft.world.entity.Entity owner) {
            super(BotaniaEntities.SKULL_LANDMINE_GREEN.get(), level);
            if (owner instanceof GaiaGuardianEntity gaia) {
                this.summoner = gaia;
            }
        }

        @Override
        public ResourceLocation getTexture() {
            return SKULL;
        }

        @Override
        protected float getParticleRed() {
            return 0.2F;
        }

        @Override
        protected float getParticleGreen() {
            return 0.6F;
        }

        @Override
        protected void explode() {
            super.explode();
            if (level().isClientSide) {
                return;
            }
            for (Player player : getVictimPlayers()) {
                if (player.isSpectator() || player.isCreative()) {
                    continue;
                }
                ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.drop(stack, false, true);
            }
        }
    }
}
