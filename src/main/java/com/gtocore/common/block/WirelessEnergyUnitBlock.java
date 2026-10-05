package com.gtocore.common.block;

import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.config.GTORules;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
public class WirelessEnergyUnitBlock extends Block {

    @RegisterLanguage(cn = "容量：%s × %s 安时（%s EU）", en = "Capacity: %s × %s Ah (%s EU)")
    private static final String CAPACITY = "gtocore.block.wireless_energy_unit.capacity";
    @RegisterLanguage(cn = "网络中同级或更高的单元共有 N 个时，每个按基础容量的 N 倍计", en = "With N units of this tier or higher in the grid, each counts as N times its base capacity")
    private static final String SCALING = "gtocore.block.wireless_energy_unit.scaling";
    private static final long TICKS_PER_HOUR = 20 * 3600;

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
        tooltip.add(Component.translatable("gtocore.machine.tooltip.upgrade_action", MultiBlockG.WIRELESS_ENERGY_SUBSTATION.get().getName(), Component.translatable("gtocore.adv_terminal.block_map.wireless_energy_unit")).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(CAPACITY, GTValues.VN[tier], FormattingUtil.formatNumbers(getAmpHours()), FormattingUtil.formatNumbers(getCapacity())));
        if (isGridScaled()) tooltip.add(Component.translatable(SCALING).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtceu.machine.fluid_drilling_rig.depletion", (double) getLoss() / 10));
    }

    @Nullable
    public static WirelessEnergyUnitBlock get(int tier) {
        if (tier < 1 || tier > BlockMap.WIRELESS_ENERGY_UNIT.length) {
            return null;
        }
        return (WirelessEnergyUnitBlock) BlockMap.WIRELESS_ENERGY_UNIT[tier - 1];
    }

    public record BlockData(@Nullable WirelessEnergyUnitBlock block, BlockPos pos) {}
}
