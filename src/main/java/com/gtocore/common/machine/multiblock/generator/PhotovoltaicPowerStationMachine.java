package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.data.IdleReason;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.gto.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gtocore.common.data.GTOMachines.ENERGY_OUTPUT_HATCH_16A;
import static com.gtocore.common.data.machines.ManaMachine.WIRELESS_MANA_OUTPUT_HATCH;
import static com.gtocore.data.IdleReason.SURFACE_ONLY_VOLTA;
import static net.minecraft.world.level.block.Blocks.AIR;

public final class PhotovoltaicPowerStationMachine extends AbstractPhotovoltaicMachine {

    public PhotovoltaicPowerStationMachine(MetaMachineBlockEntity holder, int basicRate) {
        super(holder, basicRate);
    }

    @Override
    protected @Nullable IdleReason checkEnvironment() {
        return isInSpace() ? SURFACE_ONLY_VOLTA : null;
    }

    @Override
    protected @Nullable BlockPos updateHighlightArea() {
        if (getFrontFacing().getAxis() == Direction.Axis.Y || getUpwardsFacing() != Direction.NORTH) {
            return null;
        }
        BlockPos pos = MachineUtils.getOffsetPos(1, 4, getFrontFacing(), getPos());
        if (getFrontFacing().getAxis() == Direction.Axis.Z) {
            highlightStartPos_1 = pos.offset(-3, 0, 1);
            highlightEndPos_1 = pos.offset(3, 0, 2);
            highlightStartPos_2 = pos.offset(-3, 0, -2);
            highlightEndPos_2 = pos.offset(3, 0, -1);
        } else {
            highlightStartPos_1 = pos.offset(1, 0, -3);
            highlightEndPos_1 = pos.offset(2, 0, 3);
            highlightStartPos_2 = pos.offset(-2, 0, -3);
            highlightEndPos_2 = pos.offset(-1, 0, 3);
        }
        return pos;
    }

    @Override
    protected @Nullable GTRecipeDefinition createGenerationRecipe(Level level, RecipeHandlerUnit unit, int basic) {
        int eut = (int) (basic * (GTODimensions.isVoid(level.dimension()) ? 14 : GTOUtils.getSunIntensity(level.getDayTime()) * 15 / 100 * (level.isRaining() ? (level.isThundering() ? 0.3f : 0.7f) : 1)));
        if (eut == 0) {
            setIdleReason(Component.translatable("recipe.condition.daytime.day.tooltip"));
            return null;
        }
        return buildGenerationRecipe(getRecipeBuilder().duration(20), eut);
    }

    public static BlockPattern getPatternCommon(MultiblockMachineDefinition definition, Supplier<? extends Block> casing, BlockEntry<?> photovoltaicBlock) {
        return FactoryBlockPattern.start(definition, RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                .aisle("       ", "       ", "       ", "       ", "AAAAAAA")
                .aisle("       ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("   D   ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("   D   ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("  ~CD  ", "   C   ", "   C   ", " AACAA ", "ABBCBBA")
                .aisle("   D   ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("   D   ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("       ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("       ", "       ", "       ", "       ", "AAAAAAA")
                .where('A', frames(GTMaterials.Aluminium))
                .where('B', blocks(photovoltaicBlock.get()))
                .where('C', blocks(casing.get()))
                .wherePart('D', blocks(casing.get())
                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1))
                        .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(1))
                        .or(abilities(GTOPartAbility.OUTPUT_MANA).setMaxGlobalLimited(4))
                        .or(abilities(MAINTENANCE).setExactLimit(1)))
                .where('~', controller(definition))
                .where(' ', any())
                .build();
    }

    public static MultiblockShapeInfo getPatternCommonPreview(MultiblockMachineDefinition definition, Supplier<? extends Block> casing, BlockEntry<?> photovoltaicBlock) {
        return MultiblockShapeInfo.builder()
                .aisle("       ", "       ", "       ", "       ", "AAAAAAA")
                .aisle("       ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("   q   ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("   p   ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("  ~CC  ", "   C   ", "   C   ", " AACAA ", "ABBCBBA")
                .aisle("   n   ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("   m   ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("       ", "       ", "       ", "       ", "ABBCBBA")
                .aisle("       ", "       ", "       ", "       ", "AAAAAAA")
                .where('A', ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Aluminium))
                .where('B', photovoltaicBlock)
                .where('C', casing)
                .where('m', WIRELESS_MANA_OUTPUT_HATCH[HV], Direction.WEST)
                .where('n', ENERGY_OUTPUT_HATCH_16A[HV], Direction.WEST)
                .where('p', CONTROL_HATCH, Direction.WEST)
                .where('q', MAINTENANCE_HATCH, Direction.WEST)
                .where('~', definition, Direction.WEST)
                .where(' ', AIR)
                .build(definition);
    }

    @Override
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public void onLoad() {
        super.onLoad();
        if (isInSpace() && getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.enqueueTask(serverLevel, this::migrateToSailController, 1);
        }
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private @Nullable MultiblockMachineDefinition getSailControllerDefinition() {
        var definition = getDefinition();
        if (definition == GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_ENERGETIC) return GeneratorMultiblock.PHOTOVOLTAIC_SAIL_CONTROLLER_ENERGETIC;
        if (definition == GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_PULSATING) return GeneratorMultiblock.PHOTOVOLTAIC_SAIL_CONTROLLER_PULSATING;
        if (definition == GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_VIBRANT) return GeneratorMultiblock.PHOTOVOLTAIC_SAIL_CONTROLLER_VIBRANT;
        return null;
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private void migrateToSailController() {
        if (isRemoved() || !(getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = getPos();
        if (!level.isLoaded(pos) || level.getBlockEntity(pos) != holder) return;
        var target = getSailControllerDefinition();
        if (target == null) return;
        BlockState oldState = getBlockState();
        BlockState newState = target.defaultBlockState();
        for (Property<?> property : oldState.getProperties()) {
            if (newState.hasProperty(property)) newState = copyProperty(oldState, newState, property);
        }
        CompoundTag tag = holder.saveWithoutMetadata();
        if (!(newState.getBlock() instanceof EntityBlock entityBlock) || !(entityBlock.newBlockEntity(pos, newState) instanceof MetaMachineBlockEntity newHolder)) return;
        newHolder.load(tag);
        getMachineStorage().storage.setStackInSlot(0, ItemStack.EMPTY);
        coverContainer.onUnload();
        for (Direction side : GTUtil.DIRECTIONS) {
            coverContainer.setCoverAtSideinternal(null, side);
        }
        level.setBlock(pos, newState, Block.UPDATE_ALL);
        level.setBlockEntity(newHolder);
        newHolder.setChanged();
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
