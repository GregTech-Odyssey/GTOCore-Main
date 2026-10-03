package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.EnergyAccount;
import com.gtocore.api.wireless.energy.EnergyStats;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.utils.FormatUtil;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public final class GridReadouts {

    public static final int TICKS_PER_SECOND = 20;

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

    public static Component rate(EnergyAccount account) {
        long rate = account.rate();
        int tier = GTUtil.getFloorTierByVoltage(rate);
        return FormatUtil.formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.2", Component.literal(FormatUtil.formatBigIntegerNumberOrSic(BigInteger.valueOf(rate))), Component.literal(String.valueOf(rate / GTValues.VEX[tier])), Component.literal(GTValues.VNF[tier])).withStyle(ChatFormatting.GRAY);
    }

    public static void flow(List<Component> lines, Flow flow, @Nullable EnergyStats stats, int second) {
        lines.add(Component.translatable(flow.title));
        lines.add(average("gtmthings.machine.wireless_energy_monitor.tooltip.last_minute", ChatFormatting.DARK_AQUA, flow.value(stats, EnergyStats.Window.MINUTE, second)));
        lines.add(average("gtmthings.machine.wireless_energy_monitor.tooltip.last_hour", ChatFormatting.YELLOW, flow.value(stats, EnergyStats.Window.HOUR, second)));
        lines.add(average("gtmthings.machine.wireless_energy_monitor.tooltip.last_day", ChatFormatting.DARK_GREEN, flow.value(stats, EnergyStats.Window.DAY, second)));
        lines.add(average("gtmthings.machine.wireless_energy_monitor.tooltip.now", ChatFormatting.DARK_PURPLE, flow.value(stats, null, second)));
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

    @Nullable
    public static Component binding(EnergyAccount account) {
        var bind = account.bindPos();
        if (bind == null) return null;
        return Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.2", Component.translatable("recipe.condition.dimension.tooltip", bind.dimension().location().toString()).append(" [").append(bind.pos().toShortString()).append("] ")).withStyle(ChatFormatting.GRAY);
    }

    private static Component average(String key, ChatFormatting color, double perTick) {
        var value = BigDecimal.valueOf(perTick);
        return FormatUtil.formatWithConstantWidth(key, Component.literal(FormatUtil.formatBigDecimalNumberOrSic(value)).withStyle(color), Component.literal(FormatUtil.voltageAmperage(value).toEngineeringString()), FormatUtil.voltageName(value));
    }
}
