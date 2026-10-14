package com.gtocore.api.electrochemistry;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.common.data.GTMaterials;

public final class IonCombinations {

    public static final GTRegistry.RL<IonCombinationDefinition> REGISTRY = new GTRegistry.RL<>(GTOCore.id("ion_combinations"), false);

    static {
        REGISTRY.unfreeze();
        register("water", Ions.PROTON, 1, Ions.HYDROXIDE, 1, GTMaterials.Water);
        register("hydrochloric_acid", Ions.PROTON, 1, Ions.CHLORIDE, 1, GTMaterials.HydrochloricAcid);
        register("salt", Ions.SODIUM, 1, Ions.CHLORIDE, 1, GTMaterials.Salt);
        register("sodium_hydroxide", Ions.SODIUM, 1, Ions.HYDROXIDE, 1, GTMaterials.SodiumHydroxide);
        REGISTRY.freeze();
    }

    private IonCombinations() {}

    public static void init() {}

    private static void register(String name, IonDefinition positive, long positiveAmount, IonDefinition negative, long negativeAmount, Material product) {
        REGISTRY.register(GTOCore.id(name), new IonCombinationDefinition(new IonAmount(positive, positiveAmount), new IonAmount(negative, negativeAmount), product, 1));
    }
}
