package com.gtocore.integration.emi.trades;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import com.google.common.collect.MapMaker;
import com.gto.datasynclib.datastream.DataComponentKey;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentMap;

/** World-independent identity and appearance of an EMI Trades display entity. */
public final class TradeEntitySnapshot {

    private static final ConcurrentMap<Entity, TradeEntitySnapshot> SNAPSHOTS = new MapMaker().weakKeys().makeMap();
    private static final DataComponentKey<Reference2ObjectOpenHashMap<TradeEntitySnapshot, Entity>> ENTITIES = DataComponentKey.createNoCodec("gtocore_emi_trade_entities");

    private final EntityType<?> type;
    private final ResourceLocation id;
    private final CompoundTag tag;
    private WeakReference<Entity> entity;
    private boolean retained;

    private TradeEntitySnapshot(Entity entity) {
        type = entity.getType();
        id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        tag = entity.saveWithoutId(new CompoundTag());
        this.entity = new WeakReference<>(entity);
    }

    public static @Nullable TradeEntitySnapshot of(@Nullable Entity entity) {
        return entity == null ? null : SNAPSHOTS.computeIfAbsent(entity, TradeEntitySnapshot::new);
    }

    public ResourceLocation id() {
        return id;
    }

    public CompoundTag tag() {
        return tag;
    }

    /** Called by recipe widgets/rendering; recipe indexing never needs to materialize an entity. */
    public @Nullable Entity entity() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var cached = entity.get();
        if (cached != null && cached.level() == level && retained) return cached;

        var entities = ILevel.getCapability(level, ENTITIES);
        if (entities == null) {
            entities = new Reference2ObjectOpenHashMap<>(16);
            ILevel.setCapability(level, ENTITIES, entities);
        }
        var display = entities.get(this);
        if (display == null) {
            if (cached != null && cached.level() == level) {
                display = cached;
            } else {
                display = type.create(level);
                if (display == null) return null;
                display.load(tag);
            }
            entities.put(this, display);
        }
        if (cached != display) entity = new WeakReference<>(display);
        retained = true;
        return display;
    }
}
