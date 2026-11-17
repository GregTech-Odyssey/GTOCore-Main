package com.gtocore.mixin.flerovium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Flerovium 的视锥把任何盒子都当成区块段大小的立方体只测中心，比区块段大的方块实体渲染盒（More Red 连线、大型机器特效）会在中心出视野时被误剔。
 * 超出区块段大小的盒子按真实尺寸逐面补测，区块段本身仍只走原来的快速判断。
 */
@Pseudo
@Mixin(targets = "com.moepus.flerovium.functions.Chunk.FastSimpleFrustum", remap = false)
public class FastSimpleFrustumMixin {

    @Unique
    private static final float gtocore$SECTION_SIZE = 18.25F;

    @Shadow
    private float nxX;
    @Shadow
    private float nxY;
    @Shadow
    private float nxZ;
    @Shadow
    private float negNxW;
    @Shadow
    private float pxX;
    @Shadow
    private float pxY;
    @Shadow
    private float pxZ;
    @Shadow
    private float negPxW;
    @Shadow
    private float nyX;
    @Shadow
    private float nyY;
    @Shadow
    private float nyZ;
    @Shadow
    private float negNyW;
    @Shadow
    private float pyX;
    @Shadow
    private float pyY;
    @Shadow
    private float pyZ;
    @Shadow
    private float negPyW;
    @Shadow
    private float nzX;
    @Shadow
    private float nzY;
    @Shadow
    private float nzZ;
    @Shadow
    private float negNzW;

    @ModifyExpressionValue(method = "testAab", at = @At(value = "INVOKE", target = "Lcom/moepus/flerovium/functions/Chunk/FastSimpleFrustum;testCubeQuick(FFF)Z"))
    private boolean gtocore$testLargeBox(boolean original,
                                         @Local(ordinal = 0, argsOnly = true) float minX, @Local(ordinal = 1, argsOnly = true) float minY, @Local(ordinal = 2, argsOnly = true) float minZ,
                                         @Local(ordinal = 3, argsOnly = true) float maxX, @Local(ordinal = 4, argsOnly = true) float maxY, @Local(ordinal = 5, argsOnly = true) float maxZ) {
        if (original) return true;
        float ex = maxX - minX - gtocore$SECTION_SIZE;
        float ey = maxY - minY - gtocore$SECTION_SIZE;
        float ez = maxZ - minZ - gtocore$SECTION_SIZE;
        if (ex <= 0 && ey <= 0 && ez <= 0) return false;
        ex = Math.max(ex, 0);
        ey = Math.max(ey, 0);
        ez = Math.max(ez, 0);
        float sx = minX + maxX;
        float sy = minY + maxY;
        float sz = minZ + maxZ;
        return gtocore$plane(nxX, nxY, nxZ, negNxW, sx, sy, sz, ex, ey, ez) &&
                gtocore$plane(pxX, pxY, pxZ, negPxW, sx, sy, sz, ex, ey, ez) &&
                gtocore$plane(nyX, nyY, nyZ, negNyW, sx, sy, sz, ex, ey, ez) &&
                gtocore$plane(pyX, pyY, pyZ, negPyW, sx, sy, sz, ex, ey, ez) &&
                gtocore$plane(nzX, nzY, nzZ, negNzW, sx, sy, sz, ex, ey, ez);
    }

    @Unique
    private static boolean gtocore$plane(float x, float y, float z, float negW, float sx, float sy, float sz, float ex, float ey, float ez) {
        return x * sx + y * sy + z * sz + Math.abs(x) * ex + Math.abs(y) * ey + Math.abs(z) * ez >= negW;
    }
}
