package vazkii.botania.common.rune;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.mojang.datafixers.util.Either;
import vazkii.botania.api.recipe.RuneRitualRecipe;
import vazkii.botania.api.recipe.SpecialRuneInput;

import java.util.List;
import java.util.UUID;

/**
 * 克瓦希尔之血的特殊投入：仪式范围内必须有一名流浪商人，仪式开始时献祭，取消时还原。
 */
public class WanderingTraderRuneInput extends SpecialRuneInput {

    public static final WanderingTraderRuneInput INSTANCE = new WanderingTraderRuneInput();

    private WanderingTraderRuneInput() {
        super(new ResourceLocation("mythicbotany", "wandering_trader"));
    }

    @Override
    public Either<MutableComponent, CompoundTag> apply(Level level, BlockPos pos, RuneRitualRecipe recipe) {
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        List<WanderingTrader> traders = level.getEntities(EntityType.WANDERING_TRADER, new AABB(center, center).inflate(3), trader -> true);
        if (traders.isEmpty()) {
            return Either.left(Component.translatable("message.mythicbotany.ritual_no_trader"));
        }
        WanderingTrader trader = traders.get(0);
        if (trader.getVehicle() != null) {
            trader.stopRiding();
        }
        if (trader.isVehicle()) {
            trader.ejectPassengers();
        }
        CompoundTag traderData = trader.saveWithoutId(new CompoundTag());
        // 流浪商人本身没有战利品表，直接击杀不会掉落物品
        trader.kill();
        return Either.right(traderData);
    }

    @Override
    public List<Ingredient> getJeiInputItems() {
        // 展示用：带流浪商人皮肤的玩家头颅
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag tag = head.getOrCreateTag();
        CompoundTag owner = new CompoundTag();
        owner.putUUID("Id", UUID.fromString("3358ddae-3a41-4ba0-bdfa-ee54b6c55cf5"));
        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag texture = new CompoundTag();
        texture.putString("Value", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzc5YTgyMjkwZDdhYmUxZWZhYWJiYzcwNzEwZmYyZWMwMmRkMzRhZGUzODZiYzAwYzkzMGM0NjFjZjkzMiJ9fX0=");
        textures.add(texture);
        properties.put("textures", textures);
        owner.put("Properties", properties);
        tag.put("SkullOwner", owner);
        head.setHoverName(Component.translatable("entity.minecraft.wandering_trader").withStyle(ChatFormatting.RESET));
        return List.of(StrictNBTIngredient.of(head));
    }

    @Override
    public void cancel(Level level, BlockPos pos, RuneRitualRecipe recipe, CompoundTag nbt) {
        WanderingTrader trader = new WanderingTrader(EntityType.WANDERING_TRADER, level);
        UUID uuid = trader.getUUID();
        trader.setPos(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5);
        trader.load(nbt);
        trader.setUUID(uuid);
        level.addFreshEntity(trader);
    }
}
