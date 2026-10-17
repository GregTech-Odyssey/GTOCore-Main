package com.gtocore.common.machine.multiblock.steam;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.config.GTORules;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Assembly;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Size;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.ToIntFunction;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.IMPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Steam;

@DataGeneratorScanned
public class LargeSteamSolarBoilerMachine extends WorkableMultiblockMachine implements IExplosionMachine, IDisplayUIMachine, IEnhancedRecipeLogicMachine, ICustomRecipeLogicHolder {

    @RegisterLanguage(cn = "左侧宽度", en = "Left Width")
    private static final String LEFT_NAME = "gtocore.multiblock.large_steam_solar_boiler.left";
    @RegisterLanguage(cn = "控制器正后方一行向左连续的集热管数 + 1", en = "Consecutive heat collector pipes to the left along the row behind the controller, plus 1")
    private static final String LEFT_DESC = "gtocore.multiblock.large_steam_solar_boiler.left.desc";
    @RegisterLanguage(cn = "右侧宽度", en = "Right Width")
    private static final String RIGHT_NAME = "gtocore.multiblock.large_steam_solar_boiler.right";
    @RegisterLanguage(cn = "控制器正后方一行向右连续的集热管数 + 1", en = "Consecutive heat collector pipes to the right along the row behind the controller, plus 1")
    private static final String RIGHT_DESC = "gtocore.multiblock.large_steam_solar_boiler.right.desc";
    @RegisterLanguage(cn = "深度", en = "Depth")
    private static final String BACK_NAME = "gtocore.multiblock.large_steam_solar_boiler.back";
    @RegisterLanguage(cn = "控制器向后连续的集热管数 + 1", en = "Consecutive heat collector pipes behind the controller, plus 1")
    private static final String BACK_DESC = "gtocore.multiblock.large_steam_solar_boiler.back.desc";

    public static final ParamKey LEFT_EDGE = ParamKey.of(LEFT_NAME, LEFT_DESC);
    public static final ParamKey RIGHT_EDGE = ParamKey.of(RIGHT_NAME, RIGHT_DESC);
    public static final ParamKey BACK_EDGE = ParamKey.of(BACK_NAME, BACK_DESC);

    private static final int MAX_LR_DIST = 14, MAX_B_DIST = 29;
    private static final int MIN_LR_DIST = 1, MIN_B_DIST = 3;
    private static final int STEAM_GENERATION_INTERVAL = 20;

    private int lDist, rDist, bDist, sunlit;
    private int steamGenerated;
    private int timing;

    public LargeSteamSolarBoilerMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    public static Structure structure(MultiblockMachineDefinition definition) {
        var symbols = Symbols.create()
                .wherePart('a', blocks(GTBlocks.STEEL_HULL.get())
                        .or(abilities(EXPORT_FLUIDS))
                        .or(abilities(IMPORT_FLUIDS)))
                .where('b', blocks(GTOBlocks.SOLAR_HEAT_COLLECTOR_PIPE_CASING.get()))
                .where('~', controller(definition));
        var rowEnd = new TraceabilityPredicate(state -> !isSolar(ILevel.asyncGetBlockState(state.world, state.getPos().relative(state.controller.self().getFrontFacing().getOpposite()))), null, null);
        var backEnd = new TraceabilityPredicate(state -> !isSolar(state.getBlockState()), null, null);
        return Structure.root(Piece.sized(LargeSteamSolarBoilerMachine::body))
                .symbols(symbols)
                .measure(m -> m.param(LEFT_EDGE).toward(LEFT).until(rowEnd).range(MIN_LR_DIST + 1, MAX_LR_DIST + 1))
                .measure(m -> m.param(RIGHT_EDGE).toward(RIGHT).until(rowEnd).range(MIN_LR_DIST + 1, MAX_LR_DIST + 1))
                .measure(m -> m.param(BACK_EDGE).toward(BACK).until(backEnd).range(MIN_B_DIST + 1, MAX_B_DIST + 1))
                .build();
    }

    private static Piece body(Size size) {
        var dims = dimensions(size::get, false);
        int width = dims[0] + dims[1] + 3;
        var builder = Piece.start(LEFT, UP, FRONT).aisle("a".repeat(width));
        var middle = "a" + "b".repeat(width - 2) + "a";
        for (int i = 0; i < dims[2]; i++) builder.aisle(middle);
        return builder.aisle("a".repeat(dims[1] + 1) + "~" + "a".repeat(dims[0] + 1)).build();
    }

    public static int[] dimensions(ToIntFunction<ParamKey> values, boolean flipped) {
        int left = values.applyAsInt(LEFT_EDGE) - 1;
        int right = values.applyAsInt(RIGHT_EDGE) - 1;
        int back = values.applyAsInt(BACK_EDGE) - 1;
        return flipped ? new int[] { right, left, back } : new int[] { left, right, back };
    }

    @Nullable
    public static int[] dimensions(@Nullable Assembly assembly, boolean flipped) {
        if (assembly == null || !assembly.has(BACK_EDGE)) return null;
        return dimensions(assembly::get, flipped);
    }

    private boolean updateStructureDimensions() {
        var dims = dimensions(getAssembly(), getMultiblockState().isNeededFlip());
        if (dims == null) return false;
        this.lDist = dims[0];
        this.rDist = dims[1];
        this.bDist = dims[2];
        return true;
    }

    private static boolean isSolar(BlockState state) {
        return state.is(GTOBlocks.SOLAR_HEAT_COLLECTOR_PIPE_CASING.get());
    }

    @Override
    public boolean matchRecipeOutput(GTRecipe recipe) {
        return true;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        updateStructureDimensions();
    }

    @Override
    public boolean keepSubscribing() {
        return true;
    }

    private int calculateSunlit(Level level) {
        int count = 0;
        Direction front = getFrontFacing();
        Direction back = front.getOpposite();
        Direction left = front.getCounterClockWise();
        Direction right = left.getOpposite();

        BlockPos pos = getPos();
        for (int i = 1; i <= bDist; i++) {
            if (hasClearSky(level, pos.relative(back, i))) count++;
            for (int j = 1; j <= lDist; j++) if (hasClearSky(level, pos.relative(back, i).relative(left, j))) count++;
            for (int j = 1; j <= rDist; j++) if (hasClearSky(level, pos.relative(back, i).relative(right, j))) count++;
        }
        return count;
    }

    private boolean isAppropriateDimensionAndTime(Level world) {
        if (GTODimensions.isVoid(world.dimension())) return true;
        if (!world.isDay()) {
            IdleReason.DAYTIME_ONLY.setReason(this);
            return false;
        }
        return true;
    }

    private static boolean hasClearSky(Level world, BlockPos pos) {
        BlockPos checkPos = pos.above();
        if (!world.canSeeSky(checkPos)) return false;
        Biome biome = world.getBiome(checkPos).value();
        boolean hasPrecipitation = world.isRaining() && (biome.warmEnoughToRain(checkPos) || biome.coldEnoughToSnow(checkPos));
        return !hasPrecipitation;
    }

    private GTRecipeDefinition createNextRecipe() {
        int steamAmount = GTORules.STEAM_SOLAR_RATE.get() * sunlit * STEAM_GENERATION_INTERVAL;
        int waterAmount = (int) Math.ceil((double) steamAmount / ConfigHolder.INSTANCE.machines.largeBoilers.steamPerWater);

        if (waterAmount <= 0 || steamAmount <= 0) return null;
        if (!matchFluid(Fluids.WATER, waterAmount)) {
            IdleReason.WATER_SHORT.setReason(this, waterAmount, -1);
            doExplosion(2);
            return null;
        }

        steamGenerated = steamAmount;
        return getRecipeBuilder()
                .inputFluids(Fluids.WATER, waterAmount)
                .outputFluids(Steam.getFluid(), steamAmount)
                .duration(STEAM_GENERATION_INTERVAL)
                .build();
    }

    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtocore.machine.large_steam_solar_boiler.size", lDist + rDist + 3, bDist + 2));
            textList.add(Component.translatable("gtocore.machine.large_steam_solar_boiler.heat_collector_pipe", sunlit));
            textList.add(Component.translatable("gtocore.machine.large_steam_solar_boiler.steam_production", steamGenerated));
        } else {
            textList.add(Component.translatable("gtceu.top.invalid_structure"));
        }
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        Level level = getLevel();
        if (level != null && isFormed()) {
            if (!isAppropriateDimensionAndTime(level)) return null;
            if (timing == 0) {
                sunlit = calculateSunlit(level);
                timing = 10;
            } else {
                timing--;
            }
            if (sunlit > 0) {
                return createNextRecipe();
            }
            IdleReason.NO_SUNLIGHT.setReason(this);
        }
        return null;
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }
}
