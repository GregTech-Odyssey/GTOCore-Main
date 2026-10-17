package com.gtocore.integration.emi.electrochemistry;

import com.gtocore.api.electrochemistry.Ions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;

public final class IonEmiStackSerializer implements EmiStackSerializer<IonEmiStack> {

    @Override
    public String getType() {
        return "gtocore_ion";
    }

    @Override
    public EmiStack create(ResourceLocation id, CompoundTag nbt, long amount) {
        var ion = Ions.REGISTRY_KEY.get(id);
        return ion == null ? EmiStack.EMPTY : new IonEmiStack(ion).setAmount(amount);
    }
}
