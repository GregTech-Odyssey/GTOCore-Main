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
    @RegisterLanguage(cn = "安数等于同级存储单元的安时数", en = "Amperage equals the amp-hours of the same-tier storage unit")
    private static final String AMPERAGE_RULE = "gtocore.block.wireless_energy_transfer_unit.amperage_rule";

    private final int tier;

    public WirelessEnergyTransferUnitBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public long getAmperage() {
        var storage = WirelessEnergyUnitBlock.get(tier);
        return storage == null ? 0 : storage.getAmpHours();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Tips.stat(Component.translatable(WirelessEnergyUnitBlock.THROUGHPUT, GTValues.VNF[getIoTier()], FormattingUtil.formatNumbers(getAmperage()))));
        tooltip.add(Tips.feature(Component.translatable(AMPERAGE_RULE)));
        tooltip.add(Tips.feature(Component.translatable(NO_STORAGE)));
        tooltip.add(Tips.control(Component.translatable("gtocore.machine.tooltip.upgrade_action", MultiBlockG.WIRELESS_ENERGY_SUBSTATION.get().getName(), Component.translatable("gtocore.adv_terminal.block_map.wireless_energy_transfer_unit"))));
    }

    @Nullable
    public static WirelessEnergyTransferUnitBlock get(int tier) {
        if (tier < 1 || tier > BlockMap.WIRELESS_ENERGY_TRANSFER_UNIT.length) {
            return null;
        }
        return (WirelessEnergyTransferUnitBlock) BlockMap.WIRELESS_ENERGY_TRANSFER_UNIT[tier - 1];
    }
}
