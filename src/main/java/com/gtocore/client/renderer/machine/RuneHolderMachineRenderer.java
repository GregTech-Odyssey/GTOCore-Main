package com.gtocore.client.renderer.machine;

import com.gtocore.common.machine.magic.rune.CentralRuneHolderMachine;
import com.gtocore.common.machine.magic.rune.RuneHolderMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.IRenderBoundingBox;
import com.gregtechceu.gtceu.client.renderer.machine.MachineRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.MythicBotanyItems;

/**
 * 符文支托的机器渲染器：静态模型之外，把存放的符文悬浮在支托上方，仪式进行时沿抛物线漂向大师符文支托。
 */
public class RuneHolderMachineRenderer extends MachineRenderer implements IRenderBoundingBox {

    // 渲染仅在客户端渲染线程顺序执行，复用临时向量/四元数避免每帧分配
    private static final Vector3f UP_AXIS = new Vector3f(0, 1, 0);
    private static final Vector3f SCRATCH_AXIS = new Vector3f();
    private static final Quaternionf SCRATCH_QUAT = new Quaternionf();

    public RuneHolderMachineRenderer(ResourceLocation model) {
        super(model);
    }

    @Override
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    /** 符文漂浮出方块范围时也要继续渲染。 */
    @Override
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        var box = new AABB(blockEntity.getBlockPos()).inflate(1);
        Level level = blockEntity.getLevel();
        if (level != null && blockEntity instanceof MetaMachineBlockEntity holder && holder.getMetaMachine() instanceof RuneHolderMachine machine) {
            BlockPos target = machine.getFloatTarget();
            if (target != null && isRunningRitual(level, target)) {
                BlockPos current = holder.getBlockPos();
                box = box.expandTowards(target.getX() - current.getX(), 0, target.getZ() - current.getZ());
            }
        }
        return box;
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack pose, MultiBufferSource buffer,
                       int light, int overlay) {
        if (!(blockEntity instanceof MetaMachineBlockEntity holder) || !(holder.getMetaMachine() instanceof RuneHolderMachine machine)) {
            return;
        }
        Level level = holder.getLevel();
        ItemStack stack = machine.getRune();
        if (stack.isEmpty() || level == null) {
            return;
        }
        BlockPos current = holder.getBlockPos();
        pose.pushPose();
        pose.translate(0.5, 0.25, 0.5);

        BlockPos target = machine.getFloatTarget();
        double progress = machine.getFloatProgress();
        if (target != null && progress > 0 && !current.equals(target) && isRunningRitual(level, target)) {
            float dx = target.getX() - current.getX();
            float dz = target.getZ() - current.getZ();
            pose.translate(dx * progress, Math.sin(progress * Math.PI), dz * progress);
            SCRATCH_AXIS.set(dx, 0, dz).cross(UP_AXIS).normalize();
            SCRATCH_QUAT.fromAxisAngleRad(SCRATCH_AXIS, (float) (progress * Math.PI));
            pose.mulPose(SCRATCH_QUAT);
        }

        double ticks = ClientTickHandler.total() + (7 * current.getX() + 31 * current.getZ() +
                (current.getZ() == 0 ? 87 : Math.floorMod(current.getX(), current.getZ())));
        double rotation = (BuiltInRegistries.ITEM.getId(stack.getItem()) % 2 == 0 ? 0.04 : -0.04) * ticks;
        pose.mulPose(Axis.YP.rotationDegrees(31 * current.getX() + 7 * current.getZ()));
        pose.mulPose(Axis.XP.rotationDegrees((float) (10 * Math.sin(rotation))));
        pose.mulPose(Axis.ZP.rotationDegrees((float) (10 * Math.cos(rotation))));
        pose.mulPose(Axis.XP.rotationDegrees(90));
        float scale = stack.is(MythicBotanyItems.MB_FIMBULTYR_TABLET.get()) ? 0.9F : 0.7F;
        pose.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
                OverlayTexture.NO_OVERLAY, pose, buffer, level, 0);
        pose.popPose();
    }

    /** 目标位置仍是正在举行仪式的大师符文支托时才让符文漂浮过去，否则停在原位。 */
    private static boolean isRunningRitual(Level level, BlockPos target) {
        return MetaMachine.getMachine(level, target) instanceof CentralRuneHolderMachine central && central.isRunning();
    }
}
