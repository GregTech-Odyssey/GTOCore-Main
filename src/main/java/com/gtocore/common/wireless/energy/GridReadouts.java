package com.gtocore.common.wireless.energy;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.wireless.energy.EnergyAccount;
import com.gtolib.api.wireless.energy.EnergyStats;
import com.gtolib.api.wireless.energy.GridNode;
import com.gtolib.api.wireless.energy.WirelessText;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.utils.FormatUtil;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

@DataGeneratorScanned
public final class GridReadouts {

    public static final int TICKS_PER_SECOND = 20;

    @RegisterLanguage(cn = "本维度输出吞吐：%s", en = "Output throughput here: %s")
    private static final String OUTPUT_THROUGHPUT = "gtocore.wireless_energy.readout.output_throughput";
    @RegisterLanguage(cn = "本维度输入吞吐：%s", en = "Input throughput here: %s")
    private static final String INPUT_THROUGHPUT = "gtocore.wireless_energy.readout.input_throughput";
    @RegisterLanguage(cn = "近一分钟：%s", en = "Last minute: %s")
    private static final String LAST_MINUTE = "gtocore.wireless_energy.readout.last_minute";
    @RegisterLanguage(cn = "近一小时：%s", en = "Last hour: %s")
    private static final String LAST_HOUR = "gtocore.wireless_energy.readout.last_hour";
    @RegisterLanguage(cn = "近一天：%s", en = "Last day: %s")
    private static final String LAST_DAY = "gtocore.wireless_energy.readout.last_day";
    @RegisterLanguage(cn = "当前：%s", en = "Now: %s")
    private static final String NOW = "gtocore.wireless_energy.readout.now";

    public enum Flow {

        NET("gtmthings.machine.wireless_energy_monitor.tooltip.net_power"),
        INPUT("gtmthings.machine.wireless_energy_monitor.tooltip.input_power"),
        OUTPUT("gtmthings.machine.wireless_energy_monitor.tooltip.output_power");

        public final String title;

        Flow(String title) {
            this.title = title;
        }

        public double value(@Nullable EnergyStats stats, @Nullable EnergyStats.Window window, int second) {
            if (stats == null) return 0;
            double in = window == null ? stats.nowIn(second) : stats.avgIn(window, second);
            double out = window == null ? stats.nowOut(second) : stats.avgOut(window, second);
            double loss = window == null ? stats.nowLoss(second) : stats.avgLoss(window, second);
            return switch (this) {
                case NET -> in - out - loss;
                case INPUT -> in;
                case OUTPUT -> out;
            };
        }
    }

    private GridReadouts() {}

    public static Component storage(EnergyAccount account) {
        return FormatUtil.formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.1", Component.literal(FormatUtil.formatBigIntegerNumberOrSic(account.totalStorage()))).withStyle(ChatFormatting.GOLD);
    }

    public static Component throughput(GridNode node, boolean output, int tier) {
        long used = 0, cap = 0;
        for (int m = node.throughputBanks(); m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            used = saturatedAdd(used, output ? node.outputUsed(t) : node.inputUsed(t));
            cap = saturatedAdd(cap, output ? node.outputCapacity(t) : node.inputCapacity(t));
        }
        return Component.translatable(output ? OUTPUT_THROUGHPUT : INPUT_THROUGHPUT, WirelessText.powerUsage(used, cap, tier)).withStyle(ChatFormatting.GRAY);
    }

    private static long saturatedAdd(long a, long b) {
        long s = a + b;
        return s < 0 ? Long.MAX_VALUE : s;
    }

    public static void flow(List<Component> lines, Flow flow, @Nullable EnergyStats stats, int second, int tier) {
        lines.add(Component.translatable(flow.title));
        lines.add(average(LAST_MINUTE, ChatFormatting.DARK_AQUA, flow == Flow.NET, tier, flow.value(stats, EnergyStats.Window.MINUTE, second)));
        lines.add(average(LAST_HOUR, ChatFormatting.YELLOW, flow == Flow.NET, tier, flow.value(stats, EnergyStats.Window.HOUR, second)));
        lines.add(average(LAST_DAY, ChatFormatting.DARK_GREEN, flow == Flow.NET, tier, flow.value(stats, EnergyStats.Window.DAY, second)));
        lines.add(average(NOW, ChatFormatting.DARK_PURPLE, flow == Flow.NET, tier, flow.value(stats, null, second)));
    }

    public static Component fillOrDrain(EnergyAccount account, @Nullable EnergyStats stats, int second) {
        double net = Flow.NET.value(stats, null, second);
        if (net > 0) {
            double seconds = Math.max(0, account.totalCapacityDouble() - account.totalStorageDouble()) / (net * TICKS_PER_SECOND);
            return Component.translatable("gtceu.multiblock.power_substation.time_to_fill", PowerSubstationMachine.getTimeToFillDrainText(BigDecimal.valueOf(seconds).toBigInteger())).withStyle(ChatFormatting.GRAY);
        }
        if (net < 0) {
            double seconds = account.totalStorageDouble() / (-net * TICKS_PER_SECOND);
            return Component.translatable("gtceu.multiblock.power_substation.time_to_drain", PowerSubstationMachine.getTimeToFillDrainText(BigDecimal.valueOf(seconds).toBigInteger())).withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable("gtceu.multiblock.power_substation.time_to_drain", Component.translatable("gtceu.multiblock.power_substation.time_forever")).withStyle(ChatFormatting.GRAY);
    }

    private static Component average(String key, ChatFormatting color, boolean signed, int tier, double perTick) {
        return Component.translatable(key, (signed ? WirelessText.signedPower(perTick, tier) : WirelessText.power(perTick, tier)).copy().withStyle(color));
    }
}
