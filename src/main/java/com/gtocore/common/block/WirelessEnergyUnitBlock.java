package com.gtocore.common.block;

import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.config.GTORules;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.tooltip.StatKind;
import com.gregtechceu.gtceu.api.tooltip.TipTheme;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.Nullable;

@Getter
@DataGeneratorScanned
public class WirelessEnergyUnitBlock extends Block implements WirelessEnergyTowerUnit {

    @RegisterLanguage(cn = "吞吐：输入、输出各 %s × %sA", en = "Throughput: %s × %sA each for input and output")
    static final String THROUGHPUT = "gtocore.block.wireless_energy_unit.throughput";
    @RegisterLanguage(cn = "生效条件：能源塔玻璃等级不低于 %s", en = "Takes effect when the tower's glass tier is at least %s")
    static final String REQUIRES_GLASS = "gtocore.block.wireless_energy_unit.requires_glass";
    @RegisterLanguage(cn = "能源塔的提供电压 = 内部生效单元的最高等级 + 1；吞吐按本单元电压计，以更高电压取电时按功率折算", en = "A tower supplies its highest effective unit tier + 1; throughput counts at this unit's voltage and is converted by power when drawn at a higher voltage")
    static final String SUPPLIES = "gtocore.block.wireless_energy_unit.supplies";
    @RegisterLanguage(cn = "同一维度内电压等级相同的能源塔中，同级或更高的单元共有 N 个时，每个按基础容量的 N 倍计", en = "With N units of this tier or higher across towers of the same voltage tier in one dimension, each counts as N times its base capacity")
    private static final String SCALING = "gtocore.block.wireless_energy_unit.scaling";
    @RegisterLanguage(cn = "损耗率：%s%%", en = "Loss rate: %s%%")
    private static final String LOSS = "gtocore.block.wireless_energy_unit.loss";
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final long TICKS_PER_HOUR = 20 * 3600;
    private static final long AMPERAGE = 4;

    private final int tier;

    public WirelessEnergyUnitBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public long getAmpHours() {
        long base = baseAmperage(tier) / 2;
        if (GTORules.WIRELESS_ENERGY_TIER.isEasy()) return base * 16;
        if (GTORules.WIRELESS_ENERGY_TIER.isExpert()) return base * 3 / 4;
        return base;
    }

    public static long transferAmperage(int tier) {
        long base = baseAmperage(tier);
        if (GTORules.WIRELESS_ENERGY_TIER.isEasy()) return base * 16;
        if (GTORules.WIRELESS_ENERGY_TIER.isExpert()) return base * 3 / 2;
        return base;
    }

    private static long baseAmperage(int tier) {
        return tier <= GTValues.IV ? 64 : tier <= GTValues.UEV ? 512 : 2048L << ((tier - GTValues.UIV) << 1);
    }

    @Override
    public long getAmperage() {
        return AMPERAGE;
    }

    public boolean isGridScaled() {
        return tier >= GTValues.UXV;
    }

    public BigInteger getCapacity() {
        return BigInteger.valueOf(GTValues.VEX[tier]).multiply(BigInteger.valueOf(getAmpHours() * TICKS_PER_HOUR));
    }

    public int getLoss() {
        int level = GTORules.WIRELESS_ENERGY_TIER.level();
        int loss = GTORules.WIRELESS_ENERGY_TIER.isEasy() ? 0 : (GTValues.MAX - tier) << level;
        if (tier < 6) {
            loss += 10 * (level << 2) / tier;
        }
        return loss;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        var capacity = getCapacity();
        tooltip.add(capacity.compareTo(LONG_MAX) <= 0 ? Tips.energy(StatKind.ENERGY_CAPACITY, capacity.longValue(), tier) :
                Tips.stat(StatKind.ENERGY_CAPACITY.line(Component.translatable("gtceu.tip.unit.energy_hours", String.format("%,.1f", (double) getAmpHours()))
                        .withStyle(s -> s.withColor(TipTheme.VALUE)).append(Component.translatable("gtceu.tip.unit.tier", GTValues.VNF[tier])))));
        tooltip.add(Tips.stat(Component.translatable(THROUGHPUT, GTValues.VNF[tier], AMPERAGE)));
        tooltip.add(Tips.stat(Component.translatable(LOSS, (double) getLoss() / 10)));
        tooltip.add(Tips.requirement(Component.translatable(REQUIRES_GLASS, GTValues.VNF[tier])));
        tooltip.add(Tips.detail(Component.translatable(SUPPLIES)));
        if (isGridScaled()) tooltip.add(Tips.bonus(Component.translatable(SCALING)));
        tooltip.add(Tips.control(Component.translatable("gtocore.machine.tooltip.upgrade_action", MultiBlockG.WIRELESS_ENERGY_SUBSTATION.get().getName(), Component.translatable("gtocore.adv_terminal.block_map.wireless_energy_unit"))));
    }

    @Nullable
    public static WirelessEnergyUnitBlock get(int tier) {
        if (tier < 1 || tier > BlockMap.WIRELESS_ENERGY_UNIT.length) {
            return null;
        }
        return (WirelessEnergyUnitBlock) BlockMap.WIRELESS_ENERGY_UNIT[tier - 1];
    }
}
