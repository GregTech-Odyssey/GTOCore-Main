package com.gtocore.mixin.ae2.pattern;

import com.gtocore.utils.PlayerNameUtils;

import com.gtolib.api.ae2.MyPatternDetailsHelper;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.tooltip.ElectricText;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.UUID;

@Mixin(ProcessingPatternItem.class)
public abstract class ProcessingPatternItemMixin extends EncodedPatternItem {

    protected ProcessingPatternItemMixin(Properties properties) {
        super(properties);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public AEProcessingPattern decode(AEItemKey what, Level level) {
        if (what == null || !what.hasTag()) {
            return null;
        }

        try {
            return MyPatternDetailsHelper.decode(what);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag advancedTooltips) {
        var tag = stack.getTag();
        if (tag == null) return;
        if (tag.tags.get("uuid") instanceof IntArrayTag arrayTag) {
            if (level == null) {
                level = net.minecraft.client.Minecraft.getInstance().level;
            }
            UUID uuid = NbtUtils.loadUUID(arrayTag);
            lines.add(Tips.stat(Component.translatable("tooltip.item.pattern.uuid", Component.literal(PlayerNameUtils.getLastKnownName(level, uuid)).withStyle(ChatFormatting.WHITE))));
        }
        var recipeId = tag.getString("recipe");
        if (!recipeId.isEmpty()) {
            var key = RLUtils.parse(recipeId.split("/")[0]).toLanguageKey();
            lines.add(Tips.feature(Component.translatable("gtocore.pattern.recipe")));
            lines.add(Tips.stat(Component.literal("  ").append(Component.translatable("gtocore.pattern.machine", Component.translatable(key).withStyle(ChatFormatting.WHITE)))));
            var recipe = RecipeBuilder.get(RLUtils.parse(recipeId));
            if (recipe != null && recipe.getInputEUt() > 0) {
                long eut = recipe.getInputEUt();
                lines.add(Tips.stat(Component.literal("  ").append(Component.translatable("gtocore.pattern.voltage", ElectricText.tier(GTUtil.getTierByVoltage(eut)), ElectricText.power(eut).withStyle(ChatFormatting.WHITE)))));
            }
        }
        super.appendHoverText(stack, level, lines, advancedTooltips);
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null) {
            if (tag.tags.get("in") instanceof ListTag listTag) {
                for (var t : listTag) {
                    if (t instanceof CompoundTag compoundTag && compoundTag.tags.get("#c") instanceof StringTag stringTag && stringTag.getAsString().equals("gtocore:key_type")) {
                        return Component.translatable("item.gtocore.pattern.wildcard");
                    }
                }
            }
        }
        return super.getName(stack);
    }
}
