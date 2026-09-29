package com.gtocore.common.machine.multiblock.electric.nano;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Slot;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class NanoForgeMachine extends StorageMultiblockMachine implements IParallelMachine {

    @RegisterLanguage(cn = "结构等级：%s", en = "Structure Tier: %s")
    private static final String STRUCTURE_TIER = "gtocore.nano_forge.structure_tier";
    @RegisterLanguage(cn = "纳米蜂群等级：%s", en = "Nanoswarm Tier: %s")
    private static final String SWARM_TIER = "gtocore.nano_forge.swarm_tier";
    @RegisterLanguage(cn = "生效等级：%s（取结构等级与纳米蜂群等级中的较低者）", en = "Effective Tier: %s (lower of structure tier and nanoswarm tier)")
    private static final String EFFECTIVE_TIER = "gtocore.nano_forge.effective_tier";

    @RegisterLanguage(cn = "主扩展模块", en = "Primary Extension Module")
    private static final String RIGHT_NAME = "gtocore.multiblock.nano_forge.right_module";
    @RegisterLanguage(cn = "搭建后结构等级至少为 2；内芯的方块决定模块类型", en = "Raises the structure tier to at least 2; the core blocks determine the module type")
    private static final String RIGHT_DESC = "gtocore.multiblock.nano_forge.right_module.desc";
    @RegisterLanguage(cn = "装配线内芯", en = "Assembly Line Core")
    private static final String BASIC_RIGHT_NAME = "gtocore.multiblock.nano_forge.right_module.basic";
    @RegisterLanguage(cn = "内芯使用装配线外壳，结构等级为 2", en = "Core uses Assembly Line Casings; structure tier 2")
    private static final String BASIC_RIGHT_DESC = "gtocore.multiblock.nano_forge.right_module.basic.desc";
    @RegisterLanguage(cn = "进阶装配线内芯", en = "Advanced Assembly Line Core")
    private static final String ADVANCED_RIGHT_NAME = "gtocore.multiblock.nano_forge.right_module.advanced";
    @RegisterLanguage(cn = "内芯使用进阶装配线单元，与副扩展模块同时搭建时结构等级为 3", en = "Core uses Advanced Assembly Line Units; structure tier 3 together with the secondary extension module")
    private static final String ADVANCED_RIGHT_DESC = "gtocore.multiblock.nano_forge.right_module.advanced.desc";
    @RegisterLanguage(cn = "副扩展模块", en = "Secondary Extension Module")
    private static final String LEFT_NAME = "gtocore.multiblock.nano_forge.left_module";
    @RegisterLanguage(cn = "与进阶装配线内芯的主扩展模块同时搭建时结构等级为 3", en = "Structure tier 3 together with a primary extension module using the advanced core")
    private static final String LEFT_DESC = "gtocore.multiblock.nano_forge.left_module.desc";

    public static final ParamKey RIGHT_MODULE = ParamKey.of(RIGHT_NAME, RIGHT_DESC);
    public static final ParamKey BASIC_RIGHT_MODULE = ParamKey.of(BASIC_RIGHT_NAME, BASIC_RIGHT_DESC);
    public static final ParamKey ADVANCED_RIGHT_MODULE = ParamKey.of(ADVANCED_RIGHT_NAME, ADVANCED_RIGHT_DESC);
    public static final ParamKey LEFT_MODULE = ParamKey.of(LEFT_NAME, LEFT_DESC);
    private static final PortKey RIGHT_OUT = PortKey.of("right_out");
    private static final PortKey LEFT_OUT = PortKey.of("left_out");

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int machineTier;

    @SyncToClient
    private int structureTier = 1;

    public NanoForgeMachine(MetaMachineBlockEntity holder) {
        super(holder, 64, i -> ChemicalHelper.getPrefix(i.getItem()) == GTOTagPrefix.NANITES);
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        int tier = getEffectiveTier();
        if (recipe.data.getInt(GTORecipeDataKeys.NANO_FORGE_TIER) > tier) {
            return null;
        }
        recipe = ParallelLogic.accurateParallel(this, unit, recipe, getParallel() * (1L << (tier - recipe.data.getInt(GTORecipeDataKeys.NANO_FORGE_TIER))));
        if (recipe == null) return null;
        return RecipeModifier.overclocking(this, unit, recipe, false, 1, 1, tier > recipe.data.getInt(GTORecipeDataKeys.NANO_FORGE_TIER) ? 0.25 : 0.5);
    }

    @Override
    public void onMachineChanged() {
        machineTier = 0;
        MaterialStack stack = ChemicalHelper.getMaterialStack(getStorageStack());
        if (stack.isEmpty()) return;
        Material material = stack.material();
        if (material == GTMaterials.Carbon) {
            machineTier = 1;
        } else if (material == GTOMaterials.Amprosium) {
            machineTier = 2;
        } else if (material == GTOMaterials.Draconium) {
            machineTier = 3;
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var assembly = getAssembly();
        if (assembly == null) {
            structureTier = 1;
        } else if (assembly.has(ADVANCED_RIGHT_MODULE) && assembly.has(LEFT_MODULE)) {
            structureTier = 3;
        } else {
            structureTier = assembly.has(RIGHT_MODULE) ? 2 : 1;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        structureTier = 1;
    }

    public int getEffectiveTier() {
        return Math.min(machineTier, structureTier);
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable(STRUCTURE_TIER, structureTier));
        textList.add(Component.translatable(SWARM_TIER, machineTier));
        textList.add(Component.translatable(EFFECTIVE_TIER, getEffectiveTier()));
    }

    public static Structure structure(MultiblockMachineDefinition definition) {
        return Structure.root(mainPiece())
                .symbols(Symbols.create()
                        .where('@', any())
                        .where('~', controller(definition))
                        .wherePart('A', blocks(GTOBlocks.NAQUADAH_ALLOY_CASING.get())
                                .or(abilities(IMPORT_ITEMS))
                                .or(abilities(EXPORT_ITEMS))
                                .or(abilities(IMPORT_FLUIDS))
                                .or(abilities(INPUT_LASER)))
                        .where('B', blocks(GTOBlocks.NAQUADAH_ALLOY_CASING.get()))
                        .where('C', GTOPredicates.frame(GTMaterials.Ruridit))
                        .where('D', blocks(GTBlocks.CASING_ASSEMBLY_LINE.get()))
                        .where('E', blocks(GTOBlocks.ADVANCED_ASSEMBLY_LINE_UNIT.get())))
                .atPort(RIGHT_OUT, Slot.choice(
                        Slot.one(advancedRightPiece(), StructureModuleKeys.MODULE_IN).count(ADVANCED_RIGHT_MODULE),
                        Slot.one(rightPiece(), StructureModuleKeys.MODULE_IN).count(BASIC_RIGHT_MODULE))
                        .optional().count(RIGHT_MODULE))
                .atPort(LEFT_OUT, Slot.optional(leftPiece(), StructureModuleKeys.MODULE_IN).count(LEFT_MODULE))
                .build();
    }

    private static Piece mainPiece() {
        return Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                .aisle("         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "    B    ", "    B    ", "    B    ", "    B    ", "    B    ", "    B    ", "    B    ", "    B    ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .aisle("  AAAAA  ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .aisle(" ABBBBBA ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "  CB BC  ", "  CB BC  ", "  CB BC  ", "  CB BC  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  CB BC  ", "  CB BC  ", "  CB BC  ", "  CB BC  ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "    C    ", "    C    ", "    C    ", "    C    ", "    C    ")
                .aisle("ABBBBBBBA", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", " BB   BB ", " BB   BB ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "    B    ", "    B    ", "    B    ", "    B    ", "    B    ")
                .aisle("ABBBBBBBA", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", " BB   BB ", " BB   BB ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "    B    ", "    B    ", "    B    ", "    B    ", "    B    ")
                .aisle("ABBBBBBBA", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", " BB   BB ", " BB   BB ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "    B    ", "    B    ", "    B    ", "    B    ", "    B    ")
                .aisle("ABBBBBBBA", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", " BB   BB ", " BB   BB ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "  B   B  ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "    B    ", "    B    ", "    B    ", "    B    ", "    B    ")
                .aisle(" ABBBBBA ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "  CB BC  ", "  CB BC  ", "  CB BC  ", "  CB BC  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  BB BB  ", "  CB BC  ", "  CB BC  ", "  CB BC  ", "  CB BC  ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "    C    ", "    C    ", "    C    ", "    C    ", "    C    ")
                .aisle("  AA~AA  ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "   CBC   ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .port('~', RIGHT_OUT, RelativeDirection.BACK)
                .port('~', LEFT_OUT, RelativeDirection.BACK)
                .build();
    }

    private static Piece rightPiece() {
        return Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                .aisle("        BBBBBB ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       BBBBBBBB", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("@      BBBBBBBB", "               ", "               ", "               ", "               ", "          BB   ", "          DD   ", "          BB   ", "               ", "               ", "               ", "               ", "          BB   ", "          DD   ", "          BB   ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       BBBBBBBB", "          BB   ", "          BB   ", "          BB   ", "          BB   ", "         BBBB  ", "         DBBD  ", "         BBBB  ", "          BB   ", "          BB   ", "          BB   ", "          BB   ", "         BBBB  ", "         DBBD  ", "         BBBB  ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       BBBBBBBB", "          BB   ", "          BB   ", "          BB   ", "          BB   ", "         BBBB  ", "         DBBD  ", "         BBBB  ", "          BB   ", "          BB   ", "          BB   ", "          BB   ", "B        BBBB  ", "B        DBBD  ", "B        BBBB  ", "B              ", "B              ", "B              ", "B              ", "B              ")
                .aisle("       BBBBBBBB", "               ", "               ", "               ", "               ", "          BB   ", "          DD   ", "          BB   ", "               ", "               ", "               ", "               ", "          BB   ", "          DD   ", "          BB   ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       BBBBBBBB", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("        BBBBBB ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .port('@', StructureModuleKeys.MODULE_IN, RelativeDirection.FRONT)
                .build();
    }

    private static Piece advancedRightPiece() {
        return Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                .aisle("        BBBBBB ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       BBBBBBBB", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("@      BBBBBBBB", "               ", "               ", "               ", "               ", "          BB   ", "          EE   ", "          BB   ", "               ", "               ", "               ", "               ", "          BB   ", "          EE   ", "          BB   ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       BBBBBBBB", "          BB   ", "          BB   ", "          BB   ", "          BB   ", "         BBBB  ", "         EBBE  ", "         BBBB  ", "          BB   ", "          BB   ", "          BB   ", "          BB   ", "         BBBB  ", "         EBBE  ", "         BBBB  ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       BBBBBBBB", "          BB   ", "          BB   ", "          BB   ", "          BB   ", "         BBBB  ", "         EBBE  ", "         BBBB  ", "          BB   ", "          BB   ", "          BB   ", "          BB   ", "B        BBBB  ", "B        EBBE  ", "B        BBBB  ", "B              ", "B              ", "B              ", "B              ", "B              ")
                .aisle("       BBBBBBBB", "               ", "               ", "               ", "               ", "          BB   ", "          EE   ", "          BB   ", "               ", "               ", "               ", "               ", "          BB   ", "          EE   ", "          BB   ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       BBBBBBBB", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("        BBBBBB ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .port('@', StructureModuleKeys.MODULE_IN, RelativeDirection.FRONT)
                .build();
    }

    private static Piece leftPiece() {
        return Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                .aisle(" BBBBBB        ", "               ", "    CC         ", "  CC           ", "               ", "               ", "               ", "               ", "    CC         ", "  CC           ", "               ", "               ", "               ", "               ", "    CC         ", "  CC           ", "               ", "               ", "               ", "               ", "    CC         ", "  CC           ", "               ", "               ", "               ", "               ", "               ")
                .aisle("BBBBBBBB       ", "      C        ", "               ", "               ", " C             ", "               ", "               ", "      C        ", "               ", "               ", " C             ", "               ", "               ", "      C        ", "               ", "               ", " C             ", "               ", "               ", "      C        ", "               ", "               ", " C             ", "               ", "               ", "               ", "               ")
                .aisle("BBBBBBBB      @", "               ", "               ", "               ", "   BB          ", "C  EE          ", "   BB  C       ", "               ", "               ", "               ", "               ", "C              ", "       C       ", "               ", "   BB          ", "   BB          ", "   BB          ", "C  BB          ", "       C       ", "               ", "               ", "               ", "               ", "C              ", "   BB  C       ", "   EECC        ", "   BB          ")
                .aisle("BBBBBBBB       ", "   BB          ", "   BB          ", "   BB          ", "  BBBB         ", "C EBBE         ", "  BBBB C       ", "   BB          ", "   BB          ", "   BB          ", "   BB          ", "C  BB          ", "   BB  C       ", "   BB          ", "  BBBB         ", "  BBBB         ", "  BBBB         ", "C BBBB         ", "   BB  C       ", "   BB          ", "   BB          ", "   BB          ", "   BB          ", "C  BB          ", "  BBBB C       ", "  EBBE         ", "  BBBB         ")
                .aisle("BBBBBBBB       ", "   BB          ", "   BB          ", "   BB          ", "  BBBB         ", "  EBBE C       ", "C BBBB         ", "   BB          ", "   BB          ", "   BB          ", "   BB          ", "   BB  C       ", "C  BB          ", "   BB          ", "  BBBB         ", "  BBBB         ", "  BBBB         ", "  BBBB C       ", "C  BB          ", "   BB          ", "   BB          ", "   BB          ", "   BB          ", "   BB  C       ", "C BBBB         ", "  EBBE         ", "  BBBB         ")
                .aisle("BBBBBBBB       ", "               ", "               ", "               ", "   BB          ", "   EE  C       ", "C  BB          ", "               ", "               ", "               ", "               ", "       C       ", "C              ", "               ", "   BB          ", "   BB          ", "   BB          ", "   BB  C       ", "C              ", "               ", "               ", "               ", "               ", "       C       ", "C  BB          ", " CCEE          ", "   BB          ")
                .aisle("BBBBBBBB       ", " C             ", "               ", "               ", "      C        ", "               ", "               ", " C             ", "               ", "               ", "      C        ", "               ", "               ", " C             ", "               ", "               ", "      C        ", "               ", "               ", " C             ", "               ", "               ", "      C        ", "               ", "               ", "               ", "               ")
                .aisle(" BBBBBB        ", "               ", "  CC           ", "    CC         ", "               ", "               ", "               ", "               ", "  CC           ", "    CC         ", "               ", "               ", "               ", "               ", "  CC           ", "    CC         ", "               ", "               ", "               ", "               ", "  CC           ", "    CC         ", "               ", "               ", "               ", "               ", "               ")
                .port('@', StructureModuleKeys.MODULE_IN, RelativeDirection.FRONT)
                .build();
    }

    @Override
    public long getMaxParallel() {
        return getEffectiveTier() > 0 ? getStorageStack().getCount() : 0;
    }

    @Override
    public long getMinParallel() {
        return Math.min(IParallelMachine.MIN_PARALLEL, getMaxParallel());
    }
}
