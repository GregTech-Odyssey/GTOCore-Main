package com.gtocore.data.transaction.manager;

import com.gtolib.api.wireless.WirelessManaContainer;
import com.gtolib.utils.WalletUtils;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.ImmutableList;
import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gtocore.data.transaction.TradingStationTool.*;
import static com.gtocore.data.transaction.data.trade.UnlockTrade.UNLOCK_BASE;

/**
 * 游戏内交易条目，封装交易的显示信息、输入输出资源、检查条件和执行逻辑。
 */
public record TradeEntry(
                         // 界面渲染材质
                         IGuiTexture texture,
                         // 解锁条件文本
                         String unlockCondition,
                         // 限制交易次数的条件
                         List<TradeCondition> conditions,
                         // 交易执行回调逻辑
                         TradeRunnable onExecute,
                         // 输入资源组
                         TradeGroup inputGroup,
                         // 输出资源组
                         TradeGroup outputGroup,
                         // 注册时生成的静态悬浮说明
                         List<Component> staticTooltip) {

    /**
     * 紧凑构造器
     */
    public TradeEntry {
        texture = texture != null ? texture : GuiTextures.GREGTECH_LOGO;
        unlockCondition = unlockCondition != null ? unlockCondition : UNLOCK_BASE;
        conditions = ImmutableList.copyOf(conditions != null ? conditions : Collections.emptyList());
        inputGroup = inputGroup != null ? inputGroup : new TradeGroup(Collections.emptyList(), Collections.emptyList(), new O2LOpenCacheHashMap<>(), BigInteger.ZERO, BigInteger.ZERO);
        outputGroup = outputGroup != null ? outputGroup : new TradeGroup(Collections.emptyList(), Collections.emptyList(), new O2LOpenCacheHashMap<>(), BigInteger.ZERO, BigInteger.ZERO);
        staticTooltip = ImmutableList.copyOf(staticTooltip != null ? staticTooltip : Collections.emptyList());
    }

    // ------------------- 核心业务方法 -------------------

    /**
     * 计算所有限制条件共同允许的最大交易次数。
     *
     * @param data 当前交易所使用的玩家、库存和世界数据
     * @return 所有条件上限的最小值；无条件时返回 {@link Integer#MAX_VALUE}
     */
    private int conditionMaxCount(TradeData data) {
        int maxCount = Integer.MAX_VALUE;
        for (TradeCondition condition : conditions) {
            maxCount = Math.min(maxCount, condition.maxCount(data, this));
            if (maxCount == 0) return 0;
        }
        return maxCount;
    }

    /**
     * 根据输入资源余额和输出流体容量计算最多可完成多少次交易。
     *
     * @param data 当前交易所使用的玩家、库存和世界数据
     * @return 资源允许的最大交易次数；不在服务端或任一资源不足时返回 {@code 0}
     */
    public int resourceMaxCount(TradeData data) {
        if (!(data.level() instanceof ServerLevel serverLevel)) return 0;

        int inputItem = inputGroup().items().isEmpty() ? Integer.MAX_VALUE : checkMaxMultiplier(data.inputItem(), inputGroup().items());
        if (inputItem == 0) return 0;

        int inputFluid = inputGroup().fluids().isEmpty() ? Integer.MAX_VALUE : checkMaxConsumeMultiplier(data.inputFluid(), inputGroup().fluids());
        if (inputFluid == 0) return 0;

        int outputFluid = outputGroup().fluids().isEmpty() ? Integer.MAX_VALUE : checkMaxCapacityMultiplier(data.outputFluid(), outputGroup().fluids());
        if (outputFluid == 0) return 0;

        int inputCurrencies = Integer.MAX_VALUE;
        if (!inputGroup().currencies().isEmpty()) {
            boolean foundCurrency = false;
            for (var iterator = inputGroup().currencies().object2LongEntrySet().fastIterator(); iterator.hasNext();) {
                var entry = iterator.next();
                long singleAmount = entry.getLongValue();
                if (singleAmount <= 0) continue;
                foundCurrency = true;
                long available = WalletUtils.getCurrencyAmount(data.uuid(), serverLevel, entry.getKey()) / singleAmount;
                if (available <= 0) return 0;
                inputCurrencies = Math.min(inputCurrencies, (int) Math.min(available, Integer.MAX_VALUE));
            }
            if (!foundCurrency) return 0;
        }
        if (inputCurrencies == 0) return 0;

        int inputEnergy = inputGroup().energy().equals(BigInteger.ZERO) ? Integer.MAX_VALUE : WirelessEnergyContainer.getOrCreateContainer(data.teamUUID()).getStorage()
                .divide(inputGroup().energy())
                .min(BigInteger.valueOf(Integer.MAX_VALUE)).intValueExact();
        if (inputEnergy == 0) return 0;

        int inputMana = inputGroup().mana().equals(BigInteger.ZERO) ? Integer.MAX_VALUE : WirelessManaContainer.getOrCreateContainer(data.teamUUID()).getStorage()
                .divide(inputGroup().mana())
                .min(BigInteger.valueOf(Integer.MAX_VALUE)).intValueExact();
        if (inputMana == 0) return 0;

        return Math.min(Math.min(Math.min(inputItem, inputFluid), Math.min(outputFluid, inputCurrencies)), Math.min(inputEnergy, inputMana));
    }

    /**
     * 按实际交易次数扣除输入并发放输出。
     *
     * @param data       当前交易所使用的玩家、库存和世界数据
     * @param multiplier 已通过检查的实际交易次数
     */
    private void executeInputOutput(TradeData data, int multiplier) {
        if (!(data.level() instanceof ServerLevel serverLevel)) return;

        if (!inputGroup().items().isEmpty()) {
            deductMultipliedItems(data.inputItem(), inputGroup().items(), multiplier);
        }
        if (!outputGroup().items().isEmpty()) {
            addMultipliedItems(data.outputItem(), outputGroup().items(), multiplier, serverLevel, data.pos());
        }
        if (!inputGroup().fluids().isEmpty()) {
            deductMultipliedFluids(data.inputFluid(), inputGroup().fluids(), multiplier);
        }
        if (!outputGroup().fluids().isEmpty()) {
            addMultipliedFluids(data.outputFluid(), outputGroup().fluids(), multiplier);
        }
        if (!inputGroup().currencies().isEmpty()) {
            inputGroup().currencies().forEach((currencyId, singleAmount) -> WalletUtils.subtractCurrency(data.uuid(), serverLevel, currencyId, singleAmount * multiplier));
        }
        if (!outputGroup().currencies().isEmpty()) {
            outputGroup().currencies().forEach((currencyId, singleAmount) -> WalletUtils.addCurrency(data.uuid(), serverLevel, currencyId, singleAmount * multiplier));
        }
        if (!inputGroup().energy().equals(BigInteger.ZERO)) {
            WirelessEnergyContainer energyContainer = WirelessEnergyContainer.getOrCreateContainer(data.teamUUID());
            energyContainer.setStorage(energyContainer.getStorage().subtract(inputGroup().energy().multiply(BigInteger.valueOf(multiplier))));
        }
        if (!outputGroup().energy().equals(BigInteger.ZERO)) {
            WirelessEnergyContainer energyContainer = WirelessEnergyContainer.getOrCreateContainer(data.teamUUID());
            energyContainer.setStorage(energyContainer.getStorage().add(outputGroup().energy().multiply(BigInteger.valueOf(multiplier))));
        }
        if (!inputGroup().mana().equals(BigInteger.ZERO)) {
            WirelessManaContainer manaContainer = WirelessManaContainer.getOrCreateContainer(data.teamUUID());
            manaContainer.setStorage(manaContainer.getStorage().subtract(inputGroup().mana().multiply(BigInteger.valueOf(multiplier))));
        }
        if (!outputGroup().mana().equals(BigInteger.ZERO)) {
            WirelessManaContainer manaContainer = WirelessManaContainer.getOrCreateContainer(data.teamUUID());
            manaContainer.setStorage(manaContainer.getStorage().add(outputGroup().mana().multiply(BigInteger.valueOf(multiplier))));
        }
    }

    /**
     * 综合限制条件与资源状态，计算当前实际可交易次数。
     *
     * @param data 当前交易所使用的玩家、库存和世界数据
     * @return 条件上限与资源上限中的较小值；不可交易时返回 {@code 0}
     */
    public int check(TradeData data) {
        if (!(data.level() instanceof ServerLevel)) return 0;
        int conditionMaxCount = conditionMaxCount(data);
        if (conditionMaxCount == 0) return 0;
        return Math.min(conditionMaxCount, resourceMaxCount(data));
    }

    /**
     * 执行完整交易，包括资源变更、交易回调和结果音效。
     * 实际次数不会超过 {@link #check(TradeData)} 返回的上限。
     *
     * @param data                当前交易所使用的玩家、库存和世界数据
     * @param requestedMultiplier 玩家本次请求的交易次数
     */
    public void executeTrade(TradeData data, int requestedMultiplier) {
        if (!(data.level() instanceof ServerLevel)) return;
        int finalMultiplier = Math.min(check(data), requestedMultiplier);
        if (finalMultiplier <= 0) {
            data.level().playSound(null, data.pos(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.8F, 1.4F);
            return;
        }
        executeInputOutput(data, finalMultiplier);
        if (onExecute != null) {
            onExecute.run(data, this, finalMultiplier);
        }
        data.level().playSound(null, data.pos(), SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.BLOCKS, 1.8F, 1.4F);
    }

    /**
     * 在注册阶段构建不会随玩家状态变化的交易说明。
     *
     * @param description      商品的额外说明
     * @param inputGroup       每次交易消耗的资源
     * @param outputGroup      每次交易产出的资源
     * @param weightedProducts 抽奖商品及权重，仅用于展示
     * @return 不可变的静态悬浮文本列表
     */
    private static List<Component> buildStaticTooltip(List<Component> description, TradeGroup inputGroup,
                                                      TradeGroup outputGroup, List<WeightedProduct> weightedProducts) {
        List<Component> componentList = new ArrayList<>();
        if (!description.isEmpty()) {
            componentList.add(sectionHeader("gtocore.trade_group.description", ChatFormatting.DARK_AQUA));
            componentList.addAll(description);
        }
        if (!inputGroup.isEmpty()) componentList.addAll(inputGroup.getComponentList(true));
        if (!outputGroup.isEmpty()) componentList.addAll(outputGroup.getComponentList(false));
        if (!weightedProducts.isEmpty()) {
            if (outputGroup.isEmpty()) componentList.add(sectionHeader("gtocore.trade_group.false", ChatFormatting.DARK_GREEN));
            for (WeightedProduct product : weightedProducts) {
                componentList.add(Component.literal("  - ").withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.literal(String.valueOf(product.stack().getCount())).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" "))
                        .append(product.stack().getDisplayName().copy().withStyle(ChatFormatting.GOLD))
                        .append(Component.translatable("gtocore.trade_lottery.weight", product.weight()).withStyle(ChatFormatting.DARK_GREEN)));
            }
        }
        return ImmutableList.copyOf(componentList);
    }

    /**
     * 创建带项目符号的悬浮说明分区标题。
     *
     * @param translationKey 标题翻译键
     * @param color          标题和项目符号的颜色
     * @return 格式化后的标题组件
     */
    public static Component sectionHeader(String translationKey, ChatFormatting color) {
        return Component.literal("- ").withStyle(color).append(Component.translatable(translationKey).withStyle(color));
    }

    // ------------------- 内部类：交易资源组 -------------------
    public record TradeGroup(
                             List<ItemStack> items,
                             List<FluidStack> fluids,
                             O2LOpenCacheHashMap<String> currencies,
                             BigInteger energy,
                             BigInteger mana) {

        /**
         * 紧凑构造器
         */
        public TradeGroup {
            items = ImmutableList.copyOf(items);
            fluids = ImmutableList.copyOf(fluids);
            currencies = currencies.clone();
        }

        /**
         * 检查当前 TradeGroup 是否所有字段都为空（或无效）。
         *
         * @return 没有任何有效物品、流体、货币、能量或魔力时返回 {@code true}
         */
        public boolean isEmpty() {
            boolean isItemsEmpty = items.stream().allMatch(ItemStack::isEmpty);
            boolean isFluidsEmpty = fluids.stream().allMatch(FluidStack::isEmpty);
            boolean isCurrenciesEmpty = currencies.isEmpty() || currencies.values().longStream().allMatch(amount -> amount <= 0);
            boolean isEnergyEmpty = energy.equals(BigInteger.ZERO);
            boolean isManaEmpty = mana.equals(BigInteger.ZERO);
            return isItemsEmpty && isFluidsEmpty && isCurrenciesEmpty && isEnergyEmpty && isManaEmpty;
        }

        /**
         * 生成资源组在悬浮说明中的标题和资源明细。
         *
         * @param input_output {@code true} 表示价格/输入，{@code false} 表示商品/输出
         * @return 按显示顺序排列的悬浮文本行
         */
        public List<Component> getComponentList(boolean input_output) {
            List<Component> list = new ArrayList<>();
            ChatFormatting color = input_output ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GREEN;
            list.add(Component.literal("- ").withStyle(color)
                    .append(input_output ? Component.translatable("gtocore.trade_group.true").withStyle(ChatFormatting.DARK_RED) :
                            Component.translatable("gtocore.trade_group.false").withStyle(ChatFormatting.DARK_GREEN)));
            for (ItemStack itemStack : items) {
                list.add(Component.literal("  - ").withStyle(color)
                        .append(Component.literal(String.valueOf(itemStack.getCount())).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" "))
                        .append(itemStack.getDisplayName().copy().withStyle(ChatFormatting.GOLD)));
            }
            for (FluidStack fluidStack : fluids) {
                list.add(Component.literal("  - ").withStyle(color)
                        .append(Component.literal(String.valueOf(fluidStack.getAmount())).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" "))
                        .append(fluidStack.getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
            for (var iterator = currencies.object2LongEntrySet().fastIterator(); iterator.hasNext();) {
                var entry = iterator.next();
                list.add(Component.literal("  - ").withStyle(color)
                        .append(Component.literal(String.valueOf(entry.getLongValue())).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" "))
                        .append(Component.translatable("gtocore.currency." + entry.getKey()).withStyle(ChatFormatting.YELLOW)));
            }
            if (!energy.equals(BigInteger.ZERO)) {
                list.add(Component.literal("  - ").withStyle(color)
                        .append(Component.literal(energy.toString()).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" "))
                        .append(Component.literal("EU").withStyle(ChatFormatting.DARK_AQUA)));
            }
            if (!mana.equals(BigInteger.ZERO)) {
                list.add(Component.literal("  - ").withStyle(color)
                        .append(Component.literal(mana.toString()).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" "))
                        .append(Component.literal("Mana").withStyle(ChatFormatting.DARK_PURPLE)));
            }
            return list;
        }

        // =================== TradeGroup 的 Builder ===================
        public static class Builder {

            private final List<ItemStack> items = new ArrayList<>();
            private final List<FluidStack> fluids = new ArrayList<>();
            private final O2LOpenCacheHashMap<String> currencies = new O2LOpenCacheHashMap<>();
            private BigInteger energy = BigInteger.ZERO;
            private BigInteger mana = BigInteger.ZERO;

            public void addItem(ItemStack stack) {
                if (!stack.isEmpty()) {
                    this.items.add(stack);
                }
            }

            public void addFluid(FluidStack stack) {
                if (!stack.isEmpty()) {
                    this.fluids.add(stack);
                }
            }

            public void addCurrency(String currencyId, long amount) {
                if (amount > 0) {
                    this.currencies.put(currencyId, amount);
                }
            }

            public void withEnergy(BigInteger energy) {
                this.energy = energy;
            }

            public void withMana(BigInteger mana) {
                this.mana = mana;
            }

            public void withEnergy(long energy) {
                withEnergy(BigInteger.valueOf(energy));
            }

            public void withMana(long mana) {
                withMana(BigInteger.valueOf(mana));
            }

            public TradeGroup build() {
                return new TradeGroup(items, fluids, currencies, energy, mana);
            }
        }
    }

    // ------------------- 函数式接口 -------------------
    public record TradeCondition(TradeConditionCheck check, TradeConditionDescription description) {

        /**
         * 计算该条件当前允许的最大交易次数，并把负数结果收敛为 {@code 0}。
         *
         * @param data  当前交易数据
         * @param entry 正在检查的交易条目
         * @return 该条件允许的最大次数；{@code 0} 表示不满足条件
         */
        public int maxCount(TradeData data, TradeEntry entry) {
            return Math.max(0, check.test(data, entry));
        }

        /**
         * 根据当前条件余量生成面向玩家的限制说明。
         *
         * @param maxCount {@link #maxCount(TradeData, TradeEntry)} 返回的条件余量
         * @return 条件说明组件
         */
        public Component getDescription(int maxCount) {
            return description.create(maxCount);
        }
    }

    public record WeightedProduct(ItemStack stack, int weight) {

        public WeightedProduct {
            stack = stack.copy();
        }
    }

    @FunctionalInterface
    public interface TradeConditionCheck {

        /**
         * @param data  当前交易数据
         * @param entry 正在检查的交易条目
         * @return 条件允许的最大交易次数；{@code 0} 表示禁止，{@link Integer#MAX_VALUE} 表示不限制次数
         */
        int test(TradeData data, TradeEntry entry);
    }

    @FunctionalInterface
    public interface TradeConditionDescription {

        /**
         * @param maxCount 当前条件允许的剩余交易次数
         * @return 展示给玩家的限制说明
         */
        Component create(int maxCount);
    }

    @FunctionalInterface
    public interface TradeRunnable {

        /**
         * @param data       当前交易数据
         * @param entry      已执行资源变更的交易条目
         * @param multiplier 本次实际完成的交易次数
         */
        void run(TradeData data, TradeEntry entry, int multiplier);
    }

    // ------------------- TradeEntry 的链式构建器 -------------------
    public static class Builder {

        private IGuiTexture texture;
        private final List<Component> description = new ArrayList<>();
        private String unlockCondition;
        private final List<TradeCondition> conditions = new ArrayList<>();
        private final List<WeightedProduct> weightedProducts = new ArrayList<>();
        private TradeRunnable onExecute;
        private TradeGroup.Builder inputGroupBuilder = new TradeGroup.Builder();
        private TradeGroup.Builder outputGroupBuilder = new TradeGroup.Builder();

        // ------------------- 配置方法（链式调用） -------------------
        public Builder texture(IGuiTexture texture) {
            this.texture = texture;
            return this;
        }

        public Builder description(List<Component> components) {
            this.description.clear();
            if (components != null) {
                this.description.addAll(components);
            }
            return this;
        }

        public Builder addDescription(Component component) {
            this.description.add(component);
            return this;
        }

        public Builder unlockCondition(String condition) {
            this.unlockCondition = condition;
            return this;
        }

        /**
         * 添加一个限制条件；多个条件同时存在时取允许次数的最小值。
         *
         * @param condition 要添加的限制条件
         * @return 当前构建器
         */
        public Builder condition(TradeCondition condition) {
            this.conditions.add(condition);
            return this;
        }

        /**
         * 根据检查逻辑和展示文案添加一个限制条件。
         *
         * @param check       条件允许次数的计算逻辑
         * @param description 根据条件余量生成文案的逻辑
         * @return 当前构建器
         */
        public Builder condition(TradeConditionCheck check, TradeConditionDescription description) {
            return condition(new TradeCondition(check, description));
        }

        /**
         * 添加一个仅用于悬浮说明展示的抽奖商品及权重。
         *
         * @param stack  抽奖商品
         * @param weight 商品权重
         * @return 当前构建器
         */
        public Builder weightedProduct(ItemStack stack, int weight) {
            this.weightedProducts.add(new WeightedProduct(stack, weight));
            return this;
        }

        /**
         * 设置资源输入输出完成后执行的交易回调。
         *
         * @param runnable 交易完成回调
         * @return 当前构建器
         */
        public Builder onExecute(TradeRunnable runnable) {
            this.onExecute = runnable;
            return this;
        }

        // ------------------- 输入资源配置 -------------------
        public Builder input(TradeGroup.Builder builder) {
            this.inputGroupBuilder = builder;
            return this;
        }

        public Builder inputItem(ItemStack stack) {
            this.inputGroupBuilder.addItem(stack);
            return this;
        }

        public Builder inputFluid(FluidStack stack) {
            this.inputGroupBuilder.addFluid(stack);
            return this;
        }

        public Builder inputCurrency(String currencyId, long amount) {
            this.inputGroupBuilder.addCurrency(currencyId, amount);
            return this;
        }

        public Builder inputEnergy(long energy) {
            this.inputGroupBuilder.withEnergy(energy);
            return this;
        }

        public Builder inputEnergy(BigInteger energy) {
            this.inputGroupBuilder.withEnergy(energy);
            return this;
        }

        public Builder inputMana(long mana) {
            this.inputGroupBuilder.withMana(mana);
            return this;
        }

        public Builder inputMana(BigInteger mana) {
            this.inputGroupBuilder.withMana(mana);
            return this;
        }

        // ------------------- 输出资源配置 -------------------
        public Builder output(TradeGroup.Builder builder) {
            this.outputGroupBuilder = builder;
            return this;
        }

        public Builder outputItem(ItemStack stack) {
            this.outputGroupBuilder.addItem(stack);
            return this;
        }

        public Builder outputFluid(FluidStack stack) {
            this.outputGroupBuilder.addFluid(stack);
            return this;
        }

        public Builder outputCurrency(String currencyId, long amount) {
            this.outputGroupBuilder.addCurrency(currencyId, amount);
            return this;
        }

        public Builder outputEnergy(long energy) {
            this.outputGroupBuilder.withEnergy(energy);
            return this;
        }

        public Builder outputEnergy(BigInteger energy) {
            this.outputGroupBuilder.withEnergy(energy);
            return this;
        }

        public Builder outputMana(long mana) {
            this.outputGroupBuilder.withMana(mana);
            return this;
        }

        public Builder outputMana(BigInteger mana) {
            this.outputGroupBuilder.withMana(mana);
            return this;
        }

        /**
         * 构建不可变 TradeEntry 实例
         *
         * @return 包含当前全部配置的交易条目
         */
        public TradeEntry build() {
            TradeGroup inputGroup = inputGroupBuilder.build();
            TradeGroup outputGroup = outputGroupBuilder.build();
            return new TradeEntry(
                    texture,
                    unlockCondition,
                    ImmutableList.copyOf(conditions),
                    onExecute,
                    inputGroup,
                    outputGroup,
                    buildStaticTooltip(description, inputGroup, outputGroup, weightedProducts));
        }
    }
}
