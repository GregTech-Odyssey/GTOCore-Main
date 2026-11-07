package com.gtocore.api.ae2.crafting;

import com.gtolib.api.machine.impl.part.CraftingInterfacePartMachine;

import org.jetbrains.annotations.Nullable;

/** Source of a virtual crafting CPU created by an ME supercomputing core. */
public interface ICraftingCpuSource {

    @Nullable
    CraftingInterfacePartMachine gtocore$getSourceMachine();
}
