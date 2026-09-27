package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.data.IdleReason;

import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.gto.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gtocore.common.data.GTOMachines.ENERGY_OUTPUT_HATCH_16A;
import static com.gtocore.common.data.machines.ManaMachine.WIRELESS_MANA_OUTPUT_HATCH;
import static com.gtocore.data.IdleReason.ORBIT_ONLY_VOLTA;
import static net.minecraft.world.level.block.Blocks.AIR;

public final class PhotovoltaicSailControllerMachine extends AbstractPhotovoltaicMachine {

    public PhotovoltaicSailControllerMachine(MetaMachineBlockEntity holder, int basicRate) {
        super(holder, basicRate);
    }

    @Override
    protected @Nullable IdleReason checkEnvironment() {
        return isInSpace() ? null : ORBIT_ONLY_VOLTA;
    }

    @Override
    protected @Nullable BlockPos updateHighlightArea() {
        if (getFrontFacing().getAxis() == Direction.Axis.Y) {
            return null;
        }
        BlockPos pos = MachineUtils.getOffsetPos(-11, 0, 0, getFrontFacing(), getPos());
        if (getFrontFacing().getAxis() == Direction.Axis.Z) {
            highlightStartPos_1 = pos.offset(-1, 0, -7);
            highlightEndPos_1 = pos.offset(1, 0, -1);
            highlightStartPos_2 = pos.offset(-1, 0, 1);
            highlightEndPos_2 = pos.offset(1, 0, 7);
        } else {
            highlightStartPos_1 = pos.offset(-7, 0, -1);
            highlightEndPos_1 = pos.offset(-1, 0, 1);
            highlightStartPos_2 = pos.offset(1, 0, -1);
            highlightEndPos_2 = pos.offset(7, 0, 1);
        }
        return pos;
    }

    @Override
    protected @Nullable GTRecipeDefinition createGenerationRecipe(Level level, RecipeHandlerUnit unit, int basic) {
        int water = basic / 4;
        if (!unit.matchFluid(GTMaterials.DistilledWater.getFluid(), water)) {
            setIdleReason(Component.translatable("gtceu.recipe_logic.insufficient_in").append(": ").append(GTMaterials.DistilledWater.getLocalizedName()));
            return null;
        }
        int eut = basic << 4;
        if (eut == 0) return null;
        return buildGenerationRecipe(getRecipeBuilder().duration(20).inputFluids(GTMaterials.DistilledWater.getFluid(), water), eut);
    }

    public static BlockPattern getPattern(MultiblockMachineDefinition definition, Supplier<? extends Block> casing, BlockEntry<?> photovoltaicBlock) {
        return FactoryBlockPattern.start(definition)
                .aisle(" CDC ")
                .aisle("CC CC")
                .aisle("C   C")
                .aisle("AAAAA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("AAAAA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("AAAAA")
                .where('A', GTOPredicates.frame(GTMaterials.Aluminium))
                .where('B', blocks(photovoltaicBlock.get()))
                .wherePart('C', blocks(casing.get())
                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1))
                        .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(1))
                        .or(abilities(GTOPartAbility.OUTPUT_MANA).setMaxGlobalLimited(4))
                        .or(abilities(MAINTENANCE).setExactLimit(1)))
                .where('D', controller(definition))
                .where(' ', any())
                .build();
    }

    public static MultiblockShapeInfo getPreview(MultiblockMachineDefinition definition, Supplier<? extends Block> casing, BlockEntry<?> photovoltaicBlock) {
        return MultiblockShapeInfo.builder()
                .aisle("AAAAA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("AAAAA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("ABBBA")
                .aisle("AAAAA")
                .aisle("C   o")
                .aisle("mn pq")
                .aisle(" CDC ")
                .where('A', ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Aluminium))
                .where('B', photovoltaicBlock)
                .where('C', casing)
                .where('m', WIRELESS_MANA_OUTPUT_HATCH[HV], Direction.UP)
                .where('n', ENERGY_OUTPUT_HATCH_16A[HV], Direction.UP)
                .where('o', FLUID_IMPORT_HATCH[HV], Direction.UP)
                .where('p', CONTROL_HATCH, Direction.UP)
                .where('q', MAINTENANCE_HATCH, Direction.UP)
                .where('D', definition.defaultBlockState())
                .where(' ', AIR)
                .build(definition);
    }
}
