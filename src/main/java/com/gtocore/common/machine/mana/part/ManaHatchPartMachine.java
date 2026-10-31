package com.gtocore.common.machine.mana.part;

import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.utils.ManaUnification;

import com.gtolib.api.GTOValues;
import com.gtolib.api.machine.ManaDistributorMachine;
import com.gtolib.api.machine.mana.feature.IManaMachine;
import com.gtolib.api.machine.mana.trait.NotifiableManaContainer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.handler.IO;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import com.gto.datasynclib.annotations.SaveToDisk;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.mana.ManaEndpoint;

public class ManaHatchPartMachine extends WorkableTieredIOPartMachine implements IManaMachine {

    /** tick 耗时监控（只有被 Jade 查看时才计时）；子类共用这一个。 */
    protected TickTimeMonitor manaMonitor = holder.monitorTick(GTOTickTimeMonitors.MANA, this::tickUpdate);

    TickableSubscription tickSubs;
    @SaveToDisk
    private final NotifiableManaContainer manaContainer;
    final ManaEndpoint manaEndpoint;

    public ManaHatchPartMachine(MetaMachineBlockEntity holder, int tier, IO io, int rate) {
        super(holder, tier, io);
        manaContainer = createManaContainer(rate);
        manaEndpoint = ManaUnification.of(manaContainer, 20);
        manaContainer.setAcceptDistributor(io == IO.IN);
    }

    NotifiableManaContainer createManaContainer(int rate) {
        int tierMana = GTOValues.MANA[tier] * rate;
        if (io == IO.OUT) {
            return new NotifiableManaContainer(this, IO.OUT, 256L * tierMana, 4L * tierMana);
        } else return new NotifiableManaContainer(this, IO.IN, 256L * tierMana, 4L * tierMana);
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ManaDistributorMachine) {
            manaContainer.setAcceptDistributor(false);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && io == IO.OUT) {
            tickSubs = subscribeServerTick(tickSubs, manaMonitor, 20);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    void tickUpdate() {
        if (isWorkingEnabled()) {
            Level level = getLevel();
            if (level == null) return;
            var facing = getFrontFacing();
            var receiver = this.findManaReceiver(facing, null);
            if (receiver != null && !receiver.isFull()) {
                if (ManaEndpoint.transfer(manaEndpoint, receiver, manaContainer.getCurrentMana()) > 0) {
                    manaContainer.notifyListeners();
                }
            }
            var jarTile = findSourceJar(facing);
            if (jarTile == null) return;
            if (!jarTile.canAcceptSource()) return;
            if (ManaEndpoint.transfer(manaEndpoint, ManaUnification.of(jarTile), manaContainer.getCurrentMana()) > 0) {
                manaContainer.notifyListeners();
            }
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return io == IO.IN;
    }

    @Override
    public @NotNull NotifiableManaContainer getManaContainer() {
        return this.manaContainer;
    }
}
