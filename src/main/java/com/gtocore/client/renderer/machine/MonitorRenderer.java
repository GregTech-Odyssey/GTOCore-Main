package com.gtocore.client.renderer.machine;

import com.gtocore.common.machine.monitor.IMonitor;
import com.gtocore.common.machine.monitor.Manager;
import com.gtocore.config.GTOConfig;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.MachineRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MonitorRenderer extends MachineRenderer {

    public static final ResourceLocation MONITOR_OVERLAY = GTOCore.id("block/overlay/machine/overlay_monitor");
    public static final ResourceLocation MONITOR_OVERLAY_FULL_CTM = GTOCore.id("block/overlay/machine/overlay_monitor_full_ctm");
    public static final Map<Manager.GridFacedPoint, Manager.GridNetwork> gridToNetworkCLIENT = new ConcurrentHashMap<>();
    private static final float MARGIN = 0.15f;

    public MonitorRenderer() {
        super(GTCEu.id("block/cube/tinted/all"));
        this.setTextureOverride(Map.of("all", GTCEu.id("block/casings/solid/machine_casing_solid_steel")));
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (!(machine instanceof IMonitor monitor)) {
            // machine is null when rendering is at GUI context
            // use the default monitor overlay
            quads.add(StaticFaceBakery.bakeFace(frontFacing, ModelFactory.getBlockSprite(MONITOR_OVERLAY)));
            return;
        }
        Manager.MonitorCTM ctm = Manager.MonitorCTM.getConnection(
                monitor.getFrontFacing(),
                monitor.getPos(),
                monitor.getLevel());
        Direction trueFacing = RelativeDirection.FRONT.getRelative(machine.getFrontFacing(), machine.getUpwardsFacing(), false);
        trueFacing = ModelFactory.modelFacing(frontFacing, trueFacing);
        var cube = StaticFaceBakery.SLIGHTLY_OVER_BLOCK;
        quads.add(
                StaticFaceBakery.bakeQuad(
                        new Vector3f((float) cube.minX * 16.0F, (float) cube.minY * 16.0F, (float) cube.minZ * 16.0F),
                        new Vector3f((float) cube.maxX * 16.0F, (float) cube.maxY * 16.0F, (float) cube.maxZ * 16.0F),
                        new BlockElementFace(
                                null,
                                -1,
                                "",
                                new BlockFaceUV(new float[] {
                                        (float) ctm.getU() / FULL_CTM_WIDTH * 16.0f,
                                        (float) ctm.getV() / FULL_CTM_HEIGHT * 16.0f,
                                        (ctm.getU() + 16.0F) / FULL_CTM_WIDTH * 16.0f,
                                        (ctm.getV() + 16.0F) / FULL_CTM_HEIGHT * 16.0f }, 0)),
                        ModelFactory.getBlockSprite(MONITOR_OVERLAY_FULL_CTM),
                        trueFacing,
                        ModelFactory.getRotation(frontFacing),
                        null,
                        true,
                        0));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
        super.onAdditionalModel(registry);
        registry.accept(GTOCore.id("obj/neutron_star"));
    }

    @Override
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof MetaMachineBlockEntity holder && holder.getMetaMachine() instanceof IMonitor monitor && monitor.getLevel() != null) {
            Manager.GridNetwork network = Manager.networkAt(monitor);
            if (network == null || !monitor.getPos().equals(network.getOriginPos())) return;
            stack.pushPose();
            var pos = monitor.getPos();
            if (GTOConfig.INSTANCE.devMode.dev) {
                var box = Shapes.create(network.aabb());
                LevelRenderer.renderVoxelShape(stack, buffer.getBuffer(RenderType.lines()), box, -pos.getX(), -pos.getY(), -pos.getZ(), 0f, 1f, 0f, 1f, false);
            }
            var front = monitor.getFrontFacing();
            stack.translate(
                    (front == Direction.WEST ? -0.01f : 1.01f) + (front == Direction.SOUTH ? -0.02f : 0),
                    front == Direction.DOWN ? -0.01f : 1.01f,
                    (front == Direction.NORTH ? -0.01f : 1.01f) + (front == Direction.WEST ? -0.02f : 0));
            stack.mulPose(Axis.YN.rotationDegrees(front.toYRot() + 180.0f));
            stack.mulPose(Axis.XP.rotationDegrees(toPitchAngle(front)));
            stack.translate(switch (front) {
                case WEST, SOUTH -> 1 - MARGIN;
                case EAST, NORTH -> network.width3D() - 1 - MARGIN;
                default -> 0;
            }, network.height3D() - 1 - MARGIN, 0);
            stack.scale(-1, -1, 1);
            MonitorSurface.render(network, monitor.getLevel(), stack, network.width3D() - MARGIN * 2,
                    network.height3D() - MARGIN * 2);
            stack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static float toPitchAngle(Direction dir) {
        return switch (dir) {
            case DOWN -> 90.0f;
            case UP -> 270.0f;
            default -> 0.0f;
        };
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(MONITOR_OVERLAY);
            register.accept(MONITOR_OVERLAY_FULL_CTM);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class RenderedCacheClearer {

        public static void init() {
            MinecraftForge.EVENT_BUS.addListener(RenderedCacheClearer::clearRenderCache);
        }

        public static void clearRenderCache(LevelEvent.Unload event) {
            if (event.getLevel().isClientSide()) MonitorSurface.clear();
        }
    }

    private static final int FULL_CTM_WIDTH = 64;
    private static final int FULL_CTM_HEIGHT = 64;
}
