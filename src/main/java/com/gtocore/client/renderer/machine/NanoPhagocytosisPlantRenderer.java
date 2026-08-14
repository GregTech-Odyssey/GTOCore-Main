package com.gtocore.client.renderer.machine;

import com.gtocore.client.renderer.StructureVBO;
import com.gtocore.client.renderer.TextureUpdateRequester;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.machine.multiblock.electric.nano.NanoPhagocytosisPlantMachine;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

public final class NanoPhagocytosisPlantRenderer extends WorkableCasingMachineRenderer {

    private VertexBuffer innerVerticalRing;
    private VertexBuffer outerVerticalRing;
    private VertexBuffer horizontalRing;
    private TextureUpdateRequester textureUpdateRequester;

    public NanoPhagocytosisPlantRenderer() {
        super(GTOCore.id("block/casings/naquadah_reinforced_plant_casing"), GTCEu.id("block/multiblock/fusion_reactor"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (!(blockEntity instanceof MetaMachineBlockEntity machineBlockEntity) ||
                !(machineBlockEntity.getMetaMachine() instanceof NanoPhagocytosisPlantMachine machine) ||
                !machine.isDynamicPartVisible(NanoPhagocytosisPlantMachine.INNER_VERTICAL_RING) ||
                blockEntity.getLevel() instanceof TrackedDummyWorld) return;
        if (innerVerticalRing == null) initRings(machine);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        textureUpdateRequester.requestUpdate();
        renderRing(machine, NanoPhagocytosisPlantMachine.INNER_VERTICAL_RING, innerVerticalRing, partialTicks, poseStack);
        renderRing(machine, NanoPhagocytosisPlantMachine.OUTER_VERTICAL_RING, outerVerticalRing, partialTicks, poseStack);
        renderRing(machine, NanoPhagocytosisPlantMachine.HORIZONTAL_RING, horizontalRing, partialTicks, poseStack);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    @OnlyIn(Dist.CLIENT)
    private static void renderRing(NanoPhagocytosisPlantMachine machine, String partName, VertexBuffer ring, float partialTicks, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.mulPoseMatrix(machine.getDynamicTransform(partName, partialTicks));
        ring.bind();
        ring.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        VertexBuffer.unbind();
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void initRings(NanoPhagocytosisPlantMachine machine) {
        StructureVBO structure = createRingStructure();
        innerVerticalRing = structure.assignStructure(machine.getDynamicStructure(NanoPhagocytosisPlantMachine.INNER_VERTICAL_RING)).build();
        outerVerticalRing = createRingStructure().assignStructure(machine.getDynamicStructure(NanoPhagocytosisPlantMachine.OUTER_VERTICAL_RING)).build();
        horizontalRing = createRingStructure().assignStructure(machine.getDynamicStructure(NanoPhagocytosisPlantMachine.HORIZONTAL_RING)).build();
        textureUpdateRequester = structure.getTextureUpdateRequestor();
    }

    private static StructureVBO createRingStructure() {
        return new StructureVBO()
                .addMapping('B', GTBlocks.HIGH_POWER_CASING.get())
                .addMapping('C', GTOBlocks.NAQUADAH_REINFORCED_PLANT_CASING.get())
                .addMapping('E', GTOBlocks.HYPER_MECHANICAL_CASING.get())
                .addMapping('F', GTOBlocks.NEUTRONIUM_STABLE_CASING.get())
                .addMapping('G', GTBlocks.FUSION_COIL.get())
                .addMapping('H', GTOBlocks.IRIDIUM_CASING.get())
                .addMapping('I', GTOBlocks.FUSION_COIL_MK2.get())
                .addMapping('J', GTOBlocks.AMPROSIUM_ACTIVE_CASING.get())
                .addMapping('L', GTOBlocks.AMPROSIUM_PIPE_CASING.get())
                .addMapping('M', GTOBlocks.CONTAINMENT_FIELD_GENERATOR.get())
                .addMapping('N', GTOBlocks.IMPROVED_SUPERCONDUCTOR_COIL.get())
                .addMapping('Q', GTOBlocks.PRESSURE_CONTAINMENT_CASING.get())
                .addMapping('U', GTOBlocks.HYPER_CORE.get())
                .addMapping('V', GTOBlocks.QUANTUM_GLASS.get());
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
        return 192;
    }
}
