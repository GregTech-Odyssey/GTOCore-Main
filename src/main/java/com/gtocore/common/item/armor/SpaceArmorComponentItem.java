package com.gtocore.common.item.armor;

import com.gtolib.utils.ItemUtils;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.armor.ArmorTooltips;
import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.common.registry.ModFluids;
import earth.terrarium.adastra.common.tags.ModFluidTags;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.adastra.common.utils.TooltipUtils;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidItem;
import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;
import earth.terrarium.botarium.common.fluid.utils.ClientFluidHooks;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class SpaceArmorComponentItem extends ArmorComponentItem implements BotariumFluidItem<WrappedItemFluidContainer> {

    private final long tankSize;

    public SpaceArmorComponentItem(ArmorMaterial material, Type type, long size, Properties properties) {
        super(material, type, properties);
        this.tankSize = size;
    }

    @Override
    public void attachComponents(IItemComponent @NotNull... components) {
        super.attachComponents(components);

        IDurabilityBar durabilityBar = new IDurabilityBar() {

            @Override
            public int getBarColor(ItemStack stack) {
                return ClientFluidHooks.getFluidColor(FluidUtils.getTank(stack));
            }

            @Override
            public int getBarWidth(ItemStack stack) {
                var fluidContainer = getFluidContainer(stack);
                return (int) (((double) fluidContainer.getFirstFluid().getFluidAmount() /
                        fluidContainer.getTankCapacity(0)) * 13);
            }

            @Override
            public boolean isBarVisible(ItemStack stack) {
                return FluidUtils.hasFluid(stack);
            }

            @Override
            public boolean showEmptyBar(ItemStack itemStack) {
                return false;
            }
        };

        this.components.add(durabilityBar);
        durabilityBar.onAttached(this);
    }

    @Override
    public @NotNull SpaceArmorComponentItem setArmorLogic(@NotNull IArmorLogic armorLogic) {
        return (SpaceArmorComponentItem) super.setArmorLogic(armorLogic);
    }

    @Override
    public WrappedItemFluidContainer getFluidContainer(ItemStack holder) {
        return new WrappedItemFluidContainer(holder, new SimpleFluidContainer(FluidConstants.fromMillibuckets(tankSize), 1, (t, f) -> f.is(ModFluidTags.OXYGEN)));
    }

    @Override
    public void onArmorTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        super.onArmorTick(stack, level, player);
        if (level.isClientSide) return;
        if (player.isCreative() || player.isSpectator()) return;
        player.setTicksFrozen(0);
        if (player.tickCount % 20 == 0 && SpaceArmorComponentItem.hasOxygen(player)) {
            if (!OxygenApi.API.hasOxygen(player)) SpaceArmorComponentItem.consumeOxygen(stack);
            if (player.isEyeInFluidType(ForgeMod.WATER_TYPE.get())) {
                SpaceArmorComponentItem.consumeOxygen(stack);
                player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + 4 * 10));
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(TooltipUtils.getFluidComponent(FluidUtils.getTank(stack), FluidConstants.fromMillibuckets(getFluidContainer(stack).getTankCapacity(0)), ModFluids.OXYGEN.get()));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        addSpaceFeatures(stack, tooltipComponents);
    }

    private void addSpaceFeatures(ItemStack stack, List<Component> lines) {
        boolean hasOxygen = getFluidContainer(stack).getFirstFluid().getFluidAmount() > FluidConstants.fromMillibuckets(1);
        boolean worn = ArmorTooltips.isWorn(stack);
        List<Component> space = new ArrayList<>(8);
        space.add(ArmorTooltips.section("metaarmor.gto.section.space"));
        // 与 IEnhancedPlayer.spaceTick 一致：有氧，且四个部位都是纳米肌体 / 夸克高科装备
        Component protection;
        if (!hasOxygen) protection = ArmorTooltips.oxygen(false, worn);
        else if (ArmorTooltips.isWornInSet(stack, SpaceArmorComponentItem::isSpaceSetPiece)) protection = ArmorTooltips.oxygen(true, true);
        else protection = ArmorTooltips.state("metaarmor.gto.state.need_set", ChatFormatting.YELLOW);
        // 无氧环境下每秒扣 2 mB，水下再扣 2 mB
        Component oxygenCost = Component.translatable("metaarmor.gto.cost.oxygen", 2).withStyle(ChatFormatting.GRAY);
        ArmorTooltips.addFeature(space, "metaarmor.gto.feature.space_protection", protection, oxygenCost);
        ArmorTooltips.addDetail(space, "metaarmor.gto.detail.space_protection");
        ArmorTooltips.addFeature(space, "metaarmor.gto.feature.underwater_breath", ArmorTooltips.oxygen(hasOxygen, worn), oxygenCost);
        ArmorTooltips.addDetail(space, "metaarmor.gto.detail.underwater_breath");
        // 夸克胸甲本身已列出免疫冰冻；纳米胸甲的免疫冰冻来自太空胸甲
        if (!(getArmorLogic() instanceof QuarkTechSuite)) {
            ArmorTooltips.addFeature(space, "metaarmor.gto.feature.freeze_immune", ArmorTooltips.piecePassive(), null);
        }
        // 太空胸甲都带 PPE 标签；逻辑本身不是 PPE 的（纳米胸甲 I）在这里补一行
        if (!getArmorLogic().isPPE()) {
            ArmorTooltips.addFeature(space, "metaarmor.gto.feature.ppe", ArmorTooltips.setPassive(stack, ArmorTooltips::isPPE), null);
            ArmorTooltips.addDetail(space, "metaarmor.gto.detail.ppe");
        }
        // 放在"按住 Shift 查看说明"之前
        int hint = lines.indexOf(ArmorTooltips.SHIFT_HINT);
        lines.addAll(hint < 0 ? lines.size() : hint, space);
    }

    private static boolean isSpaceSetPiece(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorComponentItem item)) return false;
        String path = ItemUtils.getIdLocation(item).getPath();
        return path.contains("nanomuscle") || path.contains("quarktech");
    }

    public static long getOxygenAmount(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return 0;
        var stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (!(stack.getItem() instanceof SpaceArmorComponentItem suit)) return 0;
        return suit.getFluidContainer(stack).getFirstFluid().getFluidAmount();
    }

    public static boolean hasOxygen(Entity entity) {
        return getOxygenAmount(entity) > FluidConstants.fromMillibuckets(1);
    }

    private static void consumeOxygen(ItemStack stack) {
        ItemStackHolder holder = new ItemStackHolder(stack);
        var container = FluidContainer.of(holder);
        if (container == null) return;
        FluidHolder extracted = container.extractFluid(container.getFirstFluid().copyWithAmount(FluidConstants.fromMillibuckets(2)), false);
        if (holder.isDirty() || extracted.getFluidAmount() > 0) stack.setTag(holder.getStack().getTag());
    }
}
