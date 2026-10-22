package com.gtocore.data.quest.chapter;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class TitlesChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("titles", c -> c
            .title("称号")
            .subtitle("Titles")
            .icon(Items.NAME_TAG)
            .disableToast());

    public static final Quest TITLE_SYSTEM = CHAPTER.quest("title_system", q -> q
            .at(-6.5, -0.5)
            .title("称号系统")
            .desc(d -> d
                    .line("每当你到达新的游戏阶段，都会自动解锁对应阶段电压的称号（不过你需要手动在本章节点击切换）")
                    .line()
                    .line("你可以佩戴这些称号，它们会在聊天栏里作为名字的前缀出现")
                    .line()
                    .line("{致谢：来自玩家 @xiaokuc666 提供的蓝图}", GRAY.italic()))
            .task(Task.checkmark())
            .hideDependentLines());

    public static final Quest TITLE_SYSTEM_INITIALIZATION = CHAPTER.quest("title_system_initialization", q -> q
            .at(-4, -2)
            .title("称号系统初始化")
            .desc(d -> d
                    .line("为所有玩家初始化称号系统，多人游戏里只需点击一次即可")
                    .line()
                    .line("初始化称号系统后才可取得称号"))
            .task(Task.checkmark().title("称号系统初始化"))
            .reward(Reward.command("/function titles:setup").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM)
            .repeatable());

    public static final Quest TITLE_SYSTEM_REMOVE = CHAPTER.quest("title_system_remove", q -> q
            .at(-2, -2)
            .title("称号系统删除")
            .desc(d -> d
                    .line("{删除称号系统，所有玩家的称号都会被删除，同时不能取得新的称号}", RED)
                    .line()
                    .line("必须拥有作弊权限才可执行")
                    .line()
                    .line("如果只是想清除自己的称号，请点击{此任务}", link(TitlesChapter.Q_4E6CF4DF)))
            .task(Task.checkmark().title("称号系统队伍删除"))
            .reward(Reward.command("/function titles:remove").silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM)
            .repeatable());

    public static final Quest Q_49943789 = CHAPTER.quest("q_49943789", q -> q
            .at(-4, -0.5)
            .icon(Items.COBBLESTONE)
            .desc(d -> d
                    .line("前往{称号}章节解锁称号系统", link(TITLE_SYSTEM))
                    .line()
                    .line("这是一个可选系统，会让你的名字多出一个电压前缀"))
            .task(Task.checkmark().title("获得称号:\"石器时代\""))
            .reward(Reward.command("/team join STONE @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(GregtechOdysseyChapter.HELLO_WORLD, TITLE_SYSTEM)
            .repeatable());

    public static final Quest Q_3BE8790F = CHAPTER.quest("q_3be8790f", q -> q
            .at(-3, -0.5)
            .icon(TagPrefix.ingot, GTMaterials.Bronze)
            .task(Task.checkmark().title("获得称号:\"蒸汽时代\""))
            .reward(Reward.command("/team join STEAM @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, SteamChapter.MISTY_MOVEMENT)
            .repeatable());

    public static final Quest Q_16D11BE4 = CHAPTER.quest("q_16d11be4", q -> q
            .at(-2, -0.5)
            .icon(GTMachines.HULL[GTValues.ULV])
            .task(Task.checkmark().title("获得称号:\"超低压时代\""))
            .reward(Reward.command("/team join ULV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, UlvChapter.WELCOME_TO_THE_ULTRA_LOW_PRESSURE)
            .repeatable());

    public static final Quest Q_1B1C35B5 = CHAPTER.quest("q_1b1c35b5", q -> q
            .at(-1, -0.5)
            .icon(GTMachines.HULL[GTValues.LV])
            .task(Task.checkmark().title("获得称号:\"低压时代\""))
            .reward(Reward.command("/team join LV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, LvChapter.WELCOME_TO_THE_LOW_VOLTAGE_STAGE)
            .repeatable());

    public static final Quest Q_6D9F90AE = CHAPTER.quest("q_6d9f90ae", q -> q
            .at(0, -0.5)
            .icon(GTMachines.HULL[GTValues.MV])
            .task(Task.checkmark().title("获得称号:\"中压时代\""))
            .reward(Reward.command("/team join MV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, MvChapter.WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .repeatable());

    public static final Quest Q_0B7B9411 = CHAPTER.quest("q_0b7b9411", q -> q
            .at(1, -0.5)
            .icon(GTMachines.HULL[GTValues.HV])
            .task(Task.checkmark().title("获得称号:\"高压时代\""))
            .reward(Reward.command("/team join HV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, HvChapter.WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .repeatable());

    public static final Quest Q_1518B60C = CHAPTER.quest("q_1518b60c", q -> q
            .at(2, -0.5)
            .icon(GTMachines.HULL[GTValues.EV])
            .task(Task.checkmark().title("获得称号:\"超高压时代\""))
            .reward(Reward.command("/team join EV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, EvChapter.WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .repeatable());

    public static final Quest Q_2E53D515 = CHAPTER.quest("q_2e53d515", q -> q
            .at(3, -0.5)
            .icon(GTMachines.HULL[GTValues.IV])
            .task(Task.checkmark().title("获得称号:\"强导压时代\""))
            .reward(Reward.command("/team join IV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE)
            .repeatable());

    public static final Quest Q_2060E87F = CHAPTER.quest("q_2060e87f", q -> q
            .at(-4, 0.5)
            .icon(GTMachines.HULL[GTValues.LuV])
            .task(Task.checkmark().title("获得称号:\"剧差压时代\""))
            .reward(Reward.command("/team join LUV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, LuvChapter.WELCOME_TO_THE_DRAMA_PRESSURE_STAGE)
            .repeatable());

    public static final Quest Q_6E340171 = CHAPTER.quest("q_6e340171", q -> q
            .at(-3, 0.5)
            .icon(GTMachines.HULL[GTValues.ZPM])
            .task(Task.checkmark().title("获得称号:\"零点压时代\""))
            .reward(Reward.command("/team join ZPM @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, ZpmChapter.WELCOME_TO_THE_ZERO_PRESSURE_PHASE)
            .repeatable());

    public static final Quest Q_4B27A758 = CHAPTER.quest("q_4b27a758", q -> q
            .at(-2, 0.5)
            .icon(GTMachines.HULL[GTValues.UV])
            .task(Task.checkmark().title("获得称号:\"极限压时代\""))
            .reward(Reward.command("/team join UV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, UvChapter.WELCOME_TO_THE_EXTREME_PRESSURE_STAGE)
            .repeatable());

    public static final Quest Q_4AE5EC18 = CHAPTER.quest("q_4ae5ec18", q -> q
            .at(-1, 0.5)
            .icon(GTMachines.HULL[GTValues.UHV])
            .task(Task.checkmark().title("获得称号:\"极高压时代\""))
            .reward(Reward.command("/team join UHV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, UhvChapter.WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE)
            .repeatable());

    public static final Quest Q_7D60930D = CHAPTER.quest("q_7d60930d", q -> q
            .at(0, 0.5)
            .icon(GTMachines.HULL[GTValues.UEV])
            .task(Task.checkmark().title("获得称号:\"极超压时代\""))
            .reward(Reward.command("/team join UEV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, UevChapter.WELCOME_TO_THE_EXTREME_OVERPRESSURE_STAGE)
            .repeatable());

    public static final Quest Q_77001F4C = CHAPTER.quest("q_77001f4c", q -> q
            .at(1, 0.5)
            .icon(GTMachines.HULL[GTValues.UIV])
            .task(Task.checkmark().title("获得称号:\"极巨压时代\""))
            .reward(Reward.command("/team join UIV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, UevChapter.EXOTIC_MAINFRAME)
            .repeatable());

    public static final Quest Q_182E5E6B = CHAPTER.quest("q_182e5e6b", q -> q
            .at(2, 0.5)
            .icon(GTMachines.HULL[GTValues.UXV])
            .task(Task.checkmark().title("获得称号:\"极顶压时代\""))
            .reward(Reward.command("/team join UXV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, UevChapter.COSMIC_MAINFRAME)
            .repeatable());

    public static final Quest Q_62D2B7F6 = CHAPTER.quest("q_62d2b7f6", q -> q
            .at(3, 0.5)
            .icon(GTMachines.HULL[GTValues.OpV])
            .task(Task.checkmark().title("获得称号:\"过载压时代\""))
            .reward(Reward.command("/team join OPV @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, UevChapter.SUPRACAUSAL_MAINFRAME)
            .repeatable());

    public static final Quest Q_3BCF277D = CHAPTER.quest("q_3bcf277d", q -> q
            .at(-4, 1.5)
            .icon(GTMachines.HULL[GTValues.MAX])
            .task(Task.checkmark().title("获得称号:\"上限压时代\""))
            .reward(Reward.command("/team join MAX @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM, UevChapter.SUPRACHRONAL_CIRCUIT_MAX)
            .repeatable());

    public static final Quest Q_4E6CF4DF = CHAPTER.quest("q_4e6cf4df", q -> q
            .at(0, -2)
            .desc(d -> d.line("清除自己的称号"))
            .task(Task.checkmark().title("清除自己的称号"))
            .reward(Reward.command("/team leave @s").elevated().silent().autoClaim(AutoClaim.NO_TOAST))
            .dependsOn(TITLE_SYSTEM)
            .repeatable());
}
