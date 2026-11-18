package com.gtocore.integration.emi.jer;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;

import java.util.Locale;

// 改写自 Just Enough Resources（way2muchnoise，"Don't Be a Jerk" 非商业许可）
// https://github.com/way2muchnoise/JustEnoughResources
public final class JerEmi implements EmiPlugin {

    static final Category MOB = new Category(new ResourceLocation("jeresources", "mob"), EmiStack.of(Items.ZOMBIE_HEAD), Lang.MOB_TITLE);
    static final Category DUNGEON = new Category(new ResourceLocation("jeresources", "dungeon"), EmiStack.of(Items.CHEST), Lang.DUNGEON_TITLE);
    static final Category PLANT = new Category(new ResourceLocation("jeresources", "plant"), EmiStack.of(Items.WHEAT), Lang.PLANT_TITLE);

    @Override
    public void register(EmiRegistry registry) {
        JerLootSync.Dataset data = JerLootSync.dataset();
        if (data != null) {
            registry.addCategory(MOB);
            registry.addCategory(DUNGEON);
        }
        registry.addCategory(PLANT);
        if (data != null) {
            JerMobRecipe.create(data).forEach(registry::addRecipe);
            JerChestRecipe.create(data).forEach(registry::addRecipe);
        }
        JerPlantRecipe.create().forEach(registry::addRecipe);
    }

    static SlotWidget decorate(SlotWidget slot, JerLoot.Drop drop) {
        String amount = drop.min() == drop.max() ? Integer.toString(drop.min()) : drop.min() + "-" + drop.max();
        if (drop.chance() < 1F) {
            float percent = drop.chance() * 100;
            amount += " (" + (percent < 10 ? String.format(Locale.ROOT, "%.1f", percent) : Integer.toString((int) percent)) + "%)";
        }
        slot.appendTooltip(Component.literal(amount));
        if (drop.playerKill()) slot.appendTooltip(Component.translatable(Lang.PLAYER_KILL).withStyle(ChatFormatting.GRAY));
        if (drop.looting()) slot.appendTooltip(Component.translatable(Lang.LOOTING).withStyle(ChatFormatting.AQUA));
        return slot;
    }

    public static final class Client {

        private Client() {}

        public static void init() {
            MinecraftForge.EVENT_BUS.addListener(Client::onLogout);
        }

        private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            JerLootSync.clear();
        }
    }

    static final class Category extends EmiRecipeCategory {

        private final String nameKey;

        Category(ResourceLocation id, EmiStack icon, String nameKey) {
            super(id, icon);
            this.nameKey = nameKey;
        }

        @Override
        public Component getName() {
            return Component.translatable(nameKey);
        }
    }

    @DataGeneratorScanned
    public static final class Lang {

        @RegisterLanguage(cn = "生物掉落", en = "Mob Drops")
        static final String MOB_TITLE = "gtocore.jer.mob.title";
        @RegisterLanguage(cn = "箱子战利品", en = "Chest Loot")
        static final String DUNGEON_TITLE = "gtocore.jer.dungeon.title";
        @RegisterLanguage(cn = "作物掉落", en = "Crop Drops")
        static final String PLANT_TITLE = "gtocore.jer.plant.title";
        @RegisterLanguage(cn = "%s（%s）", en = "%s (%s)")
        static final String SHEEP_NAME = "gtocore.jer.sheep_name";
        @RegisterLanguage(cn = "生成群系：%s", en = "Biome: %s")
        static final String BIOME = "gtocore.jer.biome";
        @RegisterLanguage(cn = "光照：%s", en = "Light: %s")
        static final String LIGHT = "gtocore.jer.light";
        @RegisterLanguage(cn = "低于 %s", en = "Below %s")
        static final String LIGHT_BELOW = "gtocore.jer.light_below";
        @RegisterLanguage(cn = "任意", en = "Any")
        static final String ANY = "gtocore.jer.any";
        @RegisterLanguage(cn = "经验：%s", en = "Experience: %s")
        static final String EXP = "gtocore.jer.exp";
        @RegisterLanguage(cn = "%s 组", en = "%s Stacks")
        static final String STACKS = "gtocore.jer.stacks";
        @RegisterLanguage(cn = "需玩家击杀", en = "Killed by player")
        static final String PLAYER_KILL = "gtocore.jer.player_kill";
        @RegisterLanguage(cn = "受抢夺影响", en = "Affected by Looting")
        static final String LOOTING = "gtocore.jer.looting";

        @RegisterLanguage(cn = "废弃矿井", en = "Mineshaft")
        static final String CHEST_ABANDONED_MINESHAFT = "gtocore.jer.chest.abandoned_mineshaft";
        @RegisterLanguage(cn = "沙漠神殿", en = "Desert Temple")
        static final String CHEST_DESERT_PYRAMID = "gtocore.jer.chest.desert_pyramid";
        @RegisterLanguage(cn = "丛林神庙", en = "Jungle Temple")
        static final String CHEST_JUNGLE_TEMPLE = "gtocore.jer.chest.jungle_temple";
        @RegisterLanguage(cn = "雪屋", en = "Igloo")
        static final String CHEST_IGLOO = "gtocore.jer.chest.igloo_chest";
        @RegisterLanguage(cn = "要塞走廊", en = "Stronghold Corridor")
        static final String CHEST_STRONGHOLD_CORRIDOR = "gtocore.jer.chest.stronghold_corridor";
        @RegisterLanguage(cn = "要塞图书馆", en = "Stronghold Library")
        static final String CHEST_STRONGHOLD_LIBRARY = "gtocore.jer.chest.stronghold_library";
        @RegisterLanguage(cn = "要塞交叉口", en = "Stronghold Crossing")
        static final String CHEST_STRONGHOLD_CROSSING = "gtocore.jer.chest.stronghold_crossing";
        @RegisterLanguage(cn = "铁匠铺", en = "Blacksmith")
        static final String CHEST_VILLAGE_BLACKSMITH = "gtocore.jer.chest.village_blacksmith";
        @RegisterLanguage(cn = "奖励箱", en = "Bonus Chest")
        static final String CHEST_SPAWN_BONUS = "gtocore.jer.chest.spawn_bonus_chest";
        @RegisterLanguage(cn = "地牢", en = "Dungeon")
        static final String CHEST_SIMPLE_DUNGEON = "gtocore.jer.chest.simple_dungeon";
        @RegisterLanguage(cn = "下界要塞", en = "Nether Fortress")
        static final String CHEST_NETHER_BRIDGE = "gtocore.jer.chest.nether_bridge";
        @RegisterLanguage(cn = "末地城", en = "End City")
        static final String CHEST_END_CITY = "gtocore.jer.chest.end_city_treasure";
        @RegisterLanguage(cn = "林地府邸", en = "Woodland Mansion")
        static final String CHEST_WOODLAND_MANSION = "gtocore.jer.chest.woodland_mansion";
        @RegisterLanguage(cn = "村庄武器匠", en = "Village Weaponsmith")
        static final String CHEST_VILLAGE_WEAPONSMITH = "gtocore.jer.chest.village_weaponsmith";
        @RegisterLanguage(cn = "村庄工具匠", en = "Village Toolsmith")
        static final String CHEST_VILLAGE_TOOLSMITH = "gtocore.jer.chest.village_toolsmith";
        @RegisterLanguage(cn = "村庄盔甲匠", en = "Village Armorer")
        static final String CHEST_VILLAGE_ARMORER = "gtocore.jer.chest.village_armorer";
        @RegisterLanguage(cn = "村庄制图师", en = "Village Cartographer")
        static final String CHEST_VILLAGE_CARTOGRAPHER = "gtocore.jer.chest.village_cartographer";
        @RegisterLanguage(cn = "村庄石匠", en = "Village Mason")
        static final String CHEST_VILLAGE_MASON = "gtocore.jer.chest.village_mason";
        @RegisterLanguage(cn = "村庄牧羊人", en = "Village Shepherd")
        static final String CHEST_VILLAGE_SHEPHERD = "gtocore.jer.chest.village_shepherd";
        @RegisterLanguage(cn = "村庄屠夫", en = "Village Butcher")
        static final String CHEST_VILLAGE_BUTCHER = "gtocore.jer.chest.village_butcher";
        @RegisterLanguage(cn = "村庄制箭师", en = "Village Fletcher")
        static final String CHEST_VILLAGE_FLETCHER = "gtocore.jer.chest.village_fletcher";
        @RegisterLanguage(cn = "村庄渔夫", en = "Village Fisher")
        static final String CHEST_VILLAGE_FISHER = "gtocore.jer.chest.village_fisher";
        @RegisterLanguage(cn = "村庄皮匠", en = "Village Tannery")
        static final String CHEST_VILLAGE_TANNERY = "gtocore.jer.chest.village_tannery";
        @RegisterLanguage(cn = "村庄神庙", en = "Village Temple")
        static final String CHEST_VILLAGE_TEMPLE = "gtocore.jer.chest.village_temple";
        @RegisterLanguage(cn = "村庄沙漠房屋", en = "Village Desert House")
        static final String CHEST_VILLAGE_DESERT_HOUSE = "gtocore.jer.chest.village_desert_house";
        @RegisterLanguage(cn = "村庄平原房屋", en = "Village Plains House")
        static final String CHEST_VILLAGE_PLAINS_HOUSE = "gtocore.jer.chest.village_plains_house";
        @RegisterLanguage(cn = "村庄针叶林房屋", en = "Village Taiga House")
        static final String CHEST_VILLAGE_TAIGA_HOUSE = "gtocore.jer.chest.village_taiga_house";
        @RegisterLanguage(cn = "村庄雪原房屋", en = "Village Snowy House")
        static final String CHEST_VILLAGE_SNOWY_HOUSE = "gtocore.jer.chest.village_snowy_house";
        @RegisterLanguage(cn = "村庄热带草原房屋", en = "Village Savanna House")
        static final String CHEST_VILLAGE_SAVANNA_HOUSE = "gtocore.jer.chest.village_savanna_house";
        @RegisterLanguage(cn = "小型海底废墟", en = "Small Underwater Ruin")
        static final String CHEST_UNDERWATER_RUIN_SMALL = "gtocore.jer.chest.underwater_ruin_small";
        @RegisterLanguage(cn = "大型海底废墟", en = "Big Underwater Ruin")
        static final String CHEST_UNDERWATER_RUIN_BIG = "gtocore.jer.chest.underwater_ruin_big";
        @RegisterLanguage(cn = "埋藏的宝藏", en = "Buried Treasure")
        static final String CHEST_BURIED_TREASURE = "gtocore.jer.chest.buried_treasure";
        @RegisterLanguage(cn = "沉船地图箱", en = "Shipwreck Map")
        static final String CHEST_SHIPWRECK_MAP = "gtocore.jer.chest.shipwreck_map";
        @RegisterLanguage(cn = "沉船补给箱", en = "Shipwreck Supply")
        static final String CHEST_SHIPWRECK_SUPPLY = "gtocore.jer.chest.shipwreck_supply";
        @RegisterLanguage(cn = "沉船宝箱", en = "Shipwreck Treasure")
        static final String CHEST_SHIPWRECK_TREASURE = "gtocore.jer.chest.shipwreck_treasure";
        @RegisterLanguage(cn = "掠夺者前哨站", en = "Pillager Outpost")
        static final String CHEST_PILLAGER_OUTPOST = "gtocore.jer.chest.pillager_outpost";
        @RegisterLanguage(cn = "堡垒遗迹宝藏室", en = "Bastion Treasure")
        static final String CHEST_BASTION_TREASURE = "gtocore.jer.chest.bastion_treasure";
        @RegisterLanguage(cn = "堡垒遗迹", en = "Bastion Other")
        static final String CHEST_BASTION_OTHER = "gtocore.jer.chest.bastion_other";
        @RegisterLanguage(cn = "堡垒遗迹桥", en = "Bastion Bridge")
        static final String CHEST_BASTION_BRIDGE = "gtocore.jer.chest.bastion_bridge";
        @RegisterLanguage(cn = "堡垒遗迹疣猪兽厩", en = "Bastion Hoglin Stable")
        static final String CHEST_BASTION_HOGLIN_STABLE = "gtocore.jer.chest.bastion_hoglin_stable";
        @RegisterLanguage(cn = "废弃传送门", en = "Ruined Portal")
        static final String CHEST_RUINED_PORTAL = "gtocore.jer.chest.ruined_portal";
        @RegisterLanguage(cn = "远古城市", en = "Ancient City")
        static final String CHEST_ANCIENT_CITY = "gtocore.jer.chest.ancient_city";
        @RegisterLanguage(cn = "远古城市冰箱", en = "Ancient City Ice Box")
        static final String CHEST_ANCIENT_CITY_ICE_BOX = "gtocore.jer.chest.ancient_city_ice_box";
    }
}
