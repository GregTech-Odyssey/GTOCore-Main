package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 分子装配工厂：把小型合成样板仓里攒下的合成产物直接插回 ME 网络（见 {@link AbstractMEPatternAssemblerMachine}）。
 * <p>
 * 每轮只处理<b>一个</b>样板仓（按安装顺序找到第一个有产物的），并从它的内部槽位里最多取<b>线程数</b>个槽位，
 * 取到的槽位整份产物一起交付、数量不设上限；一个仓取不到就换下一个仓。
 * <p>
 * 线程数按整体框架等级：ULV=1，LV=4，之后每级 +4（MV=8、HV=12…），即 {@code tier < 1 ? 1 : 4 * tier}。
 */
@DataGeneratorScanned
public final class MolecularAssemblyFactoryMachine extends AbstractMEPatternAssemblerMachine implements ITierCasingMachine {

    @RegisterLanguage(cn = "线程数：%s", en = "Threads: %s")
    private static final String THREAD_COUNT = "gtocore.machine.molecular_assembly_factory.threads";

    private final TierCasingTrait tierCasingTrait;

    public MolecularAssemblyFactoryMachine(MetaMachineBlockEntity holder) {
        super(holder);
        this.tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
    }

    public int getThreads() {
        int tier = getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
        return tier < 1 ? 1 : 4 * tier;
    }

    @Override
    protected boolean planOutputs() {
        int threads = getThreads();
        for (var machine : partMachines) {
            for (var slot : machine.getInternalInventory()) {
                var amount = slot.getAmount();
                if (amount < 1) continue;
                if (slot.getOutput() == null) continue;
                plan(machine, slot, amount);
                if (plannedCount() >= threads) break;
            }
            if (plannedCount() > 0) return true;
        }
        return false;
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable(THREAD_COUNT, getThreads()));
    }

    @Override
    public Reference2IntMap<TierDataKey> getCasingTiers() {
        return tierCasingTrait.getCasingTiers();
    }
}
