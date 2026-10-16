package com.gtocore.common.data.translation;

import com.gtocore.api.lang.Tx;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.tooltip.ElectricText;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

import static com.gtocore.api.lang.GTOTips.global;

public final class ComponentSlang {

    public static final Component Infinite = Tx.rainbow(global("无限", "Infinite"));

    public static final Component right = Component.literal("✔");
    public static final Component wrong = Component.literal("✘");

    public static final Component RecommendedToUsePrefix = global("推荐使用", "Recommended to use ");
    public static final Component Explosion = Tx.red(global("爆炸", "explode"));
    public static final Component RunningRequirements = global("运行要求", "Running Requirements");
    public static final Component MainFunction = global("主要功能", "Main Function");
    public static final Component EfficiencyBonus = global("效率加成", "Efficiency Bonus");
    public static final Component ParallelBonus = global("并行加成", "Parallel Bonus");
    public static final Component PowerGenerationEfficiency = global("发电效率", "Power Generation Efficiency");
    public static final Component AfterModuleInstallation = global("安装模块后", "After Module Installation");
    public static final Component CoilEfficiencyBonus = global("线圈效率加成", "Coil Efficiency Bonus");
    public static final Component BeAwareOfBurn = Tx.red(global("小心烫伤！", "BE AWARE OF BURNS!"));
    public static final Component OutputProbability = global("产出概率", "Output Probability");

    public static final Component GTOSignal_LOGO_GTOCoreNormal = Tx.gold(global("GTOCore", "GTOCore"));
    public static final Component GTOSignal_LOGO_GTOCoreRemix = Tx.scrollFullColor(global("GTOCore", "GTOCore"));
    public static final Component GTOSignal_Edition_Text = Tx.join(global("经 ", "By "), GTOSignal_LOGO_GTOCoreNormal, global(" 修改", " Edition"));
    public static final Component GTOSignal_Edition_Remix_Text = Tx.join(global("经 ", "By "), GTOSignal_LOGO_GTOCoreRemix, global(" 修改", " Edition"));
    public static final Component GTOSignal_Machine_MilestoneByGTORemix = Tx.join(LegendSignalWrapper(GTOSignal_LOGO_GTOCoreRemix), global("里程碑建筑", "Milestone Building"));
    public static final Component GTOSignal_Machine_MiracleByGTORemix = Tx.join(LegendSignalWrapper(GTOSignal_LOGO_GTOCoreRemix), global("奇迹工程", "Miracle Project"));

    public static final Component CoreModuleSpaceModule = Tx.aqua(global("核心舱", "Core Module"));
    public static final Component FunctionModuleSpaceModule = Tx.aqua(global("模块舱", "Function Module"));
    public static final Component ConjunctionModuleSpaceModule = Tx.aqua(global("衔接舱", "Conjunction Module"));

    static {
        Temperature(0);
        Capacity("");
        RecommendedUseAs(Component.empty());
        TemperatureMax(0);
        BaseProductionEut(0L);
        RotorEfficiency(0);
        RecipeLevelBelow(0);
        UsePerHourLubricant(0L);
        PurifyLevel(0);
    }

    private ComponentSlang() {}

    public static void init() {}

    public static MutableComponent LegendSignalWrapper(Component other) {
        return Tx.join(Tx.obfuscated(Tx.rainbowGradient(Component.literal("111"))), Component.literal(" "), other,
                Component.literal(" "), Tx.obfuscated(Tx.rainbowGradient(Component.literal("111"))));
    }

    public static MutableComponent Temperature(int temp) {
        return Tx.join(global("温度: ", "Temperature: "), Tx.gold(Tx.lit(temp)));
    }

    public static MutableComponent Capacity(String capacity) {
        return Tx.join(global("容量: ", "Capacity: "), Tx.white(Tx.lit(capacity)));
    }

    public static MutableComponent RecommendedToUse(Component other) {
        return Tx.aqua(Tx.join(RecommendedToUsePrefix, Tx.gold(other)));
    }

    public static MutableComponent RecommendedUseAs(Component other) {
        return Tx.aqua(Tx.join(global("推荐用于", "Recommended use it to "), Tx.gold(other)));
    }

    public static MutableComponent TemperatureMax(int temp) {
        return Tx.join(global("最高温度: ", "Max Temperature: "), Tx.gold(Tx.lit(temp)));
    }

    public static MutableComponent BaseProductionEut(long eut) {
        return Tx.join(global("基础产能功率: ", "Base Production: "), Tx.yellow(ElectricText.power(eut)));
    }

    public static Component RotorEfficiency(int tier) {
        String name = GTValues.VNF[tier] + "§r";
        return global("转子支架每超过" + name + "一级，每级增加10%效率，并翻倍输出功率",
                "Each Rotor Holder above " + name + " adds 10% efficiency and multiplies EU/t by 2");
    }

    public static Component RecipeLevelBelow(int tier) {
        String name = GTValues.VNF[tier] + "§r";
        return global("只能运行" + name + "级及以下配方", "Can only run " + name + "-tier and below recipes");
    }

    public static Component UsePerHourLubricant(long cnt) {
        return global("每小时消耗" + cnt + "mB润滑油", "Use " + cnt + "mB Lubricant Per Hour");
    }

    public static MutableComponent PurifyLevel(int tier) {
        return Tx.join(global("净化等级：", "Purify Level: "), Tx.green(Tx.lit(tier)));
    }

    public static MutableComponent IsWhatTypeSpaceModule(Component type) {
        return Tx.green(Tx.join(global("太空舱种类：", " Space Module Type: "), type));
    }

    public static MutableComponent CanConnectToWhatTypeSpaceModule(List<? extends Component> types) {
        MutableComponent result = Tx.join(Tx.green(global("可连接至的太空舱种类：", "Types of Space Modules can connect to:")));
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) result.append(", ");
            result.append(types.get(i));
        }
        return result;
    }
}
