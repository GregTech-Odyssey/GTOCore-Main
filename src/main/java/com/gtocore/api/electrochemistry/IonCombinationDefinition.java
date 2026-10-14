package com.gtocore.api.electrochemistry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

public record IonCombinationDefinition(IonAmount positive, IonAmount negative, Material product, long productAmount) {

    public IonCombinationDefinition {
        if (positive.ion().getCharge() <= 0 || negative.ion().getCharge() >= 0 ||
                Math.addExact(Math.multiplyExact(positive.amount(), positive.ion().getCharge()), Math.multiplyExact(negative.amount(), negative.ion().getCharge())) != 0 || productAmount <= 0) {
            throw new IllegalArgumentException("Ion combination must have positive quantities and neutral charge");
        }
    }
}
