package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.api.machine.IMultiFluidRendererMachine;
import com.gtocore.api.pattern.GTOPredicates;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Collections;
import java.util.Set;

public class PigmentMixer extends ElectricMultiblockMachine implements IMultiFluidRendererMachine {

    @SyncToClient(autoUpdate = false)
    final Set<BlockPos> cachedYellowOffsets = new OpenCacheHashSet<>();
    @SyncToClient(autoUpdate = false)
    final Set<BlockPos> cachedCyanOffsets = new OpenCacheHashSet<>();
    @SyncToClient(autoUpdate = false)
    final Set<BlockPos> cachedMagentaOffsets = new OpenCacheHashSet<>();
    @SyncToClient(autoUpdate = false)
    final Set<BlockPos> cachedBlackOffsets = new OpenCacheHashSet<>();
    @SyncToClient(autoUpdate = false)
    final Set<BlockPos> cachedWhiteOffsets = new OpenCacheHashSet<>();

    public PigmentMixer(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        cachedYellowOffsets.addAll(getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.YELLOW, Collections.emptySet()));
        cachedCyanOffsets.addAll(getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.CYAN, Collections.emptySet()));
        cachedMagentaOffsets.addAll(getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.MAGENTA, Collections.emptySet()));
        cachedBlackOffsets.addAll(getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.BLACK, Collections.emptySet()));
        cachedWhiteOffsets.addAll(getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.WHITE, Collections.emptySet()));
        markFieldsForSync("cachedYellowOffsets", "cachedYellowOffsets", "cachedCyanOffsets", "cachedMagentaOffsets", "cachedWhiteOffsets");
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        cachedYellowOffsets.clear();
        cachedCyanOffsets.clear();
        cachedMagentaOffsets.clear();
        cachedBlackOffsets.clear();
        cachedWhiteOffsets.clear();
        markFieldsForSync("cachedYellowOffsets", "cachedYellowOffsets", "cachedCyanOffsets", "cachedMagentaOffsets", "cachedWhiteOffsets");
    }

    @Override
    public Multimap<Fluid, BlockPos> getFluidBlockOffsets() {
        if (getRecipeLogic().isWorking()) {
            Multimap<Fluid, BlockPos> map = Multimaps.newMultimap(new Reference2ObjectOpenHashMap<>(), OpenCacheHashSet::new);
            map.putAll(Wrapper.Yellow, cachedYellowOffsets);
            map.putAll(Wrapper.Cyan, cachedCyanOffsets);
            map.putAll(Wrapper.Magenta, cachedMagentaOffsets);
            map.putAll(Wrapper.Black, cachedBlackOffsets);
            map.putAll(Wrapper.White, cachedWhiteOffsets);
            return map;
        }
        return ImmutableListMultimap.of();
    }

    private static class Wrapper {

        public static final Fluid Yellow = GTMaterials.DyeYellow.getFluid();
        public static final Fluid Cyan = GTMaterials.DyeCyan.getFluid();
        public static final Fluid Magenta = GTMaterials.DyeMagenta.getFluid();
        public static final Fluid Black = GTMaterials.DyeBlack.getFluid();
        public static final Fluid White = GTMaterials.DyeWhite.getFluid();
    }
}
