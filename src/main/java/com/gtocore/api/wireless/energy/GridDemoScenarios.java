package com.gtocore.api.wireless.energy;

import com.gtolib.api.data.Dimension;

import static com.gtolib.api.data.Dimension.ALFHEIM;
import static com.gtolib.api.data.Dimension.ANCIENT_WORLD;
import static com.gtolib.api.data.Dimension.BARNARDA_C;
import static com.gtolib.api.data.Dimension.CERES;
import static com.gtolib.api.data.Dimension.CREATE;
import static com.gtolib.api.data.Dimension.ENCELADUS;
import static com.gtolib.api.data.Dimension.FLAT;
import static com.gtolib.api.data.Dimension.GANYMEDE;
import static com.gtolib.api.data.Dimension.GLACIO;
import static com.gtolib.api.data.Dimension.IO;
import static com.gtolib.api.data.Dimension.MARS;
import static com.gtolib.api.data.Dimension.MERCURY;
import static com.gtolib.api.data.Dimension.MOON;
import static com.gtolib.api.data.Dimension.OTHERSIDE;
import static com.gtolib.api.data.Dimension.OVERWORLD;
import static com.gtolib.api.data.Dimension.PLUTO;
import static com.gtolib.api.data.Dimension.THE_END;
import static com.gtolib.api.data.Dimension.THE_NETHER;
import static com.gtolib.api.data.Dimension.TITAN;
import static com.gtolib.api.data.Dimension.VENUS;
import static com.gtolib.api.data.Dimension.VOID;

final class GridDemoScenarios {

    private static final int[] CONSUMER_POWERS = { 30, 14, 22, 18, 26 };
    private static final int[] PRODUCER_POWERS = { 20, 28, 16, 24 };

    private GridDemoScenarios() {}

    static void build(GridDemo.Scenario scenario, GridDemoBuilder b) {
        switch (scenario) {
            case SOLAR -> solar(b);
            case REALMS -> realms(b);
            case SATURATED -> saturated(b);
            case LARGE -> large(b);
            case EMPTY -> {}
        }
    }

    private static void solar(GridDemoBuilder b) {
        b.tower(OVERWORLD, 72, 10, 0.8).tower(MOON, 64, 9, 0.3).tower(MARS, 62, 7, 0).tower(VENUS, 60, 8, 1)
                .tower(CERES, 58, 6, 0.05).tower(TITAN, 66, 9, 0.55).tower(PLUTO, 56, 5, 0.15).tower(BARNARDA_C, 64, 9, 0.45)
                .tower(THE_NETHER, 60, 8, 0.7).tower(THE_END, 56, 5, 0.2).tower(VOID, 50, 4, 0.95);
        b.relays(OVERWORLD, MOON, 10, 2).relay(MOON, MARS, 7).relay(OVERWORLD, VENUS, 9).relay(VENUS, MERCURY, 6)
                .relay(MARS, CERES, 6).relay(CERES, IO, 5).relay(IO, GANYMEDE, 5).relay(GANYMEDE, TITAN, 8)
                .relay(TITAN, ENCELADUS, 6).relay(MARS, TITAN, 6).relay(OVERWORLD, TITAN, 3).relay(TITAN, PLUTO, 5)
                .relay(OVERWORLD, THE_NETHER, 8).relay(THE_NETHER, THE_END, 5).relay(THE_NETHER, VOID, 3)
                .relay(OVERWORLD, BARNARDA_C, 9).relay(BARNARDA_C, GLACIO, 7);
        b.consumer(MARS, 1L << 34).consumer(IO, 1L << 21).consumer(THE_END, 1L << 15).consumer(MERCURY, 1L << 17)
                .consumer(GLACIO, 1L << 22).consumer(ENCELADUS, 1L << 19);
        b.producer(THE_NETHER, 1L << 20).producer(TITAN, 1L << 18).producer(PLUTO, 1L << 16);
    }

    private static void realms(GridDemoBuilder b) {
        b.tower(OVERWORLD, 68, 9, 0.6).tower(THE_NETHER, 64, 9, 0.85).tower(THE_END, 62, 8, 0.4).tower(OTHERSIDE, 60, 7, 0.1)
                .tower(ALFHEIM, 58, 7, 0).tower(ANCIENT_WORLD, 56, 6, 0.65).tower(FLAT, 54, 5, 1).tower(VOID, 60, 8, 0.3);
        b.relay(THE_NETHER, THE_END, 8).relay(THE_END, OTHERSIDE, 7).relay(OTHERSIDE, ALFHEIM, 6).relay(ALFHEIM, ANCIENT_WORLD, 6)
                .relay(ANCIENT_WORLD, FLAT, 5).relay(FLAT, VOID, 5).relay(VOID, CREATE, 7).relay(CREATE, THE_NETHER, 7)
                .relays(OVERWORLD, THE_NETHER, 9, 2).relay(OVERWORLD, VOID, 8).relay(OVERWORLD, ALFHEIM, 4);
        b.consumer(ALFHEIM, 1L << 22).consumer(CREATE, 1L << 20).consumer(THE_END, 1L << 18);
        b.producer(FLAT, 1L << 19).producer(THE_NETHER, 1L << 21);
    }

    private static void saturated(GridDemoBuilder b) {
        b.tower(OVERWORLD, 72, 12, 0.9).tower(BARNARDA_C, 66, 10, 0.9);
        b.relays(OVERWORLD, MOON, 4, 2).relay(MOON, MARS, 3).relay(OVERWORLD, VENUS, 5).relay(VENUS, MERCURY, 2)
                .relay(MARS, CERES, 1).relay(OVERWORLD, TITAN, 6).relay(OVERWORLD, BARNARDA_C, 7).relay(BARNARDA_C, GLACIO, 5)
                .relay(OVERWORLD, THE_NETHER, 5);
        for (var dimension : new Dimension[] { MOON, MARS, VENUS, MERCURY, CERES, GLACIO, TITAN }) b.consumer(dimension, 1L << 30);
        b.producer(THE_NETHER, 1L << 28);
    }

    private static void large(GridDemoBuilder b) {
        var all = Dimension.all();
        int n = all.size(), consumers = 0, producers = 0;
        for (int i = 0; i < n; i++) {
            var dimension = all.get(i);
            switch (i % 6) {
                case 0 -> b.tower(dimension, 70 + i % 4, 12, 0.95);
                case 1 -> b.tower(dimension, 56 + i % 6, 6 + i % 5, 0.3 + (i % 5) * 0.1);
                case 2, 5 -> b.tower(dimension, 52 + i % 8, 4 + i % 6, 0);
                default -> {}
            }
            int role = i % 6;
            if (role == 2 || role == 3 || role == 5) b.consumer(dimension, 1L << CONSUMER_POWERS[consumers++ % CONSUMER_POWERS.length]);
            if (role == 4) b.producer(dimension, 1L << PRODUCER_POWERS[producers++ % PRODUCER_POWERS.length]);
            b.relay(dimension, all.get((i + 1) % n), 4 + i % 9);
            b.relay(dimension, all.get((i + 3) % n), 3 + i * 3 % 10);
            if (i % 2 == 0) b.relay(dimension, all.get((i + 7) % n), 5 + i % 8);
            if (i % 4 == 0) b.relay(dimension, all.get((i + 1) % n), 6 + i % 7);
        }
    }
}
