package com.gtocore.common.item.armor;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.common.item.armor.ArmorTooltips;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@DataGeneratorScanned
public final class WardenArmorComponentItem extends ArmorComponentItem {

    private static final EquipmentSlot[] ARMOR_SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    @RegisterLanguage(cn = "监守者套装升级", en = "Warden set upgrade")
    private static final String SECTION = "gtocore.armor.warden.section";
    @RegisterLanguage(cn = "守卫状态：无限飞行、飞行穿墙与异界通行", en = "Warden state: unlimited flight, phasing flight and Otherside access")
    private static final String FEATURE = "gtocore.armor.warden.feature";
    @RegisterLanguage(cn = "穿齐四件监守者升级版夸克高科装备后生效，无额外耗电；飞行速度与穿墙开关在「玩家实时属性」中调节", en = "Requires all four Warden-upgraded QuarkTech pieces, with no extra energy cost. Adjust flight speed and phasing in Player Realtime Attributes")
    private static final String DETAIL = "gtocore.armor.warden.detail";

    public WardenArmorComponentItem(Type type, Properties properties) {
        super(GTArmorMaterials.ARMOR, type, properties);
        int tier = ConfigHolder.INSTANCE.tools.voltageTierQuarkTech;
        setArmorLogic(new QuarkTechSuite(type, ArmorLogicSuite.fullPowerDrain(tier), ArmorLogicSuite.enduranceCapacity(tier, 9), tier));
    }

    public static boolean hasFullSet(LivingEntity entity) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof WardenArmorComponentItem item) || item.getEquipmentSlot() != slot) return false;
        }
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> lines, @NotNull TooltipFlag advanced) {
        super.appendHoverText(stack, level, lines, advanced);
        int hint = lines.indexOf(ArmorTooltips.SHIFT_HINT);
        int index = hint < 0 ? lines.size() : hint;
        lines.add(index++, ArmorTooltips.section(SECTION));
        lines.add(index++, ArmorTooltips.passive(FEATURE).append(" ").append(ArmorTooltips.setPassive(stack, piece -> piece.getItem() instanceof WardenArmorComponentItem)));
        if (ArmorTooltips.showDetails()) lines.add(index, Tips.detail(Component.translatable(DETAIL)));
    }
}
