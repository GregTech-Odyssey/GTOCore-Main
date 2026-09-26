package com.gtocore.common.item;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.item.tool.ae2.patternTool.Ae2BaseProcessingPattern;

import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uiwidgets.cover.CoverUIs;
import com.gregtechceu.gtceu.uiwidgets.item.HeldItemPage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPart;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

@DataGeneratorScanned
public final class PatternModifierProBehavior implements IItemUIFactory {

    public static final PatternModifierProBehavior INSTANCE = new PatternModifierProBehavior();

    @RegisterLanguage(cn = "乘数", en = "Multiplier")
    private static final String MULTIPLIER = "gtocore.pattern_modifier_pro.multiplier";
    @RegisterLanguage(cn = "样板中每种输入与输出的数量乘以该值", en = "Multiplies the amount of every input and output in the pattern")
    private static final String MULTIPLIER_TIP = "gtocore.pattern_modifier_pro.multiplier.tip";
    @RegisterLanguage(cn = "除数", en = "Divider")
    private static final String DIVIDER = "gtocore.pattern_modifier_pro.divider";
    @RegisterLanguage(cn = "样板中每种输入与输出的数量除以该值；任一数量不能整除时不修改该样板", en = "Divides the amount of every input and output in the pattern; a pattern with any amount that is not divisible is left unchanged")
    private static final String DIVIDER_TIP = "gtocore.pattern_modifier_pro.divider.tip";
    @RegisterLanguage(cn = "物品数量上限", en = "Item limit")
    private static final String MAX_ITEM = "gtocore.pattern_modifier_pro.max_item";
    @RegisterLanguage(cn = "相乘后任一输入物品超过该数量时，不修改该样板", en = "A pattern is left unchanged if any input item would exceed this amount after multiplying")
    private static final String MAX_ITEM_TIP = "gtocore.pattern_modifier_pro.max_item.tip";
    @RegisterLanguage(cn = "流体数量上限（桶）", en = "Fluid limit (buckets)")
    private static final String MAX_FLUID = "gtocore.pattern_modifier_pro.max_fluid";
    @RegisterLanguage(cn = "相乘后任一输入流体超过该桶数时，不修改该样板", en = "A pattern is left unchanged if any input fluid would exceed this many buckets after multiplying")
    private static final String MAX_FLUID_TIP = "gtocore.pattern_modifier_pro.max_fluid.tip";
    @RegisterLanguage(cn = "应用次数", en = "Applications")
    private static final String CYCLES = "gtocore.pattern_modifier_pro.cycles";
    @RegisterLanguage(cn = "每次潜行右击时重复执行上述修改的次数", en = "How many times the modification is repeated per sneak right-click")
    private static final String CYCLES_TIP = "gtocore.pattern_modifier_pro.cycles.tip";
    @RegisterLanguage(cn = "只能对样板供应器使用", en = "Can only be used on a pattern provider")
    private static final String ONLY_PROVIDER = "gtocore.pattern_modifier_pro.only_provider";
    @RegisterLanguage(cn = "已更新其中的样板，共执行 %s 次", en = "Patterns updated, applied %s times")
    private static final String APPLIED = "gtocore.pattern_modifier_pro.applied";
    @RegisterLanguage(cn = "对空气右击以打开设置界面", en = "Right-click air to open the settings")
    private static final String OPEN_HINT = "gtocore.pattern_modifier_pro.open_hint";

    private static final String KEY_SCALE = "Scale";
    private static final String KEY_DIV_SCALE = "DivScale";
    private static final String KEY_MAX_ITEM = "MaxItem";
    private static final String KEY_MAX_FLUID = "MaxFluid";
    private static final String KEY_CYCLES = "Cycles";
    private static final long LIMIT = 1000000L;
    private static final int MAX_CYCLES = 16;

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        return new HeldItemPage(holder, window -> UIElement.column(LayoutStyle.AUTO)
                .layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP))
                .addChildren(
                        Label.translatable("gtocore.patternModifierPro.0", UISizes.CONTENT_WIDTH),
                        UIElement.section().addChildren(
                                CoverUIs.inlineNumberRow(MULTIPLIER, field(holder, KEY_SCALE, 1, Integer.MAX_VALUE), MULTIPLIER_TIP),
                                CoverUIs.inlineNumberRow(DIVIDER, field(holder, KEY_DIV_SCALE, 1, Integer.MAX_VALUE), DIVIDER_TIP),
                                CoverUIs.inlineNumberRow(MAX_ITEM, field(holder, KEY_MAX_ITEM, LIMIT, LIMIT), MAX_ITEM_TIP),
                                CoverUIs.inlineNumberRow(MAX_FLUID, field(holder, KEY_MAX_FLUID, LIMIT, LIMIT), MAX_FLUID_TIP),
                                CoverUIs.inlineNumberRow(CYCLES, field(holder, KEY_CYCLES, 1, MAX_CYCLES), CYCLES_TIP))))
                .noInventory().createUI(player);
    }

    private static NumberField field(HeldItemUIFactory.HeldItemHolder holder, String key, long defaultValue, long max) {
        return NumberField.of(LayoutStyle.AUTO, () -> get(holder.getHeld(), key, defaultValue),
                value -> holder.getHeld().getOrCreateTag().putLong(key, Math.clamp(value, 1L, max)), 1, max);
    }

    private static long get(ItemStack stack, String key, long defaultValue) {
        var tag = stack.getTag();
        return tag != null && tag.contains(key, Tag.TAG_LONG) ? tag.getLong(key) : defaultValue;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player instanceof ServerPlayer serverPlayer) {
            HeldItemUIFactory.INSTANCE.openUI(serverPlayer, usedHand);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isShiftKeyDown()) {
                BlockPos pos = context.getClickedPos();
                Level level = context.getLevel();
                BlockEntity tile = level.getBlockEntity(pos);
                InternalInventory internalInventory;
                if (tile instanceof CableBusBlockEntity cable) {
                    Vec3 hitVec = context.getClickLocation();
                    Vec3 hitInBlock = new Vec3(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
                    IPart part = cable.getCableBus().selectPartLocal(hitInBlock).part;
                    internalInventory = (part instanceof PatternProviderLogicHost providerPart) ? providerPart.getLogic().getPatternInv() : null;
                } else {
                    internalInventory = (tile instanceof PatternProviderBlockEntity providerBlock) ? providerBlock.getLogic().getPatternInv() : null;
                }
                if (internalInventory == null) {
                    serverPlayer.displayClientMessage(Component.translatable(ONLY_PROVIDER), true);
                    return InteractionResult.FAIL;
                }
                ItemStack tool = context.getItemInHand();
                int cycles = (int) Math.clamp(get(tool, KEY_CYCLES, 1), 1, MAX_CYCLES);
                for (int i = 0; i < cycles; ++i) {
                    Int2ObjectOpenHashMap<ItemStack> newItemStackHashMap = new Int2ObjectOpenHashMap<>();
                    for (int slot = 0; slot < internalInventory.size(); slot++) {
                        ItemStack itemStack = internalInventory.getStackInSlot(slot);
                        if (!itemStack.isEmpty()) {
                            newItemStackHashMap.put(slot, getNewPatternItemStack(serverPlayer, tool, itemStack));
                        }
                    }
                    newItemStackHashMap.forEach((slot, itemStack) -> {
                        internalInventory.extractItem(slot, 1, false);
                        internalInventory.insertItem(slot, itemStack, false);
                    });
                }
                serverPlayer.displayClientMessage(Component.translatable(APPLIED, cycles), true);
            } else {
                serverPlayer.displayClientMessage(Component.translatable(OPEN_HINT), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static ItemStack getNewPatternItemStack(ServerPlayer serverPlayer, ItemStack tool, ItemStack itemStack) {
        Ae2BaseProcessingPattern pattern = new Ae2BaseProcessingPattern(itemStack, serverPlayer);
        pattern.setScale((int) Math.clamp(get(tool, KEY_SCALE, 1), 1, Integer.MAX_VALUE), false, get(tool, KEY_MAX_ITEM, LIMIT), get(tool, KEY_MAX_FLUID, LIMIT));
        pattern.setScale((int) Math.clamp(get(tool, KEY_DIV_SCALE, 1), 1, Integer.MAX_VALUE), true);
        return pattern.getPatternItemStack();
    }
}
