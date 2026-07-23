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
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:stem_cells"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:biological_cells"), ResearchPoints.of(ResearchTag.BIOLOGY, 6L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_1m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_4m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_16m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 3L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_64m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_256m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 5L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:infinite_cell_component"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 12L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:bifidobacterium_breve_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cupriavidus_necator_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:hyperthermophilic_archaeon_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:eschericia_coli_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:clostridium_pasteurianum_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:shewanella_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:streptococcus_pyogenes_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:brevibacterium_flavium_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.freeze();
    }
}
