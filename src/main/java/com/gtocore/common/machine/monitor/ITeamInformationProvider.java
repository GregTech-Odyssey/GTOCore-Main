package com.gtocore.common.machine.monitor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.util.List;

public interface ITeamInformationProvider extends IInformationProvider, IBindable {

    @Override
    default DisplayComponentList provideInformation() {
        DisplayComponentList infoList = IInformationProvider.super.provideInformation();
        if (getUUID() == null) {
            infoList.addIfAbsent(
                    DisplayRegistry.OWNER.id(),
                    Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.1")
                            .withStyle(ChatFormatting.RED)
                            .getVisualOrderText());
        } else {
            infoList.addIfAbsent(
                    DisplayRegistry.OWNER.id(),
                    Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0",
                            TeamUtil.GetName(this.getLevel(), this.getUUID()))
                            .withStyle(ChatFormatting.AQUA)
                            .getVisualOrderText());
        }
        return infoList;
    }

    @Override
    default List<ResourceLocation> getAvailableRLs() {
        var superList = IInformationProvider.super.getAvailableRLs();
        superList.add(DisplayRegistry.OWNER.id());
        return superList;
    }
}
