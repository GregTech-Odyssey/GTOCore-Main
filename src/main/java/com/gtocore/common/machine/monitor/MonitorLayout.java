package com.gtocore.common.machine.monitor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** The saved positions of the cards on one formed monitor screen. Coordinates are GUI pixels. */
public final class MonitorLayout {

    public static final int PIXELS_PER_BLOCK = 160;
    public static final int MAX_ENTRIES = 128;

    private MonitorLayout() {}

    public record Entry(long source, ResourceLocation component, int x, int y, int width, int height) {

        Entry resizeFromCorner(boolean left, boolean top, int dx, int dy, int screenWidth, int screenHeight) {
            int right = x + width;
            int bottom = y + height;
            int resizedX = left ? Math.clamp(x + dx, 0, right - 40) : x;
            int resizedY = top ? Math.clamp(y + dy, 0, bottom - 24) : y;
            int resizedRight = left ? right : Math.clamp(right + dx, x + 40, screenWidth);
            int resizedBottom = top ? bottom : Math.clamp(bottom + dy, y + 24, screenHeight);
            return new Entry(source, component, resizedX, resizedY, resizedRight - resizedX, resizedBottom - resizedY);
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("source", source);
            tag.putString("component", component.toString());
            tag.putInt("x", x);
            tag.putInt("y", y);
            tag.putInt("width", width);
            tag.putInt("height", height);
            return tag;
        }

        @Nullable
        public static Entry load(CompoundTag tag) {
            ResourceLocation component = ResourceLocation.tryParse(tag.getString("component"));
            if (component == null) return null;
            return new Entry(tag.getLong("source"), component, tag.getInt("x"), tag.getInt("y"), tag.getInt("width"), tag.getInt("height"));
        }
    }

    public static List<Entry> load(CompoundTag tag) {
        ListTag items = tag.getList("items", Tag.TAG_COMPOUND);
        ArrayList<Entry> entries = new ArrayList<>(Math.min(items.size(), MAX_ENTRIES));
        for (int i = 0, n = Math.min(items.size(), MAX_ENTRIES); i < n; i++) {
            Entry entry = Entry.load(items.getCompound(i));
            if (entry != null) entries.add(entry);
        }
        return entries;
    }

    public static CompoundTag save(List<Entry> entries) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (int i = 0, n = Math.min(entries.size(), MAX_ENTRIES); i < n; i++) items.add(entries.get(i).save());
        tag.put("items", items);
        return tag;
    }
}
