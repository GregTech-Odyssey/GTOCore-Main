package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.common.registry.ModItems;

public final class ImmersiveAircraftChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("immersive_aircraft", c -> c
            .title("沉浸式飞行")
            .subtitle("Immersive Aircraft")
            .icon(QuestExternalRefs.ImmersiveAircraftItems.BIPLANE));

    public static final Quest GYRODYNE = CHAPTER.quest("gyrodyne", q -> q
            .at(0, 1.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.GYRODYNE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .optional());

    public static final Quest ROTARY_CANNON = CHAPTER.quest("rotary_cannon", q -> q
            .at(2, -0.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.ROTARY_CANNON))
            .reward(Reward.item(Items.COPPER_INGOT, 8), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .optional());

    public static final Quest HEAVY_CROSSBOW = CHAPTER.quest("heavy_crossbow", q -> q
            .at(-2, -0.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.HEAVY_CROSSBOW))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .optional());

    public static final Quest TELESCOPE = CHAPTER.quest("telescope", q -> q
            .at(-2, 1)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.TELESCOPE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .optional());

    public static final Quest BOMB_BAY = CHAPTER.quest("bomb_bay", q -> q
            .at(2, 1)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.BOMB_BAY))
            .reward(Reward.item(ModItems.STEEL_INGOT.get(), 4), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .optional());

    public static final Quest ENHANCED_PROPELLER = CHAPTER.quest("enhanced_propeller", q -> q
            .at(0, -2)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.ENHANCED_PROPELLER))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8), Reward.item(TagPrefix.ingot, GTMaterials.Bronze, 4))
            .optional());

    public static final Quest STEEL_BOILER = CHAPTER.quest("steel_boiler", q -> q
            .at(-3.5, 0)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.STEEL_BOILER))
            .reward(Reward.item(ModItems.STEEL_INGOT.get(), 8), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .optional());

    public static final Quest INDUSTRIAL_GEARS = CHAPTER.quest("industrial_gears", q -> q
            .at(-3.5, 1.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.INDUSTRIAL_GEARS))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.Bronze, 8), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .optional());

    public static final Quest STURDY_PIPES = CHAPTER.quest("sturdy_pipes", q -> q
            .at(-2.5, 3)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.STURDY_PIPES))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 32), Reward.item(ModItems.STEEL_INGOT.get(), 16))
            .optional());

    public static final Quest GYROSCOPE = CHAPTER.quest("gyroscope", q -> q
            .at(3, 3)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.GYROSCOPE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .optional());

    public static final Quest HULL_REINFORCEMENT = CHAPTER.quest("hull_reinforcement", q -> q
            .at(3.5, 1.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.HULL_REINFORCEMENT))
            .reward(Reward.item(Items.IRON_INGOT, 8), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .optional());

    public static final Quest IMPROVED_LANDING_GEAR = CHAPTER.quest("improved_landing_gear", q -> q
            .at(3.5, 0)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.IMPROVED_LANDING_GEAR))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.Invar, 8), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .optional());

    public static final Quest Q_1CE2E908 = CHAPTER.quest("q_1ce2e908", q -> q
            .at(0, 0)
            .icon(QuestExternalRefs.ImmersiveAircraftItems.BIPLANE)
            .desc(d -> d.line("本章节只是为了展示该模组的独特物品"))
            .task(Task.checkmark().title("沉浸式飞机"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8)));

    public static final Quest ENGINE = CHAPTER.quest("engine", q -> q
            .at(0, 3)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.ENGINE))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.RedAlloy, 8), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(Q_1CE2E908));

    public static final Quest ECO_ENGINE = CHAPTER.quest("eco_engine", q -> q
            .at(1, 4)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.ECO_ENGINE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 128L))
            .dependsOn(ENGINE)
            .optional());

    public static final Quest NETHER_ENGINE = CHAPTER.quest("nether_engine", q -> q
            .at(-1, 4)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.NETHER_ENGINE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .dependsOn(ENGINE)
            .optional());

    public static final Quest HULL = CHAPTER.quest("hull", q -> q
            .at(1.5, -2.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.HULL))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8), Reward.item(TagPrefix.ingot, GTMaterials.Bronze, 2))
            .dependsOn(Q_1CE2E908));

    public static final Quest SAIL = CHAPTER.quest("sail", q -> q
            .at(0, -3.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.SAIL))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1), Reward.item(Items.WHITE_WOOL, 4))
            .dependsOn(Q_1CE2E908));

    public static final Quest PROPELLER = CHAPTER.quest("propeller", q -> q
            .at(-1.5, -2.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.PROPELLER))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8), Reward.item(Items.IRON_INGOT, 8))
            .dependsOn(Q_1CE2E908));

    public static final Quest AIRSHIP = CHAPTER.quest("airship", q -> q
            .at(0, 5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.AIRSHIP))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(ENGINE));

    public static final Quest CARGO_AIRSHIP = CHAPTER.quest("cargo_airship", q -> q
            .at(0, 6.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.CARGO_AIRSHIP))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(AIRSHIP));

    public static final Quest WARSHIP = CHAPTER.quest("warship", q -> q
            .at(0, 8)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.WARSHIP))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(CARGO_AIRSHIP));

    public static final Quest BIPLANE = CHAPTER.quest("biplane", q -> q
            .at(-1.5, 2.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.BIPLANE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(ENGINE)
            .optional());

    public static final Quest QUADROCOPTER = CHAPTER.quest("quadrocopter", q -> q
            .at(1.5, 2.5)
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.QUADROCOPTER))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(ENGINE)
            .optional());

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.BIPLANE, 0.5, 2, 10, 10).alpha(100)));
    }
}
