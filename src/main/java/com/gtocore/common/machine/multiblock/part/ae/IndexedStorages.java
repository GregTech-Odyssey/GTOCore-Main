package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.machine.noenergy.VirtualIngredientProviderMachine;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;

import com.glodblock.github.extendedae.common.inventory.InfinityCellInventory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * 按数据索引存储（存储访问仓、ME 磁盘箱子）共用的字节计算与存储转移。
 * 转移只从网络里真实的存储搬，跳过虚拟成分提供机与无限元件这类无限供给。
 */
public final class IndexedStorages {

    private IndexedStorages() {}

    public static long limitByBytes(long amount, double freeBytes, int perByte) {
        if (freeBytes <= 0) return 0;
        return (long) Math.min(amount, freeBytes * perByte);
    }

    public static double transferFromNetwork(IGrid grid, IStorageProvider self, MEStorage target, double freeBytes, IActionSource source) {
        var identities = new ReferenceOpenHashSet<>();
        var own = target.getResourceIdentity();
        if (own != null) identities.add(own);
        var sources = new ObjectArrayList<MEStorage>();
        IStorageMounts mounts = (storage, priority) -> {
            if (isInfiniteSupply(storage)) return;
            var identity = storage.getResourceIdentity();
            if (identity != null && !identities.add(identity)) return;
            sources.add(storage);
        };
        for (var node : grid.getNodes()) {
            if (!node.isActive()) continue;
            var provider = node.getService(IStorageProvider.class);
            if (provider != null && provider != self) provider.mountInventories(mounts);
        }
        double usedBytes = 0;
        var content = new KeyCounter();
        for (var storage : sources) {
            content.clear();
            storage.getAvailableStacks(content);
            for (var entry : content) {
                var what = entry.getKey();
                int perByte = what.getAmountPerByte();
                long want = limitByBytes(entry.getLongValue(), freeBytes - usedBytes, perByte);
                if (want < 1) continue;
                long possible = target.insert(what, want, Actionable.SIMULATE, source);
                if (possible < 1) continue;
                long extracted = storage.extract(what, possible, Actionable.MODULATE, source);
                if (extracted < 1) continue;
                long inserted = target.insert(what, extracted, Actionable.MODULATE, source);
                if (inserted < extracted) storage.insert(what, extracted - inserted, Actionable.MODULATE, source);
                usedBytes += (double) inserted / perByte;
            }
        }
        return usedBytes;
    }

    private static boolean isInfiniteSupply(MEStorage storage) {
        while (storage instanceof DelegatingMEInventory delegating) {
            storage = delegating.getDelegate();
        }
        return storage instanceof VirtualIngredientProviderMachine || storage instanceof InfinityCellInventory;
    }
}
