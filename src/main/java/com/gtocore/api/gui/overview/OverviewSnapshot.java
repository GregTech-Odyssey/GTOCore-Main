package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.StructurePattern;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public record OverviewSnapshot(List<Module> modules, List<Anchor> anchors, byte flags) {

    public static final byte CONNECTED = 0;
    public static final byte DETACHED = 1;
    public static final byte TRUNCATED = 1;
    public static final byte COARSE = 2;
    public static final OverviewSnapshot EMPTY = new OverviewSnapshot(Collections.emptyList(), Collections.emptyList(), (byte) 0);
    private static final int[] NO_BLOCKS = new int[0];
    private static final int MAX_MODULE_CELLS = 1 << 22;
    private static final int MAX_PALETTE = 1 << 16;
    private static final int MAX_VALUES = 64;
    private static final int MAX_RAW = 1 << 25;
    private static final int MAX_PACKED = 900 * 1024;
    private static final int READ_SLACK = 1024;
    public static final Payload EMPTY_PAYLOAD = EMPTY.encode();

    public record Module(BlockPos pos, ResourceLocation id, Direction front, Direction up, boolean flip, int[] values, byte state, int[] blocks) {

        @Nullable
        public MultiblockMachineDefinition definition() {
            return GTRegistries.MACHINES.get(id) instanceof MultiblockMachineDefinition multi ? multi : null;
        }

        @Nullable
        public Layout layout() {
            var definition = definition();
            var structure = definition == null ? null : StructurePattern.of(definition);
            return structure == null ? null : structure.layout(values);
        }

        public void collect(Long2ObjectOpenHashMap<BlockState> into) {
            collect(layout(), into);
        }

        public void collect(@Nullable Layout layout, Long2ObjectOpenHashMap<BlockState> into) {
            if (layout == null) return;
            var cursor = new BlockPos.MutableBlockPos();
            int n = Math.min(blocks.length, layout.cells().size());
            for (int i = 0; i < n; i++) {
                if (blocks[i] == 0) continue;
                var state = Block.stateById(blocks[i]);
                if (!state.isAir()) into.put(layout.worldPos(pos, i, front, up, flip, cursor).asLong(), state);
            }
        }
    }

    public record Anchor(List<BlockPos> cells, int kind, Direction outward, float markerX, float markerY, float markerZ) {}

    public record Payload(int size, byte[] packed) {

        public void write(FriendlyByteBuf out) {
            out.writeVarInt(size);
            out.writeByteArray(packed);
        }
    }

    public boolean truncated() {
        return (flags & TRUNCATED) != 0;
    }

    public boolean coarse() {
        return (flags & COARSE) != 0;
    }

    public Payload encode() {
        var payload = pack(true);
        if (payload.packed.length <= MAX_PACKED) return payload;
        payload = pack(false);
        if (payload.packed.length <= MAX_PACKED) return payload;
        return new OverviewSnapshot(Collections.emptyList(), Collections.emptyList(), TRUNCATED).pack(false);
    }

    private Payload pack(boolean withBlocks) {
        var raw = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writeRaw(raw, withBlocks);
            var bytes = new byte[raw.readableBytes()];
            raw.readBytes(bytes);
            var deflater = new Deflater(Deflater.BEST_SPEED);
            deflater.setInput(bytes);
            deflater.finish();
            var packed = new ByteArrayOutputStream(Math.max(64, bytes.length / 4));
            var chunk = new byte[8192];
            while (!deflater.finished()) packed.write(chunk, 0, deflater.deflate(chunk));
            deflater.end();
            return new Payload(bytes.length, packed.toByteArray());
        } finally {
            raw.release();
        }
    }

    private void writeRaw(FriendlyByteBuf buf, boolean withBlocks) {
        buf.writeByte(withBlocks ? flags : flags | COARSE);
        buf.writeVarInt(modules.size());
        for (var module : modules) {
            buf.writeBlockPos(module.pos);
            buf.writeResourceLocation(module.id);
            buf.writeEnum(module.front);
            buf.writeEnum(module.up);
            buf.writeBoolean(module.flip);
            buf.writeVarIntArray(module.values);
            buf.writeByte(module.state);
            if (withBlocks) writePacked(buf, module.blocks);
        }
        buf.writeVarInt(anchors.size());
        for (var anchor : anchors) {
            buf.writeVarInt(anchor.cells.size());
            for (var port : anchor.cells) buf.writeBlockPos(port);
            buf.writeVarInt(anchor.kind);
            buf.writeEnum(anchor.outward);
            buf.writeFloat(anchor.markerX);
            buf.writeFloat(anchor.markerY);
            buf.writeFloat(anchor.markerZ);
        }
    }

    private static OverviewSnapshot readRaw(FriendlyByteBuf buf) {
        byte flags = buf.readByte();
        boolean coarse = (flags & COARSE) != 0;
        int moduleCount = buf.readVarInt();
        var modules = new ArrayList<Module>(Math.min(moduleCount, OverviewCapture.MAX_MODULES));
        for (int i = 0; i < moduleCount; i++) {
            modules.add(new Module(buf.readBlockPos(), buf.readResourceLocation(), buf.readEnum(Direction.class), buf.readEnum(Direction.class), buf.readBoolean(),
                    buf.readVarIntArray(MAX_VALUES), buf.readByte(), coarse ? NO_BLOCKS : readPacked(buf)));
        }
        int anchorCount = buf.readVarInt();
        var anchors = new ArrayList<Anchor>(Math.min(anchorCount, 256));
        for (int i = 0; i < anchorCount; i++) {
            int size = buf.readVarInt();
            var ring = new ArrayList<BlockPos>(Math.min(size, 16));
            for (int k = 0; k < size; k++) ring.add(buf.readBlockPos());
            anchors.add(new Anchor(ring, buf.readVarInt(), buf.readEnum(Direction.class), buf.readFloat(), buf.readFloat(), buf.readFloat()));
        }
        return new OverviewSnapshot(modules, anchors, flags);
    }

    public static OverviewSnapshot read(FriendlyByteBuf in) {
        int size = in.readVarInt();
        if (size < 0 || size > MAX_RAW) throw new IllegalStateException("overview snapshot too large: " + size);
        var packed = in.readByteArray(MAX_PACKED + READ_SLACK);
        var inflater = new Inflater();
        inflater.setInput(packed);
        var bytes = new byte[size];
        try {
            int done = 0;
            while (done < size && !inflater.finished()) {
                int n = inflater.inflate(bytes, done, size - done);
                if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) break;
                done += n;
            }
        } catch (DataFormatException e) {
            throw new IllegalStateException("bad overview snapshot", e);
        } finally {
            inflater.end();
        }
        return readRaw(new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)));
    }

    private static int bits(int paletteSize) {
        return paletteSize <= 1 ? 0 : 32 - Integer.numberOfLeadingZeros(paletteSize - 1);
    }

    private static void writePacked(FriendlyByteBuf buf, int[] values) {
        buf.writeVarInt(values.length);
        if (values.length == 0) return;
        var index = new Int2IntOpenHashMap();
        index.defaultReturnValue(-1);
        var palette = new IntArrayList();
        for (int value : values) {
            if (index.putIfAbsent(value, palette.size()) == -1) palette.add(value);
        }
        buf.writeVarInt(palette.size());
        for (int i = 0; i < palette.size(); i++) buf.writeVarInt(palette.getInt(i));
        int bits = bits(palette.size());
        if (bits == 0) return;
        long word = 0;
        int used = 0;
        for (int value : values) {
            long code = index.get(value);
            word |= code << used;
            used += bits;
            if (used >= 64) {
                buf.writeLong(word);
                used -= 64;
                word = used == 0 ? 0 : code >>> (bits - used);
            }
        }
        if (used > 0) buf.writeLong(word);
    }

    private static int[] readPacked(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
        if (length < 0 || length > MAX_MODULE_CELLS) throw new IllegalStateException("overview snapshot too large: " + length);
        var values = new int[length];
        if (length == 0) return values;
        int size = buf.readVarInt();
        if (size <= 0 || size > Math.min(length, MAX_PALETTE)) throw new IllegalStateException("bad overview palette " + size);
        var palette = new int[size];
        for (int i = 0; i < size; i++) palette[i] = buf.readVarInt();
        int bits = bits(size);
        if (bits == 0) {
            Arrays.fill(values, palette[0]);
            return values;
        }
        long mask = (1L << bits) - 1;
        long word = 0;
        int used = 64;
        for (int i = 0; i < length; i++) {
            if (used == 64) {
                word = buf.readLong();
                used = 0;
            }
            long code = word >>> used;
            int got = 64 - used;
            if (got >= bits) {
                used += bits;
            } else {
                word = buf.readLong();
                code |= word << got;
                used = bits - got;
            }
            int index = (int) (code & mask);
            if (index >= size) throw new IllegalStateException("bad overview palette index " + index);
            values[i] = palette[index];
        }
        return values;
    }
}
