package com.gtocore.mixin.ae2.stacks;

import com.gtocore.integration.ae.hooks.IAEKeyExtension;

import com.gtolib.api.fluid.IFluid;
import com.gtolib.api.recipe.lookup.IIngredientConvertible;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;

import com.gto.recipesearch.IntLongMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.gtocore.integration.ae.hooks.IAEKeyExtension.get$Material;

@Mixin(AEFluidKey.class)
public abstract class AEFluidKeyMixin implements IIngredientConvertible, IAEKeyExtension {

    @Shadow(remap = false)
    @Final
    private Fluid fluid;

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public ResourceLocation getId() {
        return ((IFluid) fluid).gtolib$getIdLocation();
    }

    @Override
    public void gtolib$convert(long amount, IntLongMap map) {
        map.add(((IFluid) fluid).gtolib$getMapFluid(), amount);
    }

    @Unique
    private Material gtocore$material;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void onConstructed(CallbackInfo ci) {
        gtocore$material = get$Material((AEKey) (Object) this);
    }

    @Override
    public Material getGtocore$material() {
        return gtocore$material;
    }
}
