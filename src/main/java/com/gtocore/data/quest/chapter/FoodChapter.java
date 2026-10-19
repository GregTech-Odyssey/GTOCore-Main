package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.world.item.Items;

public final class FoodChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("food", c -> c
            .title("种地与吃饭")
            .subtitle("Farmer's Delight")
            .hideQuestsUntilDepsVisible()
            .autofocus(FoodChapter.GLASS_BOTTLE));

    public static final Quest CRAFTSMANSHIP = CHAPTER.quest("craftsmanship", q -> q
            .at(0, 0)
            .title("匠心手作")
            .desc(d -> d
                    .line("本整合包添加农夫乐事与农夫暇事，生活调味料与生活调味料：胡萝卜版mod，吃的越好长的越壮")
                    .line()
                    .line("不送下界疣 因为要去下界"))
            .task(
                    Task.item(QuestExternalRefs.FarmersDelightItems.COOKING_POT),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.KETTLE),
                    Task.item(Items.SMOKER))
            .reward(
                    Reward.item(Items.WHEAT_SEEDS),
                    Reward.item(Items.PUMPKIN_SEEDS),
                    Reward.item(Items.MELON_SEEDS),
                    Reward.item(Items.BEETROOT_SEEDS),
                    Reward.item(Items.CARROT),
                    Reward.item(Items.POTATO),
                    Reward.item(Items.COCOA_BEANS),
                    Reward.item(QuestExternalRefs.FarmersDelightItems.ONION),
                    Reward.item(QuestExternalRefs.FarmersDelightItems.TOMATO_SEEDS),
                    Reward.item(QuestExternalRefs.FarmersDelightItems.CABBAGE_SEEDS),
                    Reward.item(QuestExternalRefs.FarmersDelightItems.RICE),
                    Reward.item(QuestExternalRefs.FarmersRespiteItems.COFFEE_BEANS),
                    Reward.item(QuestExternalRefs.FarmersRespiteItems.GREEN_TEA_LEAVES),
                    Reward.item(QuestExternalRefs.FarmersRespiteItems.YELLOW_TEA_LEAVES),
                    Reward.item(QuestExternalRefs.FarmersRespiteItems.BLACK_TEA_LEAVES),
                    Reward.item(Items.BROWN_MUSHROOM),
                    Reward.item(Items.RED_MUSHROOM),
                    Reward.item(Items.GLOW_BERRIES),
                    Reward.item(Items.SWEET_BERRIES),
                    Reward.item(Items.KELP)));

    public static final Quest WHEAT = CHAPTER.quest("wheat", q -> q
            .at(-2, 1.5)
            .task(Task.item(Items.WHEAT)));

    public static final Quest CARROT = CHAPTER.quest("carrot", q -> q
            .at(-3, 1.5)
            .task(Task.item(Items.CARROT)));

    public static final Quest POTATO = CHAPTER.quest("potato", q -> q
            .at(-4, 1.5)
            .task(Task.item(Items.POTATO)));

    public static final Quest BEETROOT = CHAPTER.quest("beetroot", q -> q
            .at(-5, 1.5)
            .task(Task.item(Items.BEETROOT)));

    public static final Quest CABBAGE = CHAPTER.quest("cabbage", q -> q
            .at(-9, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CABBAGE)));

    public static final Quest TOMATO = CHAPTER.quest("tomato", q -> q
            .at(-10, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.TOMATO)));

    public static final Quest RICE = CHAPTER.quest("rice", q -> q
            .at(-7, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.RICE)));

    public static final Quest ONION = CHAPTER.quest("onion", q -> q
            .at(-6, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.ONION)));

    public static final Quest COFFEE_BERRIES = CHAPTER.quest("coffee_berries", q -> q
            .at(-4, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.COFFEE_BERRIES)));

    public static final Quest GREEN_TEA_LEAVES = CHAPTER.quest("green_tea_leaves", q -> q
            .at(-3, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.GREEN_TEA_LEAVES)));

    public static final Quest YELLOW_TEA_LEAVES = CHAPTER.quest("yellow_tea_leaves", q -> q
            .at(-2, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.YELLOW_TEA_LEAVES)));

    public static final Quest BLACK_TEA_LEAVES = CHAPTER.quest("black_tea_leaves", q -> q
            .at(-1, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.BLACK_TEA_LEAVES)));

    public static final Quest COCOA_BEANS = CHAPTER.quest("cocoa_beans", q -> q
            .at(-1, 1.5)
            .task(Task.item(Items.COCOA_BEANS)));

    public static final Quest KELP = CHAPTER.quest("kelp", q -> q
            .at(-6, 1.5)
            .task(Task.item(Items.KELP)));

    public static final Quest BROWN_MUSHROOM = CHAPTER.quest("brown_mushroom", q -> q
            .at(-9, 1.5)
            .task(Task.item(Items.BROWN_MUSHROOM), Task.item(Items.RED_MUSHROOM)));

    public static final Quest WARPED_FUNGUS = CHAPTER.quest("warped_fungus", q -> q
            .at(-10, 1.5)
            .task(Task.item(Items.WARPED_FUNGUS), Task.item(Items.CRIMSON_FUNGUS)));

    public static final Quest PORKCHOP = CHAPTER.quest("porkchop", q -> q
            .at(-12, 5.5)
            .task(Task.item(Items.PORKCHOP)));

    public static final Quest BEEF = CHAPTER.quest("beef", q -> q
            .at(-11, 5.5)
            .task(Task.item(Items.BEEF)));

    public static final Quest CHICKEN = CHAPTER.quest("chicken", q -> q
            .at(-10, 5.5)
            .task(Task.item(Items.CHICKEN)));

    public static final Quest MUTTON = CHAPTER.quest("mutton", q -> q
            .at(-8, 5.5)
            .task(Task.item(Items.MUTTON)));

    public static final Quest RABBIT = CHAPTER.quest("rabbit", q -> q
            .at(-9, 5.5)
            .task(Task.item(Items.RABBIT)));

    public static final Quest MILK_BUCKET = CHAPTER.quest("milk_bucket", q -> q
            .at(-12, 6.5)
            .task(Task.item(Items.MILK_BUCKET)));

    public static final Quest EGG = CHAPTER.quest("egg", q -> q
            .at(-11, 6.5)
            .task(Task.item(Items.EGG)));

    public static final Quest BONE = CHAPTER.quest("bone", q -> q
            .at(-10, 6.5)
            .task(Task.item(Items.BONE)));

    public static final Quest COD = CHAPTER.quest("cod", q -> q
            .at(-12, 7.5)
            .task(Task.item(Items.COD)));

    public static final Quest SALMON = CHAPTER.quest("salmon", q -> q
            .at(-11, 7.5)
            .task(Task.item(Items.SALMON)));

    public static final Quest TROPICAL_FISH = CHAPTER.quest("tropical_fish", q -> q
            .at(-10, 7.5)
            .task(Task.item(Items.TROPICAL_FISH)));

    public static final Quest PUFFERFISH = CHAPTER.quest("pufferfish", q -> q
            .at(-9, 7.5)
            .task(Task.item(Items.PUFFERFISH)));

    public static final Quest HAM = CHAPTER.quest("ham", q -> q
            .at(-5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.HAM)));

    public static final Quest BOWL = CHAPTER.quest("bowl", q -> q
            .at(1.5, 4)
            .task(Task.item(Items.BOWL)));

    public static final Quest GLASS_BOTTLE = CHAPTER.quest("glass_bottle", q -> q
            .at(0, 4)
            .task(Task.item(Items.GLASS_BOTTLE)));

    public static final Quest COOKED_CHICKEN_CUTS = CHAPTER.quest("cooked_chicken_cuts", q -> q
            .at(8.5, 1.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.COOKED_CHICKEN_CUTS)));

    public static final Quest COOKED_BEEF = CHAPTER.quest("cooked_beef", q -> q
            .at(2.5, 1.5)
            .task(Task.item(Items.COOKED_BEEF)));

    public static final Quest FRIED_EGG = CHAPTER.quest("fried_egg", q -> q
            .at(11.5, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.FRIED_EGG)));

    public static final Quest COOKED_BACON = CHAPTER.quest("cooked_bacon", q -> q
            .at(5.5, 1.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.COOKED_BACON)));

    public static final Quest BEEF_PATTY = CHAPTER.quest("beef_patty", q -> q
            .at(3.5, 1.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.BEEF_PATTY)));

    public static final Quest COOKED_COD = CHAPTER.quest("cooked_cod", q -> q
            .at(3.5, 2.5)
            .task(Task.item(Items.COOKED_COD)));

    public static final Quest BREAD = CHAPTER.quest("bread", q -> q
            .at(8.5, 2.5)
            .task(Task.item(Items.BREAD)));

    public static final Quest COOKED_MUTTON = CHAPTER.quest("cooked_mutton", q -> q
            .at(10.5, 1.5)
            .task(Task.item(Items.COOKED_MUTTON)));

    public static final Quest COOKED_CHICKEN = CHAPTER.quest("cooked_chicken", q -> q
            .at(7.5, 1.5)
            .task(Task.item(Items.COOKED_CHICKEN)));

    public static final Quest COOKED_SALMON = CHAPTER.quest("cooked_salmon", q -> q
            .at(4.5, 2.5)
            .task(Task.item(Items.COOKED_SALMON)));

    public static final Quest COOKED_MUTTON_CHOPS = CHAPTER.quest("cooked_mutton_chops", q -> q
            .at(11.5, 1.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.COOKED_MUTTON_CHOPS)));

    public static final Quest DRIED_KELP = CHAPTER.quest("dried_kelp", q -> q
            .at(7.5, 2.5)
            .task(Task.item(Items.DRIED_KELP)));

    public static final Quest SMOKED_HAM = CHAPTER.quest("smoked_ham", q -> q
            .at(6.5, 1.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.SMOKED_HAM)));

    public static final Quest BAKED_POTATO = CHAPTER.quest("baked_potato", q -> q
            .at(9.5, 2.5)
            .task(Task.item(Items.BAKED_POTATO)));

    public static final Quest COOKED_RABBIT = CHAPTER.quest("cooked_rabbit", q -> q
            .at(9.5, 1.5)
            .task(Task.item(Items.COOKED_RABBIT)));

    public static final Quest COOKED_PORKCHOP = CHAPTER.quest("cooked_porkchop", q -> q
            .at(4.5, 1.5)
            .task(Task.item(Items.COOKED_PORKCHOP)));

    public static final Quest COOKED_COD_SLICE = CHAPTER.quest("cooked_cod_slice", q -> q
            .at(2.5, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.COOKED_COD_SLICE)));

    public static final Quest CHICKEN_CUTS = CHAPTER.quest("chicken_cuts", q -> q
            .at(-4, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CHICKEN_CUTS)));

    public static final Quest BACON = CHAPTER.quest("bacon", q -> q
            .at(-3, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.BACON)));

    public static final Quest SALMON_SLICE = CHAPTER.quest("salmon_slice", q -> q
            .at(-2, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.SALMON_SLICE)));

    public static final Quest MINCED_BEEF = CHAPTER.quest("minced_beef", q -> q
            .at(-2, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.MINCED_BEEF)));

    public static final Quest DOUGH = CHAPTER.quest("dough", q -> q
            .at(-2, 6.5)
            .task(Task.item(GTItems.DOUGH)));

    public static final Quest MUTTON_CHOPS = CHAPTER.quest("mutton_chops", q -> q
            .at(-1, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.MUTTON_CHOPS)));

    public static final Quest COD_SLICE = CHAPTER.quest("cod_slice", q -> q
            .at(-1, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.COD_SLICE)));

    public static final Quest COOKED_SALMON_SLICE = CHAPTER.quest("cooked_salmon_slice", q -> q
            .at(5.5, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.COOKED_SALMON_SLICE)));

    public static final Quest HONEY_BOTTLE = CHAPTER.quest("honey_bottle", q -> q
            .at(-9, 6.5)
            .task(Task.item(Items.HONEY_BOTTLE)));

    public static final Quest RAW_PASTA = CHAPTER.quest("raw_pasta", q -> q
            .at(-1, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.RAW_PASTA)));

    public static final Quest CABBAGE_LEAF = CHAPTER.quest("cabbage_leaf", q -> q
            .at(-8, 2.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CABBAGE_LEAF)));

    public static final Quest SQUID_INK_PASTA = CHAPTER.quest("squid_ink_pasta", q -> q
            .at(2.5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.SQUID_INK_PASTA)));

    public static final Quest BAKED_COD_STEW = CHAPTER.quest("baked_cod_stew", q -> q
            .at(3.5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.BAKED_COD_STEW)));

    public static final Quest BLAZING_CHILI = CHAPTER.quest("blazing_chili", q -> q
            .at(9.5, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.BLAZING_CHILI)));

    public static final Quest BONE_BROTH = CHAPTER.quest("bone_broth", q -> q
            .at(8.5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.BONE_BROTH)));

    public static final Quest RABBIT_STEW = CHAPTER.quest("rabbit_stew", q -> q
            .at(9.5, 5.5)
            .task(Task.item(Items.RABBIT_STEW)));

    public static final Quest TEA_CURRY = CHAPTER.quest("tea_curry", q -> q
            .at(10.5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.TEA_CURRY)));

    public static final Quest CABBAGE_ROLLS = CHAPTER.quest("cabbage_rolls", q -> q
            .at(11.5, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CABBAGE_ROLLS)));

    public static final Quest BEEF_STEW = CHAPTER.quest("beef_stew", q -> q
            .at(11.5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.BEEF_STEW)));

    public static final Quest COOKED_RICE = CHAPTER.quest("cooked_rice", q -> q
            .at(5.5, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.COOKED_RICE)));

    public static final Quest VEGETABLE_NOODLES = CHAPTER.quest("vegetable_noodles", q -> q
            .at(2.5, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.VEGETABLE_NOODLES)));

    public static final Quest MUSHROOM_RICE = CHAPTER.quest("mushroom_rice", q -> q
            .at(3.5, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.MUSHROOM_RICE)));

    public static final Quest DUMPLINGS = CHAPTER.quest("dumplings", q -> q
            .at(10.5, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.DUMPLINGS)));

    public static final Quest FRIED_RICE = CHAPTER.quest("fried_rice", q -> q
            .at(4.5, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.FRIED_RICE)));

    public static final Quest VEGETABLE_SOUP = CHAPTER.quest("vegetable_soup", q -> q
            .at(9.5, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.VEGETABLE_SOUP)));

    public static final Quest MUSHROOM_STEW = CHAPTER.quest("mushroom_stew", q -> q
            .at(7.5, 6.5)
            .task(Task.item(Items.MUSHROOM_STEW)));

    public static final Quest STUFFED_PUMPKIN_BLOCK = CHAPTER.quest("stuffed_pumpkin_block", q -> q
            .at(2.5, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.STUFFED_PUMPKIN_BLOCK)));

    public static final Quest TOMATO_SAUCE = CHAPTER.quest("tomato_sauce", q -> q
            .at(3.5, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.TOMATO_SAUCE)));

    public static final Quest CHICKEN_SOUP = CHAPTER.quest("chicken_soup", q -> q
            .at(7.5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CHICKEN_SOUP)));

    public static final Quest PASTA_WITH_MEATBALLS = CHAPTER.quest("pasta_with_meatballs", q -> q
            .at(11.5, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.PASTA_WITH_MEATBALLS)));

    public static final Quest GLOW_BERRY_CUSTARD = CHAPTER.quest("glow_berry_custard", q -> q
            .at(5.5, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.GLOW_BERRY_CUSTARD)));

    public static final Quest DOG_FOOD = CHAPTER.quest("dog_food", q -> q
            .at(7.5, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.DOG_FOOD)));

    public static final Quest BEETROOT_SOUP = CHAPTER.quest("beetroot_soup", q -> q
            .at(6.5, 6.5)
            .task(Task.item(Items.BEETROOT_SOUP)));

    public static final Quest PASTA_WITH_MUTTON_CHOP = CHAPTER.quest("pasta_with_mutton_chop", q -> q
            .at(10.5, 7.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.PASTA_WITH_MUTTON_CHOP)));

    public static final Quest PUMPKIN_SOUP = CHAPTER.quest("pumpkin_soup", q -> q
            .at(8.5, 6.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.PUMPKIN_SOUP)));

    public static final Quest NOODLE_SOUP = CHAPTER.quest("noodle_soup", q -> q
            .at(6.5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.NOODLE_SOUP)));

    public static final Quest FISH_STEW = CHAPTER.quest("fish_stew", q -> q
            .at(4.5, 5.5)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.FISH_STEW)));

    public static final Quest MELON_SLICE = CHAPTER.quest("melon_slice", q -> q
            .at(-7, 1.5)
            .task(Task.item(Items.MELON_SLICE), Task.item(QuestExternalRefs.FarmersDelightItems.PUMPKIN_SLICE)));

    public static final Quest PIE_CRUST = CHAPTER.quest("pie_crust", q -> q
            .at(2.5, 10)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.PIE_CRUST)));

    public static final Quest APPLE_PIE = CHAPTER.quest("apple_pie", q -> q
            .at(3.5, 10)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.APPLE_PIE)));

    public static final Quest SWEET_BERRY_CHEESECAKE = CHAPTER.quest("sweet_berry_cheesecake", q -> q
            .at(4.5, 10)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.SWEET_BERRY_CHEESECAKE)));

    public static final Quest CHOCOLATE_PIE = CHAPTER.quest("chocolate_pie", q -> q
            .at(5.5, 10)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CHOCOLATE_PIE)));

    public static final Quest CAKE = CHAPTER.quest("cake", q -> q
            .at(11.5, 10)
            .task(Task.item(Items.CAKE)));

    public static final Quest SWEET_BERRY_COOKIE = CHAPTER.quest("sweet_berry_cookie", q -> q
            .at(10.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.SWEET_BERRY_COOKIE)));

    public static final Quest HONEY_COOKIE = CHAPTER.quest("honey_cookie", q -> q
            .at(11.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.HONEY_COOKIE)));

    public static final Quest MELON_POPSICLE = CHAPTER.quest("melon_popsicle", q -> q
            .at(11.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.MELON_POPSICLE)));

    public static final Quest FRUIT_SALAD = CHAPTER.quest("fruit_salad", q -> q
            .at(2.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.FRUIT_SALAD)));

    public static final Quest MIXED_SALAD = CHAPTER.quest("mixed_salad", q -> q
            .at(3.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.MIXED_SALAD)));

    public static final Quest NETHER_SALAD = CHAPTER.quest("nether_salad", q -> q
            .at(4.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.NETHER_SALAD)));

    public static final Quest BARBECUE_STICK = CHAPTER.quest("barbecue_stick", q -> q
            .at(2.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.BARBECUE_STICK)));

    public static final Quest EGG_SANDWICH = CHAPTER.quest("egg_sandwich", q -> q
            .at(3.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.EGG_SANDWICH)));

    public static final Quest CHICKEN_SANDWICH = CHAPTER.quest("chicken_sandwich", q -> q
            .at(4.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CHICKEN_SANDWICH)));

    public static final Quest HAMBURGER = CHAPTER.quest("hamburger", q -> q
            .at(5.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.HAMBURGER)));

    public static final Quest BACON_SANDWICH = CHAPTER.quest("bacon_sandwich", q -> q
            .at(6.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.BACON_SANDWICH)));

    public static final Quest MUTTON_WRAP = CHAPTER.quest("mutton_wrap", q -> q
            .at(7.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.MUTTON_WRAP)));

    public static final Quest STUFFED_POTATO = CHAPTER.quest("stuffed_potato", q -> q
            .at(8.5, 11)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.STUFFED_POTATO)));

    public static final Quest SALMON_ROLL = CHAPTER.quest("salmon_roll", q -> q
            .at(2.5, 13)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.SALMON_ROLL)));

    public static final Quest COD_ROLL = CHAPTER.quest("cod_roll", q -> q
            .at(3.5, 13)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.COD_ROLL)));

    public static final Quest KELP_ROLL = CHAPTER.quest("kelp_roll", q -> q
            .at(4.5, 13)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.KELP_ROLL)));

    public static final Quest KELP_ROLL_SLICE = CHAPTER.quest("kelp_roll_slice", q -> q
            .at(5.5, 13)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.KELP_ROLL_SLICE)));

    public static final Quest BACON_AND_EGGS = CHAPTER.quest("bacon_and_eggs", q -> q
            .at(5.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.BACON_AND_EGGS)));

    public static final Quest ROASTED_MUTTON_CHOPS = CHAPTER.quest("roasted_mutton_chops", q -> q
            .at(6.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.ROASTED_MUTTON_CHOPS)));

    public static final Quest STEAK_AND_POTATOES = CHAPTER.quest("steak_and_potatoes", q -> q
            .at(7.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.STEAK_AND_POTATOES)));

    public static final Quest GRILLED_SALMON = CHAPTER.quest("grilled_salmon", q -> q
            .at(8.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.GRILLED_SALMON)));

    public static final Quest ROAST_CHICKEN_BLOCK = CHAPTER.quest("roast_chicken_block", q -> q
            .at(9.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.ROAST_CHICKEN_BLOCK)));

    public static final Quest HONEY_GLAZED_HAM_BLOCK = CHAPTER.quest("honey_glazed_ham_block", q -> q
            .at(10.5, 12)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.HONEY_GLAZED_HAM_BLOCK)));

    public static final Quest SHEPHERDS_PIE_BLOCK = CHAPTER.quest("shepherds_pie_block", q -> q
            .at(6.5, 10)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.SHEPHERDS_PIE_BLOCK)));

    public static final Quest RICE_ROLL_MEDLEY_BLOCK = CHAPTER.quest("rice_roll_medley_block", q -> q
            .at(6.5, 13)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.RICE_ROLL_MEDLEY_BLOCK)));

    public static final Quest GREEN_TEA_COOKIE = CHAPTER.quest("green_tea_cookie", q -> q
            .at(10.5, 13)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.GREEN_TEA_COOKIE)));

    public static final Quest NETHER_WART_SOURDOUGH = CHAPTER.quest("nether_wart_sourdough", q -> q
            .at(11.5, 13)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.NETHER_WART_SOURDOUGH)));

    public static final Quest BLACK_COD = CHAPTER.quest("black_cod", q -> q
            .at(7.5, 13)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.BLACK_COD)));

    public static final Quest COFFEE_CAKE = CHAPTER.quest("coffee_cake", q -> q
            .at(10.5, 10)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.COFFEE_CAKE)));

    public static final Quest ROSE_HIP_PIE = CHAPTER.quest("rose_hip_pie", q -> q
            .at(7.5, 10)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.ROSE_HIP_PIE)));

    public static final Quest BLACK_TEA = CHAPTER.quest("black_tea", q -> q
            .at(-12, 10)
            .task(
                    Task.item(QuestExternalRefs.FarmersRespiteItems.BLACK_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_BLACK_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_BLACK_TEA))
            .requireAny());

    public static final Quest STRONG_GREEN_TEA = CHAPTER.quest("strong_green_tea", q -> q
            .at(-11, 10)
            .task(
                    Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_GREEN_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_GREEN_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.GREEN_TEA))
            .requireAny());

    public static final Quest STRONG_YELLOW_TEA = CHAPTER.quest("strong_yellow_tea", q -> q
            .at(-10, 10)
            .task(
                    Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_YELLOW_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_YELLOW_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.YELLOW_TEA))
            .requireAny());

    public static final Quest PURULENT_TEA = CHAPTER.quest("purulent_tea", q -> q
            .at(-9, 10)
            .task(
                    Task.item(QuestExternalRefs.FarmersRespiteItems.PURULENT_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_PURULENT_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_PURULENT_TEA)));

    public static final Quest LONG_ROSE_HIP_TEA = CHAPTER.quest("long_rose_hip_tea", q -> q
            .at(-8, 10)
            .task(
                    Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_ROSE_HIP_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_ROSE_HIP_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.ROSE_HIP_TEA)));

    public static final Quest LONG_DANDELION_TEA = CHAPTER.quest("long_dandelion_tea", q -> q
            .at(-7, 10)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_DANDELION_TEA), Task.item(QuestExternalRefs.FarmersRespiteItems.DANDELION_TEA)));

    public static final Quest HOT_COCOA = CHAPTER.quest("hot_cocoa", q -> q
            .at(-3, 10)
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.HOT_COCOA), Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_HOT_COCOA)));

    public static final Quest STRONG_COFFEE = CHAPTER.quest("strong_coffee", q -> q
            .at(-5, 10)
            .task(
                    Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_COFFEE),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.COFFEE),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_COFFEE)));

    public static final Quest STRONG_MELON_JUICE = CHAPTER.quest("strong_melon_juice", q -> q
            .at(-1, 10)
            .task(Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_MELON_JUICE), Task.item(QuestExternalRefs.FarmersDelightItems.MELON_JUICE)));

    public static final Quest APPLE_CIDER = CHAPTER.quest("apple_cider", q -> q
            .at(-2, 10)
            .task(
                    Task.item(QuestExternalRefs.FarmersDelightItems.APPLE_CIDER),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_APPLE_CIDER),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_APPLE_CIDER)));

    public static final Quest LONG_GAMBLERS_TEA = CHAPTER.quest("long_gamblers_tea", q -> q
            .at(-6, 10)
            .task(
                    Task.item(QuestExternalRefs.FarmersRespiteItems.LONG_GAMBLERS_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.STRONG_GAMBLERS_TEA),
                    Task.item(QuestExternalRefs.FarmersRespiteItems.GAMBLERS_TEA)));

    public static final Quest ELECTRIC_COOKING = CHAPTER.quest("electric_cooking", q -> q
            .at(1.5, 0)
            .subtitle("格雷牌电饭煲")
            .task(Task.item(MultiBlockG.ELECTRIC_COOKING))
            .reward(Reward.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_FEEDING_UPGRADE))
            .dependsOn(HvChapter.WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest GLOW_BERRIES = CHAPTER.quest("glow_berries", q -> q
            .at(-8, 1.5)
            .task(Task.item(Items.GLOW_BERRIES), Task.item(Items.SWEET_BERRIES)));

    public static final Quest NETHER_WART = CHAPTER.quest("nether_wart", q -> q
            .at(-5, 2.5)
            .task(Task.item(Items.NETHER_WART), Task.item(Items.CHORUS_FRUIT)));

    public static final Quest WICKER_BASKET = CHAPTER.quest("wicker_basket", q -> q
            .at(0.75, -4)
            .task(
                    Task.item(QuestExternalRefs.SOLCLASSIC_WICKER_BASKET),
                    Task.item(QuestExternalRefs.SOLCLASSIC_BASKET),
                    Task.item(QuestExternalRefs.FarmersDelightItems.BAMBOO_BASKET)));
}
