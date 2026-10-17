package com.gtocore.integration.ae;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.multiblock.electric.processing.ProcessingPlantMachine;
import com.gtocore.common.machine.multiblock.part.ProgrammableHatchPartMachine;

import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.AEItemKey;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class PatternContainerGroupHelper {

    private PatternContainerGroupHelper() {}

    /**
     * 自定义名是否为普通改名。以 "+" 开头的名字是给机器名追加后缀的写法，名字仍以机器为主，不算改名。
     */
    public static boolean isPlainCustomName(String customName) {
        return !customName.isEmpty() && !customName.startsWith("+");
    }

    public static @Nullable PatternContainerGroup fromMachine(Level level, BlockPos pos, String extraSuffix) {
        MachineNameContext context = getMachineNameContext(level, pos);
        if (context == null) {
            return null;
        }

        return createGroup(context.displayMachine(), extraSuffix, context.selectedRecipeType(),
                getAvailableRecipeTypes(context.recipeMachine()), context.showAllRecipeTypes(), context.tooltip());
    }

    public static PatternContainerGroup forPatternBuffer(MetaMachine displayMachine, MetaMachine actualMachine,
                                                         String customName,
                                                         @Nullable GTRecipeType selectedRecipeType,
                                                         Collection<GTRecipeType> availableRecipeTypes) {
        var icon = AEItemKey.of(displayMachine.getDefinition().asStack());
        List<Component> tooltip = List.of(
                Component.translatable(actualMachine.getDefinition().getDescriptionId()));
        if (isPlainCustomName(customName)) {
            return new PatternContainerGroup(icon, Component.literal(customName), tooltip);
        }

        String extraSuffix = customName.startsWith("+") ? customName.substring(1).strip() : "";
        boolean showAllRecipeTypes = selectedRecipeType == null ||
                selectedRecipeType == GTORecipeTypes.HATCH_COMBINED;
        return createGroup(displayMachine, extraSuffix, selectedRecipeType, availableRecipeTypes,
                showAllRecipeTypes, tooltip);
    }

    private static PatternContainerGroup createGroup(MetaMachine displayMachine, String extraSuffix,
                                                     @Nullable GTRecipeType selectedRecipeType,
                                                     Collection<GTRecipeType> availableRecipeTypes,
                                                     boolean showAllRecipeTypes,
                                                     List<Component> tooltip) {
        MutableComponent name = buildName(displayMachine, extraSuffix, selectedRecipeType, availableRecipeTypes,
                showAllRecipeTypes);
        return new PatternContainerGroup(AEItemKey.of(displayMachine.getDefinition().asStack()), name, tooltip);
    }

    public static Component getSearchName(MetaMachine displayMachine, String extraSuffix,
                                          @Nullable GTRecipeType selectedRecipeType,
                                          Collection<GTRecipeType> availableRecipeTypes) {
        return buildName(displayMachine, extraSuffix, selectedRecipeType, availableRecipeTypes,
                selectedRecipeType == null || selectedRecipeType == GTORecipeTypes.HATCH_COMBINED);
    }

    public static @Nullable Component getSearchName(Level level, BlockPos pos, String extraSuffix) {
        MachineNameContext context = getMachineNameContext(level, pos);
        if (context == null) {
            return null;
        }

        return buildName(context.displayMachine(), extraSuffix, context.selectedRecipeType(),
                getAvailableRecipeTypes(context.recipeMachine()), context.showAllRecipeTypes());
    }

    private static @Nullable MachineNameContext getMachineNameContext(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MetaMachineBlockEntity blockEntity)) {
            return null;
        }

        MetaMachine machine = blockEntity.getMetaMachine();
        if (machine == null) {
            return null;
        }

        MetaMachine displayMachine = machine;
        IRecipeLogicMachine recipeMachine = machine instanceof IRecipeLogicMachine logicMachine ? logicMachine : null;
        GTRecipeType selectedRecipeType = getCurrentRecipeType(recipeMachine);
        boolean showAllRecipeTypes = false;
        List<Component> tooltip = Collections.emptyList();

        if (machine instanceof IMultiPart partMachine) {
            IMultiController controller = partMachine.getController();
            if (controller != null) {
                displayMachine = controller.self();
                recipeMachine = controller instanceof IRecipeLogicMachine logicMachine ? logicMachine : null;
                tooltip = List.of(Component.translatable(machine.getDefinition().getDescriptionId()));
                if (machine instanceof ProgrammableHatchPartMachine programmableHatch) {
                    selectedRecipeType = programmableHatch.getRecipeType();
                    showAllRecipeTypes = selectedRecipeType == null ||
                            selectedRecipeType == GTORecipeTypes.HATCH_COMBINED;
                } else {
                    selectedRecipeType = getCurrentRecipeType(recipeMachine);
                }
            }
        }

        return new MachineNameContext(displayMachine, recipeMachine, selectedRecipeType, showAllRecipeTypes, tooltip);
    }

    private static Collection<GTRecipeType> getAvailableRecipeTypes(@Nullable IRecipeLogicMachine recipeMachine) {
        return recipeMachine == null ? Collections.emptyList() : Arrays.asList(recipeMachine.getAvailableRecipeTypes());
    }

    private static MutableComponent buildName(MetaMachine displayMachine, String extraSuffix,
                                              @Nullable GTRecipeType selectedRecipeType,
                                              Collection<GTRecipeType> availableRecipeTypes,
                                              boolean showAllRecipeTypes) {
        MutableComponent machineName = getMachineName(displayMachine);
        MutableComponent result = Component.empty().append(machineName);
        appendPart(result, getMachineTier(displayMachine));
        if (!extraSuffix.isBlank()) {
            appendPart(result, Component.literal(extraSuffix.strip()));
        }
        appendPart(result, getRecipeTypeName(machineName.getString().toLowerCase(Locale.ROOT), selectedRecipeType,
                availableRecipeTypes, showAllRecipeTypes));
        return result;
    }

    private static void appendPart(MutableComponent result, @Nullable Component part) {
        if (part != null) {
            result.append(" ").append(part);
        }
    }

    private static MutableComponent getMachineName(MetaMachine machine) {
        var title = Component.translatable(machine.getDefinition().getDescriptionId());
        if (machine instanceof ProcessingPlantMachine processingPlantMachine) {
            ItemStack stack = processingPlantMachine.getMachineStorage().storage.getStackInSlot(0);
            if (stack.getItem() instanceof MetaMachineItem metaMachineItem) {
                return title.copy()
                        .append(" - ")
                        .append(Component.translatable(metaMachineItem.getDefinition().getDescriptionId()));
            }
        }
        return title;
    }

    private static @Nullable Component getMachineTier(MetaMachine machine) {
        Integer tier = getMachineRecipeTier(machine);
        if (tier == null) {
            return null;
        }
        if (tier >= 0 && tier < GTValues.TIER_COUNT) {
            return Component.literal(GTValues.VNF[tier])
                    .withStyle(style -> style.withColor(GTValues.VC[tier]));
        }
        return Component.literal("MAX");
    }

    private static @Nullable Integer getMachineRecipeTier(MetaMachine machine) {
        if (machine instanceof ITieredMachine tieredMachine && tieredMachine.getTier() >= GTValues.ULV) {
            return tieredMachine.getTier();
        }
        if (machine instanceof IOverclockMachine overclockMachine &&
                overclockMachine.getMaxOverclockTier() >= GTValues.ULV) {
            return overclockMachine.getMaxOverclockTier();
        }
        if (machine instanceof IOverclockMachine overclockMachine) {
            long voltage = overclockMachine.getOverclockVoltage();
            if (voltage > 0) {
                return (int) GTUtil.getFloorTierByVoltage(voltage);
            }
        }
        if (machine instanceof ProcessingPlantMachine || !(machine instanceof ITierCasingMachine tierCasingMachine)) {
            return null;
        }
        if (!tierCasingMachine.getCasingTiers().containsKey(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER)) {
            return null;
        }
        return tierCasingMachine.getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
    }

    private static @Nullable Component getRecipeTypeName(String machineName,
                                                         @Nullable GTRecipeType selectedRecipeType,
                                                         Collection<GTRecipeType> availableRecipeTypes,
                                                         boolean showAllRecipeTypes) {
        List<GTRecipeType> recipeTypes = availableRecipeTypes.stream()
                .filter(PatternContainerGroupHelper::isDisplayableRecipeType)
                .toList();
        if (recipeTypes.size() > 1 && !showAllRecipeTypes) {
            recipeTypes = isDisplayableRecipeType(selectedRecipeType) ? Collections.singletonList(selectedRecipeType) : Collections.emptyList();
        }

        MutableComponent result = null;
        for (GTRecipeType recipeType : recipeTypes) {
            Component recipeTypeName = getRecipeTypeDisplayName(recipeType);
            if (machineName.contains(recipeTypeName.getString().toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (result == null) {
                result = Component.empty();
            } else {
                result.append("/");
            }
            result.append(recipeTypeName);
        }
        return result;
    }

    private static Component getRecipeTypeDisplayName(GTRecipeType recipeType) {
        return Component.translatable(recipeType.registryName.toLanguageKey());
    }

    private static boolean isDisplayableRecipeType(@Nullable GTRecipeType recipeType) {
        return recipeType != null &&
                recipeType != GTORecipeTypes.DUMMY_RECIPES &&
                recipeType != GTORecipeTypes.HATCH_COMBINED;
    }

    private static @Nullable GTRecipeType getCurrentRecipeType(@Nullable IRecipeLogicMachine recipeMachine) {
        if (recipeMachine == null || recipeMachine.getAvailableRecipeTypes().length == 0) {
            return null;
        }
        return recipeMachine.getRecipeType();
    }

    private record MachineNameContext(MetaMachine displayMachine,
                                      @Nullable IRecipeLogicMachine recipeMachine,
                                      @Nullable GTRecipeType selectedRecipeType,
                                      boolean showAllRecipeTypes,
                                      List<Component> tooltip) {}
}
