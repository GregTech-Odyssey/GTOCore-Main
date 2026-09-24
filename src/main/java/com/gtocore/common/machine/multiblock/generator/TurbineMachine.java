package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.Scanned;
import com.gtolib.api.annotation.dynamic.DynamicInitialValue;
import com.gtolib.api.annotation.dynamic.DynamicInitialValueTypes;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.machine.part.ItemPartMachine;
import com.gtolib.api.machine.trait.CoilTrait;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.RecipeInfo;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.PercentField;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.number.NumberSettingPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.hepdd.gtmthings.utils.FormatUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Scanned
public class TurbineMachine extends ElectricMultiblockMachine {

    @DynamicInitialValue(key = "gtocore.machine.mega_turbine.high_speed_mode_output_multiplier", typeKey = DynamicInitialValueTypes.KEY_MULTIPLY, easyValue = "4.0F", normalValue = "3.0F", expertValue = "2.5F", cn = "高速模式输出乘数 : %s", en = "High Speed Mode Output Multiplier : %s Multiplier")
    private static float highSpeedModeOutputMultiplier = 3.0F;
    @DynamicInitialValue(key = "gtocore.machine.mega_turbine.high_speed_mode_rotor_damage_multiplier", typeKey = DynamicInitialValueTypes.KEY_MULTIPLY, easyValue = "4", normalValue = "10", expertValue = "12", cn = "高速模式转子损坏乘数 : %s", en = "High Speed Mode Rotor Damage Multiplier : %s Multiplier")
    private static int highSpeedModeRotorDamageMultiplier = 10;
    @DynamicInitialValue(key = "gtocore.machine.mega_turbine.high_speed_mode_machine_fault", typeKey = DynamicInitialValueTypes.KEY_MULTIPLY, easyValue = "4F", normalValue = "8F", expertValue = "10F", cn = "高速模式机器故障乘数 : %s", en = "High Speed Mode Machine Fault Multiplier : %s Multiplier")
    private static float highSpeedModeMachineFault = 8.0F;

    private final long baseEUOutput;
    private final int tier;
    private final boolean mega;
    private long energyPerTick;
    @SaveToDisk(defaultValue = "false")
    private boolean highSpeedMode;
    @SaveToDisk(defaultValue = "1.0")
    private float highSpeedFactor = 1.0f;
    final List<RotorHolderPartMachine> rotorHolderMachines = new ArrayList<>();
    private ItemPartMachine rotorHatchPartMachine;
    private final ConditionalSubscriptionHandler rotorSubs;

    private double extraOutput = 1;
    private double extraDamage = 1;
    private double extraEfficiency = 1;
    double damageBase = 2.2;
    private float accumulatedDamage = 0;

    public TurbineMachine(MetaMachineBlockEntity holder, int tier, boolean special, boolean mega) {
        super(holder);
        this.mega = mega;
        this.tier = tier;
        baseEUOutput = (long) (GTValues.V[tier] * (mega ? 4 : 1) * (special ? 2.5 : 2));
        rotorSubs = new ConditionalSubscriptionHandler(this, this::rotorUpdate, 20, () -> rotorHatchPartMachine != null);
    }

    private void rotorUpdate() {
        if (!isActive()) {
            rotorSubs.updateSubscription();
            if (rotorHatchPartMachine == null || rotorHatchPartMachine.getInventory().isEmpty()) return;
            boolean full = true;
            for (RotorHolderPartMachine part : rotorHolderMachines) {
                if (part.getRotorStack().isEmpty()) {
                    full = false;
                    part.setRotorStack(rotorHatchPartMachine.getInventory().getStackInSlot(0));
                    rotorHatchPartMachine.getInventory().setStackInSlot(0, ItemStack.EMPTY);
                    break;
                }
            }
            if (full) {
                rotorSubs.unsubscribe();
            }
        }
    }

    @Override
    public boolean matchRecipeInput(RecipeHandlerUnit unit, GTRecipe recipe) {
        for (RotorHolderPartMachine part : rotorHolderMachines) {
            if (part.getRotorStack().isEmpty()) return false;
        }
        return super.matchRecipeInput(unit, recipe);
    }

    @Override
    public void onPartScan(IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof RotorHolderPartMachine rotorHolderMachine) {
            rotorHolderMachines.add(rotorHolderMachine);
            traitSubscriptions.add(rotorHolderMachine.inventory.addChangedListener(rotorSubs::updateSubscription));
        } else if (rotorHatchPartMachine == null && part instanceof ItemPartMachine rotorHatchPart) {
            rotorHatchPartMachine = rotorHatchPart;
            traitSubscriptions.add(rotorHatchPartMachine.getInventory().addChangedListener(rotorSubs::updateSubscription));
        }
    }

    @Override
    public void onStructureFormed() {
        rotorHolderMachines.clear();
        super.onStructureFormed();
        if (mega) {
            rotorSubs.initialize(getLevel());
            if (GTOCore.isExpert() && this instanceof MegaTurbine) {
                damageBase = Math.max(2.2 - 0.08 * ((MegaTurbine) this).getCasingTier(GTORecipeDataKeys.GLASS_TIER), 1.2);
            }
        }
        if (formedAmount > 0) {
            if (mega) {
                extraOutput = 3;
                extraDamage = 3;
                extraEfficiency = 1.3;
            } else {
                extraOutput = 2;
                extraDamage = 2;
                extraEfficiency = 1.2;
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        rotorHolderMachines.clear();
        rotorHatchPartMachine = null;
        extraOutput = 1;
        extraDamage = 1;
        extraEfficiency = 1;
        damageBase = 2.0;
    }

    @Override
    public void onWorking() {
        if (getOffsetTimer() % 20 == 0) {
            float damageMultiplier = highSpeedMode ? getHighSpeedModeDamageMultiplier() : 1;
            // RotorHolderPartMachine applies the base damage in super.onWorking().
            accumulatedDamage += (float) (damageMultiplier * extraDamage) - 1;
            if (accumulatedDamage >= 1) {
                int damageToApply = (int) accumulatedDamage;
                accumulatedDamage -= damageToApply;
                for (RotorHolderPartMachine part : rotorHolderMachines) {
                    part.damageRotor(damageToApply);
                }
            }
        }
        super.onWorking();
    }

    @Override
    public void afterWorking() {
        energyPerTick = 0;
        var recipe = getRecipeLogic().getLastRecipe();
        for (IMultiPart part : getParts()) {
            if (highSpeedMode && recipe != null && part instanceof IMaintenanceMachine maintenanceMachine) {
                maintenanceMachine.calculateMaintenance(maintenanceMachine, (int) (highSpeedModeMachineFault * recipe.duration * extraDamage));
                continue;
            }
            if (part instanceof IWorkableMultiPart workableMultiPart) workableMultiPart.afterWorking(this);
        }
    }

    @Nullable
    private RotorHolderPartMachine getRotorHolder() {
        for (RotorHolderPartMachine part : rotorHolderMachines) {
            return part;
        }
        return null;
    }

    private int getRotorSpeed() {
        if (mega) {
            Set<Material> material = new OpenCacheHashSet<>(2);
            int speed = 0;
            for (RotorHolderPartMachine part : rotorHolderMachines) {
                ItemStack stack = part.getRotorStack();
                TurbineRotorBehaviour rotorBehaviour = TurbineRotorBehaviour.getBehaviour(stack);
                if (rotorBehaviour == null) return -1;
                material.add(rotorBehaviour.getPartMaterial(stack));
                speed += part.getRotorSpeed();
            }
            return material.size() == 1 ? (speed / 12) : -1;
        }
        RotorHolderPartMachine rotor = getRotorHolder();
        if (rotor != null) {
            return rotor.getRotorSpeed();
        }
        return 0;
    }

    private long getVoltage() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return (long) (baseEUOutput * rotorHolder.getTotalPower() * (highSpeedMode ? getHighSpeedModeOutputMultiplier() : 1L) / 100 * extraOutput);
        }
        return 0;
    }

    //////////////////////////////////////
    // ****** Recipe Logic *******//
    //////////////////////////////////////
    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        long EUt = recipe.getOutputEUt();
        if (rotorHolder == null || EUt <= 0) return null;
        int rotorSpeed = getRotorSpeed();
        if (rotorSpeed < 0) return null;
        int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
        long turbineMaxVoltage = Math.min(getOverclockVoltage(), (long) (getVoltage() * Math.pow((double) Math.min(maxSpeed, rotorSpeed) / maxSpeed, 2)));
        recipe = ParallelLogic.accurateContentParallel(this, unit, recipe, turbineMaxVoltage / EUt);
        if (recipe == null) return null;
        long eut = Math.min(turbineMaxVoltage, recipe.parallels * EUt);
        energyPerTick = eut;
        recipe.duration = (int) (recipe.duration * rotorHolder.getTotalEfficiency() * extraEfficiency / 100);
        recipe.setEUt(-eut);
        return recipe;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeInfo capability) {
        return true;
    }

    //////////////////////////////////////
    // ******* GUI ********//
    //////////////////////////////////////
    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(GTOGuiTextures.HIGH_SPEED_OFF, GTOGuiTextures.HIGH_SPEED_ON, () -> highSpeedMode, (clickData, pressed) -> {
            for (RotorHolderPartMachine part : rotorHolderMachines) {
                part.setRotorSpeed(0);
            }
            highSpeedMode = pressed;
            onChanged();
        }).setTooltipsSupplier(pressed -> List.of(Component.translatable("gtocore.machine.mega_turbine.high_speed_mode").append("[").append(Component.translatable(pressed ? "gtocore.machine.on" : "gtocore.machine.off")).append("]"))));

        if (mega && GTOCore.isExpert()) {
            configuratorPanel.attachConfigurators(new IFancyConfigurator() {

                @Override
                public Component getTitle() {
                    return Component.translatable(ADJUSTMENT1);
                }

                @Override
                public List<Component> getTooltips() {
                    return List.of(
                            Component.translatable(DESC1),
                            Component.translatable(DESC2),
                            Component.translatable(DESC3),
                            Component.translatable(DESC4),
                            Component.translatable(DESC5));
                }

                @Override
                public IGuiTexture getIcon() {
                    return WidgetIcons.SETTINGS;
                }

                @Override
                public Widget createConfigurator() {
                    return createAdjustmentPanel();
                }
            });
        }
    }

    /// 调节乘数范围 0.1 ~ 5（10% ~ 500%），步进 0.01（1%，Shift / Ctrl 为 10% / 100%，与原来的加减一致）
    private static final float ADJUST_MIN = 0.1f;
    private static final float ADJUST_MAX = 5f;

    /**
     * 专家模式高速乘数调节（机器左侧小组件）：
     *
     * <pre>
     *  ┌──────────────────────────┐
     *  │ 输出乘数 ……………… 7.50x │   ← 状态面板（只读，服务端下发）
     *  │ 转子损坏乘数 ………… 10.00x │
     *  └──────────────────────────┘
     *  调节乘数
     *  [-1%] [  100  ] % [+1%]      ← 百分数调节器（回车确认）；转子转动时整组禁止操作
     *  [          重置          ]
     * </pre>
     *
     * 数值都由组件自己从服务端下发（状态行、数值输入、禁用条件），加减、输入、重置都在服务端执行并夹到 0.1 ~ 5。
     */
    private Widget createAdjustmentPanel() {
        var status = new StatusPanel(NumberSettingPage.COMPACT_WIDTH);
        status.addLine(OUTPUT_MULTIPLIER, multiplierText(this::getHighSpeedModeOutputMultiplier));
        status.addLine(DAMAGE_MULTIPLIER, multiplierText(this::getHighSpeedModeDamageMultiplier));

        // 范围只有 490%，用不上 ±1000% 那一档：只给 1% / 10% / 100%（Ctrl+Shift 沿用 100%）
        var factor = PercentField.of(NumberSettingPage.COMPACT_WIDTH, () -> highSpeedFactor, value -> setHighSpeedFactor((float) value),
                ADJUST_MIN, ADJUST_MAX, PercentField.DEFAULT_STEP, 1, 10, 100);
        var reset = Button.translatable(LayoutStyle.AUTO, RESET).setOnServerClick(() -> setHighSpeedFactor(1.0f));
        // 两种锁定原因分两层：外层转子缺失 / 材料不一致，内层转子仍在转动（每个元素只能设一次禁用）
        var controls = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(TextLine.translatable(LayoutStyle.AUTO, ADJUST_FACTOR).setColor(UITheme.TEXT), factor, reset)
                .disabled(this::isAnyRotorSpinning, ROTOR_SPINNING);
        var guarded = UIElement.column(LayoutStyle.AUTO).addChildren(controls)
                .disabled(() -> !hasMatchingRotors(), ROTOR_MISMATCH);

        return UIElement.column(NumberSettingPage.COMPACT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP))
                .addChildren(status, guarded);
    }

    /** 调节乘数：夹到 0.1 ~ 5，变了才标记存盘（字段只有 @SaveToDisk，不标脏重启会丢）。 */
    private void setHighSpeedFactor(float factor) {
        factor = Math.clamp(factor, ADJUST_MIN, ADJUST_MAX);
        if (factor == highSpeedFactor) return;
        highSpeedFactor = factor;
        onChanged();
    }

    /** 转子仓是否都装了转子且材料相同（界面每刻在服务端判定，不分配）。 */
    private boolean hasMatchingRotors() {
        Material first = null;
        for (RotorHolderPartMachine part : rotorHolderMachines) {
            ItemStack stack = part.getRotorStack();
            TurbineRotorBehaviour behaviour = TurbineRotorBehaviour.getBehaviour(stack);
            if (behaviour == null) return false;
            Material material = behaviour.getPartMaterial(stack);
            if (first == null) first = material;
            else if (first != material) return false;
        }
        return first != null;
    }

    /** 是否有转子仍在转动。 */
    private boolean isAnyRotorSpinning() {
        for (RotorHolderPartMachine part : rotorHolderMachines) {
            if (part.getRotorSpeed() != 0) return true;
        }
        return false;
    }

    /** 状态行里的乘数文字（服务端每刻取值），数值不变时复用上次的文字，不每刻格式化。 */
    private static Supplier<Component> multiplierText(DoubleSupplier value) {
        return new Supplier<>() {

            private double last = Double.NaN;
            private Component text = Component.empty();

            @Override
            public Component get() {
                double current = value.getAsDouble();
                if (current != last) {
                    last = current;
                    text = Component.literal(String.format("%.2fx", current));
                }
                return text;
            }
        };
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        var v = getVoltage();
        textList.add(Component.translatable(ESTIMATED_MAX_OUTPUT, FormattingUtil.formatNumbers(v))
                .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.empty()
                                .append(Component.literal(FormatUtil.voltageAmperage(BigDecimal.valueOf(v)).toEngineeringString()).append("A "))
                                .append(FormatUtil.voltageName(BigDecimal.valueOf(v)))))));
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.getRotorEfficiency() > 0) {
            textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_speed", FormattingUtil.formatNumbers(getRotorSpeed() * (highSpeedMode ? highSpeedModeOutputMultiplier : 1) * extraOutput), FormattingUtil.formatNumbers(rotorHolder.getMaxRotorHolderSpeed() * (highSpeedMode ? highSpeedModeOutputMultiplier : 1) * extraOutput)));
            textList.add(Component.translatable("gtceu.multiblock.turbine.efficiency", rotorHolder.getTotalEfficiency() * extraEfficiency));
            if (isActive()) {
                String voltageName = GTValues.VNF[GTUtil.getTierByVoltage(energyPerTick)];
                textList.add(3, Component.translatable("gtceu.multiblock.turbine.energy_per_tick", FormattingUtil.formatNumbers(energyPerTick), voltageName));
            }
            if (!mega) {
                int rotorDurability = rotorHolder.getRotorDurabilityPercent();
                if (rotorDurability > 10) {
                    textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability));
                } else {
                    textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                }
            }
        }
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    private float getHighSpeedModeOutputMultiplier() {
        if (!GTOCore.isExpert()) {
            return highSpeedModeOutputMultiplier;
        }
        return highSpeedModeOutputMultiplier * highSpeedFactor;
    }

    private float getHighSpeedModeDamageMultiplier() {
        if (!GTOCore.isExpert()) {
            return highSpeedModeRotorDamageMultiplier;
        }
        return Math.max(1f, (float) (highSpeedModeRotorDamageMultiplier * Math.pow(damageBase, highSpeedFactor - 1)));
    }

    public static class MegaTurbine extends TurbineMachine implements ICoilMachine, ITierCasingMachine {

        private final CoilTrait coilTrait;
        private final TierCasingTrait tierCasingTrait;
        private float workAccumulation = 0;

        public MegaTurbine(MetaMachineBlockEntity holder, int tier, boolean special) {
            super(holder, tier, special, true);
            coilTrait = new CoilTrait(this, false, false);
            this.tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.GLASS_TIER);
        }

        @Override
        public void onWorking() {
            if (getCoilTier() > 0) {
                this.workAccumulation += getCoilTier() * 1.25f + 4;
                int addition = (int) Math.floor(this.workAccumulation);
                this.workAccumulation -= addition;
                for (var part : this.rotorHolderMachines) {
                    part.setRotorSpeed(Math.min(part.getRotorSpeed() + addition, part.getMaxRotorHolderSpeed()));
                }
            }
            super.onWorking();
        }

        @Override
        public ICoilType getCoilType() {
            return coilTrait.getCoilType();
        }

        @Override
        public void customText(List<Component> textList) {
            super.customText(textList);
            textList.add(Component.translatable(COIL_BONUS, getCoilTier(), getCoilTier() * 20));
            if (GTOCore.isExpert())
                textList.add(Component.translatable(GLASS_BONUS, getCasingTier(GTORecipeDataKeys.GLASS_TIER), FormattingUtil.formatNumber2Places(damageBase)));
        }

        @Override
        public Reference2IntMap<TierDataKey> getCasingTiers() {
            return tierCasingTrait.getCasingTiers();
        }
    }

    @RegisterLanguage(cn = "线圈等级: %s，转子启动增速 %s%%", en = "Coil Tier: %s, Rotor Launch Speed Bonus %s%%")
    public static final String COIL_BONUS = "gtocore.machine.mega_turbine.coil_tier";
    @RegisterLanguage(cn = "玻璃等级: %s，转子损坏乘数加成：%s", en = "Glass Tier: %s, Rotor Damage Multiplier Bonus: %s")
    public static final String GLASS_BONUS = "gtocore.machine.mega_turbine.glass_tier";
    @RegisterLanguage(cn = "高速模式乘数调节：", en = "High Speed Mode Multiplier Adjustment:")
    public static final String ADJUSTMENT1 = "gtocore.machine.mega_turbine.expert.adjustment.1";
    @RegisterLanguage(cn = "输出乘数", en = "Output Multiplier")
    public static final String OUTPUT_MULTIPLIER = "gtocore.machine.mega_turbine.expert.output_multiplier";
    @RegisterLanguage(cn = "转子损坏乘数", en = "Rotor Damage Multiplier")
    public static final String DAMAGE_MULTIPLIER = "gtocore.machine.mega_turbine.expert.damage_multiplier";
    @RegisterLanguage(cn = "预计最大输出：%s EU/t", en = "Estimated Max Output: %s EU/t")
    public static final String ESTIMATED_MAX_OUTPUT = "gtocore.machine.mega_turbine.expert.estimated_max_output";
    @RegisterLanguage(cn = "专家模式下，允许调节高速模式下的输出乘数。", en = "In Expert Mode, allows adjustment of the output multiplier in High Speed Mode.")
    public static final String DESC1 = "gtocore.machine.mega_turbine.expert.desc.1";
    @RegisterLanguage(cn = "不过，调节输出乘数会同时大幅牺牲转子寿命。", en = "However, adjusting the output multiplier will also significantly sacrifice rotor durability.")
    public static final String DESC2 = "gtocore.machine.mega_turbine.expert.desc.2";
    @RegisterLanguage(cn = "乘数调节范围：0.1 - 5。", en = "Adjustment Range: 0.1x - 5x.")
    public static final String DESC3 = "gtocore.machine.mega_turbine.expert.desc.3";
    @RegisterLanguage(cn = "公式：输出乘数 = 基础乘数 × 调节乘数", en = "Formula: Output Multiplier = Base Multiplier x Adjustment Multiplier")
    public static final String DESC4 = "gtocore.machine.mega_turbine.expert.desc.4";
    @RegisterLanguage(cn = "转子损坏乘数 = 基础乘数 × max(2.2 - 0.08 × 玻璃等级, 1.2) ^ (调节乘数 - 1)", en = "Rotor Damage Multiplier = Base Multiplier x max(2.2 - 0.08 * Glass Tier, 1.2) ^ (Adjustment Multiplier - 1)")
    public static final String DESC5 = "gtocore.machine.mega_turbine.expert.desc.5";
    @RegisterLanguage(cn = "调节乘数（%%）", en = "Adjustment Multiplier (%%)")
    public static final String ADJUST_FACTOR = "gtocore.machine.mega_turbine.expert.adjust_factor";
    @RegisterLanguage(cn = "重置", en = "Reset")
    public static final String RESET = "gtocore.machine.mega_turbine.expert.reset";
    @RegisterLanguage(cn = "转子仍在转动，停转后才能调节", en = "The rotor is still spinning; adjust after it stops")
    public static final String ROTOR_SPINNING = "gtocore.machine.mega_turbine.expert.rotor_spinning";
    @RegisterLanguage(cn = "转子缺失或材料不一致", en = "Rotors are missing or of different materials")
    public static final String ROTOR_MISMATCH = "gtocore.machine.mega_turbine.expert.rotor_mismatch";
}
