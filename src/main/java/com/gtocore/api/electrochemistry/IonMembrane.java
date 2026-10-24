package com.gtocore.api.electrochemistry;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.config.GTORules;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.tooltip.StatKind;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTOMaterials.*;

public record IonMembrane(Material material,
                          int durability, double efficiency, double expertEfficiency,
                          double durationMultiplier, double euMultiplier,
                          int fuelcellParallel,
                          MembraneType type) {

    private static final class Materials {

        private static final Reference2ObjectOpenHashMap<Material, IonMembrane> VALUES = new Reference2ObjectOpenHashMap<>(6);

        static {
            // iv
            register(Polytetrafluoroethylene,
                    4096, 1.05, 1.01,
                    1.0, 1.0,
                    MembraneType.CATION, 40);
            register(Graphene,
                    4096, 1.05, 1.01,
                    1.2, 0.9,
                    MembraneType.ANION);
            register(PolousPolyolefinSulfonate,
                    4096, 1.05, 1.01,
                    1.0, 1.0,
                    MembraneType.PROTON);
            register(PolyvinylideneFluoride,
                    1024, 1.35, 1.25,
                    1.1, 1.1,
                    MembraneType.CATION, 60);
            register(PolyacrylicAcid,
                    1024, 1.25, 1.15,
                    1.0, 0.9,
                    MembraneType.PROTON, 50);

            // luv
            register(PerfluorosulfonicAcidPolytetrafluoroethyleneCopolymer,
                    6144, 1.35, 1.25,
                    1.0, 1.0,
                    MembraneType.CATION, 90);
            register(SDBHICResin,
                    12288, 1.25, 1.15,
                    0.8, 0.9,
                    MembraneType.PROTON, 60);
            register(Polypyrrole,
                    10240, 1.25, 1.15,
                    0.9, 0.9,
                    MembraneType.ANION, 75);
            register(PolyvinylAlcohol,
                    10240, 1.35, 1.25,
                    1.0, 0.6,
                    MembraneType.PROTON);

            // zpm
            register(SulfonatedPolyetherEtherKetone,
                    20480, 1.95, 1.75,
                    0.7, 0.7,
                    MembraneType.PROTON, 65);
            register(Polyacrylamide,
                    15760, 1.75, 1.55,
                    0.9, 0.8,
                    MembraneType.CATION, 105);

            // uv
            register(NanocrackRegulatedSelfHumidifyingCompositeMaterial,
                    72000, 1.75, 1.55,
                    0.8, 0.6,
                    MembraneType.ANION, 105);
            register(CeOxPolyDopamineReinforcedPolytetrafluoroethylene,
                    102400, 2.25, 1.96,
                    0.6, 0.5,
                    MembraneType.PROTON, 105);
            register(Polysulfone,
                    6144, 1.75, 1.55,
                    0.5, 1.2,
                    MembraneType.CATION, 105);
            register(BPDCNiPPYHydrogel,
                    102400, 2.75, 2.25,
                    0.8, 0.8,
                    MembraneType.CATION, 75);

            // special
            register(PolysulfonePolyvinylpyrrolidoneBlend,
                    6144, 3.43, 2.56,
                    0.25, 0.5,
                    MembraneType.PROTON, 384);
            register(BoronOxygenFramework,
                    10240000, 0.75, 0.65,
                    1.3, 1.1,
                    MembraneType.ANION);
            register(MetallochaperoneBiomaterial,
                    10240, 0.75, 0.65,
                    0.25, 2.4,
                    MembraneType.CATION, 75);
            register(Polyimide,
                    307200, 1.25, 1.15,
                    1.0, 1.0,
                    MembraneType.CATION);
            register(Polybenzimidazole,
                    409600, 1.05, 0.95,
                    1.0, 1.0,
                    MembraneType.PROTON);
        }

        private static void register(Material material,
                                     int durability, double efficiency, double expertEfficiency,
                                     double durationMultiplier, double euMultiplier,
                                     MembraneType type) {
            register(material, durability, efficiency, expertEfficiency, durationMultiplier, euMultiplier, type, 50);
        }

        private static void register(Material material,
                                     int durability, double efficiency, double expertEfficiency,
                                     double durationMultiplier, double euMultiplier,
                                     MembraneType type, int fuelcellParallel) {
            VALUES.put(material, new IonMembrane(material, durability, efficiency, expertEfficiency, durationMultiplier, euMultiplier, fuelcellParallel, type));
        }
    }

    public static IonMembrane of(Material material) {
        return Materials.VALUES.get(material);
    }

    public static int maxDamage(Material material) {
        return of(material).durability;
    }

    public double fuelEfficiency() {
        return GTORules.FUEL_CELL_TIER.isExpert() ? expertEfficiency : efficiency;
    }

    public static IonMembrane of(ItemStack stack) {
        return stack.getItem() instanceof TagPrefixItem item && isMembrane(item) ? of(item.material) : null;
    }

    private static boolean isMembrane(TagPrefixItem item) {
        return item.tagPrefix == GTOTagPrefix.ION_MEMBRANE;
    }

    public static MembraneType typeOf(ItemStack stack) {
        var membrane = of(stack);
        return membrane == null ? null : membrane.type;
    }

    public static boolean permits(ItemStack stack, IonDefinition ion) {
        var type = typeOf(stack);
        return type != null && type.permits(ion);
    }

    public static void tooltip(Material material, List<Component> components) {
        var membrane = of(material);
        components.add(Tips.stat(StatKind.DURABILITY, FormattingUtil.formatNumbers(membrane.durability)));
        components.add(Tips.stat(Component.translatable("gtocore.membrane.recipe.duration_multiplier", membrane.durationMultiplier)));
        components.add(Tips.stat(Component.translatable("gtocore.membrane.recipe.energy_multiplier", membrane.euMultiplier)));
        components.add(Tips.stat(Component.translatable("gtocore.machine.fuelcell_efficiency", membrane.fuelEfficiency())));
        components.add(Tips.stat(Component.translatable("gtocore.machine.fuelcell_parallel", membrane.fuelcellParallel)));
    }

    public static Component name(Material material) {
        var materialName = material.getLocalizedName();
        return switch (of(material).type) {
            case PROTON -> Component.translatable("tagprefix.proton_membrane", materialName);
            case CATION -> Component.translatable("tagprefix.cation_membrane", materialName);
            case ANION -> Component.translatable("tagprefix.anion_membrane", materialName);
        };
    }
}
