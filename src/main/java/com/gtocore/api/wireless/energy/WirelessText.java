package com.gtocore.api.wireless.energy;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.Dimension;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.tooltip.ElectricText;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public final class WirelessText {

    @RegisterLanguage(cn = "无覆盖", en = "No coverage")
    public static final String NO_COVERAGE = "gtocore.wireless_energy.no_coverage";
    @RegisterLanguage(cn = "本地 %s", en = "Local %s")
    public static final String NODE_LOCAL = "gtocore.wireless_energy.node_local";
    @RegisterLanguage(cn = "仅经线路 · 最高 %s", en = "Via lines only · up to %s")
    public static final String NODE_REMOTE = "gtocore.wireless_energy.node_remote";
    @RegisterLanguage(cn = "%s（每个方向）", en = "%s (each way)")
    public static final String LINE = "gtocore.wireless_energy.line";
    @RegisterLanguage(cn = "%s（含轨道）", en = "%s (incl. orbit)")
    public static final String WITH_ORBIT = "gtocore.wireless_energy.with_orbit";
    @RegisterLanguage(cn = "不连接", en = "Not connected")
    public static final String NOT_CONNECTED = "gtocore.wireless_energy.not_connected";
    @RegisterLanguage(cn = "本维度电网：%s", en = "Grid in this dimension: %s")
    public static final String DIMENSION_COVERAGE = "gtocore.wireless_energy.dimension_coverage";
    @RegisterLanguage(cn = "只有所有者、同队成员或管理员可以改绑", en = "Only the owner, a teammate or an operator can rebind this")
    public static final String REBIND_DENIED = "gtocore.wireless_energy.rebind_denied";

    public static final Component ZERO_POWER = ElectricText.amps(0);
    private static final double TICKS_PER_HOUR = 72000;

    private WirelessText() {}

    public static Component voltage(int tier) {
        if (tier < 0) return Component.translatable(NO_COVERAGE);
        return Component.literal(GTValues.VNF[tier]);
    }

    public static Component node(int tier, int reachTier, double storage, double capacity, String[] tierNames) {
        if (tier >= 0) return Component.translatable(NODE_LOCAL, ElectricText.charge(storage, capacity, tier, tierNames[tier]));
        if (reachTier >= 0) return Component.translatable(NODE_REMOTE, tierNames[reachTier]);
        return Component.translatable(NO_COVERAGE);
    }

    public static Component node(GridNode node) {
        return node(node.tier(), node.reachTier(), node.storageDouble(), node.capacityDouble(), GTValues.VNF);
    }

    public static Component plainNode(GridNode node) {
        return node(node.tier(), node.reachTier(), node.storageDouble(), node.capacityDouble(), GTValues.VN);
    }

    public static Component line(int tier, long budget) {
        return Component.translatable(LINE, ElectricText.flow(budget / (double) GTValues.V[tier], tier));
    }

    public static int voltageTier(double perTick, int tier) {
        return tier >= GTValues.ULV && tier <= GTValues.MAX ? tier : powerTier(perTick);
    }

    public static int powerTier(double perTick) {
        double abs = Math.abs(perTick);
        if (!(abs >= GTValues.V[GTValues.LV])) return GTValues.ULV;
        if (abs >= GTValues.V[GTValues.MAX]) return GTValues.MAX;
        return GTUtil.getFloorTierByVoltage((long) abs);
    }

    public static double powerAmps(double perTick, int tier) {
        double amps = Math.abs(perTick) / GTValues.V[tier];
        return Double.isFinite(amps) ? amps : 0;
    }

    public static Component power(double perTick, int tier) {
        return power(perTick, tier, GTValues.VNF);
    }

    public static Component power(double perTick, int tier, String[] tierNames) {
        if (!(Math.abs(perTick) >= 1)) return ZERO_POWER;
        int t = voltageTier(perTick, tier);
        return ElectricText.flow(powerAmps(perTick, t), tierNames[t]);
    }

    public static Component signedPower(double perTick, int tier) {
        var power = power(perTick, tier);
        if (!(Math.abs(perTick) >= 1)) return power;
        return Component.literal(perTick > 0 ? "+" : "-").append(power);
    }

    public static Component powerUsage(double used, double capacity, int tier) {
        return powerUsage(used, capacity, tier, GTValues.VNF);
    }

    public static Component powerUsage(double used, double capacity, int tier, String[] tierNames) {
        int t = voltageTier(capacity, tier);
        return ElectricText.flowUsage(powerAmps(used, t), powerAmps(capacity, t), tierNames[t]);
    }

    public static MutableComponent signedPowerUsage(double net, double capacity, int tier, ChatFormatting netColor) {
        int t = voltageTier(Math.max(Math.abs(net), capacity), tier);
        double amps = powerAmps(net, t);
        String sign = !(amps > 0) ? "" : net > 0 ? "+" : "-";
        var used = Component.literal(sign + ElectricText.ampsOf(amps, powerAmps(capacity, t))).withStyle(netColor);
        return ElectricText.flowUsage(used, powerAmps(capacity, t), GTValues.VNF[t]);
    }

    public static Component energy(double eu, int tier) {
        int t = tier >= GTValues.LV && tier <= GTValues.MAX ? tier : powerTier(eu / TICKS_PER_HOUR);
        return ElectricText.energy(eu, t);
    }

    public static Component dimension(@Nullable ResourceKey<Level> key) {
        if (key == null) return Component.translatable(NOT_CONNECTED);
        var body = Dimension.getIncludingOrbits(key);
        if (body == null) return Component.literal(key.location().toString());
        var name = Component.translatable(body.getKey());
        return body.hasOrbitDimension() ? Component.translatable(WITH_ORBIT, name) : name;
    }

    public static String amperes(double amperage) {
        return ElectricText.ampsValue(amperage);
    }
}
