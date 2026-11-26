package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.monitor.MonitorRegistries;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.task.Task;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.registries.BuiltInRegistries;

import com.hepdd.gtmthings.data.CustomItems;
import com.hepdd.gtmthings.data.WirelessMachines;

import static com.gtolib.api.quest.QuestStyle.*;

public final class WirelessEnergyChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("wireless_energy", c -> c
            .title("无线电网")
            .subtitle("Wireless Energy Net")
            .icon(GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.MAX])
            .autofocus(WirelessEnergyChapter.Q_3D4EDD8B));

    public static final Quest WIRELESS_ENERGY_BINDING_TOOL = CHAPTER.quest("wireless_energy_binding_tool", q -> q
            .at(-4.5, 1.5)
            .icon(CustomItems.WIRELESS_ENERGY_TERMINAL)
            .desc(d -> d
                    .line("得益于GTMThings提供的{无线能源系统}，格雷员工不再需要布置意大利面能源网络", YELLOW)
                    .line()
                    .line("一个功能完整的无线电网包含以下部分：")
                    .line()
                    .line("- {电力储存与总I/O功率限制}：{无线能源塔}", AQUA, YELLOW)
                    .line()
                    .line("{- }{输入能量}：{无线动力仓 / 无线能源接口}", YELLOW, AQUA, YELLOW)
                    .line()
                    .line("{- }{输出能量}：{无线能源仓 / 无线能源接收器}", YELLOW, AQUA, YELLOW)
                    .line()
                    .line("{-} {跨纬度输电}：{无线电网维度中继器}", YELLOW, AQUA, YELLOW))
            .task(
                    Task.item(MultiBlockG.WIRELESS_ENERGY_SUBSTATION), Task.item(BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtmthings", "wireless_energy_terminal"))).optional())
            .dependsOn(MvChapter.Q_2386117F));

    public static final Quest Q_1506E8C5 = CHAPTER.quest("q_1506e8c5", q -> q
            .at(-2.5, 1.5)
            .desc(d -> d
                    .line("给单方块机器使用的覆盖板，每台机器只能安装一个")
                    .line()
                    .line("可以安装到电池箱上"))
            .task(
                    Task.anyOf(1, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_EV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_EV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_IV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_IV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LUV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LUV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_MV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_MV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_OPV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_OPV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UEV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UEV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UHV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UHV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UIV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UIV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UXV_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UXV, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_ZPM_4A, CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_ZPM, GTOItems.WIRELESS_ENERGY_RECEIVE_COVER_MAX_4A, GTOItems.WIRELESS_ENERGY_RECEIVE_COVER_MAX).title("任意无线能源接收器"))
            .dependsOn(WIRELESS_ENERGY_BINDING_TOOL));

    public static final Quest WIRELESS_ENERGY_INTERFACE = CHAPTER.quest("wireless_energy_interface", q -> q
            .at(-3, 0)
            .desc(d -> d
                    .line("{无线能源接口}负责接收发电机器的电力并发送至电网", GOLD)
                    .line()
                    .line("通过{无线能源接口}输入电网的电量有{10%}的能量损失", GOLD, DARK_GREEN)
                    .line()
                    .line(" 只有无法使用无线动力仓的发电设施（比如单方块发电机）才需要使用这个"))
            .task(Task.item(WirelessMachines.WIRELESS_ENERGY_INTERFACE))
            .dependsOn(WIRELESS_ENERGY_BINDING_TOOL));

    public static final Quest WIRELESS_ENERGY_MONITOR = CHAPTER.quest("wireless_energy_monitor", q -> q
            .at(1, 3).minWidth(250)
            .desc(d -> d
                    .line("顾名思义，用来监控无线电网的运行状态")
                    .line()
                    .line("监控器的界面主要显示的信息：")
                    .line()
                    .line("·能源总量：由{无线能源塔}的{能量单元}{等级和数量}决定", GOLD, GOLD, YELLOW)
                    .line("·单次传输上限：由{电池箱}或{蓄能变电站}的{传输上限}决定", GOLD, GOLD, YELLOW)
                    .line("·净功率")
                    .line("    ·正净功率：表示系统在发电，电能流入电网")
                    .line("    ·负净功率：表示系统在用电，电能流出电网")
                    .line()
                    .line("{显示器}和{无线能源终端}也可以用来查看无线电网的状态", GOLD, GOLD))
            .task(Task.item(WirelessMachines.WIRELESS_ENERGY_MONITOR), Task.item(MonitorRegistries.DISPLAY_PANEL.get()))
            .dependsOn(WirelessEnergyChapter.LV_WIRELESS_ENERGY_UNIT));

    public static final Quest Q_2F9D9F92 = CHAPTER.quest("q_2f9d9f92", q -> q
            .at(-3, 3)
            .desc(d -> d
                    .line("无线电网的能源储存需要载体")
                    .line()
                    .line("{无线能源塔}和{无线能量单元}提供了相应功能，你可以拥有多座该结构", GOLD, GOLD)
                    .line()
                    .line("同一纬度多座{无线能源塔}的容量、存量和总吞吐相加", GOLD))
            .task(Task.checkmark().title("储能上限"))
            .dependsOn(WIRELESS_ENERGY_BINDING_TOOL));

    public static final Quest LV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("lv_wireless_energy_unit", q -> q
            .at(-1, 3)
            .task(Task.item(GTOBlocks.LV_WIRELESS_ENERGY_UNIT))
            .requireAnyStarted()
            .dependsOn(Q_2F9D9F92));

    public static final Quest MV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("mv_wireless_energy_unit", q -> q
            .at(-1, 4)
            .task(Task.item(GTOBlocks.MV_WIRELESS_ENERGY_UNIT))
            .dependsOn(LV_WIRELESS_ENERGY_UNIT));

    public static final Quest HV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("hv_wireless_energy_unit", q -> q
            .at(-1, 5)
            .task(Task.item(GTOBlocks.HV_WIRELESS_ENERGY_UNIT))
            .dependsOn(MV_WIRELESS_ENERGY_UNIT));

    public static final Quest EV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("ev_wireless_energy_unit", q -> q
            .at(-1, 6)
            .task(Task.item(GTOBlocks.EV_WIRELESS_ENERGY_UNIT))
            .dependsOn(HV_WIRELESS_ENERGY_UNIT));

    public static final Quest Q_08280145 = CHAPTER.quest("q_08280145", q -> q
            .at(-6, 3)
            .subtitle("是整个电网的传输上限！！")
            .desc(d -> d
                    .line("电网内所有{用电设备共用输出上限}，所有{发电设备共用输入上限}", RED, RED)
                    .line()
                    .line("{能量单元}提供{4A}对应电压的输入输出上限", GOLD, GOLD)
                    .line()
                    .line("{传输单元}提供{96A}对应电压的输入输出上限", GREEN, GREEN))
            .task(Task.checkmark().title("传输速率上限"))
            .dependsOn(WIRELESS_ENERGY_BINDING_TOOL));

    public static final Quest IV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("iv_wireless_energy_unit", q -> q
            .at(-1, 7)
            .task(Task.item(GTOBlocks.IV_WIRELESS_ENERGY_UNIT))
            .dependsOn(EV_WIRELESS_ENERGY_UNIT));

    public static final Quest LUV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("luv_wireless_energy_unit", q -> q
            .at(-1, 8)
            .task(Task.item(GTOBlocks.LUV_WIRELESS_ENERGY_UNIT))
            .dependsOn(IV_WIRELESS_ENERGY_UNIT));

    public static final Quest ZPM_WIRELESS_ENERGY_UNIT = CHAPTER.quest("zpm_wireless_energy_unit", q -> q
            .at(-1, 9)
            .task(Task.item(GTOBlocks.ZPM_WIRELESS_ENERGY_UNIT))
            .dependsOn(LUV_WIRELESS_ENERGY_UNIT));

    public static final Quest UV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uv_wireless_energy_unit", q -> q
            .at(-1, 10)
            .task(Task.item(GTOBlocks.UV_WIRELESS_ENERGY_UNIT))
            .dependsOn(ZPM_WIRELESS_ENERGY_UNIT));

    public static final Quest UHV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uhv_wireless_energy_unit", q -> q
            .at(-1, 11)
            .task(Task.item(GTOBlocks.UHV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UV_WIRELESS_ENERGY_UNIT));

    public static final Quest UEV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uev_wireless_energy_unit", q -> q
            .at(-1, 12)
            .task(Task.item(GTOBlocks.UEV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UHV_WIRELESS_ENERGY_UNIT));

    public static final Quest UIV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uiv_wireless_energy_unit", q -> q
            .at(-1, 13)
            .task(Task.item(GTOBlocks.UIV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UEV_WIRELESS_ENERGY_UNIT));

    public static final Quest UXV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uxv_wireless_energy_unit", q -> q
            .at(-1, 14)
            .task(Task.item(GTOBlocks.UXV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UIV_WIRELESS_ENERGY_UNIT));

    public static final Quest OPV_WIRELESS_ENERGY_UNIT = CHAPTER.quest("opv_wireless_energy_unit", q -> q
            .at(-1, 15)
            .task(Task.item(GTOBlocks.OPV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UXV_WIRELESS_ENERGY_UNIT));

    public static final Quest MAX_WIRELESS_ENERGY_UNIT = CHAPTER.quest("max_wireless_energy_unit", q -> q
            .at(-1, 16)
            .task(Task.item(GTOBlocks.MAX_WIRELESS_ENERGY_UNIT))
            .dependsOn(OPV_WIRELESS_ENERGY_UNIT));

    public static final Quest TEMPERED_GLASS = CHAPTER.quest("tempered_glass", q -> q
            .at(-4.5, 4)
            .title("玻璃等级")
            .subtitle("何原理")
            .desc(d -> d
                    .line("你需要注意升级无线电网的玻璃")
                    .line()
                    .line("{超出玻璃等级的单元是不生效的}", RED))
            .task(Task.item(GTBlocks.CASING_TEMPERED_GLASS))
            .dependsOn(WIRELESS_ENERGY_BINDING_TOOL));

    public static final Quest BOROSILICATE_GLASS = CHAPTER.quest("borosilicate_glass", q -> q
            .at(-4.5, 5)
            .task(Task.item(GTOBlocks.BOROSILICATE_GLASS))
            .dependsOn(TEMPERED_GLASS));

    public static final Quest TITANIUM_BOROSILICATE_GLASS = CHAPTER.quest("titanium_borosilicate_glass", q -> q
            .at(-4.5, 6)
            .task(Task.item(GTOBlocks.TITANIUM_BOROSILICATE_GLASS))
            .dependsOn(BOROSILICATE_GLASS));

    public static final Quest TUNGSTEN_BOROSILICATE_GLASS = CHAPTER.quest("tungsten_borosilicate_glass", q -> q
            .at(-4.5, 7)
            .task(Task.item(GTOBlocks.TUNGSTEN_BOROSILICATE_GLASS))
            .dependsOn(TITANIUM_BOROSILICATE_GLASS));

    public static final Quest HSSS_BOROSILICATE_GLASS = CHAPTER.quest("hsss_borosilicate_glass", q -> q
            .at(-4.5, 8)
            .task(Task.item(GTOBlocks.HSSS_BOROSILICATE_GLASS))
            .dependsOn(TUNGSTEN_BOROSILICATE_GLASS));

    public static final Quest NAQUADAH_BOROSILICATE_GLASS = CHAPTER.quest("naquadah_borosilicate_glass", q -> q
            .at(-4.5, 9)
            .task(Task.item(GTOBlocks.NAQUADAH_BOROSILICATE_GLASS))
            .dependsOn(HSSS_BOROSILICATE_GLASS));

    public static final Quest TRITANIUM_BOROSILICATE_GLASS = CHAPTER.quest("tritanium_borosilicate_glass", q -> q
            .at(-4.5, 10)
            .task(Task.item(GTOBlocks.TRITANIUM_BOROSILICATE_GLASS))
            .dependsOn(NAQUADAH_BOROSILICATE_GLASS));

    public static final Quest AMPROSIUM_BOROSILICATE_GLASS = CHAPTER.quest("amprosium_borosilicate_glass", q -> q
            .at(-4.5, 11)
            .task(Task.item(GTOBlocks.AMPROSIUM_BOROSILICATE_GLASS))
            .dependsOn(TRITANIUM_BOROSILICATE_GLASS));

    public static final Quest ENDERIUM_BOROSILICATE_GLASS = CHAPTER.quest("enderium_borosilicate_glass", q -> q
            .at(-4.5, 12)
            .task(Task.item(GTOBlocks.ENDERIUM_BOROSILICATE_GLASS))
            .dependsOn(AMPROSIUM_BOROSILICATE_GLASS));

    public static final Quest TARANIUM_BOROSILICATE_GLASS = CHAPTER.quest("taranium_borosilicate_glass", q -> q
            .at(-4.5, 13)
            .task(Task.item(GTOBlocks.TARANIUM_BOROSILICATE_GLASS))
            .dependsOn(ENDERIUM_BOROSILICATE_GLASS));

    public static final Quest QUARKS_BOROSILICATE_GLASS = CHAPTER.quest("quarks_borosilicate_glass", q -> q
            .at(-4.5, 14)
            .task(Task.item(GTOBlocks.QUARKS_BOROSILICATE_GLASS))
            .dependsOn(TARANIUM_BOROSILICATE_GLASS));

    public static final Quest DRACONIUM_BOROSILICATE_GLASS = CHAPTER.quest("draconium_borosilicate_glass", q -> q
            .at(-4.5, 15)
            .task(Task.item(GTOBlocks.DRACONIUM_BOROSILICATE_GLASS))
            .dependsOn(QUARKS_BOROSILICATE_GLASS));

    public static final Quest COSMIC_NEUTRONIUM_BOROSILICATE_GLASS = CHAPTER.quest("cosmic_neutronium_borosilicate_glass", q -> q
            .at(-4.5, 16)
            .task(Task.item(GTOBlocks.COSMIC_NEUTRONIUM_BOROSILICATE_GLASS))
            .dependsOn(DRACONIUM_BOROSILICATE_GLASS));

    public static final Quest Q_3D4EDD8B = CHAPTER.quest("q_3d4edd8b", q -> q
            .at(-4.5, -1)
            .desc(d -> d
                    .line("得益于新无线电网机制的改动，你可以通过无线电网主动发出的红石信号来控制电网{供给侧/需求侧}机器的{启停}", YELLOW, LIGHT_PURPLE)
                    .line()
                    .line("在相当程度上可以实现{智能化发电和生产}，减少资源浪费！", LIGHT_PURPLE)
                    .line()
                    .line("{在无线能源塔主方块上安装能量探测覆盖板，可以根据无线电网电量百分比发出红石信号}", YELLOW))
            .task(Task.checkmark().title("自动化控制"))
            .dependsOn(WIRELESS_ENERGY_BINDING_TOOL));

    public static final Quest ENERGY_DETECTOR_COVER = CHAPTER.quest("energy_detector_cover", q -> q
            .at(-6, -2)
            .desc(d -> d.line("安装于无线能源塔，根据电网当前电量/总容量百分比发出强度为0-15的红石信号"))
            .task(Task.item(GTItems.COVER_ENERGY_DETECTOR))
            .dependsOn(Q_3D4EDD8B));

    public static final Quest ADVANCED_ENERGY_DETECTOR_COVER = CHAPTER.quest("advanced_energy_detector_cover", q -> q
            .at(-4.5, -3)
            .desc(d -> d.line("安装于无线能源塔，根据电网电量百分比/绝对值和设定的最大、最小阈值发出红石信号，带有RS锁存功能"))
            .task(Task.item(GTItems.COVER_ENERGY_DETECTOR_ADVANCED))
            .dependsOn(ENERGY_DETECTOR_COVER));

    public static final Quest MACHINE_CONTROLLER_COVER = CHAPTER.quest("machine_controller_cover", q -> q
            .at(-3, -2)
            .desc(d -> d.line("控制机器的启停"))
            .task(Task.item(GTItems.COVER_MACHINE_CONTROLLER))
            .requireAny()
            .dependsOn(Q_3D4EDD8B));

    public static final Quest Q_7A409438 = CHAPTER.quest("q_7a409438", q -> q
            .at(-6, 0)
            .icon(GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.IV])
            .desc(d -> d
                    .line("{无线动力仓}可以直接向无线电网推送电力", GOLD)
                    .line()
                    .line("同样受到单次传输上限的限制"))
            .task(
                    Task.anyOf(1, GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.EV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.EV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.EV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.EV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.HV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.HV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.HV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.IV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.LuV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.LV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.LV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.LV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.MAX], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.MV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.MV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.MV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.OpV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.UEV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.UHV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.UIV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.UV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.UXV], GTOMachines.WIRELESS_OUTPUT_HATCH_2[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_1024[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_1048576[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_16384[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_16777216[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_16[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_262144[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_4096[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_4194304[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_4[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_64[GTValues.ZPM], GTOMachines.WIRELESS_OUTPUT_HATCH_65536[GTValues.ZPM]).title("任意无线动力仓"))
            .dependsOn(WIRELESS_ENERGY_BINDING_TOOL));

    public static final Quest Q_6AB84D44 = CHAPTER.quest("q_6ab84d44", q -> q
            .at(-6.75, 1.5)
            .icon(GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.IV])
            .desc(d -> d
                    .line("{无线能源仓}能直接从无线电网拉取电力", GOLD)
                    .line()
                    .line("同样受到单次传输上限的限制"))
            .task(
                    Task.anyOf(1, GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.EV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.EV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.EV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.EV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.HV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.HV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.HV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.IV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.LuV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.LV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.LV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.LV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.MAX], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.MV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.MV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.MV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.OpV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.UEV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.UHV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.UIV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.UV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.UXV], GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_1024[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_1048576[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_16384[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_16777216[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_16[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_262144[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_4194304[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_4[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.ZPM], GTOMachines.WIRELESS_INPUT_HATCH_65536[GTValues.ZPM]).title("任意无线能源仓"))
            .dependsOn(WIRELESS_ENERGY_BINDING_TOOL));

    public static final Quest WIRELESS_DIMENSION_REPEATER = CHAPTER.quest("wireless_dimension_repeater", q -> q
            .at(-10, 3)
            .subtitle("终于不需要在外星放发电机了")
            .desc(d -> d
                    .line("跨维度传无线电")
                    .line()
                    .line("{选择一个维度与当前维度搭建电桥}，能够传输的{最高电压}取决于其上安装的{整体框架}", AQUA, AQUA, GOLD)
                    .line()
                    .line("{如果你需要一些哭泣的黑曜石，可以去看看黑曜石的tooltips}", GRAY.italic()))
            .task(Task.item(MultiBlockG.WIRELESS_DIMENSION_REPEATER))
            .dependsOn(WirelessEnergyChapter.LV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest LV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("lv_wireless_energy_transfer_unit", q -> q
            .at(-8, 3)
            .task(Task.item(GTOBlocks.LV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(Q_08280145));

    public static final Quest MV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("mv_wireless_energy_transfer_unit", q -> q
            .at(-8, 4)
            .task(Task.item(GTOBlocks.MV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(LV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest HV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("hv_wireless_energy_transfer_unit", q -> q
            .at(-8, 5)
            .task(Task.item(GTOBlocks.HV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(MV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest EV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("ev_wireless_energy_transfer_unit", q -> q
            .at(-8, 6)
            .task(Task.item(GTOBlocks.EV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(HV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest IV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("iv_wireless_energy_transfer_unit", q -> q
            .at(-8, 7)
            .task(Task.item(GTOBlocks.IV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(EV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest LUV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("luv_wireless_energy_transfer_unit", q -> q
            .at(-8, 8)
            .task(Task.item(GTOBlocks.LUV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(IV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest ZPM_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("zpm_wireless_energy_transfer_unit", q -> q
            .at(-8, 9)
            .task(Task.item(GTOBlocks.ZPM_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(LUV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uv_wireless_energy_transfer_unit", q -> q
            .at(-8, 10)
            .task(Task.item(GTOBlocks.UV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(ZPM_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UHV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uhv_wireless_energy_transfer_unit", q -> q
            .at(-8, 11)
            .task(Task.item(GTOBlocks.UHV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UEV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uev_wireless_energy_transfer_unit", q -> q
            .at(-8, 12)
            .task(Task.item(GTOBlocks.UEV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UHV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UIV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uiv_wireless_energy_transfer_unit", q -> q
            .at(-8, 13)
            .task(Task.item(GTOBlocks.UIV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UEV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UXV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uxv_wireless_energy_transfer_unit", q -> q
            .at(-8, 14)
            .task(Task.item(GTOBlocks.UXV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UIV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest OPV_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("opv_wireless_energy_transfer_unit", q -> q
            .at(-8, 15)
            .task(Task.item(GTOBlocks.OPV_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UXV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest MAX_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("max_wireless_energy_transfer_unit", q -> q
            .at(-8, 16)
            .task(Task.item(GTOBlocks.MAX_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(OPV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UHV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uhv_super_wireless_energy_transfer_unit", q -> q
            .at(-10, 11)
            .desc(d -> d
                    .line("UHV 开始，解锁了为普通单元提供乘数的超级单元。")
                    .line()
                    .line("{注意超级单元只提供乘数，不能只放超级单元！}", RED))
            .task(Task.item(GTOBlocks.UHV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UHV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UEV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uev_super_wireless_energy_transfer_unit", q -> q
            .at(-10, 12)
            .task(Task.item(GTOBlocks.UEV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UEV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UIV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uiv_super_wireless_energy_transfer_unit", q -> q
            .at(-10, 13)
            .task(Task.item(GTOBlocks.UIV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UIV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UXV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("uxv_super_wireless_energy_transfer_unit", q -> q
            .at(-10, 14)
            .task(Task.item(GTOBlocks.UXV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(UXV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest OPV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("opv_super_wireless_energy_transfer_unit", q -> q
            .at(-10, 15)
            .task(Task.item(GTOBlocks.OPV_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(OPV_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest MAX_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT = CHAPTER.quest("max_super_wireless_energy_transfer_unit", q -> q
            .at(-10, 16)
            .task(Task.item(GTOBlocks.MAX_SUPER_WIRELESS_ENERGY_TRANSFER_UNIT))
            .dependsOn(MAX_WIRELESS_ENERGY_TRANSFER_UNIT));

    public static final Quest UHV_SUPER_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uhv_super_wireless_energy_unit", q -> q
            .at(1, 11)
            .desc(d -> d
                    .line("UHV 开始，解锁了为普通单元提供乘数的超级单元。")
                    .line()
                    .line("{注意超级单元只提供乘数，不能只放超级单元！}", RED))
            .task(Task.item(GTOBlocks.UHV_SUPER_WIRELESS_ENERGY_UNIT))
            .dependsOn(UHV_WIRELESS_ENERGY_UNIT));

    public static final Quest UEV_SUPER_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uev_super_wireless_energy_unit", q -> q
            .at(1, 12)
            .task(Task.item(GTOBlocks.UEV_SUPER_WIRELESS_ENERGY_UNIT))
            .dependsOn(UEV_WIRELESS_ENERGY_UNIT));

    public static final Quest UIV_SUPER_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uiv_super_wireless_energy_unit", q -> q
            .at(1, 13)
            .task(Task.item(GTOBlocks.UIV_SUPER_WIRELESS_ENERGY_UNIT))
            .dependsOn(UIV_WIRELESS_ENERGY_UNIT));

    public static final Quest UXV_SUPER_WIRELESS_ENERGY_UNIT = CHAPTER.quest("uxv_super_wireless_energy_unit", q -> q
            .at(1, 14)
            .task(Task.item(GTOBlocks.UXV_SUPER_WIRELESS_ENERGY_UNIT))
            .dependsOn(UXV_WIRELESS_ENERGY_UNIT));

    public static final Quest OPV_SUPER_WIRELESS_ENERGY_UNIT = CHAPTER.quest("opv_super_wireless_energy_unit", q -> q
            .at(1, 15)
            .task(Task.item(GTOBlocks.OPV_SUPER_WIRELESS_ENERGY_UNIT))
            .dependsOn(OPV_WIRELESS_ENERGY_UNIT));

    public static final Quest MAX_SUPER_WIRELESS_ENERGY_UNIT = CHAPTER.quest("max_super_wireless_energy_unit", q -> q
            .at(1, 16)
            .task(Task.item(GTOBlocks.MAX_SUPER_WIRELESS_ENERGY_UNIT))
            .dependsOn(MAX_WIRELESS_ENERGY_UNIT));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -10.5, 2.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_MV, -4, 3.5, 2, 2))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -7.25, 1, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -6.5, -0.5, 1, 1)));
    }
}
