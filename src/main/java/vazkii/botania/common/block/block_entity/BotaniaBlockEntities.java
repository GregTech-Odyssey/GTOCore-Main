package vazkii.botania.common.block.block_entity;

import com.gto.registrate.util.entry.BlockEntityEntry;

import vazkii.botania.common.BotaniaBlocks;
import vazkii.botania.common.MythicBotanyBlocks;
import vazkii.botania.common.block.block_entity.mana.LargeManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPumpBlockEntity;

import static vazkii.botania.common.BotaniaRegistration.BOTANIA;

/**
 * 复刻内容的方块实体注册表。
 *
 * <p>
 * 所有魔力池变体共用一个方块实体类型，容量差异由各自方块的 {@code ManaPoolSpec} 决定；
 * 三种水晶共用一套方块实体与渲染器。
 * </p>
 */
public final class BotaniaBlockEntities {

    private BotaniaBlockEntities() {}

    public static void init() {}

    public static final BlockEntityEntry<ManaPoolBlockEntity> MANA_POOL = BOTANIA
            .<ManaPoolBlockEntity>blockEntity("mana_pool", ManaPoolBlockEntity::new)
            .validBlocks(
                    BotaniaBlocks.MANA_POOL,
                    BotaniaBlocks.CREATIVE_POOL,
                    BotaniaBlocks.DILUTED_POOL,
                    BotaniaBlocks.FABULOUS_POOL,
                    BotaniaBlocks.MANA_POOL)
            .register();

    public static final BlockEntityEntry<LargeManaPoolBlockEntity> LARGE_MANA_POOL = BOTANIA
            .<LargeManaPoolBlockEntity>blockEntity("large_mana_pool", LargeManaPoolBlockEntity::new)
            .validBlocks(BotaniaBlocks.LARGE_MANA_POOL)
            .register();

    public static final BlockEntityEntry<vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity> CORPOREA_INDEX = BOTANIA.<vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity>blockEntity("corporea_index", vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity::new)
            .validBlocks(BotaniaBlocks.CORPOREA_INDEX)
            .register();

    public static final BlockEntityEntry<PetalApothecaryBlockEntity> PETAL_APOTHECARY = BOTANIA
            .<PetalApothecaryBlockEntity>blockEntity("petal_apothecary", PetalApothecaryBlockEntity::new)
            .validBlocks(
                    BotaniaBlocks.APOTHECARY_DEFAULT,
                    BotaniaBlocks.APOTHECARY_FOREST,
                    BotaniaBlocks.APOTHECARY_PLAINS,
                    BotaniaBlocks.APOTHECARY_MOUNTAIN,
                    BotaniaBlocks.APOTHECARY_FUNGAL,
                    BotaniaBlocks.APOTHECARY_SWAMP,
                    BotaniaBlocks.APOTHECARY_DESERT,
                    BotaniaBlocks.APOTHECARY_TAIGA,
                    BotaniaBlocks.APOTHECARY_MESA,
                    BotaniaBlocks.APOTHECARY_MOSSY,
                    BotaniaBlocks.APOTHECARY_LIVINGROCK,
                    BotaniaBlocks.APOTHECARY_DEEPSLATE)
            .register();

    public static final BlockEntityEntry<vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity> MANA_SPREADER = BOTANIA.<vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity>blockEntity("mana_spreader", vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity::new)
            .validBlocks(BotaniaBlocks.MANA_SPREADER, BotaniaBlocks.REDSTONE_SPREADER, BotaniaBlocks.ELVEN_SPREADER, BotaniaBlocks.GAIA_SPREADER)
            .register();

    public static final BlockEntityEntry<AlfheimPortalBlockEntity> ALF_PORTAL = BOTANIA
            .<AlfheimPortalBlockEntity>blockEntity("alf_portal", AlfheimPortalBlockEntity::new)
            .validBlocks(BotaniaBlocks.ALFHEIM_PORTAL)
            .register();

    public static final BlockEntityEntry<vazkii.botania.forge.integration.ae2.FluixManaPoolBlockEntity> FLUIX_MANA_POOL = BOTANIA.<vazkii.botania.forge.integration.ae2.FluixManaPoolBlockEntity>blockEntity("fluix_mana_pool", vazkii.botania.forge.integration.ae2.FluixManaPoolBlockEntity::new)
            .validBlocks(BotaniaBlocks.FLUIX_MANA_POOL)
            .register();

    public static final BlockEntityEntry<ManaPumpBlockEntity> PUMP = BOTANIA
            .<ManaPumpBlockEntity>blockEntity("pump", ManaPumpBlockEntity::new)
            .validBlocks(BotaniaBlocks.PUMP)
            .register();

    public static final BlockEntityEntry<TileReturnPortal> RETURN_PORTAL = BOTANIA
            .<TileReturnPortal>blockEntity("return_portal", TileReturnPortal::new)
            .validBlocks(MythicBotanyBlocks.MB_RETURN_PORTAL)
            .register();

    public static final BlockEntityEntry<RunicAltarBlockEntity> RUNIC_ALTAR = BOTANIA
            .<RunicAltarBlockEntity>blockEntity("runic_altar", RunicAltarBlockEntity::new)
            .validBlocks(BotaniaBlocks.RUNIC_ALTAR)
            .register();

    public static final BlockEntityEntry<PylonBlockEntity> PYLON = BOTANIA
            .<PylonBlockEntity>blockEntity("pylon", PylonBlockEntity::new)
            .validBlocks(
                    BotaniaBlocks.MANA_PYLON,
                    BotaniaBlocks.NATURA_PYLON,
                    BotaniaBlocks.GAIA_PYLON,
                    MythicBotanyBlocks.MB_ALFSTEEL_PYLON)
            .register();
}
