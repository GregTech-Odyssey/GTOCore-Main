package com.gtocore.client.screen.starmap;

import com.gtolib.GTOCore;
import com.gtolib.api.data.CelestialBody;
import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.data.Galaxy;
import com.gtolib.api.misc.PlanetManagement;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.planets.AdAstraData;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 星图的数据与布局：哪些星系、哪些星球可见（与原选星页的判定一致），以及它们在画布上的位置；卫星绕母星排布。
 */
@OnlyIn(Dist.CLIENT)
final class StarMapModel {

    static final float STAR_SIZE = 30;
    static final float BODY_SIZE = 18;
    static final float TILT = 0.55f;
    static final float RING_BASE = 42;
    static final float RING_STEP = 18;
    static final float DISTANCE_BASE = 20;
    static final float DISTANCE_SCALE = 60;
    static final float SPREAD = 10;
    static final float SATELLITE_BASE = 15;
    static final float SATELLITE_STEP = 10;
    static final float SYSTEM_GAP = 70 * SPREAD;
    static final float EMPTY_RADIUS = 50 * SPREAD;
    static final float TITLE_ROOM = 28;
    private static final float GOLDEN_ANGLE = 2.39996f;
    private static final ResourceLocation FALLBACK_ICON = RLUtils.ad("textures/environment/earth.png");
    private static final Map<ResourceLocation, CelestialBody> VIRTUAL_PRIMARIES = virtualPrimaries();

    record Body(int index, Planet planet, @Nullable Dimension dimension, SystemInfo system, int requiredTier, int ring, int order, float angle,
                int moon, float parentAngle, float orbitRadius, ResourceLocation icon, Component name, int stations) {

        boolean satellite() {
            return moon >= 0;
        }

        boolean centralStar() {
            return dimensionKey() == GTODimensions.SOLAR_SURFACE;
        }

        ResourceKey<Level> dimensionKey() {
            return planet.dimension();
        }

        ResourceKey<Level> orbit() {
            return planet.orbitIfPresent();
        }
    }

    record Anchor(int order, float angle, ResourceLocation icon, Component name) {}

    static final class SystemInfo {

        final ResourceLocation id;
        @Nullable
        final Galaxy galaxy;
        final Component name;
        final ResourceLocation star;
        final IntArrayList rings = new IntArrayList();
        final Int2FloatOpenHashMap ringRadii = new Int2FloatOpenHashMap();
        final IntArrayList ringMoons = new IntArrayList();
        final List<Body> bodies = new ArrayList<>();
        final List<Anchor> anchors = new ArrayList<>();
        @Nullable
        Body centralStar;
        float cx, cy, radius;

        private SystemInfo(ResourceLocation id, @Nullable Galaxy galaxy) {
            this.id = id;
            this.galaxy = galaxy;
            this.name = galaxy != null ? Component.translatable(galaxy.getTranslationKey()) :
                    Component.translatableWithFallback("solar_system." + id.getNamespace() + "." + id.getPath(), PlanetsScreen.title(id.getPath()));
            this.star = galaxy != null ? galaxy.getStar() : RLUtils.ad("textures/environment/sun.png");
        }

        CanvasRect bounds() {
            return CanvasRect.of(cx - radius, cy - radius * TILT - TITLE_ROOM, 2 * radius, 2 * radius * TILT + TITLE_ROOM);
        }

        float ringRadius(int ring) {
            return ringRadii.get(ring);
        }
    }

    final List<SystemInfo> systems = new ArrayList<>();
    final List<Body> bodies = new ArrayList<>();
    final int rocketTier;
    @Nullable
    Body current;
    SystemInfo focus;

    private StarMapModel(int rocketTier) {
        this.rocketTier = rocketTier;
    }

    static StarMapModel build(PlanetsMenu menu) {
        var model = new StarMapModel(menu.tier());
        var here = menu.player().level().dimension();
        var systems = new Object2ObjectLinkedOpenHashMap<ResourceLocation, SystemInfo>();
        var known = AdAstraData.solarSystems();
        for (var galaxy : Galaxy.all()) {
            if (galaxy == Galaxy.NONE || !known.contains(galaxy.getSolarSystem()) || systems.containsKey(galaxy.getSolarSystem())) continue;
            systems.put(galaxy.getSolarSystem(), new SystemInfo(galaxy.getSolarSystem(), galaxy));
        }
        var others = new ArrayList<>(known);
        others.sort(Comparator.comparing(ResourceLocation::getPath));
        for (var id : others) {
            if (!systems.containsKey(id)) systems.put(id, new SystemInfo(id, null));
        }
        var pending = new ArrayList<Candidate>();
        var planets = new ArrayList<>(AdAstraData.planets().values());
        planets.sort(Comparator.comparingInt(Planet::tier).thenComparing(p -> menu.getPlanetName(p.dimension()).getString()));
        for (var planet : planets) {
            if (planet.isSpace() || planet.tier() >= GTODimensions.UNREACHABLE_PLANET_TIER) continue;
            if (menu.disabledPlanets().contains(planet.dimension().location())) continue;
            int requiredTier = PlanetManagement.requiredTier(planet, here);
            var system = systems.get(planet.solarSystem());
            if (system == null) continue;
            PlanetManagement.checkPlanetIsUnlocked(planet.dimension());
            var dimension = GTODimensions.getDimensionIncludingOrbits(planet.dimension());
            int ring = dimension != null && dimension.getStellarDistance() > 0 ? dimension.getStellarDistance() : 0;
            pending.add(new Candidate(planet, dimension, system, requiredTier, ring));
        }
        for (var system : systems.values()) layoutSystem(system, pending, menu);
        float x = 0;
        for (var system : systems.values()) {
            system.cx = x + system.radius;
            system.cy = 0;
            x += 2 * system.radius + SYSTEM_GAP;
            model.systems.add(system);
        }
        for (var system : model.systems) {
            for (int i = 0; i < system.bodies.size(); i++) {
                var body = system.bodies.get(i);
                var placed = new Body(model.bodies.size(), body.planet(), body.dimension(), system, body.requiredTier(), body.ring(), body.order(),
                        body.angle(), body.moon(), body.parentAngle(), body.orbitRadius(), body.icon(), body.name(), body.stations());
                system.bodies.set(i, placed);
                if (placed.centralStar()) system.centralStar = placed;
                model.bodies.add(placed);
                if (body.dimensionKey() == here || body.orbit() == here) model.current = placed;
            }
        }
        if (model.current != null) {
            model.focus = model.current.system();
        } else {
            var galaxy = GTODimensions.getGalaxy(here);
            model.focus = model.systems.isEmpty() ? null : model.systems.getFirst();
            for (var system : model.systems) {
                if (galaxy != null && system.galaxy == galaxy) model.focus = system;
            }
        }
        return model;
    }

    private static void layoutSystem(SystemInfo system, List<Candidate> pending, PlanetsMenu menu) {
        int nextRing = 0;
        for (var entry : pending) {
            if (entry.system == system && entry.planet.dimension() != GTODimensions.SOLAR_SURFACE) nextRing = Math.max(nextRing, entry.ring);
        }
        var byRing = new Object2ObjectLinkedOpenHashMap<Integer, List<Candidate>>();
        for (var entry : pending) {
            if (entry.system != system) continue;
            if (entry.planet.dimension() == GTODimensions.SOLAR_SURFACE) {
                system.bodies.add(body(entry, system, -1, 0, -1, 0, 0, menu));
                continue;
            }
            if (entry.ring <= 0) entry.ring = ++nextRing;
            byRing.computeIfAbsent(entry.ring, r -> new ArrayList<>()).add(entry);
        }
        var order = new ArrayList<>(byRing.keySet());
        order.sort(Integer::compare);
        float radius = EMPTY_RADIUS;
        for (int ring : order) {
            var members = byRing.get(ring);
            var groups = new Object2ObjectLinkedOpenHashMap<ResourceLocation, List<Candidate>>();
            for (var member : members) {
                var id = member.planet.dimension().location();
                var primary = primaryKey(member);
                boolean primaryVisible = primary == null || VIRTUAL_PRIMARIES.containsKey(primary) ||
                        members.stream().anyMatch(m -> m.planet.dimension().location().equals(primary));
                groups.computeIfAbsent(primary != null && primaryVisible ? primary : id, k -> new ArrayList<>()).add(member);
            }
            int moons = 0;
            for (var group : groups.entrySet()) {
                boolean orbiting = VIRTUAL_PRIMARIES.containsKey(group.getKey()) || group.getValue().stream().anyMatch(m -> m.planet.dimension().location().equals(group.getKey()));
                if (orbiting) moons = Math.max(moons, (int) group.getValue().stream().filter(m -> !m.planet.dimension().location().equals(group.getKey())).count());
            }
            float extent = BODY_SIZE / 2 + (moons > 0 ? SATELLITE_BASE + (moons - 1) * SATELLITE_STEP : 0);
            float ringRadius = RING_BASE + system.rings.size() * RING_STEP;
            for (var member : members) {
                var body = member.dimension != null ? member.dimension.getBody() : null;
                if (body != null && body.getMapDistanceRatio() > 0) {
                    ringRadius = (DISTANCE_BASE + (float) Math.sqrt(body.getMapDistanceRatio()) * DISTANCE_SCALE) * SPREAD;
                    break;
                }
            }
            int ringOrder = system.rings.size();
            system.ringRadii.put(ring, ringRadius);
            system.rings.add(ring);
            system.ringMoons.add(moons);
            int slot = 0;
            for (var group : groups.entrySet()) {
                float angle = (float) (ring * GOLDEN_ANGLE + slot++ * (2 * Math.PI / groups.size()));
                var list = group.getValue();
                boolean hasPrimaryBody = list.stream().anyMatch(m -> m.planet.dimension().location().equals(group.getKey()));
                var virtual = hasPrimaryBody ? null : VIRTUAL_PRIMARIES.get(group.getKey());
                if (virtual != null) system.anchors.add(new Anchor(ringOrder, angle, virtual.getTexture(), Component.translatable(virtual.getTranslationKey())));
                boolean orbiting = hasPrimaryBody || virtual != null;
                int moon = 0;
                for (var member : list) {
                    boolean primary = member.planet.dimension().location().equals(group.getKey());
                    if (primary || !orbiting) {
                        system.bodies.add(body(member, system, ringOrder, angle, -1, 0, ringRadius, menu));
                        continue;
                    }
                    float orbit = SATELLITE_BASE + moon * SATELLITE_STEP;
                    system.bodies.add(body(member, system, ringOrder, (float) (angle + 2.1 + moon * 2.4), moon, angle, orbit, menu));
                    moon++;
                }
            }
            radius = Math.max(radius, ringRadius + extent + 4);
        }
        system.radius = radius;
    }

    @Nullable
    private static ResourceLocation primaryKey(Candidate entry) {
        var body = entry.dimension != null ? entry.dimension.getBody() : null;
        var primary = body != null ? body.getPrimary() : null;
        if (primary == null) return null;
        var dimension = Dimension.get(primary);
        return dimension != null ? dimension.getLocation() : GTOCore.id(primary.getId());
    }

    private static Map<ResourceLocation, CelestialBody> virtualPrimaries() {
        var map = new HashMap<ResourceLocation, CelestialBody>();
        for (var body : CelestialBody.all()) {
            var primary = body.getPrimary();
            if (primary != null && Dimension.get(primary) == null) map.put(GTOCore.id(primary.getId()), primary);
        }
        return map;
    }

    private static Body body(Candidate entry, SystemInfo system, int order, float angle, int moon, float parentAngle, float orbitRadius,
                             PlanetsMenu menu) {
        var icon = entry.dimension != null ? entry.dimension.getIcon() : FALLBACK_ICON;
        return new Body(-1, entry.planet, entry.dimension, system, entry.requiredTier, entry.ring, order, angle, moon, parentAngle, orbitRadius, icon,
                menu.getPlanetName(entry.planet.dimension()), menu.getOwnedAndTeamSpaceStations(entry.planet.orbitIfPresent()).size());
    }

    private static final class Candidate {

        final Planet planet;
        @Nullable
        final Dimension dimension;
        final SystemInfo system;
        final int requiredTier;
        int ring;

        Candidate(Planet planet, @Nullable Dimension dimension, SystemInfo system, int requiredTier, int ring) {
            this.planet = planet;
            this.dimension = dimension;
            this.system = system;
            this.requiredTier = requiredTier;
            this.ring = ring;
        }
    }

    @Nullable
    Component parentName(Body body) {
        if (!body.satellite()) return null;
        for (var other : body.system().bodies) {
            if (!other.satellite() && other.order() == body.order() && other.angle() == body.parentAngle()) return other.name();
        }
        for (var anchor : body.system().anchors) {
            if (anchor.order() == body.order()) return anchor.name();
        }
        return null;
    }

    boolean reachable(Body body) {
        return rocketTier >= body.requiredTier();
    }

    int reachableCount(SystemInfo system) {
        int count = 0;
        for (var body : system.bodies) {
            if (reachable(body)) count++;
        }
        return count;
    }

    static ResourceLocation fallbackIcon() {
        return FALLBACK_ICON;
    }
}
