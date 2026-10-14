package com.gtocore.client.screen.starmap.base;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.Galaxy;
import com.gtolib.utils.RLUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
final class StarEntries {

    static final int FALLBACK_SYSTEM_COLOR = 0xFFFFD27A;

    private StarEntries() {}

    static final class SystemEntry {

        final int index;
        final ResourceLocation id;
        @Nullable
        final Galaxy galaxy;
        final Component name;
        final ResourceLocation star;
        final IntArrayList rings = new IntArrayList();
        final Int2FloatOpenHashMap ringRadii = new Int2FloatOpenHashMap();
        final FloatArrayList orderRadii = new FloatArrayList();
        final ObjectArrayList<BodyEntry> bodies = new ObjectArrayList<>();
        final ObjectArrayList<AnchorEntry> anchors = new ObjectArrayList<>();
        float cx, cy, radius;
        @Nullable
        StarSprites.Sprite sprite;

        SystemEntry(int index, ResourceLocation id, @Nullable Galaxy galaxy) {
            this.index = index;
            this.id = id;
            this.galaxy = galaxy;
            this.name = galaxy != null ? Component.translatable(galaxy.getTranslationKey()) :
                    Component.translatableWithFallback("solar_system." + id.getNamespace() + "." + id.getPath(), PlanetsScreen.title(id.getPath()));
            this.star = galaxy != null ? galaxy.getStar() : RLUtils.ad("textures/environment/sun.png");
        }

        int color() {
            return galaxy != null ? galaxy.getColor() : FALLBACK_SYSTEM_COLOR;
        }
    }

    static final class BodyEntry {

        int index = -1;
        final ResourceKey<Level> key;
        @Nullable
        final ResourceKey<Level> orbit;
        @Nullable
        final Planet planet;
        @Nullable
        final Dimension dimension;
        final ResourceLocation icon;
        final Component name;
        final boolean realm;
        boolean stellar;
        int system = -1, order, moon = -1, parent = -1, anchor = -1;
        float angle, parentAngle, orbitRadius, cos, sin, parentCos, parentSin, realmX, realmY;
        @Nullable
        StarSprites.Sprite sprite, badge;

        BodyEntry(ResourceKey<Level> key, @Nullable ResourceKey<Level> orbit, @Nullable Planet planet, @Nullable Dimension dimension,
                  ResourceLocation icon, Component name, boolean realm) {
            this.key = key;
            this.orbit = orbit;
            this.planet = planet;
            this.dimension = dimension;
            this.icon = icon;
            this.name = name;
            this.realm = realm;
        }

        void place(int system, int order, float angle, int moon, float parentAngle, float orbitRadius) {
            this.system = system;
            this.order = order;
            this.angle = angle;
            this.moon = moon;
            this.parentAngle = parentAngle;
            this.orbitRadius = orbitRadius;
            this.cos = (float) Math.cos(angle);
            this.sin = (float) Math.sin(angle);
            this.parentCos = (float) Math.cos(parentAngle);
            this.parentSin = (float) Math.sin(parentAngle);
        }

        void placeStellar(int system) {
            this.system = system;
            this.order = -1;
            this.stellar = true;
        }

        boolean isMoon() {
            return moon >= 0;
        }
    }

    static final class AnchorEntry {

        final int system, order;
        final float cos, sin;
        final ResourceLocation icon;
        final Component name;
        @Nullable
        StarSprites.Sprite sprite;

        AnchorEntry(int system, int order, float angle, ResourceLocation icon, Component name) {
            this.system = system;
            this.order = order;
            this.cos = (float) Math.cos(angle);
            this.sin = (float) Math.sin(angle);
            this.icon = icon;
            this.name = name;
        }
    }
}
