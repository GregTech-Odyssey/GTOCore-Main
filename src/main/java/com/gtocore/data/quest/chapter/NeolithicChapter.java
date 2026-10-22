package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import com.ultramega.ae2insertexportcard.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NeolithicChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("neolithic", c -> c
            .title("1. 石器时代(Stone Age)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(ToolHelper.get(GTToolType.AXE, GTMaterials.Flint).getItem()));

    public static final Quest GOLD_PANNER = CHAPTER.quest("gold_panner", q -> q
            .at(2, 2)
            .title("淘金客")
            .task(Task.item(Items.GRAVEL, 16), Task.item(Items.FLINT, 4))
            .scaledReward(Items.GRAVEL, 16)
            .scaledReward(Items.FLINT, 4)
            .dependsOn(NewGregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest FLINT_KNAPPER = CHAPTER.quest("flint_knapper", q -> q
            .at(0, 2)
            .title("陶棍客")
            .task(Task.item(Items.STICK, 4))
            .scaledReward(Items.STICK, 4)
            .dependsOn(NewGregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest FLINT_KNIFE = CHAPTER.quest("flint_knife", q -> q
            .at(2, 4)
            .title("燧石小刀")
            .icon(vectorwing.farmersdelight.common.registry.ModItems.FLINT_KNIFE.get())
            .task(Task.tag(CustomTags.KNIVES, 1).title("任意小刀"))
            .scaledReward(Items.FLINT, 2)
            .dependsOn(GOLD_PANNER, FLINT_KNAPPER));

    public static final Quest PLANT_FIBER = CHAPTER.quest("plant_fiber", q -> q
            .at(4, 2)
            .title("植物纤维")
            .task(Task.item(GTOItems.PLANT_FIBER, 2))
            .scaledReward(GTOItems.PLANT_FIBER, 2)
            .dependsOn(FLINT_KNIFE));

    public static final Quest FLINT_AXE = CHAPTER.quest("flint_axe", q -> q
            .at(4, 0)
            .milestone()
            .title("燧石斧")
            .task(Task.item(ToolHelper.get(GTToolType.AXE, GTMaterials.Flint).getItem()))
            .scaledReward(Items.FLINT, 2)
            .dependsOn(PLANT_FIBER));

    public static final Quest CHOP_TREES = CHAPTER.quest("chop_trees", q -> q
            .at(6, 2)
            .title("要致富，先撸树")
            .task(Task.tag(ItemTags.LOGS, 16).title("任意原木"))
            .scaledReward(Items.OAK_LOG, 16)
            .dependsOn(FLINT_AXE));

    public static final Quest WORKBENCH = CHAPTER.quest("workbench", q -> q
            .at(8, 0)
            .milestone()
            .title("工作台")
            .task(Task.item(Items.CRAFTING_TABLE))
            .scaledReward(Items.FLINT, 4)
            .dependsOn(CHOP_TREES));

    public static final Quest FLINT_PICKAXE = CHAPTER.quest("flint_pickaxe", q -> q
            .at(10, -2)
            .title("燧石镐")
            .task(Task.item(ToolHelper.get(GTToolType.PICKAXE, GTMaterials.Flint).getItem()))
            .scaledReward(Items.COBBLESTONE, 8)
            .dependsOn(WORKBENCH));

    public static final QuestLink FLINT_PICKAXE_COPY_1 = CHAPTER.copy(FLINT_PICKAXE, 14, 2);

    public static final Quest FURNACE = CHAPTER.quest("furnace", q -> q
            .at(12, 0)
            .milestone()
            .title("熔炉")
            .task(Task.item(Items.FURNACE))
            .scaledReward(Items.COAL, 4)
            .dependsOn(FLINT_PICKAXE));

    public static final Quest STONE = CHAPTER.quest("stone", q -> q
            .at(12, -2)
            .title("石头")
            .task(Task.item(Items.STONE, 5))
            .scaledReward(Items.COBBLESTONE, 8)
            .dependsOn(FURNACE));

    public static final Quest FLINT_MORTAR = CHAPTER.quest("flint_mortar", q -> q
            .at(14, -2)
            .title("燧石研钵")
            .icon(ToolHelper.get(GTToolType.MORTAR, GTMaterials.Flint).getItem())
            .task(Task.tag(CustomTags.MORTARS, 1).title("任意研钵"))
            .scaledReward(Items.FLINT, 2)
            .dependsOn(STONE));

    public static final Quest TIME_TO_MINE = CHAPTER.quest("time_to_mine", q -> q
            .at(16, 2)
            .title("下矿时间到")
            .task(
                    Task.anyOf(16, ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Tin), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Cassiterite), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.CassiteriteSand)).title("任意粗锡"),
                    Task.anyOf(16, Items.RAW_COPPER, ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Chalcopyrite), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Malachite)).title("任意粗铜"),
                    Task.item(Items.RAW_IRON, 8))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(FLINT_PICKAXE_COPY_1));

    public static final Quest SMELTING = CHAPTER.quest("smelting", q -> q
            .at(16, 0)
            .milestone()
            .title("铜锭、锡锭与铁锭")
            .icon(TagPrefix.ingot, GTMaterials.Copper)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Copper, 12), Task.item(TagPrefix.ingot, GTMaterials.Tin, 4), Task.item(Items.IRON_INGOT, 6))
            .scaledReward(Items.COAL, 4)
            .dependsOn(TIME_TO_MINE, FURNACE));

    public static final Quest TOOL_USER = CHAPTER.quest("tool_user", q -> q
            .at(22, 4)
            .title("人类会使用工具")
            .icon(ToolHelper.get(GTToolType.HARD_HAMMER, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.HAMMERS, 1).title("任意锻造锤"))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(SMELTING));

    public static final Quest COPPER_TIN_DUST = CHAPTER.quest("copper_tin_dust", q -> q
            .at(16, -2)
            .title("铜粉与锡粉")
            .icon(TagPrefix.dust, GTMaterials.Copper)
            .task(Task.item(TagPrefix.dust, GTMaterials.Copper, 12), Task.item(TagPrefix.dust, GTMaterials.Tin, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Copper, 3)
            .dependsOn(SMELTING, FLINT_MORTAR));

    public static final Quest STIR_STIR = CHAPTER.quest("stir_stir", q -> q
            .at(18, -2)
            .title("搅拌，搅拌！")
            .task(Task.item(TagPrefix.dust, GTMaterials.Bronze, 12))
            .scaledReward(Items.COAL, 4)
            .dependsOn(COPPER_TIN_DUST));

    public static final Quest WESTERNIZATION_MOVEMENT = CHAPTER.quest("westernization_movement", q -> q
            .at(20, 0)
            .milestone()
            .title("扬雾运动")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Bronze, 12))
            .scaledReward(TagPrefix.ingot, GTMaterials.Bronze, 4)
            .dependsOn(STIR_STIR));

    public static final Quest BRONZE_PLATE = CHAPTER.quest("bronze_plate", q -> q
            .at(22, 2)
            .title("青铜板")
            .task(Task.item(TagPrefix.plate, GTMaterials.Bronze, 5))
            .scaledReward(TagPrefix.ingot, GTMaterials.Bronze, 2)
            .dependsOn(WESTERNIZATION_MOVEMENT, TOOL_USER));

    public static final Quest STICKY = CHAPTER.quest("sticky", q -> q
            .at(22, -4)
            .title("黏糊糊？")
            .task(Task.item(Items.CLAY_BALL, 12))
            .scaledReward(Items.CLAY_BALL, 4)
            .dependsOn(NewGregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest BAKE_IT = CHAPTER.quest("bake_it", q -> q
            .at(24, -4)
            .title("烧一烧")
            .task(Task.item(Items.BRICK, 12))
            .scaledReward(Items.BRICK, 4)
            .dependsOn(STICKY, FURNACE));

    public static final Quest BRICK_VEIN = CHAPTER.quest("brick_vein", q -> q
            .at(24, -2)
            .title("有没有砖矿脉？")
            .task(Task.item(Items.BRICKS, 3))
            .scaledReward(Items.BRICKS, 2)
            .dependsOn(BAKE_IT));

    public static final Quest BRONZE_BRICK_HULL = CHAPTER.quest("bronze_brick_hull", q -> q
            .at(24, 0)
            .milestone()
            .title("砖砌青铜外壳")
            .task(Task.item(GTBlocks.BRONZE_BRICKS_HULL))
            .reward(Reward.item(GTBlocks.BRONZE_BRICKS_HULL, 4))
            .dependsOn(BRONZE_PLATE, BRICK_VEIN));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewGregtechOdysseyChapter.HELLO_WORLD, 0, 0))
                .mainLine(NewGregtechOdysseyChapter.HELLO_WORLD, FLINT_AXE, WORKBENCH, FURNACE, SMELTING, WESTERNIZATION_MOVEMENT, BRONZE_BRICK_HULL));
    }
}
