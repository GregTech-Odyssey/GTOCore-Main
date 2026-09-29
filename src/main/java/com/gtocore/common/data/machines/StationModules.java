package com.gtocore.common.data.machines;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.machine.multiblock.electric.space.spacestaion.ISpacePredicateMachine;

import com.gtolib.api.registries.MultiblockBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;

import java.util.function.Function;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gtocore.utils.register.MachineRegisterUtils.multiblock;

final class StationModules {

    private StationModules() {}

    static MultiblockBuilder module(String name, String cn, Function<MetaMachineBlockEntity, ? extends MultiblockControllerMachine> factory) {
        return multiblock(name, cn, factory)
                .mountedOn(GTOMachineProtocols.STATION_JUNCTION)
                .workableInSpace();
    }

    static Symbols ringSymbols(Symbols symbols) {
        return symbols
                .where('A', blocks(GTOBlocks.TITANIUM_ALLOY_FRAME_INTERNAL.get()))
                .where('C', blocks(GTOBlocks.ALUMINUM_ALLOY_7050_SUPPORT_MECHANICAL_BLOCK.get()))
                .where('D', blocks(GTOBlocks.SPACECRAFT_DOCKING_CASING.get()))
                .where('E', blocks(GTOBlocks.ALUMINUM_ALLOY_2090_SKIN_MECHANICAL_BLOCK.get()))
                .where('F', GTOPredicates.frame(GTOMaterials.StainlessSteel316))
                .where('p', ISpacePredicateMachine.innerBlockPredicate.get());
    }
}
