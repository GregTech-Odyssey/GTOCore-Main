package com.gtocore.common.machine.mana.part;

import com.gtocore.utils.ManaUnification;

import com.gtolib.api.machine.mana.feature.IManaMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.handler.IO;

import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaEndpoint;
import vazkii.botania.api.mana.ManaPool;

public final class ManaExtractHatchPartMachine extends ManaHatchPartMachine {
    // 复用父类的 manaMonitor：同一个 entry 在同一个方块实体上只会有一个监控器，子类再声明一个也拿不到新的。

    public ManaExtractHatchPartMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.IN, 4);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubs = subscribeServerTick(tickSubs, manaMonitor, 20);
        }
    }

    @Override
    void tickUpdate() {
        if (isWorkingEnabled()) {
            Level level = getLevel();
            if (level != null && !isFull()) {
                var facing = getFrontFacing();
                var receiver = this.findManaReceiver(facing, null);
                if (receiver instanceof ManaPool || receiver instanceof IManaMachine) {
                    if (ManaEndpoint.transfer(receiver, manaEndpoint, receiver.getCurrentManaLong()) <= 0) return;
                }
                var jarTile = findSourceJar(facing);
                if (jarTile == null) return;
                ManaEndpoint.transfer(ManaUnification.of(jarTile), manaEndpoint, ManaUnification.sourceToMana(jarTile.getSourceLong()));
            }
        }
    }
}
