package com.gtolib.drone;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneSettingsTest {

    private static final ResourceLocation DIMENSION = ResourceLocation.parse("minecraft:overworld");

    @Test
    void itemPersistenceRestoresSpeedModeShapeAndBothFullHeightAnchors() {
        var settings = new DroneSettings(0.235f, DroneMode.MODEL, true, true,
                new BlockPos(-150, -20000000, 20), new BlockPos(40, 20000000, 80), DIMENSION);
        assertEquals(settings, DroneSettings.KEY.reader.apply(DroneSettings.KEY.writer.apply(settings)));
    }

    @Test
    void compactNetworkRoundTripPreservesIncompleteSelectionsAndDoesNotPackAnchorHeight() {
        var settings = new DroneSettings(0.025f, DroneMode.OBSERVE, false, true,
                null, new BlockPos(-29999999, 20000003, 29999999), DIMENSION);
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            settings.writeToBuf(buf);
            assertEquals(settings, DroneSettings.readFromBuf(buf, DIMENSION));
            assertFalse(buf.isReadable());
        } finally {
            buf.release();
        }
    }

    @Test
    void missingPreferencesUseDefaultsAndChangingDimensionsOnlyClearsAnchors() {
        var tag = new CompoundTag();
        tag.putString("dimension", DIMENSION.toString());
        assertEquals(DroneSettings.defaults(DIMENSION), DroneSettings.KEY.reader.apply(tag));
        var settings = new DroneSettings(0.2f, DroneMode.MODEL, true, true, BlockPos.ZERO, new BlockPos(2, 3, 4), DIMENSION);
        var otherDimension = ResourceLocation.parse("minecraft:the_nether");
        assertEquals(new DroneSettings(0.2f, DroneMode.MODEL, true, true, null, null, otherDimension), settings.forSession(4, otherDimension));
        assertEquals(settings, settings.forSession(4, DIMENSION));
        assertEquals(DroneSelection.maxSpeed(3), settings.forSession(3, DIMENSION).speed());
        assertEquals(0.1f, new DroneSettings(Float.NaN, DroneMode.INTERACT, false, false, null, null, DIMENSION).forSession(3, DIMENSION).speed());
    }
}
