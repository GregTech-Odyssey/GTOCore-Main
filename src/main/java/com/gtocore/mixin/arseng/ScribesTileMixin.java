package com.gtocore.mixin.arseng;

import com.gregtechceu.gtceu.core.ILevel;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.MEStorageHost;
import appeng.api.storage.StorageAccess;
import appeng.api.storage.StorageTargetResolver;

import com.hollingsworth.arsnouveau.common.block.tile.ModdedTile;
import com.hollingsworth.arsnouveau.common.block.tile.ScribesTile;
import com.hollingsworth.arsnouveau.common.entity.EntityFlyingItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(value = ScribesTile.class, remap = false, priority = 0)
public abstract class ScribesTileMixin extends ModdedTile {

    @Shadow
    public List<ItemStack> consumedStacks;

    public ScribesTileMixin(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Shadow
    public abstract boolean canConsumeItemstack(ItemStack stack);

    /// Replaces Ars Energistics' ME extraction with GTOCore's version
    /// Code under GNU LGPLv3 License
    /// @see gripe._90.arseng.mixin.ScribesTileMixin
    @Inject(method = "takeNearby",
            at = @At("TAIL"))
    private void takeFromInterfaces(CallbackInfo ci) {
        if (level == null) {
            return;
        }

        var area = BlockPos.betweenClosed(
                worldPosition.north(6).east(6).below(2),
                worldPosition.south(6).west(6).above(2));

        StorageTargetResolver resolver = null;
        for (var pos : area) {
            var be = ILevel.getCachedBlockEntity(level, pos);
            if (be == null) continue;
            MEStorage storage;
            if (be instanceof MEStorageHost host) {
                storage = host.getAnyMEStorage();
            } else {
                if (resolver == null) resolver = new StorageTargetResolver();
                storage = gto$anyStorage(resolver, be);
            }
            if (storage != null) {
                gto$replaceArseng$extract(storage, pos);
                return;
            }
        }
    }

    @Unique
    @Nullable
    private static MEStorage gto$anyStorage(StorageTargetResolver resolver, BlockEntity be) {
        if (resolver.resolveAll(be, null, StorageAccess.FULL) == StorageTargetResolver.Tier.STORAGE) return (MEStorage) resolver.raw();
        for (var side : GTUtil.DIRECTIONS) {
            if (resolver.resolveAll(be, side, StorageAccess.FULL) == StorageTargetResolver.Tier.STORAGE) return (MEStorage) resolver.raw();
        }
        return null;
    }

    @Unique
    private void gto$replaceArseng$extract(MEStorage storage, BlockPos pos) {
        for (var stored : storage.getAvailableStacks()) {
            if (stored.getKey() instanceof AEItemKey item && canConsumeItemstack(item.wrapForDisplayOrFilter())) {
                var extracted = storage.extract(item, 1, Actionable.MODULATE, IActionSource.empty());
                var taken = item.toStack((int) extracted);
                consumedStacks.add(taken);

                var flyingItem = new EntityFlyingItem(level, pos, getBlockPos());
                flyingItem.setStack(taken);
                Objects.requireNonNull(level).addFreshEntity(flyingItem);
                updateBlock();
                return;
            }
        }
    }
}
