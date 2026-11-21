package com.gtocore.data.quest;

import com.gtocore.data.quest.chapter.AeChapter;
import com.gtocore.data.quest.chapter.ApotheosisChapter;
import com.gtocore.data.quest.chapter.BackpackChapter;
import com.gtocore.data.quest.chapter.BeyondTheStarsChapter;
import com.gtocore.data.quest.chapter.ChemicalChapter;
import com.gtocore.data.quest.chapter.EnergyProducingChapter;
import com.gtocore.data.quest.chapter.EvChapter;
import com.gtocore.data.quest.chapter.FoodChapter;
import com.gtocore.data.quest.chapter.GeneratorChapter;
import com.gtocore.data.quest.chapter.GregtechOdysseyChapter;
import com.gtocore.data.quest.chapter.HiEndManufaturingChapter;
import com.gtocore.data.quest.chapter.HvChapter;
import com.gtocore.data.quest.chapter.ImmersiveAircraftChapter;
import com.gtocore.data.quest.chapter.IvChapter;
import com.gtocore.data.quest.chapter.LuvChapter;
import com.gtocore.data.quest.chapter.LvChapter;
import com.gtocore.data.quest.chapter.Magic1Chapter;
import com.gtocore.data.quest.chapter.Magic2Chapter;
import com.gtocore.data.quest.chapter.MassiveManufacturingChapter;
import com.gtocore.data.quest.chapter.MvChapter;
import com.gtocore.data.quest.chapter.OpvChapter;
import com.gtocore.data.quest.chapter.OreProcessingChapter;
import com.gtocore.data.quest.chapter.OresChapter;
import com.gtocore.data.quest.chapter.PeriodicTableChapter;
import com.gtocore.data.quest.chapter.ProgressChapter;
import com.gtocore.data.quest.chapter.RareDustChapter;
import com.gtocore.data.quest.chapter.ShopChapter;
import com.gtocore.data.quest.chapter.SopDatabaseChapter;
import com.gtocore.data.quest.chapter.SpaceStationChapter;
import com.gtocore.data.quest.chapter.SteamChapter;
import com.gtocore.data.quest.chapter.StoneageChapter;
import com.gtocore.data.quest.chapter.TechnodeChapter;
import com.gtocore.data.quest.chapter.TechtreeChapter;
import com.gtocore.data.quest.chapter.TechtreeFtbqChapter;
import com.gtocore.data.quest.chapter.TipsChapter;
import com.gtocore.data.quest.chapter.TitlesChapter;
import com.gtocore.data.quest.chapter.UevChapter;
import com.gtocore.data.quest.chapter.UhvChapter;
import com.gtocore.data.quest.chapter.UivChapter;
import com.gtocore.data.quest.chapter.UlvChapter;
import com.gtocore.data.quest.chapter.UtilChapter;
import com.gtocore.data.quest.chapter.UvChapter;
import com.gtocore.data.quest.chapter.UxvChapter;
import com.gtocore.data.quest.chapter.WirelessEnergyChapter;
import com.gtocore.data.quest.chapter.ZpmChapter;

import com.gtolib.api.quest.QuestBook;
import com.gtolib.api.quest.QuestBookSettings;
import com.gtolib.api.quest.QuestGroup;

public final class QuestBookContents {

    public static final QuestBookSettings SETTINGS = QuestBookSettings.of(s -> s
            .title("GregTech: Odyssey")
            .defaultRewardTeam(true)
            .showLockIcons(false));

    public static final QuestGroup ROOT = QuestGroup.root(g -> g
            .chapters(GregtechOdysseyChapter.CHAPTER, TipsChapter.CHAPTER)
            .devChapters(SopDatabaseChapter.CHAPTER, UtilChapter.CHAPTER)
            .chapters(ShopChapter.CHAPTER));

    public static final QuestGroup MAIN = QuestGroup.of("main", g -> g
            .title("主线")
            .chapters(
                    StoneageChapter.CHAPTER,
                    SteamChapter.CHAPTER,
                    UlvChapter.CHAPTER,
                    LvChapter.CHAPTER,
                    MvChapter.CHAPTER,
                    HvChapter.CHAPTER,
                    EvChapter.CHAPTER,
                    IvChapter.CHAPTER,
                    LuvChapter.CHAPTER,
                    ZpmChapter.CHAPTER,
                    UvChapter.CHAPTER,
                    UhvChapter.CHAPTER,
                    UevChapter.CHAPTER,
                    UivChapter.CHAPTER,
                    UxvChapter.CHAPTER,
                    OpvChapter.CHAPTER));

    public static final QuestGroup TECH_TREE = QuestGroup.of("tech_tree", g -> g
            .title("科技树")
            .chapters(
                    TechtreeChapter.CHAPTER,
                    TechtreeFtbqChapter.CHAPTER,
                    TechnodeChapter.CHAPTER));

    public static final QuestGroup MULTIBLOCK = QuestGroup.of("multiblock", g -> g
            .title("多方块机器")
            .chapters(
                    MassiveManufacturingChapter.CHAPTER,
                    HiEndManufaturingChapter.CHAPTER,
                    BeyondTheStarsChapter.CHAPTER,
                    SpaceStationChapter.CHAPTER,
                    EnergyProducingChapter.CHAPTER));

    public static final QuestGroup SELF_SUFFICIENT = QuestGroup.of("self_sufficient", g -> g
            .title("独善其身")
            .chapters(
                    AeChapter.CHAPTER,
                    Magic1Chapter.CHAPTER,
                    Magic2Chapter.CHAPTER,
                    ApotheosisChapter.CHAPTER,
                    BackpackChapter.CHAPTER,
                    FoodChapter.CHAPTER,
                    ImmersiveAircraftChapter.CHAPTER));

    public static final QuestGroup RESOURCES = QuestGroup.of("resources", g -> g
            .title("资源与产线")
            .chapters(
                    OreProcessingChapter.CHAPTER,
                    WirelessEnergyChapter.CHAPTER,
                    GeneratorChapter.CHAPTER,
                    ChemicalChapter.CHAPTER,
                    OresChapter.CHAPTER,
                    RareDustChapter.CHAPTER));

    public static final QuestGroup MILESTONE = QuestGroup.of("milestone", g -> g
            .title("里程碑")
            .chapters(
                    TitlesChapter.CHAPTER,
                    ProgressChapter.CHAPTER,
                    PeriodicTableChapter.CHAPTER));

    private QuestBookContents() {}

    public static void init() {
        QuestRewardTables.init();
        QuestBook.build();
    }
}
