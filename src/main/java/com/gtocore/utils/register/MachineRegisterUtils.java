package com.gtocore.utils.register;

import com.gtocore.common.machine.mana.SimpleWorkManaMachine;

import com.gtolib.GTOCore;
import com.gtolib.api.GTOValues;
import com.gtolib.api.blockentity.ManaMachineBlockEntity;
import com.gtolib.api.machine.SimpleNoEnergyMachine;
import com.gtolib.api.machine.impl.part.WirelessEnergyHatchPartMachine;
import com.gtolib.api.recipe.GTORecipeModifiers;
import com.gtolib.api.registries.GTOMachineBuilder;
import com.gtolib.api.registries.MultiblockBuilder;
import com.gtolib.utils.GTOUtils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.SimpleGeneratorMachineRenderer;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.hepdd.gtmthings.GTMThings;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gtolib.api.GTOValues.MANACN;
import static com.gtolib.api.GTOValues.MANAN;
import static com.gtolib.api.registries.GTORegistration.GTM;
import static com.gtolib.api.registries.GTORegistration.GTO;
import static com.gtolib.utils.register.BlockRegisterUtils.addLang;

public final class MachineRegisterUtils {

    private MachineRegisterUtils() {}

    public static final int[] MANA_TIERS = GTValues.tiersBetween(LV, ZPM);

    public static final BiConsumer<IMultiController, List<Component>> CHEMICAL_PLANT_DISPLAY = (controller, components) -> {
        double value = 1 - ((ICoilMachine) controller).getCoilTier() * 0.05;
        components.add(Component.translatable("gtocore.machine.eut_multiplier.tooltip", FormattingUtil.formatNumbers(value)));
        components.add(Component.translatable("gtocore.machine.duration_multiplier.tooltip", FormattingUtil.formatNumbers(value)));
    };

    public static GTOMachineBuilder machine(String name, String cn, Function<MetaMachineBlockEntity, MetaMachine> metaMachine) {
        addLang(name, cn);
        return GTO.machine(name, metaMachine);
    }

    public static MultiblockBuilder multiblock(String name, String cn, Function<MetaMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        addLang(name, cn);
        return GTO.multiblock(name, metaMachine);
    }

    public static MachineDefinition[] registerWirelessEnergyHatch(IO io, int amperage, PartAbility ability) {
        String id = io == IO.IN ? "input" : "output";
        String iao = io == IO.IN ? "in" : "out";
        String render = "wireless_energy_hatch";
        render = switch (amperage) {
            case 2 -> render;
            case 4 -> render + "_4a";
            case 16 -> render + "_16a";
            case 64 -> render + "_64a";
            default -> "wireless_laser_hatch.target";
        };
        String finalRender = render;
        int t = LV;
        if (amperage == 64) t = EV;
        else if (amperage > 64) t = IV;
        return registerTieredMachines("wireless_" + id + "_hatch" + (amperage > 2 ? "_" + amperage + "a" : ""), tier -> (amperage > 2 ? amperage + (amperage > 64 ? "§e安§r" : "安") : "") + GTOValues.VNFR[tier] + "无线" + (io == IO.IN ? "能源" : "动力") + "仓",
                (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, io, amperage), (tier, builder) -> {
                    builder.langValue(GTOValues.VNFR[tier] + " " + (amperage > 2 ? FormattingUtil.formatNumbers(amperage) + (amperage > 64 ? "§eA§r " : "A ") : "") + "Wireless " + (io == IO.IN ? "Energy" : "Dynamo") + " Hatch")
                            .allRotation()
                            .abilities(ability)
                            .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_" + iao, FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                                    Component.translatable("gtceu.universal.tooltip.amperage_" + iao, amperage),
                                    Component.translatable("gtceu.universal.tooltip.energy_storage_capacity", FormattingUtil.formatNumbers(WirelessEnergyHatchPartMachine.getHatchEnergyCapacity(tier, amperage))),
                                    Component.translatable("gtmthings.machine.wireless_energy_hatch." + id + ".tooltip"));
                    return builder.renderer(() -> new OverlayTieredMachineRenderer(tier, GTMThings.id("block/machine/part/" + finalRender)))
                            .register();
                },
                tiersBetween(t, MAX));
    }

    public static MachineDefinition[] registerLaserHatch(IO io, int amperage, PartAbility ability) {
        String name = io == IO.IN ? "target" : "source";
        return registerTieredMachines(amperage + "a_laser_" + name + "_hatch", tier -> amperage + "§e安§r" + GTOValues.VNFR[tier] + "激光" + (io == IO.IN ? "靶" : "源") + "仓",
                (holder, tier) -> new LaserHatchPartMachine(holder, io, tier, amperage), (tier, builder) -> builder
                        .langValue(GTOValues.VNFR[tier] + " " + FormattingUtil.formatNumbers(amperage) + "§eA§r Laser " + FormattingUtil.toEnglishName(name) + " Hatch")
                        .allRotation()
                        .tooltips(Component.translatable("gtceu.machine.laser_hatch." + name + ".tooltip"),
                                Component.translatable("gtceu.machine.laser_hatch.both.tooltip"))
                        .notAllowSharedTooltips()
                        .abilities(ability)
                        .renderer(() -> new OverlayTieredMachineRenderer(tier, GTCEu.id("block/machine/part/laser_hatch." + name)))
                        .register(),
                tiersBetween(IV, MAX));
    }

    public static MachineDefinition[] registerSimpleGenerator(String name, String cn, GTRecipeType recipeType, Int2IntFunction tankScalingFunction, int... tiers) {
        return registerTieredMachines(name, tier -> "%s%s%s".formatted(GTOValues.VLVHCN[tier], cn, VLVT[tier]),
                (holder, tier) -> new SimpleGeneratorMachine(holder, tier, 0.1F * tier, tankScalingFunction),
                (tier, builder) -> builder
                        .langValue("%s %s%s".formatted(VLVH[tier], FormattingUtil.toEnglishName(name), VLVT[tier]))
                        .editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .allRotation()
                        .recipeType(recipeType)
                        .recipeModifier(GTORecipeModifiers.SIMPLE_GENERATOR_MACHINEMODIFIER)
                        .addOutputLimit(ItemRecipeInfo.INSTANCE, 0)
                        .addOutputLimit(FluidRecipeInfo.INSTANCE, 0)
                        .renderer(() -> new SimpleGeneratorMachineRenderer(tier, GTOCore.id("block/generators/" + name)))
                        .tooltips(Component.translatable("gtocore.machine.efficiency.tooltip", GTOUtils.getGeneratorEfficiency(recipeType, tier)).append("%"))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.amperage_out", GTOUtils.getGeneratorAmperage(tier)))
                        .tooltips(GTMachineUtils.workableTiered(tier, V[tier], V[tier] << 6, recipeType, tankScalingFunction.apply(tier), false))
                        .register(),
                tiers);
    }

    public static MachineDefinition[] registerRocketSimpleGenerator(String name, String cn, GTRecipeType recipeType, Int2IntFunction tankScalingFunction, int... tiers) {
        return registerTieredMachines(name, tier -> "%s%s%s".formatted(GTOValues.VLVHCN[tier], cn, VLVT[tier]),
                (holder, tier) -> new SimpleGeneratorMachine(holder, tier, 0.1F * tier, tankScalingFunction),
                (tier, builder) -> builder
                        .langValue("%s %s%s".formatted(VLVH[tier], FormattingUtil.toEnglishName(name), VLVT[tier]))
                        .editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .allRotation()
                        .workableInSpace()
                        .recipeType(recipeType)
                        .recipeModifier(GTORecipeModifiers.SIMPLE_GENERATOR_MACHINEMODIFIER)
                        .addOutputLimit(ItemRecipeInfo.INSTANCE, 0)
                        .addOutputLimit(FluidRecipeInfo.INSTANCE, 0)
                        .renderer(() -> new SimpleGeneratorMachineRenderer(tier, GTOCore.id("block/generators/" + name)))
                        .tooltips(Component.translatable("gtocore.machine.efficiency.tooltip", GTOUtils.getGeneratorEfficiency(recipeType, tier)).append("%"))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.amperage_out", GTOUtils.getGeneratorAmperage(tier)))
                        .tooltips(GTMachineUtils.workableTiered(tier, V[tier], V[tier] << 6, recipeType, tankScalingFunction.apply(tier), false))
                        .register(),
                tiers);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, String cn, GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction) {
        return registerSimpleMachines(name, cn, recipeType, tankScalingFunction, GTMachineUtils.ELECTRIC_TIERS);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, String cn,
                                                             GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction,
                                                             int... tiers) {
        return registerSimpleMachines(name, cn, recipeType, tankScalingFunction, GTOCore.id("block/machines/" + name), tiers);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, String cn,
                                                             GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction,
                                                             ResourceLocation workableModel, int... tiers) {
        return registerTieredMachines(name, tier -> "%s%s%s".formatted(GTOValues.VLVHCN[tier], cn, VLVT[tier]),
                (holder, tier) -> new SimpleTieredMachine(holder, tier, tankScalingFunction), (tier, builder) -> {
                    builder.recipeModifier(GTORecipeModifiers.UPGRADE_OVERCLOCK);
                    return builder
                            .langValue("%s %s%s".formatted(VLVH[tier], FormattingUtil.toEnglishName(name), VLVT[tier]))
                            .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                            .nonYAxisRotation()
                            .recipeType(recipeType)
                            .workableTieredHullRenderer(workableModel)
                            .tooltips(GTMachineUtils.workableTiered(tier, V[tier], V[tier] << 6, recipeType,
                                    tankScalingFunction.apply(tier), true))
                            .register();
                },
                tiers);
    }

    public static MachineDefinition[] registerSimpleNoEnergyMachines(String name, String cn,
                                                                     GTRecipeType recipeType,
                                                                     Int2IntFunction tankScalingFunction,
                                                                     int... tiers) {
        return registerSimpleNoEnergyMachines(name, cn, recipeType, tankScalingFunction, GTOCore.id("block/machines/" + name), tiers);
    }

    private static MachineDefinition[] registerSimpleNoEnergyMachines(String name, String cn,
                                                                      GTRecipeType recipeType,
                                                                      Int2IntFunction tankScalingFunction,
                                                                      ResourceLocation workableModel, int... tiers) {
        return registerTieredMachines(name, tier -> "%s%s%s".formatted(GTOValues.VLVHCN[tier], cn, VLVT[tier]),
                (holder, tier) -> new SimpleNoEnergyMachine(holder, tier, tankScalingFunction), (tier, builder) -> builder
                        .langValue("%s %s%s".formatted(VLVH[tier], FormattingUtil.toEnglishName(name), VLVT[tier]))
                        .editableUI(SimpleNoEnergyMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .nonYAxisRotation()
                        .recipeType(recipeType)
                        .noRecipeModifier()
                        .workableTieredHullRenderer(workableModel)
                        .tooltips(workableNoEnergy(recipeType, tankScalingFunction.apply(tier)))
                        .register(),
                tiers);
    }

    public static Component[] workableNoEnergy(GTRecipeType recipeType, long tankCapacity) {
        List<Component> tooltipComponents = new ArrayList<>();
        if (recipeType.getMaxInputs(FluidRecipeInfo.INSTANCE) > 0 ||
                recipeType.getMaxOutputs(FluidRecipeInfo.INSTANCE) > 0)
            tooltipComponents
                    .add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                            FormattingUtil.formatNumbers(tankCapacity)));
        return tooltipComponents.toArray(Component[]::new);
    }

    public static MachineDefinition[] registerTieredMachines(String name, Function<Integer, String> cn, BiFunction<MetaMachineBlockEntity, Integer, MetaMachine> factory, BiFunction<Integer, GTOMachineBuilder, MachineDefinition> builder, int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[TIER_COUNT];
        for (int tier : tiers) {
            String n = VN[tier].toLowerCase(Locale.ROOT) + "_" + name;
            if (cn != null) addLang(n, cn.apply(tier));
            GTOMachineBuilder register = GTO.machine(n, holder -> factory.apply(holder, tier)).tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSteamMachines(String name, String cn,
                                                                                   BiFunction<MetaMachineBlockEntity, Boolean, MetaMachine> factory,
                                                                                   BiFunction<Boolean, GTOMachineBuilder, MachineDefinition> builder) {
        MachineDefinition lowTier = builder.apply(false,
                machine("lp_%s".formatted(name), "低压蒸汽%s".formatted(cn), holder -> factory.apply(holder, false))
                        .langValue("Low Pressure " + FormattingUtil.toEnglishName(name))
                        .tier(0));
        MachineDefinition highTier = builder.apply(true,
                machine("hp_%s".formatted(name), "高压蒸汽%s".formatted(cn), holder -> factory.apply(holder, true))
                        .langValue("High Pressure " + FormattingUtil.toEnglishName(name))
                        .tier(1));
        return Pair.of(lowTier, highTier);
    }

    public static MultiblockMachineDefinition[] registerTieredMultis(String name, Function<Integer, String> cn,
                                                                     BiFunction<MetaMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
                                                                     BiFunction<Integer, MultiblockBuilder, MultiblockMachineDefinition> builder,
                                                                     int... tiers) {
        MultiblockMachineDefinition[] definitions = new MultiblockMachineDefinition[TIER_COUNT];
        for (int tier : tiers) {
            MultiblockBuilder register = multiblock(VN[tier].toLowerCase(Locale.ROOT) + "_" + name, cn.apply(tier), holder -> factory.apply(holder, tier)).tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    //////////////////////////////////////
    // *** Mana Machine ***//

    /// ///////////////////////////////////
    public static GTOMachineBuilder manaMachine(String name, String cn, Function<MetaMachineBlockEntity, MetaMachine> metaMachine) {
        addLang(name, cn);
        return GTO.machine(name, metaMachine, ManaMachineBlockEntity::createBlockEntity);
    }

    public static MachineDefinition[] registerSimpleManaMachines(String name, String cn, GTRecipeType recipeType, Int2IntFunction tankScalingFunction, ResourceLocation workableModel, int... tiers) {
        return registerTieredManaMachines(name, tier -> "%s%s".formatted(MANACN[tier], cn), (holder, tier) -> new SimpleWorkManaMachine(holder, tier, tankScalingFunction), (tier, builder) -> {
            builder.noRecipeModifier();
            return builder
                    .langValue("%s %s".formatted(MANAN[tier], FormattingUtil.toEnglishName(name)))
                    .editableUI(SimpleNoEnergyMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                    .nonYAxisRotation()
                    .recipeType(recipeType)
                    .tooltips(Component.translatable("gtocore.machine.mana_eu").withStyle(ChatFormatting.GREEN))
                    .tooltips(Component.translatable("gtceu.machine.perfect_oc").withStyle(ChatFormatting.YELLOW))
                    .tooltips(Component.translatable("gtocore.machine.mana_input", Component.literal(FormattingUtil.formatNumbers(GTOValues.MANA[tier]) + " /t").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.AQUA))
                    .workableManaTieredHullRenderer(tier, workableModel)
                    .tooltips(workableNoEnergy(recipeType, tankScalingFunction.apply(tier)))
                    .register();
        },
                tiers);
    }

    public static MachineDefinition[] registerTieredManaMachines(String name, Function<Integer, String> cn, BiFunction<MetaMachineBlockEntity, Integer, MetaMachine> factory, BiFunction<Integer, GTOMachineBuilder, MachineDefinition> builder, int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[TIER_COUNT];
        for (int tier : tiers) {
            GTOMachineBuilder register = manaMachine(VN[tier].toLowerCase(Locale.ROOT) + "_" + name, cn.apply(tier), holder -> factory.apply(holder, tier)).tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static MachineDefinition[] registerTieredGTMMachines(String name, BiFunction<MetaMachineBlockEntity, Integer, MetaMachine> factory, BiFunction<Integer, GTOMachineBuilder, MachineDefinition> builder, int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = GTM.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                    holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static GTOMachineBuilder blockEntityMachine(String name, String cn, Function<MetaMachineBlockEntity, MetaMachine> metaMachine, TriFunction<BlockEntityType<?>, BlockPos, BlockState, MetaMachineBlockEntity> blockEntityFactory) {
        addLang(name, cn);
        return GTO.machine(name, metaMachine, blockEntityFactory);
    }
}
