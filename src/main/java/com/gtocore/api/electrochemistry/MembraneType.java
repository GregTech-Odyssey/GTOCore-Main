package com.gtocore.api.electrochemistry;

public enum MembraneType {

    PROTON,
    CATION,
    ANION;

    public static MembraneType forIon(IonDefinition ion) {
        return ion == Ions.PROTON ? PROTON : ion.getCharge() > 0 ? CATION : ANION;
    }

    public boolean permits(IonDefinition ion) {
        return this == forIon(ion);
    }

    public String translationKey() {
        return switch (this) {
            case PROTON -> "gtocore.membrane_type.proton";
            case CATION -> "gtocore.membrane_type.cation";
            case ANION -> "gtocore.membrane_type.anion";
        };
    }
}
