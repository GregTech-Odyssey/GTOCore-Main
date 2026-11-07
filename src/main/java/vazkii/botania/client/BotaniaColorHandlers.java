package vazkii.botania.client;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

import vazkii.botania.common.BotaniaReplica;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 花瓣方块与对应物品的着色。
 *
 * <p>
 * 原模组的花瓣方块共用一张灰度贴图，颜色由方块染色器按变体给出；对应物品沿用同一颜色。
 * 复刻方块只有基础行为，颜色表按原模组的调色板固定。
 * </p>
 */
public final class BotaniaColorHandlers {

    private static final String[] COLORS = {
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
    };

    private static final int[] PALETTE = {
            0xF0FFFF, 0xF88A10, 0xDC04D7, 0x72C4FF, 0xFFF148, 0x62FF2C, 0xFB8BC7, 0x71747C,
            0x9DA8A7, 0x34B0D0, 0x950BA8, 0x2448EB, 0x976405, 0x3AAB0D, 0xDD111F, 0x261E28
    };

    private static Map<Block, Integer> petalBlockColors;

    private BotaniaColorHandlers() {}

    private static Map<Block, Integer> colors() {
        Map<Block, Integer> map = petalBlockColors;
        if (map == null) {
            map = new LinkedHashMap<>(COLORS.length);
            for (int i = 0; i < COLORS.length; i++) {
                Block block = BotaniaReplica.BOTANIA.getBlock(COLORS[i] + "_petal_block");
                if (block != null) {
                    map.put(block, PALETTE[i]);
                }
            }
            petalBlockColors = map;
        }
        return map;
    }

    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        Map<Block, Integer> map = colors();
        event.register((state, level, pos, tint) -> tint == 0 ? map.getOrDefault(state.getBlock(), -1) : -1,
                map.keySet().toArray(new Block[0]));
    }

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        Map<Block, Integer> map = colors();
        event.register((stack, tint) -> tint == 0 ? colorOf(stack) : -1,
                map.keySet().toArray(new Block[0]));
    }

    private static int colorOf(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return -1;
        }
        Block block = blockItem.getBlock();
        if (block == Blocks.AIR) {
            return -1;
        }
        return colors().getOrDefault(block, -1);
    }
}
