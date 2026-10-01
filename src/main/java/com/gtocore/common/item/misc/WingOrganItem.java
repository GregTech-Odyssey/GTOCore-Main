package com.gtocore.common.item.misc;

import com.gtolib.api.capability.IWirelessChargerInteraction;
import com.gtolib.api.player.PlayerData;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 翅膀：装上即可飞行。耐久型飞行时每秒消耗 1 点耐久（耐久即可飞行的秒数）；电动型飞行时每秒耗电。
 */
public final class WingOrganItem extends OrganItemBase {

    public static final int DURABILITY_PER_SECOND = 1;

    private final float maxFlySpeed;
    private final int voltage;
    private final int flightHours;

    private WingOrganItem(Properties properties, float maxFlySpeed, int voltage, int flightHours) {
        super(properties, OrganType.WING);
        this.maxFlySpeed = maxFlySpeed;
        this.voltage = voltage;
        this.flightHours = flightHours;
    }

    public static WingOrganItem durability(Properties properties, float maxFlySpeed) {
        return new WingOrganItem(properties, maxFlySpeed, -1, 0);
    }

    public static WingOrganItem electric(Properties properties, float maxFlySpeed, int voltage, int flightHours) {
        return new WingOrganItem(properties, maxFlySpeed, voltage, flightHours);
    }

    public float getMaxFlySpeed() {
        return maxFlySpeed;
    }

    public boolean isElectric() {
        return voltage >= 0;
    }

    public int getVoltage() {
        return voltage;
    }

    public int getFlightHours() {
        return flightHours;
    }

    public boolean fly(ItemStack stack, int slot, ServerPlayer player, PlayerData data) {
        return isElectric() ? flyElectric(stack, slot, player, data) : flyDurability(stack, slot, player, data);
    }

    public void chargeWireless(ItemStack stack, int slot, PlayerData data) {
        var machine = data.getNetMachine();
        if (machine == null) return;
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return;
        long before = electricItem.getCharge();
        IWirelessChargerInteraction.charge(machine, stack);
        if (electricItem.getCharge() != before) data.organs.markStackUpdated(slot);
    }

    private boolean flyElectric(ItemStack stack, int slot, ServerPlayer player, PlayerData data) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;
        long before = electricItem.getCharge();
        if (isFlyingInAir(player)) {
            electricItem.discharge(GTValues.V[voltage], electricItem.getTier(), true, false, false);
        }
        if (electricItem.getCharge() != before) data.organs.markStackUpdated(slot);
        return electricItem.getCharge() > 0;
    }

    /** 耐久翅膀：悬空飞行时每秒消耗 1 点耐久，耐久用尽后损坏消失。 */
    private static boolean flyDurability(ItemStack stack, int slot, ServerPlayer player, PlayerData data) {
        if (stack.getMaxDamage() - stack.getDamageValue() <= 0) return false;
        if (isFlyingInAir(player)) {
            int damage = stack.getDamageValue();
            stack.hurtAndBreak(DURABILITY_PER_SECOND, player, p -> p.sendSystemMessage(Component.translatable("gtocore.player.organ.you_wing_is_broken")));
            // 创造模式等情况下耐久不变，不必同步
            if (stack.isEmpty() || stack.getDamageValue() != damage) data.organs.markStackUpdated(slot);
        }
        return true;
    }

    private static boolean isFlyingInAir(ServerPlayer player) {
        return player.getAbilities().flying && player.level().getBlockState(player.getOnPos().below()).getBlock() == Blocks.AIR;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        OrganTooltips.addWingTooltip(stack, this, tooltipComponents);
    }
}
