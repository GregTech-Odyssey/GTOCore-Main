package com.gtocore.common.machine.multiblock.storage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.world.item.ItemStack;

import appeng.items.materials.StorageComponentItem;

/**
 * ME 磁盘存储器：和 ME 存储器（{@link MEStorageMachine}）同一套实现——存储本身由结构里的
 * {@code StorageAccessPartMachine 存储访问仓}承担，本机只负责给它定容量与数据索引；
 * 数据索引位置同样有玩家 / 机器两种（界面上点一下切换）。
 * <p>
 * 区别只在容量来源：容量 = 输入总线里 AE2 存储组件（1k…256k）的字节之和，没有无限存储。
 */
public final class MEDiskStorageMachine extends MEStorageMachine {

    private final StorageBusListener componentBuses = new StorageBusListener();

    public MEDiskStorageMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        // 先挂总线监听（容量取自这些总线），再让父类绑定访问仓
        componentBuses.bind(getMultiblockState().getMatchContext(), this, this::onComponentBusChanged);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        componentBuses.unbind();
        super.onStructureInvalid();
    }

    @Override
    public void onUnload() {
        componentBuses.unbind();
        super.onUnload();
    }

    /** 容量 = 输入总线里 AE2 存储组件的字节之和。 */
    @Override
    protected double getStorageCapacity() {
        long bytes = 0;
        for (var bus : componentBuses.buses()) {
            for (int i = 0, slots = bus.getSlots(); i < slots; i++) {
                long term = componentBytes(bus.getStackInSlot(i));
                if (term < 1) continue;
                bytes = bytes > Long.MAX_VALUE - term ? Long.MAX_VALUE : bytes + term;
            }
        }
        return bytes;
    }

    /// 磁盘存储器没有无限模式
    @Override
    protected boolean isInfiniteStorage(double capacity) {
        return false;
    }

    private static long componentBytes(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageComponentItem component)) return 0;
        long bytes = component.getBytes();
        long count = stack.getCount();
        return bytes > Long.MAX_VALUE / count ? Long.MAX_VALUE : bytes * count;
    }

    /** 输入总线里的存储组件增减：容量立刻跟着变。 */
    private void onComponentBusChanged() {
        if (isRemote() || !isFormed || accessPartMachine == null) return;
        accessPartMachine.setCapacity(getStorageCapacity());
        onChanged();
    }
}
