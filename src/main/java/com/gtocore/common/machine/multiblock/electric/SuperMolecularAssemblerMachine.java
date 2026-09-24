package com.gtocore.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/**
 * 超级分子装配室：把合成样板仓里攒下的合成产物直接插回 ME 网络（见 {@link AbstractMEPatternAssemblerMachine}）。
 * <p>
 * 每轮处理<b>所有</b>样板仓的全部内部槽位：每次运行同时处理所有的配方以及所有的输入物品，
 * 每个物品合成消耗 1EU。
 */
public class SuperMolecularAssemblerMachine extends AbstractMEPatternAssemblerMachine {

    public SuperMolecularAssemblerMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    protected boolean planOutputs() {
        for (var machine : partMachines) {
            for (var slot : machine.getInternalInventory()) {
                var amount = slot.getAmount();
                if (amount < 1) continue;
                if (slot.getOutput() == null) continue;
                plan(machine, slot, amount);
            }
        }
        return plannedCount() > 0;
    }
}
