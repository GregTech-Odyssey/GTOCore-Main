package vazkii.botania.common.alfheim;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import vazkii.botania.common.BotaniaItems;
import vazkii.botania.common.ExtraBotanyItems;
import vazkii.botania.common.MythicBotanyBlocks;
import vazkii.botania.common.MythicBotanyPlayerData;

import java.util.List;
import java.util.Set;

/**
 * 亚尔夫海姆的准入条件：饮下诗之蜜酒获得诗之知识，并集齐十二遗物。
 *
 * <p>
 * 遗物按背包、副手与饰品栏判定；集齐后写入玩家数据，之后不再携带也仍然保留。
 * </p>
 */
public final class AlfheimAccess {

    /** 十二遗物，按界面展示顺序排列。 */
    public static final List<Item> REQUIRED_ITEMS = List.of(
            BotaniaItems.KING_KEY.get(),
            BotaniaItems.FLUGEL_EYE.get(),
            BotaniaItems.INFINITE_FRUIT.get(),
            BotaniaItems.THOR_RING.get(),
            BotaniaItems.ODIN_RING.get(),
            BotaniaItems.LOKI_RING.get(),
            MythicBotanyBlocks.MB_MJOELLNIR.get().asItem(),
            ExtraBotanyItems.EXCALIBUR.get(),
            ExtraBotanyItems.FAILNAUGHT.get(),
            ExtraBotanyItems.RHEIN_HAMMER.get(),
            ExtraBotanyItems.ACHILLES_SHIELD.get(),
            ExtraBotanyItems.VOID_ARCHIVES.get());

    private static final Set<Item> REQUIRED_ITEM_SET = Set.copyOf(REQUIRED_ITEMS);

    private AlfheimAccess() {}

    /** 服务端判定：创造与旁观直接放行，否则须有诗之知识且遗物齐备。 */
    public static boolean canEnter(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return true;
        }
        if (!MythicBotanyPlayerData.hasKvasirKnowledge(player)) {
            return false;
        }
        if (MythicBotanyPlayerData.hasEnteredAlfheim(player)) {
            return true;
        }
        if (itemObtained(player).size() < REQUIRED_ITEM_SET.size()) {
            return false;
        }
        MythicBotanyPlayerData.markEnteredAlfheim(player);
        return true;
    }

    /** 玩家当前携带的遗物，含背包、副手与饰品栏。 */
    public static Set<Item> itemObtained(Player player) {
        ReferenceSet<Item> items = new ReferenceOpenHashSet<>(45);
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                items.add(stack.getItem());
            }
        }
        items.add(player.getOffhandItem().getItem());
        LazyOptional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);
        if (curios.isPresent()) {
            for (ICurioStacksHandler handler : curios.resolve().get().getCurios().values()) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStacks().getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        items.add(stack.getItem());
                    }
                }
            }
        }
        items.retainAll(REQUIRED_ITEM_SET);
        return items;
    }
}
