package com.gtocore.common.data;

import com.gtocore.common.effect.GTOMysteriousBoostEffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.gtolib.GTOCore.MOD_ID;

public final class GTOEffects {

    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);

    public static final RegistryObject<GTOMysteriousBoostEffect> MYSTERIOUS_BOOST = MOB_EFFECTS.register("mysterious_boost",
            () -> new GTOMysteriousBoostEffect(MobEffectCategory.BENEFICIAL, 0xFF00FF));

    public static void init(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
