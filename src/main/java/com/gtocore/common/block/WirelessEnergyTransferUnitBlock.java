package com.gtocore.common.block;

import com.gtocore.common.data.machines.MultiBlockG;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

@Getter
@DataGeneratorScanned
public class WirelessEnergyTransferUnitBlock extends Block implements WirelessEnergyTowerUnit {

    @RegisterLanguage(cn = "不储存电量，只提高所在能源塔的吞吐", en = "Stores no energy; only raises the throughput of its tower")
    private static final String NO_STORAGE = "gtocore.block.wireless_energy_transfer_unit.no_storage";
    @RegisterLanguage(cn = "为当前能源塔的传输上限提升×%s", en = "Raises the throughput of its tower by ×%s")
    private static final String SUPER_TRANSFER = "gtocore.block.wireless_energy_transfer_unit.super_transfer";

    private final int tier;
    private final boolean superUnit;

    public WirelessEnergyTransferUnitBlock(Properties properties, int tier) {
        this(properties, tier, false);
    }

    public WirelessEnergyTransferUnitBlock(Properties properties, int tier, boolean superUnit) {
        super(properties);
        this.tier = tier;
        this.superUnit = superUnit;
    }

    @Override
    public long getAmperage() {
        return superUnit ? 0 : WirelessEnergyUnitBlock.transferAmperage(tier);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (superUnit) {
            tooltip.add(Tips.bonus(Component.translatable(SUPER_TRANSFER, getSuperMultiplierHalf() / 2.0)));
            tooltip.add(Tips.feature(Component.translatable(WirelessEnergyUnitBlock.MULTIPLIER_ONLY)));
        } else {
            tooltip.add(Tips.stat(Component.translatable(WirelessEnergyUnitBlock.THROUGHPUT, GTValues.VNF[tier], FormattingUtil.formatNumbers(getAmperage()))));
            tooltip.add(Tips.feature(Component.translatable(NO_STORAGE)));
        }
        tooltip.add(Tips.requirement(Component.translatable(WirelessEnergyUnitBlock.REQUIRES_GLASS, GTValues.VNF[tier])));
        tooltip.add(Tips.detail(Component.translatable(WirelessEnergyUnitBlock.SUPPLIES)));
        tooltip.add(Tips.control(Component.translatable("gtocore.machine.tooltip.upgrade_action", MultiBlockG.WIRELESS_ENERGY_SUBSTATION.get().getName(), Component.translatable(superUnit ? "gtocore.adv_terminal.block_map.super_wireless_energy_transfer_unit" : "gtocore.adv_terminal.block_map.wireless_energy_transfer_unit"))));
    }

    @Nullable
    public static WirelessEnergyTransferUnitBlock get(int tier) {
        if (tier < 1 || tier > BlockMap.WIRELESS_ENERGY_TRANSFER_UNIT.length) {
            return null;
        }
        return (WirelessEnergyTransferUnitBlock) BlockMap.WIRELESS_ENERGY_TRANSFER_UNIT[tier - 1];
    }
}
