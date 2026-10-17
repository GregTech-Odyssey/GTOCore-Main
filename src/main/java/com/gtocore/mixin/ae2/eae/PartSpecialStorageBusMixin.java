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
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PartSpecialStorageBus.class)
public abstract class PartSpecialStorageBusMixin extends UpgradeablePart {

    @Shadow(remap = false)
    @Final
    protected PartSpecialStorageBus.StorageBusInventory handler;

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

    @Redirect(method = "updateTarget", at = @At(value = "INVOKE", target = "Lappeng/util/Platform;areBlockEntitiesTicking(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"), remap = false)
    private boolean areBlockEntitiesTicking(Level level, BlockPos pos) {
        ((IStorageBusInventory) handler).gtocore$setIdentity(null);
        var host = getHost().getBlockEntity();
        if (host instanceof IDirectionCacheBlockEntity cacheBlockEntity) {
            var cache = cacheBlockEntity.gtolib$getDirectionCache();
            if (cache == null) return false;
            var n = cache.getAdjacentBlockEntity(level, host.getBlockPos(), getSide());
            if (n == null || StorageBusBlacklist.LIST.contains(n.getClass())) return false;
            ((IStorageBusInventory) handler).gtocore$setIdentity(n);
            return Platform.areBlockEntitiesTicking(level, pos);
        }
        return false;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    protected void checkStorageBusOnInterface() {}

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(getSide()).equals(neighbor)) {
            var te = adjacentStorageAccessor.getBlockEntity();
            if (te == null) {
                ((IStorageBusInventory) handler).gtocore$setIdentity(null);
                // In case the TE was destroyed, we have to update the target handler immediately.
                this.updateTarget(false);
            } else {
                ((IStorageBusInventory) handler).gtocore$setIdentity(te);
                this.scheduleUpdate();
            }
        }
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        handler.onUnmount(null);
        ((IStorageBusInventory) handler).gtocore$setIdentity(null);
    }

    @Mixin(PartSpecialStorageBus.StorageBusInventory.class)
    public static class StorageBusInventoryMixin extends MEInventoryHandler implements IStorageBusInventory {

        @Unique
        private Object gtocore$identity;

        public StorageBusInventoryMixin(MEStorage inventory) {
            super(inventory);
        }

        @Override
        public void gtocore$setIdentity(Object identity) {
            this.gtocore$identity = identity;
        }

        @Override
        public Object getResourceIdentity() {
            var identity = super.getResourceIdentity();
            return identity != null ? identity : this.gtocore$identity;
        }
    }
}
