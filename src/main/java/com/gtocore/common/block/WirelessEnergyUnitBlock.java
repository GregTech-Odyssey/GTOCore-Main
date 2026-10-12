package com.gtocore.common.block;

import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.config.GTORules;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.tooltip.StatKind;
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
        long base = tier <= GTValues.IV ? 64 : tier <= GTValues.UIV ? 256 : 1024L << ((tier - GTValues.UXV) << 1);
        if (GTORules.WIRELESS_ENERGY_TIER.isEasy()) return base * 3 / 2;
        if (GTORules.WIRELESS_ENERGY_TIER.isExpert()) return base / 2;
        return base;
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
        tooltip.add(Tips.energy(StatKind.ENERGY_CAPACITY, getCapacity().min(LONG_MAX).longValue(), tier));
        tooltip.add(Tips.stat(Component.translatable(THROUGHPUT, GTValues.VNF[getIoTier()], AMPERAGE)));
        tooltip.add(Tips.stat(Component.translatable(LOSS, (double) getLoss() / 10)));
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
