package com.gtocore.dimensionprobe;

import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.api.dimension.DimensionTemplates;
import com.gtolib.api.dimension.OwnerRef;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

/** Two launches of an isolated save, with an abrupt JVM halt instead of the first shutdown save. */
final class CrashProbe {

    CrashProbe() {
        MinecraftForge.EVENT_BUS.addListener(this::started);
    }

    private void started(ServerStartedEvent event) {
        var server = event.getServer();
        var manager = DimensionManager.get(server);
        Path marker = Path.of("crash-stage.txt");
        try {
            if (Files.notExists(marker)) {
                var owner = new OwnerRef(OwnerRef.Kind.PLAYER, UUID.fromString("abddc7d2-d087-407d-bdbb-3a7751caab87"));
                var instance = manager.getOrCreatePrivate(owner, DimensionTemplates.VOID, "crash-recovery", 71823L);
                var level = manager.loadNow(instance.dimension());
                level.setBlockAndUpdate(new BlockPos(0, 64, 0), Blocks.STONE.defaultBlockState());
                level.setChunkForced(0, 0, true);
                var storage = level.getDataStorage();
                var nativeData = new NativeData(77);
                var binaryData = new BinaryData(77);
                storage.set("crash_native", nativeData);
                storage.set("crash_binary", binaryData);
                nativeData.setDirty();
                binaryData.setDirty();
                level.save(null, true, false);
                storage.save();
                // Exercise normal SavedData saving, outside the checked unload scope.
                Path nativeFile = storage.getDataFile("crash_native").toPath();
                byte[] nativeBytes = Files.readAllBytes(nativeFile);
                Path temporary = nativeFile.resolveSibling(nativeFile.getFileName() + ".tmp");
                Files.createDirectory(temporary);
                nativeData.value = 88;
                nativeData.setDirty();
                try {
                    nativeData.save(nativeFile.toFile());
                    throw new AssertionError("Native save swallowed the IO failure");
                } catch (UncheckedIOException expected) {
                    require(nativeData.isDirty(), "Native save failure cleared dirty");
                    require(Arrays.equals(nativeBytes, Files.readAllBytes(nativeFile)), "Native save failure replaced the old file");
                } finally {
                    Files.delete(temporary);
                }
                Path binaryFile = storage.getDataFile("crash_binary").toPath();
                byte[] binaryBytes = Files.readAllBytes(binaryFile);
                binaryData.value = 88;
                binaryData.fail = true;
                binaryData.setDirty();
                try {
                    binaryData.save(binaryFile.toFile());
                    throw new AssertionError("Binary save swallowed the serialization failure");
                } catch (UncheckedIOException expected) {
                    require(binaryData.isDirty(), "Binary save failure cleared dirty");
                    require(Arrays.equals(binaryBytes, Files.readAllBytes(binaryFile)), "Binary save failure replaced the old file");
                }
                // The old chunks.dat still contains the forced ticket. Crash before its removal is saved.
                level.setChunkForced(0, 0, false);
                manager.updateForcedIndex(level);
                require(manager.catalog().forcedDimensions().contains(instance.dimension().location()), "Unsaved ticket removal lost its startup candidate");
                Files.writeString(marker, instance.dimension().location().toString());
                Files.writeString(Path.of("crash-result.txt"), "CRASH_PROBE_HALTING");
                System.out.println("CRASH_PROBE_HALTING");
                Runtime.getRuntime().halt(0);
            } else {
                var key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(Files.readString(marker)));
                var level = server.getLevel(key);
                require(level != null && level.getSeed() == 71823L, "Crash restart did not load the saved instance");
                require(level.getForcedChunks().contains(0L), "Crash restart lost the durable forced ticket");
                require(level.getBlockState(new BlockPos(0, 64, 0)).is(Blocks.STONE), "Crash restart lost committed terrain");
                var storage = level.getDataStorage();
                var nativeData = storage.get(tag -> new NativeData(tag.getInt("value")), "crash_native");
                require(nativeData != null && nativeData.value == 77, "Crash restart lost the old native metadata");
                var binaryData = FastSavedData.getFromFile("crash_binary", storage, input -> new BinaryData(input.readInt()));
                require(binaryData != null && binaryData.value == 77, "Crash restart lost the old binary metadata");
                level.setChunkForced(0, 0, false);
                var chunks = storage.get(ForcedChunksSavedData::load, "chunks");
                chunks.save(storage.getDataFile("chunks"));
                require(NbtIo.readCompressed(storage.getDataFile("chunks")).getCompound("data").getLongArray("Forced").length == 0, "Ticket removal was not committed");
                manager.updateForcedIndex(level);
                require(!manager.catalog().forcedDimensions().contains(key.location()), "Committed ticket removal retained its candidate");
                Files.writeString(Path.of("crash-result.txt"), "CRASH_PROBE_PASSED");
                System.out.println("CRASH_PROBE_PASSED");
                server.halt(false);
            }
        } catch (Throwable failure) {
            failure.printStackTrace();
            try {
                Files.writeString(Path.of("crash-result.txt"), "CRASH_PROBE_FAILED: " + failure);
            } catch (IOException recordingFailure) {
                failure.addSuppressed(recordingFailure);
            }
            server.halt(false);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class NativeData extends SavedData {

        private int value;

        private NativeData(int value) {
            this.value = value;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putInt("value", value);
            return tag;
        }
    }

    private static final class BinaryData extends FastSavedData {

        private int value;
        private boolean fail;

        private BinaryData(int value) {
            this.value = value;
        }

        @Override
        public void save(DataIOStream stream) throws IOException {
            stream.writeInt(value);
            if (fail) throw new IOException("Injected interrupted binary write");
        }
    }
}
