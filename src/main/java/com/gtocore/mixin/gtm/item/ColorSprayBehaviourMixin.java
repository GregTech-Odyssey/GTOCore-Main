package com.gtocore.mixin.gtm.item;

import com.gtocore.common.block.ColorBlockMap;
import com.gtocore.common.data.GTOBlocks;

import com.gregtechceu.gtceu.common.item.ColorSprayBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * GTM 将 tryPaintSpecialBlock 改为 static 并增加 {@code DyeColor color} 参数后，
 * mixin 必须同为 static 才能注入。
 */
@Mixin(ColorSprayBehaviour.class)
public class ColorSprayBehaviourMixin {

    @Shadow(remap = false)
    private static boolean recolorBlockNoState(Map<DyeColor, Block> map, DyeColor color, Level level, BlockPos pos,
                                               Block defaultBlock) {
        return false;
    }

    @Inject(method = "tryPaintSpecialBlock", at = @At("HEAD"), remap = false, cancellable = true)
    private static void gto$tryPaintSpecialBlock(Level world, BlockPos pos, Block block, @Nullable DyeColor color,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (color == null) {
            return;
        }
        if (ColorBlockMap.ABS_MAP.containsValue(block)) {
            if (recolorBlockNoState(ColorBlockMap.ABS_MAP, color, world, pos, GTOBlocks.ABS_WHITE_CASING.get())) {
                cir.setReturnValue(true);
            }
        }
    }
}
