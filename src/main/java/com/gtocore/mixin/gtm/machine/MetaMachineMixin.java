package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.capability.IIWirelessInteractor;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(MetaMachine.class)
public abstract class MetaMachineMixin implements IIWirelessInteractor.IWirelessProvider {}
