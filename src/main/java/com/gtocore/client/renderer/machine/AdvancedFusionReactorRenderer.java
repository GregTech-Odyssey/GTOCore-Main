package com.gtocore.client.renderer.machine;

import com.gtocore.common.machine.multiblock.electric.AdvancedFusionReactorMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.renderpro.EffectClock;
import com.gregtechceu.gtceu.renderpro.EffectPalette;
import com.gregtechceu.gtceu.renderpro.ParticleRing;
import com.gregtechceu.gtceu.renderpro.RenderProFrame;
import com.gregtechceu.gtceu.renderpro.RingShape;
import com.gregtechceu.gtceu.renderpro.RingStyle;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

public final class AdvancedFusionReactorRenderer extends WorkableCasingMachineRenderer {

    private static final float RISE = 30;
    private static final float FADEOUT = 60;
    private static final int FALLBACK_COLOR = 0xFFA040;
    private static final int MODULES = 4;
    private static final float RING_RADIUS = 8;
    private static final float TUBE_RADIUS = 1.4F;
    private static final int BASE_COUNT = 3600;
    private static final int MODULE_COUNT = 900;
    private static final ParticleRing[] RINGS = new ParticleRing[MODULES + 1];
    private static final RingStyle STYLE = new RingStyle(2.4F, 150, 2.4F, 55, 0.6F, 110);
    private static final float BACK = 19;
    private static final float LIFT = 6;
    private static final float HELIX_RADIUS = 4.6F;
    private static final float HELIX_TUBE = 0.45F;
    private static final int HELIX_COUNT = 3000;
    private static final float HELIX_BOTTOM = 8;
    private static final RingShape HELIX = RingShape.helix(3, 18.3F, 3, 0, 0.1F, 0.04F);
    private static final RingStyle HELIX_STYLE = new RingStyle(2.4F, 190, 0.8F, 70, 0.3F, 150);
    private static ParticleRing helixRing;

    public AdvancedFusionReactorRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        super(baseCasing, workableModel);
    }

    @OnlyIn(Dist.CLIENT)
    private static ParticleRing ring(int modules) {
        int index = Mth.clamp(modules, 0, MODULES);
        var ring = RINGS[index];
        if (ring == null) {
            float boost = 1 + 0.2F * index;
            ring = new ParticleRing(RING_RADIUS, TUBE_RADIUS, BASE_COUNT + MODULE_COUNT * index, 0.18F * boost, 0.42F * boost, 0x6B1A0000L + index);
            RINGS[index] = ring;
        }
        return ring;
    }

    @OnlyIn(Dist.CLIENT)
    private static ParticleRing helixRing() {
        if (helixRing == null) helixRing = new ParticleRing(HELIX_RADIUS, HELIX_TUBE, HELIX_COUNT, 0.35F, 0.7F, 0x6B1A00C0L);
        return helixRing;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (!(blockEntity instanceof MetaMachineBlockEntity machineBlockEntity) ||
                !(machineBlockEntity.getMetaMachine() instanceof AdvancedFusionReactorMachine machine)) {
            return;
        }
        var clock = EffectClock.of(machine, RISE, FADEOUT);
        clock.update(machine.recipeLogic.isWorking(), machine.getOffsetTimer() + partialTicks);
        if (!clock.visible() || !RenderProFrame.collecting(blockEntity.getLevel())) return;
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        var back = RelativeDirection.BACK.getRelative(front, upwards, flipped);
        var up = RelativeDirection.UP.getRelative(front, upwards, flipped);
        var axis = up.getAxis();
        var pos = machine.getPos();
        double x = pos.getX() + 0.5 + back.getStepX() * BACK + up.getStepX() * LIFT;
        double y = pos.getY() + 0.5 + back.getStepY() * BACK + up.getStepY() * LIFT;
        double z = pos.getZ() + 0.5 + back.getStepZ() * BACK + up.getStepZ() * LIFT;
        int base = clock.color(machine.getColor(), FALLBACK_COLOR);
        var palette = EffectPalette.shades(base);
        double time = clock.time();
        float intensity = clock.intensity();
        int modules = machine.getHighEnergyModules();
        RenderProFrame.ring(ring(modules), STYLE, x, y, z, axis, time, intensity, base, palette);
        if (modules < MODULES) return;
        RenderProFrame.ring(helixRing(), HELIX_STYLE, HELIX, x + up.getStepX() * HELIX_BOTTOM, y + up.getStepY() * HELIX_BOTTOM,
                z + up.getStepZ() * HELIX_BOTTOM, axis, time, intensity, base, palette);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getViewDistance() {
        return 96;
    }
}
