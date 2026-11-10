package com.gtocore.integration.ae;

import com.gtocore.mixin.ae2.crafting.PatternProviderLogicMixin;

import com.gtolib.api.ae2.IPatternProviderLogic.PushResult;

import net.minecraft.core.Direction;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.helpers.patternprovider.PatternProviderTargetCache;

import com.gto.datasynclib.util.holder.ObjHolder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class PatternProviderPushGuardTest {

    private static final int AVAILABLE_CRAFTS = 3;
    private static final KeyCounter[] INPUTS = new KeyCounter[0];
    private static Method pushTarget;
    private static Field sendList;
    private static Field pushedCount;
    private static Field patternInputs;

    private Logic logic;
    private Pattern pattern;
    private ObjHolder<KeyCounter[]> inputs;

    @BeforeAll
    static void findMembers() throws ReflectiveOperationException {
        pushTarget = PatternProviderLogicMixin.class.getDeclaredMethod("gtolib$pushTarget", IPatternDetails.class,
                ObjHolder.class, Supplier.class, BooleanSupplier.class, Direction.class, PatternProviderTarget.class, boolean.class);
        pushTarget.setAccessible(true);
        sendList = field("sendList");
        pushedCount = field("gtocore$pushedCount");
        patternInputs = field("patternInputs");
    }

    @BeforeEach
    void prepareLogic() throws IllegalAccessException {
        logic = new Logic();
        pattern = new Pattern();
        inputs = new ObjHolder<>(INPUTS);
        sendList.set(logic, logic.pending);
        pushedCount.setInt(logic, AVAILABLE_CRAFTS);
        patternInputs.set(logic, Collections.emptySet());
    }

    @Test
    void lockedProviderDoesNotTouchTarget() throws ReflectiveOperationException {
        logic.locked = LockCraftingMode.LOCK_UNTIL_RESULT;
        Target target = new Target();

        assertEquals(PushResult.REJECTED, push(target));

        assertEquals(0, target.inserts);
        assertEquals(0, target.blockingChecks);
        assertEquals(0, logic.acceptanceChecks);
        assertEquals(0, logic.successes);
        assertEquals(0, logic.callbacks);
    }

    @Test
    void targetLoopStopsWhenFirstSuccessfulPushLocksProvider() throws ReflectiveOperationException {
        logic.lockAfterPush = true;
        Target target = new Target();

        assertEquals(PushResult.SUCCESS, push(target));

        assertEquals(1, target.inserts);
        assertEquals(1, logic.successes);
        assertEquals(1, logic.callbacks);
    }

    @Test
    void nextTargetReceivesNothingAfterFirstPushLocksProvider() throws ReflectiveOperationException {
        logic.lockAfterPush = true;
        Target first = new Target();
        Target second = new Target();

        assertEquals(PushResult.SUCCESS, push(first));
        assertEquals(PushResult.REJECTED, push(second));

        assertEquals(1, first.inserts);
        assertEquals(0, second.inserts);
        assertEquals(0, second.blockingChecks);
        assertEquals(1, logic.successes);
        assertEquals(1, logic.callbacks);
    }

    @Test
    void pendingSendListPreventsTargetPush() throws ReflectiveOperationException {
        logic.pending.add(null);
        Target target = new Target();

        assertEquals(PushResult.REJECTED, push(target));

        assertEquals(0, target.inserts);
        assertEquals(0, target.blockingChecks);
        assertEquals(0, logic.acceptanceChecks);
        assertEquals(0, logic.successes);
    }

    @Test
    void blockingTargetReceivesNothing() throws ReflectiveOperationException {
        Target target = new Target();
        target.blocked = true;

        assertEquals(PushResult.REJECTED, push(target));

        assertEquals(0, target.inserts);
        assertEquals(1, target.blockingChecks);
        assertEquals(0, logic.acceptanceChecks);
        assertEquals(0, logic.successes);
        assertEquals(0, logic.callbacks);
    }

    @Test
    void targetLoopRechecksBlockingAfterEachSuccessfulPush() throws ReflectiveOperationException {
        Target target = new Target();
        target.blockAfterInsert = true;

        assertEquals(PushResult.SUCCESS, push(target));

        assertEquals(1, target.inserts);
        assertEquals(2, target.blockingChecks);
        assertEquals(1, logic.successes);
        assertEquals(1, logic.callbacks);
    }

    @Test
    void lockedProviderDoesNotTouchCraftingMachine() throws ReflectiveOperationException {
        logic.locked = LockCraftingMode.LOCK_UNTIL_RESULT;
        Machine machine = new Machine();

        assertEquals(PushResult.REJECTED, push(machine));

        assertEquals(0, machine.pushes);
        assertEquals(0, machine.acceptanceChecks);
        assertEquals(0, logic.successes);
        assertEquals(0, logic.callbacks);
    }

    @Test
    void craftingMachineLoopStopsWhenFirstSuccessfulPushLocksProvider() throws ReflectiveOperationException {
        logic.lockAfterPush = true;
        Machine machine = new Machine();

        assertEquals(PushResult.SUCCESS, push(machine));

        assertEquals(1, machine.pushes);
        assertEquals(1, machine.acceptanceChecks);
        assertEquals(1, logic.successes);
        assertEquals(1, logic.callbacks);
    }

    @Test
    void nextCraftingMachineReceivesNothingAfterFirstPushLocksProvider() throws ReflectiveOperationException {
        logic.lockAfterPush = true;
        Machine first = new Machine();
        Machine second = new Machine();

        assertEquals(PushResult.SUCCESS, push(first));
        assertEquals(PushResult.REJECTED, push(second));

        assertEquals(1, first.pushes);
        assertEquals(0, second.pushes);
        assertEquals(0, second.acceptanceChecks);
        assertEquals(1, logic.successes);
        assertEquals(1, logic.callbacks);
    }

    @Test
    void pendingSendListPreventsCraftingMachinePush() throws ReflectiveOperationException {
        logic.pending.add(null);
        Machine machine = new Machine();

        assertEquals(PushResult.REJECTED, push(machine));

        assertEquals(0, machine.pushes);
        assertEquals(0, machine.acceptanceChecks);
        assertEquals(0, logic.successes);
        assertEquals(0, logic.callbacks);
    }

    private PushResult push(Target target) throws ReflectiveOperationException {
        return (PushResult) pushTarget.invoke(logic, pattern, inputs, (Supplier<PushResult>) logic::pushed,
                (BooleanSupplier) logic::canPush, Direction.NORTH, target, true);
    }

    private PushResult push(Machine machine) throws ReflectiveOperationException {
        Method method = PatternProviderLogicMixin.class.getDeclaredMethod("gtolib$pushCraftingMachine",
                ICraftingMachine.class, IPatternDetails.class, ObjHolder.class, Supplier.class, Direction.class, BooleanSupplier.class);
        method.setAccessible(true);
        return (PushResult) method.invoke(logic, machine, pattern, inputs, (Supplier<PushResult>) logic::pushed,
                Direction.SOUTH, (BooleanSupplier) logic::canPush);
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = PatternProviderLogicMixin.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static final class Logic extends PatternProviderLogicMixin {

        private final ObjectArrayList<GenericStack> pending = new ObjectArrayList<>();
        private LockCraftingMode locked = LockCraftingMode.NONE;
        private boolean lockAfterPush;
        private int acceptanceChecks;
        private int successes;
        private int callbacks;

        private boolean canPush() {
            return getCraftingLockedReason() == LockCraftingMode.NONE && pending.isEmpty();
        }

        private PushResult pushed() {
            callbacks++;
            return PushResult.SUCCESS;
        }

        @Override
        public LockCraftingMode getCraftingLockedReason() {
            return locked;
        }

        @Override
        protected Set<Direction> getActiveSides() {
            return Collections.emptySet();
        }

        @Override
        public PatternContainerGroup getTerminalGroup() {
            return null;
        }

        @Override
        protected void onPushPatternSuccess(IPatternDetails pattern) {
            successes++;
            if (lockAfterPush) locked = LockCraftingMode.LOCK_UNTIL_RESULT;
        }

        @Override
        protected boolean adapterAcceptsAll(PatternProviderTarget target, KeyCounter[] inputHolder) {
            acceptanceChecks++;
            return true;
        }

        @Override
        protected void addToSendList(AEKey what, long amount) {
            pending.add(null);
        }

        @Override
        public PatternProviderTargetCache getTargetCache(Direction side) {
            throw new AssertionError("Direct push tests must not resolve a world target");
        }
    }

    private static final class Pattern implements IPatternDetails {

        private static final IInput[] NO_INPUTS = new IInput[0];
        private static final GenericStack[] NO_OUTPUTS = new GenericStack[0];

        @Override
        public AEItemKey getDefinition() {
            return null;
        }

        @Override
        public IInput[] getInputs() {
            return NO_INPUTS;
        }

        @Override
        public GenericStack[] getOutputs() {
            return NO_OUTPUTS;
        }

        @Override
        public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
            // A null key keeps this guard test independent of Minecraft item registries.
            inputSink.pushInput(null, 1);
        }
    }

    private static final class Target implements PatternProviderTarget {

        private boolean blocked;
        private boolean blockAfterInsert;
        private int inserts;
        private int blockingChecks;

        @Override
        public long insert(AEKey what, long amount, Actionable type) {
            assertEquals(Actionable.MODULATE, type);
            inserts++;
            if (blockAfterInsert) blocked = true;
            return amount;
        }

        @Override
        public boolean containsPatternInput(Set<AEKey> patternInputs) {
            blockingChecks++;
            assertNotNull(patternInputs);
            return blocked;
        }
    }

    private static final class Machine implements ICraftingMachine {

        private int pushes;
        private int acceptanceChecks;

        @Override
        public PatternContainerGroup getCraftingMachineInfo() {
            return null;
        }

        @Override
        public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs, Direction ejectionDirection) {
            pushes++;
            return true;
        }

        @Override
        public boolean acceptsPlans() {
            acceptanceChecks++;
            return pushes < AVAILABLE_CRAFTS;
        }
    }
}
