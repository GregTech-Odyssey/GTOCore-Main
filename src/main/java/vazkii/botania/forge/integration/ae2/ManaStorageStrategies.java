package vazkii.botania.forge.integration.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.Arrays;
import java.util.function.Supplier;

final class ManaStorageStrategies {

    private static final boolean HAS_STATS = Arrays.stream(StackTransferContext.class.getMethods()).anyMatch(m -> m.getName().equals("getStats"));

    private ManaStorageStrategies() {}

    static ExternalStorageStrategy external(ServerLevel level, BlockPos pos, Direction side) {
        var lookup = new ManaReceiverLookup(level, pos, side);
        var busPos = pos.relative(side);
        var busSide = side.getOpposite();
        return (extractableOnly, callback) -> {
            var receiver = lookup.find();
            return receiver == null ? null : new ReceiverStorage(receiver, callback, () -> busGrid(level, busPos, busSide));
        };
    }

    @Nullable
    private static IGrid busGrid(ServerLevel level, BlockPos pos, Direction side) {
        var host = GridHelper.getNodeHost(level, pos);
        var node = host == null ? null : host.getGridNode(side);
        return node == null ? null : node.getGrid();
    }

    static StackImportStrategy importer(ServerLevel level, BlockPos pos, Direction side) {
        return new Importer(new ManaReceiverLookup(level, pos, side));
    }

    static StackExportStrategy exporter(ServerLevel level, BlockPos pos, Direction side) {
        return new Exporter(new ManaReceiverLookup(level, pos, side));
    }

    private static void count(StackTransferContext context, long amount, boolean imported) {
        if (HAS_STATS && amount > 0) {
            if (imported) {
                context.getStats().add(ManaKey.KEY, amount);
            } else {
                context.getStats().remove(ManaKey.KEY, amount);
            }
        }
    }

    private record ReceiverStorage(ManaReceiver receiver, Runnable callback, Supplier<IGrid> busGrid) implements MEStorage {

        private boolean ownNetwork() {
            return receiver instanceof FluixManaPoolBlockEntity pool && pool.isOnGrid(busGrid.get());
        }

        @Override
        public int operationTime() {
            return 1;
        }

        @Override
        public boolean supportsKeyType(appeng.api.stacks.AEKeyType<?> type) {
            return type == ManaKeyType.TYPE;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (what != ManaKey.KEY || ownNetwork()) {
                return 0;
            }
            long inserted = ManaAccess.insert(receiver, amount, mode == Actionable.SIMULATE);
            if (inserted > 0 && mode == Actionable.MODULATE) {
                callback.run();
            }
            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (what != ManaKey.KEY || ownNetwork()) {
                return 0;
            }
            long extracted = ManaAccess.extract(receiver, amount, mode == Actionable.SIMULATE);
            if (extracted > 0 && mode == Actionable.MODULATE) {
                callback.run();
            }
            return extracted;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            if (ownNetwork()) {
                return;
            }
            long stored = ManaAccess.stored(receiver);
            if (stored > 0) {
                out.add(ManaKey.KEY, stored);
            }
        }

        @Override
        public Component getDescription() {
            var level = receiver.getManaReceiverLevel();
            var pos = receiver.getManaReceiverPos();
            return level != null && pos != null ? level.getBlockState(pos).getBlock().getName() : Component.translatable(ManaKeyType.NAME_KEY);
        }
    }

    private record Importer(ManaReceiverLookup lookup) implements StackImportStrategy {

        @Override
        public boolean transfer(StackTransferContext context) {
            if (!context.isKeyTypeEnabled(ManaKeyType.TYPE) || context.isInFilter(ManaKey.KEY) == context.isInverted()) {
                return false;
            }
            var receiver = lookup.find();
            if (receiver == null) {
                return false;
            }
            int perOperation = ManaKeyType.TYPE.getAmountPerOperation();
            long budget = (long) context.getOperationsRemaining() * perOperation;
            long available = ManaAccess.extract(receiver, budget, true);
            if (available <= 0) {
                return false;
            }
            var inventory = context.getInternalStorage().getInventory();
            var source = context.getActionSource();
            long fits = inventory.insert(ManaKey.KEY, available, Actionable.SIMULATE, source);
            if (fits <= 0) {
                return false;
            }
            long taken = ManaAccess.extract(receiver, fits, false);
            if (taken <= 0) {
                return false;
            }
            long inserted = StorageHelper.poweredInsert(context.getEnergySource(), inventory, ManaKey.KEY, taken, source);
            if (inserted < taken) {
                ManaAccess.insert(receiver, taken - inserted, false);
            }
            if (inserted > 0) {
                count(context, inserted, true);
                context.reduceOperationsRemaining(Math.max(1, inserted / perOperation));
            }
            return false;
        }
    }

    private record Exporter(ManaReceiverLookup lookup) implements StackExportStrategy {

        @Override
        public long transfer(StackTransferContext context, AEKey what, long maxAmount) {
            if (what != ManaKey.KEY) {
                return 0;
            }
            var receiver = lookup.find();
            if (receiver == null) {
                return 0;
            }
            long space = ManaAccess.insert(receiver, maxAmount, true);
            if (space <= 0) {
                return 0;
            }
            var inventory = context.getInternalStorage().getInventory();
            var source = context.getActionSource();
            long extracted = StorageHelper.poweredExtraction(context.getEnergySource(), inventory, ManaKey.KEY, space, source);
            if (extracted <= 0) {
                return 0;
            }
            long inserted = ManaAccess.insert(receiver, extracted, false);
            if (inserted < extracted) {
                inventory.insert(ManaKey.KEY, extracted - inserted, Actionable.MODULATE, source);
            }
            count(context, inserted, false);
            return inserted;
        }

        @Override
        public long push(AEKey what, long maxAmount, Actionable mode) {
            if (what != ManaKey.KEY) {
                return 0;
            }
            var receiver = lookup.find();
            return receiver == null ? 0 : ManaAccess.insert(receiver, maxAmount, mode == Actionable.SIMULATE);
        }
    }
}
