package com.gtocore.api.machine.dynamic;

import com.gregtechceu.gtceu.api.pattern.BlockPattern;

import net.minecraft.core.Direction;

import lombok.Getter;
import org.joml.Matrix4f;

@Getter
public final class DynamicPartDefinition {

    private final String name;
    private final String[][] structure;
    private final int aisleOffset;
    private final int rowOffset;
    private final int columnOffset;
    private final float pivotX;
    private final float pivotY;
    private final float pivotZ;
    private final float modelOffsetX;
    private final float modelOffsetY;
    private final float modelOffsetZ;
    private final DynamicMotion motion;

    DynamicPartDefinition(String name, String[][] structure, int aisleOffset, int rowOffset, int columnOffset, float pivotX, float pivotY, float pivotZ, DynamicMotion motion) {
        this(name, structure, aisleOffset, rowOffset, columnOffset, pivotX, pivotY, pivotZ, 0, 0, 0, motion);
    }

    private DynamicPartDefinition(String name, String[][] structure, int aisleOffset, int rowOffset, int columnOffset, float pivotX, float pivotY, float pivotZ, float modelOffsetX, float modelOffsetY, float modelOffsetZ, DynamicMotion motion) {
        this.name = name;
        this.structure = structure;
        this.aisleOffset = aisleOffset;
        this.rowOffset = rowOffset;
        this.columnOffset = columnOffset;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
        this.modelOffsetX = modelOffsetX;
        this.modelOffsetY = modelOffsetY;
        this.modelOffsetZ = modelOffsetZ;
        this.motion = motion;
    }

    public DynamicPartDefinition bind(BlockPattern pattern) {
        float originX = pattern.centerOffset[4] - aisleOffset - structure.length / 2F;
        float originY = rowOffset - pattern.centerOffset[1] + structure[0].length / 2F;
        float originZ = columnOffset - pattern.centerOffset[0] + structure[0][0].length() / 2F;
        return new DynamicPartDefinition(name, structure, aisleOffset, rowOffset, columnOffset, pivotX, pivotY, pivotZ, originX - pivotX, originY - pivotY, originZ - pivotZ, motion);
    }

    public Matrix4f transform(Direction facing, float value) {
        float rotation = switch (facing) {
            case NORTH -> 270;
            case SOUTH -> 90;
            case EAST -> 180;
            default -> 0;
        };
        Matrix4f transform = new Matrix4f()
                .translate(.5F, .5F, .5F)
                .rotateY((float) Math.toRadians(rotation))
                .translate(pivotX - .5F, pivotY - .5F, pivotZ - .5F);
        return motion.apply(transform, value).translate(modelOffsetX, modelOffsetY, modelOffsetZ);
    }
}
