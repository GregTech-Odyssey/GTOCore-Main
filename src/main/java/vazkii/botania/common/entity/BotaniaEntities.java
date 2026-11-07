package vazkii.botania.common.entity;

import net.minecraft.world.entity.MobCategory;

import com.gto.registrate.util.entry.EntityEntry;

import vazkii.botania.client.render.entity.GaiaGuardianRenderer;

import static vazkii.botania.common.BotaniaRegistration.BOTANIA;

/**
 * 复刻内容的生物实体注册表。
 */
public final class BotaniaEntities {

    private BotaniaEntities() {}

    public static void init() {}

    public static final EntityEntry<PixieEntity> PIXIE = BOTANIA
            .<PixieEntity>entity("pixie", PixieEntity::new, MobCategory.MISC)
            .renderer(() -> vazkii.botania.client.render.entity.PixieRenderer::new)
            .register();

    public static final EntityEntry<EnderAirEntity> ENDER_AIR = BOTANIA
            .<EnderAirEntity>entity("ender_air", EnderAirEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<MagicLandmineEntity> MAGIC_LANDMINE = BOTANIA
            .<MagicLandmineEntity>entity("magic_landmine", MagicLandmineEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<FallingStarEntity> FALLING_STAR = BOTANIA
            .<FallingStarEntity>entity("falling_star", FallingStarEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<FlameRingEntity> FLAME_RING = BOTANIA
            .<FlameRingEntity>entity("flame_ring", FlameRingEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<ThrownItemEntity> THROWN_ITEM = BOTANIA
            .<ThrownItemEntity>entity("thrown_item", ThrownItemEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<VineBallEntity> VINE_BALL = BOTANIA
            .<VineBallEntity>entity("vine_ball", VineBallEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<ThornChakramEntity> THORN_CHAKRAM = BOTANIA
            .<ThornChakramEntity>entity("thorn_chakram", ThornChakramEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<MagicMissileEntity> MAGIC_MISSILE = BOTANIA
            .<MagicMissileEntity>entity("magic_missile", MagicMissileEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<EnderAirBottleEntity> ENDER_AIR_BOTTLE = BOTANIA
            .<EnderAirBottleEntity>entity("ender_air_bottle", EnderAirBottleEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<ManaPoolMinecartEntity> POOL_MINECART = BOTANIA
            .<ManaPoolMinecartEntity>entity("pool_minecart", ManaPoolMinecartEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<ManaStormEntity> MANA_STORM = BOTANIA
            .<ManaStormEntity>entity("mana_storm", ManaStormEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<PinkWitherEntity> PINK_WITHER = BOTANIA
            .<PinkWitherEntity>entity("pink_wither", PinkWitherEntity::new, MobCategory.MONSTER)
            .register();

    public static final EntityEntry<BabylonWeaponEntity> BABYLON_WEAPON = BOTANIA
            .<BabylonWeaponEntity>entity("babylon_weapon", BabylonWeaponEntity::new, MobCategory.MISC)
            .register();

    public static final EntityEntry<AlfPixie> ALF_PIXIE = BOTANIA
            .<AlfPixie>entity("alf_pixie", AlfPixie::new, MobCategory.CREATURE)
            .renderer(() -> vazkii.botania.client.render.entity.AlfPixieRender::new)
            .register();

    public static final EntityEntry<ManaSparkEntity> MANA_SPARK = BOTANIA
            .<ManaSparkEntity>entity("mana_spark", ManaSparkEntity::new, MobCategory.MISC)
            .renderer(() -> vazkii.botania.client.render.entity.ManaSparkRenderer::new)
            .register();

    public static final EntityEntry<ManaBurstEntity> MANA_BURST = BOTANIA
            .<ManaBurstEntity>entity("mana_burst", ManaBurstEntity::new, MobCategory.MISC)
            .renderer(() -> vazkii.botania.client.render.entity.ManaBurstRenderer::new)
            .register();

    public static final EntityEntry<SkullLandMineEntity.Default> SKULL_LANDMINE_BLUE = BOTANIA.<SkullLandMineEntity.Default>entity("skull_landmine_blue", SkullLandMineEntity.Default::new, MobCategory.MISC)
            .renderer(() -> vazkii.botania.client.render.entity.SkullLandMineRenderer::new)
            .register();

    public static final EntityEntry<SkullLandMineEntity.Danger> SKULL_LANDMINE_RED = BOTANIA.<SkullLandMineEntity.Danger>entity("skull_landmine_red", SkullLandMineEntity.Danger::new, MobCategory.MISC)
            .renderer(() -> vazkii.botania.client.render.entity.SkullLandMineRenderer::new)
            .register();

    public static final EntityEntry<SkullLandMineEntity.Disarm> SKULL_LANDMINE_GREEN = BOTANIA.<SkullLandMineEntity.Disarm>entity("skull_landmine_green", SkullLandMineEntity.Disarm::new, MobCategory.MISC)
            .renderer(() -> vazkii.botania.client.render.entity.SkullLandMineRenderer::new)
            .register();

    public static final EntityEntry<SkullMissileEntity> SKULL_MISSILE = BOTANIA
            .<SkullMissileEntity>entity("skull_missile", SkullMissileEntity::new, MobCategory.MISC)
            .renderer(() -> vazkii.botania.client.render.entity.SkullMissileRenderer::new)
            .register();

    public static final EntityEntry<GaiaGuardianIIIEntity> GAIA_III = BOTANIA
            .<GaiaGuardianIIIEntity>entity("gaia_3rd", GaiaGuardianIIIEntity::new, MobCategory.MONSTER)
            .attributes(GaiaGuardianIIIEntity::createGaiaAttributes)
            .renderer(() -> vazkii.botania.client.render.entity.GaiaGuardianIIIRenderer::new)
            .register();

    public static final EntityEntry<GaiaGuardianEntity> DOPPLEGANGER = BOTANIA
            .entity("doppleganger", GaiaGuardianEntity::new, MobCategory.MONSTER)
            .attributes(GaiaGuardianEntity::createAttributes)
            .renderer(() -> GaiaGuardianRenderer::new)
            .register();
}
