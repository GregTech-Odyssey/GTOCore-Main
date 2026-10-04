package com.gtocore.common.block;

import com.gregtechceu.gtceu.api.block.IFilterType;

import org.jetbrains.annotations.NotNull;

public final class CleanroomFilterType implements IFilterType {

    public static final CleanroomFilterType FILTER_CASING_LAW = new CleanroomFilterType("law_filter_casing", 3);
    private final String name;
    private final int cleanroomTier;

    private CleanroomFilterType(String name, int cleanroomTier) {
        this.name = name;
        this.cleanroomTier = cleanroomTier;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return name;
    }

    @NotNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int getCleanroomTier() {
        return this.cleanroomTier;
    }
}
