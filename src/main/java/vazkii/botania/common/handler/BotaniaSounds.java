package vazkii.botania.common.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 植物魔法复刻的音效事件。 */
public final class BotaniaSounds {

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister
            .create(ForgeRegistries.SOUND_EVENTS, "botania");

    private BotaniaSounds() {}

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private static RegistryObject<SoundEvent> make(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("botania", id)));
    }

    public static final RegistryObject<SoundEvent> altarCraft = make("altar_craft");
    public static final RegistryObject<SoundEvent> bellows = make("bellows");
    public static final RegistryObject<SoundEvent> drum = make("drum");
    public static final RegistryObject<SoundEvent> enchanterEnchant = make("enchanter_enchant");
    public static final RegistryObject<SoundEvent> enchanterFade = make("enchanter_fade");
    public static final RegistryObject<SoundEvent> enchanterForm = make("enchanter_form");
    public static final RegistryObject<SoundEvent> hornDoot = make("horn_doot");
    public static final RegistryObject<SoundEvent> incensePlateIgnite = make("incense_plate_ignite");
    public static final RegistryObject<SoundEvent> lightRelay = make("light_relay");
    public static final RegistryObject<SoundEvent> manaPoolCraft = make("mana_pool_craft");
    public static final RegistryObject<SoundEvent> potionCreate = make("potion_create");
    public static final RegistryObject<SoundEvent> redStringInterceptorClick = make("red_string_interceptor_click");
    public static final RegistryObject<SoundEvent> runeAltarCraft = make("rune_altar_craft");
    public static final RegistryObject<SoundEvent> runeAltarStart = make("rune_altar_start");
    public static final RegistryObject<SoundEvent> prismAddLens = make("prism_add_lens");
    public static final RegistryObject<SoundEvent> prismRemoveLens = make("prism_remove_lens");
    public static final RegistryObject<SoundEvent> spreaderAddLens = make("spreader_add_lens");
    public static final RegistryObject<SoundEvent> spreaderRemoveLens = make("spreader_remove_lens");
    public static final RegistryObject<SoundEvent> spreaderCover = make("spreader_cover");
    public static final RegistryObject<SoundEvent> spreaderUncover = make("spreader_uncover");
    public static final RegistryObject<SoundEvent> spreaderScaffold = make("spreader_scaffold");
    public static final RegistryObject<SoundEvent> spreaderUnScaffold = make("spreader_un_scaffold");
    public static final RegistryObject<SoundEvent> spreaderFire = make("spreader_fire");
    public static final RegistryObject<SoundEvent> terrasteelCraft = make("terrasteel_craft");
    public static final RegistryObject<SoundEvent> airRod = make("air_rod");
    public static final RegistryObject<SoundEvent> astrolabeConfigure = make("astrolabe_configure");
    public static final RegistryObject<SoundEvent> bifrostRod = make("bifrost_rod");
    public static final RegistryObject<SoundEvent> blackHoleTalismanConfigure = make("black_hole_talisman_configure");
    public static final RegistryObject<SoundEvent> blackLotus = make("black_lotus");
    public static final RegistryObject<SoundEvent> dash = make("dash");
    public static final RegistryObject<SoundEvent> diceOfFate = make("dice_of_fate");
    public static final RegistryObject<SoundEvent> divaCharm = make("diva_charm");
    public static final RegistryObject<SoundEvent> divinationRod = make("divination_rod");
    public static final RegistryObject<SoundEvent> enderAirThrow = make("ender_air_throw");
    public static final RegistryObject<SoundEvent> equipBauble = make("equip_bauble");
    public static final RegistryObject<SoundEvent> equipElementium = make("equip_elementium");
    public static final RegistryObject<SoundEvent> equipManasteel = make("equip_manasteel");
    public static final RegistryObject<SoundEvent> equipManaweave = make("equip_manaweave");
    public static final RegistryObject<SoundEvent> equipTerrasteel = make("equip_terrasteel");
    public static final RegistryObject<SoundEvent> fireRod = make("fire_rod");
    public static final RegistryObject<SoundEvent> flareChakramThrow = make("flare_chakram_throw");
    public static final RegistryObject<SoundEvent> flugelEyeBind = make("flugel_eye_bind");
    public static final RegistryObject<SoundEvent> flugelEyeTeleport = make("flugel_eye_teleport");
    public static final RegistryObject<SoundEvent> holyCloak = make("holy_cloak");
    public static final RegistryObject<SoundEvent> laputaStart = make("laputa_start");
    public static final RegistryObject<SoundEvent> lexiconOpen = make("lexicon_open");
    public static final RegistryObject<SoundEvent> lexiconPage = make("lexicon_page");
    public static final RegistryObject<SoundEvent> manaBlaster = make("mana_blaster");
    public static final RegistryObject<SoundEvent> manaBlasterCycle = make("mana_blaster_cycle");
    public static final RegistryObject<SoundEvent> manaBlasterMisfire = make("mana_blaster_misfire");
    public static final RegistryObject<SoundEvent> missile = make("missile");
    public static final RegistryObject<SoundEvent> missileFunny = make("missile_funny");
    public static final RegistryObject<SoundEvent> pinkinator = make("pinkinator");
    public static final RegistryObject<SoundEvent> smeltRod = make("smelt_rod");
    public static final RegistryObject<SoundEvent> smeltRod2 = make("smelt_rod_extra_no_subtitle");
    public static final RegistryObject<SoundEvent> smeltRodSimmer = make("smelt_rod_simmer");
    public static final RegistryObject<SoundEvent> starcaller = make("starcaller");
    public static final RegistryObject<SoundEvent> temperanceStoneConfigure = make("temperance_stone_configure");
    public static final RegistryObject<SoundEvent> manufactoryHaloConfigure = make("manufactory_halo_configure");
    public static final RegistryObject<SoundEvent> terraBlade = make("terrablade");
    public static final RegistryObject<SoundEvent> terraPickMode = make("terra_pick_mode");
    public static final RegistryObject<SoundEvent> terraformRod = make("terraform_rod");
    public static final RegistryObject<SoundEvent> thornChakramThrow = make("thorn_chakram_throw");
    public static final RegistryObject<SoundEvent> unholyCloak = make("unholy_cloak");
    public static final RegistryObject<SoundEvent> vineBallThrow = make("vine_ball_throw");
    public static final RegistryObject<SoundEvent> virusInfect = make("virus_infect");
    public static final RegistryObject<SoundEvent> worldSeedTeleport = make("world_seed_teleport");
    public static final RegistryObject<SoundEvent> agricarnation = make("agricarnation");
    public static final RegistryObject<SoundEvent> arcaneRoseDisenchant = make("arcane_rose_disenchant");
    public static final RegistryObject<SoundEvent> endoflame = make("endoflame");
    public static final RegistryObject<SoundEvent> entropinnyumAngry = make("entropinnyum_angry");
    public static final RegistryObject<SoundEvent> entropinnyumHappy = make("entropinnyum_happy");
    public static final RegistryObject<SoundEvent> labellia = make("labellia");
    public static final RegistryObject<SoundEvent> narslimmusEatBig = make("narslimmus_eat_big");
    public static final RegistryObject<SoundEvent> narslimmusEatSmall = make("narslimmus_eat_small");
    public static final RegistryObject<SoundEvent> orechid = make("orechid");
    public static final RegistryObject<SoundEvent> shulkMeNot = make("shulk_me_not");
    public static final RegistryObject<SoundEvent> thermalily = make("thermalily");
    public static final RegistryObject<SoundEvent> tigerseyePacify = make("tigerseye_pacify");
    public static final RegistryObject<SoundEvent> babylonAttack = make("babylon_attack");
    public static final RegistryObject<SoundEvent> babylonSpawn = make("babylon_spawn");
    public static final RegistryObject<SoundEvent> gaiaDeath = make("gaia_death");
    public static final RegistryObject<SoundEvent> gaiaSummon = make("gaia_summon");
    public static final RegistryObject<SoundEvent> gaiaTeleport = make("gaia_teleport");
    public static final RegistryObject<SoundEvent> gaiaTrap = make("gaia_trap");
    public static final RegistryObject<SoundEvent> ding = make("ding");
    public static final RegistryObject<SoundEvent> doit = make("doit");
    public static final RegistryObject<SoundEvent> gaiaMusic1 = make("music.gaia1");
    public static final RegistryObject<SoundEvent> gaiaMusic2 = make("music.gaia2");
    public static final RegistryObject<SoundEvent> way = make("way");
}
