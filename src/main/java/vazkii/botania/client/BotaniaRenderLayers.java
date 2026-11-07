package vazkii.botania.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 复刻方块的渲染层分配。
 *
 * <p>
 * 与原模组一致：花、盆栽、药水台、幻影轨道与藤蔓用 {@code cutout}；
 * 玻璃、彩虹桥与棱镜用 {@code translucent}；星空用 {@code cutoutMipped}。
 * 花按注册名判定，覆盖三个模组的普通花、浮动花、双花与蘑菇。
 * </p>
 */
public final class BotaniaRenderLayers {

    private static final Set<String> CUTOUT = Set.of(
            "apothecary_default", "apothecary_forest", "apothecary_plains", "apothecary_mountain",
            "apothecary_fungal", "apothecary_swamp", "apothecary_desert", "apothecary_taiga",
            "apothecary_mesa", "apothecary_mossy", "apothecary_livingrock", "apothecary_deepslate",
            "ghost_rail", "solid_vine");

    private static final Set<String> TRANSLUCENT = Set.of(
            "corporea_crystal_cube", "mana_glass", "mana_glass_pane", "elf_glass", "elf_glass_pane",
            "bifrost", "bifrost_pane", "bifrost_perm", "prism");

    private static final Set<String> NAMESPACES = Set.of("botania", "mythicbotany", "extrabotany");

    private static final Pattern FLOWER = Pattern.compile(
            "^(floating_|potted_)?(agricarnation|bellethorn|bergamute|bubbell|clayconia|daffomill|dandelifeon" + "|dreadthorn|endoflame|entropinnyum|exoflame|fallen_kanade|gourmaryllis|heisei_dream" + "|hopperhock|hyacidus|hydroangeas|jaded_amaranthus|jiyuulia|kekimurus|labellia|loonium" + "|manastar|marimorphosis|medumone|munchdew|narslimmus|orechid|pollidisiac|pure_daisy" + "|rafflowsia|rannuncarpus|rosa_arcana|shulk_me_not|solegnolia|spectranthemum|spectrolus" + "|tangleberrie|thermalily|tigerseye|vinculotus|annoyingflower|bellflower|blood_enchantress" + "|edelweiss|manalink|mirrowtunia|moonlight_lily|necrofleur|omniviolet|reikarlily|resoncund" + "|serenitian|stonesia|sunshine_lily|tinkle|trade_orchid|twinstar|woodienia|aquapanthus" + "|feysythia|hellebore|petrunia|raindeletia|wither_aconite|exoblaze)" + "(_chibi|_floating|_ignem)?$");

    private BotaniaRenderLayers() {}

    /** 在客户端初始化阶段调用。 */
    public static void init() {
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!NAMESPACES.contains(id.getNamespace())) {
                continue;
            }
            RenderType type = typeFor(id);
            if (type != null) {
                ItemBlockRenderTypes.setRenderLayer(block, type);
            }
        }
    }

    private static RenderType typeFor(ResourceLocation id) {
        String path = id.getPath();
        if (TRANSLUCENT.contains(path)) {
            return RenderType.translucent();
        }
        if ("starfield".equals(path)) {
            return RenderType.cutoutMipped();
        }
        if (CUTOUT.contains(path)) {
            return RenderType.cutout();
        }
        if (isFlower(path) || path.endsWith("_motif") || "enchanter".equals(path) && "extrabotany".equals(id.getNamespace())) {
            return RenderType.cutout();
        }
        return null;
    }

    private static boolean isFlower(String path) {
        if (path.endsWith("_mystical_flower") || path.endsWith("_shiny_flower") || path.endsWith("_double_flower") || path.endsWith("_floating_flower") || path.endsWith("_mushroom")) {
            return true;
        }
        return FLOWER.matcher(path).matches();
    }
}
