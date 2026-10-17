package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GTOMachineProtocols;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Slot;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.common.data.GTMachines;

import com.gto.datasynclib.annotations.SyncToClient;

import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.FRONT;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.LEFT;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.UP;
import static com.gtocore.common.data.GTOMachines.EXHAUST_FAN;

@DataGeneratorScanned
public class WorkspaceExtension extends Extension {

    @RegisterLanguage(cn = "舱段长度", en = "Segment Length")
    private static final String LENGTH_NAME = "gtocore.multiblock.space_station_extension_module.length";
    @RegisterLanguage(cn = "中部重复舱段的数量，决定舱体长度与末端模块接口的位置", en = "Number of repeated middle segments; determines the module length and the position of the far-end module connectors")
    private static final String LENGTH_DESC = "gtocore.multiblock.space_station_extension_module.length.desc";

    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 9;
    public static final ParamKey LENGTH = ParamKey.of(LENGTH_NAME, LENGTH_DESC);
    public static final PortKey IN = PortKey.of("in");
    public static final PortKey OUT = PortKey.of("out");

    @SyncToClient
    private int length;

    public WorkspaceExtension(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
    }

    public int getLength() {
        return length;
    }

    @Override
    public void onStructureFormed() {
        var assembly = getAssembly();
        length = assembly == null ? 0 : assembly.get(LENGTH);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        length = 0;
        super.onStructureInvalid();
    }

    private static final String[][] BLOCK = {
            { "      ", "      ", "      ", "      ", "      ", " LpLpp", "      ", "FFFFFF", "      ", "      ", "      ", "FFFFFF", "      ", " LpLpp", "      ", "      ", "      ", "      ", "      " },
            { "      ", "      ", "      ", "      ", "     C", "LLLLLC", "OLLLOC", "LLLLLC", "LLLLLC", "GGGGGC", "LLLLLC", "LLLLLC", "OLLLOC", "LLLLLC", "     C", "      ", "      ", "      ", "      " },
            { "      ", "      ", "      ", "     C", "GGGGGG", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "GGGGGG", "     C", "      ", "      ", "      " },
            { "      ", "      ", "     C", "GGGGGG", "HIIIHH", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "HIIIHH", "GGGGGG", "     C", "      ", "      " },
            { "      ", "     C", "GGGGGG", "HIIIHH", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "HIIIHH", "GGGGGG", "     C", "      " },
            { "      ", "LLLLLC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "LLLLLC", "      " },
            { "      ", "LLLLLC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "LLLLLC", "      " },
            { "FFFFFF", "LLLLLC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "LLLLLC", "FFFFFF" },
            { "      ", "LLLLLC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "LLLLLC", "      " },
            { "      ", "GGGGGC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "GGGGGC", "      " },
            { "      ", "LLLLLC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "LLLLLC", "      " },
            { "FFFFFF", "LLLLLC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "LLLLLC", "FFFFFF" },
            { "      ", "LLLLLC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "LLLLLC", "      " },
            { "      ", "LLLLLC", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "LLLLLC", "      " },
            { "      ", "     C", "GGGGGG", "HIIIHH", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "HIIIHH", "GGGGGG", "     C", "      " },
            { "      ", "      ", "     C", "GGGGGG", "HIIIHH", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "HIIIHH", "GGGGGG", "     C", "      ", "      " },
            { "      ", "      ", "      ", "     C", "GGGGGG", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "pppppp", "GGGGGG", "     C", "      ", "      ", "      " },
            { "      ", "      ", "      ", "      ", "     C", "LLLLLC", "OLLLOC", "LLLLLC", "LLLLLC", "GGGGGC", "LLLLLC", "LLLLLC", "OLLLOC", "LLLLLC", "     C", "      ", "      ", "      ", "      " },
            { "      ", "      ", "      ", "      ", "      ", " LpLpp", "      ", "FFFFFF", "      ", "      ", "      ", "FFFFFF", "      ", " LpLpp", "      ", "      ", "      ", "      ", "      " },

    };
    private static final String[][] HEAD = {
            { "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "               FF", "                 ", "                 ", "                 ", "               FF", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 " },
            { "                 ", "                 ", "                 ", "                 ", "                C", "                C", "               EC", "              FLC", "              NLC", "              NLC", "              NLC", "              FLC", "                C", "                C", "                C", "                 ", "                 ", "                 ", "                 " },
            { "                 ", "                 ", "                 ", "                C", "               EG", "               Ep", "              FHp", "            FFAAp", "              AAp", "              AAp", "              AAp", "            FFAAp", "              FHp", "               Ep", "                G", "                C", "                 ", "                 ", "                 " },
            { "                 ", "                 ", "                C", "               EG", "               EH", "              FHp", "              AAp", "           FLLppp", "           FLLppp", "      FFFFFFMLppp", "           FLLppp", "           FLLppp", "              AAp", "              FHp", "               EH", "                G", "                C", "                 ", "                 " },
            { "                 ", "                C", "               EG", "               EH", "              FHp", "            LLAAp", "         KppLLppp", "       EEKEEGpppp", "       EEKEEGpppp", "      FEEKEEGpppp", "       EEKEEGpppp", "       EEKEEGpppp", "         KppLLppp", "            LLAAp", "              FHp", "               EH", "                G", "                C", "                 " },
            { "                 ", "                C", "               Ep", "              FHp", "            LLAAp", "         KppLLppp", "       EEKEEGpppp", "      AHJJJJppppp", "     FAHJJJJppppp", "  CCFFAIJJJJppppp", "     FAHJJJJppppp", "      AHJJJJppppp", "       EEKEEGpppp", "         KppLLppp", "            LLAAp", "              FHp", "               Ep", "                C", "                 " },
            { "                 ", "               EC", "              FHp", "              AAp", "         KppLLppp", "       EEKEEGpppp", "      AHHHHHGpppp", "     FApppppppppp", "   EEAGpppppppppp", "   CCAGpppppppppp", "   EEAGpppppppppp", "     FApppppppppp", "      AHHHHHGpppp", "       EEKEEGpppp", "         KppLLppp", "              AAp", "              FHp", "                C", "                 " },
            { "               FF", "              FLC", "            FFAAp", "           FLLppp", "       EEKEEGpppp", "      AHJJJJppppp", "     FApppppppppp", "   EEAGpppppppppp", "AAAAAAppppppppppp", "ABDDAAppppppppppp", "AAAAAAppppppppppp", "   EEAGpppppppppp", "     FApppppppppp", "      AHJJJJppppp", "       EEKEEGpppp", "           FLLppp", "            FFAAp", "              FLC", "               FF" },
            { "                 ", "              NLC", "              AAp", "           FLLppp", "       EEKEEGpppp", "     FAHJJJJppppp", "   EEAGpppppppppp", "   AAAppppppppppp", "ppppppppppppppppp", "ppppppppppppppppp", "ppppppppppppppppp", "   AAAppppppppppp", "   EEAGpppppppppp", "     FAHJJJJppppp", "       EEKEEGpppp", "           FLLppp", "              AAp", "              NLC", "                 " },
            { "                 ", "              NLC", "              AAp", "      FFFFFFMLppp", "      FEEKEEGpppp", "  CCFFAIJJJJppppp", "   CCAGpppppppppp", "   DAAppppppppppp", "ppppppppppppppppp", "ppppppppppppppppp", "ppppppppppppppppp", "   DAAppppppppppp", "   CCAGpppppppppp", "  CCFFAIJJJJppppp", "      FEEKEEGpppp", "      FFFFFFMLppp", "              AAp", "              NLC", "                 " },
            { "                 ", "              NLC", "              AAp", "           FLLppp", "       EEKEEGpppp", "     FAHJJJJppppp", "   EEAGpppppppppp", "   AAAppppppppppp", "ppppppppppppppppp", "ppppppppppppppppp", "ppppppppppppppppp", "   AAAppppppppppp", "   EEAGpppppppppp", "     FAHJJJJppppp", "       EEKEEGpppp", "           FLLppp", "              AAp", "              NLC", "                 " },
            { "               FF", "              FLC", "            FFAAp", "           FLLppp", "       EEKEEGpppp", "      AHJJJJppppp", "     FApppppppppp", "   EEAGpppppppppp", "   AAAppppppppppp", "   DAAppppppppppp", "   AAAppppppppppp", "   EEAGpppppppppp", "     FApppppppppp", "      AHJJJJppppp", "       EEKEEGpppp", "           FLLppp", "            FFAAp", "              FLC", "               FF" },
            { "                 ", "               EC", "              FHp", "              AAp", "         KppLLppp", "       EEKEEGpppp", "      AHHHHHGpppp", "     FApppppppppp", "   EEAGpppppppppp", "   CCAGpppppppppp", "   EEAGpppppppppp", "     FApppppppppp", "      AHHHHHGpppp", "       EEKEEGpppp", "         KppLLppp", "              AAp", "              FHp", "               EC", "                 " },
            { "                 ", "                C", "               Ep", "              FHp", "            LLAAp", "         KppLLppp", "       EEKEEGpppp", "      AHJJJJppppp", "     FAHJJJJppppp", "  CCFFAIJJJJppppp", "     FAHJJJJppppp", "      AHJJJJppppp", "       EEKEEGpppp", "         KppLLppp", "            LLAAp", "              FHp", "               Ep", "                C", "                 " },
            { "                 ", "                C", "               EG", "               EH", "              FHp", "            LLAAp", "         KppLLppp", "       EEKEEGpppp", "       EEKEEGpppp", "      FEEKEEGpppp", "       EEKEEGpppp", "       EEKEEGpppp", "         KppLLppp", "            LLAAp", "              FHp", "               EH", "               EG", "                C", "                 " },
            { "                 ", "                 ", "                C", "               EG", "               EH", "              FHp", "              AAp", "           FLLppp", "           FLLppp", "      FFFFFFMLppp", "           FLLppp", "           FLLppp", "              AAp", "              FHp", "               EH", "               EG", "                C", "                 ", "                 " },
            { "                 ", "                 ", "                 ", "                C", "               EG", "               Ep", "              FHp", "            FFAAp", "              AAp", "              AAp", "              AAp", "            FFAAp", "              FHp", "               Ep", "               EG", "                C", "                 ", "                 ", "                 " },
            { "                 ", "                 ", "                 ", "                 ", "                C", "                C", "               EC", "              FLC", "              NLC", "              NLC", "              NLC", "              FLC", "               EC", "                C", "                C", "                 ", "                 ", "                 ", "                 " },
            { "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "               FF", "                 ", "                 ", "                 ", "               FF", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 " },
    };

    private static final String[][] TAIL = {
            { "          ", "          ", "          ", "          ", "          ", "          ", "          ", "F         ", "          ", "          ", "          ", "F         ", "          ", "          ", "          ", "          ", "          ", "          ", "          " },
            { "          ", "          ", "          ", "          ", "          ", "          ", "E         ", "LF        ", "LN        ", "LN        ", "LN        ", "LF        ", "E         ", "          ", "          ", "          ", "          ", "          ", "          " },
            { "          ", "          ", "          ", "          ", "E         ", "E         ", "HF        ", "AAFF      ", "AA        ", "AA        ", "AA        ", "AAFF      ", "HF        ", "E         ", "E         ", "          ", "          ", "          ", "          " },
            { "          ", "          ", "          ", "E         ", "E         ", "HF        ", "AA        ", "ppLLF     ", "ppLLF     ", "ppLMFF    ", "ppLLF     ", "ppLLF     ", "AA        ", "HF        ", "E         ", "E         ", "          ", "          ", "          " },
            { "          ", "          ", "E         ", "E         ", "HF        ", "AALL      ", "ppLL      ", "pppGE     ", "pppGE     ", "pppGEF    ", "pppGE     ", "pppGE     ", "ppLL      ", "AALL      ", "HF        ", "E         ", "E         ", "          ", "          " },
            { "          ", "          ", "E         ", "HF        ", "AALL      ", "ppLL      ", "pppGE     ", "ppppAA    ", "ppppAAF   ", "ppppAAFFCC", "ppppAAF   ", "ppppAA    ", "pppGE     ", "ppLL      ", "AALL      ", "HF        ", "E         ", "          ", "          " },
            { "          ", "E         ", "HF        ", "AA        ", "ppLL      ", "pppGE     ", "pppGIA    ", "pppppAF   ", "pppppGAEE ", "pppppGACC ", "pppppGAEE ", "pppppAF   ", "pppGIA    ", "pppGE     ", "ppLL      ", "AA        ", "HF        ", "E         ", "          " },
            { "F         ", "LF        ", "AAFF      ", "ppLLF     ", "pppGE     ", "ppppAA    ", "pppppAF   ", "pppppGAEE ", "ppppppAAA ", "ppppppAcD ", "ppppppAAA ", "pppppGAEE ", "pppppAF   ", "ppppAA    ", "pppGE     ", "ppLLF     ", "AAFF      ", "LF        ", "F         " },
            { "          ", "LN        ", "AA        ", "ppLLF     ", "pppGE     ", "ppppAAF   ", "pppppGAEE ", "ppppppAAA ", "pppppppppp", "pppppppppp", "pppppppppp", "ppppppAAA ", "pppppGAEE ", "ppppAAF   ", "pppGE     ", "ppLLF     ", "AA        ", "LN        ", "          " },
            { "          ", "LN        ", "AA        ", "ppLMFF    ", "pppGEF    ", "ppppAAFFCC", "pppppGACC ", "ppppppAcD ", "pppppppppp", "pppppppppp", "pppppppppp", "ppppppAcD ", "pppppGACC ", "ppppAAFFCC", "pppGEF    ", "ppLMFF    ", "AA        ", "LN        ", "          " },
            { "          ", "LN        ", "AA        ", "ppLLF     ", "pppGE     ", "ppppAAF   ", "pppppGAEE ", "ppppppAAA ", "pppppppppp", "pppppppppp", "pppppppppp", "ppppppAAA ", "pppppGAEE ", "ppppAAF   ", "pppGE     ", "ppLLF     ", "AA        ", "LN        ", "          " },
            { "F         ", "LF        ", "AAFF      ", "ppLLF     ", "pppGE     ", "ppppAA    ", "pppppAF   ", "pppppGAEE ", "ppppppAAA ", "ppppppAcD ", "ppppppAAA ", "pppppGAEE ", "pppppAF   ", "ppppAA    ", "pppGE     ", "ppLLF     ", "AAFF      ", "LF        ", "F         " },
            { "          ", "E         ", "HF        ", "AA        ", "ppLL      ", "pppGE     ", "pppGIA    ", "pppppAF   ", "pppppGAEE ", "pppppGACC ", "pppppGAEE ", "pppppAF   ", "pppGIA    ", "pppGE     ", "ppLL      ", "AA        ", "HF        ", "          ", "          " },
            { "          ", "          ", "E         ", "HF        ", "AALL      ", "ppLL      ", "pppGE     ", "ppppAA    ", "ppppAAF   ", "ppppAAFFCC", "ppppAAF   ", "ppppAA    ", "pppGE     ", "ppLL      ", "AALL      ", "HF        ", "E         ", "          ", "          " },
            { "          ", "          ", "E         ", "E         ", "HF        ", "AALL      ", "ppLL      ", "pppGE     ", "pppGE     ", "pppGEF    ", "pppGE     ", "pppGE     ", "ppLL      ", "AALL      ", "HF        ", "E         ", "          ", "          ", "          " },
            { "          ", "          ", "          ", "E         ", "E         ", "HF        ", "AA        ", "ppLLF     ", "ppLLF     ", "ppLMFF    ", "ppLLF     ", "ppLLF     ", "AA        ", "HF        ", "E         ", "          ", "          ", "          ", "          " },
            { "          ", "          ", "          ", "          ", "E         ", "E         ", "HF        ", "AAFF      ", "AA        ", "AA        ", "AA        ", "AAFF      ", "HF        ", "E         ", "          ", "          ", "          ", "          ", "          " },
            { "          ", "          ", "          ", "          ", "          ", "          ", "E         ", "LF        ", "LN        ", "LN        ", "LN        ", "LF        ", "          ", "          ", "          ", "          ", "          ", "          ", "          " },
            { "          ", "          ", "          ", "          ", "          ", "          ", "          ", "F         ", "          ", "          ", "          ", "F         ", "          ", "          ", "          ", "          ", "          ", "          ", "          " },
    };

    public static Structure structure(MultiblockMachineDefinition definition, int min, int max) {
        var head = transposed(HEAD).portAfter(OUT).build();
        var block = transposed(BLOCK).portBefore(IN).portAfter(OUT).build();
        var tail = transposed(TAIL).portBefore(IN).port('c', GTOMachineProtocols.STATION_RING).build();
        return Structure.root(head)
                .symbols(Symbols.create()
                        .where('A', blocks(GTOBlocks.TITANIUM_ALLOY_FRAME_INTERNAL.get()))
                        .where('B', controller(definition))
                        .where('C', blocks(GTOBlocks.ALUMINUM_ALLOY_7050_SUPPORT_MECHANICAL_BLOCK.get()))
                        .where('D', blocks(GTOBlocks.SPACECRAFT_DOCKING_CASING.get()))
                        .where('E', blocks(GTOBlocks.ALUMINUM_ALLOY_2090_SKIN_MECHANICAL_BLOCK.get()))
                        .where('F', GTOPredicates.frame(GTOMaterials.StainlessSteel316))
                        .where('G', blocks(GTOBlocks.PRESSURE_RESISTANT_HOUSING_MECHANICAL_BLOCK.get()))
                        .where('H', blocks(GTOBlocks.SPACECRAFT_SEALING_MECHANICAL_BLOCK.get()))
                        .where('I', GTOPredicates.light())
                        .where('J', blocks(GTOBlocks.SPACE_STATION_CONTROL_CASING.get()))
                        .where('K', blocks(GTOBlocks.ALUMINUM_ALLOY_8090_SKIN_MECHANICAL_BLOCK.get()))
                        .where('L', blocks(GTOBlocks.TITANIUM_ALLOY_PROTECTIVE_MECHANICAL_BLOCK.get()))
                        .where('M', blocks(GTOBlocks.SPACE_ENGINE_NOZZLE.get()))
                        .where('N', blocks(GTOBlocks.LOAD_BEARING_STRUCTURAL_STEEL_MECHANICAL_BLOCK.get()))
                        .where('O', blocks(Stream.of(GTMachines.HULL).map(MachineDefinition::get).toArray(MetaMachineBlock[]::new))
                                .or(blocks(EXHAUST_FAN.get())))
                        .where('p', ISpacePredicateMachine.innerBlockPredicate.get())
                        .where(' ', any()))
                .atPort(OUT, Slot.chain(block, IN, OUT, IN).count(LENGTH, min, max)
                        .atPort(OUT, Slot.one(tail, IN).atPort(GTOMachineProtocols.STATION_RING, Slot.machines(GTOMachineProtocols.STATION_DOCKING))))
                .build();
    }

    private static Piece.Builder transposed(String[][] part) {
        var builder = Piece.start(FRONT, UP, LEFT);
        int strings = part[0].length;
        int chars = part[0][0].length();
        for (int a = 0; a < chars; a++) {
            var aisle = new String[strings];
            for (int b = 0; b < strings; b++) {
                var row = new char[part.length];
                for (int c = 0; c < part.length; c++) row[c] = part[c][b].charAt(a);
                aisle[b] = new String(row);
            }
            builder.aisle(aisle);
        }
        return builder;
    }
}
