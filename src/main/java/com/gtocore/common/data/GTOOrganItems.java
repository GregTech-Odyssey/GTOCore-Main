package com.gtocore.common.data;

import com.gtocore.common.item.OrganModifierBehaviour;
import com.gtocore.common.item.misc.OrganItemBase;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.common.item.misc.TierOrganItem;
import com.gtocore.common.item.misc.WingOrganItem;

import com.gtolib.GTOCore;
import com.gtolib.api.player.OrganTier;
import com.gtolib.utils.TagUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;

import net.minecraft.world.item.Item;

import com.gto.registrate.util.entry.ItemEntry;
import com.gto.registrate.util.nullness.NonNullConsumer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.EnumMap;
import java.util.function.Function;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.gtolib.utils.register.ItemRegisterUtils.item;

public final class GTOOrganItems {

    private GTOOrganItems() {}

    /// 电动机械之翼：EV 电池，悬空飞行时每秒耗 V[EV] EU（即 0.05 A），满电可飞 32 小时
    private static final int MECHANICAL_WING_FLIGHT_HOURS = 32;

    // 翅膀：耐久型的耐久即可飞行的秒数

    public static final ItemEntry<WingOrganItem> FAIRY_WING = registerOrgan("fairy_wing", OrganType.WING, "fairy_wing", "翅膀 妖精之翼", "Fairy Wing",
            p -> WingOrganItem.durability(p.durability(4 * 3600), 0.15F), item -> {});
    public static final ItemEntry<WingOrganItem> MANA_STEEL_WING = registerOrgan("mana_steel_wing", OrganType.WING, "mana_steel_wing", "翅膀 魔力钢之翼", "Mana Steel Wing",
            p -> WingOrganItem.durability(p.durability(15 * 60), 0.15F), item -> {});
    public static final ItemEntry<WingOrganItem> MECHANICAL_WING = registerOrgan("mechanical_wing", OrganType.WING, "mechanical_wing", "翅膀 电动机械之翼", "Mechanical Wing",
            p -> WingOrganItem.electric(p, 0.25F, GTValues.EV, MECHANICAL_WING_FLIGHT_HOURS),
            attach(ElectricStats.createElectricItem(GTValues.V[GTValues.EV] * MECHANICAL_WING_FLIGHT_HOURS * 3600L, GTValues.EV)));

    public static final ItemEntry<ComponentItem> ORGAN_MODIFIER = item("organ_modifier", "器官修改器", p -> ComponentItem.create(p.stacksTo(1).setNoRepair()))
            .lang("Organ Modifier")
            .model((ctx, prov) -> prov.generated(ctx, GTOCore.id("item/organ/item/visceral_editor")))
            .onRegister(attach(new OrganModifierBehaviour()))
            .register();

    private static final EnumMap<OrganType, Reference2ObjectOpenHashMap<OrganTier, ItemEntry<TierOrganItem>>> TIER_ORGANS = new EnumMap<>(OrganType.class);

    public static void init() {
        for (var type : OrganType.BODY) {
            TIER_ORGANS.put(type, new Reference2ObjectOpenHashMap<>(OrganTier.all().size()));
        }
        for (var tier : OrganTier.all()) {
            for (var type : OrganType.BODY) {
                TIER_ORGANS.get(type).put(tier, registerOrgan(tier.getItemPrefix() + "_" + type.key, type, tier.getTexture(),
                        "器官 " + type.cn + " " + tier.getCn(), "Organ " + type.en + " " + tier.getEn(),
                        p -> new TierOrganItem(tier, p, type), item -> {}));
            }
        }
    }

    /** 分级身体器官的物品。 */
    public static ItemEntry<TierOrganItem> tierOrgan(OrganType type, OrganTier tier) {
        return TIER_ORGANS.get(type).get(tier);
    }

    private static <T extends OrganItemBase> ItemEntry<T> registerOrgan(String id, OrganType type, String texture, String cn, String en,
                                                                        Function<Item.Properties, T> factory, NonNullConsumer<? super T> onRegister) {
        return item(id, cn, p -> factory.apply(p.stacksTo(1).setNoRepair()))
                .lang(en)
                .tag(TagUtils.createItemTag(GTOCore.id("organ_" + type.key)))
                .model((ctx, prov) -> prov.generated(ctx, GTOCore.id("item/organ/part/" + type.key + "/" + texture)))
                .onRegister(onRegister)
                .register();
    }
}
