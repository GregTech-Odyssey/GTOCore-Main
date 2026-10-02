package com.gtocore.client.renderer

import net.minecraft.util.Mth

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import org.joml.Quaternionf
import org.joml.Vector3f

open class PosestackHelper {
    companion object {
        /** 修改传入的姿态栈；调用方负责在调用前后执行 pushPose/popPose。 */
        @JvmStatic
        fun spinningTransformPosestack(posestack: PoseStack, fx: Float, maxFx: Float, intensity: Int, accTicks: Long, partialTicks: Float) {
            val f1 = Mth.lerp(partialTicks, fx, maxFx)
            if (f1 > 0f) {
                var f2 = 5f / (f1 * f1 + 5f) - f1 * 0.04f
                f2 *= f2
                val axis = Axis.of(Vector3f(0f, Mth.SQRT_OF_TWO / 2f, Mth.SQRT_OF_TWO / 2f))
                posestack.mulPose(axis.rotationDegrees((accTicks.toFloat() + partialTicks) * intensity.toFloat()))
                posestack.scale(1f / f2, 1f, 1f)
                val f3 = -(accTicks.toFloat() + partialTicks) * intensity.toFloat()
                posestack.mulPose(axis.rotationDegrees(f3))
            }
        }

        @JvmStatic
        fun stereoTransformPosestack(poseStack: PoseStack, axisX: Float, axisY: Float, axisZ: Float, angle: Float) {
            poseStack.mulPose(Quaternionf().fromAxisAngleDeg(axisX, axisY, axisZ, angle))
        }
    }
}
