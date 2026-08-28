package com.gtocore.common.data.machines;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.client.renderer.machine.ExResearchPartRenderer;
import com.gtocore.client.renderer.machine.OverlayActiveMachineRenderer;
import com.gtocore.common.block.BlockMap;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.common.data.translation.GTOMachineTooltipsA;
import com.gtocore.common.machine.multiblock.electric.SupercomputingCenterMachine;
import com.gtocore.common.machine.multiblock.electric.research.*;
import com.gtocore.common.machine.multiblock.part.research.*;
import com.gtocore.common.machine.multiblock.part.research.computer.*;

import com.gtolib.GTOCore;
import com.gtolib.api.registries.GTOMachineBuilder;
import com.gtolib.utils.MultiBlockFileReader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredActiveMachineRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DataAccessHatchMachine;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.machines.GTResearchMachines.OVERHEAT_TOOLTIPS;
import static com.gtocore.api.machine.part.GTOPartAbility.HEAT_CONDUCTION;
import static com.gtocore.api.pattern.GTOPredicates.DataKeys.HIGH_TEMP_INTERFACE;
import static com.gtocore.api.pattern.GTOPredicates.DataKeys.LOW_TEMP_INTERFACE;
import static com.gtocore.common.data.GTORecipeTypes.*;
import static com.gtocore.utils.register.MachineRegisterUtils.machine;
import static com.gtocore.utils.register.MachineRegisterUtils.multiblock;
import static com.gtolib.api.registries.GTORegistration.GTO;
import static com.gtolib.utils.register.BlockRegisterUtils.addLang;

public final class ExResearchMachines {

    public static void init() {}

    /////////////////////////////////////
    // *********** 算力机器 *********** //
    /////////////////////////////////////

    public static final MachineDefinition COMPUTATIONAL_DATA_HOLDER = machine("computational_data_holder", "计算数据支架", ComputationalDataHolderMachine::new)
            .tier(LuV)
            .tooltips(GTOMachineTooltipsA.ComputationalDataHolder)
            .tooltips(GTOMachineTooltipsA.DataHolderUniversal)
            .abilities(HPCA_COMPONENT)
            .allRotation()
            .renderer(() -> new OverlayActiveMachineRenderer(GTCEu.id("block/advanced_computer_casing"), GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();

    public static final MultiblockMachineDefinition SUPERCOMPUTING_CENTER = multiblock("supercomputing_center", "运算中心", SupercomputingCenterMachine::new)
            .tooltips(GTOMachineTooltips.SupercomputingTooltips)
            .nonYAxisRotation()
            .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
            .block(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING)
            .pattern(definition -> FactoryBlockPattern.start(definition)
                    .aisle(" BBB                   BBB  ", " BKB                   BKB  ", " BKB                   BKB  ", " BKB                   BKB  ", " BKB                   BKB  ", " BKBBBBBBBBBBBBBBBBBBBBBKB  ", " BKB                   BKB  ", " BKB                   BKB  ", " BKB                   BKB  ", " BKBBBBBBBBBBBBBBBBBBBBBKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKBBBBBBBBBBBBBBBBBBBBBKB  ", " BKBBBBBBBBBBBBBBBBBBBBBKB  ", " BKB                   BKB  ", " BKB                   BKB  ", " BKBAAAAAAAAAAAAAAAAAAABKB  ", " BBB                   BBB  ", "                            ")
                    .aisle(" BIBAAAAAAAAAAAAAAAAAAABIBF ", " BLB   GG         GG   BLBF ", " BLB   GG         GG   BLBF ", " BLB   GG         GG   BLBF ", " BLBAAAAAAAAAAAAAAAAAAABLBF ", " BNNNNNNNNNNNNNNNNNNNNNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNFMVVVVVVVVVVVVVMFNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBNKKKKKKKKKKKKKKKKKNBLBF ", " BLBGGGGGGGGGGGGGGGGGGGBLB  ", " BBBGGGGGGGGGGGGGGGGGGGBBB  ", " BBBBBBBBBBBBBBBBBBBBBBBBB  ")
                    .aisle(" BJBJJJJJJJJJJJJJJJJJJJBIB  ", " BJBJJJJJJJJJJJJJJJJJJJBBB  ", " BJBJJJJJJJJJJJJJJJJJJJBBB  ", " BJBJJJJJJJJJJJJJJJJJJJBBBBB", " BJBJJJJJJJJJJJJJJJJJJJBBB  ", " BNBJJJJJJJJJJJJJJJJJJNBBB  ", " BBBJJJJJJJJJJJJJJJJJJJBBB  ", " BBBJJJJJJJJJJJJJJJJJJJBBBHH", " BBBJJJJJJJJJJJJJJJJJJJBBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBBF ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBGGGGGGGGGGGGGGGGGGGBBB  ", " BBBBBBBBBBBBBBBBBBBBBBBBB  ")
                    .aisle(" AAQQQQQQQQQQQQQQQQQQQQGGGG ", "  AJJJJJJJJJJJJJJJJJJJGGGGG ", "  AJJJJJJJJJJJJJJJJJJJGGGGG ", "  AJJJJJJJJJJJJJJJJJJJGGGGGB", " DAJJJJJJJJJJJJJJJJJJJDDDDD ", " ENJJJJJJJJJJJJJJJJJJJNNNNN ", " ENJJJJJJJJJJJJJJJJJJJJJNJJ ", " ENJJJJJJJJJJJJJJJJJJJJJNJJH", " BNJJJJJJJJJJJJJJJJJJJJJNJJ ", " FNQQQQQQQQQQQQQQQQQQQQQNEF ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", " BNQ                   QNB  ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  N                     N   ", " BN                     NB  ", "  Q                     Q   ", "  Q                     Q   ", " HQ                     QH  ", "  QGGGGGGGGGGGGGGGGGGGGGQ   ", " BBB                   BBB  ")
                    .aisle(" CJQQQQQQQQQQQQQQQQQQQQQQQG ", " CJ                   FF  G ", " CJ                   FF  G ", " CJ                   FF  GB", " DJ                   FFFFD ", " ENPPPPPPPPPPPPPPPPPPBFFK N ", " EO                   FFK J ", " EK                   FFK JH", " BK                   FFK J ", " FKQQQQQQQQQQQQQQQQQQQQQKEF ", "  DQPTTTTTTU   UTTTTTTPQD   ", "  DQPTTTTTTU   UTTTTTTPQD   ", "  DQPTTTTTTU   UTTTTTTPQD   ", " BKQPTTTTTTU   UTTTTTTPQKB  ", "  KQPTTTTTTU   UTTTTTTPQK   ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", "  K                     K   ", " BK                     KB  ", "  Q                     Q   ", "  Q                     Q   ", " HQ                     QH  ", "  QGGGGGGGGGGGGGGGGGGGGGQ   ", " BBB                   BBB  ")
                    .aisle(" AAQQQQQQQQQQQQQQQQQQQQQQQGA", "  A                       GA", "  A                       GA", "  A                       GB", " DA                   FFFFDA", " ENPPPPPPPPPPPPPPPPPPB  K NA", " EO                     K JA", " EK                     K JH", " BK                     K J ", " FKQQQQQQQQQQQQQQQQQQQQQKEF ", "  DQSSSSSSSU   USSSSSSSQD   ", "  DQSSSSSSSU   USSSSSSSQD   ", "  DQSSSSSSSU   USSSSSSSQD   ", " BKQSSSSSSSU   USSSSSSSQKB  ", "  KQSSSSSSSU   USSSSSSSQK   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", "  K                     K   ", " BK                     KB  ", "  Q                     Q   ", "  Q                     Q   ", " HQ                     QH  ", "  QGGGGGGGGGGGGGGGGGGGGGQ   ", " BBB                   BBB  ")
                    .aisle(" CJQQQQQQQQQQQQQQQQQQQQQQQG ", " CJ                       G ", " CJ                       G ", " CJ                       GB", " DJ                   FFFFD ", " ENPPPPPPPPPPPPPPPPPPB  K N ", " EO                     K J ", " EK                     K JH", " BK                     K J ", " FKQQQQQQQQQQQQQQQQQQQQQKEF ", "  DQ                   QD   ", "  DQ                   QD   ", "  DQ                   QD   ", " BKQ                   QKB  ", "  KQ                   QK   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  OQ                   QO   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  OQ                   QO   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  OQ                   QO   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  OQ                   QO   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  OQ                   QO   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  OQ                   QO   ", " GGQ                   QGG  ", "  DQ                   QD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", "  K                     K   ", " BK                     KB  ", "  Q                     Q   ", "  Q                     Q   ", " HQ                     QH  ", "  QGGGGGGGGGGGGGGGGGGGGGQ   ", " BBB                   BBB  ")
                    .aisle(" AAQQQQQQQQQQQQQQQQQQQQQQQGA", "  A                       GA", "  A                       GA", "  A                       GB", " DA                   FFFFDA", " ENPPPPPPPPPPPPPPPPPPB  K NA", " EO                     K JA", " EK                     K JH", " BK                     K J ", " FKQQQQQQQQQQQQQQQQQQQQQKEF ", "  DQSSSSSSSU   USSSSSSSQD   ", "  DQSSSSSSSU   USSSSSSSQD   ", "  DQSSSSSSSU   USSSSSSSQD   ", " BKQSSSSSSSU   USSSSSSSQKB  ", "  KQSSSSSSSU   USSSSSSSQK   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  OQSSSSSSSU   USSSSSSSQO   ", " GGQSSSSSSSU   USSSSSSSQGG  ", "  DQSSSSSSSU   USSSSSSSQD   ", "  KQQQQQQQQQQQQQQQQQQQQQK   ", "  K                     K   ", " BK                     KB  ", "  Q                     Q   ", "  Q                     Q   ", " HQ                     QH  ", "  QGGGGGGGGGGGGGGGGGGGGGQ   ", " BBB                   BBB  ")
                    .aisle(" CJQQQQQQQQQQQQQQQQQQQQQQQG ", " CJ                       G ", " CJ                       G ", " CJ                       GB", " DJ                   FFFFD ", " ENPPPPPPPPPPPPPPPPPPB  K N ", " EO                     K J ", " EK                     K JH", " BK                     K J ", " FKQQQQQQQQQQQQQQQQQQQQQKEF ", "  DQPTTTTTTU   UTTTTTTPQD   ", "  DQPTTTTTTU   UTTTTTTPQD   ", "  DQPTTTTTTU   UTTTTTTPQD   ", " BKQPTTTTTTU   UTTTTTTPQKB  ", "  KQPTTTTTTU   UTTTTTTPQK   ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APPPTTTTTTU   UTTTTTTPPPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " APQPTTTTTTU   UTTTTTTPQPA  ", " AKQQQQQQQQQQQQQQQQQQQQQKA  ", "  K                     K   ", " BK                     KB  ", "  Q                     Q   ", "  Q                     Q   ", " HQ                     QH  ", "  QGGGGGGGGGGGGGGGGGGGGGQ   ", " BBB                   BBB  ")
                    .aisle(" AAQQQQQQQQQQQQQQQQQQQQQQQG ", "  A                       G ", "  A                       G ", "  A                       GB", " DA                   FFFFD ", " ENPPPPPPPPPPPPPPPPPPB  NNN ", " EN                     NJJ ", " EN                     NJJH", " BN                     NJJ ", " FNQQQQQQQQQQQQQQQQQQQQQNEF ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", " BNQ                   QNB  ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQ                   QN   ", "  NQQQQQQQQQQQQQQQQQQQQQN   ", "  N                     N   ", " BN                     NB  ", "  Q                     Q   ", "  Q                     Q   ", " HQ                     QH  ", "  QGGGGGGGGGGGGGGGGGGGGGQ   ", " BBB                   BBB  ")
                    .aisle(" BJQQQQQQQQQQQQQQQQQQQQQQQBB", " BJ                   BBBBBB", " BJ                   BBBBBB", " BJ                   BBBBBB", " BJ                   BBBBBB", " BNPPPPPPPPPPPPPPPPPPBBBBBNB", " BBB                  BBBBBB", " BBB                  BBBBBB", " BBB                  BBBBBB", " BBBQQQQQQQQQQQQQQQQQQQBBBFC", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBQQQQQQQQQQQQQQQQQQQBBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBB                   BBB  ", " BBBGGGGGGGGGGGGGGGGGGGBBB  ", " BBBBBBBBBBBBBBBBBBBBBBBBB  ")
                    .aisle("ABBQQQQQQQQQQQQQQQQQQQQQQQBB", "ABB                       BB", "ABB                       BB", "ABB                       BB", "ABB                       BB", "ABNNNNNNNNNNNNNNNNNNNNNNNNNB", "ABBBNBBBBBBBBBBBBBBBBBNBBBBB", "ABLBNKKKKKKKKKKKKKKKKKNBLBBB", "ABLBNKKKKKKKKKKKKKKKKKNBLBBB", "ABLBNKKKKKKKKKKKKKKKKKNBLBFC", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNFMVVVVVVVVVVVVVMFNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", "ABLBNKKKKKKKKKKKKKKKKKNBLB  ", " BLBGGGGGGGGGGGGGGGGGGGBLB  ", " BBBGGGGGGGGGGGGGGGGGGGBBB  ", " BBBBBBBBBBBBBBBBBBBBBBBBB  ")
                    .aisle(" BIQQQQQQQQQQQQQQQQQQQQQQQBB", " BG                       BB", " BG                       BB", " BI                       BB", " BM                       BB", " BMMMMMMMMMMMMMMMMMMMMMMMMNB", " BJJJJJJJJJJJJJJJJJJJJJBBBBB", " BBB                   BBBBB", " BBB                   BBBBB", " BBBBBBBBBBBBBBBBBBBBBBBBBFC", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB  U   H     H   U  BKB  ", " BKB   UUUUUUUUUUUUU   BKB  ", " BKB                   BKB  ", " BKBBBBBBBBBBBBBBBBBBBBBKB  ", " BKBBBBBBBBBBBBBBBBBBBBBKB  ", " BKB                   BKB  ", " BKB                   BKB  ", " BKBAAAAAAAAAAAAAAAAAAABKB  ", " BBB                   BBB  ", "                            ")
                    .aisle("  IQQQQQQQQQQQQQQQQQQQQQQQI ", "  G                       J ", "  G                       J ", "  I                       J ", " DM                       IE", " EMMMMMMMMMMMMMMMMMMMMMMMMNE", " EJGGGGGGGGGGGGGGGGGGGGGGGBE", " E                        F ", " B                        F ", "                          F ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ")
                    .aisle("  IQQQQQQQQQQQQQQQQQQQQQQQI ", "  G                       J ", "  G                       J ", "  I                       J ", " DM                       IE", " EMMMMMMMMMMMMMMMMMMMMMMMMNE", " EJGHHHHHHHHHHHHHHHHHHHHHGBE", " E                        F ", " B                          ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ")
                    .aisle("  IQQQQQQQQQQQQQQQQQQQQQQQI ", "  G                       J ", "  G                       J ", "  I                       J ", " DM                       IE", " EMMMMMMMMMMMMMMMMMMMMMMMMNE", " EJGHHHHHHHHHHHHHHHHHHHHHGBE", " E                        F ", " B                          ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ")
                    .aisle("  IQQQQQQQQQQQQQQQQQQQQQQQI ", "  G                       J ", "  G                       J ", "  I                       J ", " DM                       IE", " EMMMMMMMMMMMMMMMMMMMMMMMMNE", " EJGHHHHHHHHHHHHHHHHHHHHHGBE", " E                        F ", " B                          ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ")
                    .aisle("  IQQQQQQQQQQQQQQQQQQQQQQQI ", "  G                       J ", "  G                       J ", "  I                       J ", " DM                       IE", " EMMMMMMMMMMMMMMMMMMMMMMMMNE", " EJGGGGGGGGGGGGGGGGGGGGGGGBE", " E                        F ", " B                          ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ")
                    .aisle(" BIBIIIIIIIIIIIIIIIIQQQQQQBB", " BGBRDDRRDDRRDDRIIIIPPPPPPBB", " BGBRDDRRDDRRDDRIIIIPPPPPPBB", " BIBRDDRRDDRRDDRIIIIPPPPPPBB", " BBBBBBBBBBBBBBBBBBBPPPPPPBB", " BBBBBBBBBBBBBBOBBBBMMMMMMBB", " BBBJJJJJJJJJJJJJBBBJJJJJJBB", " BBB             BBB     BBB", " BBB             BBB     BBB", "                          H ", "                          H ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ")
                    .aisle(" BIBAAAAAAAAAAAAABBBRRRRRBBB", " BLB            ABLBRRWRRBLB", " BLB            ABLBRRRRRBLB", " BLB            ABLBRRRRRBLB", " BLBCCCCCCCCCCCCABLBIIIIIBLB", " BLBOOOOOOOOOOOOABLBBBBBBBLB", " BLBAAAAAAAAAAAAABLBBBBBBBLB", " BBBFFFFFFFFFFFFFBBBFFFFFBBB", " BBB             BBB     BBB", "                          BB", "                          B ", "                          B ", "                          H ", "                          H ", "                          X ", "                          X ", "                          Y ", "                          Y ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ")
                    .aisle(" BBB             BBB     BBB", " BBB             BBB     BBB", " BBB             BBB     BBB", " BBB             BBB     BBB", " BBBEEEEEEEEEEEEEBBBEEEEEBBB", " BBBEEEEEEEEEEEEEBBBEEEEEBBB", " BBBEEEEEEEEEEEEEBBBEEEEEBBB", " BBB             BBB     BBB", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ", "                            ")
                    .where('A', blocks(GTOBlocks.NAQUADAH_ALLOY_CASING.get()))
                    .where('B', blocks(GTOBlocks.IRIDIUM_CASING.get()))
                    .where('C', GTOPredicates.frame(GTOMaterials.BabbittAlloy))
                    .where('D', blocks(GTOBlocks.STRONTIUM_CARBONATE_CERAMIC_RAY_ABSORBING_MECHANICAL_CUBE.get()))
                    .where('E', blocks(GTBlocks.MACHINE_CASING_UHV.get()))
                    .where('F', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                    .where('G', blocks(GTOBlocks.ANTIFREEZE_HEATPROOF_MACHINE_CASING.get()))
                    .where('H', GTOPredicates.frame(GTOMaterials.HastelloyN))
                    .where('I', blocks(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                    .where('J', blocks(GTOBlocks.PRESSURE_CONTAINMENT_CASING.get()))
                    .where('K', GTOPredicates.absBlocks())
                    .where('L', GTOPredicates.frame(GTMaterials.Ruridit))
                    .where('M', blocks(GTOBlocks.LITHIUM_OXIDE_CERAMIC_HEAT_RESISTANT_SHOCK_RESISTANT_MECHANICAL_CUBE.get()))
                    .where('N', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where('O', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                    .where('P', blocks(GTOBlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.get()))
                    .where('Q', blocks(GTOBlocks.MC_NYLON_TENSILE_MECHANICAL_SHELL.get()))
                    .where('R', blocks(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get())
                            .or(blocks(GTOMachines.THERMAL_CONDUCTOR_HATCH.get()).setMaxGlobalLimited(1))
                            .or(abilities(IMPORT_ITEMS))
                            .or(abilities(IMPORT_FLUIDS))
                            .or(abilities(EXPORT_ITEMS))
                            .or(abilities(EXPORT_FLUIDS))
                            .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                            .or(abilities(COMPUTATION_DATA_TRANSMISSION).setMaxGlobalLimited(1))
                            .or(blocks(COMPUTATIONAL_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                            .or(abilities(MAINTENANCE).setExactLimit(1)))
                    .where('S', GTOPredicates.tierBlock(BlockMap.COMPUTER_HEAT_MAP, GTORecipeDataKeys.COMPUTER_HEAT_TIER))
                    .where('T', abilities(GTOPartAbility.COMPUTING_COMPONENT, HPCA_COMPONENT))
                    .where('U', GTOPredicates.tierBlock(BlockMap.COMPUTER_CASING_MAP, GTORecipeDataKeys.COMPUTER_CASING_TIER))
                    .where('V', GTOPredicates.glass())
                    .where('W', controller(definition))
                    .where('X', blocks(Blocks.ANDESITE_WALL))
                    .where('Y', blocks(Blocks.IRON_BARS))
                    .where(' ', any())
                    .build())
            .workableCasingRenderer(GTOCore.id("block/casings/oxidation_resistant_hastelloy_n_mechanical_casing"), GTCEu.id("block/multiblock/large_miner"))
            .register();

    public static final MachineDefinition NICH_EMPTY_COMPONENT = registerHPCAPart(
            "nich_empty_component", "空NICH组件",
            holder -> new ExResearchEmptyPartMachine(holder, 3), false, false, 3)
            .register();

    public static final MachineDefinition NICH_COMPUTING_COMPONENTS = registerHPCAPart(
            "nich_computing_components", "NICH计算组件",
            holder -> new ExResearchComputationPartMachine(holder, 3), true, true, 3)
            .tooltips(
                    Component.translatable("gtceu.machine.hpca.component_general.upkeep_eut", GTValues.VA[ZPM]),
                    Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.UHV]),
                    Component.translatable("gtceu.machine.hpca.component_type.computation_cwut", 64),
                    Component.translatable("gtceu.machine.hpca.component_type.computation_cooling", 16))
            .tooltipBuilder(OVERHEAT_TOOLTIPS)
            .register();

    public static final MachineDefinition NICH_COOLING_COMPONENTS = registerHPCAPart(
            "nich_cooling_components", "NICH冷却组件",
            holder -> new ExResearchCoolerPartMachine(holder, 3), true, false, 3)
            .tooltips(Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.UV]),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_active"),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_active_coolant",
                            80, GTMaterials.Helium.getLocalizedName()),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_cooling", 8))
            .register();

    public static final MachineDefinition NICH_BRIDGE_COMPONENT = registerHPCAPart(
            "nich_bridge_component", "NICH桥接组件",
            holder -> new ExResearchBridgePartMachine(holder, 3), true, false, 3)
            .tooltips(Component.translatable("gtceu.machine.hpca.component_type.bridge"),
                    Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.UHV]))
            .register();

    public static final MachineDefinition GWCA_EMPTY_COMPONENT = registerHPCAPart(
            "gwca_empty_component", "空GWCA组件",
            holder -> new ExResearchEmptyPartMachine(holder, 4), false, false, 4)
            .register();

    public static final MachineDefinition GWCA_COMPUTING_COMPONENTS = registerHPCAPart(
            "gwca_computing_components", "GWCA计算组件",
            holder -> new ExResearchComputationPartMachine(holder, 4), true, true, 4)
            .tooltips(
                    Component.translatable("gtceu.machine.hpca.component_general.upkeep_eut", GTValues.VA[GTValues.UV]),
                    Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.UEV]),
                    Component.translatable("gtceu.machine.hpca.component_type.computation_cwut", 1024),
                    Component.translatable("gtceu.machine.hpca.component_type.computation_cooling", 256))
            .tooltipBuilder(OVERHEAT_TOOLTIPS)
            .register();

    public static final MachineDefinition GWCA_COOLING_COMPONENTS = registerHPCAPart(
            "gwca_cooling_components", "GWCA冷却组件",
            holder -> new ExResearchCoolerPartMachine(holder, 4), true, false, 4)
            .tooltips(Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.UHV]),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_active"),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_active_coolant",
                            320, GTMaterials.Helium.getLocalizedName()),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_cooling", 64))
            .register();

    private static GTOMachineBuilder registerHPCAPart(String name, String cn,
                                                      Function<MetaMachineBlockEntity, MetaMachine> constructor,
                                                      boolean activeTexture,
                                                      boolean damagedTexture,
                                                      int tire) {
        addLang(name, cn);
        return GTO.machine(name, constructor)
                .allRotation()
                .abilities(GTOPartAbility.COMPUTING_COMPONENT)
                .renderer(() -> new ExResearchPartRenderer(
                        tire, GTOCore.id("block/casings/about_computer/" + name),
                        !activeTexture ? null : GTOCore.id("block/casings/about_computer/" + name + "_active"),
                        !activeTexture ? null : GTOCore.id("block/casings/about_computer/" + name + "_active_emissive"),
                        !damagedTexture ? null : GTOCore.id("block/casings/about_computer/" + "damaged_" + name),
                        !damagedTexture ? null : GTOCore.id("block/casings/about_computer/" + "damaged_" + name + "_active"),
                        !damagedTexture ? null : GTOCore.id("block/casings/about_computer/" + "damaged_" + name + "_active_emissive")));
    }

    /////////////////////////////////////
    // *********** 数据机器 *********** //
    /////////////////////////////////////

    public static final MachineDefinition BIO_DATA_ACCESS_HATCH = machine("bio_data_access_hatch", "生物数据访问仓", (holder) -> new DataAccessHatchMachine(holder, UHV, false))
            .tier(UHV)
            .allRotation()
            .abilities(DATA_ACCESS)
            .tooltips(Component.translatable("gtceu.machine.data_access_hatch.tooltip.0"),
                    Component.translatable("gtceu.machine.data_access_hatch.tooltip.1", 25))
            .tooltipsText("Can hold §2Atomic Archive§r and lower tier data storage media.", "可以放入§2原子档案§r及以下等级的数据存储介质。")
            .notAllowSharedTooltips()

            .renderer(() -> new OverlayTieredMachineRenderer(UHV, GTCEu.id("block/machine/part/data_access_hatch")))
            .register();

    public static final MachineDefinition BLACK_HOLE_DATA_ACCESS_HATCH = machine("black_hole_data_access_hatch", "黑洞数据访问仓", (holder) -> new DataAccessHatchMachine(holder, UIV, false))
            .tier(UIV)
            .allRotation()
            .abilities(DATA_ACCESS)
            .tooltips(Component.translatable("gtceu.machine.data_access_hatch.tooltip.0"),
                    Component.translatable("gtceu.machine.data_access_hatch.tooltip.1", 36))
            .tooltipsText("Can hold §6Obsidian Matrix§r and lower tier data storage media.", "可以放入§6黑曜石矩阵§r及以下等级的数据存储介质。")
            .notAllowSharedTooltips()

            .renderer(() -> new OverlayTieredMachineRenderer(UIV, GTCEu.id("block/machine/part/data_access_hatch")))
            .register();

    public static final MachineDefinition VIRTUAL_UNIVERSE_DATA_ACCESS_HATCH = machine("virtual_universe_data_access_hatch", "虚拟宇宙数据访问仓", (holder) -> new DataAccessHatchMachine(holder, OpV, false))
            .tier(OpV)
            .allRotation()
            .abilities(DATA_ACCESS)
            .tooltips(Component.translatable("gtceu.machine.data_access_hatch.tooltip.0"),
                    Component.translatable("gtceu.machine.data_access_hatch.tooltip.1", 49))
            .tooltipsText("Can hold §dMiniature Universe§r and lower tier data storage media.", "可以放入§d微缩宇宙§r及以下等级的数据存储介质。")
            .notAllowSharedTooltips()

            .renderer(() -> new OverlayTieredMachineRenderer(OpV, GTCEu.id("block/machine/part/data_access_hatch")))
            .register();

    public static final MachineDefinition DATA_FORM_TESTING_ME_INTERFACE = machine("data_form_testing_me_interface", "数据形式测试机ME接口", DataFormTestingPart::new)
            .tier(EV)
            .allRotation()
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayActiveMachineRenderer(GTCEu.id("block/high_power_casing"), GTCEu.id("block/machine/part/me_pattern_buffer_proxy"), GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();

    public static final MachineDefinition INTELLIGENT_SCANNING_ME_PROXY = machine("intelligent_scanning_me_proxy", "智能扫描ME代理接口", IntelligentScanningProxyPartMachine::new)
            .tier(UV)
            .allRotation()
            .tooltips(GTOMachineTooltipsA.IntelligentScanningProxyTooltips)
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayActiveMachineRenderer(GTOCore.id("block/neutronium_stable_casing"), GTCEu.id("block/machine/part/me_pattern_buffer_proxy"), GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();

    public static final MultiblockMachineDefinition DATA_CENTER = multiblock("data_center", "数据中心", DataCenter::new)
            .tooltipsSupplier(GTOMachineTooltipsA.DataCenterTooltips)
            .nonYAxisRotation()
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .block(GTBlocks.HIGH_POWER_CASING)
            .pattern(definition -> FactoryBlockPattern.start(definition)
                    .aisle("AAAAAAAAAAA AAA", "ACCCCCCCCCA ADA", "ACCCCCCCCCA ADA", "AAAAAAAAAAA ADA", "ADA         ADA", "ADA AAAAAAAAAAA", "ADA ACCCCCCCCCA", "ADA ACCCCCCCCCA", "AAA AAAAAAAAAAA")
                    .aisle("AEEEEEEEEEEEEEA", "DFFFFFFFFFEGEFC", "DFFFFFFFFFEGEFC", "DEEEEEEEEEEGEEA", "D EGGGGGGGGGE D", "AEEGEEEEEEEEEED", "CFEGEFFFFFFFF D", "CFEGEFFFFFFFF D", "AEEEEEEEEEEEEEA")
                    .aisle("AEAAAAAAAAA AEA", "AEAHHHHHHHA AFC", "AEAHHHHHHHA AFC", "AEAAAAIAIAA AEA", "AEA  A A A  AEA", "AEA AAIAIAAAAEA", "CFA AHHHHHHHAEA", "CFA AHHHHHHHAEA", "AEA AAAAAAAAAAA")
                    .aisle(" E          AEA", " G          HFC", " G          HFC", " G    I I   AEA", " G           G ", "AEA   I I    G ", "CFH          G ", "CFH          G ", "AEA          A ")
                    .aisle("AEA J  J  J AEA", "AEA JJJJJJJ HFC", "AEA JKKJKKJ HFC", "AEA JKIJIKJ AEA", " G  JJJJJJJ  G ", "AEA JKIJIKJ AEA", "CFH JKKJKKJ AEA", "CFH JJJJJJJ AEA", "AEA JLLJLLJ AEA")
                    .aisle("AEA         AEA", "CFH JKKJKKJ HFC", "CFH KMMMMMK HFC", "AEA KMMMNMK AEA", " GA JMMJMMJ AG ", "AEA KMMMNMK AEA", "CFH KMMMMMK HFC", "CFH JKKJKKJ HFC", "AEA LKKLKKL AEA")
                    .aisle("AEA         AEA", "CFH JKKJKKJ HFC", "CFH KMMMMMK HFC", "AEIIINNMNMIIIEA", " G  JMMJMMJ  G ", "AEIIINNMNMIIIEA", "CFH KMMMMMK HFC", "CFH JKKJKKJ HFC", "AEA LKKLKKL AEA")
                    .aisle("AEA J  J  J AEA", "CFH JJJJJJJ HFC", "CFH JMMJMMJ HFC", "AEA JMMJMMJ AEA", " GA JMMJJJJ AG ", "AEA JMMJMMJ AEA", "CFH JMMJMMJ HFC", "CFH JJJJJJJ HFC", "AEA JLLJLLJ AEA")
                    .aisle("AEA         AEA", "CFH JKKJKKJ HFC", "CFH KMMMMMK HFC", "AEIIINNMNNIIIEA", " G  JMMJMMJ  G ", "AEIIINNMNNIIIEA", "CFH KMMMMMK HFC", "CFH JKKJKKJ HFC", "AEA LKKLKKL AEA")
                    .aisle("AEA         AEA", "CFH JKKJKKJ HFC", "CFH KMMMMMK HFC", "AEA KMMMMMK AEA", " GA JMMJMMJ AG ", "AEA KMMMMMK AEA", "CFH KMMMMMK HFC", "CFH JKKJKKJ HFC", "AEA LKKLKKL AEA")
                    .aisle("AEA J   J J AEA", "CFH JJJJJJJ AEA", "CFH JKKJKKJ AEA", "AEA JKIJIKJ AEA", " G  JJJJJJJ  G ", "AEA JKIJIKJ AEA", "AEA JKKJKKJ HFC", "AEA JJJJJJJ HFC", "AEA JLLJLLJ AEA")
                    .aisle("AEA          E ", "CFH          G ", "CFH          G ", "AEA   I I    G ", " G           G ", " G    I I   AEA", " G          HFC", " G          HFC", " E          AEA")
                    .aisle("AEA AAAAAAAAAEA", "CFA AHHHHHHHAEA", "CFA AHHHHHHHAEA", "AEA AAIAIAAAAEA", "AEA  A A A  AEA", "AEAAAAIAIAA AEA", "AEAHHHHHHHA AFC", "AEAHHHHHHHA AFC", "AEAAAAAAAAA AEA")
                    .aisle("AEEEEEEEEEEEEEA", "CFEGEFFFFFFFFFD", "CFEGEFFFFFFFFFD", "AEEGEEEEEEEEEED", "D EGGGGGGGGGE D", "DEEEEEEEEEEGEEA", "DFFFFFFFFFEGEFC", "DFFFFFFFFFEGEFC", "AEEEEEEEEEEEEEA")
                    .aisle("AAA AAAAAAAAAAA", "ABA ACCCCCCCCCA", "ADA ACCCCCCCCCA", "ADA AAAAAAAAAAA", "ADA         ADA", "AAAAAAAAAAA ADA", "ACCCCCCCCCA ADA", "ACCCCCCCCCA ADA", "AAAAAAAAAAA AAA")
                    .where('A', blocks(GTBlocks.COMPUTER_CASING.get()))
                    .where('B', controller(definition))
                    .where('C', abilities(PartAbility.DATA_ACCESS)
                            .or(blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .or(abilities(IMPORT_FLUIDS))
                            .or(abilities(COMPUTATION_DATA_RECEPTION))
                            .or(abilities(PartAbility.OPTICAL_DATA_RECEPTION)))
                    .where('D', blocks(GTBlocks.HIGH_POWER_CASING.get())
                            .or(abilities(PartAbility.OPTICAL_DATA_TRANSMISSION).setMaxGlobalLimited(4, 1))
                            .or(abilities(PartAbility.OPTICAL_DATA_RECEPTION).setMaxGlobalLimited(4, 1))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(8, 2))
                            .or(abilities(COMPUTATION_DATA_RECEPTION, IMPORT_FLUIDS))
                            .or(abilities(MAINTENANCE).setExactLimit(1)))
                    .where('E', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where('F', blocks(GTOBlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.get()))
                    .where('G', GTOPredicates.glass())
                    .where('H', blocks(COMPUTER_HEAT_VENT.get()))
                    .where('I', blocks(GTOBlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING.get()))
                    .where('J', blocks(GTOBlocks.IRIDIUM_CASING.get()))
                    .where('K', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                    .where('L', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                    .where('M', GTOPredicates.frame(GTOMaterials.BerylliumAluminumZ))
                    .where('N', blocks(GTOBlocks.IRIDIUM_PIPE_CASING.get()))
                    .where(' ', any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/hpca/high_power_casing"), GTCEu.id("block/multiblock/research_station"))
            .register();

    /////////////////////////////////////
    // *********** 研究机器 *********** //
    /////////////////////////////////////

    public static final MachineDefinition CATALYSIS_DATA_HOLDER = machine("catalysis_data_holder", "催化反应数据支架", SimpleResearchTagPartMachine.create(ResearchTag.CATALYSIS, 1024))
            .tier(LuV)
            .tooltips(GTOMachineTooltipsA.CatalystDataHolder)
            .tooltips(GTOMachineTooltipsA.DataHolderUniversal)
            .allRotation()
            .renderer(() -> new OverlayTieredActiveMachineRenderer(LuV, GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();
    public static final MachineDefinition THERMODYNAMIC_DATA_HOLDER = machine("thermodynamic_data_holder", "热力学数据支架", SimpleResearchTagPartMachine.create(ResearchTag.THERMODYNAMICS, 1024))
            .tier(LuV)
            .tooltips(GTOMachineTooltipsA.ThermaldynamicsDataHolder)
            .tooltips(GTOMachineTooltipsA.DataHolderUniversal)
            .allRotation()
            .renderer(() -> new OverlayActiveMachineRenderer(GTOCore.id("block/stable_base_casing"), GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();

    public static final MachineDefinition ENERGY_DATA_HOLDER = machine("energy_data_holder", "能量数据支架", SimpleResearchTagPartMachine.create(ResearchTag.ENERGY, 1024))
            .tier(LuV)
            .tooltips(GTOMachineTooltipsA.EnergyDataHolder)
            .tooltips(GTOMachineTooltipsA.DataHolderUniversal)
            .abilities(OUTPUT_ENERGY, OUTPUT_LASER)
            .allRotation()
            .renderer(() -> new OverlayTieredActiveMachineRenderer(LuV, GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();
    public static final MachineDefinition INTERSTELLAR_ENGINEERING_DATA_HOLDER = machine("interstellar_engineering_data_holder", "星际工程数据支架", SimpleResearchTagPartMachine.create(ResearchTag.INTERSTELLAR_ENGINEERING, 256))
            .tier(UV)
            .tooltips(GTOMachineTooltipsA.DataHolderUniversal)
            .allRotation()
            .renderer(() -> new OverlayActiveMachineRenderer(GTOCore.id("block/space_elevator_mechanical_casing"), GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();
    public static final MachineDefinition STORAGE_DATA_HOLDER = machine("storage_data_holder", "存储数据支架", SimpleResearchTagPartMachine.create(ResearchTag.DATA_STORAGE, 256))
            .tier(LuV)
            .tooltips(GTOMachineTooltipsA.DataHolderUniversal)
            .allRotation()
            .renderer(() -> new OverlayActiveMachineRenderer(GTCEu.id("block/computer_casing"), GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();
    public static final MachineDefinition BIOLOGY_DATA_HOLDER = machine("biology_data_holder", "生物学数据支架", SimpleResearchTagPartMachine.create(ResearchTag.BIOLOGY, 256))
            .tier(LuV)
            .tooltips(GTOMachineTooltipsA.DataHolderUniversal)
            .allRotation()
            .renderer(() -> new OverlayActiveMachineRenderer(GTOCore.id("block/biological_mechanical_casing"), GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();
    public static final MachineDefinition OPTICAL_DATA_HOLDER = machine("optical_data_holder", "光学数据支架", SimpleResearchTagPartMachine.create(ResearchTag.OPTICS, 256))
            .tier(UHV)
            .tooltips(GTOMachineTooltipsA.DataHolderUniversal)
            .allRotation()
            .renderer(() -> new OverlayActiveMachineRenderer(GTCEu.id("block/laser_safe_engraving_casing"), GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();

    public static final MachineDefinition ANALYZE_HOLDER = machine("analyze_holder", "分析支架", AnalyzeHolderMachine::new)
            .tier(IV)
            .allRotation()
            .renderer(() -> new OverlayTieredActiveMachineRenderer(IV, GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();

    public static final MachineDefinition RESEARCH_HOLDER = machine("research_holder", "研究支架", ResearchHolderMachine::new)
            .tier(IV)
            .allRotation()
            .renderer(() -> new OverlayTieredActiveMachineRenderer(IV, GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();

    public static final MachineDefinition DATA_GENERATE_HOLDER = machine("data_generate_holder", "数据构建支架", DataGenerateHolderMachine::new)
            .tier(IV)
            .allRotation()
            .renderer(() -> new OverlayTieredActiveMachineRenderer(IV, GTCEu.id("block/machine/part/object_holder"),
                    GTCEu.id("block/machine/part/object_holder_active")))
            .notAllowSharedTooltips()
            .register();

    public static final MultiblockMachineDefinition PRIMORDIAL_SCANNING_STATION = multiblock("dataunit_scanning_station", "基元扫描站", ScanningStationMachine::new)
            .nonYAxisRotation()
            .tooltipsSupplier(GTOMachineTooltipsA.ScanStationMachineTooltips)
            .recipeTypes(CRYSTAL_SCAN_RECIPES)
            .block(GTBlocks.HIGH_POWER_CASING)
            .pattern(definition -> MultiBlockFileReader.start(definition)
                    .where('A', blocks(GTBlocks.COMPUTER_HEAT_VENT.get()))
                    .where('B', blocks(GTBlocks.COMPUTER_CASING.get()))
                    .where('C', blocks(COMPUTER_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2, 1))
                            .or(abilities(PartAbility.COMPUTATION_DATA_RECEPTION).setExactLimit(1))
                            .or(autoAbilities(true, false, false)))
                    .where('D', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where('E', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                    .where('F', blocks(GTBlocks.CLEANROOM_GLASS.get()))
                    .where('G', GTOPredicates.frame(GTOMaterials.StainlessSteel420))
                    .where('H', blocks(RESEARCH_HOLDER.get()))
                    .where('I', controller(definition))
                    .where(' ', any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/hpca/high_power_casing"), GTCEu.id("block/multiblock/research_station"))
            .register();

    public static final MultiblockMachineDefinition ANALYSIS_AND_RESEARCH_CENTER = multiblock("analysis_and_research_center", "分析推演中心", AnalysisAndResearchCenterMachine::new)
            .nonYAxisRotation()
            .recipeTypes(DUMMY_RECIPES)
            .block(GTBlocks.HIGH_POWER_CASING)
            .pattern(definition -> FactoryBlockPattern.start(definition)
                    .aisle("    ABBBA    ", "    AAAAA    ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "    AAAAA    ", "    AAAAA    ")
                    .aisle("   ACCCCCA   ", "   A D D A   ", "     E E     ", "     E E     ", "     E E     ", "     D D     ", "     D D     ", "     D D     ", "     D D     ", "   A D D A   ", "   ACCCCCA   ")
                    .aisle("  AFCFAFCFA  ", "  A   A   A  ", "     ACA     ", "     ACA     ", "     ACA     ", "      A      ", "      A      ", "      A      ", "      A      ", "  A   A   A  ", "  AFCFAFCFA  ")
                    .aisle(" AFACFAFCAFA ", " A A     A A ", "   A     A   ", "   CA   AC   ", "   CA   AC   ", "   CA   AC   ", "   A     A   ", "   A     A   ", "   A     A   ", " A A     A A ", " AFACFAFCAFA ")
                    .aisle("ACCCCCCCCCCCA", "A           A", "             ", "   A     A   ", "   A     A   ", "   A     A   ", "             ", "             ", "             ", "A           A", "ACCCCCCCCCCCA")
                    .aisle("BCFFCAAACFFCB", "AD         DA", " D         D ", " D         D ", " EA       AE ", " EA       AE ", " EA       AE ", " D         D ", " D         D ", "AD         DA", "ACFFCAAACFFCA")
                    .aisle("BCAACAAACAACB", "A A CEEEC A A", " BA CEGEC AB ", " BA CEEEC AB ", "  C CEHEC C  ", "  C CEEEC C  ", "  C CCCCC C  ", " BA       AB ", " BA       AB ", "A A       A A", "ACAACAAACAACA")
                    .aisle("BCFFCAAACFFCB", "AD         DA", " D         D ", " D         D ", " EA       AE ", " EA       AE ", " EA       AE ", " D         D ", " D         D ", "AD         DA", "ACFFCAAACFFCA")
                    .aisle("ACCCCCCCCCCCA", "A           A", "             ", "             ", "             ", "   A     A   ", "   A     A   ", "   A     A   ", "             ", "A           A", "ACCCCCCCCCCCA")
                    .aisle(" AFACFAFCAFA ", " A A     A A ", "   A     A   ", "   A     A   ", "   A     A   ", "   CA   AC   ", "   CA   AC   ", "   CA   AC   ", "   A     A   ", " A A     A A ", " AFACFAFCAFA ")
                    .aisle("  AFCFAFCFA  ", "  A       A  ", "             ", "             ", "             ", "     AAA     ", "     ACA     ", "     ACA     ", "     ACA     ", "  A   A   A  ", "  AFCFAFCFA  ")
                    .aisle("   ACCCCCA   ", "   A D D A   ", "     D D     ", "     D D     ", "     D D     ", "     D D     ", "     E E     ", "     E E     ", "     E E     ", "   A D D A   ", "   ACCCCCA   ")
                    .aisle("    AAAAA    ", "    A   A    ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "    AAAAA    ", "    AAAAA    ")
                    .where('A', blocks(GTBlocks.COMPUTER_CASING.get()))
                    .where('B', blocks(GTBlocks.COMPUTER_HEAT_VENT.get()))
                    .where('C', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where('D', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                    .where('E', blocks(GTBlocks.HIGH_POWER_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.COMPUTATION_DATA_RECEPTION).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('F', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                    .where('G', controller(definition))
                    .where('H', blocks(RESEARCH_HOLDER.get()))
                    .where(' ', any())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("    AAAAA    ", "    A   A    ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "    AAAAA    ", "    AAAAA    ")
                        .aisle("   ACCCCCA   ", "   A D D A   ", "     D D     ", "     D D     ", "     D D     ", "     D D     ", "     E E     ", "     E E     ", "     E E     ", "   A D D A   ", "   ACCCCCA   ")
                        .aisle("  AFCFAFCFA  ", "  A       A  ", "             ", "             ", "             ", "     AAA     ", "     ACA     ", "     ACA     ", "     ACA     ", "  A   A   A  ", "  AFCFAFCFA  ")
                        .aisle(" AFACFAFCAFA ", " A A     A A ", "   A     A   ", "   A     A   ", "   A     A   ", "   CA   AC   ", "   CA   AC   ", "   CA   AC   ", "   A     A   ", " A A     A A ", " AFACFAFCAFA ")
                        .aisle("ACCCCCCCCCCCA", "A           A", "             ", "             ", "             ", "   A     A   ", "   A     A   ", "   A     A   ", "             ", "A           A", "ACCCCCCCCCCCA")
                        .aisle("BCFFCAAACFFCB", "AD         DA", " D         D ", " D         D ", " EA       AE ", " EA       AE ", " EA       AE ", " D         D ", " D         D ", "AD         DA", "ACFFCAAACFFCA")
                        .aisle("BCAACAAACAACB", "A A CLKJC A A", " BA CLGIC AB ", " BA CEEEC AB ", "  C CEHEC C  ", "  C CEEEC C  ", "  C CCCCC C  ", " BA       AB ", " BA       AB ", "A A       A A", "ACAACAAACAACA")
                        .aisle("BCFFCAAACFFCB", "AD         DA", " D         D ", " D         D ", " EA       AE ", " EA       AE ", " EA       AE ", " D         D ", " D         D ", "AD         DA", "ACFFCAAACFFCA")
                        .aisle("ACCCCCCCCCCCA", "A           A", "             ", "   A     A   ", "   A     A   ", "   A     A   ", "             ", "             ", "             ", "A           A", "ACCCCCCCCCCCA")
                        .aisle(" AFACFAFCAFA ", " A A     A A ", "   A     A   ", "   CA   AC   ", "   CA   AC   ", "   CA   AC   ", "   A     A   ", "   A     A   ", "   A     A   ", " A A     A A ", " AFACFAFCAFA ")
                        .aisle("  AFCFAFCFA  ", "  A   A   A  ", "     ACA     ", "     ACA     ", "     ACA     ", "      A      ", "      A      ", "      A      ", "      A      ", "  A   A   A  ", "  AFCFAFCFA  ")
                        .aisle("   ACCCCCA   ", "   A D D A   ", "     E E     ", "     E E     ", "     E E     ", "     D D     ", "     D D     ", "     D D     ", "     D D     ", "   A D D A   ", "   ACCCCCA   ")
                        .aisle("    ABBBA    ", "    AAAAA    ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "    AAAAA    ", "    AAAAA    ")
                        .where('A', GTBlocks.COMPUTER_CASING.get())
                        .where('B', GTBlocks.COMPUTER_HEAT_VENT.get())
                        .where('C', GTBlocks.ADVANCED_COMPUTER_CASING.get())
                        .where('D', GTBlocks.HIGH_POWER_CASING.get())
                        .where('E', GTBlocks.HIGH_POWER_CASING.get())
                        .where('F', GCYMBlocks.ELECTROLYTIC_CELL.get())
                        .where('G', ExResearchMachines.ANALYSIS_AND_RESEARCH_CENTER, Direction.NORTH)
                        .where(' ', Blocks.AIR)
                        .where('I', GTMachines.ITEM_EXPORT_BUS[GTValues.ZPM].get(), Direction.NORTH)
                        .where('J', GTResearchMachines.COMPUTATION_HATCH_RECEIVER, Direction.NORTH)
                        .where('K', GTMachines.MAINTENANCE_HATCH.get(), Direction.NORTH)
                        .where('L', GTMachines.ENERGY_INPUT_HATCH[ZPM], Direction.NORTH);
                shapeInfo.add(builder.shallowCopy()
                        .where('H', ANALYZE_HOLDER.get(), Direction.NORTH)
                        .build(definition));
                shapeInfo.add(builder.shallowCopy()
                        .where('H', RESEARCH_HOLDER.get(), Direction.NORTH)
                        .build(definition));
                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/hpca/high_power_casing"), GTCEu.id("block/multiblock/research_station"))
            .register();

    public static final MultiblockMachineDefinition DATA_FORM_TESTING_PLANT = multiblock("data_form_testing_plant", "数据形式测试厂", DataFormTestingPlantMachine::new)
            .nonYAxisRotation()
            .recipeTypes(DATA_TESTING_RECIPES)
            .block(GTBlocks.HIGH_POWER_CASING)
            .tooltipsSupplier(GTOMachineTooltipsA.DataFormTestingPlantMachineTooltips)
            .pattern(definition -> FactoryBlockPattern.start(definition)
                    .aisle("           ", "    EEE    ", "    EGE    ", "    EEE    ", "           ")
                    .aisle("  A  A  A  ", "  A DDD A  ", "  B DDD B  ", "  A DDD A  ", "  A  A  A  ")
                    .aisle(" AA  A  AA ", " AD DDD DA ", " BBBBBBBBB ", " AD DDD DA ", " AA  A  AA ")
                    .aisle("AAA  A  AAA", "AAD DDD DAA", "BBD DBD DBB", "AAD DDD DAA", "AAA  A  AAA")
                    .aisle("AA  AAA  AA", "AC  ABA  CA", "BC  ABA  CB", "AC  ABA  CA", "AA  AAA  AA")
                    .aisle("AA AABAA AA", "AC ADBDA CA", "BC ADDDA CB", "AC A B A CA", "AA AABAA AA")
                    .aisle("AA  BBB  AA", "AC BB BB CA", "BC BD DB CB", "AC BB BB CA", "AA  BBB  AA")
                    .aisle("AA AABAA AA", "AC ADBDA CA", "BC ADDDA CB", "AC A B A CA", "AA AABAA AA")
                    .aisle("AA  AAA  AA", "AC  ABA  CA", "BC  ABA  CB", "AC  ABA  CA", "AA  AAA  AA")
                    .aisle("AAA  A  AAA", "AAD DDD DAA", "BBD DBD DBB", "AAD DDD DAA", "AAA  A  AAA")
                    .aisle(" AA  A  AA ", " AD DDD DA ", " BBBBBBBBB ", " AD DDD DA ", " AA  A  AA ")
                    .aisle("  A  A  A  ", "  A DDD A  ", "  B DDD B  ", "  A DDD A  ", "  A  A  A  ")
                    .aisle("           ", "    EEE    ", "    EFE    ", "    EEE    ", "           ")
                    .where('A', blocks(GTBlocks.COMPUTER_CASING.get()))
                    .where('B', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where('C', blocks(GTOBlocks.HIGH_PRESSURE_RESISTANT_CASING.get()))
                    .where('D', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                    .where('E', blocks(GTBlocks.HIGH_POWER_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                            .or(abilities(IMPORT_ITEMS))
                            .or(blocks(STORAGE_DATA_HOLDER.get()).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('F', controller(definition))
                    .where('G', blocks(DATA_FORM_TESTING_ME_INTERFACE.get()))
                    .where(' ', any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/hpca/high_power_casing"), GTCEu.id("block/multiblock/research_station"))
            .register();

    // 热力学分析平台
    public static final MultiblockMachineDefinition THERMODYNAMIC_ANALYSIS_PLATFORM = multiblock("thermodynamic_analysis_platform", "热力学分析平台", ThermodynamicAnalysisPlatformMachine::new)
            .nonYAxisRotation()
            .recipeTypes(DUMMY_RECIPES)
            .block(GTOBlocks.STABLE_BASE_CASING)
            .tooltipsSupplier(GTOMachineTooltipsA.ThermodynamicAnalysisPlatformMachineTooltips)
            .nonYAxisRotation()
            .pattern(definition -> MultiBlockFileReader.start(definition)
                    .where('A', blocks(GTBlocks.COMPUTER_CASING.get()))
                    .where('B', blocks(GTOBlocks.STABLE_BASE_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(blocks(THERMODYNAMIC_DATA_HOLDER.get()).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(blocks(GTMachines.CONTROL_HATCH.get())))
                    .where('C', controller(definition))
                    .where('D', blocks(GTOBlocks.STABLE_BASE_CASING.get()))
                    .where('E', GTOPredicates.recordPosition(LOW_TEMP_INTERFACE,
                            blocks(GTOBlocks.STABLE_BASE_CASING.get())
                                    .or(abilities(HEAT_CONDUCTION).setExactLimit(1))))
                    .where('F', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where('G', blocks(GTOBlocks.VACUUM_CHAMBER_OBSERVATION_GLASS.get()))
                    .where('H', blocks(GTOBlocks.PRESSURE_RESISTANT_HOUSING_MECHANICAL_BLOCK.get()))
                    .where('I', blocks(GTOBlocks.HIGH_PRESSURE_PIPE_CASING.get()))
                    .where('J', blocks(GTOBlocks.THREE_PROOF_COMPUTER_CASING.get()))
                    .where('K', blocks(GTBlocks.FUSION_GLASS.get()))
                    .where('L', blocks(GTOBlocks.COLD_ICE_CASING.get()))
                    .where('M', blocks(GTOBlocks.BLAZE_CASING.get()))
                    .where('N', blocks(GTOBlocks.ELECTRIC_POWER_TRANSMISSION_CASING.get()))
                    .where('O', GTOPredicates.recordPosition(HIGH_TEMP_INTERFACE,
                            blocks(GTOBlocks.STABLE_BASE_CASING.get())
                                    .or(abilities(HEAT_CONDUCTION).setExactLimit(1))))
                    .where(' ', any())
                    .build())
            .workableCasingRenderer(GTOCore.id("block/casings/stable_base_casing"), GTCEu.id("block/multiblock/research_station"))
            .register();
    // 激光计算测试平台
    public static final MultiblockMachineDefinition LASER_COMPUTATION_TESTING_PLATFORM = multiblock("laser_computation_testing_platform", "激光计算测试平台", LaserComputationTestingPlatformMachine::new)
            .nonYAxisRotation()
            .recipeTypes(BEAM_GUIDED_COMPUTATION_TESTING_RECIPES)
            .tooltipsSupplier(GTOMachineTooltipsA.LaserComputationTestingPlatformMachineTooltips)
            .nonYAxisRotation()
            .block(GCYMBlocks.CASING_LASER_SAFE_ENGRAVING)
            .pattern(definition -> MultiBlockFileReader.start(definition)
                    .where('A', blocks(GCYMBlocks.CASING_LASER_SAFE_ENGRAVING.get()))
                    .where('B', blocks(GTOMachines.BEAM_ACCESS_HATCH.get()))
                    .where('C', blocks(GTOBlocks.THREE_PROOF_COMPUTER_CASING.get()))
                    .where('D', blocks(GTOBlocks.OPTICAL_RESONANCE_CHAMBER.get()))
                    .where('E', blocks(GTBlocks.COMPUTER_CASING.get()))
                    .where('F', blocks(GTOBlocks.LASER_CASING.get()))
                    .where('G', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where('H', blocks(GTOBlocks.ELECTRON_PERMEABLE_AMPROSIUM_COATED_GLASS.get()))
                    .where('I', blocks(GTOBlocks.IRIDIUM_CASING.get()))
                    .where('J', blocks(GTOBlocks.OPTICAL_DYNAMIC_COATING_INSTRUMENT_PROTECTIVE_SHIELD_GLASS.get()))
                    .where('K', blocks(GTOBlocks.PRESSURE_RESISTANT_HOUSING_MECHANICAL_BLOCK.get()))
                    .where('L', blocks(GTOBlocks.HIGH_ENERGY_LASER_EMITTER.get()))
                    .where('M', blocks(GCYMBlocks.CASING_LASER_SAFE_ENGRAVING.get())
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(MAINTENANCE).setExactLimit(1))
                            .or(blocks(OPTICAL_DATA_HOLDER.get()).setExactLimit(1)))
                    .where('N', controller(definition))
                    .where('O', blocks(GTOMachines.EXCITATION_CRYSTAL.get()))
                    .where(' ', any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/laser_safe_engraving_casing"), GTCEu.id("block/multiblock/research_station"))
            .register();

    // 智能扫描管理平台
    public static final MultiblockMachineDefinition INTELLIGENT_SCANNING_MANAGEMENT_PLATFORM = multiblock("intelligent_scanning_management_platform", "智能扫描管理平台", IntelligentScanningManagementPlatformMachine::new)
            .nonYAxisRotation()
            .recipeTypes(SCANNER_RECIPES)
            .tooltipsSupplier(GTOMachineTooltipsA.IntelligentScanningManagementPlatformMachineTooltips)
            .nonYAxisRotation()
            .recipeModifier(RecipeModifier.overclocking(0.75, 1, 1))
            .block(GTOBlocks.NEUTRONIUM_STABLE_CASING)
            .pattern(definition -> MultiBlockFileReader.start(definition)
                    .where('A', GTOPredicates.frame(GTOMaterials.Photonium))
                    .where('B', blocks(GTOBlocks.GRAVITY_STABILIZATION_CASING.get()))
                    .where('C', blocks(GTOBlocks.NAQUADAH_ALLOY_CASING.get()))
                    .where('D', GTOPredicates.frame(GTOMaterials.CarbonFiberPolyphenyleneSulfideComposite))
                    .where('E', blocks(GTOBlocks.NEUTRONIUM_STABLE_CASING.get()))
                    .where('F', blocks(GTBlocks.COMPUTER_CASING.get()))
                    .where('G', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                    .where('H', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                    .where('I', blocks(GTOBlocks.BORON_CARBIDE_CERAMIC_RADIATION_RESISTANT_MECHANICAL_CUBE.get()))
                    .where('J', blocks(GTOBlocks.TITANIUM_ALLOY_PROTECTIVE_MECHANICAL_BLOCK.get()))
                    .where('K', GTOPredicates.frame(GTOMaterials.PhotonicKristallite))
                    .where('L', blocks(GTOBlocks.LITHIUM_OXIDE_CERAMIC_HEAT_RESISTANT_SHOCK_RESISTANT_MECHANICAL_CUBE.get()))
                    .where('M', blocks(GTOBlocks.THREE_PROOF_COMPUTER_CASING.get()))
                    .where('N', blocks(GTOBlocks.MOLECULAR_CASING.get()))
                    .where('O', blocks(GTOBlocks.VACUUM_CHAMBER_PROTECTION_CASING.get()))
                    .where('P', blocks(GTOBlocks.STRONTIUM_CARBONATE_CERAMIC_RAY_ABSORBING_MECHANICAL_CUBE.get()))
                    .where('Q', blocks(GTOBlocks.NEUTRONIUM_STABLE_CASING.get())
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(blocks(INTELLIGENT_SCANNING_ME_PROXY.get()).setExactLimit(1))
                            .or(abilities(MAINTENANCE).setExactLimit(1)))
                    .where('R', GTOPredicates.frame(GTOMaterials.RadiationShieldingHighDensityTungstenSteel))
                    .where('S', blocks(GTOBlocks.PRECISION_PROCESSING_MECHANICAL_CASING.get()))
                    .where('T', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where('U', blocks(GTOBlocks.COOLANT_PIPE_CASING.get()))
                    .where('V', blocks(GTBlocks.COMPUTER_HEAT_VENT.get()))
                    .where('W', blocks(GTOBlocks.OPTICAL_DYNAMIC_COATING_INSTRUMENT_PROTECTIVE_SHIELD_GLASS.get()))
                    .where('X', blocks(GTOBlocks.ELECTRON_PERMEABLE_AMPROSIUM_COATED_GLASS.get()))
                    .where('Y', blocks(GTOBlocks.HIGH_ENERGY_LASER_EMITTER.get()))
                    .where('Z', blocks(GTOBlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.get()))
                    .where('[', blocks(GTOBlocks.INSULATION_TILE_MECHANICAL_BLOCK.get()))
                    .where('\\', controller(definition))
                    .where(' ', any())
                    .build())
            .workableCasingRenderer(GTOCore.id("block/neutronium_stable_casing"), GTCEu.id("block/multiblock/research_station"))
            .register();
}
