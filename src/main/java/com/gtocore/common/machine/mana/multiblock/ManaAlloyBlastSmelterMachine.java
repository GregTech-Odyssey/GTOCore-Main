package com.gtocore.common.machine.mana.multiblock;

import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.mana.feature.IManaMultiblock;
import com.gtolib.api.machine.mana.trait.ManaTrait;
import com.gtolib.api.machine.multiblock.CoilCustomParallelMultiblockMachine;
import com.gtolib.api.misc.ManaContainerList;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyTypes;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.item.BotaniaItems;

import java.util.List;
import java.util.Map;

public final class ManaAlloyBlastSmelterMachine extends CoilCustomParallelMultiblockMachine implements IManaMultiblock {

    public static Component getRunes() {
        var c = Component.empty();
        for (var i = 1; i <= 8; i++) {
            c.append(i + ": ").append(RUNES.get(i).getDescription()).append("\n");
        }
        return c;
    }

    private static final Map<Integer, Item> RUNES = Map.of(
            1, BotaniaItems.runeWater,
            2, BotaniaItems.runeFire,
            3, BotaniaItems.runeAir,
            4, BotaniaItems.runeEarth,
            5, BotaniaItems.runeSpring,
            6, BotaniaItems.runeSummer,
            7, BotaniaItems.runeAutumn,
            8, BotaniaItems.runeWinter);

    @SaveToDisk(defaultValue = "0")
    private int tick;

    @SaveToDisk(defaultValue = "0")
    private int time;

    @SaveToDisk(defaultValue = "0")
    private int signal;

    private int mana;

    private final ManaTrait manaTrait;

    public ManaAlloyBlastSmelterMachine(MetaMachineBlockEntity holder) {
        super(holder, true, true, m -> 16);
        this.manaTrait = new ManaTrait(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        mana = 1 << getTier();
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        return signal;
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (super.handleTickRecipe(recipe)) {
            tick++;
            if (time > 1) {
                time--;
                if (signal > 0) {
                    Item item = RUNES.get(signal);
                    boolean success = forEachKey(AEKeyTypes.ITEMS, true, (key, amount) -> key instanceof AEItemKey itemKey && itemKey.getItem() == item && inputItem(item, 1));
                    if (success) {
                        signal = 0;
                        updateSignal();
                        mana = 1 << getTier();
                        time = 0;
                    }
                }
            } else if (time == 1) {
                mana <<= 2;
                time = 0;
            }
            if (tick > 1200) {
                tick = 0;
                signal = GTValues.RNG.nextInt(8) + 1;
                time = 200;
                updateSignal();
            }
            if (removeMana(mana, 1, false) == mana) return true;
            IdleReason.NO_MANA.setReason(this, mana, getManaContainer().getCurrentMana());
            return false;
        }
        return false;
    }

    @Override
    public boolean handleRecipeInput(RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (removeMana(mana, 1, false) != mana) {
            IdleReason.NO_MANA.setReason(this, mana, getManaContainer().getCurrentMana());
            return false;
        }
        return super.handleRecipeInput(unit, recipe);
    }

    @Override
    public void afterWorking() {
        signal = 0;
        updateSignal();
        super.afterWorking();
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (!MultiblockPage.isScreenText()) textList.add(Component.translatable("gtocore.recipe.mana_consumption").append(": ").append(String.valueOf(mana)));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addNumber("gtocore.recipe.mana_consumption", () -> mana, "");
    }

    @Override
    public @NotNull ManaContainerList getManaContainer() {
        return manaTrait.getManaContainers();
    }

    @Override
    public boolean isGeneratorMana() {
        return false;
    }
}
