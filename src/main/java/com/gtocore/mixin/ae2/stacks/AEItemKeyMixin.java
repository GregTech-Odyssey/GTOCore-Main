package com.gtocore.mixin.ae2.stacks;

import com.gtocore.integration.ae.hooks.IAEKeyExtension;

import com.gtolib.api.item.IItem;
import com.gtolib.api.recipe.lookup.IIngredientConvertible;
import com.gtolib.api.recipe.lookup.MapIngredient;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import com.gto.recipesearch.IntLongMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.gtocore.integration.ae.hooks.IAEKeyExtension.get$Material;

@Mixin(AEItemKey.class)
public abstract class AEItemKeyMixin implements IIngredientConvertible, IAEKeyExtension {

    @Shadow(remap = false)
    public abstract ItemStack getReadOnlyStack();

    @Shadow(remap = false)
    @Final
    public Item item;

    @Unique
    private int[] gtocore$is;

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public ResourceLocation getId() {
        return ((IItem) item).gtolib$getIdLocation();
    }

    @Override
    public void gtolib$convert(long amount, IntLongMap map) {
        if (gtocore$is == null) {
            var m = new IntLongMap();
            MapIngredient.ITEM_CONVERTER.convert(getReadOnlyStack(), 1, m);
            gtocore$is = m.toIntArray();
        }
        for (var i : gtocore$is) {
            map.add(i, amount);
        }
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
