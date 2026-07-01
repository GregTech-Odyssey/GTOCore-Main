package com.gtocore.mixin.ae2.eae;

import com.gtocore.api.ae2.IStorageBusInventory;
import com.gtocore.config.StorageBusBlacklist;

import com.gtolib.api.blockentity.IDirectionCacheBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import appeng.api.parts.IPartItem;
import appeng.api.storage.MEStorage;
import appeng.me.storage.MEInventoryHandler;
import appeng.parts.PartAdjacentApi;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.Platform;

import com.glodblock.github.extendedae.common.parts.base.PartSpecialStorageBus;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PartSpecialStorageBus.class)
public abstract class PartSpecialStorageBusMixin extends UpgradeablePart {

    @Shadow(remap = false)
    @Final
    protected PartSpecialStorageBus.StorageBusInventory handler;

    @Shadow(remap = false)
    protected abstract void remountStorage();

    @Shadow(remap = false)
    @Final
    protected PartAdjacentApi<MEStorage> adjacentStorageAccessor;

    @Shadow(remap = false)
    protected abstract void updateTarget(boolean forceFullUpdate);

    @Shadow(remap = false)
    protected abstract void scheduleUpdate();

    public PartSpecialStorageBusMixin(IPartItem<?> partItem) {
        super(partItem);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void init(IPartItem partItem, CallbackInfo ci) {
        ((IStorageBusInventory) handler).setListener(this::remountStorage);
    }

    @Redirect(method = "updateTarget", at = @At(value = "INVOKE", target = "Lappeng/util/Platform;areBlockEntitiesTicking(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"), remap = false)
    private boolean areBlockEntitiesTicking(Level level, BlockPos pos) {
        ((IStorageBusInventory) handler).setIdentity(null);
        var host = getHost().getBlockEntity();
        if (host instanceof IDirectionCacheBlockEntity cacheBlockEntity) {
            var cache = cacheBlockEntity.gtolib$getDirectionCache();
            if (cache == null) return false;
            var n = cache.getAdjacentBlockEntity(level, host.getBlockPos(), getSide());
            if (n == null || StorageBusBlacklist.LIST.contains(n.getClass())) return false;
            ((IStorageBusInventory) handler).setIdentity(n);
            return Platform.areBlockEntitiesTicking(level, pos);
        }
        return false;
    }

    @Overwrite(remap = false)
    protected void checkStorageBusOnInterface() {}

    @Overwrite(remap = false)
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(getSide()).equals(neighbor)) {
            var te = adjacentStorageAccessor.getBlockEntity();
            if (te == null) {
                ((IStorageBusInventory) handler).setIdentity(null);
                // In case the TE was destroyed, we have to update the target handler immediately.
                this.updateTarget(false);
            } else {
                ((IStorageBusInventory) handler).setIdentity(te);
                this.scheduleUpdate();
            }
        }
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        handler.onUnmount(null);
        ((IStorageBusInventory) handler).setIdentity(null);
    }

    @Mixin(PartSpecialStorageBus.StorageBusInventory.class)
    public static class StorageBusInventoryMixin extends MEInventoryHandler implements IStorageBusInventory {

        @Nullable
        private Runnable listenerDelete;
        @Nullable
        private Runnable parentListenerDelete;
        @Setter
        private Runnable listener;
        @Setter
        private Object identity;

        public StorageBusInventoryMixin(MEStorage inventory) {
            super(inventory);
        }

        @Override
        public Object getResourceIdentity() {
            var identity = super.getResourceIdentity();
            return identity != null ? identity : this.identity;
        }

        @Override
        public void onMount(MEStorage parent) {
            listenerDelete = this.addMountListener(listener);
            parentListenerDelete = parent.addMountListener(listener);
        }

        @Override
        public void onUnmount(MEStorage parent) {
            if (listenerDelete != null) {
                listenerDelete.run();
                listenerDelete = null;
            }
            if (parentListenerDelete != null) {
                parentListenerDelete.run();
                parentListenerDelete = null;
            }
        }
    }
}
