package com.gtocore.common.item;

import com.gtocore.config.GTOConfig;
import com.gtocore.eio_travel.api.ITravelTarget;
import com.gtocore.eio_travel.api.TravelTargetType;
import com.gtocore.eio_travel.implementations.AnchorTravelTarget;
import com.gtocore.eio_travel.logic.TravelSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.suried.gtoutils.spatial.BlockRange;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 维度权杖上的旅行锚收藏。权杖物品 NBT 里保存一份跨维度的锚点坐标与名称快照，选中后右键传送到该锚。
 */
public final class DimensionScepterBookmarks {

    public static final int MAX_BOOKMARKS = 16;
    private static final String BOOKMARKS = "ScepterBookmarks";
    private static final String SELECTED = "ScepterBookmarkSelected";
    private static final String DIMENSION = "dim";
    private static final String POS = "pos";
    private static final String NAME = "name";

    private DimensionScepterBookmarks() {}

    public record Bookmark(ResourceKey<Level> dimension, BlockPos pos, String name) {}

    public static List<Bookmark> get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return Collections.emptyList();
        ListTag list = tag.getList(BOOKMARKS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return Collections.emptyList();
        ArrayList<Bookmark> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString(DIMENSION));
            if (id == null) continue;
            result.add(new Bookmark(ResourceKey.create(Registries.DIMENSION, id), BlockPos.of(entry.getLong(POS)),
                    entry.getString(NAME)));
        }
        return result;
    }

    /** 只取条数，不解析条目。 */
    public static int count(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getList(BOOKMARKS, Tag.TAG_COMPOUND).size();
    }

    @Nullable
    public static Bookmark get(ItemStack stack, int index) {
        if (index < 0) return null;
        List<Bookmark> bookmarks = get(stack);
        return index < bookmarks.size() ? bookmarks.get(index) : null;
    }

    public static int selectedIndex(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SELECTED)) return -1;
        return tag.getInt(SELECTED);
    }

    @Nullable
    public static Bookmark selected(ItemStack stack) {
        return get(stack, selectedIndex(stack));
    }

    public static void setSelectedIndex(ItemStack stack, int index) {
        if (index < 0) {
            CompoundTag tag = stack.getTag();
            if (tag == null) return;
            tag.remove(SELECTED);
            if (tag.isEmpty()) stack.setTag(null);
            return;
        }
        stack.getOrCreateTag().putInt(SELECTED, index);
    }

    /** 点击已选中的收藏会取消选中，此时右键不再传送。 */
    public static void toggleSelected(ItemStack stack, int index) {
        setSelectedIndex(stack, selectedIndex(stack) == index ? -1 : index);
    }

    public static boolean isFull(ItemStack stack) {
        return count(stack) >= MAX_BOOKMARKS;
    }

    public static boolean contains(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos) {
        return contains(get(stack), dimension, pos);
    }

    private static boolean contains(List<Bookmark> bookmarks, ResourceKey<Level> dimension, BlockPos pos) {
        for (Bookmark bookmark : bookmarks) {
            if (bookmark.pos().equals(pos) && bookmark.dimension().equals(dimension)) return true;
        }
        return false;
    }

    public static boolean add(ItemStack stack, Level level, AnchorTravelTarget target) {
        return add(stack, new Bookmark(level.dimension(), target.getPos(), target.getName()));
    }

    private static boolean add(ItemStack stack, Bookmark bookmark) {
        List<Bookmark> bookmarks = get(stack);
        if (bookmarks.size() >= MAX_BOOKMARKS) return false;
        if (bookmarks.isEmpty()) bookmarks = new ArrayList<>(4);
        for (Bookmark existing : bookmarks) {
            if (existing.pos().equals(bookmark.pos()) && existing.dimension().equals(bookmark.dimension())) return false;
        }
        bookmarks.add(bookmark);
        write(stack, bookmarks);
        if (selectedIndex(stack) < 0) setSelectedIndex(stack, bookmarks.size() - 1);
        return true;
    }

    public static void remove(ItemStack stack, int index) {
        List<Bookmark> bookmarks = get(stack);
        if (index < 0 || index >= bookmarks.size()) return;
        bookmarks.remove(index);
        int selected = selectedIndex(stack);
        if (selected >= 0) {
            if (bookmarks.isEmpty()) {
                selected = -1;
            } else if (selected > index) {
                selected--;
            } else if (selected == index) {
                selected = Math.min(index, bookmarks.size() - 1);
            }
            if (selected >= bookmarks.size()) selected = bookmarks.size() - 1;
        }
        write(stack, bookmarks);
        setSelectedIndex(stack, selected);
    }

    private static void write(ItemStack stack, List<Bookmark> bookmarks) {
        CompoundTag tag = stack.getOrCreateTag();
        if (bookmarks.isEmpty()) {
            tag.remove(BOOKMARKS);
        } else {
            ListTag list = new ListTag();
            for (Bookmark bookmark : bookmarks) {
                CompoundTag entry = new CompoundTag();
                entry.putString(DIMENSION, bookmark.dimension().location().toString());
                entry.putLong(POS, bookmark.pos().asLong());
                entry.putString(NAME, bookmark.name());
                list.add(entry);
            }
            tag.put(BOOKMARKS, list);
        }
        if (tag.isEmpty()) stack.setTag(null);
    }

    /** 收藏或选中项变化的轻量签名：直接对 NBT 列表求 hash，不解析条目。 */
    public static int signature(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int result = tag == null ? 0 : tag.getList(BOOKMARKS, Tag.TAG_COMPOUND).hashCode();
        return 31 * result + selectedIndex(stack);
    }

    /**
     * 当前维度、维度权杖可传送范围内、已经命名且尚未收藏的旅行锚，按距离排序。
     */
    public static List<AnchorTravelTarget> candidates(Level level, ItemStack scepter, BlockPos center) {
        return candidates(level, get(scepter), center);
    }

    /** 用已解析的收藏列表（界面里按签名缓存的一份）筛选附近可收藏的锚。 */
    public static List<AnchorTravelTarget> candidates(Level level, List<Bookmark> bookmarks, BlockPos center) {
        int range = GTOConfig.INSTANCE.travelConfig.itemRange;
        List<ITravelTarget> found = TravelSavedData.getTravelData(level).getTravelTargetsWithin(center, BlockRange.sphere(range));
        if (found.isEmpty()) return Collections.emptyList();
        long rangeSquared = (long) range * range;
        ArrayList<AnchorTravelTarget> result = new ArrayList<>(found.size());
        for (ITravelTarget target : found) {
            if (target.getTargetType() != TravelTargetType.ANCHOR) continue;
            if (!(target instanceof AnchorTravelTarget anchor)) continue;
            if (anchor.getName().isBlank()) continue;
            if (center.distSqr(anchor.getPos()) > rangeSquared) continue;
            if (contains(bookmarks, level.dimension(), anchor.getPos())) continue;
            result.add(anchor);
        }
        result.sort(Comparator.comparingDouble(anchor -> center.distSqr(anchor.getPos())));
        return result;
    }

    /**
     * 校验并取得当前维度中可作为收藏目标的旅行锚；不在范围内、未命名或已收藏时返回 null。
     */
    @Nullable
    public static AnchorTravelTarget findCandidate(Level level, ItemStack scepter, Player player, BlockPos pos) {
        return findCandidate(level, get(scepter), player, pos);
    }

    /** 用已解析的收藏列表校验附近可收藏的锚。 */
    @Nullable
    public static AnchorTravelTarget findCandidate(Level level, List<Bookmark> bookmarks, Player player, BlockPos pos) {
        if (!(TravelSavedData.getTravelData(level).getTravelTarget(pos).orElse(null) instanceof AnchorTravelTarget anchor)) {
            return null;
        }
        if (anchor.getTargetType() != TravelTargetType.ANCHOR || anchor.getName().isBlank()) return null;
        if (contains(bookmarks, level.dimension(), pos)) return null;
        int range = GTOConfig.INSTANCE.travelConfig.itemRange;
        return player.blockPosition().distSqr(pos) <= (long) range * range ? anchor : null;
    }

    /**
     * 收藏项显示名：与收藏项同维度时跟随锚点当前名称，跨维度时使用收藏时的名称快照。
     */
    public static String displayName(@Nullable Level level, Bookmark bookmark) {
        if (level != null && level.dimension().equals(bookmark.dimension())) {
            ITravelTarget target = TravelSavedData.getTravelData(level).getTravelTarget(bookmark.pos()).orElse(null);
            if (target instanceof AnchorTravelTarget anchor && !anchor.getName().isBlank()) return anchor.getName();
        }
        return bookmark.name();
    }
}
