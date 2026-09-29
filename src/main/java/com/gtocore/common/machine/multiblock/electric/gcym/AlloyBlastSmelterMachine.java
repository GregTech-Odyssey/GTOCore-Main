package com.gtocore.common.machine.multiblock.electric.gcym;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.CoilCustomParallelMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;

import java.util.function.ToLongFunction;

@DataGeneratorScanned
public final class AlloyBlastSmelterMachine extends CoilCustomParallelMultiblockMachine {

    @RegisterLanguage(cn = "扩展冶炼室", en = "Auxiliary Smelting Chamber")
    public static final String EXTENSION_NAME = "gtocore.multiblock.alloy_blast_smelter.extension";
    @RegisterLanguage(cn = "搭建后配方耗时减半，并可在其中额外安装输入输出仓", en = "When built, recipe duration is halved and additional input/output hatches can be installed in it")
    public static final String EXTENSION_DESC = "gtocore.multiblock.alloy_blast_smelter.extension.desc";
    public static final ParamKey EXTENSION = ParamKey.of(EXTENSION_NAME, EXTENSION_DESC);

    public AlloyBlastSmelterMachine(MetaMachineBlockEntity holder, ToLongFunction<CoilCustomParallelMultiblockMachine> parallel) {
        super(holder, true, false, parallel);
    }
}
