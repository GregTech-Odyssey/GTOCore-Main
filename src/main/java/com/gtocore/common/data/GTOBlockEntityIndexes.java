package com.gtocore.common.data;

import com.gtocore.integration.ae.wireless.WirelessMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;

import com.suried.gtoutils.spatial.BlockEntityIndex;
import vazkii.botania.common.block.flower.generating.EntropinnyumBlockEntity;

/**
 * GTOCore 全部按范围查找方块实体所用的索引，由 GTOUtils 在区块增删方块实体时自动维护；模组构造时调用 {@link #init()} 完成创建。
 */
public final class GTOBlockEntityIndexes {

    public static final BlockEntityIndex<MetaMachineBlockEntity> CLIENT_WIRELESS_MACHINES = BlockEntityIndex.create(MetaMachineBlockEntity.class,
            be -> be.getLevel().isClientSide() && be.getMetaMachine() instanceof WirelessMachine);
    public static final BlockEntityIndex<MetaMachineBlockEntity> TIERED_WORKABLE_MACHINES = BlockEntityIndex.create(MetaMachineBlockEntity.class,
            be -> !be.getLevel().isClientSide() && be.getMetaMachine() instanceof WorkableTieredMachine);
    public static final BlockEntityIndex<EntropinnyumBlockEntity> ENTROPINNYUMS = BlockEntityIndex.create(EntropinnyumBlockEntity.class,
            flower -> !flower.getLevel().isClientSide());

    private GTOBlockEntityIndexes() {}

    public static void init() {}
}
