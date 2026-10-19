package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.task.Task;

import static com.gtolib.api.quest.QuestStyle.*;

public final class MassiveManufacturingChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("massive_manufacturing", c -> c
            .title("大批量加工")
            .subtitle("Massive Manufacturing")
            .icon(GCYMMachines.LARGE_AUTOCLAVE));

    public static final Quest LARGE_MIXER = CHAPTER.quest("large_mixer", q -> q
            .at(-3, 1)
            .task(Task.item(GCYMMachines.LARGE_MIXER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_ARC_SMELTER = CHAPTER.quest("large_arc_smelter", q -> q
            .at(-2, -2)
            .task(Task.item(GCYMMachines.LARGE_ARC_SMELTER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_CIRCUIT_ASSEMBLER = CHAPTER.quest("large_circuit_assembler", q -> q
            .at(-4, -6)
            .task(Task.item(GCYMMachines.LARGE_CIRCUIT_ASSEMBLER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_BREWER = CHAPTER.quest("large_brewer", q -> q
            .at(-2, -8)
            .task(Task.item(GCYMMachines.LARGE_BREWER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_EXTRUDER = CHAPTER.quest("large_extruder", q -> q
            .at(-3, -1)
            .task(Task.item(GCYMMachines.LARGE_EXTRUDER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_EXTRACTOR = CHAPTER.quest("large_extractor", q -> q
            .at(-1, -3)
            .task(Task.item(GCYMMachines.LARGE_EXTRACTOR))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_DISTILLERY = CHAPTER.quest("large_distillery", q -> q
            .at(-5, -3)
            .task(Task.item(GCYMMachines.LARGE_DISTILLERY))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_MATERIAL_PRESS = CHAPTER.quest("large_material_press", q -> q
            .at(-5, -1)
            .task(Task.item(GCYMMachines.LARGE_MATERIAL_PRESS))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_AUTOCLAVE = CHAPTER.quest("large_autoclave", q -> q
            .at(-3, -3)
            .task(Task.item(GCYMMachines.LARGE_AUTOCLAVE))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_ENGRAVING_LASER = CHAPTER.quest("large_engraving_laser", q -> q
            .at(-4, -4)
            .task(Task.item(GCYMMachines.LARGE_ENGRAVING_LASER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_ASSEMBLER = CHAPTER.quest("large_assembler", q -> q
            .at(-2, -6)
            .task(Task.item(GCYMMachines.LARGE_ASSEMBLER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_PACKER = CHAPTER.quest("large_packer", q -> q
            .at(-2, 2)
            .task(Task.item(GCYMMachines.LARGE_PACKER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_ELECTROLYZER = CHAPTER.quest("large_electrolyzer", q -> q
            .at(-3, -7)
            .task(Task.item(GCYMMachines.LARGE_ELECTROLYZER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_PYROLYSE_OVEN = CHAPTER.quest("large_pyrolyse_oven", q -> q
            .at(-13, -3)
            .task(Task.item(MultiBlockA.LARGE_PYROLYSE_OVEN))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest Q_68C24A1E = CHAPTER.quest("q_68c24a1e", q -> q
            .at(-10, -3)
            .desc(d -> d
                    .line("喜欢吗？多方块可是GT的拿手好戏，请{IV}再来探索吧！", IV)
                    .line()
                    .line("这里所有的机器都可以安装{能量升级模块}和{速度升级模块}，除了左边这两台", GOLD, GOLD))
            .task(Task.checkmark().title("大批量加工")));

    public static final Quest LARGE_ROLLING = CHAPTER.quest("large_rolling", q -> q
            .at(-1, -5)
            .task(Task.item(GCYMMachines.LARGE_ROLLING))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_BENDER = CHAPTER.quest("large_bender", q -> q
            .at(-5, -5)
            .task(Task.item(GCYMMachines.LARGE_BENDER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_ARC_GENERATOR = CHAPTER.quest("large_arc_generator", q -> q
            .at(-1, -7)
            .task(Task.item(GCYMMachines.LARGE_ARC_GENERATOR))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_ELECTROMAGNET = CHAPTER.quest("large_electromagnet", q -> q
            .at(-5, -7)
            .task(Task.item(GCYMMachines.LARGE_ELECTROMAGNET))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_CUTTER = CHAPTER.quest("large_cutter", q -> q
            .at(-4, 2)
            .task(Task.item(GCYMMachines.LARGE_CUTTER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_FORMING = CHAPTER.quest("large_forming", q -> q
            .at(-1, -1)
            .task(Task.item(GCYMMachines.LARGE_FORMING))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_SOLIDIFIER = CHAPTER.quest("large_solidifier", q -> q
            .at(-7, -3)
            .task(Task.item(GCYMMachines.LARGE_SOLIDIFIER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_CHEMICAL_BATH = CHAPTER.quest("large_chemical_bath", q -> q
            .at(1, -3)
            .task(Task.item(GCYMMachines.LARGE_CHEMICAL_BATH))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_CENTRIFUGE = CHAPTER.quest("large_centrifuge", q -> q
            .at(-4, 0)
            .task(Task.item(GCYMMachines.LARGE_CENTRIFUGE))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_MACERATION_TOWER = CHAPTER.quest("large_maceration_tower", q -> q
            .at(-5, 1)
            .task(Task.item(GCYMMachines.LARGE_MACERATION_TOWER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_LASER_WELDER = CHAPTER.quest("large_laser_welder", q -> q
            .at(-2, -4)
            .task(Task.item(GCYMMachines.LARGE_LASER_WELDER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_LAMINATOR = CHAPTER.quest("large_laminator", q -> q
            .at(-4, -2)
            .task(Task.item(GCYMMachines.LARGE_LAMINATOR))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_WIREMILL = CHAPTER.quest("large_wiremill", q -> q
            .at(-3, -5)
            .task(Task.item(GCYMMachines.LARGE_WIREMILL))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_SIFTING_FUNNEL = CHAPTER.quest("large_sifting_funnel", q -> q
            .at(-2, 0)
            .task(Task.item(GCYMMachines.LARGE_SIFTING_FUNNEL))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_CRUSHER = CHAPTER.quest("large_crusher", q -> q
            .at(-1, 1)
            .task(Task.item(GCYMMachines.LARGE_CRUSHER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest LARGE_ROCK_CRUSHER = CHAPTER.quest("large_rock_crusher", q -> q
            .at(-4, -8)
            .task(Task.item(GCYMMachines.LARGE_ROCK_CRUSHER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest VACUUM_DRYING_FURNACE = CHAPTER.quest("vacuum_drying_furnace", q -> q
            .at(-15, -3)
            .task(Task.item(MultiBlockA.VACUUM_DRYING_FURNACE))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -9.54, -3.5, 2, 2)));
    }
}
