package com.hepdd.gtmthings.data;

import net.minecraft.world.item.CreativeModeTab;

import com.gto.registrate.util.entry.RegistryEntry;
import com.hepdd.gtmthings.GTMThings;

import static com.gregtechceu.gtceu.common.data.GTMachines.CREATIVE_ENERGY;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class CreativeModeTabs {

    public static final RegistryEntry<CreativeModeTab> CREATIVE_TAB = GTMTHINGS_REGISTRATE
            .defaultCreativeTab("creative", builder -> builder
                    .title(GTMTHINGS_REGISTRATE.addLang("itemGroup", GTMThings.id("creative"), GTMThings.NAME))
                    .icon(CREATIVE_ENERGY::asStack)
                    .build())
            .register();

    public static final RegistryEntry<CreativeModeTab> MORE_MACHINES = GTMTHINGS_REGISTRATE
            .defaultCreativeTab("more_machines", builder -> builder
                    .title(GTMTHINGS_REGISTRATE.addLang("itemGroup", GTMThings.id("more_machines"), GTMThings.NAME))
                    .icon(CustomMachines.ME_EXPORT_BUFFER::asStack)
                    .build())
            .register();

    public static void init() {}
}
