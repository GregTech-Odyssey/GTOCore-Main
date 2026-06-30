package com.gtocore.mixin.gtm.machine;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import com.hepdd.gtmthings.api.machine.IPowerSubstationMachine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PowerSubstationMachine.class)
public abstract class PowerSubstationMachineMixin implements IPowerSubstationMachine {}
