package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.client.forge.ForgeClientEvent;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.feature.multiblock.ICustomHighlightMachine;
import com.gtolib.api.machine.mana.feature.IManaMultiblock;
import com.gtolib.api.machine.mana.trait.ManaTrait;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;
import com.gtolib.api.misc.ManaContainerList;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.annotations.SyncToClient;
import earth.terrarium.adastra.api.planets.PlanetApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.List;

import static com.gtocore.data.IdleReason.INCORRECT_DIRECTION_VOLTA;
import static com.gtocore.data.IdleReason.OBSTRUCTED_VOLTA;

public abstract class AbstractPhotovoltaicMachine extends StorageMultiblockMachine implements IManaMultiblock, ICustomHighlightMachine, ICustomRecipeLogicHolder {

    protected final int basicRate;

    private final ManaTrait manaTrait;

    private boolean inSpace;
    private int refreshSky = 0;
    private boolean canSeeSky;
    private IdleReason idleReason = null;

    @SyncToClient
    protected BlockPos highlightStartPos_1 = BlockPos.ZERO;
    @SyncToClient
    protected BlockPos highlightEndPos_1 = BlockPos.ZERO;
    @SyncToClient
    protected BlockPos highlightStartPos_2 = BlockPos.ZERO;
    @SyncToClient
    protected BlockPos highlightEndPos_2 = BlockPos.ZERO;

    protected AbstractPhotovoltaicMachine(MetaMachineBlockEntity holder, int basicRate) {
        super(holder, 64, i -> i.getItem() == BotaniaBlocks.motifDaybloom.asItem());
        this.basicRate = basicRate;
        this.manaTrait = new ManaTrait(this);
    }

    @Nullable
    protected abstract IdleReason checkEnvironment();

    @Nullable
    protected abstract BlockPos updateHighlightArea();

    @Nullable
    protected abstract GTRecipeDefinition createGenerationRecipe(Level level, RecipeHandlerUnit unit, int basic);

    @Override
    public void onLoad() {
        super.onLoad();
        Level level = getLevel();
        inSpace = level != null && PlanetApi.API.isSpace(level);
    }

    protected final boolean isInSpace() {
        return inSpace;
    }

    @Override
    public @NotNull ManaContainerList getManaContainer() {
        return manaTrait.getManaContainers();
    }

    @Override
    public boolean isGeneratorMana() {
        return true;
    }

    private boolean canSeeSky(Level level) {
        BlockPos pos = updateHighlightArea();
        if (pos == null) {
            setIdleReason(INCORRECT_DIRECTION_VOLTA);
            idleReason = INCORRECT_DIRECTION_VOLTA;
            return false;
        }
        for (BlockPos checkPos : BlockPos.betweenClosed(highlightStartPos_1, highlightEndPos_1)) {
            if (!level.canSeeSky(new BlockPos(checkPos.getX(), pos.getY() + 1, checkPos.getZ()))) {
                setIdleReason(OBSTRUCTED_VOLTA);
                idleReason = OBSTRUCTED_VOLTA;
                return false;
            }
        }
        for (BlockPos checkPos : BlockPos.betweenClosed(highlightStartPos_2, highlightEndPos_2)) {
            if (!level.canSeeSky(new BlockPos(checkPos.getX(), pos.getY() + 1, checkPos.getZ()))) {
                setIdleReason(OBSTRUCTED_VOLTA);
                idleReason = OBSTRUCTED_VOLTA;
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean keepSubscribing() {
        return true;
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        attachHighlightConfigurators(configuratorPanel);
    }

    @Override
    public List<ForgeClientEvent.HighlightNeed> getCustomHighlights() {
        return List.of(
                new ForgeClientEvent.HighlightNeed(highlightStartPos_1, highlightEndPos_1, ChatFormatting.YELLOW.getColor()),
                new ForgeClientEvent.HighlightNeed(highlightStartPos_2, highlightEndPos_2, ChatFormatting.YELLOW.getColor()));
    }

    @Override
    public List<Component> getHighlightText() {
        return List.of(Component.translatable("gtocore.machine.highlight_obstruction"));
    }

    protected final GTRecipeDefinition buildGenerationRecipe(RecipeBuilder builder, int eut) {
        if (getStorageStack().getCount() == 64) {
            builder.MANAt(-eut);
        } else {
            builder.EUt(-eut);
        }
        return builder.build();
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        Level level = getLevel();
        if (level != null) {
            IdleReason environment = checkEnvironment();
            if (environment != null) {
                setIdleReason(environment);
                return null;
            }
            boolean canSeeSky;
            if (refreshSky > 0) {
                refreshSky--;
                canSeeSky = this.canSeeSky;
            } else {
                this.canSeeSky = canSeeSky = canSeeSky(level);
                refreshSky = 10;
            }
            if (!canSeeSky) {
                setIdleReason(idleReason);
                return null;
            }
            return createGenerationRecipe(level, unit, (int) (basicRate * PlanetApi.API.getSolarPower(level)));
        }
        return null;
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }
}
