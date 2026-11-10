package com.gtocore.integration.ae;

import com.gtolib.api.ae2.BlockingPatternTarget;
import com.gtolib.api.ae2.BlockingType;
import com.gtolib.api.ae2.IPatternProviderLogic;
import com.gtolib.api.ae2.PatternPushRecord;

import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidView;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeItemView;
import com.gregtechceu.gtceu.api.transfer.key.KeyTarget;
import com.gregtechceu.gtceu.api.transfer.key.MEStorageKeyView;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyHandlerList;
import com.gregtechceu.gtceu.api.transfer.key.KeyHandlerView;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeySet;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.KeyTypedStorage;
import appeng.api.storage.MEStorage;
import appeng.api.storage.MEStorageHost;
import appeng.api.storage.StorageAccess;
import appeng.helpers.patternprovider.MEStorageTarget;
import appeng.me.storage.DelegatingMEInventory;
import appeng.me.storage.MEInventoryHandler;

import com.gto.datasynclib.util.holder.ObjHolder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class PatternBlockingIdentityTest {

    private static final Type ITEMS = new Type("items");
    private static final Type FLUIDS = new Type("fluids");
    private static final Key IRON = new Key(ITEMS);
    private static final Key WATER = new Key(FLUIDS);
    private static final GenericStack[] NO_OUTPUTS = new GenericStack[0];
    private static Unsafe unsafe;
    private static Method record;
    private static Method blocks;
    private static Field targetRecord;

    @BeforeAll
    static void findMembers() throws ReflectiveOperationException {
        Field instance = Unsafe.class.getDeclaredField("theUnsafe");
        instance.setAccessible(true);
        unsafe = (Unsafe) instance.get(null);
        record = method("record", BlockEntity.class, Direction.class, IPatternDetails.class);
        blocks = method("blocks", BlockEntity.class, Direction.class, IPatternDetails.class);
        targetRecord = BlockingPatternTarget.class.getDeclaredField("record");
        targetRecord.setAccessible(true);
    }

    @Test
    void differentInventoriesWithSameIngredientStayIndependent() throws ReflectiveOperationException {
        Memory first = new Memory(IRON, 1);
        Memory second = new Memory(IRON, 1);
        Host host = host(first, second);
        PatternPushRecord state = new PatternPushRecord();
        Pattern previous = new Pattern(IRON);
        Pattern next = new Pattern(IRON);
        record(state, host, Direction.NORTH, previous);

        assertTrue(blocks(state, host, Direction.NORTH, next));
        assertFalse(blocks(state, host, Direction.SOUTH, next));
    }

    @Test
    void sameInventorySharesRecordAcrossFaces() throws ReflectiveOperationException {
        Memory shared = new Memory(IRON, 1);
        Host host = host(shared, shared);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));

        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
        shared.amount = 0;
        assertFalse(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void separateGtViewsShareTheirUnderlyingInventory() throws ReflectiveOperationException {
        Memory shared = new Memory(IRON, 1);
        Host host = host(new KeyHandlerView<>(shared, false, true), new KeyHandlerView<>(shared, true, false));
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));

        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void partiallyOverlappingListsOnlyCheckSharedLeaf() throws ReflectiveOperationException {
        Memory x = new Memory(IRON, 1);
        Memory y = new Memory(IRON, 0);
        Memory z = new Memory(IRON, 1);
        Host host = host(new KeyHandlerList<>(ITEMS, x, y), new KeyHandlerList<>(ITEMS, y, z));
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        Pattern next = new Pattern(IRON);

        assertFalse(blocks(state, host, Direction.SOUTH, next));
        y.amount = 1;
        assertTrue(blocks(state, host, Direction.SOUTH, next));
        y.amount = 0;
        assertFalse(blocks(state, host, Direction.SOUTH, next));
        assertEquals(1, x.amount);
        assertEquals(1, z.amount);
    }

    @Test
    void unrelatedFluidReplacementKeepsItemRecord() throws ReflectiveOperationException {
        Channels storage = new Channels(new Memory(IRON, 1), new Memory(WATER, 1));
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));

        storage.fluid = new Memory(WATER, 1);

        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void samePatternRefillRestoresPreviouslyDrainedChannel() throws ReflectiveOperationException {
        Memory iron = new Memory(IRON, 1);
        Memory water = new Memory(WATER, 1);
        Channels storage = new Channels(iron, water);
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        Pattern previous = new Pattern(IRON, WATER);
        record(state, host, Direction.NORTH, previous);
        iron.amount = 0;
        Logic logic = new Logic(previous);
        BlockingPatternTarget target = target(state, host, storage, logic);

        assertEquals(1, target.insert(IRON, 1, Actionable.MODULATE));
        target.record(previous);
        water.amount = 0;

        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON, WATER)));
    }

    @Test
    void switchingToWaterOnlyDropsPreviousItemIngredients() throws ReflectiveOperationException {
        Memory iron = new Memory(IRON, 1);
        Memory water = new Memory(WATER, 1);
        Channels storage = new Channels(iron, water);
        Host host = host(storage, iron);
        PatternPushRecord state = new PatternPushRecord();
        Pattern next = new Pattern(IRON, WATER);
        record(state, host, Direction.NORTH, new Pattern(IRON, WATER));
        assertTrue(blocks(state, host, Direction.SOUTH, next));

        record(state, host, Direction.NORTH, new Pattern(WATER));

        assertFalse(blocks(state, host, Direction.SOUTH, next));
        assertTrue(blocks(state, host, Direction.NORTH, next));
        water.amount = 0;
        assertFalse(blocks(state, host, Direction.NORTH, next));
        assertEquals(1, iron.amount);
    }

    @Test
    void switchingBackToIronRestoresItsBlockingRecord() throws ReflectiveOperationException {
        Memory iron = new Memory(IRON, 1);
        Memory water = new Memory(WATER, 1);
        Channels storage = new Channels(iron, water);
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        Pattern next = new Pattern(IRON, WATER);
        record(state, host, Direction.NORTH, new Pattern(IRON, WATER));
        record(state, host, Direction.NORTH, new Pattern(WATER));
        water.amount = 0;
        assertFalse(blocks(state, host, Direction.SOUTH, next));

        record(state, host, Direction.NORTH, new Pattern(IRON));

        assertTrue(blocks(state, host, Direction.SOUTH, next));
        iron.amount = 0;
        assertFalse(blocks(state, host, Direction.SOUTH, next));
    }

    @Test
    void stableWrapperChangingResourceIdentityDropsOldRecord() throws ReflectiveOperationException {
        Wrapper storage = new Wrapper(new Memory(IRON, 1));
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        Pattern next = new Pattern(IRON);
        assertTrue(blocks(state, host, Direction.NORTH, next));

        storage.delegate = new Memory(IRON, 1);

        assertFalse(blocks(state, host, Direction.NORTH, next));
    }

    @Test
    void stableHandlerChangingUnrestrictedIdentityDropsOldRecord() throws ReflectiveOperationException {
        Memory storage = new Memory(IRON, 1);
        storage.identity = new Memory(IRON, 1);
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        Pattern next = new Pattern(IRON);
        assertTrue(blocks(state, host, Direction.NORTH, next));

        storage.identity = new Memory(IRON, 1);

        assertFalse(blocks(state, host, Direction.NORTH, next));
    }

    @Test
    void delegatingInventoriesWithNullNativeIdentityShareUnderlyingStorage() throws ReflectiveOperationException {
        Memory shared = new Memory(IRON, 1);
        DelegatingMEInventory first = new DelegatingMEInventory(shared);
        DelegatingMEInventory second = new DelegatingMEInventory(shared);
        assertNull(first.getResourceIdentity());
        assertNull(second.getResourceIdentity());
        Host host = host(first, second);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));

        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void stableDelegatingInventoryDropsRecordAfterDelegateChanges() throws ReflectiveOperationException {
        DelegatingMEInventory storage = new DelegatingMEInventory(new Memory(IRON, 1));
        assertNull(storage.getResourceIdentity());
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        Pattern next = new Pattern(IRON);
        assertTrue(blocks(state, host, Direction.NORTH, next));

        storage.setDelegate(new Memory(IRON, 1));

        assertFalse(blocks(state, host, Direction.NORTH, next));
    }

    @Test
    void batchPreparationOnlyRunsBeforeFirstInput() throws ReflectiveOperationException {
        Memory iron = new Memory(IRON, 1);
        Memory water = new Memory(WATER, 0);
        Channels storage = new Channels(iron, water);
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        water.afterInsert = () -> iron.amount = 0;
        BlockingPatternTarget target = target(state, host, storage, new Logic(new Pattern(WATER, IRON)));

        assertEquals(1, target.insert(WATER, 1, Actionable.MODULATE));
        assertEquals(0, iron.amount);
        assertEquals(1, target.insert(IRON, 1, Actionable.MODULATE));

        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void cachedTargetPreparesAgainWhenNextBatchStarts() throws ReflectiveOperationException {
        Memory iron = new Memory(IRON, 1);
        Channels storage = new Channels(iron, new Memory(WATER, 0));
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        Logic logic = new Logic(new Pattern(WATER));
        MEStorageTarget adapter = new MEStorageTarget(storage, IActionSource.empty());
        BlockingPatternTarget target = target(state, host, adapter, logic);
        target.insert(WATER, 1, Actionable.MODULATE);
        iron.amount = 0;

        assertSame(target, BlockingPatternTarget.of(target, adapter, host, logic, Direction.NORTH));
        target.insert(WATER, 1, Actionable.MODULATE);
        iron.amount = 1;

        assertFalse(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void parallelModeContinuesCurrentBatchAndBlocksCachedNextBatch() throws ReflectiveOperationException {
        Memory storage = new Memory(IRON, 0);
        Host host = host(storage, storage);
        PatternPushRecord state = new PatternPushRecord();
        Pattern pattern = new Pattern(IRON);
        Logic logic = new Logic(pattern);
        logic.mode = BlockingType.PARALLEL;
        MEStorageTarget adapter = new MEStorageTarget(storage, IActionSource.empty());
        BlockingPatternTarget target = target(state, host, adapter, logic);
        AEKeySet<AEKey> keys = new AEKeySet<>(1);
        keys.add(IRON);
        assertFalse(target.containsPatternInput(keys));

        target.insert(IRON, 1, Actionable.MODULATE);
        target.record(pattern);

        assertFalse(target.containsPatternInput(keys));
        assertSame(target, BlockingPatternTarget.of(target, adapter, host, logic, Direction.NORTH));
        assertTrue(target.containsPatternInput(keys));
    }

    @Test
    void separateMeStorageViewsShareBackingStorage() throws ReflectiveOperationException {
        Memory shared = new Memory(IRON, 1);
        var exposed = new DelegatingMEInventory(shared);
        Host remote = host(exposed, exposed);
        var first = KeyTarget.resolve(remote, Direction.NORTH, ITEMS, StorageAccess.EXTRACT);
        var second = KeyTarget.resolve(remote, Direction.SOUTH, ITEMS, StorageAccess.EXTRACT);
        assertNotSame(first, second);
        assertInstanceOf(MEStorageKeyView.class, first);
        assertSame(((MEStorageKeyView<?>) first).getStorage(), ((MEStorageKeyView<?>) second).getStorage());
        Host host = host(first, second);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void meStorageViewKeepsMarkRestriction() throws ReflectiveOperationException {
        Memory shared = new Memory(IRON, 1);
        Key mark = new Key(ITEMS);
        var first = new MEStorageKeyView<>(shared, ITEMS);
        var second = new MEStorageKeyView<>(shared, ITEMS).marked(mark);
        Host host = host(first, second);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        assertFalse(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void forgeItemViewsShareBackingHandlerIdentity() throws ReflectiveOperationException {
        var handler = (IItemHandler) Proxy.newProxyInstance(PatternBlockingIdentityTest.class.getClassLoader(), new Class<?>[] { IItemHandler.class }, (proxy, method, args) -> { throw new AssertionError("Identity probing must not access inventory contents"); });
        var first = ForgeItemView.of(handler);
        var second = ForgeItemView.of(handler);
        assertNotSame(first, second);
        assertSame(identityToken(first, AEKeyTypes.ITEMS), identityToken(second, AEKeyTypes.ITEMS));
    }

    @Test
    void forgeFluidViewsShareBackingHandlerIdentity() throws ReflectiveOperationException {
        var handler = (IFluidHandler) Proxy.newProxyInstance(PatternBlockingIdentityTest.class.getClassLoader(), new Class<?>[] { IFluidHandler.class }, (proxy, method, args) -> { throw new AssertionError("Identity probing must not access inventory contents"); });
        var first = ForgeFluidView.of(handler);
        var second = ForgeFluidView.of(handler);
        assertNotSame(first, second);
        assertSame(identityToken(first, AEKeyTypes.FLUIDS), identityToken(second, AEKeyTypes.FLUIDS));
    }

    private static Object identityToken(MEStorage storage, AEKeyType type) throws ReflectiveOperationException {
        Class<?> identity = Class.forName("com.gtolib.api.ae2.PatternTargetIdentity");
        var constructor = identity.getDeclaredConstructor(BlockEntity.class, Direction.class);
        constructor.setAccessible(true);
        var copy = identity.getDeclaredMethod("copyTokens", AEKeyType.class, ObjectArrayList.class);
        copy.setAccessible(true);
        var tokens = new ObjectArrayList<Object>();
        copy.invoke(constructor.newInstance(host(storage, storage), Direction.NORTH), type, tokens);
        assertEquals(1, tokens.size());
        return tokens.get(0);
    }

    @Test
    void restrictedPushKeepsOtherFacesVisibleIngredients() throws ReflectiveOperationException {
        Key copper = new Key(ITEMS);
        Memory iron = new Memory(IRON, 1);
        Memory copperStorage = new Memory(copper, 0);
        KeyHandlerList<Key> shared = new KeyHandlerList<>(ITEMS, iron, copperStorage);
        var narrow = new MEInventoryHandler(shared);
        narrow.setAllowExtraction(false);
        Host host = host(shared, narrow);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        Pattern copperPattern = new Pattern(copper);
        BlockingPatternTarget target = target(state, host, new MEStorageTarget(narrow, IActionSource.empty()), new Logic(copperPattern), Direction.SOUTH);

        assertEquals(1, target.insert(copper, 1, Actionable.MODULATE));
        target.record(copperPattern);
        assertEquals(1, copperStorage.amount);
        copperStorage.amount = 0;

        assertTrue(blocks(state, host, Direction.NORTH, new Pattern(IRON)));
        assertEquals(1, iron.amount);
    }

    @Test
    void replacingSourceFaceKeepsOtherFacesSharedRecord() throws ReflectiveOperationException {
        Memory shared = new Memory(IRON, 1);
        Host host = host(shared, shared);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));

        host.sides[Direction.NORTH.ordinal()] = new Memory(IRON, 0);

        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
        shared.amount = 0;
        assertFalse(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void pushingAfterSourceReplacementKeepsUnvisitedSharedFaceHistory() throws ReflectiveOperationException {
        Memory shared = new Memory(IRON, 1);
        Memory replacement = new Memory(IRON, 0);
        Host host = host(shared, shared);
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        host.sides[Direction.NORTH.ordinal()] = replacement;
        Pattern next = new Pattern(IRON);
        BlockingPatternTarget target = target(state, host, replacement, new Logic(next));

        assertEquals(1, target.insert(IRON, 1, Actionable.MODULATE));
        target.record(next);
        replacement.amount = 0;

        assertTrue(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
        shared.amount = 0;
        assertFalse(blocks(state, host, Direction.SOUTH, new Pattern(IRON)));
    }

    @Test
    void partiallyRetargetedFaceKeepsHistoryOnlyOnDetachedLeaf() throws ReflectiveOperationException {
        Memory x = new Memory(IRON, 1);
        Memory y = new Memory(IRON, 0);
        Memory z = new Memory(IRON, 0);
        Host host = host(new KeyHandlerList<>(ITEMS, x, y), x);
        PatternPushRecord state = new PatternPushRecord();
        Pattern previous = new Pattern(IRON);
        record(state, host, Direction.NORTH, previous);
        var replacement = new KeyHandlerList<>(ITEMS, y, z);
        host.sides[Direction.NORTH.ordinal()] = replacement;
        Pattern next = new Pattern(IRON);
        BlockingPatternTarget target = target(state, host, replacement, new Logic(next));

        assertEquals(1, target.insert(IRON, 1, Actionable.MODULATE));
        target.record(next);

        assertTrue(blocks(state, host, Direction.SOUTH, next));
        assertFalse(blocks(state, host, Direction.SOUTH, previous));
        assertTrue(blocks(state, host, Direction.NORTH, previous));
        y.amount = 0;
        assertFalse(blocks(state, host, Direction.NORTH, previous));
        assertTrue(blocks(state, host, Direction.SOUTH, next));
    }

    @Test
    void unvisitedWideFaceKeepsDetachedHistoryDuringRestrictedPush() throws ReflectiveOperationException {
        Key copper = new Key(ITEMS);
        Memory iron = new Memory(IRON, 1);
        Memory copperStorage = new Memory(copper, 0);
        var shared = new KeyHandlerList<>(ITEMS, iron, copperStorage);
        var narrow = new MEInventoryHandler(shared);
        narrow.setAllowExtraction(false);
        Host host = host(shared, narrow);
        host.sides[Direction.EAST.ordinal()] = shared;
        PatternPushRecord state = new PatternPushRecord();
        record(state, host, Direction.NORTH, new Pattern(IRON));
        Pattern next = new Pattern(copper);
        assertFalse(blocks(state, host, Direction.SOUTH, next));
        host.sides[Direction.NORTH.ordinal()] = new Memory(IRON, 0);
        BlockingPatternTarget target = target(state, host, new MEStorageTarget(narrow, IActionSource.empty()), new Logic(next), Direction.SOUTH);

        assertEquals(1, target.insert(copper, 1, Actionable.MODULATE));
        target.record(next);
        copperStorage.amount = 0;

        assertTrue(blocks(state, host, Direction.EAST, new Pattern(IRON)));
        assertEquals(1, iron.amount);
    }

    private static Host host(MEStorage north, MEStorage south) throws InstantiationException {
        // The resolver only needs MEStorageHost; skip Minecraft's world and block constructor state.
        Host host = (Host) unsafe.allocateInstance(Host.class);
        host.sides = new MEStorage[6];
        host.sides[Direction.NORTH.ordinal()] = north;
        host.sides[Direction.SOUTH.ordinal()] = south;
        return host;
    }

    private static BlockingPatternTarget target(PatternPushRecord state, Host host, MEStorage storage, Logic logic) throws IllegalAccessException {
        return target(state, host, new MEStorageTarget(storage, IActionSource.empty()), logic);
    }

    private static BlockingPatternTarget target(PatternPushRecord state, Host host, MEStorageTarget adapter, Logic logic) throws IllegalAccessException {
        return target(state, host, adapter, logic, Direction.NORTH);
    }

    private static BlockingPatternTarget target(PatternPushRecord state, Host host, MEStorageTarget adapter, Logic logic, Direction side) throws IllegalAccessException {
        BlockingPatternTarget target = BlockingPatternTarget.of(null, adapter, host, logic, side);
        targetRecord.set(target, state);
        return target;
    }

    private static void record(PatternPushRecord state, Host host, Direction side, Pattern pattern) throws ReflectiveOperationException {
        record.invoke(state, host, side, pattern);
    }

    private static boolean blocks(PatternPushRecord state, Host host, Direction side, Pattern pattern) throws ReflectiveOperationException {
        return (boolean) blocks.invoke(state, host, side, pattern);
    }

    private static Method method(String name, Class<?>... arguments) throws NoSuchMethodException {
        Method method = PatternPushRecord.class.getDeclaredMethod(name, arguments);
        method.setAccessible(true);
        return method;
    }

    private static final class Host extends BlockEntity implements MEStorageHost {

        private MEStorage[] sides;

        private Host() {
            super(null, null, null);
        }

        @Override
        public boolean isRemoved() {
            return false;
        }

        @Override
        public MEStorage getMEStorage(Direction side) {
            return side == null ? null : sides[side.ordinal()];
        }
    }

    private static final class Memory implements IKeyHandler<Key> {

        private final Key key;
        private long amount;
        private Memory identity;
        private Runnable afterInsert;

        private Memory(Key key, long amount) {
            this.key = key;
            this.amount = amount;
        }

        @Override
        public AEKeyType keyType() {
            return key.getType();
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean fixedSize() {
            return true;
        }

        @Override
        public Key keyAt(int slot) {
            return amount == 0 ? null : key;
        }

        @Override
        public long amountAt(int slot) {
            return amount;
        }

        @Override
        public long slotLimit(int slot) {
            return Long.MAX_VALUE;
        }

        @Override
        public long insert(int slot, Key key, long amount, boolean simulate) {
            if (key != this.key) return 0;
            if (!simulate) {
                this.amount += amount;
                if (afterInsert != null) afterInsert.run();
            }
            return amount;
        }

        @Override
        public long extract(int slot, Key key, long amount, boolean simulate) {
            if (key != this.key) return 0;
            long extracted = Math.min(this.amount, amount);
            if (!simulate) this.amount -= extracted;
            return extracted;
        }

        @Override
        public IKeyHandler<Key> unrestricted() {
            return identity == null ? this : identity;
        }
    }

    private static final class Channels implements KeyTypedStorage {

        private final Memory item;
        private Memory fluid;

        private Channels(Memory item, Memory fluid) {
            this.item = item;
            this.fluid = fluid;
        }

        @Override
        public boolean supportsKeyType(AEKeyType type) {
            return type == ITEMS || type == FLUIDS;
        }

        @Override
        public MEStorage forKeyType(AEKeyType type) {
            return type == ITEMS ? item : type == FLUIDS ? fluid : null;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return forKeyType(what.getType()).insert(what, amount, mode, source);
        }

        @Override
        public Component getDescription() {
            return null;
        }
    }

    private static final class Wrapper implements KeyTypedStorage {

        private Memory delegate;

        private Wrapper(Memory delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object getResourceIdentity() {
            return delegate;
        }

        @Override
        public boolean supportsKeyType(AEKeyType type) {
            return type == ITEMS;
        }

        @Override
        public MEStorage forKeyType(AEKeyType type) {
            return supportsKeyType(type) ? this : null;
        }

        @Override
        public boolean containsAny(Set<AEKey> keys) {
            return delegate.containsAny(keys);
        }

        @Override
        public Component getDescription() {
            return null;
        }
    }

    private static final class Logic implements IPatternProviderLogic {

        private BlockingType mode = BlockingType.NON_CONTAIN;
        private final Pattern pattern;

        private Logic(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public BlockingType gtolib$getBlocking() {
            return mode;
        }

        @Override
        public IPatternDetails gtolib$getCurrentPattern() {
            return pattern;
        }

        @Override
        public PushResult gtolib$pushPattern(IPatternDetails pattern, ObjHolder<KeyCounter[]> inputs, Supplier<PushResult> success) {
            throw new AssertionError("Identity tests must not push through a grid");
        }
    }

    private static final class Pattern implements IPatternDetails {

        private final IInput[] inputs;

        private Pattern(Key... keys) {
            inputs = new IInput[keys.length];
            for (int i = 0; i < keys.length; i++) inputs[i] = new Input(new GenericStack[] { new GenericStack(keys[i], 1) });
        }

        @Override
        public AEItemKey getDefinition() {
            return null;
        }

        @Override
        public IInput[] getInputs() {
            return inputs;
        }

        @Override
        public GenericStack[] getOutputs() {
            return NO_OUTPUTS;
        }
    }

    private record Input(GenericStack[] candidates) implements IPatternDetails.IInput {

        @Override
        public GenericStack[] getPossibleInputs() {
            return candidates;
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input == candidates[0].what();
        }

        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }

    private static final class Type extends AEKeyType<Key> {

        private Type(String name) {
            super(ResourceLocation.fromNamespaceAndPath("test", name), Key.class, null);
        }

        @Override
        public Key readFromPacket(FriendlyByteBuf input) {
            throw new AssertionError("No packet decoding in identity tests");
        }

        @Override
        public Key loadKeyFromTag(CompoundTag tag) {
            throw new AssertionError("No saved data decoding in identity tests");
        }
    }

    private static final class Key extends AEKey {

        private final Type type;

        private Key(Type type) {
            this.type = type;
        }

        @Override
        public AEKeyType getType() {
            return type;
        }

        @Override
        public AEKey dropSecondary() {
            return this;
        }

        @Override
        public Object getPrimaryKey() {
            return this;
        }

        @Override
        public ResourceLocation getId() {
            return type.getId();
        }

        @Override
        protected Component computeDisplayName() {
            return null;
        }

        @Override
        public CompoundTag toTag() {
            throw new AssertionError("No saved data encoding in identity tests");
        }

        @Override
        public void writeToPacket(FriendlyByteBuf data) {
            throw new AssertionError("No packet encoding in identity tests");
        }

        @Override
        public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
            throw new AssertionError("No world drops in identity tests");
        }
    }
}
