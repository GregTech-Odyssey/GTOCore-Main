package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.item.IElectricItem;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@DataGeneratorScanned
public final class TreeGrowthSimulator extends StorageMultiblockMachine {

    @RegisterLanguage(cn = "斧 / 链锯", en = "Axe / Chainsaw")
    private static final String SLOT_LABEL = "gtocore.machine.tree_growth_simulator.slot";

    @RegisterLanguage(cn = "主产物产出%s%%", en = "Main Output %s%%")
    private static final String MAIN = "gtocore.machine.main_output";
    @RegisterLanguage(cn = "主产物产出", en = "Main Output")
    private static final String MAIN_NAME = "gtocore.machine.tree_growth_simulator.main_output";

    /**
     * 材料采掘速度的映射指数。原实现用 0.5（即开方）把整棵材料树压平成一条曲线——采掘速度差 90 倍，
     * 速度却只差 4.5 倍，高品位材料几乎没有质变感。提到 0.6 后，铁到末影素的速度差从 9.8 倍拉到 17 倍。
     */
    private static final double SPEED_EXPONENT = 0.6;
    /** 工具采掘等级（含工具自带品质）的线性加成，让高等级素材再多一档收益。 */
    private static final double SPEED_PER_LEVEL = 0.08;
    /** 效率附魔每级的速度倍率，与 GTM 转子 {@code level * 0.1 + 1} 的约定保持一致。 */
    private static final double EFFICIENCY_PER_LEVEL = 0.1;

    private int output = 1;
    private float speed = 1;

    public TreeGrowthSimulator(MetaMachineBlockEntity holder) {
        // actsAs(AXE) 同时覆盖斧头、电锯与 VAJRA 系列；原先用 == AXE 的身份比较把它们全挡在门外
        super(holder, 1, i -> i.getItem() instanceof IGTTool item && item.getToolType().actsAs(GTToolType.AXE));
    }

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (!(getStorageStack().getItem() instanceof IGTTool tool) || !tool.getToolType().actsAs(GTToolType.AXE)) {
            setIdleReason(IdleReason.FELLING_TOOL);
            return null;
        }
        recipe = super.getRealRecipe(unit, recipe);
        if (recipe == null) return null;
        // 速度再快也要留 1 tick：duration 为 0 时超频逻辑走 else 分支，会静默跳过超频与并行补偿
        recipe.duration = Math.max(1, (int) (recipe.duration / speed));
        // scaled 逐条缩放全部产物（含双产物配方的副产物），时运因此对所有产出生效
        if (output > 1) recipe.itemOutputs = recipe.itemOutputs.scaled(output);
        return RecipeModifier.overclocking(this, unit, recipe);
    }

    /**
     * 工具磨损必须发生在扣料成功之后：{@code getRealRecipe} 处在配方搜索路径上，每次搜索都会执行，
     * 扣料失败或同一 tick 多次搜索时都会白扣。这里由 RecipeLogic 在 handleRecipeInput 成功后才调用一次。
     * <p>
     * 磨损统一交给 {@link ToolHelper#damageItem}：它已经实现了 unbreakable 直接跳过、
     * 电动工具真实放电（simulate = false）、UNBREAKING 用 {@code DigDurabilityEnchantment} 判定，
     * 与玩家手持使用完全一致，因此耐久附魔在这里天然生效。
     */
    @Override
    public void beforeWorking(RecipeHandlerUnit unit, GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        ItemStack stack = getStorageStack();
        if (!(stack.getItem() instanceof IGTTool tool)) return;
        IElectricItem electric = tool.isElectric() ? GTCapabilityHelper.getElectricItem(stack) : null;
        int damageBefore = stack.getDamageValue();
        long chargeBefore = electric == null ? Long.MIN_VALUE : electric.getCharge();
        ToolHelper.damageItem(stack, null);
        if (stack.isEmpty()) {
            machineStorage.storage.setStackInSlot(0, ItemStack.EMPTY);
            setIdleReason(IdleReason.FELLING_TOOL);
            return;
        }
        // 磨损写在活体 ItemStack 上，必须显式标脏才会存盘并同步给客户端；
        // 但只在耐久或电量真的变化时才标脏，避免 unbreakable 工具每次配方都触发一次存盘
        if (stack.getDamageValue() != damageBefore || (electric != null && electric.getCharge() != chargeBefore)) {
            machineStorage.storage.onContentsChanged(0);
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        recalcToolProfile();
    }

    @Override
    public void onMachineChanged() {
        recalcToolProfile();
    }

    /**
     * 工具档位只在槽位内容变化或结构成形时重算：速度由材料采掘速度与采掘等级决定，产出由工具类型与时运决定，
     * 效率附魔再叠一层速度倍率。磨损同样会触发 {@code onMachineChanged}，所以只在速度或产出真的变化时才重订阅 tick。
     */
    private void recalcToolProfile() {
        int previousOutput = output;
        float previousSpeed = speed;
        output = 1;
        speed = 1;
        ItemStack stack = getStorageStack();
        if (!(stack.getItem() instanceof IGTTool tool)) return;
        double harvestSpeed = Math.max(1, tool.getMaterialToolSpeed(stack));
        double base = 1 + 0.5 * Math.pow(harvestSpeed, SPEED_EXPONENT) + SPEED_PER_LEVEL * tool.getTotalHarvestLevel(stack);
        speed = (float) (base * (1 + EFFICIENCY_PER_LEVEL * EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack)));
        // 按工具头判定电锯，不绑定具体电压等级：GTM 侧新增的 MV/HV/EV/IV 链锯无需再改这里
        if (tool.getToolType().toolHead() == TagPrefix.toolHeadChainsaw) output++;
        output += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);
        if (previousOutput != output || previousSpeed != speed) getRecipeLogic().updateTickSubscription();
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (MultiblockPage.isScreenText()) return;
        textList.add(Component.translatable(MAIN, output * 100));
        textList.add(Component.translatable("jade.horseStat.speed", "x " + FormattingUtil.formatNumbers(speed)));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addLine(MAIN_NAME, MultiblockPage.percentText(() -> output * 100L));
        page.addReading("jade.horseStat.speed", MultiblockPage.cached(() -> Float.floatToIntBits(speed),
                bits -> Component.literal("x " + FormattingUtil.formatNumber2Places(Float.intBitsToFloat((int) bits)))));
    }

    @Override
    public String getStorageSlotLabel() {
        return SLOT_LABEL;
    }
}
