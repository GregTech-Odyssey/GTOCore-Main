package com.gtocore.mixin.corpse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageOpenHistoryMixinTest {

    private static final int MAX_PAYLOAD_BYTES = 1_048_576;
    // SimpleChannel writes the discriminator before calling the message encoder.
    private static final int MESSAGE_ID = 1;
    private static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath("corpse", "main");
    private static Field deathsField;
    private static Field toNbtMethodField;
    private static Method encodeMethod;

    private FriendlyByteBuf buffer;

    @BeforeAll
    static void findEncoder() throws ReflectiveOperationException {
        deathsField = MessageOpenHistoryMixin.class.getDeclaredField("deaths");
        deathsField.setAccessible(true);
        toNbtMethodField = MessageOpenHistoryMixin.class.getDeclaredField("gtocore$toNbtMethod");
        toNbtMethodField.setAccessible(true);
        encodeMethod = MessageOpenHistoryMixin.class.getDeclaredMethod("gtocore$writeBoundedHistory",
                FriendlyByteBuf.class, CallbackInfo.class);
        encodeMethod.setAccessible(true);
    }

    @BeforeEach
    void prepareBuffer() throws IllegalAccessException {
        toNbtMethodField.set(null, null);
        buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(MESSAGE_ID);
    }

    @AfterEach
    void releaseBuffer() {
        buffer.release();
    }

    @Test
    void preservesSmallHistoryIncludingEquipmentAndOrder() throws ReflectiveOperationException {
        ObjectArrayList<SampleDeath> history = bookHistory(3, 1);

        assertTrue(encode(history));

        assertEquals(historyTag(history), receive());
    }

    @ParameterizedTest
    @CsvSource({ "80, false", "140, true" })
    void trimsBookHistoryToBothBudgets(int count, boolean exceedsNbtBudget) throws ReflectiveOperationException {
        ObjectArrayList<SampleDeath> history = bookHistory(count, 20);
        writeRawHistory(history);
        assertTrue(buffer.writerIndex() > MAX_PAYLOAD_BYTES);
        assertEquals(exceedsNbtBudget, readUsage() > 2_000_000L);

        assertTrue(encode(history));

        CompoundTag decoded = receive();
        ListTag received = decoded.getList("Deaths", Tag.TAG_COMPOUND);
        assertFalse(received.isEmpty());
        assertTrue(received.size() <= count);
        boolean equipmentOmitted = false;
        for (int i = 0; i < received.size(); i++) {
            CompoundTag expected = history.get(i).toNBT(false);
            CompoundTag actual = received.getCompound(i);
            if (!actual.contains("Equipment")) {
                equipmentOmitted = true;
                expected.remove("Equipment");
            } else {
                assertFalse(equipmentOmitted, "Equipment must be retained newest first");
            }
            assertEquals(expected, actual);
        }
        assertTrue(received.getCompound(0).contains("Equipment"));
        assertTrue(equipmentOmitted);
        if (received.size() < count) {
            CompoundTag next = history.get(received.size()).toNBT(false);
            next.remove("Equipment");
            received.add(next);
            buffer.clear();
            buffer.writeByte(MESSAGE_ID);
            buffer.writeNbt(decoded);
            assertThrows(IllegalArgumentException.class, () -> new ClientboundCustomPayloadPacket(CHANNEL, buffer),
                    "Another metadata entry must not fit after the retained prefix");
        }
    }

    @ParameterizedTest
    @CsvSource({ "0, true", "1, true", "0, false", "1, false" })
    void respectsExactPayloadBoundaryIncludingMessageId(int excess, boolean inEquipment)
                                                                                         throws ReflectiveOperationException {
        SampleDeath death = bookHistory(1, 1).get(0);
        if (!inEquipment) {
            death.tag.remove("Equipment");
        }
        CompoundTag payload = inEquipment ? death.tag.getList("Equipment", Tag.TAG_COMPOUND).getCompound(0) : death.tag;
        payload.putByteArray("Payload", new byte[0]);
        List<SampleDeath> history = List.of(death);
        writeRawHistory(history);
        payload.putByteArray("Payload", new byte[MAX_PAYLOAD_BYTES + excess - buffer.writerIndex()]);
        writeRawHistory(history);
        assertEquals(MAX_PAYLOAD_BYTES + excess, buffer.writerIndex());

        assertTrue(encode(history));

        CompoundTag received = receive();
        if (excess == 0) {
            assertEquals(MAX_PAYLOAD_BYTES, buffer.writerIndex());
            assertEquals(historyTag(history), received);
        } else if (inEquipment) {
            CompoundTag expected = death.toNBT(false);
            expected.remove("Equipment");
            assertEquals(1, received.getList("Deaths", Tag.TAG_COMPOUND).size());
            assertEquals(expected, received.getList("Deaths", Tag.TAG_COMPOUND).getCompound(0));
        } else {
            assertTrue(received.getList("Deaths", Tag.TAG_COMPOUND).isEmpty());
        }
    }

    @Test
    void trimsWhenNbtAccountingFillsBeforeWireBudget() throws ReflectiveOperationException {
        ObjectArrayList<SampleDeath> history = bookHistory(2, 1);
        ListTag tags = new ListTag();
        StringTag empty = StringTag.valueOf("");
        for (int i = 0; i < 55_000; i++) {
            tags.add(empty);
        }
        history.get(1).tag.getList("Equipment", Tag.TAG_COMPOUND).getCompound(0).getCompound("tag").put("Entries", tags);
        writeRawHistory(history);
        assertTrue(buffer.writerIndex() < MAX_PAYLOAD_BYTES);
        assertTrue(readUsage() > 2_097_152L);

        assertTrue(encode(history));

        ListTag received = receive().getList("Deaths", Tag.TAG_COMPOUND);
        assertEquals(2, received.size());
        assertEquals(history.get(0).toNBT(false), received.getCompound(0));
        CompoundTag expected = history.get(1).toNBT(false);
        expected.remove("Equipment");
        assertEquals(expected, received.getCompound(1));
    }

    @Test
    void leavesEmptyHistoryToOriginalEncoder() throws ReflectiveOperationException {
        assertFalse(encode(Collections.emptyList()));
        assertOnlyMessageIdWritten();
    }

    @Test
    void leavesMissingSerializerToOriginalEncoder() throws ReflectiveOperationException {
        assertFalse(encode(List.of(new Object())));
        assertOnlyMessageIdWritten();
    }

    @Test
    void leavesNoPartialNbtWhenSerializationFails() throws ReflectiveOperationException {
        ObjectArrayList<SampleDeath> history = bookHistory(2, 1);
        history.set(1, new SampleDeath(new CompoundTag()) {

            @Override
            public CompoundTag toNBT(boolean includeInventory) {
                throw new IllegalStateException("Serialization failed");
            }
        });

        assertFalse(encode(history));
        assertOnlyMessageIdWritten();
    }

    private boolean encode(List<?> history) throws ReflectiveOperationException {
        buffer.clear();
        buffer.writeByte(MESSAGE_ID);
        MessageOpenHistoryMixin mixin = new MessageOpenHistoryMixin() {};
        deathsField.set(mixin, history);
        CallbackInfo callback = new CallbackInfo("toBytes", true);
        encodeMethod.invoke(mixin, buffer, callback);
        return callback.isCancelled();
    }

    private CompoundTag receive() {
        assertDoesNotThrow(() -> new ClientboundCustomPayloadPacket(CHANNEL, buffer),
                () -> "Payload bytes including message ID: " + buffer.writerIndex());
        assertEquals(MESSAGE_ID, buffer.readUnsignedByte());
        CompoundTag received = buffer.readNbt(new NbtAccounter(2_097_152L));
        assertNotNull(received);
        assertFalse(buffer.isReadable());
        return received;
    }

    private void writeRawHistory(List<SampleDeath> history) {
        buffer.clear();
        buffer.writeByte(MESSAGE_ID);
        buffer.writeNbt(historyTag(history));
    }

    private long readUsage() {
        assertEquals(MESSAGE_ID, buffer.readUnsignedByte());
        NbtAccounter accounter = new NbtAccounter(Long.MAX_VALUE);
        buffer.readNbt(accounter);
        return accounter.getUsage();
    }

    private void assertOnlyMessageIdWritten() {
        assertEquals(MESSAGE_ID, buffer.readUnsignedByte());
        assertFalse(buffer.isReadable());
    }

    private static CompoundTag historyTag(List<SampleDeath> history) {
        ListTag entries = new ListTag();
        for (SampleDeath death : history) {
            entries.add(death.toNBT(false));
        }
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("Deaths", entries);
        return wrapper;
    }

    private static ObjectArrayList<SampleDeath> bookHistory(int count, int pages) {
        ObjectArrayList<SampleDeath> history = new ObjectArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", new UUID(0, i + 1));
            entry.putUUID("PlayerUuid", new UUID(1, 1));
            entry.putString("PlayerName", "TestPlayer");
            entry.putLong("Timestamp", 1_700_000_000_000L - i);
            entry.putInt("Experience", 20);
            entry.putDouble("PosX", 0);
            entry.putDouble("PosY", 64);
            entry.putDouble("PosZ", 0);
            entry.putString("Dimension", "minecraft:overworld");
            entry.putByte("Model", (byte) 0);
            CompoundTag book = new CompoundTag();
            book.putString("id", "minecraft:written_book");
            book.putByte("Count", (byte) 1);
            CompoundTag bookData = new CompoundTag();
            bookData.putString("title", "Travel notes");
            bookData.putString("author", "TestPlayer");
            ListTag bookPages = new ListTag();
            StringTag page = StringTag.valueOf("{\"text\":\"" + "A".repeat(900) + "\"}");
            for (int j = 0; j < pages; j++) {
                bookPages.add(page);
            }
            bookData.put("pages", bookPages);
            book.put("tag", bookData);
            ListTag equipment = new ListTag();
            equipment.add(book);
            for (int j = 1; j < 6; j++) {
                equipment.add(new CompoundTag());
            }
            entry.put("Equipment", equipment);
            history.add(new SampleDeath(entry));
        }
        return history;
    }

    // Corpse is optional; only its reflective toNBT contract is replaced here.
    public static class SampleDeath {

        private final CompoundTag tag;

        SampleDeath(CompoundTag tag) {
            this.tag = tag;
        }

        public CompoundTag toNBT(boolean includeInventory) {
            assertFalse(includeInventory);
            return tag.copy();
        }
    }
}
