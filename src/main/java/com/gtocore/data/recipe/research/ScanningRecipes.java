package com.gtocore.data.recipe.research;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.scanning.DataScanningManager;

import com.gtolib.utils.RegistriesUtils;

import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.GTValues.*;

public final class ScanningRecipes {

    public static void init() {
        /// 基元扫描
        DataScanningManager.registerDataScanning(Items.COMPASS, ResearchPoints.of(ResearchTag.MECHANICS, 1));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:catalyst_base"), ResearchPoints.of(ResearchTag.CATALYSIS, 1L));
        DataScanningManager.freeze();
    }
}
