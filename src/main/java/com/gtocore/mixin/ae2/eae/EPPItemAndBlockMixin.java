package com.gtocore.mixin.ae2.eae;

import com.gtocore.integration.ae.TieredInterfaces;
import com.gtocore.mixin.mc.MappedRegistryAccessor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.block.AEBaseEntityBlock;

import com.glodblock.github.extendedae.common.EAERegistryHandler;
import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EPPItemAndBlock.class)
public abstract class EPPItemAndBlockMixin {

    @Deprecated(since = "0.6.0", forRemoval = true)
    @WrapWithCondition(method = "init", at = @At(value = "INVOKE", target = "Lcom/glodblock/github/extendedae/common/EAERegistryHandler;item(Ljava/lang/String;Lnet/minecraft/world/item/Item;)V"), remap = false)
    private static boolean gtocore$skipOversizePart(EAERegistryHandler handler, String name, Item item) {
        return !TieredInterfaces.LEGACY_PART.equals(name);
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @WrapWithCondition(method = "init", at = @At(value = "INVOKE", target = "Lcom/glodblock/github/extendedae/common/EAERegistryHandler;block(Ljava/lang/String;Lappeng/block/AEBaseEntityBlock;Ljava/lang/Class;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;)V"), remap = false)
    private static boolean gtocore$skipOversizeBlock(EAERegistryHandler handler, String name, AEBaseEntityBlock<?> block, Class<?> type, BlockEntityType.BlockEntitySupplier<?> supplier) {
        return !TieredInterfaces.LEGACY_BLOCK.equals(name);
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private static void gtocore$dropOversizeHolders(EAERegistryHandler handler, CallbackInfo ci) {
        var blocks = ((MappedRegistryAccessor) BuiltInRegistries.BLOCK).gtocore$getUnregisteredIntrusiveHolders();
        if (blocks != null) blocks.remove(EPPItemAndBlock.OVERSIZE_INTERFACE);
        var items = ((MappedRegistryAccessor) BuiltInRegistries.ITEM).gtocore$getUnregisteredIntrusiveHolders();
        if (items != null) items.remove(EPPItemAndBlock.OVERSIZE_INTERFACE_PART);
    }
}
