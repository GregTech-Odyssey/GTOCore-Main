package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;

public final class ProgressChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("progress", c -> c
            .title("里程碑")
            .subtitle("Milestone"));

    public static final Quest A_JOURNEY_OF_A_THOUSAND_MILES = CHAPTER.quest("a_journey_of_a_thousand_miles", q -> q
            .at(-10.5, -19.5)
            .title("千里之行始于足下")
            .subtitle("准备踏上旅途")
            .desc(d -> d.line("虽然很粗糙，但是代表着你准备好！"))
            .task(Task.item(GTItems.VACUUM_TUBE)));

    public static final Quest BASIC_ELECTRONIC_CIRCUIT = CHAPTER.quest("basic_electronic_circuit", q -> q
            .at(-10.5, -18.5)
            .subtitle("第一块电路")
            .desc(d -> d.line("恭喜你，踏上旅途，请享受一路上的风景！"))
            .task(Task.item(GTItems.ELECTRONIC_CIRCUIT_LV)));

    public static final Quest GOOD_ELECTRONIC_CIRCUIT = CHAPTER.quest("good_electronic_circuit", q -> q
            .at(-10.5, -17.5)
            .subtitle("第二块电路")
            .desc(d -> d.line("看起来，有模有样了。"))
            .task(Task.item(GTItems.ELECTRONIC_CIRCUIT_MV)));

    public static final Quest BASIC_INTEGRATED_CIRCUIT = CHAPTER.quest("basic_integrated_circuit", q -> q
            .at(-9.5, -18.5)
            .subtitle("更廉价的电路板")
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_LV)));

    public static final Quest GOOD_INTEGRATED_CIRCUIT = CHAPTER.quest("good_integrated_circuit", q -> q
            .at(-9.5, -17.5)
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_MV)));

    public static final Quest Q_29E7BC53 = CHAPTER.quest("q_29e7bc53", q -> q
            .at(-2.5, -17)
            .icon(QuestTextures.GUI_RGBCOLORWHEEL)
            .subtitle("里程碑收集")
            .desc(d -> d.line("本章节展示所有线圈方块与电路板收集。显示每个阶段制作的电路板。"))
            .task(Task.checkmark().title("里程碑"))
            .optional());

    public static final Quest ADVANCED_INTEGRATED_CIRCUIT = CHAPTER.quest("advanced_integrated_circuit", q -> q
            .at(-9.5, -16.5)
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_HV)));

    public static final Quest MICROCHIP_PROCESSOR = CHAPTER.quest("microchip_processor", q -> q
            .at(-8.5, -18.5)
            .subtitle("晶体管的第一次紧密排列")
            .task(Task.item(GTItems.MICROPROCESSOR_LV)));

    public static final Quest MICRO_PROCESSOR = CHAPTER.quest("micro_processor", q -> q
            .at(-8.5, -17.5)
            .task(Task.item(GTItems.PROCESSOR_MV)));

    public static final Quest MICRO_PROCESSOR_ASSEMBLY = CHAPTER.quest("micro_processor_assembly", q -> q
            .at(-8.5, -16.5)
            .task(Task.item(GTItems.PROCESSOR_ASSEMBLY_HV)));

    public static final Quest MICRO_PROCESSOR_COMPUTER = CHAPTER.quest("micro_processor_computer", q -> q
            .at(-8.5, -15.5)
            .task(Task.item(GTItems.WORKSTATION_EV)));

    public static final Quest MICRO_PROCESSOR_MAINFRAME = CHAPTER.quest("micro_processor_mainframe", q -> q
            .at(-8.5, -14.5)
            .task(Task.item(GTItems.MAINFRAME_IV)));

    public static final Quest NANO_PROCESSOR = CHAPTER.quest("nano_processor", q -> q
            .at(-7.5, -16.5)
            .subtitle("信息时代工艺水准")
            .task(Task.item(GTItems.NANO_PROCESSOR_HV)));

    public static final Quest NANO_PROCESSOR_ASSEMBLY = CHAPTER.quest("nano_processor_assembly", q -> q
            .at(-7.5, -15.5)
            .task(Task.item(GTItems.NANO_PROCESSOR_ASSEMBLY_EV)));

    public static final Quest NANO_PROCESSOR_COMPUTER = CHAPTER.quest("nano_processor_computer", q -> q
            .at(-7.5, -14.5)
            .task(Task.item(GTItems.NANO_COMPUTER_IV)));

    public static final Quest NANO_PROCESSOR_MAINFRAME = CHAPTER.quest("nano_processor_mainframe", q -> q
            .at(-7.5, -13.5)
            .task(Task.item(GTItems.NANO_MAINFRAME_LuV)));

    public static final Quest QUANTUM_PROCESSOR = CHAPTER.quest("quantum_processor", q -> q
            .at(-6.5, -15.5)
            .subtitle("大概一百万个哈达玛门")
            .task(Task.item(GTItems.QUANTUM_PROCESSOR_EV)));

    public static final Quest CRYSTAL_PROCESSOR = CHAPTER.quest("crystal_processor", q -> q
            .at(-5.5, -14.5)
            .subtitle("完美刻蚀技巧")
            .task(Task.item(GTItems.CRYSTAL_PROCESSOR_IV)));

    public static final Quest WETWARE_PROCESSOR = CHAPTER.quest("wetware_processor", q -> q
            .at(-4.5, -13.5)
            .subtitle("真正意义的神经网络")
            .task(Task.item(GTItems.WETWARE_PROCESSOR_LuV)));

    public static final Quest BIOWARE_PROCESSOR = CHAPTER.quest("bioware_processor", q -> q
            .at(-3.5, -12.5)
            .subtitle("寄生菌群")
            .task(Task.item(GTOItems.BIOWARE_PROCESSOR)));

    public static final Quest OPTICAL_PROCESSOR = CHAPTER.quest("optical_processor", q -> q
            .at(-2.5, -11.5)
            .subtitle("秒速3亿米")
            .task(Task.item(GTOItems.OPTICAL_PROCESSOR)));

    public static final Quest EXOTIC_PROCESSOR = CHAPTER.quest("exotic_processor", q -> q
            .at(-1.5, -10.5)
            .subtitle("紫色超高能激光")
            .task(Task.item(GTOItems.EXOTIC_PROCESSOR)));

    public static final Quest COSMIC_PROCESSOR = CHAPTER.quest("cosmic_processor", q -> q
            .at(-0.5, -9.5)
            .subtitle("那个核心是黑洞吗？")
            .task(Task.item(GTOItems.COSMIC_PROCESSOR)));

    public static final Quest SUPRACAUSAL_PROCESSOR = CHAPTER.quest("supracausal_processor", q -> q
            .at(0.5, -8.5)
            .subtitle("答案在运算前早已揭晓")
            .task(Task.item(GTOItems.SUPRACAUSAL_PROCESSOR)));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_ULV = CHAPTER.quest("magneto_resonatic_circuit_ulv", q -> q
            .at(-11.5, -19.5)
            .subtitle("请LuV后再来探索吧")
            .desc(d -> d.line("你必须要解锁大型虚空采矿场才可以解锁这里的电路！"))
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.ULV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_LV = CHAPTER.quest("magneto_resonatic_circuit_lv", q -> q
            .at(-11.5, -18.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.LV])));

    public static final Quest QUANTUM_PROCESSOR_ASSEMBLY = CHAPTER.quest("quantum_processor_assembly", q -> q
            .at(-6.5, -14.5)
            .task(Task.item(GTItems.QUANTUM_ASSEMBLY_IV)));

    public static final Quest QUANTUM_PROCESSOR_COMPUTER = CHAPTER.quest("quantum_processor_computer", q -> q
            .at(-6.5, -13.5)
            .task(Task.item(GTItems.QUANTUM_COMPUTER_LuV)));

    public static final Quest QUANTUM_PROCESSOR_MAINFRAME = CHAPTER.quest("quantum_processor_mainframe", q -> q
            .at(-6.5, -12.5)
            .task(Task.item(GTItems.QUANTUM_MAINFRAME_ZPM)));

    public static final Quest CRYSTAL_PROCESSOR_ASSEMBLY = CHAPTER.quest("crystal_processor_assembly", q -> q
            .at(-5.5, -13.5)
            .task(Task.item(GTItems.CRYSTAL_ASSEMBLY_LuV)));

    public static final Quest CRYSTAL_PROCESSOR_COMPUTER = CHAPTER.quest("crystal_processor_computer", q -> q
            .at(-5.5, -12.5)
            .task(Task.item(GTItems.CRYSTAL_COMPUTER_ZPM)));

    public static final Quest CRYSTAL_PROCESSOR_MAINFRAME = CHAPTER.quest("crystal_processor_mainframe", q -> q
            .at(-5.5, -11.5)
            .task(Task.item(GTItems.CRYSTAL_MAINFRAME_UV)));

    public static final Quest WETWARE_PROCESSOR_ASSEMBLY = CHAPTER.quest("wetware_processor_assembly", q -> q
            .at(-4.5, -12.5)
            .task(Task.item(GTItems.WETWARE_PROCESSOR_ASSEMBLY_ZPM)));

    public static final Quest WETWARE_PROCESSOR_COMPUTER = CHAPTER.quest("wetware_processor_computer", q -> q
            .at(-4.5, -11.5)
            .task(Task.item(GTItems.WETWARE_SUPER_COMPUTER_UV)));

    public static final Quest WETWARE_PROCESSOR_MAINFRAME = CHAPTER.quest("wetware_processor_mainframe", q -> q
            .at(-4.5, -10.5)
            .task(Task.item(GTItems.WETWARE_MAINFRAME_UHV)));

    public static final Quest BIOWARE_ASSEMBLY = CHAPTER.quest("bioware_assembly", q -> q
            .at(-3.5, -11.5)
            .task(Task.item(GTOItems.BIOWARE_ASSEMBLY)));

    public static final Quest BIOWARE_COMPUTER = CHAPTER.quest("bioware_computer", q -> q
            .at(-3.5, -10.5)
            .task(Task.item(GTOItems.BIOWARE_COMPUTER)));

    public static final Quest BIOWARE_MAINFRAME = CHAPTER.quest("bioware_mainframe", q -> q
            .at(-3.5, -9.5)
            .task(Task.item(GTOItems.BIOWARE_MAINFRAME)));

    public static final Quest OPTICAL_ASSEMBLY = CHAPTER.quest("optical_assembly", q -> q
            .at(-2.5, -10.5)
            .task(Task.item(GTOItems.OPTICAL_ASSEMBLY)));

    public static final Quest OPTICAL_COMPUTER = CHAPTER.quest("optical_computer", q -> q
            .at(-2.5, -9.5)
            .task(Task.item(GTOItems.OPTICAL_COMPUTER)));

    public static final Quest OPTICAL_MAINFRAME = CHAPTER.quest("optical_mainframe", q -> q
            .at(-2.5, -8.5)
            .task(Task.item(GTOItems.OPTICAL_MAINFRAME)));

    public static final Quest EXOTIC_ASSEMBLY = CHAPTER.quest("exotic_assembly", q -> q
            .at(-1.5, -9.5)
            .task(Task.item(GTOItems.EXOTIC_ASSEMBLY)));

    public static final Quest EXOTIC_COMPUTER = CHAPTER.quest("exotic_computer", q -> q
            .at(-1.5, -8.5)
            .task(Task.item(GTOItems.EXOTIC_COMPUTER)));

    public static final Quest EXOTIC_MAINFRAME = CHAPTER.quest("exotic_mainframe", q -> q
            .at(-1.5, -7.5)
            .task(Task.item(GTOItems.EXOTIC_MAINFRAME)));

    public static final Quest COSMIC_ASSEMBLY = CHAPTER.quest("cosmic_assembly", q -> q
            .at(-0.5, -8.5)
            .task(Task.item(GTOItems.COSMIC_ASSEMBLY)));

    public static final Quest COSMIC_COMPUTER = CHAPTER.quest("cosmic_computer", q -> q
            .at(-0.5, -7.5)
            .task(Task.item(GTOItems.COSMIC_COMPUTER)));

    public static final Quest COSMIC_MAINFRAME = CHAPTER.quest("cosmic_mainframe", q -> q
            .at(-0.5, -6.5)
            .task(Task.item(GTOItems.COSMIC_MAINFRAME)));

    public static final Quest SUPRACAUSAL_ASSEMBLY = CHAPTER.quest("supracausal_assembly", q -> q
            .at(0.5, -7.5)
            .task(Task.item(GTOItems.SUPRACAUSAL_ASSEMBLY)));

    public static final Quest SUPRACAUSAL_COMPUTER = CHAPTER.quest("supracausal_computer", q -> q
            .at(0.5, -6.5)
            .task(Task.item(GTOItems.SUPRACAUSAL_COMPUTER)));

    public static final Quest SUPRACAUSAL_MAINFRAME = CHAPTER.quest("supracausal_mainframe", q -> q
            .at(0.5, -5.5)
            .task(Task.item(GTOItems.SUPRACAUSAL_MAINFRAME)));

    public static final Quest UNIVERSAL_CIRCUIT_ULV = CHAPTER.quest("universal_circuit_ulv", q -> q
            .at(-12.5, -19.5)
            .subtitle("早点准备")
            .desc(d -> d.line("请在EV至IV就准备产线，以便于将生产的电路板转换成通用电路板，免去后续每个电压更换电路样板的不便。"))
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.ULV])));

    public static final Quest UNIVERSAL_CIRCUIT_LV = CHAPTER.quest("universal_circuit_lv", q -> q
            .at(-12.5, -18.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.LV])));

    public static final Quest UNIVERSAL_CIRCUIT_MV = CHAPTER.quest("universal_circuit_mv", q -> q
            .at(-12.5, -17.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.MV])));

    public static final Quest UNIVERSAL_CIRCUIT_HV = CHAPTER.quest("universal_circuit_hv", q -> q
            .at(-12.5, -16.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.HV])));

    public static final Quest UNIVERSAL_CIRCUIT_EV = CHAPTER.quest("universal_circuit_ev", q -> q
            .at(-12.5, -15.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.EV])));

    public static final Quest UNIVERSAL_CIRCUIT_IV = CHAPTER.quest("universal_circuit_iv", q -> q
            .at(-12.5, -14.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.IV])));

    public static final Quest UNIVERSAL_CIRCUIT_LUV = CHAPTER.quest("universal_circuit_luv", q -> q
            .at(-12.5, -13.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.LuV])));

    public static final Quest UNIVERSAL_CIRCUIT_ZPM = CHAPTER.quest("universal_circuit_zpm", q -> q
            .at(-12.5, -12.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.ZPM])));

    public static final Quest UNIVERSAL_CIRCUIT_UV = CHAPTER.quest("universal_circuit_uv", q -> q
            .at(-12.5, -11.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.UV])));

    public static final Quest UNIVERSAL_CIRCUIT_UHV = CHAPTER.quest("universal_circuit_uhv", q -> q
            .at(-12.5, -10.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.UHV])));

    public static final Quest UNIVERSAL_CIRCUIT_UEV = CHAPTER.quest("universal_circuit_uev", q -> q
            .at(-12.5, -9.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.UEV])));

    public static final Quest UNIVERSAL_CIRCUIT_UIV = CHAPTER.quest("universal_circuit_uiv", q -> q
            .at(-12.5, -8.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.UIV])));

    public static final Quest UNIVERSAL_CIRCUIT_UXV = CHAPTER.quest("universal_circuit_uxv", q -> q
            .at(-12.5, -7.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.UXV])));

    public static final Quest UNIVERSAL_CIRCUIT_OPV = CHAPTER.quest("universal_circuit_opv", q -> q
            .at(-12.5, -6.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.OpV])));

    public static final Quest UNIVERSAL_CIRCUIT_MAX = CHAPTER.quest("universal_circuit_max", q -> q
            .at(-12.5, -5.5)
            .task(Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.MAX])));

    public static final Quest SUPRACHRONAL_CIRCUIT_ULV = CHAPTER.quest("suprachronal_circuit_ulv", q -> q
            .at(1.5, -19.5)
            .subtitle("游戏的终章MAX")
            .desc(d -> d.line("当你制造此电路时，你已经接近了旅途的终章，回望基地，去看看你所创造的自动化世界。"))
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.ULV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_LV = CHAPTER.quest("suprachronal_circuit_lv", q -> q
            .at(1.5, -18.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.LV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_MV = CHAPTER.quest("suprachronal_circuit_mv", q -> q
            .at(1.5, -17.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.MV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_HV = CHAPTER.quest("suprachronal_circuit_hv", q -> q
            .at(1.5, -16.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.HV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_EV = CHAPTER.quest("suprachronal_circuit_ev", q -> q
            .at(1.5, -15.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.EV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_IV = CHAPTER.quest("suprachronal_circuit_iv", q -> q
            .at(1.5, -14.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.IV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_LUV = CHAPTER.quest("suprachronal_circuit_luv", q -> q
            .at(1.5, -13.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.LuV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_ZPM = CHAPTER.quest("suprachronal_circuit_zpm", q -> q
            .at(1.5, -12.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.ZPM])));

    public static final Quest SUPRACHRONAL_CIRCUIT_UV = CHAPTER.quest("suprachronal_circuit_uv", q -> q
            .at(1.5, -11.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_UHV = CHAPTER.quest("suprachronal_circuit_uhv", q -> q
            .at(1.5, -10.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UHV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_UEV = CHAPTER.quest("suprachronal_circuit_uev", q -> q
            .at(1.5, -9.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UEV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_UIV = CHAPTER.quest("suprachronal_circuit_uiv", q -> q
            .at(1.5, -8.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UIV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_UXV = CHAPTER.quest("suprachronal_circuit_uxv", q -> q
            .at(1.5, -7.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UXV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_OPV = CHAPTER.quest("suprachronal_circuit_opv", q -> q
            .at(1.5, -6.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.OpV])));

    public static final Quest SUPRACHRONAL_CIRCUIT_MAX = CHAPTER.quest("suprachronal_circuit_max", q -> q
            .at(1.5, -5.5)
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.MAX])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_MV = CHAPTER.quest("magneto_resonatic_circuit_mv", q -> q
            .at(-11.5, -17.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.MV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_HV = CHAPTER.quest("magneto_resonatic_circuit_hv", q -> q
            .at(-11.5, -16.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.HV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_EV = CHAPTER.quest("magneto_resonatic_circuit_ev", q -> q
            .at(-11.5, -15.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.EV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_IV = CHAPTER.quest("magneto_resonatic_circuit_iv", q -> q
            .at(-11.5, -14.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.IV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_LUV = CHAPTER.quest("magneto_resonatic_circuit_luv", q -> q
            .at(-11.5, -13.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.LuV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_ZPM = CHAPTER.quest("magneto_resonatic_circuit_zpm", q -> q
            .at(-11.5, -12.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.ZPM])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_UV = CHAPTER.quest("magneto_resonatic_circuit_uv", q -> q
            .at(-11.5, -11.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_UHV = CHAPTER.quest("magneto_resonatic_circuit_uhv", q -> q
            .at(-11.5, -10.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UHV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_UEV = CHAPTER.quest("magneto_resonatic_circuit_uev", q -> q
            .at(-11.5, -9.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UEV])));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_UIV = CHAPTER.quest("magneto_resonatic_circuit_uiv", q -> q
            .at(-11.5, -8.5)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UIV])));

    public static final Quest CUPRONICKEL_COIL_BLOCK = CHAPTER.quest("cupronickel_coil_block", q -> q
            .at(-13.5, -18.5)
            .subtitle("踏上旅途")
            .task(Task.item(GTBlocks.COIL_CUPRONICKEL)));

    public static final Quest KANTHAL_COIL_BLOCK = CHAPTER.quest("kanthal_coil_block", q -> q
            .at(-13.5, -17.5)
            .task(Task.item(GTBlocks.COIL_KANTHAL)));

    public static final Quest NICHROME_COIL_BLOCK = CHAPTER.quest("nichrome_coil_block", q -> q
            .at(-13.5, -16.5)
            .task(Task.item(GTBlocks.COIL_NICHROME)));

    public static final Quest RTM_ALLOY_COIL_BLOCK = CHAPTER.quest("rtm_alloy_coil_block", q -> q
            .at(-13.5, -15.5)
            .task(Task.item(GTBlocks.COIL_RTMALLOY)));

    public static final Quest HSSG_COIL_BLOCK = CHAPTER.quest("hssg_coil_block", q -> q
            .at(-13.5, -14.5)
            .task(Task.item(GTBlocks.COIL_HSSG)));

    public static final Quest NAQUADAH_COIL_BLOCK = CHAPTER.quest("naquadah_coil_block", q -> q
            .at(-14.5, -13.5)
            .task(Task.item(GTBlocks.COIL_NAQUADAH)));

    public static final Quest TRINIUM_COIL_BLOCK = CHAPTER.quest("trinium_coil_block", q -> q
            .at(-13.5, -13.5)
            .task(Task.item(GTBlocks.COIL_TRINIUM)));

    public static final Quest TRITANIUM_COIL_BLOCK = CHAPTER.quest("tritanium_coil_block", q -> q
            .at(-13.5, -12.5)
            .task(Task.item(GTBlocks.COIL_TRITANIUM)));

    public static final Quest TITANSTEEL_COIL_BLOCK = CHAPTER.quest("titansteel_coil_block", q -> q
            .at(-13.5, -10.5)
            .task(Task.item(GTOBlocks.TITANSTEEL_COIL_BLOCK)));

    public static final Quest ADAMANTINE_COIL_BLOCK = CHAPTER.quest("adamantine_coil_block", q -> q
            .at(-13.5, -9.5)
            .task(Task.item(GTOBlocks.ADAMANTINE_COIL_BLOCK)));

    public static final Quest NAQUADRIATICTARANIUM_COIL_BLOCK = CHAPTER.quest("naquadriatictaranium_coil_block", q -> q
            .at(-13.5, -8.5)
            .task(Task.item(GTOBlocks.NAQUADRIATICTARANIUM_COIL_BLOCK)));

    public static final Quest STARMETAL_COIL_BLOCK = CHAPTER.quest("starmetal_coil_block", q -> q
            .at(-13.5, -7.5)
            .task(Task.item(GTOBlocks.STARMETAL_COIL_BLOCK)));

    public static final Quest INFINITY_COIL_BLOCK = CHAPTER.quest("infinity_coil_block", q -> q
            .at(-13.5, -6.5)
            .task(Task.item(GTOBlocks.INFINITY_COIL_BLOCK)));

    public static final Quest HYPOGEN_COIL_BLOCK = CHAPTER.quest("hypogen_coil_block", q -> q
            .at(-14.5, -5.5)
            .task(Task.item(GTOBlocks.HYPOGEN_COIL_BLOCK)));

    public static final Quest ETERNITY_COIL_BLOCK = CHAPTER.quest("eternity_coil_block", q -> q
            .at(-13.5, -5.5)
            .task(Task.item(GTOBlocks.ETERNITY_COIL_BLOCK)));

    public static final Quest URUIUM_COIL_BLOCK = CHAPTER.quest("uruium_coil_block", q -> q
            .at(-14.5, -8.5)
            .task(Task.item(GTOBlocks.URUIUM_COIL_BLOCK)));

    public static final Quest ABYSSALALLOY_COIL_BLOCK = CHAPTER.quest("abyssalalloy_coil_block", q -> q
            .at(-13.5, -11.5)
            .task(Task.item(GTOBlocks.ABYSSALALLOY_COIL_BLOCK)));

    public static final Quest NAND_CHIP = CHAPTER.quest("nand_chip", q -> q
            .at(-8.5, -19.5)
            .task(Task.item(GTItems.NAND_CHIP_ULV)));
}
