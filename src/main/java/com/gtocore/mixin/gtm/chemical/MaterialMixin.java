package com.gtocore.mixin.gtm.chemical;

import com.gtocore.api.data.material.MaterialType;
import com.gtocore.client.renderer.item.MaterialsColorMap;

import com.gtolib.api.data.chemical.material.GTOMaterial;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.IntSupplier;

@Mixin(Material.class)
public abstract class MaterialMixin implements GTOMaterial {

    @Unique
    private volatile MaterialType gtocore$materialType;

    @Override
    public MaterialType gtocore$getBaseType() {
        MaterialType materialType = gtocore$materialType;
        if (materialType == null) {
            materialType = MaterialType.classify((Material) (Object) this);
            gtocore$materialType = materialType;
        }
        return materialType;
    }

    @Override
    public void gtocore$setBaseType(MaterialType materialType) {
        gtocore$materialType = materialType;
    }

    @Inject(method = { "getMaterialRGB()I", "getMaterialSecondaryRGB" }, at = @At("HEAD"), remap = false, cancellable = true)
    private void getMaterialRGB(CallbackInfoReturnable<Integer> cir) {
        if (GTCEu.isClientSide()) {
            IntSupplier supplier = MaterialsColorMap.MaterialColors.get(this);
            if (supplier == null) return;
            cir.setReturnValue(supplier.getAsInt() & 0xFFFFFF);
        }
    }

    @Inject(method = { "getMaterialARGB(I)I", "getMaterialSecondaryARGB" }, at = @At("HEAD"), remap = false, cancellable = true)
    private void getMaterialARGB(CallbackInfoReturnable<Integer> cir) {
        if (GTCEu.isClientSide()) {
            IntSupplier supplier = MaterialsColorMap.MaterialColors.get(this);
            if (supplier == null) return;
            if ((supplier.getAsInt() & 0xFF000000) == 0) {
                cir.setReturnValue(supplier.getAsInt() | 0xFF000000);
            } else {
                cir.setReturnValue(supplier.getAsInt());
            }
        }
    }
}
