package com.gtocore.common.machine.multiblock.electric.processing;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Slot;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.MAINTENANCE;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.PARALLEL_HATCH;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CompoundExtremeCoolingMachine extends CrossRecipeMultiblockMachine implements IIssueProvider {

    @RegisterLanguage(cn = "等离子冷凝翼", en = "Plasma Condensing Wings")
    private static final String WINGS_NAME = "gtocore.multiblock.compound_extreme_cooling_unit.wings";
    @RegisterLanguage(cn = "搭建后，切换到等离子冷凝模式时可运行等离子冷凝配方", en = "Required to run plasma condensing recipes in plasma condensing mode")
    private static final String WINGS_DESC = "gtocore.multiblock.compound_extreme_cooling_unit.wings.desc";
    @RegisterLanguage(cn = "等离子冷凝翼：已搭建", en = "Plasma Condensing Wings: Built")
    private static final String WINGS_BUILT = "gtocore.multiblock.compound_extreme_cooling_unit.wings.built";
    @RegisterLanguage(cn = "等离子冷凝翼：未搭建，无法运行等离子冷凝配方", en = "Plasma Condensing Wings: Not built; plasma condensing recipes are unavailable")
    private static final String WINGS_MISSING = "gtocore.multiblock.compound_extreme_cooling_unit.wings.missing";
    @RegisterLanguage(cn = "已搭建", en = "Built")
    private static final String WINGS_BUILT_VALUE = "gtocore.multiblock.compound_extreme_cooling_unit.wings.built_value";
    @RegisterLanguage(cn = "未搭建", en = "Not built")
    private static final String WINGS_MISSING_VALUE = "gtocore.multiblock.compound_extreme_cooling_unit.wings.missing_value";
    private static final Component BUILT_TEXT = Component.translatable(WINGS_BUILT_VALUE);
    private static final Component MISSING_TEXT = Component.translatable(WINGS_MISSING_VALUE);

    public static final ParamKey PLASMA_WINGS = ParamKey.of(WINGS_NAME, WINGS_DESC);
    private static final PortKey WING_OUT = PortKey.of("wing_out");
    private static final PortKey WING_IN = PortKey.of("wing_in");

    public CompoundExtremeCoolingMachine(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
    }

    @Override
    public boolean checkConditions(RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (recipe.recipeType == GTORecipeTypes.PLASMA_CONDENSER_RECIPES) {
            if (getRecipeType() != GTORecipeTypes.PLASMA_CONDENSER_RECIPES) {
                IdleReason.NOT_APPLICABLE.report(this, IssueStage.CONDITION, recipe);
                return false;
            }
            if (!hasStructurePart(PLASMA_WINGS)) {
                IdleReason.PLASMA_WINGS_MISSING.report(this, IssueStage.CONDITION, recipe);
                return false;
            }
        }
        return super.checkConditions(unit, recipe);
    }

    @Override
    public void collectIssues(IssueSink sink) {
        if (getRecipeType() == GTORecipeTypes.PLASMA_CONDENSER_RECIPES && !hasStructurePart(PLASMA_WINGS)) IdleReason.PLASMA_WINGS_MISSING.collect(sink);
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (!MultiblockPage.isScreenText()) textList.add(Component.translatable(hasStructurePart(PLASMA_WINGS) ? WINGS_BUILT : WINGS_MISSING));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addLine(WINGS_NAME, () -> hasStructurePart(PLASMA_WINGS) ? BUILT_TEXT : MISSING_TEXT)
                .bindLevel(this::wingsLevel)
                .tooltips(WINGS_DESC);
    }

    private Level wingsLevel() {
        if (hasStructurePart(PLASMA_WINGS)) return Level.GOOD;
        return getRecipeType() == GTORecipeTypes.PLASMA_CONDENSER_RECIPES ? Level.WARNING : Level.NORMAL;
    }

    public static Structure structure(MultiblockMachineDefinition definition) {
        return Structure.root(mainPiece())
                .symbols(Symbols.create()
                        .where('@', any())
                        .where('A', blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                        .where('B', GTOPredicates.frame(GTMaterials.WatertightSteel))
                        .where('C', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                        .where('D', blocks(GTOBlocks.LASER_COOLING_CASING.get()))
                        .where('E', blocks(GTOBlocks.LASER_CASING.get()))
                        .where('F', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                        .where('G', blocks(GTOBlocks.ANTIFREEZE_HEATPROOF_MACHINE_CASING.get()))
                        .where('H', blocks(GTOBlocks.HOLLOW_CASING.get()))
                        .where('I', GTOPredicates.frame(GTMaterials.Neutronium))
                        .where('J', blocks(GTOBlocks.AMPROSIUM_PIPE_CASING.get()))
                        .where('K', blocks(GTBlocks.FILTER_CASING.get()))
                        .wherePart('L', blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get())
                                .or(GTOPredicates.autoThreadLaserAbilities(definition.getRecipeTypes()))
                                .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                                .or(abilities(MAINTENANCE).setExactLimit(1)))
                        .where('M', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                        .where('N', blocks(GTBlocks.FUSION_GLASS.get()))
                        .where('O', controller(definition))
                        .where('P', blocks(GCYMBlocks.CASING_LASER_SAFE_ENGRAVING.get()))
                        .where('Q', blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
                        .where('R', blocks(GTOBlocks.OPTICAL_RESONANCE_CHAMBER.get()))
                        .where('S', blocks(GTBlocks.BATTERY_ULTIMATE_UHV.get())))
                .atPort(WING_OUT, Slot.optional(wingPiece(), WING_IN).count(PLASMA_WINGS))
                .build();
    }

    private static Piece mainPiece() {
        return Piece.start(RelativeDirection.RIGHT, RelativeDirection.UP, RelativeDirection.BACK)
                .aisle("   GG     GG       GG     GG   ", "   GGIIIIIGG       GGIIIIIGG   ", "   GGJJJJJGG       GGJJJJJGG   ", "   GGIIIIIGG LLLLL GGIIIIIGG   ", "   GGKKKKKGG LLOLL GGKKKKKGG   ", "   GGIIIIIGG LLLLL GGIIIIIGG   ", "   GGJJJJJGG       GGJJJJJGG   ", "   GGIIIIIGG       GGIIIIIGG   ", "   GG     GG       GG     GG   ")
                .aisle(" AAGGAAAAAGGLLLLLLLGGAAAAAGGAA ", " AAAAAAAAAAAAMMMMMAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAMMMMMAAAAAAAAAAAA ", " AAGGAAAAAGGLLLLLLLGGAAAAAGGAA ")
                .aisle(" AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ")
                .aisle(" AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", "AA                           AA", "AA                           AA", "AA                           AA", "AA HH     HH       HH     HH AA", "AA                           AA", "AA                           AA", "AA                           AA", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ")
                .aisle(" AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", "AAAAAA   AAAAI   IAAAA   AAAAAA", "BC           GGGGG           CB", "BC           GNNNG           CB", "BC HH     HH GNNNG HH     HH CB", "BC           GNNNG           CB", "BC           GGGGG           CB", "AAAAAA   AAAAI   IAAAA   AAAAAA", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ")
                .aisle(" AACCAAAAACCAAAAAAACCAAAAACCAA ", "AAAAAA   AAAA     AAAA   AAAAAA", "BC           GNNNG           CB", " DFFFFFFFFFFFF   FFFFFFFFFFFFD ", " E HH     HH G   G HH     HH E ", " DFFFFFFFFFFFF   FFFFFFFFFFFFD ", "BC           GNNNG           CB", "AAAAAA   AAAA     AAAA   AAAAAA", " AACCAAAAACCAAAAAAACCAAAAACCAA ")
                .aisle(" AACCAAAAACCAAAAAAACCAAAAACCAA ", "AAAAAA   AAAA     AAAA   AAAAAA", "BC           GNNNG           CB", " E           G   G           E ", " E HH     HH G   G HH     HH E ", " E           G   G           E ", "BC           GNNNG           CB", "AAAAAA   AAAA     AAAA   AAAAAA", " AACCAAAAACCAAAAAAACCAAAAACCAA ")
                .aisle(" AACCAAAAACCAAAAAAACCAAAAACCAA ", "AAAAAA   AAAA     AAAA   AAAAAA", "BC           GNNNG           CB", " DFFFFFFFFFFFF   FFFFFFFFFFFFD ", " E HH     HH G   G HH     HH E ", " DFFFFFFFFFFFF   FFFFFFFFFFFFD ", "BC           GNNNG           CB", "AAAAAA   AAAA     AAAA   AAAAAA", " AACCAAAAACCAAAAAAACCAAAAACCAA ")
                .aisle(" AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", "AAAAAA   AAAAI   IAAAA   AAAAAA", "BC           GGGGG           CB", "BC           GNNNG           CB", "BC HH     HH GNNNG HH     HH CB", "BC           GNNNG           CB", "BC           GGGGG           CB", "AAAAAA   AAAAI   IAAAA   AAAAAA", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ")
                .aisle(" AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", "AA                           AA", "AA                           AA", "AA                           AA", "AA HH     HH       HH     HH AA", "AA                           AA", "AA                           AA", "AA                           AA", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ")
                .aisle(" AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " A FF     FF       FF     FF A ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ")
                .aisle(" AAGGAAAAAGGLLLLLLLGGAAAAAGGAA ", " AAAAAAAAAAAAMMMMMAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAA ", " AAAAAAAAAAAAMMMMMAAAAAAAAAAAA ", " AAGGAAAAAGGLLLLLLLGGAAAAAGGAA ")
                .aisle("   GG     GG       GG     GG   ", "   GGIIIIIGG       GGIIIIIGG   ", "   GGJJJJJGG       GGJJJJJGG   ", "   GGIIIIIGG       GGIIIIIGG   ", "   GGKKKKKGG       GGKKKKKGG   ", "   GGIIIIIGG       GGIIIIIGG   ", "   GGJJJJJGG       GGJJJJJGG   ", "   GGIIIIIGG       GGIIIIIGG   ", "   GG     GG       GG     GG   ")
                .port('O', WING_OUT, RelativeDirection.BACK)
                .build();
    }

    private static Piece wingPiece() {
        return Piece.start(RelativeDirection.RIGHT, RelativeDirection.UP, RelativeDirection.BACK)
                .aisle("                                                               ", "                                                               ", "                                                               ", "                                                               ", "                               @                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ")
                .aisle("                                                               ", "                                                               ", "  Q    Q    Q                                     Q    Q    Q  ", "  Q    Q    Q                                     Q    Q    Q  ", "  Q    Q    Q                                     Q    Q    Q  ", "  Q    Q    Q                                     Q    Q    Q  ", "  Q    Q    Q                                     Q    Q    Q  ", "                                                               ", "                                                               ")
                .aisle("                                                               ", "  Q    Q    Q                                     Q    Q    Q  ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "  Q    Q    Q                                     Q    Q    Q  ", "                                                               ")
                .aisle("  Q    Q    Q                                     Q    Q    Q  ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "BCE          CB                                 BC          ECB", "BCERRRRRRRRRRCB                                 BCRRRRRRRRRRECB", "BCE          CB                                 BC          ECB", "BCERRRRRRRRRRCB                                 BCRRRRRRRRRRECB", "BCE          CB                                 BC          ECB", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "  Q    Q    Q                                     Q    Q    Q  ")
                .aisle("  Q    Q    Q                                     Q    Q    Q  ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "BCERRRRRRRRRRCB                                 BCRRRRRRRRRRECB", " EEDDDDDDDDDDDNNN                             NNNDDDDDDDDDDDEE ", " EEIIIIIIIIIIE                                   EIIIIIIIIIIEE ", " EEDDDDDDDDDDDNNN                             NNNDDDDDDDDDDDEE ", "BCERRRRRRRRRRCB                                 BCRRRRRRRRRRECB", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "  Q    Q    Q                                     Q    Q    Q  ")
                .aisle("  Q    Q    Q                                     Q    Q    Q  ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "BCE          CB                                 BC          ECB", " EEIIIIIIIIIIE                                   EIIIIIIIIIIEE ", " EES         E                                   E         SEE ", " EEIIIIIIIIIIE                                   EIIIIIIIIIIEE ", "BCE          CB                                 BC          ECB", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "  Q    Q    Q                                     Q    Q    Q  ")
                .aisle("  Q    Q    Q                                     Q    Q    Q  ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "BCERRRRRRRRRRCB                                 BCRRRRRRRRRRECB", " EEDDDDDDDDDDDNNN                             NNNDDDDDDDDDDDEE ", " EEIIIIIIIIIIE                                   EIIIIIIIIIIEE ", " EEDDDDDDDDDDDNNN                             NNNDDDDDDDDDDDEE ", "BCERRRRRRRRRRCB                                 BCRRRRRRRRRRECB", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "  Q    Q    Q                                     Q    Q    Q  ")
                .aisle("  Q    Q    Q                                     Q    Q    Q  ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "BCE          CB                                 BC          ECB", "BCERRRRRRRRRRCB                                 BCRRRRRRRRRRECB", "BCE          CB                                 BC          ECB", "BCERRRRRRRRRRCB                                 BCRRRRRRRRRRECB", "BCE          CB                                 BC          ECB", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "  Q    Q    Q                                     Q    Q    Q  ")
                .aisle("                                                               ", "  Q    Q    Q                                     Q    Q    Q  ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", " PPPPPPPPPPPPP                                   PPPPPPPPPPPPP ", "  Q    Q    Q                                     Q    Q    Q  ", "                                                               ")
                .aisle("                                                               ", "                                                               ", "  Q    Q    Q                                     Q    Q    Q  ", "  Q    Q    Q                                     Q    Q    Q  ", "  Q    Q    Q                                     Q    Q    Q  ", "  Q    Q    Q                                     Q    Q    Q  ", "  Q    Q    Q                                     Q    Q    Q  ", "                                                               ", "                                                               ")
                .port('@', WING_IN, RelativeDirection.FRONT)
                .build();
    }
}
