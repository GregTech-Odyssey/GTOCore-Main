package com.gtocore.mixin.mc.mob;

import com.gtocore.api.entity.ILivingEntity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

import com.mojang.datafixers.util.Pair;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntity {

    @Shadow
    protected abstract void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean hitByPlayer);

    @Shadow
    protected abstract void dropEquipment();

    @Shadow
    public abstract RandomSource getRandom();

    protected LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "die", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void gto$fixSpam(Logger instance, String s, Object o1, Object o2) {}

    /**
     * 原版把同一个 ItemStack 既存为 lastHand/ArmorItemStacks 又放进装备包。单人本地连接不序列化包，
     * 客户端实体会直接持有这个对象，客户端线程写它的 NBT 时与服务端每 tick 的 equipmentHasChanged 比较并发，
     * 非线程安全的 NBT map 会被读到中间状态而崩溃（GregTech-Odyssey#1753）。给包里换成独立副本。
     */
    @ModifyArg(method = "handleEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundSetEquipmentPacket;<init>(ILjava/util/List;)V"), index = 1)
    private List<Pair<EquipmentSlot, ItemStack>> gtocore$unshareEquipmentPacketStacks(List<Pair<EquipmentSlot, ItemStack>> slots) {
        for (int i = 0, size = slots.size(); i < size; i++) {
            var pair = slots.get(i);
            slots.set(i, Pair.of(pair.getFirst(), pair.getSecond().copy()));
        }
        return slots;
    }

    @Override
    public void gtocore$getAllDeathLoot(DamageSource source, Set<ItemStack> itemStacks, int multiplier, boolean filterNbt) {
        this.captureDrops(new ArrayList<>());
        this.dropCustomDeathLoot(source, ForgeHooks.getLootingLevel(this, source.getEntity(), source), true);
        this.dropEquipment();
        this.captureDrops(null).forEach(e -> {
            if (e != null) {
                var item = e.getItem();
                if (filterNbt && item.hasTag()) {
                    return;
                }
                var count = item.getCount();
                if (count < 1) return;
                item.setCount(count * getRandom().nextInt(multiplier / 2, multiplier));
                if (item.isEmpty()) return;
                itemStacks.add(item);
            }
        });
    }
}
