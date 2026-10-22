package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockG;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewSteamChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_steam", c -> c
            .title("2. 蒸汽时代(Steam Age)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.STEAM_SOLID_BOILER.first()));

    public static final Quest WRENCH = CHAPTER.quest("wrench", q -> q
            .at(2, 2)
            .title("扳手")
            .task(Task.tag(CustomTags.WRENCHES, 1).title("任意扳手"))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final QuestLink WRENCH_COPY_1 = CHAPTER.copy(WRENCH, 8, 4);

    public static final QuestLink WRENCH_COPY_2 = CHAPTER.copy(WRENCH, 12, -2);

    public static final Quest FILE = CHAPTER.quest("file", q -> q
            .at(10, 4)
            .title("锉")
            .icon(ToolHelper.get(GTToolType.FILE, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.FILES, 1).title("任意锉"))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final QuestLink FILE_COPY_1 = CHAPTER.copy(FILE, 26, 4);

    public static final Quest SAW = CHAPTER.quest("saw", q -> q
            .at(12, 4)
            .title("锯")
            .icon(ToolHelper.get(GTToolType.SAW, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.SAWS, 1).title("任意锯"))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(FILE));

    public static final Quest SCREWDRIVER = CHAPTER.quest("screwdriver", q -> q
            .at(10, 2)
            .title("螺丝刀")
            .icon(ToolHelper.get(GTToolType.SCREWDRIVER, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.SCREWDRIVERS, 1).title("任意螺丝刀"))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(FILE));

    public static final QuestLink SCREWDRIVER_COPY_1 = CHAPTER.copy(SCREWDRIVER, 20, 4);

    public static final QuestLink SCREWDRIVER_COPY_2 = CHAPTER.copy(SCREWDRIVER, 20, 2);

    public static final Quest IRON_SCREW = CHAPTER.quest("iron_screw", q -> q
            .at(12, 2)
            .title("铁螺丝")
            .task(Task.item(TagPrefix.screw, GTMaterials.Iron, 5))
            .scaledReward(TagPrefix.rod, GTMaterials.Iron, 2)
            .dependsOn(SAW));

    public static final QuestLink IRON_SCREW_COPY_1 = CHAPTER.copy(IRON_SCREW, 18, 2);

    public static final QuestLink IRON_SCREW_COPY_2 = CHAPTER.copy(IRON_SCREW, 20, -2);

    public static final Quest WIRE_CUTTER = CHAPTER.quest("wire_cutter", q -> q
            .at(18, 4)
            .title("剪线钳")
            .icon(ToolHelper.get(GTToolType.WIRE_CUTTER, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.WIRE_CUTTERS, 1).title("任意剪线钳"))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(IRON_SCREW_COPY_1, SCREWDRIVER_COPY_1));

    public static final QuestLink WIRE_CUTTER_COPY_1 = CHAPTER.copy(WIRE_CUTTER, 40, 4);

    public static final Quest IRON_PICKAXE = CHAPTER.quest("iron_pickaxe", q -> q
            .at(28, 4)
            .title("铁镐")
            .task(Task.item(Items.IRON_PICKAXE))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(FILE_COPY_1));

    public static final Quest DIAMOND = CHAPTER.quest("diamond", q -> q
            .at(28, 2)
            .title("钻石")
            .task(Task.item(Items.DIAMOND, 2))
            .scaledReward(Items.TORCH, 16)
            .dependsOn(IRON_PICKAXE));

    public static final Quest BLAST_FURNACE = CHAPTER.quest("blast_furnace", q -> q
            .at(12, 0)
            .milestone()
            .title("高炉")
            .task(Task.item(Items.BLAST_FURNACE))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(WRENCH_COPY_2, IRON_SCREW, SCREWDRIVER));

    public static final Quest HOT_IRON = CHAPTER.quest("hot_iron", q -> q
            .at(44, 2)
            .title("热铁锭")
            .task(Task.item(GTOItems.HOT_IRON_INGOT))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(BLAST_FURNACE));

    public static final Quest WROUGHT_IRON = CHAPTER.quest("wrought_iron", q -> q
            .at(46, 2)
            .title("锻铁")
            .task(Task.item(TagPrefix.ingot, GTMaterials.WroughtIron, 22))
            .scaledReward(TagPrefix.ingot, GTMaterials.WroughtIron, 2)
            .dependsOn(HOT_IRON));

    public static final Quest ULV_CASING = CHAPTER.quest("ulv_casing", q -> q
            .at(46, 0)
            .title("ULV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_ULV))
            .scaledReward(TagPrefix.plate, GTMaterials.WroughtIron, 1)
            .dependsOn(WROUGHT_IRON));

    public static final Quest SOLID_BOILER = CHAPTER.quest("solid_boiler", q -> q
            .at(4, 0)
            .milestone()
            .title("低压蒸汽固体燃料锅炉")
            .task(Task.item(GTMachines.STEAM_SOLID_BOILER.first()))
            .scaledReward(Items.COAL, 4)
            .dependsOn(WRENCH));

    public static final Quest BRONZE_PIPE = CHAPTER.quest("bronze_pipe", q -> q
            .at(8, 2)
            .title("小型青铜流体管道")
            .task(Task.item(TagPrefix.pipeSmallFluid, GTMaterials.Bronze, 12))
            .scaledReward(TagPrefix.plate, GTMaterials.Bronze, 2)
            .dependsOn(WRENCH_COPY_1));

    public static final QuestLink BRONZE_PIPE_COPY_1 = CHAPTER.copy(BRONZE_PIPE, 14, -2);

    public static final QuestLink BRONZE_PIPE_COPY_2 = CHAPTER.copy(BRONZE_PIPE, 32, 2);

    public static final Quest ALLOY_SMELTER = CHAPTER.quest("alloy_smelter", q -> q
            .at(8, 0)
            .milestone()
            .title("低压蒸汽合金炉")
            .task(Task.item(GTMachines.STEAM_ALLOY_SMELTER.first()))
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 1)
            .scaledReward(Items.REDSTONE, 4)
            .dependsOn(BRONZE_PIPE));

    public static final Quest BRONZE_HULL = CHAPTER.quest("bronze_hull", q -> q
            .at(14, 2)
            .title("青铜外壳")
            .task(Task.item(GTBlocks.BRONZE_HULL, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.Bronze, 2)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final QuestLink BRONZE_HULL_COPY_1 = CHAPTER.copy(BRONZE_HULL, 30, 4);

    public static final QuestLink BRONZE_HULL_COPY_2 = CHAPTER.copy(BRONZE_HULL, 34, 2);

    public static final Quest RED_ALLOY = CHAPTER.quest("red_alloy", q -> q
            .at(14, 4)
            .title("红色合金")
            .task(Task.item(TagPrefix.ingot, GTMaterials.RedAlloy, 10))
            .scaledReward(Items.REDSTONE, 8)
            .dependsOn(ALLOY_SMELTER));

    public static final QuestLink RED_ALLOY_COPY_1 = CHAPTER.copy(RED_ALLOY, 44, 4);

    public static final Quest IRON_SMALL_GEAR = CHAPTER.quest("iron_small_gear", q -> q
            .at(16, 4)
            .title("铁小齿轮")
            .task(Task.item(TagPrefix.gearSmall, GTMaterials.Iron, 6))
            .scaledReward(TagPrefix.rod, GTMaterials.Iron, 2)
            .dependsOn(WIRE_CUTTER));

    public static final Quest PISTON = CHAPTER.quest("piston", q -> q
            .at(16, 2)
            .title("活塞")
            .task(Task.item(Items.PISTON, 3))
            .scaledReward(TagPrefix.plate, GTMaterials.Bronze, 2)
            .dependsOn(RED_ALLOY, IRON_SMALL_GEAR));

    public static final QuestLink PISTON_COPY_1 = CHAPTER.copy(PISTON, 32, 4);

    public static final QuestLink PISTON_COPY_2 = CHAPTER.copy(PISTON, 36, 2);

    public static final Quest COMPRESSOR = CHAPTER.quest("compressor", q -> q
            .at(16, 0)
            .milestone()
            .title("低压蒸汽压缩机")
            .task(Task.item(GTMachines.STEAM_COMPRESSOR.first()))
            .scaledReward(Items.CLAY_BALL, 16)
            .dependsOn(PISTON, BRONZE_HULL, BRONZE_PIPE_COPY_1));

    public static final Quest STEAM_MACERATOR = CHAPTER.quest("steam_macerator", q -> q
            .at(30, 2)
            .title("低压蒸汽粉碎机")
            .task(Task.item(GTMachines.STEAM_MACERATOR.first()))
            .scaledReward(Items.SAND, 16)
            .dependsOn(DIAMOND, PISTON_COPY_1, BRONZE_HULL_COPY_1, BRONZE_PIPE_COPY_2));

    public static final Quest CLAY_BRICK_DUST = CHAPTER.quest("clay_brick_dust", q -> q
            .at(14, -4)
            .title("黏土粉与砖粉")
            .icon(TagPrefix.dust, GTMaterials.Clay)
            .task(Task.item(TagPrefix.dust, GTMaterials.Clay, 8), Task.item(TagPrefix.dust, GTMaterials.Brick, 8))
            .scaledReward(Items.CLAY_BALL, 8)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest FIRECLAY_DUST = CHAPTER.quest("fireclay_dust", q -> q
            .at(16, -4)
            .title("耐火黏土粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Fireclay, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Fireclay, 4)
            .dependsOn(CLAY_BRICK_DUST));

    public static final Quest COMPRESSED_FIRECLAY = CHAPTER.quest("compressed_fireclay", q -> q
            .at(16, -2)
            .title("压缩耐火黏土")
            .task(Task.item(GTItems.COMPRESSED_FIRECLAY, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Fireclay, 4)
            .dependsOn(FIRECLAY_DUST, COMPRESSOR));

    public static final Quest FIREBRICK = CHAPTER.quest("firebrick", q -> q
            .at(18, -2)
            .title("耐火砖")
            .task(Task.item(GTItems.FIRECLAY_BRICK, 16))
            .scaledReward(GTBlocks.CASING_PRIMITIVE_BRICKS, 2)
            .dependsOn(COMPRESSED_FIRECLAY));

    public static final Quest PRIMITIVE_BLAST_FURNACE = CHAPTER.quest("primitive_blast_furnace", q -> q
            .at(20, 0)
            .milestone()
            .title("土高炉")
            .task(Task.item(GTMultiMachines.PRIMITIVE_BLAST_FURNACE), Task.item(GTOMachines.PRIMITIVE_BLAST_FURNACE_HATCH, 2), Task.item(GTBlocks.CASING_PRIMITIVE_BRICKS, 30))
            .scaledReward(Items.COAL, 8)
            .dependsOn(FIREBRICK, IRON_SCREW_COPY_2, SCREWDRIVER_COPY_2));

    public static final Quest STEEL = CHAPTER.quest("steel", q -> q
            .at(24, 0)
            .milestone()
            .title("第一块钢锭")
            .task(Task.item(ModItems.STEEL_INGOT.get(), 16))
            .scaledReward(ModItems.STEEL_INGOT.get(), 2)
            .dependsOn(PRIMITIVE_BLAST_FURNACE));

    public static final Quest EMPTY_MOLD = CHAPTER.quest("empty_mold", q -> q
            .at(32, -2)
            .title("空白模具")
            .task(Task.item(GTItems.SHAPE_EMPTY, 3))
            .scaledReward(ModItems.STEEL_INGOT.get(), 2)
            .dependsOn(STEEL));

    public static final Quest MOLDS = CHAPTER.quest("molds", q -> q
            .at(34, -2)
            .title("模具")
            .task(Task.item(GTItems.SHAPE_MOLD_BLOCK), Task.item(GTItems.SHAPE_MOLD_PLATE), Task.item(GTItems.SHAPE_MOLD_BALL))
            .scaledReward(Items.SAND, 8)
            .dependsOn(EMPTY_MOLD));

    public static final QuestLink MOLDS_COPY_1 = CHAPTER.copy(MOLDS, 40, 2);

    public static final Quest GLASS_DUST = CHAPTER.quest("glass_dust", q -> q
            .at(36, -4)
            .title("玻璃粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Glass, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Glass, 2)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest GLASS = CHAPTER.quest("glass", q -> q
            .at(36, -2)
            .title("玻璃")
            .task(Task.item(Items.GLASS, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Bronze, 2)
            .dependsOn(MOLDS, GLASS_DUST, ALLOY_SMELTER));

    public static final Quest STEAM_EXTRACTOR = CHAPTER.quest("steam_extractor", q -> q
            .at(36, 0)
            .milestone()
            .title("低压蒸汽提取机")
            .task(Task.item(GTMachines.STEAM_EXTRACTOR.first()))
            .scaledReward(GTItems.STICKY_RESIN, 4)
            .dependsOn(GLASS, PISTON_COPY_2, BRONZE_HULL_COPY_2));

    public static final Quest STICKY_RESIN = CHAPTER.quest("sticky_resin", q -> q
            .at(38, -4)
            .title("粘性树脂")
            .task(Task.item(GTItems.STICKY_RESIN, 4))
            .scaledReward(GTBlocks.RUBBER_SAPLING, 4)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest RAW_RUBBER = CHAPTER.quest("raw_rubber", q -> q
            .at(38, -2)
            .title("生橡胶粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.RawRubber, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.RawRubber, 6)
            .dependsOn(STEAM_EXTRACTOR, STICKY_RESIN));

    public static final Quest PULSATING_ALLOY = CHAPTER.quest("pulsating_alloy", q -> q
            .at(28, -2)
            .title("脉冲合金")
            .icon(TagPrefix.ingot, GTOMaterials.PulsatingAlloy)
            .task(Task.item(TagPrefix.block, GTOMaterials.PulsatingAlloy))
            .scaledReward(Items.ENDER_PEARL, 4)
            .dependsOn(ALLOY_SMELTER, COMPRESSOR));

    public static final Quest ANCIENT_REACTOR_CORE = CHAPTER.quest("ancient_reactor_core", q -> q
            .at(28, 0)
            .milestone()
            .title("远古反应核")
            .task(Task.item(MultiBlockG.ANCIENT_REACTOR_CORE))
            .scaledReward(ModItems.STEEL_INGOT.get(), 8)
            .dependsOn(PULSATING_ALLOY, STEEL, DIAMOND));

    public static final Quest ANCIENT_WORLD = CHAPTER.quest("ancient_world", q -> q
            .at(32, 0)
            .milestone()
            .title("远古世界")
            .task(Task.dimension(Dimension.ANCIENT_WORLD).title("远古世界"))
            .scaledReward(TagPrefix.dust, GTMaterials.Sphalerite, 8)
            .dependsOn(ANCIENT_REACTOR_CORE));

    public static final Quest SULFUR = CHAPTER.quest("sulfur", q -> q
            .at(40, -4)
            .title("硫粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Sulfur, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 2)
            .dependsOn(ANCIENT_WORLD));

    public static final Quest RUBBER_INGOT = CHAPTER.quest("rubber_ingot", q -> q
            .at(40, -2)
            .title("橡胶锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Rubber, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.Rubber, 2)
            .dependsOn(RAW_RUBBER, SULFUR));

    public static final Quest RUBBER_PLATE = CHAPTER.quest("rubber_plate", q -> q
            .at(40, 0)
            .milestone()
            .title("橡胶片")
            .task(Task.item(TagPrefix.plate, GTMaterials.Rubber, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.Rubber, 2)
            .dependsOn(RUBBER_INGOT, MOLDS_COPY_1));

    public static final Quest RED_ALLOY_WIRE = CHAPTER.quest("red_alloy_wire", q -> q
            .at(42, 4)
            .title("红色合金导线")
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.RedAlloy, 2))
            .scaledReward(TagPrefix.ingot, GTMaterials.RedAlloy, 2)
            .dependsOn(RED_ALLOY_COPY_1, WIRE_CUTTER_COPY_1));

    public static final Quest RED_ALLOY_CABLE = CHAPTER.quest("red_alloy_cable", q -> q
            .at(42, 2)
            .title("红色合金线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.RedAlloy, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.WroughtIron, 1)
            .dependsOn(RED_ALLOY_WIRE, RUBBER_PLATE));

    public static final Quest ULV_HULL = CHAPTER.quest("ulv_hull", q -> q
            .at(44, 0)
            .milestone()
            .title("ULV 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.ULV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.ULV], 1))
            .dependsOn(ULV_CASING, RED_ALLOY_CABLE));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NeolithicChapter.BRONZE_BRICK_HULL, 0, 0))
                .mainLine(NeolithicChapter.BRONZE_BRICK_HULL, SOLID_BOILER, ALLOY_SMELTER, BLAST_FURNACE, COMPRESSOR, PRIMITIVE_BLAST_FURNACE, STEEL, ANCIENT_REACTOR_CORE, ANCIENT_WORLD, STEAM_EXTRACTOR, RUBBER_PLATE, ULV_HULL));
    }
}
