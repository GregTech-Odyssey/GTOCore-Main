package com.gtocore.api.machine.dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiPredicate;

public interface IDynamicStructureMachine {

    @Nullable
    Level getDynamicLevel();

    BlockPos getDynamicOrigin();

    DynamicBlockPattern getDynamicPattern();

    Direction getFrontFacing();

    Direction getUpwardsFacing();

    boolean isFlipped();

    boolean isDynamicPartVisible(String partName);

    @Nullable
    BlockState getDynamicBlockState(String partName, char symbol);

    float getDynamicMotionValue(String partName, float partialTicks);

    default Map<String, DynamicPartDefinition> getDynamicParts() {
        return getDynamicPattern().getDynamicParts();
    }

    default DynamicPartDefinition getDynamicPart(String partName) {
        return getDynamicPattern().getDynamicPart(partName);
    }

    default String[][] getDynamicStructure(String partName) {
        return getDynamicPart(partName).getStructure();
    }

    default BlockPos getDynamicSourcePos(String partName, int x, int y, int z) {
        return getDynamicPattern().getSourcePos(getDynamicPart(partName), x, y, z, getDynamicOrigin(), getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    default Matrix4f getDynamicTransform(String partName, float partialTicks) {
        return getDynamicPart(partName).transform(getFrontFacing(), getDynamicMotionValue(partName, partialTicks));
    }

    default boolean visitDynamicBlocks(String partName, BiPredicate<Character, BlockPos> visitor) {
        String[][] structure = getDynamicStructure(partName);
        for (int x = 0; x < structure.length; x++) {
            String[] plane = structure[x];
            for (int y = 0; y < plane.length; y++) {
                String row = plane[y];
                for (int z = 0; z < row.length(); z++) {
                    char symbol = row.charAt(z);
                    if (symbol != ' ' && !visitor.test(symbol, getDynamicSourcePos(partName, x, y, z))) return false;
                }
            }
        }
        return true;
    }
}
