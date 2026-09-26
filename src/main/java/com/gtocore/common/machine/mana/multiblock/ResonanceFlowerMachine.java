package com.gtocore.common.machine.mana.multiblock;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.codec.CombinedCodec;
import com.gto.datasynclib.datastream.data.Data;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.lowdragmc.lowdraglib.LDLib.random;

public class ResonanceFlowerMachine extends ManaMultiblockMachine implements IStorageMultiblock, IDropSaveMachine {

    // 进度表最多保留的配方条数
    private static final int MAX_SIZE = 10;
    // 等级上限
    private static final short MAX_TIER = 256;

    // 以物品形式掉落/放置时保存进度的 NBT 键
    private static final String NBT_KEY_RECIPE_PROGRESS = "RecipeProgress";

    // 共鸣标签的键
    private static final String KEY_TYPE = "type";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_STACK = "stack";
    private static final String KEY_AMOUNT = "Amount";
    private static final String TYPE_ITEM = "item";
    private static final String TYPE_FLUID = "fluid";

    // 进度条目的键
    private static final String KEY_TIER = "tier";

    // 时间消耗波动系数
    @SaveToDisk(defaultValue = "1.0")
    private double timeFluctuationCoefficient = 1.0D;
    // 元素消耗波动系数
    @SaveToDisk(defaultValue = "1.0")
    private double elementalFluctuationCoefficient = 1.0D;

    // 剩余的锚定时间
    @SaveToDisk(defaultValue = "0")
    private int stableTime = 0;

    /**
     * 配方进度表：{@link GTRecipeDefinition} → {@code {tier, frequency}}，最多 {@link #MAX_SIZE} 条。
     *
     * <p>
     * 键直接用配方定义本身（注册对象、标识稳定，按引用比较），于是查找/去重/界面取名都不必再拿
     * 字符串在配方表里线性扫描。用 Linked 版引用容器是为了保留「最近使用」顺序：累加时先移除再放回
     * 末尾，超量时 {@code removeFirst()} 淘汰最久未使用的那条。
     *
     * <p>
     * 持久化走 DataSyncLib 的 Map 访问器：键用 gtm 注册的 {@code GTRecipeDefinition} 编解码器
     * （只写配方类型 + 注册 id，读回时从配方表解析），值用 {@code CompoundTag}。
     *
     * <p>
     * <b>存档兼容：</b>旧实现是 {@code List<CompoundTag>}（条目自带 id 字符串），字段名
     * {@code recipeIncremental}。数据形状完全不同，沿用旧键会让新格式去解析旧数据而崩溃，故字段
     * （即存储键）改名为 {@code recipeProgress}，旧数据自然被忽略，代价只是等级进度重置一次。
     */
    @SaveToDisk
    private final Reference2ObjectLinkedOpenHashMap<GTRecipeDefinition, Entry> recipeProgress = new Reference2ObjectLinkedOpenHashMap<>(MAX_SIZE);

    /** 最近一次真正开跑的配方定义；未跑过时为 {@code null}。只用于界面显示。 */
    @SaveToDisk
    private GTRecipeDefinition lastRecipe = null;

    // 额外共鸣输入
    @SaveToDisk(defaultValue = "2147483647")
    private int frequency = Integer.MAX_VALUE;
    @SaveToDisk(defaultValueGetter = "getDefaultResonanceItem")
    private ItemStack resonanceItem = ItemStack.EMPTY;
    @SaveToDisk(defaultValueGetter = "getDefaultResonanceFluid")
    private FluidStack resonanceFluid = FluidStack.EMPTY;

    private ItemStack getDefaultResonanceItem() {
        return ItemStack.EMPTY;
    }

    private FluidStack getDefaultResonanceFluid() {
        return FluidStack.EMPTY;
    }

    @SaveToDisk
    protected final NotifiableItemStackHandler machineStorage;

    public ResonanceFlowerMachine(MetaMachineBlockEntity holder) {
        super(holder);
        machineStorage = createMachineStorage(i -> i.getItem() == Items.NETHER_STAR || i.getItem() == GTOItems.STABILIZER_CORE.asItem());
    }

    @Override
    public NotifiableItemStackHandler getMachineStorage() {
        return machineStorage;
    }

    @Override
    public @NotNull Widget createUIWidget() {
        return IStorageMultiblock.super.createUIWidget(super.createUIWidget());
    }

    /**
     * 只计算「这条配方此刻该跑成什么样」：等级给出时长减免与并行上限，其余交给
     * {@link ParallelLogic} 依据内容库存与持续消耗继续收紧。
     *
     * <p>
     * <b>不要在这里改机器状态。</b>本方法在配方搜索期间（机器空闲时每 {@code interval} tick 搜一次）
     * 会对每个候选定义各调用一次。原实现在这里累计进度、消耗存储里的稳定资源、记录最近配方，于是
     * 连「只是被扫到、并没有跑」的配方也会升级，放入的下界之星也会被白吃掉。副作用已移到
     * {@link #beforeWorking} 与 {@link #afterWorking}，只在配方真正开跑、真正完成时各执行一次。
     */
    @Override
    public @Nullable GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        short tier = getTier(recipe.definition);
        // 时长 = 基础时长 × 时间波动系数 × 等级减免
        recipe.duration = (int) Math.max(1D, recipe.duration * timeFluctuationCoefficient * getTimeMultiplier(tier));
        // 并行上限由等级给出，内容库存/持续消耗不足时 ParallelLogic 会自行收紧（返回 null 表示这条配方跑不了）
        return ParallelLogic.accurateParallel(this, unit, recipe, getMaxParallel(tier));
    }

    /** 配方真正开跑（输入已扣）时才记录当前配方与它要求的共鸣物。 */
    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        lastRecipe = recipe.definition;
        applyResonance(recipe);
    }

    @Override
    public void afterWorking() {
        // 先抓住刚跑完的那条配方：父类回调里有部件逻辑，不能假设它之后 lastRecipe 还在
        GTRecipe finished = getRecipeLogic().getLastRecipe();
        super.afterWorking();
        resetResonance();
        // 进度按「真正跑完的配方」结算，且只加本次实际生效的并行数
        if (finished != null) {
            addEntry(finished.definition, finished.parallels);
            upgradeEntry(finished.definition);
        }
        // 补料：存储槽里的下界之星/稳定核心换成稳定次数
        updateStableTime();
        // 每次完工消耗一次锚定，没有锚定就波动一次
        if (stableTime > 0) stableTime--;
        else triggerFluctuation();
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (!super.handleTickRecipe(recipe)) return false;
        int progress = getRecipeLogic().getProgress();
        if (frequency > 0 && progress != 0 && progress % frequency == 0) {
            // 元素消耗波动：一次脉冲吃多少随系数缩放（至少 1 个），系数失控时消耗随之暴涨
            if (!resonanceFluid.isEmpty()) {
                int amount = scaleElementalAmount(resonanceFluid.getAmount());
                if (amount == resonanceFluid.getAmount()) return inputFluid(resonanceFluid);
                return inputFluid(new FluidStack(resonanceFluid.getFluid(), amount, resonanceFluid.getTag()));
            }
            if (!resonanceItem.isEmpty()) {
                int count = scaleElementalAmount(resonanceItem.getCount());
                if (count == resonanceItem.getCount()) return inputItem(resonanceItem);
                return inputItem(resonanceItem.copyWithCount(count));
            }
        }
        return true;
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        var recipeEntry = getEntry(lastRecipe);
        if (recipeEntry != null) {
            short tier = recipeEntry.tier;
            textList.add(Component.translatable("gtocore.machine.resonance_flower.current_recipe",
                    getLastRecipeName().copy().withStyle(ChatFormatting.GREEN)));
            if (tier >= MAX_TIER) {
                textList.add(Component.translatable("gtocore.machine.resonance_flower.tier_max",
                        Component.literal(Short.toString(tier)).withStyle(ChatFormatting.GREEN)));
            } else {
                textList.add(Component.translatable("gtocore.machine.resonance_flower.tier_progress",
                        Component.literal(Short.toString(tier)).withStyle(ChatFormatting.GREEN),
                        Component.literal(FormattingUtil.formatNumbers(recipeEntry.frequency)).withStyle(ChatFormatting.GREEN),
                        Component.literal(FormattingUtil.formatNumbers(calculateUpgradeRequirement(tier))).withStyle(ChatFormatting.GREEN)));
            }
            textList.add(Component.translatable("gtocore.machine.resonance_flower.tier_effect",
                    Component.literal(FormattingUtil.formatNumbers(getTimeMultiplier(tier) * 100.0F)).withStyle(ChatFormatting.GREEN),
                    Component.literal(FormattingUtil.formatNumbers(getMaxParallel(tier))).withStyle(ChatFormatting.GREEN)));
        }
        textList.add(Component.translatable("gtocore.machine.resonance_flower.stable_operation_times",
                Component.literal(Integer.toString(stableTime)).withStyle(ChatFormatting.AQUA)));
        textList.add(Component.translatable("gtocore.machine.resonance_flower.time_fluctuation_coefficient",
                Component.literal(String.format("%.3f", timeFluctuationCoefficient)).withStyle(ChatFormatting.AQUA)));
        textList.add(Component.translatable("gtocore.machine.resonance_flower.elemental_fluctuation_coefficient",
                Component.literal(String.format("%.3f", elementalFluctuationCoefficient)).withStyle(ChatFormatting.AQUA)));
    }

    /** 最近一次运行配方的显示名：优先取产物名，取不到就退回注册 id。 */
    private Component getLastRecipeName() {
        GTRecipeDefinition recipe = lastRecipe;
        if (recipe == null) return Component.literal("");
        if (!recipe.itemOutputs.isEmpty()) return recipe.itemOutputs.getFirst().inner.getName();
        if (!recipe.fluidOutputs.isEmpty()) return recipe.fluidOutputs.getFirst().inner.getName();
        return Component.literal(recipe.id.toString());
    }

    /**
     * 物品形式保存：直接复用 {@code @SaveToDisk} 那套字段编解码（{@link Data} 二进制），
     * 不再自己手搓一份 NBT 格式——同一份数据只留一条持久化路径。
     */
    @Override
    public void saveToItem(CompoundTag tag) {
        if (recipeProgress.isEmpty() && lastRecipe == null) return;
        byte[] data = getFieldDataManager().writeFieldsToData("recipeProgress", "lastRecipe").writeToBytes();
        tag.put(NBT_KEY_RECIPE_PROGRESS, new ByteArrayTag(data));
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (tag.get(NBT_KEY_RECIPE_PROGRESS) instanceof ByteArrayTag data) {
            getFieldDataManager().readFieldsFromData(Data.readData(data.getAsByteArray()), 0, "recipeProgress", "lastRecipe");
        }
    }

    /** 读取配方携带的共鸣需求（没有标签则清空：不消耗任何共鸣物）。 */
    private void applyResonance(GTRecipe recipe) {
        resetResonance();
        if (!recipe.data.containsKey(GTORecipeDataKeys.RESONANCE)) return;
        Object[] resonance = fromResonanceTag(recipe.data.getData(GTORecipeDataKeys.RESONANCE));
        if (resonance[0] instanceof ItemStack itemStack) {
            resonanceItem = itemStack;
        } else if (resonance[0] instanceof FluidStack fluidStack) {
            resonanceFluid = fluidStack;
        }
        frequency = (int) resonance[1];
    }

    private void resetResonance() {
        resonanceItem = ItemStack.EMPTY;
        resonanceFluid = FluidStack.EMPTY;
        frequency = Integer.MAX_VALUE;
    }

    private void updateStableTime() {
        if (stableTime >= 100000000) return;
        ItemStack stack = machineStorage.getStackInSlot(0);
        if (stack.isEmpty()) return;
        if (stack.getItem() == GTOItems.STABILIZER_CORE.asItem()) {
            stableTime += 10000000;
            stack.shrink(1);
            machineStorage.setStackInSlot(0, stack);
        } else if (stack.getItem() == Items.NETHER_STAR) {
            stableTime += 5 * stack.getCount();
            machineStorage.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    /////////////////////////////////////
    // ********** 共鸣消耗系统 ********** //
    /////////////////////////////////////

    // 通用序列化：ItemStack/FluidStack + 频率 → CompoundTag
    public static CompoundTag toResonanceTag(Object stack, int frequency) {
        CompoundTag root = new CompoundTag();
        root.putInt(KEY_FREQUENCY, frequency);
        if (stack instanceof ItemStack itemStack) {
            root.putString(KEY_TYPE, TYPE_ITEM);
            CompoundTag stackTag = new CompoundTag();
            itemStack.save(stackTag);
            root.put(KEY_STACK, stackTag);
        } else if (stack instanceof FluidStack fluidStack) {
            root.putString(KEY_TYPE, TYPE_FLUID);
            CompoundTag stackTag = new CompoundTag();
            stackTag.putString("FluidName", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid())).toString());
            stackTag.putInt(KEY_AMOUNT, fluidStack.getAmount());
            if (fluidStack.hasTag()) stackTag.put("tag", fluidStack.getTag().copy());
            root.put(KEY_STACK, stackTag);
        }
        return root;
    }

    // 通用反序列化：CompoundTag → Object[] [stack, frequency]
    public static Object[] fromResonanceTag(CompoundTag tag) {
        int frequency = tag.getInt(KEY_FREQUENCY);
        String type = tag.getString(KEY_TYPE);
        if (!tag.contains(KEY_STACK, Tag.TAG_COMPOUND)) return new Object[] { null, frequency };

        CompoundTag stackTag = tag.getCompound(KEY_STACK);
        Object stack = null;
        if (type.equals(TYPE_ITEM)) {
            stack = ItemStack.of(stackTag);
        } else if (type.equals(TYPE_FLUID)) {
            Fluid fluid = RegistriesUtils.getFluid(stackTag.getString("FluidName"));
            if (fluid != null && fluid != Fluids.EMPTY) {
                int amount = stackTag.getInt(KEY_AMOUNT);
                CompoundTag extraTag = stackTag.contains("tag", Tag.TAG_COMPOUND) ? stackTag.getCompound("tag") : null;
                stack = new FluidStack(fluid, amount, extraTag);
            }
        }
        return new Object[] { stack, frequency };
    }

    /** 一次共鸣脉冲实际消耗的元素量：受元素消耗波动系数缩放，至少 1。 */
    private int scaleElementalAmount(int baseAmount) {
        if (elementalFluctuationCoefficient == 1.0D) return baseAmount;
        return Math.max(1, (int) (baseAmount * elementalFluctuationCoefficient));
    }

    /** 波动系数系统 */
    public void triggerFluctuation() {
        // 1. 时间消耗波动：每次跳变乘数范围 0.2 ~ 2.6，最终乘数范围 0.05 ~ 20
        double newTimeMultiplier = timeFluctuationCoefficient * (0.2D + random.nextDouble() * 2.4D);
        timeFluctuationCoefficient = Mth.clamp(newTimeMultiplier, 0.05D, 20.0D);
        // 2. 元素消耗波动：每次跳变乘数范围 0.5 ~ 1.8，最终乘数范围 0.1 ~ 16
        double newElemMultiplier = elementalFluctuationCoefficient * (0.5D + random.nextDouble() * 1.3D);
        elementalFluctuationCoefficient = Mth.clamp(newElemMultiplier, 0.1D, 16.0D);
    }

    /////////////////////////////////////
    // ********** 配方记录系统 ********** //
    /////////////////////////////////////

    /**
     * 累加配方的进度，并把它挪到「最近使用」的位置。
     *
     * <ul>
     * <li>尚无记录 → 新建条目，{@code tier = 1}，{@code frequency = 本次并行数}；</li>
     * <li>已有记录 → {@code frequency} 累加到旧条目上；</li>
     * <li>记录数超过 {@link #MAX_SIZE} → 淘汰最久未使用（链表头）的那条。</li>
     * </ul>
     */
    public void addEntry(@Nullable GTRecipeDefinition definition, long frequency) {
        if (definition == null) return;
        recipeProgress.compute(definition, (k, v) -> {
            if (v == null) {
                return new Entry((short) 1, frequency);
            } else {
                return new Entry(v.tier, v.frequency + frequency);
            }
        });
        while (recipeProgress.size() > MAX_SIZE) recipeProgress.removeFirst();
    }

    /** 取某个配方的进度条目；没有记录（或传入 null）时返回 {@code null}。 */
    @Nullable
    public Entry getEntry(@Nullable GTRecipeDefinition definition) {
        return definition == null ? null : recipeProgress.get(definition);
    }

    /** 取某个配方的等级；没有记录时为 0（此时不减免时长、并行 1）。 */
    public short getTier(@Nullable GTRecipeDefinition definition) {
        var entry = getEntry(definition);
        return entry == null ? 0 : entry.tier;
    }

    /**
     * 把攒够的 frequency 兑换成等级：只要还够下一级的门槛就继续升。
     *
     * <p>
     * 用循环而不是「一次只升一级」，是因为并行数拉高后单次运行就能攒下远超一级门槛的 frequency，
     * 一次只扣一级会让 frequency 无限累积，最终溢出 {@code long}。等级上限 {@link #MAX_TIER}。
     */
    public void upgradeEntry(@Nullable GTRecipeDefinition definition) {
        var entry = getEntry(definition);
        if (entry == null) return;

        short tier = entry.tier;
        long frequency = entry.frequency;
        boolean upgraded = false;
        while (tier < MAX_TIER) {
            long requirement = calculateUpgradeRequirement(tier);
            if (frequency < requirement) break;
            frequency -= requirement;
            tier++;
            upgraded = true;
        }
        if (upgraded) {
            entry.tier = tier;
            entry.frequency = frequency;
        }
    }

    /** 计算升级所需frequency */
    private long calculateUpgradeRequirement(short currentTier) {
        if (currentTier >= MAX_TIER) return Long.MAX_VALUE;
        if (currentTier <= 0) return 10L;

        if (currentTier <= 4) {
            return 10L + currentTier * 100L;
        } else if (currentTier <= 8) {
            return 410L + (currentTier - 4) * 200L;
        } else if (currentTier <= 16) {
            return 1210L + (currentTier - 8) * 400L;
        } else if (currentTier <= 32) {
            return 4410L + (currentTier - 16) * 800L;
        } else if (currentTier <= 48) {
            return 17210L + (currentTier - 32) * 2600L;
        } else if (currentTier <= 64) {
            return 58900L + (currentTier - 48) * 9400L;
        } else if (currentTier <= 96) {
            return 360000L + (currentTier - 64) * 5000000L;
        } else if (currentTier <= 128) {
            return 180000000L + (currentTier - 96) * 64000000L;
        } else if (currentTier <= 192) {
            return 2400000000L + (currentTier - 128) * 800000000000000L;
        } else {
            return 52000000000000000L + (currentTier - 192) * 140000000000000000L;
        }
    }

    /** 指定等级的时长减免系数（≥64 级固定 0.1，即最多减 90%）。 */
    public float getTimeMultiplier(short tier) {
        if (tier >= 64) return 0.1f;
        if (tier <= 0) return 1.0f;

        return tier <= 8 ? 1.0f - tier * 0.025f : 0.8f - (tier - 8) * 0.0125f;
    }

    /** 指定等级的并行上限（≥256 级为 {@link Long#MAX_VALUE}）。 */
    public long getMaxParallel(short tier) {
        if (tier >= MAX_TIER) return Long.MAX_VALUE;
        if (tier <= 0) return 1L;

        if (tier <= 4) {
            return 1L + tier * 10L;
        } else if (tier <= 8) {
            return 50L + (tier - 4) * 20L;
        } else if (tier <= 16) {
            return 200L + (tier - 8) * 40L;
        } else if (tier <= 32) {
            return 800L + (tier - 16) * 80L;
        } else if (tier <= 48) {
            return 2400L + (tier - 32) * 250L;
        } else if (tier <= 64) {
            return 8000L + (tier - 48) * 800L;
        } else if (tier <= 96) {
            return 24000L + (tier - 64) * 50000L;
        } else if (tier <= 128) {
            return 1800000L + (tier - 96) * 600000L;
        } else if (tier <= 192) {
            return 42000000L + (tier - 128) * 800000000L;
        } else {
            return 52000000000L + (tier - 192) * 140000000000L;
        }
    }

    public static void addCodec() {
        Entry.CODEC.register(Entry.class);
    }

    private static final class Entry {

        private static final DataSyncCodec<Entry> CODEC = CombinedCodec.composite(
                DataSyncCodec.SHORT_CODEC, entry -> entry.tier,
                DataSyncCodec.LONG_CODEC, entry -> entry.frequency,
                Entry::new);

        private short tier;
        private long frequency;

        private Entry(short tier, long frequency) {
            this.tier = tier;
            this.frequency = frequency;
        }
    }
}
