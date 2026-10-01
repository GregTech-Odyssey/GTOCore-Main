package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.trait.ElectricTrait;
import com.gtolib.api.recipe.GTORecipeModifiers;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IElectricMachine;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Slot;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

@DataGeneratorScanned
public final class NeutronVortexMachine extends NeutronActivatorMachine implements IElectricMachine {

    @RegisterLanguage(cn = "能源接收段", en = "Energy Acceptor Section")
    private static final String RECEIVER_NAME = "gtocore.multiblock.neutron_vortex.energy_receiver";
    @RegisterLanguage(cn = "搭建后可启用能源接收器，由电力按配方直接设定中子动能", en = "Allows the Energy Acceptor to be enabled, which sets kinetic energy from the recipe using power")
    private static final String RECEIVER_DESC = "gtocore.multiblock.neutron_vortex.energy_receiver.desc";
    @RegisterLanguage(cn = "能源接收段：已搭建", en = "Energy Acceptor Section: Built")
    private static final String RECEIVER_BUILT = "gtocore.multiblock.neutron_vortex.energy_receiver.built";
    @RegisterLanguage(cn = "能源接收段：未搭建，能源接收器不可用", en = "Energy Acceptor Section: Not built; the Energy Acceptor is unavailable")
    private static final String RECEIVER_MISSING = "gtocore.multiblock.neutron_vortex.energy_receiver.missing";

    public static final ParamKey ENERGY_RECEIVER = ParamKey.of(RECEIVER_NAME, RECEIVER_DESC);
    private static final PortKey RECEIVER_OUT = PortKey.of("receiver_out");
    private static final PortKey RECEIVER_IN = PortKey.of("receiver_in");

    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean energy;

    private final ElectricTrait electricTrait;

    public NeutronVortexMachine(MetaMachineBlockEntity holder) {
        super(holder);
        electricTrait = new ElectricTrait(this);
    }

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (isEnergyMode()) {
            recordTarget(recipe);
            int ev = (recipe.data.getInt(GTORecipeDataKeys.EV_MAX) + recipe.data.getInt(GTORecipeDataKeys.EV_MIN)) * 5;
            eV = ev * 100000;
            recipe.duration = recipe.duration / 5;
            recipe.eut = ev;
            return GTORecipeModifiers.parallel(this, unit, recipe);
        }
        return super.getRealRecipe(unit, recipe);
    }

    boolean isEnergyMode() {
        return energy && hasStructurePart(ENERGY_RECEIVER);
    }

    boolean isEnergySwitchOn() {
        return energy;
    }

    void setEnergySwitch(boolean on) {
        energy = on;
    }

    boolean hasEnergyReceiver() {
        return hasStructurePart(ENERGY_RECEIVER);
    }

    @Override
    int getMaxAccelerators() {
        return 4;
    }

    @Override
    boolean consumesKineticEnergy() {
        return false;
    }

    @Override
    public void afterWorking() {
        eV = 0;
        super.afterWorking();
    }

    @Override
    protected void neutronEnergyUpdate() {
        if (isEnergyMode()) return;
        super.neutronEnergyUpdate();
    }

    @Override
    public boolean handleTickRecipe(@NotNull GTRecipe recipe) {
        return true;
    }

    @Override
    public void collectIssues(IssueSink sink) {
        if (isEnergyMode()) return;
        super.collectIssues(sink);
    }

    @Override
    protected double getEVtMultiplier() {
        return 1;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        height = 100;
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable(hasEnergyReceiver() ? RECEIVER_BUILT : RECEIVER_MISSING));
    }

    public static Structure structure(MultiblockMachineDefinition definition) {
        return Structure.root(mainPiece())
                .symbols(Symbols.create()
                        .where('@', any())
                        .where('$', any())
                        .where('A', blocks(GTOBlocks.NAQUADAH_REINFORCED_PLANT_CASING.get()))
                        .where('B', blocks(GTOBlocks.DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                        .where('C', GTOPredicates.frame(GTOMaterials.Quantanium))
                        .where('D', GTOPredicates.frame(GTOMaterials.Vibranium))
                        .where('E', blocks(GTOBlocks.STRONTIUM_CARBONATE_CERAMIC_RAY_ABSORBING_MECHANICAL_CUBE.get()))
                        .where('F', blocks(GTBlocks.FUSION_GLASS.get()))
                        .where('G', blocks(GTOBlocks.SPEEDING_PIPE.get()))
                        .wherePart('H', blocks(GTOBlocks.NAQUADAH_REINFORCED_PLANT_CASING.get())
                                .or(autoAbilities(definition.getRecipeTypes()))
                                .or(blocks(GTOMachines.NEUTRON_SENSOR.get()).setMaxGlobalLimited(1))
                                .or(abilities(GTOPartAbility.NEUTRON_ACCELERATOR).setMaxGlobalLimited(4))
                                .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(1))
                                .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                                .or(abilities(MAINTENANCE).setExactLimit(1)))
                        .where('I', blocks(GTOBlocks.ENDERIUM_BOROSILICATE_GLASS.get()))
                        .where('J', blocks(GTOBlocks.AMPROSIUM_ACTIVE_CASING.get()))
                        .where('K', controller(definition))
                        .where('L', blocks(GTOBlocks.DIMENSIONAL_BRIDGE_CASING.get()))
                        .where('M', blocks(GTOBlocks.CONTAINMENT_FIELD_GENERATOR.get()))
                        .where('N', blocks(GTOBlocks.SPS_CASING.get()))
                        .where('O', blocks(GTOBlocks.RESTRAINT_DEVICE.get()))
                        .where('P', blocks(GTOBlocks.ACCELERATED_PIPELINE.get()))
                        .where('Q', blocks(GTOBlocks.DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                        .where('R', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                        .where('S', blocks(GTOBlocks.TITANIUM_BOROSILICATE_GLASS.get())))
                .atPort(RECEIVER_OUT, Slot.optional(receiverPiece(), RECEIVER_IN).count(ENERGY_RECEIVER))
                .build();
    }

    private static Piece mainPiece() {
        return Piece.start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                .aisle("    AHHHHHA    ", "    AHHHHHA    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    AAAAAAA    ", "    AAAAAAA    ")
                .aisle("   ABBBBBBBA   ", "   A       A   ", "               ", "         D     ", "        D      ", "       D       ", "      D        ", "     D         ", "               ", "               ", "         C     ", "        C      ", "       C       ", "      C        ", "     C         ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "         D     ", "        D      ", "       D       ", "      D        ", "     D         ", "               ", "               ", "         C     ", "        C      ", "       C       ", "      C        ", "     C         ", "               ", "   A       A   ", "   ABBBBBBBA   ")
                .aisle("   ABEEEEEBA   ", "   A FFFFF A   ", "     FFFFFD    ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "    DFFFFF     ", "     FFFFFC    ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "    CFFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFFD    ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "    DFFFFF     ", "     FFFFFC    ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "    CFFFFF     ", "   A FFFFF A   ", "   ABEEEEEBA   ")
                .aisle("  ABBEEEEEBBA  ", "  A  FGFGFD A  ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGFC    ", "    DFGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "    CFGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGFD    ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGFC    ", "    DFGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "  A CFGFGF  A  ", "  ABBEEEEEBBA  ")
                .aisle("  ABEEEEEEEBA  ", "  A FFFFFFF A  ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFFC   ", "    FFFFFFF    ", "    FFFFFFF    ", "   DFFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "   CFFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFFD   ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFFC   ", "    FFFFFFF    ", "    FFFFFFF    ", "   DFFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "  A FFFFFFF A  ", "  ABEEEEEEEBA  ")
                .aisle(" ABBEEEEEEEBBA ", " A  FGFGFGF  A ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGFC   ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "   DFGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "   CFGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGFD   ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGFC   ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "   DFGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", " A  FGFGFGF  A ", " ABBEEEEEEEBBA ")
                .aisle(" ABEEEEEEEEEBA ", " A FFFFFFFFF A ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFFC  ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "  DFFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "  CFFFFFFFFF   ", "   FFFFFFFFFD  ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFFC  ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "  DFFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", " A FFFFFFFFF A ", " ABEEEEEEEEEBA ")
                .aisle("ABBEEEEEEEEEBBA", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGFC A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A DFGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGFD A", "A CFGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGFC A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A DFGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "ABBEEEEEEEEEBBA")
                .aisle("HBEEEEEEEEEEEBA", "H FFFIIIIIFFF A", "  FFFIIIIIFFF  ", "  FFFIIIIIFFFC ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", " DFFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFFD ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", " CFFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFFC ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", " DFFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "A FFFIIIIIFFF A", "ABEEEEEEEEEEEBA")
                .aisle("HBEEEEEEEEEEEBA", "H FGFIJIJIFGF A", "  FGFIJIJIFGFC ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", " DFGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGFD ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", " CFGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGFC ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", " DFGFIJIJIFGF  ", "A FGFIJIJIFGF A", "ABEEEEEEEEEEEBA")
                .aisle("HBEEEEEEEEEEEBA", "KCFFFIIIIIFFFCA", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF $", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", " DFFFIIIIIFFFD ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", " CFFFIIIIIFFFC ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "ADFFFIIIIIFFFDA", "ABEEEEEEEEEEEBA")
                .aisle("HBEEEEEEEEEEEBA", "H FGFIJIJIFGF A", " CFGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGFD ", "  FGFIJIJIFGF  ", " DFGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGFC ", "  FGFIJIJIFGF  ", " CFGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGF  ", "  FGFIJIJIFGFD ", "A FGFIJIJIFGF A", "ABEEEEEEEEEEEBA")
                .aisle("HBEEEEEEEEEEEBA", "H FFFIIIIIFFF A", "  FFFIIIIIFFF  ", " CFFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFFD ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", " DFFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFFC ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", " CFFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFF  ", "  FFFIIIIIFFFD ", "  FFFIIIIIFFF  ", "A FFFIIIIIFFF A", "ABEEEEEEEEEEEBA")
                .aisle("ABBEEEEEEEEEBBA", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A CFGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGFD A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A DFGFGFGFGF  A", "A  FGFGFGFGFC A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A CFGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGFD A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "A  FGFGFGFGF  A", "ABBEEEEEEEEEBBA")
                .aisle(" ABEEEEEEEEEBA ", " A FFFFFFFFF A ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "  CFFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFFD  ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFFC  ", "  DFFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "  CFFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFFD  ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", "   FFFFFFFFF   ", " A FFFFFFFFF A ", " ABEEEEEEEEEBA ")
                .aisle(" ABBEEEEEEEBBA ", " A  FGFGFGF  A ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "   CFGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGFD   ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGFC   ", "    FGFGFGF    ", "    FGFGFGF    ", "   DFGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "   CFGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGFD   ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", "    FGFGFGF    ", " A  FGFGFGF  A ", " ABBEEEEEEEBBA ")
                .aisle("  ABEEEEEEEBA  ", "  A FFFFFFF A  ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "   CFFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFFD   ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFFC   ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "   DFFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "   CFFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFFD   ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "    FFFFFFF    ", "  A FFFFFFF A  ", "  ABEEEEEEEBA  ")
                .aisle("  ABBEEEEEBBA  ", "  A DFGFGF  A  ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "    CFGFGF     ", "     FGFGFD    ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGFC    ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "    DFGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "    CFGFGF     ", "     FGFGFD    ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "     FGFGF     ", "  A  FGFGFC A  ", "  ABBEEEEEBBA  ")
                .aisle("   ABEEEEEBA   ", "   A FFFFF A   ", "    DFFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFFD    ", "    CFFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFFC    ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "    DFFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFFD    ", "    CFFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFF     ", "     FFFFFC    ", "   A FFFFF A   ", "   ABEEEEEBA   ")
                .aisle("   ABBBBBBBA   ", "   A       A   ", "               ", "     D         ", "      D        ", "       D       ", "        D      ", "         D     ", "               ", "               ", "     C         ", "      C        ", "       C       ", "        C      ", "         C     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "     D         ", "      D        ", "       D       ", "        D      ", "         D     ", "               ", "               ", "     C         ", "      C        ", "       C       ", "        C      ", "         C     ", "               ", "   A       A   ", "   ABBBBBBBA   ")
                .aisle("    AHHHHHA    ", "    AHHHHHA    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    A     A    ", "    AAAAAAA    ", "    AAAAAAA    ")
                .port('$', RECEIVER_OUT, RelativeDirection.BACK)
                .build();
    }

    private static Piece receiverPiece() {
        return Piece.start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                .aisle(" BBBBB    ", " BRRRB    ", " BRRRB    ", " BRRRB    ", " BBBBB    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " BBBBB    ", " BRRRB    ", " BRRRB    ", " BRRRB    ", " BBBBB    ")
                .aisle("BBBBBBB   ", "M RRR M   ", "B RRR B   ", "M RRR M   ", "BBBBBBB   ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "BBBBBBB   ", "M RRR M   ", "B RRR B   ", "M RRR M   ", "BBBBBBB   ")
                .aisle("BBBBBBBB  ", "MM    MM  ", "BB    BB  ", "MM    MM  ", "BBBLBLBB  ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "BBBLBLBB  ", "MM    MM  ", "BB    BB  ", "MM    MM  ", "BBBBBBBB  ")
                .aisle(" BBBBBBB  ", " M     M  ", " B     B  ", " M     M  ", " BBBBBBB  ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", " BBBBBBB  ", " M     M  ", " B     B  ", " M     M  ", " BBBBBBB  ")
                .aisle(" BBBBBBBB ", " MM    MM ", " BB    BB ", " MM    MM ", " BBLBLBBB ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", " BBLBLBBB ", " MM    MM ", " BB    BB ", " MM    MM ", " BBBBBBBB ")
                .aisle("  BBBBBBB ", "  M     M ", "  B     B ", "  M     M ", "  BBBBBBB ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "  BBBBBBB ", "  M     M ", "  B     B ", "  M     M ", "  BBBBBBB ")
                .aisle("  BBBBBBBB", "  MBNNNNNB", "  BBNNNNNB", "  MBNNNNNB", "  BBBQQQBB", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "  BBBQQQBB", "  MBNNNNNB", "  BBNNNNNB", "  MBNNNNNB", "  BBBBBBBB")
                .aisle("SSSBBBBBBB", "SSSBOOOOOL", "SSSBNNNNNB", "   BOOOOOL", "   BBQQQBB", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   BBQQQBB", "   BOOOOOL", "SSSBNNNNNB", "SSSBOOOOOL", "SSSBBBBBBB")
                .aisle("SSSBBBBBBB", "PPPPPPPPPL", "SSSBNNNNNB", "   BNNNNNL", "   BBQQQBB", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "   BBQQQBB", "   BNNNNNL", "SSSBNNNNNB", "PPPPPPPPPL", "SSSBBBBBBB")
                .aisle("SSSBBBBBBB", "SSSBOOOOOB", "SSSBNNNNNB", "   BOOOOOB", "   BBQQQBB", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "   BBQQQBB", "   BOOOOOB", "SSSBNNNNNB", "SSSBOOOOOB", "SSSBBBBBBB")
                .aisle("SSSBBBBBBB", "PPPPPPPPPB", "SSSBNNNNNB", "@  BNNNNNB", "   BBQQQBB", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "   BBQQQBB", "   BNNNNNB", "SSSBNNNNNB", "PPPPPPPPPB", "SSSBBBBBBB")
                .aisle("SSSBBBBBBB", "SSSBOOOOOB", "SSSBNNNNNB", "   BOOOOOB", "   BBQQQBB", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "   BBQQQBB", "   BOOOOOB", "SSSBNNNNNB", "SSSBOOOOOB", "SSSBBBBBBB")
                .aisle("SSSBBBBBBB", "PPPPPPPPPL", "SSSBNNNNNB", "   BNNNNNL", "   BBQQQBB", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "   BBQQQBB", "   BNNNNNL", "SSSBNNNNNB", "PPPPPPPPPL", "SSSBBBBBBB")
                .aisle("SSSBBBBBBB", "SSSBOOOOOL", "SSSBNNNNNB", "   BOOOOOL", "   BBQQQBB", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   B      ", "   BBQQQBB", "   BOOOOOL", "SSSBNNNNNB", "SSSBOOOOOL", "SSSBBBBBBB")
                .aisle("  BBBBBBBB", "  MBNNNNNB", "  BBNNNNNB", "  MBNNNNNB", "  BBBQQQBB", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "         B", "  BBBQQQBB", "  MBNNNNNB", "  BBNNNNNB", "  MBNNNNNB", "  BBBBBBBB")
                .aisle("  BBBBBBB ", "  M     M ", "  B     B ", "  M     M ", "  BBBBBBB ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "  BBBBBBB ", "  M     M ", "  B     B ", "  M     M ", "  BBBBBBB ")
                .aisle(" BBBBBBBB ", " MM    MM ", " BB    BB ", " MM    MM ", " BBLBLBBB ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", " BBLBLBBB ", " MM    MM ", " BB    BB ", " MM    MM ", " BBBBBBBB ")
                .aisle(" BBBBBBB  ", " M     M  ", " B     B  ", " M     M  ", " BBBBBBB  ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", " BBBBBBB  ", " M     M  ", " B     B  ", " M     M  ", " BBBBBBB  ")
                .aisle("BBBBBBBB  ", "MM    MM  ", "BB    BB  ", "MM    MM  ", "BBBLBLBB  ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "BBBLBLBB  ", "MM    MM  ", "BB    BB  ", "MM    MM  ", "BBBBBBBB  ")
                .aisle("BBBBBBB   ", "M RRR M   ", "B RRR B   ", "M RRR M   ", "BBBBBBB   ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "          ", "BBBBBBB   ", "M RRR M   ", "B RRR B   ", "M RRR M   ", "BBBBBBB   ")
                .aisle(" BBBBB    ", " BRRRB    ", " BRRRB    ", " BRRRB    ", " BBBBB    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " B   B    ", " BBBBB    ", " BRRRB    ", " BRRRB    ", " BRRRB    ", " BBBBB    ")
                .port('@', RECEIVER_IN, RelativeDirection.FRONT)
                .build();
    }

    @Override
    public @NotNull IEnergyContainer getEnergyContainer() {
        return electricTrait.getEnergyContainer();
    }
}
