package com.gtocore.common.machine.mana.multiblock;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.data.IdleReason;
import com.gtocore.integration.botania.IClientPylon;
import com.gtocore.utils.StxckUtil;

import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.ManaDistributorMachine;
import com.gtolib.api.machine.mana.trait.ManaTrait;
import com.gtolib.api.misc.ManaContainerList;
import com.gtolib.api.recipe.extension.MANATRecipeExtension;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.PlanScratch;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import com.gto.datasynclib.datastream.DataComponentKey;
import com.gto.recipesearch.IntLongMap;
import mythicbotany.pylon.BlockAlfsteelPylon;
import mythicbotany.register.ModBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.PylonBlock;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.handler.BotaniaSounds;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ManaFlowAssembler extends ManaMultiblockMachine {

    private static final DataComponentKey<AtomicInteger> MAX_RATE = DataComponentKey.createNoCodec("maxRate");
    private static final DataComponentKey<List<BlockPos>> POOL = DataComponentKey.createNoCodec("manaPool");

    private final static int SIZE = 9;
    private final ItemEntityRecipeHandler itemIn = new ItemEntityRecipeHandler();
    private int maxRate = 0;
    private final List<WeakReference<ManaPoolBlockEntity>> manaPools = new ArrayList<>();
    private final InWorldManaContainer inWorldManaContainer = new InWorldManaContainer();
    private ManaContainerList manaContainerList = ManaContainerList.EMPTY;
    private TickableSubscription tickSubscription;
    private TickableSubscription clientTickSubscription;
    private final EnumMap<Direction, Integer> colors = new EnumMap<>(Direction.class);

    public ManaFlowAssembler(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        addHandlerList(RecipeHandlerUnit.of(IO.IN, itemIn));
        addHandlerList(RecipeHandlerUnit.of(IO.OUT, itemIn));

        var f = getMultiblockState().getMatchContext().get(MAX_RATE);
        maxRate = f == null ? 0 : f.get();
        manaPools.clear();
        var f1 = getMultiblockState().getMatchContext().get(POOL);
        var poolPositions = f1 == null ? Collections.<BlockPos>emptyList() : f1;
        var level = getLevel();
        if (level != null) {
            for (var pos : poolPositions) {
                if (!level.isLoaded(pos)) {
                    continue;
                }
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ManaPoolBlockEntity manaPool) {
                    manaPools.add(new WeakReference<>(manaPool));
                }
            }
        }
        manaContainerList = new ManaContainerList(inWorldManaContainer);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        var level = getLevel();
        if (level != null) {
            level.playSound(null, getPos().getX(), getPos().above().getY(), getPos().getZ(), BotaniaSounds.terrasteelCraft, SoundSource.BLOCKS, 1F, 1F);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        maxRate = 0;
        manaPools.clear();
        manaContainerList = ManaContainerList.EMPTY;
        colors.clear();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tickSubscription = subscribeServerTick(() -> getRecipeLogic().updateTickSubscription(), 20);
        if (isRemote()) {
            clientTickSubscription = subscribeClientTick(() -> {
                if (isActive() && getLevel() != null) {
                    var center = getPos().above(4).getCenter();
                    var dir = Direction.NORTH;
                    for (int i = 0; i < 4; i++) {
                        var pylonPos = getPos().above(3).relative(dir, 2);
                        dir = dir.getClockWise();
                        pylonPos = pylonPos.relative(dir, 2);
                        BlockPos finalPylonPos = pylonPos;

                        int color = colors.computeIfAbsent(dir, d -> {
                            var blockState = getLevel().getBlockState(finalPylonPos);
                            if (blockState.getBlock() instanceof PylonBlock pb) {
                                return (int) (pb.variant.r * 255 + pb.variant.g * 255 * 256 + pb.variant.b * 255 * 256 * 256);
                            }
                            return 15629312;
                        });

                        IClientPylon.particle(pylonPos, getOffsetTimer(), center, getLevel(), color);
                    }
                }
            }, 1);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        maxRate = 0;
        manaPools.clear();
        manaContainerList = ManaContainerList.EMPTY;
        colors.clear();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        if (clientTickSubscription != null) {
            clientTickSubscription.unsubscribe();
            clientTickSubscription = null;
        }
    }

    @Override
    public void customText(List<Component> textList) {
        for (var trait : getMultiblockTraits()) {
            if (!(trait instanceof ManaTrait)) trait.customText(textList);
        }
        if (MultiblockPage.isScreenText()) return;
        var manaText = Component.literal("∞");
        if (!inWorldManaContainer.hasCreativePool()) {
            manaText = Component.literal(FormattingUtil.formatNumbers(manaContainerList.getCurrentMana()))
                    .append(" / ")
                    .append(FormattingUtil.formatNumbers(manaContainerList.getMaxMana()));
        }
        textList.add(Component.translatable("gtocore.machine.mana_stored", manaText));
        var consumptionText = Component.literal(FormattingUtil.formatNumbers(manaContainerList.getMaxIORate())).append(" /t");
        textList.add(Component.translatable("gtocore.machine.mana_consumption", consumptionText));
    }

    private static final Component INFINITE_MANA = Component.literal("∞");

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        for (var trait : getMultiblockTraits()) {
            if (!(trait instanceof ManaTrait)) trait.addScreenReadouts(page);
        }
        var stored = MultiblockPage.fractionText(() -> manaContainerList.getCurrentMana(), () -> manaContainerList.getMaxMana(), "");
        page.addReading("gtocore.machine.mana_stored", () -> inWorldManaContainer.hasCreativePool() ? INFINITE_MANA : stored.get());
        page.addReading("gtocore.machine.mana_consumption", MultiblockPage.numberText(() -> manaContainerList.getMaxIORate(), "/t"));
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, GTRecipe recipe) {
        if (recipe.eut != 0 || maxRate == 0) {
            if (recipe.eut != 0) IdleReason.NOT_APPLICABLE.setReason(this);
            else IdleReason.MANA_FLOW_TOO_WEAK.setReason(this, (recipe.duration * MANATRecipeExtension.getMANAt(recipe) + 199) / 200, 0);
            return null;
        }
        int duration = Math.toIntExact(recipe.duration * MANATRecipeExtension.getMANAt(recipe) / maxRate);
        if (duration > 200) {
            IdleReason.MANA_FLOW_TOO_WEAK.setReason(this, (recipe.duration * MANATRecipeExtension.getMANAt(recipe) + 199) / 200, maxRate);
            return null;
        }
        recipe.duration = 200;
        MANATRecipeExtension.setMANAt(recipe, maxRate);
        return super.getRealRecipe(unit, recipe);
    }

    @Override
    public @NotNull ManaContainerList getManaContainer() {
        return manaContainerList;
    }

    private List<ItemEntity> getItemEntitiesAbove() {
        var level = getHolder().getLevel();
        if (level == null) {
            return Collections.emptyList();
        }
        var pos = getPos().above(2);
        var aabb = new AABB(pos).inflate(1);
        int[] counter = new int[1];
        return level.getEntitiesOfClass(ItemEntity.class, aabb, itemEntity -> itemEntity.isAlive() && !itemEntity.getItem().isEmpty() && counter[0]++ < SIZE);
    }

    private class ItemEntityRecipeHandler implements IRecipeHandler {

        private List<ItemEntity> planned = Collections.emptyList();

        private List<ItemEntity> plannedEntities(PlanScratch plan, int member) {
            for (int i = 0; i < plan.logSize(); i++) {
                if (plan.logMember(i) == member && !plan.logIsFluid(i)) return planned;
            }
            planned = getItemEntitiesAbove();
            return planned;
        }

        private static long reserved(PlanScratch plan, int member, int token, boolean consumeOnly) {
            long r = 0;
            for (int i = 0; i < plan.logSize(); i++) {
                if (plan.logMember(i) == member && plan.logToken(i) == token && !plan.logIsFluid(i) && (!consumeOnly || plan.logConsumes(i))) r += plan.logAmount(i);
            }
            return r;
        }

        @Override
        public boolean handlesItems() {
            return true;
        }

        @Override
        public long available(AEKeyType type, KeyIngredient ingredient) {
            long total = 0;
            for (var itemEntity : getItemEntitiesAbove()) {
                if (itemEntity.isAlive() && KeyIngredient.acceptsStack(ingredient, itemEntity.getItem())) total += StxckUtil.getTotalCount(itemEntity);
            }
            return total;
        }

        @Override
        public long reserveInput(PlanScratch plan, int member, AEKeyType type, int entry, KeyIngredient ingredient, long need, boolean consume) {
            var entities = plannedEntities(plan, member);
            long got = 0;
            for (int s = 0, size = entities.size(); s < size && got < need; s++) {
                var itemEntity = entities.get(s);
                if (!itemEntity.isAlive()) continue;
                var stack = itemEntity.getItem();
                if (stack.isEmpty() || !KeyIngredient.acceptsStack(ingredient, stack)) continue;
                long free = StxckUtil.getTotalCount(itemEntity) - reserved(plan, member, s, false);
                if (free <= 0) continue;
                long t = Math.min(free, need - got);
                plan.logCustom(member, s, entry, t, type, consume, false);
                got += t;
            }
            return got;
        }

        @Override
        public boolean commitInput(PlanScratch plan, int member, AEKeyType type) {
            var entities = planned;
            int size = entities.size();
            for (int s = 0; s < size; s++) {
                long take = reserved(plan, member, s, true);
                if (take > 0) {
                    var itemEntity = entities.get(s);
                    if (!itemEntity.isAlive() || StxckUtil.getTotalCount(itemEntity) < take) return false;
                }
            }
            for (int s = 0; s < size; s++) {
                long take = reserved(plan, member, s, true);
                if (take > 0) StxckUtil.shrink(entities.get(s), (int) take);
            }
            return true;
        }

        @Override
        public void rollbackInput(PlanScratch plan, int member, AEKeyType type) {
            var entities = planned;
            for (int s = 0, size = entities.size(); s < size; s++) {
                long take = reserved(plan, member, s, true);
                if (take <= 0) continue;
                var itemEntity = entities.get(s);
                if (itemEntity.isAlive()) {
                    StxckUtil.grow(itemEntity, (int) take);
                } else if (itemEntity.level() instanceof ServerLevel level) {
                    var restored = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), itemEntity.getItem().copyWithCount((int) take));
                    restored.setDeltaMovement(0, 0, 0);
                    level.addFreshEntity(restored);
                }
            }
        }

        @Override
        public long reserveOutput(PlanScratch plan, int member, AEKeyType type, int entry, AEKey key, long amount) {
            return amount;
        }

        @Override
        public long insertOutput(AEKeyType type, AEKey key, long amount) {
            if (getLevel() instanceof ServerLevel level) {
                var pos = getPos().above(3);
                var posCenter = pos.getCenter();
                var random = level.random;
                var itemEntity = new ItemEntity(level, posCenter.x(), posCenter.y(), posCenter.z(), Keys.toStack((AEItemKey) key, amount));
                itemEntity.setDeltaMovement(random.nextDouble() * 0.2 - 0.1, 0.2, random.nextDouble() * 0.2 - 0.1);
                level.addFreshEntity(itemEntity);
            }
            return amount;
        }

        @Override
        public boolean isInfiniteCapacity(AEKeyType type) {
            return true;
        }

        @Override
        public boolean forEachKey(AEKeyType type, KeyVisitor visitor) {
            for (var itemEntity : getItemEntitiesAbove()) {
                if (!itemEntity.isAlive()) continue;
                var key = AEItemKey.of(itemEntity.getItem());
                if (key != null && visitor.visit(key, StxckUtil.getTotalCount(itemEntity))) return true;
            }
            return false;
        }

        @Override
        public IntLongMap getSearchMap(@NotNull GTRecipeType type) {
            var intIngredientMap = new IntLongMap();
            addToSearchMap(intIngredientMap, type);
            return intIngredientMap;
        }

        @Override
        public void addToSearchMap(@NotNull IntLongMap target, @NotNull GTRecipeType type) {
            for (var i : getItemEntitiesAbove()) {
                if (!i.isAlive()) continue;
                var key = AEItemKey.of(i.getItem());
                if (key != null) type.convertKey(key, StxckUtil.getTotalCount(i), target);
            }
        }
    }

    private class InWorldManaContainer implements IManaContainer {

        @Override
        public boolean acceptDistributor() {
            return false;
        }

        @Override
        public MetaMachine getMachine() {
            return ManaFlowAssembler.this;
        }

        @Override
        public long getMaxMana() {
            long maxMana = 0;
            for (var poolRef : manaPools) {
                var pool = poolRef.get();
                if (pool == null || pool.isRemoved()) continue;
                maxMana += Math.max(0, Math.max(pool.getMaxMana(), pool.getCurrentMana()));
            }
            return maxMana;
        }

        @Override
        public long getCurrentMana() {
            long currentMana = 0;
            for (var poolRef : manaPools) {
                var pool = poolRef.get();
                if (pool == null || pool.isRemoved()) continue;
                currentMana += Math.max(0, pool.getCurrentMana());
            }
            return currentMana;
        }

        @Override
        public void setCurrentMana(long mana) {
            var currentMana = getCurrentMana();
            var manaToSet = Math.clamp(mana, 0, getMaxMana());
            if (manaToSet == currentMana) return;
            if (manaToSet < currentMana) {
                if (hasCreativePool()) return;
                removeMana(currentMana - manaToSet);
                return;
            }
            addMana(manaToSet - currentMana);
        }

        private void removeMana(long mana) {
            for (var poolRef : manaPools) {
                if (mana <= 0) break;
                var pool = poolRef.get();
                if (pool == null || pool.isRemoved()) continue;
                var currentMana = Math.max(0, pool.getCurrentMana());
                var toRemove = (int) Math.min(mana, currentMana);
                if (toRemove == 0) continue;
                pool.receiveMana(-toRemove);
                mana -= Math.max(0, currentMana - pool.getCurrentMana());
            }
        }

        private void addMana(long mana) {
            for (var poolRef : manaPools) {
                if (mana <= 0) break;
                var pool = poolRef.get();
                if (pool == null || pool.isRemoved()) continue;
                var currentMana = Math.max(0, pool.getCurrentMana());
                var availableSpace = Math.max(0, pool.getMaxMana() - currentMana);
                var toAdd = (int) Math.min(mana, availableSpace);
                if (toAdd == 0) continue;
                pool.receiveMana(toAdd);
                mana -= Math.max(0, pool.getCurrentMana() - currentMana);
            }
        }

        private boolean isCreativePool(ManaPoolBlockEntity pool) {
            return pool.getBlockState().getBlock() instanceof ManaPoolBlock poolBlock && poolBlock.variant == ManaPoolBlock.Variant.CREATIVE;
        }

        private boolean hasCreativePool() {
            for (var poolRef : manaPools) {
                var pool = poolRef.get();
                if (pool != null && !pool.isRemoved() && isCreativePool(pool)) return true;
            }
            return false;
        }

        @Override
        public long getMaxIORate() {
            return maxRate;
        }

        @Override
        public UUID getOwnerUUID() {
            return null;
        }

        @Override
        public ManaDistributorMachine getNetMachineCache() {
            return null;
        }

        @Override
        public void setNetMachineCache(ManaDistributorMachine cache) {}
    }

    public static Supplier<TraceabilityPredicate> MANA_PYLON = GTMemoizer.memoize(
            () -> GTOPredicates.dataBlock(MAX_RATE, AtomicInteger::new, (data, state) -> {
                if (state.getBlockState().getBlock() instanceof PylonBlock block) {
                    switch (block.variant) {
                        case MANA -> data.getAndAdd(8);
                        case NATURA -> data.getAndAdd(8 << 2);
                        case GAIA -> data.getAndAdd(8 << 6);
                    }
                }
                if (state.getBlockState().getBlock() instanceof BlockAlfsteelPylon) {
                    data.getAndAdd(8 << 4);
                }
                return data;
            }, BotaniaBlocks.manaPylon, BotaniaBlocks.naturaPylon, BotaniaBlocks.gaiaPylon, ModBlocks.alfsteelPylon));

    public static TraceabilityPredicate manaPool(Block... blocks) {
        return GTOPredicates.dataBlock(POOL, ArrayList::new, (data, state) -> {
            data.add(state.getPos());
            return data;
        }, blocks);
    }
}
