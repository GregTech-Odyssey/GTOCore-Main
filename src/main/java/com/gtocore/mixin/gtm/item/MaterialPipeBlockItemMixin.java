package com.gtocore.mixin.gtm.item;

import com.gtolib.api.data.chemical.material.GTOPropertyKeys;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.item.MaterialPipeBlockItem;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MaterialPipeBlockItem.class)
public abstract class MaterialPipeBlockItemMixin extends PipeBlockItem {

    @Shadow(remap = false)
    public abstract @NotNull MaterialPipeBlock<?, ?, ?> getBlock();

    @Unique
    private boolean gtolib$glow;

    protected MaterialPipeBlockItemMixin(PipeBlock<?, ?, ?> block, Properties properties) {
        super(block, properties);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void gtolib$init(MaterialPipeBlock<?, ?, ?> block, Properties properties, CallbackInfo ci) {
        gtolib$glow = GTOPropertyKeys.glow(block.material);
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), index = 2, argsOnly = true, remap = false)
    private static Properties init(Properties value, @Local(argsOnly = true) MaterialPipeBlock<?, ?, ?> block) {
        Rarity rarity = GTOPropertyKeys.rarity(block.material);
        if (rarity != null) value.rarity(rarity);
        return value;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack itemStack) {
        if (gtolib$glow) return true;
        return super.isFoil(itemStack);
    }
}
