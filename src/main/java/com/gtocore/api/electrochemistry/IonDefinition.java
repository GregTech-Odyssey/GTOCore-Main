package com.gtocore.api.electrochemistry;

import lombok.Getter;

@Getter
public class IonDefinition {

    private final String name;
    private final int charge;
    private final String symbol;
    private final int color;

    public IonDefinition(String name, int charge, String symbol) {
        this(name, charge, symbol, charge > 0 ? 0xEF8C8C : 0x78B9ED);
    }

    public IonDefinition(String name, int charge, String symbol, int color) {
        this.name = name;
        this.charge = charge;
        this.symbol = symbol;
        this.color = color;
    }

    public String translationKey() {
        return "ion." + name;
    }
}
