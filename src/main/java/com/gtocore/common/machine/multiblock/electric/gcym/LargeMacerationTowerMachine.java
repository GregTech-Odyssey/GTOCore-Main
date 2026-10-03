package com.gtocore.common.machine.multiblock.electric.gcym;

import com.gtocore.common.data.GTOTickTimeMonitors;

import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.Keys;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;

import java.util.ArrayList;
import java.util.List;

public class LargeMacerationTowerMachine extends GCYMMultiblockMachine {

    private AABB grindBound = new AABB(BlockPos.ZERO);
    private final List<IKeyHandler<AEItemKey>> handlers = new ArrayList<>();

    private TickableSubscription hurtSub;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor manaMonitor = holder.monitorTick(GTOTickTimeMonitors.MANA, this::spinWheels);

    public LargeMacerationTowerMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStructureFormed() {
        super.onStructureFormed();
        updateBounds();
        for (var handler : getCapabilitiesFlat(IO.IN, IKeyHandler.class)) {
            if (handler.keyType() == AEKeyType.items()) handlers.add((IKeyHandler<AEItemKey>) handler);
        }
        hurtSub = subscribeServerTick(hurtSub, manaMonitor, 20);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        ITickSubscription.unsubscribe(hurtSub);
        hurtSub = null;
        handlers.clear();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        ITickSubscription.unsubscribe(hurtSub);
        hurtSub = null;
        handlers.clear();
    }

    private void updateBounds() {
        var fl = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 1, 2, 0);
        var br = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 2, -2, -4);
        grindBound = new AABB(fl, br);
    }

    private void spinWheels() {
        if (isRemote() || getLevel() == null || recipeLogic.isSuspend()) return;

        List<ItemEntity> itemEntities = new ArrayList<>();
        for (var entity : getLevel().getEntities(null, grindBound)) {
            if (entity instanceof ItemEntity ie) {
                itemEntities.add(ie);
            } else {
                if (recipeLogic.isWorking()) {
                    entity.hurt(entity.damageSources().cramming(), getTier());
                }
            }
        }

        if (handlers.isEmpty()) return;

        for (ItemEntity item : itemEntities) {
            if (item.isRemoved()) continue;
            var stack = item.getItem();
            var key = Keys.item(stack);
            if (key == null) continue;
            long count = stack.getCount();
            long left = count;
            for (var holder : handlers) {
                left -= holder.insert(key, left, false);
                if (left <= 0) break;
            }
            if (left <= 0) {
                item.discard();
            } else if (left < count) {
                item.setItem(stack.copyWithCount((int) left));
            }
        }
    }
}
