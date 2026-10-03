package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.EnergyPort;
import com.gtocore.api.wireless.energy.GridScheduler;
import com.gtocore.api.wireless.energy.PortKind;
import com.gtocore.api.wireless.energy.U126;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.electric.HullMachine;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.api.machine.WirelessEnergyReceiveCoverHolder;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 从队伍电网取电送入所在机器；由 GridScheduler 按优先级服务，按目标缓冲选择调度间隔，一次调度按实际接收量扣电。
 */
public final class WirelessReceiveCover extends CoverBehavior implements IBindable, IUICover {

    private final int tier;
    private final long voltage;
    private final long perTick;
    @SaveToDisk(defaultValue = GridScheduler.DEFAULT_PRIORITY_TEXT)
    private int priority = GridScheduler.DEFAULT_PRIORITY;
    @Nullable
    private MetaMachine machine;
    @Nullable
    private EnergyPort port;
    @Nullable
    private IEnergyContainer target;
    private int workTicks = 1;
    private long carry;

    public WirelessReceiveCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier, int amperage) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.voltage = GTValues.VEX[tier];
        this.perTick = voltage * amperage;
    }

    @Override
    public boolean canAttach() {
        var host = machine();
        if (host instanceof TieredEnergyMachine tieredEnergyMachine && tieredEnergyMachine.energyContainer.getHandlerIO() == IO.IN && tieredEnergyMachine.getTier() >= this.tier) {
            for (var cover : tieredEnergyMachine.getCoverContainer().getCovers()) {
                if (cover instanceof WirelessReceiveCover) return false;
            }
            return true;
        } else if (host instanceof BatteryBufferMachine batteryBufferMachine) {
            return batteryBufferMachine.getTier() >= this.tier;
        } else if (host instanceof HullMachine hullMachine) {
            return hullMachine.getTier() >= this.tier;
        } else if (host instanceof WirelessEnergyReceiveCoverHolder holder) {
            return holder.getTier() >= this.tier;
        }
        return false;
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        var host = machine();
        if (host != null && host.getOwnerUUID() == null) host.setOwnerUUID(player.getUUID());
        start();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        start();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        stop();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        stop();
        machine = null;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = GridScheduler.clampPriority(priority);
        if (port != null) port.setPriority(this.priority);
        coverHolder.onChanged();
    }

    @Override
    public Widget createUIWidget() {
        return Form.page().addChild(UIElement.section().addChildren(PortPriorityUI.row(this::getPriority, this::setPriority)));
    }

    private void start() {
        var host = machine();
        if (host == null || coverHolder.isRemote()) return;
        if (port == null) {
            port = new EnergyPort(PortKind.RECEIVE_COVER, host, tier);
            port.setService(this::serve);
        }
        port.setPriority(priority);
        port.wake();
    }

    private void stop() {
        if (port != null) {
            port.returnUnused(carry);
            port.release();
        }
        target = null;
        carry = 0;
    }

    private int serve(EnergyPort port) {
        if (target == null) {
            target = GTCapabilityHelper.getEnergyContainer(coverHolder.holder(), attachedSide);
            if (target == null) return EnergyPort.IDLE_TICKS;
            workTicks = workTicks(target.getEnergyCapacity());
        }
        long free = target.getEnergyCapacity() - target.getEnergyStored();
        if (free <= 0) return EnergyPort.IDLE_TICKS;
        long budget = Math.min(free, U126.saturatedMul(perTick, workTicks));
        long got = carry + port.pull(Math.max(0, budget - carry));
        long delivered = 0;
        while (delivered < got) {
            long accepted = target.acceptEnergyFromNetwork(this, null, voltage, Math.min(perTick, got - delivered));
            if (accepted <= 0) break;
            delivered += accepted;
        }
        carry = got - delivered;
        return delivered > 0 ? workTicks : got < budget ? 1 : EnergyPort.IDLE_TICKS;
    }

    private int workTicks(long capacity) {
        long ticks = perTick <= 0 ? 1 : capacity / (perTick << 1);
        return (int) Math.max(1, Math.min(EnergyPort.IDLE_TICKS, ticks));
    }

    @Nullable
    private MetaMachine machine() {
        if (machine == null) machine = MetaMachine.getMachine(coverHolder.holder());
        return machine;
    }

    @Override
    @Nullable
    public UUID getUUID() {
        var host = machine();
        return host == null ? null : host.getOwnerUUID();
    }

    @Override
    public boolean cover() {
        return true;
    }

    @Override
    public boolean preferTeamName() {
        return true;
    }
}
