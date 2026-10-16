package com.gtocore.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.STEAM_CRACKING_RECIPES;

public final class PetrochemRecipes {

    public static void init() {
        crack(HeavyFuel, SeverelySteamCrackedHeavyFuel, LightlySteamCrackedHeavyFuel);
        crack(LightFuel, SeverelySteamCrackedLightFuel, LightlySteamCrackedLightFuel);
        crack(Naphtha, SeverelySteamCrackedNaphtha, LightlySteamCrackedNaphtha);
        crack(RefineryGas, SeverelySteamCrackedGas, LightlySteamCrackedGas);
    }

    private static void crack(Material... cracked) {
        STEAM_CRACKING_RECIPES.recipeBuilder("steam_crack_" + cracked[0].getName())
                .inputFluids(cracked[0], 1000)
                .outputFluids(cracked[1].getFluid(200), cracked[2].getFluid(200))
                .duration(200)
                .EUt(VA[LV])
                .save();
    }
}
