package com.gtocore.common.machine.magic.rune;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.mojang.datafixers.util.Either;
import com.suried.gtoutils.schedule.Task;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.recipe.RuneRitualRecipe;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.BotaniaItems;
import vazkii.botania.common.MythicBotanyItems;
import vazkii.botania.common.MythicBotanyPlayerData;
import vazkii.botania.common.rune.RuneRitualRecipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 大师符文支托：符文仪式的核心，内部储存魔力。
 *
 * <p>
 * 仪式每 tick 从内部魔力扣除配方的均摊消耗，魔力不足时进度暂停，补足后继续。
 * 右键大师符文支托检查阵型与投入，匹配就启动；潜行右键保持放入与取回。
 * </p>
 */
public class CentralRuneHolderMachine extends RuneHolderMachine implements ManaReceiver {

    /** 内部魔力上限。 */
    public static final long MAX_MANA = 1_000_000L;
    private static final int SYNC_INTERVAL = 5;
    private static final int INPUT_RADIUS = 2;
    private static final int NO_MANA_NOTIFY_INTERVAL = 100;

    @SaveToDisk(defaultValue = "0")
    private long mana;

    @SaveToDisk
    @SyncToClient
    private String recipeId = "";

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int progress;

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int transform;

    @SaveToDisk
    private ListTag consumed = new ListTag();

    @SaveToDisk
    private CompoundTag specialData = new CompoundTag();

    private final LazyOptional<ManaReceiver> manaReceiverCap = LazyOptional.of(() -> this);

    @Nullable
    private RuneRitualRecipe recipe;
    private Task tickSubscription;

    public CentralRuneHolderMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onMachineRemoved() {
        cancelRitual();
        super.onMachineRemoved();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(tickSubscription, this::serverTick);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        tickSubscription = Task.cancel(tickSubscription);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            return manaReceiverCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public boolean isRunning() {
        return recipe != null;
    }

    public int getProgress() {
        return progress;
    }

    public int getTransform() {
        return transform;
    }

    public long getMana() {
        return mana;
    }

    // ==================== 仪式 ====================

    private void serverTick() {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        if (!recipeId.isEmpty()) {
            resolveRecipe();
        }
        if (recipe != null) {
            if (!patternValid(recipe, transform)) {
                cancelRitual();
                return;
            }
            int cost = recipe.getManaPerTick();
            if (mana < cost) {
                if (level.getGameTime() % NO_MANA_NOTIFY_INTERVAL == 0) {
                    notifyNoMana();
                }
                return;
            }
            if (cost > 0) {
                mana -= cost;
                onChanged();
            }
            progress++;
            if (progress >= recipe.getTicks()) {
                finishRitual();
            } else if (progress % SYNC_INTERVAL == 0) {
                updatePatterns(progress / (double) recipe.getTicks());
                requestSync();
            }
        }
    }

    private void resolveRecipe() {
        Level level = getLevel();
        if (level == null || recipeId.isEmpty()) {
            return;
        }
        if (recipe != null && recipeId.equals(recipe.getId().toString())) {
            return;
        }
        if (level.getRecipeManager().byKey(new ResourceLocation(recipeId)).orElse(null) instanceof RuneRitualRecipe found) {
            recipe = found;
        } else {
            resetRitual();
        }
    }

    /** 右键大师符文支托：检查阵型并启动仪式；潜行时保持放入与取回。 */
    @Override
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown()) {
            if (isRunning()) {
                player.displayClientMessage(Component.translatable("message.mythicbotany.ritual_running"), true);
                return InteractionResult.CONSUME;
            }
            if (hasRune()) {
                RecipeMatch match = findRecipe();
                if (match == null) {
                    player.sendSystemMessage(Component.translatable("message.mythicbotany.ritual_wrong_shape").withStyle(ChatFormatting.GRAY));
                } else if (!MythicBotanyPlayerData.hasMimirKnowledge(player)) {
                    player.sendSystemMessage(Component.translatable("message.mythicbotany.mimir_unknown").withStyle(ChatFormatting.GRAY));
                } else {
                    startRitual(match.recipe(), match.transform(), player::sendSystemMessage);
                }
                return InteractionResult.CONSUME;
            }
        }
        return super.onUse(state, level, pos, player, hand, hit);
    }

    @Nullable
    private RecipeMatch findRecipe() {
        Level level = getLevel();
        if (level == null) {
            return null;
        }
        for (RuneRitualRecipe candidate : level.getRecipeManager().getAllRecipesFor(RuneRitualRecipes.RUNE_RITUAL_TYPE.get())) {
            if (!candidate.getCenterRune().test(getRune())) {
                continue;
            }
            for (int i = 0; i < 8; i++) {
                if (patternValid(candidate, i)) {
                    return new RecipeMatch(candidate, i);
                }
            }
        }
        return null;
    }

    private boolean patternValid(RuneRitualRecipe recipe, int transformIdx) {
        Level level = getLevel();
        if (level == null || !recipe.getCenterRune().test(getRune())) {
            return false;
        }
        for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
            BlockPos runePos = getPos().offset(rune.getX(transformIdx), 0, rune.getZ(transformIdx));
            if (!(MetaMachine.getMachine(level, runePos) instanceof RuneHolderMachine holder)) {
                return false;
            }
            if (!rune.getRune().test(holder.getRune())) {
                return false;
            }
        }
        return true;
    }

    private boolean startRitual(RuneRitualRecipe recipe, int transformIdx, @Nullable Consumer<Component> messages) {
        Level level = getLevel();
        if (level == null) {
            return false;
        }
        List<ItemEntity> entities = collectInputEntities();
        int[] remaining = new int[entities.size()];
        for (int i = 0; i < entities.size(); i++) {
            remaining[i] = entities.get(i).getItem().getCount();
        }

        List<ItemStack> consumedStacks = new ArrayList<>(recipe.getInputs().size());
        for (Ingredient ingredient : recipe.getInputs()) {
            int found = -1;
            for (int i = 0; i < entities.size(); i++) {
                if (remaining[i] > 0 && ingredient.test(entities.get(i).getItem())) {
                    found = i;
                    break;
                }
            }
            if (found < 0) {
                if (messages != null) {
                    messages.accept(Component.translatable("message.mythicbotany.ritual_wrong_items"));
                }
                return false;
            }
            consumedStacks.add(entities.get(found).getItem().copyWithCount(1));
            remaining[found]--;
        }

        CompoundTag specialTag = new CompoundTag();
        if (recipe.getSpecialInput() != null) {
            Either<MutableComponent, CompoundTag> result = recipe.getSpecialInput().apply(level, getPos(), recipe);
            Optional<MutableComponent> error = result.left();
            if (error.isPresent()) {
                if (messages != null) {
                    messages.accept(error.get());
                }
                return false;
            }
            specialTag = result.right().orElseGet(CompoundTag::new);
        }

        for (int i = 0; i < entities.size(); i++) {
            ItemEntity entity = entities.get(i);
            if (remaining[i] <= 0) {
                entity.discard();
            } else {
                entity.setItem(entity.getItem().copyWithCount(remaining[i]));
            }
        }

        this.recipe = recipe;
        this.recipeId = recipe.getId().toString();
        this.progress = 0;
        this.transform = transformIdx;
        this.consumed = toListTag(consumedStacks);
        this.specialData = specialTag;
        updatePatterns(0);
        onChanged();
        requestSync();
        return true;
    }

    private List<ItemEntity> collectInputEntities() {
        Vec3 center = new Vec3(getPos().getX() + 0.5, getPos().getY(), getPos().getZ() + 0.5);
        AABB area = new AABB(center, center).inflate(INPUT_RADIUS);
        return getLevel().getEntities(EntityType.ITEM, area, entity -> true);
    }

    private void finishRitual() {
        RuneRitualRecipe finished = recipe;
        Level level = getLevel();
        if (finished == null || level == null) {
            return;
        }
        setRune(ItemStack.EMPTY);
        for (ItemStack output : finished.getOutputs()) {
            spawnResult(output.copy());
        }
        for (RuneRitualRecipe.RunePosition rune : finished.getRunes()) {
            BlockPos runePos = getPos().offset(rune.getX(transform), 0, rune.getZ(transform));
            if (MetaMachine.getMachine(level, runePos) instanceof RuneHolderMachine holder) {
                ItemStack runeStack = holder.getRune();
                holder.setRune(ItemStack.EMPTY);
                if (!rune.isConsumed() && !runeStack.isEmpty()) {
                    net.minecraft.world.level.block.Block.popResource(level, getPos().above(), runeStack);
                }
            }
        }
        List<ItemStack> consumedStacks = fromListTag(consumed);
        resetRitual();
        if (finished.getSpecialOutput() != null) {
            finished.getSpecialOutput().apply(level, getPos(), consumedStacks);
        }
        onChanged();
        requestSync();
    }

    private void spawnResult(ItemStack stack) {
        Level level = getLevel();
        if (level == null || stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, stack);
        entity.setPickUpDelay(40);
        entity.setGlowingTag(true);
        level.addFreshEntity(entity);
    }

    /** 取消仪式：清空漂浮动画，退回已投入的物品，并把特殊投入还原。 */
    public void cancelRitual() {
        Level level = getLevel();
        if (recipe == null || level == null) {
            resetRitual();
            return;
        }
        updatePatterns(0);
        for (ItemStack stack : fromListTag(consumed)) {
            net.minecraft.world.level.block.Block.popResource(level, getPos().above(), stack);
        }
        if (recipe.getSpecialInput() != null) {
            recipe.getSpecialInput().cancel(level, getPos(), recipe, specialData);
        }
        resetRitual();
        onChanged();
        requestSync();
    }

    private void resetRitual() {
        recipe = null;
        recipeId = "";
        progress = 0;
        transform = 0;
        consumed = new ListTag();
        specialData = new CompoundTag();
    }

    private void updatePatterns(double progressValue) {
        Level level = getLevel();
        if (recipe == null || level == null) {
            return;
        }
        for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
            BlockPos runePos = getPos().offset(rune.getX(transform), 0, rune.getZ(transform));
            if (MetaMachine.getMachine(level, runePos) instanceof RuneHolderMachine holder) {
                holder.setFloatTarget(progressValue <= 0 ? null : getPos(), progressValue);
            }
        }
    }

    private void notifyNoMana() {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        Component message = Component.translatable("message.mythicbotany.ritual_no_mana");
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(getPos()).inflate(8))) {
            player.displayClientMessage(message, true);
        }
    }

    private static ListTag toListTag(List<ItemStack> stacks) {
        ListTag list = new ListTag();
        for (ItemStack stack : stacks) {
            list.add(stack.save(new CompoundTag()));
        }
        return list;
    }

    private static List<ItemStack> fromListTag(ListTag list) {
        List<ItemStack> stacks = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.of(list.getCompound(i));
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    // ==================== 客户端表现 ====================

    @Override
    public void animateTick(RandomSource random) {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        if (recipe == null && !recipeId.isEmpty()) {
            resolveRecipe();
        }
        if (recipe == null || progress <= 0) {
            return;
        }
        if (progress >= recipe.getTicks() - 2) {
            // 完成前一刻的闪光
            level.addParticle(ParticleTypes.FLASH, getPos().getX() + 0.5, getPos().getY() + 0.45, getPos().getZ() + 0.5, 0, 0, 0);
            return;
        }
        double scaled = Math.max(0, (progress - 2) / (double) recipe.getTicks());
        for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
            BlockPos runePos = getPos().offset(rune.getX(transform), 0, rune.getZ(transform));
            if (!(MetaMachine.getMachine(level, runePos) instanceof RuneHolderMachine holder) || holder.getRune().isEmpty()) {
                continue;
            }
            double x = rune.getX(transform) * (1 - scaled);
            double y = Math.sin(scaled * Math.PI);
            double z = rune.getZ(transform) * (1 - scaled);
            double xr = (random.nextDouble() * 0.6) - 0.3;
            double yr = (random.nextDouble() * 0.6) - 0.3;
            double zr = (random.nextDouble() * 0.6) - 0.3;
            level.addParticle(particleFor(holder.getRune().getItem()),
                    getPos().getX() + 0.5 + x + xr,
                    getPos().getY() + 0.25 + y + yr,
                    getPos().getZ() + 0.5 + z + zr,
                    0, 0, 0);
        }
    }

    private static ParticleOptions particleFor(Item item) {
        if (item == MythicBotanyItems.MB_FIMBULTYR_TABLET.get()) {
            return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.GOLD_BLOCK));
        } else if (item == BotaniaItems.RUNE_MANA.get()) {
            return WispParticleData.wisp(0.2f, 0, 0, 1, 0.3f);
        } else if (item == BotaniaItems.RUNE_FIRE.get()) {
            return ParticleTypes.FLAME;
        } else if (item == BotaniaItems.RUNE_AIR.get()) {
            return ParticleTypes.CLOUD;
        } else if (item == BotaniaItems.RUNE_EARTH.get()) {
            return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.DIRT));
        } else if (item == BotaniaItems.RUNE_WATER.get()) {
            return ParticleTypes.DOLPHIN;
        }
        int color = runeColor(item);
        return SparkleParticleData.sparkle(2, ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 6);
    }

    private static int runeColor(Item item) {
        String id = String.valueOf(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item));
        return switch (id) {
            case "botania:rune_spring" -> 0xFF919F;
            case "botania:rune_summer" -> 0x00DDED;
            case "botania:rune_autumn" -> 0xE5C200;
            case "botania:rune_winter" -> 0xE0DBD5;
            case "botania:rune_lust" -> 0xF346D1;
            case "botania:rune_gluttony" -> 0x6E6E6E;
            case "botania:rune_greed" -> 0x009431;
            case "botania:rune_sloth" -> 0xBB9661;
            case "botania:rune_wrath" -> 0xFF2424;
            case "botania:rune_envy" -> 0xC858E6;
            case "botania:rune_pride" -> 0x2C3237;
            case "mythicbotany:asgard_rune" -> 0xE1C500;
            case "mythicbotany:vanaheim_rune" -> 0x5FC748;
            case "mythicbotany:alfheim_rune" -> 0xFF76F7;
            case "mythicbotany:midgard_rune" -> 0x19B13B;
            case "mythicbotany:joetunheim_rune" -> 0xB16719;
            case "mythicbotany:muspelheim_rune" -> 0xBF0000;
            case "mythicbotany:niflheim_rune" -> 0x00B1BF;
            case "mythicbotany:nidavellir_rune" -> 0x6F6F6F;
            case "mythicbotany:helheim_rune" -> 0x780000;
            default -> 0xFFFFFF;
        };
    }

    // ==================== 魔力 ====================

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getPos();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(Integer.MAX_VALUE, mana);
    }

    @Override
    public long getCurrentManaLong() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return mana >= MAX_MANA;
    }

    @Override
    public void receiveMana(int amount) {
        if (amount == 0) {
            return;
        }
        long before = mana;
        mana = Math.max(0, Math.min(MAX_MANA, mana + amount));
        if (mana != before) {
            onChanged();
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    private record RecipeMatch(RuneRitualRecipe recipe, int transform) {}
}
