package com.hepdd.gtmthings.api.machine;

import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;

public interface IPowerSubstationMachine {

    boolean isFormed();

    IEnergyInfoProvider.EnergyInfo getEnergyInfo();
}
