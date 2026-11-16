package com.gtocore.common.data.material;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VH;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTOMaterials.*;
import static com.gtocore.data.recipe.processing.CompositeMaterialsProcessing.registerFiberExtrusionTemperature;

public class MaterialPostInit {

    public static void init() {
        // Only the block model is overridden; other forms retain their original icon set.
        for (Material material : new Material[] {
                Aluminium, Titanium, Bismuth, Osmium, NickelZincFerrite,
                DarkSteel, Livingsteel, StructuralSteel45, StructuralSteelQ690,
                Stellite, PulsatingAlloy, OriginalBronze,
                AluminumAlloy2090, AluminumAlloy5A06, AluminumAlloy7050, AluminumAlloy8090, Soularium
        }) {
            material.setMaterialIconSet(new MaterialIconSet("layered_" + material.getName(), material.getMaterialIconSet()));
        }

        registerFiberExtrusionTemperature(Quartzite, 3000);
        registerFiberExtrusionTemperature(Alumina, 3000);
        registerFiberExtrusionTemperature(Titanium, 3800);
        registerFiberExtrusionTemperature(SiliconCarbide, 5000);
        registerFiberExtrusionTemperature(Tungsten, 5000);
        registerFiberExtrusionTemperature(StainlessSteel155Ph, 3000);
        registerFiberExtrusionTemperature(Aluminium, 1600);
        registerFiberExtrusionTemperature(Tantalum, 3300);
        registerFiberExtrusionTemperature(Copper, 1600);
        registerFiberExtrusionTemperature(Kevlar, 800);
        registerFiberExtrusionTemperature(TungstenCarbide, 4500);
        registerFiberExtrusionTemperature(TitaniumCarbide, 4000);

        MATERIAL_VOLTAGE = Map.of(
                GTMaterials.Gold, VA[LV],
                GTMaterials.Copper, VH[LV],
                GTMaterials.Cupronickel, VH[LV],
                GTMaterials.AnnealedCopper, VH[LV]);
    }
}
