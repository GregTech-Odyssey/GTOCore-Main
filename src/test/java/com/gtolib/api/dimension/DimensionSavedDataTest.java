package com.gtolib.api.dimension;

import com.gtolib.api.misc.FastSavedData;

import net.minecraft.network.FriendlyByteBuf;

import com.gto.datasynclib.util.FileUtil;
import com.gto.datasynclib.util.VersionedFriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DimensionSavedDataTest {

    @TempDir
    Path directory;

    @Test
    void versionedDataRetainsCommittedBytesAndDirtyAfterEncodingOrCommitFailure() throws Exception {
        var data = new VersionedData();
        var target = directory.resolve("versioned.dat");
        data.value = 77;
        data.setDirty();
        data.save(target.toFile());
        assertEquals(77, FileUtil.readVersioned(target, (VersionedFriendlyByteBuf buffer) -> {
            assertEquals(1, buffer.version());
            return buffer.readInt();
        }));
        byte[] committed = Files.readAllBytes(target);
        data.value = 88;
        data.fail = true;
        data.setDirty();
        assertThrows(IllegalStateException.class, () -> data.save(target.toFile()));
        assertTrue(data.isDirty());
        assertArrayEquals(committed, Files.readAllBytes(target));
        data.fail = false;
        var temporary = target.resolveSibling("versioned.dat.tmp");
        Files.deleteIfExists(temporary);
        Files.createDirectory(temporary);
        assertThrows(RuntimeException.class, () -> data.save(target.toFile()));
        assertTrue(data.isDirty());
        assertArrayEquals(committed, Files.readAllBytes(target));
        Files.delete(temporary);
        data.save(target.toFile());
        assertFalse(data.isDirty());
        assertEquals(88, FileUtil.readVersioned(target, FriendlyByteBuf::readInt));
    }

    private static final class VersionedData extends FastSavedData {

        private int value;
        private boolean fail;

        @Override
        public int version() {
            return 1;
        }

        @Override
        public void save(FriendlyByteBuf buffer) {
            buffer.writeInt(value);
            if (fail) throw new IllegalStateException("Injected encoding failure");
        }
    }
}
