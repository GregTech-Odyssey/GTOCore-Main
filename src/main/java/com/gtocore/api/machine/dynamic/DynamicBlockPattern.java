package com.gtocore.api.machine.dynamic;

import com.gregtechceu.gtceu.api.pattern.BlockPattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DynamicBlockPattern extends BlockPattern {

    @Getter
    private final Map<String, DynamicPartDefinition> dynamicParts;

    public DynamicBlockPattern(BlockPattern pattern, Map<String, DynamicPartDefinition> dynamicParts) {
        super(pattern.blockMatches, pattern.structureDir, pattern.aisleRepetitions, pattern.centerOffset, pattern.fingerLength, pattern.thumbLength, pattern.palmLength);
        this.dynamicParts = Collections.unmodifiableMap(new LinkedHashMap<>(dynamicParts));
        isSubPattern = pattern.isSubPattern;
        predicates = pattern.predicates;
        condition = pattern.condition;
        info = pattern.info;
    }

    public DynamicPartDefinition getDynamicPart(String name) {
        DynamicPartDefinition part = dynamicParts.get(name);
        if (part == null) throw new IllegalArgumentException("Unknown dynamic part: " + name);
        return part;
    }

    public BlockPos getSourcePos(DynamicPartDefinition part, int x, int y, int z, BlockPos origin, Direction frontFacing, Direction upwardsFacing, boolean flipped) {
        int localX = part.getColumnOffset() + z - centerOffset[0];
        int localY = part.getRowOffset() + y - centerOffset[1];
        int localZ = part.getAisleOffset() + x - centerOffset[4];
        return setActualRelativeOffset(localX, localY, localZ, frontFacing, frontFacing.ordinal(), upwardsFacing, flipped).offset(origin);
    }
}
