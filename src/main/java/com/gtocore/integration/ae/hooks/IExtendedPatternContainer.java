package com.gtocore.integration.ae.hooks;

import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.integration.ae.PatternContainerGroupHelper;

import com.gtolib.api.blockentity.IDirectionCacheBlockEntity;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

import com.glodblock.github.extendedae.common.tileentities.TileExMolecularAssembler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface IExtendedPatternContainer extends PatternContainer {

    @Nullable
    default GTRecipeType gto$getRecipeType() {
        return null;
    }

    @Nullable
    default Collection<GTRecipeType> gto$getRecipeTypes() {
        return null;
    }

    default boolean gto$supportsRecipeType(GTRecipeType targetRecipeType) {
        var recipeType = gto$getRecipeType();
        if (recipeType == null ||
                recipeType == GTORecipeTypes.DUMMY_RECIPES ||
                recipeType == GTORecipeTypes.HATCH_COMBINED) {
            var recipeTypes = gto$getRecipeTypes();
            if (recipeTypes == null) {
                return false;
            }
            for (GTRecipeType type : recipeTypes) {
                if (matchesRecipeType(type, targetRecipeType)) {
                    return true;
                }
            }
            return false;
        }
        return matchesRecipeType(recipeType, targetRecipeType);
    }

    default boolean gto$isCraftingContainer() {
        return false;
    }

    default boolean hasEmptyPatternSlot() {
        var inv = getTerminalPatternInventory();
        for (int slot = 0; slot < inv.size(); slot++) {
            if (inv.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    default boolean isOutOfService() {
        return getGrid() == null;
    }

    default Component gto$getTerminalGroupSearchName() {
        if (this instanceof Nameable nameable && nameable.hasCustomName()) {
            String customName = nameable.getCustomName().getString();
            if (!customName.startsWith("+")) {
                return nameable.getCustomName();
            }
        }
        if (this instanceof IPPPC self) {
            var level = self.gto$getLevel();
            var pos = self.gto$getBlockPos();
            var extraSuffix = IExtendedPatternContainer.gto$getExtraSuffix(this);
            for (var direction : self.gto$getPushDirection()) {
                var adjacentPos = pos.relative(direction);
                var searchName = PatternContainerGroupHelper.getSearchName(level, adjacentPos, extraSuffix);
                if (searchName != null) {
                    return searchName;
                }
                var fallbackGroup = PatternContainerGroup.fromMachine(level, adjacentPos, direction.getOpposite());
                if (fallbackGroup != null) {
                    return extraSuffix.isEmpty() ?
                            fallbackGroup.name() :
                            fallbackGroup.name().copy().append(" ").append(extraSuffix);
                }
            }
        }
        return getTerminalGroup().name();
    }

    /**
     * 发送样板面板：目的地本体（样板供应器、样板总成等）的图标，显示在对接机器图标左边；null 表示不单独显示。
     */
    @Nullable
    default AEKey gto$getProviderIcon() {
        return this instanceof PatternProviderLogicHost host ? host.getTerminalIcon() : null;
    }

    /**
     * 发送样板面板：普通改名时返回自定义名，否则返回 null（见 {@link PatternContainerGroupHelper#isPlainCustomName}）。
     */
    @Nullable
    default Component gto$getPlainCustomName() {
        if (this instanceof Nameable nameable && nameable.hasCustomName()) {
            var customName = nameable.getCustomName();
            if (PatternContainerGroupHelper.isPlainCustomName(customName.getString())) {
                return customName;
            }
        }
        return null;
    }

    /**
     * 发送样板面板：目的地已有的样板，供按产物搜索。样板供应器、装配矩阵、GT 样板总成等都持有解码好的样板，
     * 直接取用；其他容器才现场解码。
     */
    default List<IPatternDetails> gto$getAvailablePatterns(Level level) {
        if (this instanceof ICraftingProvider provider) {
            return provider.getAvailablePatterns();
        }
        if (this instanceof PatternProviderLogicHost host) {
            return host.getLogic().getAvailablePatterns();
        }
        var inv = getTerminalPatternInventory();
        var patterns = new ArrayList<IPatternDetails>(inv.size());
        for (int slot = 0; slot < inv.size(); slot++) {
            var details = PatternDetailsHelper.decodePattern(inv.getStackInSlot(slot), level);
            if (details != null) patterns.add(details);
        }
        return patterns;
    }

    /**
     * 发送样板面板：忽略普通改名时对接机器的分组（图标 + 名称）。未改名时就是 {@link #getTerminalGroup()}。
     */
    default PatternContainerGroup gto$getMachineGroup() {
        if (gto$getPlainCustomName() != null && this instanceof IPPPC self) {
            var level = self.gto$getLevel();
            var pos = self.gto$getBlockPos();
            for (var direction : self.gto$getPushDirection()) {
                var adjacentPos = pos.relative(direction);
                var group = PatternContainerGroupHelper.fromMachine(level, adjacentPos, "");
                if (group == null) group = PatternContainerGroup.fromMachine(level, adjacentPos, direction.getOpposite());
                if (group != null) return group;
            }
        }
        return getTerminalGroup();
    }

    interface IPPPC extends IExtendedPatternContainer {

        Level gto$getLevel();

        BlockPos gto$getBlockPos();

        BlockEntity gto$getBlockEntity();

        EnumSet<Direction> gto$getPushDirection();
    }

    static BlockEntity getPushBlockEntity(IPPPC be) {
        var cache = IDirectionCacheBlockEntity.getBlockEntityDirectionCache(be.gto$getBlockEntity());
        var pos = be.gto$getBlockPos();

        for (var direction : be.gto$getPushDirection()) {
            var adjBe = cache.getAdjacentBlockEntity(be.gto$getLevel(), pos, direction);
            if (adjBe != null) {
                return adjBe;
            }
        }
        return null;
    }

    static String gto$getExtraSuffix(PatternContainer container) {
        if (container instanceof Nameable nameable && nameable.hasCustomName()) {
            String customName = nameable.getCustomName().getString();
            if (customName.startsWith("+")) {
                return customName.substring(1).strip();
            }
        }
        return "";
    }

    static boolean matchesRecipeType(@Nullable GTRecipeType recipeType, GTRecipeType targetRecipeType) {
        if (recipeType == null) {
            return false;
        }
        return recipeType == targetRecipeType || recipeType.getSmallRecipeMap() == targetRecipeType;
    }

    static boolean gto$isCraftingContainer(IPPPC self) {
        var adjBe = IExtendedPatternContainer.getPushBlockEntity(self);
        return adjBe instanceof MolecularAssemblerBlockEntity || adjBe instanceof TileExMolecularAssembler;
    }

    static GTRecipeType gto$getRecipeType(IPPPC self) {
        var adjBe = IExtendedPatternContainer.getPushBlockEntity(self);
        if (!(adjBe instanceof MetaMachineBlockEntity mmbe)) {
            return null;
        }
        MetaMachine mm = mmbe.getMetaMachine();
        if (mm instanceof IMultiPart partMachine) {
            return partMachine.getController() instanceof IRecipeLogicMachine rlm ? rlm.getRecipeType() : null;
        }

        if (mm instanceof IRecipeLogicMachine rlm) {
            return rlm.getRecipeType();
        }

        return null;
    }

    @Nullable
    static List<GTRecipeType> gto$getRecipeTypes(IPPPC self) {
        var adjBe = IExtendedPatternContainer.getPushBlockEntity(self);
        if (!(adjBe instanceof MetaMachineBlockEntity mmbe)) {
            return null;
        }
        MetaMachine mm = mmbe.getMetaMachine();
        if (mm instanceof IMultiPart partMachine) {
            return partMachine.getController() instanceof IRecipeLogicMachine rlm ? Arrays.asList(rlm.getAvailableRecipeTypes()) : null;
        }

        if (mm instanceof IRecipeLogicMachine rlm) {
            return Arrays.asList(rlm.getAvailableRecipeTypes());
        }

        return null;
    }
}
