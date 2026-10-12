package com.gtocore.data.recipe.classified;

/** Small fuel batches. Atom counts are in 0.1 mol, except chloride in whole mol; see docs/fuel-cell-fuels.md. */
record FuelCellFuel(String fluidId, long amount, long carbon, long hydrogen, long oxygen, long nitrogen, long sulfur, long chlorine, long helium) {

    static final FuelCellFuel[] VALUES = {
            new FuelCellFuel("gtceu:naphtha", 1000, 48, 89, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:sulfuric_light_fuel", 1000, 70, 107, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:methanol", 1000, 10, 40, 10, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:ethanol", 1000, 20, 60, 10, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:octane", 1000, 80, 180, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:bio_diesel", 1000, 200, 400, 20, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:light_fuel", 1000, 70, 109, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:diesel", 1000, 69, 104, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:cetane_boosted_diesel", 1000, 72, 118, 1, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:gasoline", 1000, 44, 80, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:high_octane_gasoline", 1000, 37, 68, 1, 1, 0, 0, 0),
            new FuelCellFuel("gtceu:toluene", 1000, 70, 80, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:natural_gas", 1000, 14, 46, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:wood_gas", 1000, 10, 6, 13, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:sulfuric_gas", 1000, 14, 46, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:sulfuric_naphtha", 1000, 48, 87, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:coal_gas", 1000, 31, 42, 5, 3, 0, 0, 0),
            new FuelCellFuel("gtceu:methane", 1000, 10, 40, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:ethylene", 1000, 20, 40, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:refinery_gas", 1000, 14, 47, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:ethane", 1000, 20, 60, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:propene", 1000, 30, 60, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:butadiene", 1000, 40, 60, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:propane", 1000, 30, 80, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:butene", 1000, 40, 80, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:phenol", 1000, 60, 60, 10, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:benzene", 1000, 60, 60, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:butane", 1000, 40, 100, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:lpg", 1000, 33, 87, 0, 0, 0, 0, 0),
            new FuelCellFuel("gtceu:nitrobenzene", 1000, 60, 50, 20, 10, 0, 0, 0),
            new FuelCellFuel("gtceu:rocket_fuel", 2000, 4, 35, 15, 13, 0, 1, 0),
            new FuelCellFuel("gtocore:rocket_fuel_rp_1", 1000, 240, 480, 20, 0, 0, 0, 0),
            new FuelCellFuel("gtocore:dense_hydrazine_fuel_mixture", 1000, 10, 80, 10, 20, 0, 0, 0),
            new FuelCellFuel("gtocore:rocket_fuel_cn3h7o3", 1000, 10, 70, 30, 30, 0, 0, 0),
            new FuelCellFuel("gtocore:rocket_fuel_h8n4c2o4", 1000, 20, 80, 40, 40, 0, 0, 0),
            new FuelCellFuel("gtocore:explosivehydrazine", 1000, 56, 104, 30, 22, 4, 0, 0),
            new FuelCellFuel("ad_astra:cryo_fuel", 1000, 129, 222, 31, 23, 4, 0, 0),
            new FuelCellFuel("gtocore:stellar_energy_rocket_fuel", 1000, 229, 362, 110, 65, 2, 0, 0),
            new FuelCellFuel("gtceu:dimethylhydrazine", 1000, 20, 80, 0, 20, 0, 0, 0)
    };

    static FuelCellFuel find(String fluidId) {
        for (var fuel : VALUES) {
            if (fuel.fluidId.equals(fluidId)) return fuel;
        }
        throw new IllegalArgumentException("Missing fuel composition: " + fluidId);
    }

    long protons() {
        // Discard the fractional proton equivalent instead of scaling up the entire batch.
        return (hydrogen + 2 * water()) / 10;
    }

    long water() {
        // Signed water input, in 0.1 mol; oxygen-rich fuels may instead release water.
        return 2 * carbon + 2 * sulfur - oxygen;
    }
}
