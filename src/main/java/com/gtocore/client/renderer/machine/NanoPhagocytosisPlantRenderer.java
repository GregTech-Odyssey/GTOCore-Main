package com.gtocore.client.renderer.machine;

import com.gtocore.client.renderer.StructureVBO;
import com.gtocore.client.renderer.TextureUpdateRequester;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.machine.multiblock.electric.nano.NanoPhagocytosisPlantMachine;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.joml.Matrix4f;

public final class NanoPhagocytosisPlantRenderer extends WorkableCasingMachineRenderer {

    private RingBuffers activeRings;
    private RingBuffers inactiveRings;
    private final Matrix4f dynamicTransform = new Matrix4f();

    public NanoPhagocytosisPlantRenderer() {
        super(GTOCore.id("block/casings/naquadah_reinforced_plant_casing"), GTCEu.id("block/multiblock/fusion_reactor"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (!(blockEntity instanceof MetaMachineBlockEntity machineBlockEntity) ||
                !(machineBlockEntity.getMetaMachine() instanceof NanoPhagocytosisPlantMachine machine) ||
                !machine.isDynamicPartVisible(NanoPhagocytosisPlantMachine.INNER_VERTICAL_RING) ||
                blockEntity.getLevel() instanceof TrackedDummyWorld)
            return;
        RingBuffers rings = getRings(machine, machine.isActive());

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        rings.textureUpdateRequester().requestUpdate();
        renderRing(machine, NanoPhagocytosisPlantMachine.INNER_VERTICAL_RING, rings.innerVerticalRing(), partialTicks, poseStack);
        renderRing(machine, NanoPhagocytosisPlantMachine.OUTER_VERTICAL_RING, rings.outerVerticalRing(), partialTicks, poseStack);
        renderRing(machine, NanoPhagocytosisPlantMachine.HORIZONTAL_RING, rings.horizontalRing(), partialTicks, poseStack);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderRing(NanoPhagocytosisPlantMachine machine, String partName, VertexBuffer ring, float partialTicks, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.mulPoseMatrix(machine.getDynamicTransform(partName, partialTicks, dynamicTransform));
        ring.bind();
        ring.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        VertexBuffer.unbind();
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private RingBuffers getRings(NanoPhagocytosisPlantMachine machine, boolean active) {
        if (active) {
            if (activeRings == null) activeRings = createRings(machine, true);
            return activeRings;
        }
        if (inactiveRings == null) inactiveRings = createRings(machine, false);
        return inactiveRings;
    }

    private static RingBuffers createRings(NanoPhagocytosisPlantMachine machine, boolean active) {
        StructureVBO structure = createRingStructure(active);
        VertexBuffer innerVerticalRing = structure.assignStructure(machine.getDynamicStructure(NanoPhagocytosisPlantMachine.INNER_VERTICAL_RING)).build();
        VertexBuffer outerVerticalRing = createRingStructure(active).assignStructure(machine.getDynamicStructure(NanoPhagocytosisPlantMachine.OUTER_VERTICAL_RING)).build();
        VertexBuffer horizontalRing = createRingStructure(active).assignStructure(machine.getDynamicStructure(NanoPhagocytosisPlantMachine.HORIZONTAL_RING)).build();
        return new RingBuffers(innerVerticalRing, outerVerticalRing, horizontalRing, structure.getTextureUpdateRequestor());
    }

    private static StructureVBO createRingStructure(boolean active) {
        return new StructureVBO()
                .addMapping('B', state(GTBlocks.HIGH_POWER_CASING.get(), active))
                .addMapping('C', state(GTOBlocks.NAQUADAH_REINFORCED_PLANT_CASING.get(), active))
                .addMapping('E', state(GTOBlocks.HYPER_MECHANICAL_CASING.get(), active))
                .addMapping('F', state(GTOBlocks.NEUTRONIUM_STABLE_CASING.get(), active))
                .addMapping('G', state(GTBlocks.FUSION_COIL.get(), active))
                .addMapping('H', state(GTOBlocks.IRIDIUM_CASING.get(), active))
                .addMapping('I', state(GTOBlocks.FUSION_COIL_MK2.get(), active))
                .addMapping('J', state(GTOBlocks.AMPROSIUM_ACTIVE_CASING.get(), active))
                .addMapping('L', state(GTOBlocks.AMPROSIUM_PIPE_CASING.get(), active))
                .addMapping('M', state(GTOBlocks.CONTAINMENT_FIELD_GENERATOR.get(), active))
                .addMapping('N', state(GTOBlocks.IMPROVED_SUPERCONDUCTOR_COIL.get(), active))
                .addMapping('Q', state(GTOBlocks.PRESSURE_CONTAINMENT_CASING.get(), active))
                .addMapping('U', state(GTOBlocks.HYPER_CORE.get(), active))
                .addMapping('V', state(GTOBlocks.QUANTUM_GLASS.get(), active));
    }

    private static BlockState state(Block block, boolean active) {
        BlockState state = block.defaultBlockState();
        return state.hasProperty(ActiveBlock.ACTIVE) ? state.setValue(ActiveBlock.ACTIVE, active) : state;
    }

    private record RingBuffers(VertexBuffer innerVerticalRing, VertexBuffer outerVerticalRing, VertexBuffer horizontalRing,
                               TextureUpdateRequester textureUpdateRequester) {}

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
        return 192;
    }
}
