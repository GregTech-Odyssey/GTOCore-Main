package com.gtocore.client.overlay;

import com.gtocore.client.forge.ThickPolylineRenderer;
import com.gtocore.client.renderer.RenderHelper;
import com.gtocore.common.item.DataStickExtension;
import com.gtocore.common.machine.multiblock.part.WirelessOpticalDataHatchMachine;

import com.gtolib.api.wireless.ReceiverTransmitterHandler;
import com.gtolib.api.wireless.ReceiverTransmitterHandler.Connection;
import com.gtolib.api.wireless.ReceiverTransmitterHandler.ConnectionType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import kotlin.Pair;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class ReceiverTransmitterClientHandler {

    private static final String COMPUTATION_TRANSMITTER = "wireless_computation_transmitter";
    private static final String COMPUTATION_RECEIVER = "wireless_computation_receiver";
    private static final float COMPUTATION_RED = 0.4F;
    private static final float COMPUTATION_GREEN = 0.8F;
    private static final float COMPUTATION_BLUE = 1.0F;
    private static final float DATA_RED = 1.0F;
    private static final float DATA_GREEN = 0.9F;
    private static final float DATA_BLUE = 0.45F;
    private static final float LINE_WIDTH = 0.12F;
    private static final int COMPUTATION_LINE_COLOR = 0x904CCFFF;
    private static final int DATA_LINE_COLOR = 0x90FFE673;

    private static final ObjectArrayList<Pair<Vec3, Vec3>> COMPUTATION_SEGMENTS = new ObjectArrayList<>();
    private static final ObjectArrayList<Pair<Vec3, Vec3>> DATA_SEGMENTS = new ObjectArrayList<>();
    private static final StoredPosition COMPUTATION_TRANSMITTER_POS = new StoredPosition(COMPUTATION_TRANSMITTER);
    private static final StoredPosition COMPUTATION_RECEIVER_POS = new StoredPosition(COMPUTATION_RECEIVER);
    private static final StoredPosition DATA_TRANSMITTER_POS = new StoredPosition(WirelessOpticalDataHatchMachine.KEY_TRANSMITTER);
    private static final StoredPosition DATA_RECEIVER_POS = new StoredPosition(WirelessOpticalDataHatchMachine.KEY_RECEIVER);

    private static Connection[] cachedConnections;
    private static ResourceKey<Level> cachedDimension;

    private ReceiverTransmitterClientHandler() {}

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null) return;

        ItemStack dataStick = heldDataStick(player);
        if (dataStick.isEmpty()) return;

        ResourceKey<Level> dimension = level.dimension();
        Connection[] connections = ReceiverTransmitterHandler.getClientConnections(dimension);
        rebuildSegmentsIfNeeded(dimension, connections);
        for (Connection connection : connections) {
            if (connection.type() == ConnectionType.COMPUTATION) {
                highlightConnection(event, connection, COMPUTATION_RED, COMPUTATION_GREEN, COMPUTATION_BLUE);
            } else {
                highlightConnection(event, connection, DATA_RED, DATA_GREEN, DATA_BLUE);
            }
        }

        ThickPolylineRenderer.drawSegments(
                event.getPoseStack(), event.getCamera(), COMPUTATION_LINE_COLOR, LINE_WIDTH, COMPUTATION_SEGMENTS);
        ThickPolylineRenderer.drawSegments(
                event.getPoseStack(), event.getCamera(), DATA_LINE_COLOR, LINE_WIDTH, DATA_SEGMENTS);
        renderStoredPositions(event, level, dataStick.getTag());
    }

    public static void clear() {
        cachedConnections = null;
        cachedDimension = null;
        COMPUTATION_SEGMENTS.clear();
        DATA_SEGMENTS.clear();
        COMPUTATION_TRANSMITTER_POS.clear();
        COMPUTATION_RECEIVER_POS.clear();
        DATA_TRANSMITTER_POS.clear();
        DATA_RECEIVER_POS.clear();
    }

    private static ItemStack heldDataStick(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (DataStickExtension.isItem(stack)) return stack;
        stack = player.getOffhandItem();
        return DataStickExtension.isItem(stack) ? stack : ItemStack.EMPTY;
    }

    private static void highlightConnection(RenderLevelStageEvent event, Connection connection, float red, float green, float blue) {
        RenderHelper.highlightBlock(event.getCamera(), event.getPoseStack(), red, green, blue, 1.5F,
                connection.transmitter(), connection.transmitter());
        RenderHelper.highlightBlock(event.getCamera(), event.getPoseStack(), red, green, blue, 1.5F,
                connection.receiver(), connection.receiver());
    }

    private static void rebuildSegmentsIfNeeded(ResourceKey<Level> dimension, Connection[] connections) {
        if (cachedConnections == connections && cachedDimension == dimension) return;
        cachedConnections = connections;
        cachedDimension = dimension;
        COMPUTATION_SEGMENTS.clear();
        DATA_SEGMENTS.clear();
        for (Connection connection : connections) {
            Pair<Vec3, Vec3> segment = new Pair<>(
                    Vec3.atCenterOf(connection.transmitter()), Vec3.atCenterOf(connection.receiver()));
            if (connection.type() == ConnectionType.COMPUTATION) COMPUTATION_SEGMENTS.add(segment);
            else DATA_SEGMENTS.add(segment);
        }
    }

    private static void renderStoredPositions(RenderLevelStageEvent event, Level level, CompoundTag tag) {
        COMPUTATION_TRANSMITTER_POS.update(tag);
        COMPUTATION_RECEIVER_POS.update(tag);
        DATA_TRANSMITTER_POS.update(tag);
        DATA_RECEIVER_POS.update(tag);
        float pulse = 0.5F + 0.5F * Mth.sin((level.getGameTime() + event.getPartialTick()) * 0.2F);
        highlightStored(event, COMPUTATION_TRANSMITTER_POS.pos, pulse,
                COMPUTATION_RED, COMPUTATION_GREEN, COMPUTATION_BLUE);
        highlightStored(event, COMPUTATION_RECEIVER_POS.pos, 1.0F - pulse,
                COMPUTATION_RED, COMPUTATION_GREEN, COMPUTATION_BLUE);
        highlightStored(event, DATA_TRANSMITTER_POS.pos, pulse, DATA_RED, DATA_GREEN, DATA_BLUE);
        highlightStored(event, DATA_RECEIVER_POS.pos, 1.0F - pulse, DATA_RED, DATA_GREEN, DATA_BLUE);
    }

    private static void highlightStored(RenderLevelStageEvent event, @Nullable BlockPos pos,
                                        float pulse, float red, float green, float blue) {
        if (pos == null) return;
        RenderHelper.highlightBlock(event.getCamera(), event.getPoseStack(),
                Mth.lerp(pulse, red, 1.0F), Mth.lerp(pulse, green, 1.0F), Mth.lerp(pulse, blue, 1.0F),
                3.0F, pos, pos);
    }

    private static final class StoredPosition {

        private final String key;
        private BlockPos pos;
        private int x;
        private int y;
        private int z;

        private StoredPosition(String key) {
            this.key = key;
        }

        private void update(CompoundTag tag) {
            if (tag == null || !tag.contains(key, Tag.TAG_COMPOUND)) {
                pos = null;
                return;
            }
            CompoundTag posTag = tag.getCompound(key);
            int newX = posTag.getInt("x");
            int newY = posTag.getInt("y");
            int newZ = posTag.getInt("z");
            if (pos == null || x != newX || y != newY || z != newZ) {
                x = newX;
                y = newY;
                z = newZ;
                pos = new BlockPos(x, y, z);
            }
        }

        private void clear() {
            pos = null;
        }
    }
}
