package com.gtocore.common.machine.multiblock.storage;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import java.util.ArrayList;
import java.util.List;

/**
 * 多方块里的物品输入总线：成型时从匹配上下文收集一遍并给它的槽挂上内容变化监听。
 * <p>
 * 抽屉存储器（放抽屉）与 ME 磁盘存储器（放存储组件）都靠总线内容算容量，共用这一份收集/监听逻辑：
 * 内容是普通的 {@link NotifiableItemStackHandler} 才算（AE 中转仓不是这个类型，自然被排除），
 * 回调在总线内容变化时触发，改动立刻重算、不需要轮询。
 */
final class StorageBusListener {

    private final List<NotifiableItemStackHandler> buses = new ArrayList<>();
    private final List<ISubscription> subscriptions = new ArrayList<>();

    /** 收集物品输入总线并挂监听；要在算容量之前调用（容量本身就取这些总线的内容）。 */
    void bind(PatternMatchContext context, MetaMachine controller, Runnable onChange) {
        unbind();
        for (var part : context.getParts()) {
            var machine = part.self();
            if (machine == controller || !PartAbility.IMPORT_ITEMS.isApplicable(machine.getDefinition().get())) continue;
            if (machine.getItemHandlerCap(null, false) instanceof NotifiableItemStackHandler bus) {
                buses.add(bus);
                subscriptions.add(bus.addChangedListener(onChange));
            }
        }
    }

    void unbind() {
        subscriptions.forEach(ISubscription::unsubscribe);
        subscriptions.clear();
        buses.clear();
    }

    List<NotifiableItemStackHandler> buses() {
        return buses;
    }
}
