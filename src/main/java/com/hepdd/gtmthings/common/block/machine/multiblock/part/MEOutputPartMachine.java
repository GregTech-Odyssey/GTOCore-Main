package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

public class MEOutputPartMachine extends MultiblockPartMachine {

    public MEOutputPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public int tintColor(int index) {
        return index == 9 ? getRealColor() : super.tintColor(index);
    }
}
