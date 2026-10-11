package com.gtocore.common.block;

import com.gtocore.api.lang.GTOTips;

import com.gtolib.api.lang.GTOAttributes;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.tooltip.TipSpec;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

@Getter
public class MEStorageCoreBlock extends Block {

    private final long capacity;
    private final TipSpec tips;

    public MEStorageCoreBlock(Properties properties, int tier) {
        super(properties);
        this.capacity = 67108864 * (1L << (tier << 1));
        tips = TipSpec.builder()
                .stat(GTOAttributes.CAPACITY, NumberUtils.formatLongToKorM(capacity) + " Bytes")
                .lines(l -> l.note(GTOTips.global("帮你算了一下，是 %s Bytes".formatted(capacity), "I calculated it for you, it is %s Bytes".formatted(capacity))))
                .build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.addAll(tips.get());
    }
}
