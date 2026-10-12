package com.gtocore.data.recipe.classified;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuelCellFuelTest {

    @Test
    void dimethylhydrazineOxidationUsesWaterAndReleasesProtons() {
        var fuel = FuelCellFuel.find("gtceu:dimethylhydrazine");
        // C2H8N2 + 4 H2O -> 2 CO2 + N2 + 16 H+ + 16 e-.
        assertEquals(1000, fuel.amount());
        assertEquals(40, fuel.water());
        assertEquals(16, fuel.protons());
        assertEquals(20, fuel.carbon());
        assertEquals(20, fuel.nitrogen());
    }

    @Test
    void everyFuelHasSmallBatchesPositiveChargeAndOnlyFractionalHydrogenLoss() {
        assertEquals(39, FuelCellFuel.VALUES.length);
        for (var fuel : FuelCellFuel.VALUES) {
            String id = fuel.fluidId();
            assertTrue(fuel.amount() > 0 && fuel.amount() <= 2000, id);
            assertTrue(fuel.protons() > fuel.chlorine(), id);
            assertTrue(fuel.protons() <= 140, id);
            long lostHydrogen = fuel.hydrogen() + 2 * fuel.water() - 10 * fuel.protons();
            assertTrue(lostHydrogen >= 0 && lostHydrogen < 10, id);
        }
    }

    @Test
    void screenshotFuelsNoLongerNeedMillionMillibucketBatches() {
        var gasoline = FuelCellFuel.find("gtceu:high_octane_gasoline");
        assertEquals(1000, gasoline.amount());
        assertEquals(21, gasoline.protons());
        assertEquals(37, gasoline.carbon());
        assertEquals(73, gasoline.water());
        var cryo = FuelCellFuel.find("ad_astra:cryo_fuel");
        assertEquals(1000, cryo.amount());
        assertEquals(69, cryo.protons());
        assertEquals(235, cryo.water());
    }

    @Test
    void gelledTolueneAndFireChargeContributeCarbonHydrogenAndSulfur() {
        var explosive = FuelCellFuel.find("gtocore:explosivehydrazine");
        // Base C1.5H7.25O3N2.25 plus four gelled toluene and two fire charges:
        // C = 1.5 + 2.8 + 4/3, H = 7.25 + 3.2, S = 4/9; round down to 0.1 mol.
        assertEquals(56, explosive.carbon());
        assertEquals(104, explosive.hydrogen());
        assertEquals(4, explosive.sulfur());
        assertEquals(28, explosive.protons());
        assertEquals(4, FuelCellFuel.find("ad_astra:cryo_fuel").sulfur());
        assertEquals(2, FuelCellFuel.find("gtocore:stellar_energy_rocket_fuel").sulfur());
    }

    @Test
    void oxygenRichRocketFuelReleasesWaterAndRetainsPositiveChargeWithChloride() {
        var fuel = FuelCellFuel.find("gtceu:rocket_fuel");
        assertEquals(2000, fuel.amount());
        assertEquals(1, fuel.chlorine());
        assertEquals(2, fuel.protons());
        assertEquals(-7, fuel.water());
        assertEquals(4, fuel.carbon());
        assertEquals(13, fuel.nitrogen());
    }
}
