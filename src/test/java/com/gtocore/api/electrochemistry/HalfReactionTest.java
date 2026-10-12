package com.gtocore.api.electrochemistry;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.targets.ForgeServerLaunchHandler;

import com.gto.datasynclib.datastream.codec.JavaValueOps;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.netty.buffer.Unpooled;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HalfReactionTest {

    private static Object previousLaunchHandler;
    private static Dist previousDist;
    private static Path[] previousPaths;

    @TempDir
    static Path gameDirectory;

    @BeforeAll
    static void initializeRegistryEnvironment() throws ReflectiveOperationException {
        // Ion codecs initialize the registry, which queries Forge's launch mode.
        var field = FMLLoader.class.getDeclaredField("commonLaunchHandler");
        field.setAccessible(true);
        previousLaunchHandler = field.get(null);
        field.set(null, new ForgeServerLaunchHandler());
        var distField = FMLLoader.class.getDeclaredField("dist");
        distField.setAccessible(true);
        previousDist = (Dist) distField.get(null);
        distField.set(null, Dist.DEDICATED_SERVER);
        var paths = FMLPaths.values();
        previousPaths = new Path[paths.length];
        for (int i = 0; i < paths.length; i++) previousPaths[i] = paths[i].get();
        FMLPaths.loadAbsolutePaths(gameDirectory);
        SharedConstants.tryDetectVersion();
        // Codec tests only need vanilla registries; Forge networking requires ModLauncher transforms.
        var bootstrap = Bootstrap.class.getDeclaredField("isBootstrapped");
        bootstrap.setAccessible(true);
        if (!bootstrap.getBoolean(null)) {
            bootstrap.setBoolean(null, true);
            BuiltInRegistries.bootStrap();
        }
    }

    @AfterAll
    static void restoreRegistryEnvironment() throws ReflectiveOperationException {
        var field = FMLLoader.class.getDeclaredField("commonLaunchHandler");
        field.setAccessible(true);
        field.set(null, previousLaunchHandler);
        var distField = FMLLoader.class.getDeclaredField("dist");
        distField.setAccessible(true);
        distField.set(null, previousDist);
        var pathField = FMLPaths.class.getDeclaredField("absolutePath");
        pathField.setAccessible(true);
        var paths = FMLPaths.values();
        for (int i = 0; i < paths.length; i++) pathField.set(paths[i], previousPaths[i]);
    }

    private static final IonDefinition PROTON = new IonDefinition("test_proton", 1, "H+");
    private static final IonDefinition SODIUM = new IonDefinition("test_sodium", 1, "Na+");
    private static final IonDefinition CHLORIDE = new IonDefinition("test_chloride", -1, "Cl-");

    @Test
    void consumedIonsContributeOppositeChargeAndBalanceAgainstTheirSource() {
        var source = HalfReaction.builder(ElectrolyteType.WATER).ions(new IonAmount(PROTON, 2)).build();
        var receiver = HalfReaction.builder(ElectrolyteType.WATER).requiredIons(new IonAmount(PROTON, 4)).build();
        assertEquals(-4, receiver.charge());
        var balance = ReactionBalance.cross(source.charge(), receiver.charge());
        assertTrue(source.canSupply(receiver, balance.firstMultiplier(), balance.secondMultiplier()));
        assertTrue(receiver.canSupply(source, balance.secondMultiplier(), balance.firstMultiplier()));
        assertFalse(source.canSupply(receiver, 1, 1));
    }

    @Test
    void chargeBalanceDoesNotSubstituteSpeciesOrAllowSelfSupply() {
        var source = HalfReaction.builder(ElectrolyteType.WATER).ions(new IonAmount(SODIUM, 2)).build();
        var receiver = HalfReaction.builder(ElectrolyteType.WATER).ions(new IonAmount(PROTON, 1))
                .requiredIons(new IonAmount(PROTON, 3)).build();
        assertEquals(-2, receiver.charge());
        assertNotNull(ReactionBalance.cross(source.charge(), receiver.charge()));
        assertFalse(source.canSupply(receiver, 1, 1));
    }

    @Test
    void balancedPairStillNeedsEnoughOfEachRequiredIon() {
        var source = HalfReaction.builder(ElectrolyteType.WATER).ions(new IonAmount(PROTON, 1), new IonAmount(SODIUM, 1)).build();
        var receiver = HalfReaction.builder(ElectrolyteType.WATER).requiredIons(new IonAmount(PROTON, 2)).build();
        var balance = ReactionBalance.cross(source.charge(), receiver.charge());
        assertFalse(source.canSupply(receiver, balance.firstMultiplier(), balance.secondMultiplier()));
    }

    @Test
    void repeatedEntriesAreSummedOnBothSides() {
        var source = HalfReaction.builder(ElectrolyteType.WATER).ions(new IonAmount(PROTON, 1), new IonAmount(PROTON, 2)).build();
        var receiver = HalfReaction.builder(ElectrolyteType.WATER).requiredIons(new IonAmount(PROTON, 2), new IonAmount(PROTON, 2)).build();
        assertFalse(source.canSupply(receiver, 1, 1));
        assertTrue(source.canSupply(receiver, 4, 3));
    }

    @Test
    void bothHalvesCanRequireIonsFromOneAnother() {
        var first = HalfReaction.builder(ElectrolyteType.WATER).ions(new IonAmount(PROTON, 4))
                .requiredIons(new IonAmount(CHLORIDE, 2)).build();
        var second = HalfReaction.builder(ElectrolyteType.WATER).ions(new IonAmount(CHLORIDE, 2))
                .requiredIons(new IonAmount(PROTON, 4)).build();
        assertEquals(6, first.charge());
        assertEquals(-6, second.charge());
        assertTrue(first.canSupply(second, 1, 1));
        assertTrue(second.canSupply(first, 1, 1));
    }

    @Test
    void neutralHalfReactionCannotBeBuilt() {
        assertThrows(IllegalArgumentException.class, () -> HalfReaction.builder(ElectrolyteType.WATER).ions(new IonAmount(PROTON, 1))
                .requiredIons(new IonAmount(PROTON, 1)).build());
    }

    @Test
    void differentElectrolytesCannotSupplyEvenWhenChargesAndIonAmountsMatch() {
        for (var sourceType : ElectrolyteType.values()) {
            var source = HalfReaction.builder(sourceType).ions(new IonAmount(PROTON, 2)).fuel(true).build();
            for (var receiverType : ElectrolyteType.values()) {
                var receiver = HalfReaction.builder(receiverType).requiredIons(new IonAmount(PROTON, 4)).build();
                var balance = ReactionBalance.cross(source.charge(), receiver.charge());
                assertNotNull(balance);
                assertEquals(sourceType == receiverType, source.canSupply(receiver, balance.firstMultiplier(), balance.secondMultiplier()));
                assertEquals(sourceType == receiverType, receiver.canSupply(source, balance.secondMultiplier(), balance.firstMultiplier()));
                var producedOnly = HalfReaction.builder(receiverType).ions(new IonAmount(CHLORIDE, 4)).build();
                assertEquals(sourceType == receiverType, source.canSupply(producedOnly, 2, 1));
                assertEquals(sourceType == receiverType, producedOnly.canSupply(source, 1, 2));
            }
        }
    }

    @Test
    void codecPreservesBothIonDirectionsFuelFlagAndEveryElectrolyte() {
        for (var electrolyteType : ElectrolyteType.values()) {
            var reaction = HalfReaction.builder(electrolyteType).ions(new IonAmount(Ions.PROTON, 2))
                    .requiredIons(new IonAmount(Ions.CHLORIDE, 1)).fuel(true).build();
            assertCodecRoundTrip(reaction);
        }
    }

    @Test
    void codecRestoresEmptyIonListsInEitherDirection() {
        for (var electrolyteType : ElectrolyteType.values()) {
            for (boolean fuel : new boolean[] { false, true }) {
                assertCodecRoundTrip(HalfReaction.builder(electrolyteType).ions(new IonAmount(Ions.PROTON, 2)).fuel(fuel).build());
                assertCodecRoundTrip(HalfReaction.builder(electrolyteType).requiredIons(new IonAmount(Ions.PROTON, 2)).fuel(fuel).build());
            }
        }
    }

    private static void assertCodecRoundTrip(HalfReaction reaction) {
        var ops = JavaValueOps.INSTANCE;
        var encoded = HalfReaction.CODEC.encode(ops, reaction);
        assertReaction(reaction, HalfReaction.CODEC.decode(ops, encoded));
        assertReaction(reaction, HalfReaction.CODEC.decode(ops, ops.fromBytes(ops.toBytes(encoded))));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            HalfReaction.CODEC.encode(buffer, reaction);
            assertReaction(reaction, HalfReaction.CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    private static void assertReaction(HalfReaction expected, HalfReaction actual) {
        assertEquals(expected.ions(), actual.ions());
        assertEquals(expected.requiredIons(), actual.requiredIons());
        assertEquals(expected.fuel(), actual.fuel());
        assertEquals(expected.electrolyteType(), actual.electrolyteType());
        assertEquals(expected.charge(), actual.charge());
        var receiver = HalfReaction.builder(expected.electrolyteType()).requiredIons(new IonAmount(Ions.PROTON, 2)).build();
        assertEquals(expected.canSupply(receiver, 1, 1), actual.canSupply(receiver, 1, 1));
        assertEquals(receiver.canSupply(expected, 1, 1), receiver.canSupply(actual, 1, 1));
    }
}
