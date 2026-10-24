package com.gtocore.integration.emi;

import com.gtocore.common.machine.multiblock.part.ae.PatternEditor;

import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.MEStorageInfoManager;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe;
import com.gregtechceu.gtceu.uipro.ElementState;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.integration.modules.jeirei.TransferHelper;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static appeng.integration.modules.emi.AbstractRecipeHandler.isInputSlot;

/** 样板编辑器打开时优先接管 EMI 的填充；其他页面仍使用普通机器处理器。 */
final class GTPatternEditorRecipeHandler implements EmiRecipeHandler<ModularUIContainer> {

    private final Reference2ObjectOpenHashMap<EmiIngredient, AEKey[]> ingredientKeys = new Reference2ObjectOpenHashMap<>();
    private final Reference2ObjectOpenHashMap<EmiRecipe, TemplateVariants> templates = new Reference2ObjectOpenHashMap<>();

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<ModularUIContainer> screen) {
        var items = screen.getMenu().getModularUI().entityPlayer.getInventory().items;
        var stacks = new ArrayList<EmiStack>(items.size());
        for (var item : items) stacks.add(EmiStack.of(item));
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        var player = Minecraft.getInstance().player;
        return player != null && player.containerMenu instanceof ModularUIContainer menu && PatternEditor.find(menu.getModularUI()) != null && (recipe instanceof GTEmiRecipe gtRecipe && gtRecipe.supportsTransfer() || GTAe2PatternTerminalHandler.supportsEncodingRecipe(recipe) || EncodingHelper.isSupportedCraftingRecipe(recipe.getBackingRecipe()));
    }

    @Override
    public boolean alwaysDisplaySupport(EmiRecipe recipe) {
        return false;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        var editor = PatternEditor.find(context.getScreenHandler().getModularUI());
        if (editor == null || editor.isDisabled()) return false;
        var player = context.getScreenHandler().getModularUI().entityPlayer;
        var template = template(recipe, player);
        return template != null && editor.canFillRecipe(template.inputs(), template.outputs());
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        var editor = PatternEditor.find(context.getScreenHandler().getModularUI());
        if (editor == null || editor.isDisabled()) return false;
        var template = template(recipe, context.getScreenHandler().getModularUI().entityPlayer);
        if (template == null) return false;
        ResourceLocation recipeId = recipe instanceof GTEmiRecipe gtRecipe ? gtRecipe.getEncodingRecipe().id : null;
        if (!editor.fillRecipe(template.inputs(), template.outputs(), recipeId)) return false;
        Minecraft.getInstance().setScreen(context.getScreen());
        return true;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        var player = context.getScreenHandler().getModularUI().entityPlayer;
        var storage = storage(player);
        var tooltip = new ArrayList<Component>(8);
        for (var ingredient : recipe.getInputs()) {
            if (isCraftable(storage, player, ingredient)) {
                tooltip.addAll(TransferHelper.createEncodingTooltip(true));
                break;
            }
        }
        tooltip.addAll(GTAe2PatternTerminalHandler.getCatalystTooltip(recipe));
        tooltip.add(Component.translatable("gtocore.ae.appeng.me2in1.emi.no_merge").withStyle(ChatFormatting.YELLOW));
        var editor = PatternEditor.find(context.getScreenHandler().getModularUI());
        var disabledReason = editor == null ? null : ElementState.disabledReason(editor);
        if (disabledReason != null) tooltip.add(disabledReason.copy().withStyle(ChatFormatting.RED));
        else if (!canCraft(recipe, context)) tooltip.add(ItemModText.RECIPE_TOO_LARGE.text().withStyle(ChatFormatting.RED));
        var result = new ArrayList<ClientTooltipComponent>(tooltip.size());
        for (var line : tooltip) result.add(ClientTooltipComponent.create(line.getVisualOrderText()));
        return result;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context, List<Widget> widgets, GuiGraphics graphics) {
        var player = context.getScreenHandler().getModularUI().entityPlayer;
        var storage = storage(player);
        for (var widget : widgets) {
            if (widget instanceof SlotWidget slot && isInputSlot(slot) && isCraftable(storage, player, slot.getStack())) {
                GTAe2PatternTerminalHandler.renderSlotOverlay(graphics, slot);
            }
        }
    }

    private static MEStorageInfoManager storage(Player player) {
        return IEnhancedPlayer.of(player).getPlayerData().getMeStorageInfoManager();
    }

    private boolean isCraftable(MEStorageInfoManager storage, Player player, EmiIngredient ingredient) {
        var keys = ingredientKeys.computeIfAbsent(ingredient, GTPatternEditorRecipeHandler::ingredientKeys);
        boolean reachable = IEnhancedPlayer.isClientAEReachable(player);
        for (var key : keys) {
            if (key == null) continue;
            storage.fetchAEKey(key);
            if (reachable && storage.getClientKnownCraftables().contains(key)) return true;
        }
        return false;
    }

    private static AEKey[] ingredientKeys(EmiIngredient ingredient) {
        var candidates = ingredient.getEmiStacks();
        var keys = new AEKey[candidates.size()];
        for (int i = 0; i < keys.length; i++) {
            var stack = EmiStackHelper.toGenericStack(candidates.get(i));
            if (stack != null) keys[i] = stack.what();
        }
        return keys;
    }

    @Nullable
    private Template template(EmiRecipe recipe, Player player) {
        int modifiers = (GTUtil.isShiftDown() ? 1 : 0) | (GTUtil.isCtrlDown() ? 2 : 0) | (GTUtil.isAltDown() ? 4 : 0);
        var encodingRecipe = recipe instanceof GTEmiRecipe gtRecipe ? gtRecipe.getEncodingRecipe() : null;
        var variants = templates.get(recipe);
        if (variants == null || variants.recipe() != encodingRecipe) {
            variants = new TemplateVariants(encodingRecipe, new PreparedTemplate[8]);
            templates.put(recipe, variants);
        }
        var prepared = variants.templates()[modifiers];
        if (prepared == null) {
            prepared = new PreparedTemplate(GTEmiEncodingHelper.ofInputs(recipe), GTAe2PatternTerminalHandler.ofOutputs(recipe), (modifiers & 4) == 0);
            variants.templates()[modifiers] = prepared;
        }
        return prepared.select(player);
    }

    private static GenericStack bestIngredient(List<GenericStack> candidates, MEStorageInfoManager storage, Player player) {
        var best = candidates.getFirst();
        int bestSource = -1;
        boolean bestCraftable = false;
        boolean bestUndamaged = false;
        long bestAmount = 0;
        boolean reachable = IEnhancedPlayer.isClientAEReachable(player);
        for (var candidate : candidates) {
            if (candidate == null || candidate.amount() <= 0) continue;
            var key = candidate.what();
            storage.fetchAEKey(key);
            boolean craftable = reachable && storage.getClientKnownCraftables().contains(key);
            long amount = reachable ? storage.getClientKnownAvailableAmounts().getLong(key) : 0;
            int source = craftable || amount > 0 ? 2 : inPlayerInventory(key instanceof AEItemKey item ? item : null, player) ? 1 : 0;
            boolean undamaged = !(key instanceof AEItemKey item) || !item.isDamaged();
            if (source > bestSource || source == 2 && bestSource == 2 &&
                    (craftable && !bestCraftable || craftable == bestCraftable &&
                            (undamaged && !bestUndamaged || undamaged == bestUndamaged && amount > bestAmount))) {
                best = candidate;
                bestSource = source;
                bestCraftable = craftable;
                bestUndamaged = undamaged;
                bestAmount = amount;
            }
        }
        return best;
    }

    private static boolean inPlayerInventory(@Nullable AEItemKey key, Player player) {
        if (key == null) return false;
        for (var stack : player.getInventory().items) if (key.matches(stack)) return true;
        return false;
    }

    static boolean addStack(ArrayList<GenericStack> stacks, @Nullable GenericStack next, boolean merge, int limit) {
        if (next == null || next.what() == null || next.amount() <= 0) return false;
        long total = next.amount();
        int mergeIndex = -1;
        for (int i = 0; i < stacks.size(); i++) {
            var previous = stacks.get(i);
            if (previous.what() != next.what()) continue;
            if (previous.amount() > Long.MAX_VALUE - total) return false;
            total += previous.amount();
            mergeIndex = i;
        }
        if (merge && mergeIndex >= 0) {
            stacks.set(mergeIndex, new GenericStack(next.what(), total));
            return true;
        }
        if (stacks.size() >= limit) return false;
        stacks.add(next);
        return true;
    }

    private record Template(ArrayList<GenericStack> inputs, ArrayList<GenericStack> outputs) {}

    private record TemplateVariants(@Nullable GTRecipeDefinition recipe, PreparedTemplate[] templates) {}

    /** EMI 每帧查询填充状态；候选只转换一次，最佳原料变化时才重建合并结果。 */
    private static final class PreparedTemplate {

        private final List<List<GenericStack>> ingredients;
        private final List<GenericStack> results;
        private final GenericStack[] selected;
        private final boolean merge;
        private UUID selectedPlayer;
        private long selectedTick = Long.MIN_VALUE;
        private boolean initialized;
        @Nullable
        private Template template;

        private PreparedTemplate(List<List<GenericStack>> ingredients, List<GenericStack> results, boolean merge) {
            this.ingredients = ingredients;
            this.results = results;
            this.selected = new GenericStack[ingredients.size()];
            this.merge = merge;
        }

        @Nullable
        private Template select(Player player) {
            long tick = player.level().getGameTime();
            if (player.getUUID().equals(selectedPlayer) && selectedTick == tick) return template;
            selectedPlayer = player.getUUID();
            selectedTick = tick;
            boolean changed = !initialized;
            var storage = storage(player);
            for (int i = 0; i < selected.length; i++) {
                var candidates = ingredients.get(i);
                var best = candidates.isEmpty() ? null : bestIngredient(candidates, storage, player);
                if (selected[i] != best) {
                    selected[i] = best;
                    changed = true;
                }
            }
            if (!changed) return template;
            initialized = true;
            template = build();
            return template;
        }

        @Nullable
        private Template build() {
            var inputs = new ArrayList<GenericStack>(Math.min(selected.length, AEProcessingPattern.MAX_INPUT_SLOTS));
            var outputs = new ArrayList<GenericStack>(Math.min(results.size(), AEProcessingPattern.MAX_OUTPUT_SLOTS));
            for (int i = 0; i < selected.length; i++) {
                if (!ingredients.get(i).isEmpty() && !addStack(inputs, selected[i], merge, AEProcessingPattern.MAX_INPUT_SLOTS)) return null;
            }
            for (var result : results) {
                if (!addStack(outputs, result, merge, AEProcessingPattern.MAX_OUTPUT_SLOTS)) return null;
            }
            return inputs.isEmpty() || outputs.isEmpty() ? null : new Template(inputs, outputs);
        }
    }
}
