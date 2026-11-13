package com.gtocore.config;

public enum BlankVirtualProviders {

    BOTH(true, true),
    ITEM(true, false),
    FLUID(false, true),
    NONE(false, false);

    public final boolean item;
    public final boolean fluid;

    BlankVirtualProviders(boolean item, boolean fluid) {
        this.item = item;
        this.fluid = fluid;
    }
}
