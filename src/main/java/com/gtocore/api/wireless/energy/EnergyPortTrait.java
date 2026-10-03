package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

import com.gto.datasynclib.annotations.SaveToDisk;

/**
 * 机器上的电网端点：存盘优先级，机器加载时应用、卸载时释放端点。
 */
public final class EnergyPortTrait extends MachineTrait {

    private final EnergyPort port;
    @SaveToDisk(defaultValue = GridScheduler.DEFAULT_PRIORITY_TEXT)
    private int priority = GridScheduler.DEFAULT_PRIORITY;

    public EnergyPortTrait(MetaMachine machine, PortKind kind, int tier) {
        super(machine);
        this.port = new EnergyPort(kind, machine, tier);
    }

    public EnergyPort port() {
        return port;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = port.setPriority(priority);
    }

    public void release() {
        port.release();
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        port.setPriority(priority);
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        port.release();
    }
}
