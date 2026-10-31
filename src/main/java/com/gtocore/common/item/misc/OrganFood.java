package com.gtocore.common.item.misc;

import com.gtocore.common.data.GTOOrganItems;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.OrganTier;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IDataComponentItem;
import com.gregtechceu.gtceu.api.item.ItemDataComponentKey;
import com.gregtechceu.gtceu.api.item.component.FoodStats;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.common.item.armor.ArmorTooltips;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;

import com.gto.registrate.util.entry.ItemEntry;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.gtolib.utils.register.ItemRegisterUtils.item;

/**
 * 吃器官：萌芽级器官生吃、烤熟、全席，来源玩家组件，以及食用公告、幻痛、喂狗、啃不动、排异、统计与成就。
 */
@DataGeneratorScanned
public final class OrganFood {

    private OrganFood() {}

    @RegisterLanguage(cn = "取自 %s", en = "Taken from %s")
    private static final String INFO_OWNER = "gtocore.organ_food.owner";
    @RegisterLanguage(cn = "食用后解除中毒", en = "Cures poison when eaten")
    private static final String INFO_ANTIDOTE = "gtocore.organ_food.antidote";
    @RegisterLanguage(cn = "某个无名氏", en = "some nameless soul")
    private static final String OWNER_UNKNOWN = "gtocore.organ_food.owner_unknown";
    @RegisterLanguage(cn = "整个身体", en = "an entire body")
    private static final String WHOLE_BODY = "gtocore.organ_food.whole_body";
    @RegisterLanguage(cn = "%s 全席", en = "%s Feast")
    private static final String FEAST_NAMED = "gtocore.organ_food.feast_named";
    @RegisterLanguage(cn = "%s 的狗", en = "%s's dog")
    private static final String DOG_OF = "gtocore.organ_food.dog_of";

    @RegisterLanguage(cn = "我擦嘞，%s 吃掉了 %s 的%s！！！害怕", en = "Holy crap, %s ate %s's %s!!! Terrifying")
    private static final String MESSAGE_EATEN = "gtocore.organ_food.eaten";
    @RegisterLanguage(cn = "我擦嘞，%s 把自己的%s吃了？？？自给自足了属于是", en = "Holy crap, %s ate their own %s??? Talk about self-sufficiency")
    private static final String MESSAGE_EATEN_SELF = "gtocore.organ_food.eaten_self";
    @RegisterLanguage(cn = "我擦嘞，%s 把 %s 整个人吃了！！！害怕", en = "Holy crap, %s ate %s whole!!! Terrifying")
    private static final String MESSAGE_FEAST = "gtocore.organ_food.feast";
    @RegisterLanguage(cn = "我擦嘞，%s 把自己整个人吃了？？？", en = "Holy crap, %s ate their entire self???")
    private static final String MESSAGE_FEAST_SELF = "gtocore.organ_food.feast_self";
    @RegisterLanguage(cn = "你试图啃一口%s，牙崩了", en = "You tried to bite the %s and chipped a tooth")
    private static final String MESSAGE_BITE = "gtocore.organ_food.bite";
    @RegisterLanguage(cn = "你突然感到一阵幻痛……仿佛失去了%s", en = "A sudden phantom pain... as if you had lost your %s")
    private static final String MESSAGE_PHANTOM = "gtocore.organ_food.phantom";
    @RegisterLanguage(cn = "排异反应：你的身体在排斥 %s 的%s", en = "Rejection: your body is fighting %s's %s")
    private static final String MESSAGE_REJECTION = "gtocore.organ_food.rejection";
    @RegisterLanguage(cn = "%s 达成了成就 %s", en = "%s has earned the achievement %s")
    private static final String MESSAGE_ACHIEVEMENT = "gtocore.organ_food.achievement";

    @RegisterLanguage(cn = "尝鲜", en = "First Bite")
    private static final String ACH_FIRST = "gtocore.organ_food.ach.first";
    @RegisterLanguage(cn = "第一次吃下器官", en = "Eat an organ for the first time")
    private static final String ACH_FIRST_DESC = "gtocore.organ_food.ach.first.desc";
    @RegisterLanguage(cn = "汉尼拔", en = "Hannibal")
    private static final String ACH_HANNIBAL = "gtocore.organ_food.ach.hannibal";
    @RegisterLanguage(cn = "吃下别人的器官", en = "Eat someone else's organ")
    private static final String ACH_HANNIBAL_DESC = "gtocore.organ_food.ach.hannibal.desc";
    @RegisterLanguage(cn = "庖丁", en = "Master Butcher")
    private static final String ACH_BUTCHER = "gtocore.organ_food.ach.butcher";
    @RegisterLanguage(cn = "九种器官都吃过一遍", en = "Eat all nine kinds of organs")
    private static final String ACH_BUTCHER_DESC = "gtocore.organ_food.ach.butcher.desc";

    @RegisterLanguage(cn = "吃掉的器官", en = "Organs Eaten")
    private static final String STAT_EATEN_NAME = "stat.gtocore.organs_eaten";
    @RegisterLanguage(cn = "吃掉的他人器官", en = "Other Players' Organs Eaten")
    private static final String STAT_FOREIGN_NAME = "stat.gtocore.foreign_organs_eaten";

    private static final ResourceLocation STAT_EATEN = GTOCore.id("organs_eaten");
    private static final ResourceLocation STAT_FOREIGN = GTOCore.id("foreign_organs_eaten");

    private static final float RAW_SATURATION = 0.3F;
    private static final float COOKED_SATURATION = 0.8F;
    private static final int RAW_POISON_TICKS = 200;
    private static final int FEAST_BUFF_TICKS = 2400;
    private static final int PHANTOM_TICKS = 100;
    private static final int BITE_COOLDOWN = 20;
    private static final float BITE_DAMAGE = 1.0F;
    private static final int REJECTION_INTERVAL = 600;
    private static final float REJECTION_CHANCE = 0.15F;

    private record Diet(int raw, int cooked, @Nullable MobEffect buff, int buffTicks) {}

    private static final EnumMap<OrganType, Diet> DIETS = new EnumMap<>(OrganType.class);

    static {
        DIETS.put(OrganType.EYE, new Diet(1, 3, MobEffects.NIGHT_VISION, 1200));
        DIETS.put(OrganType.LUNG, new Diet(2, 5, MobEffects.WATER_BREATHING, 1200));
        DIETS.put(OrganType.HEART, new Diet(3, 8, MobEffects.REGENERATION, 200));
        DIETS.put(OrganType.LIVER, new Diet(3, 7, null, 0));
        DIETS.put(OrganType.SPINE, new Diet(1, 4, MobEffects.DAMAGE_RESISTANCE, 600));
        DIETS.put(OrganType.LEFT_ARM, new Diet(3, 6, MobEffects.DIG_SPEED, 600));
        DIETS.put(OrganType.RIGHT_ARM, new Diet(3, 6, MobEffects.DIG_SPEED, 600));
        DIETS.put(OrganType.LEFT_LEG, new Diet(4, 8, MobEffects.MOVEMENT_SPEED, 600));
        DIETS.put(OrganType.RIGHT_LEG, new Diet(4, 8, MobEffects.MOVEMENT_SPEED, 600));
    }

    // 来源玩家

    public record Owner(UUID id, String name) {

        public static final ItemDataComponentKey<Owner, CompoundTag> KEY = ItemDataComponentKey.create("gtocore_organ_owner", Owner::write, Owner::read);

        private static CompoundTag write(Owner owner) {
            var tag = new CompoundTag();
            tag.putUUID("id", owner.id);
            tag.putString("name", owner.name);
            return tag;
        }

        private static Owner read(CompoundTag tag) {
            return new Owner(tag.getUUID("id"), tag.getString("name"));
        }
    }

    @Nullable
    public static Owner owner(ItemStack stack) {
        return IDataComponentItem.get(stack, Owner.KEY);
    }

    private static ItemStack carryOwner(ItemStack from, ItemStack to) {
        var owner = owner(from);
        if (owner != null) IDataComponentItem.set(to, Owner.KEY, owner);
        return to;
    }

    private static boolean isOwner(@Nullable Owner owner, Player player) {
        return owner != null && owner.id.equals(player.getUUID());
    }

    public static ItemStack sprout(OrganType type, Player owner) {
        var stack = GTOOrganItems.tierOrgan(type, OrganTier.SPROUT).asStack();
        IDataComponentItem.set(stack, Owner.KEY, new Owner(owner.getUUID(), owner.getGameProfile().getName()));
        return stack;
    }

    // 物品

    private static final EnumMap<OrganType, ItemEntry<ComponentItem>> COOKED = new EnumMap<>(OrganType.class);
    private static ItemEntry<FeastItem> feast;

    public static void initItems() {
        for (var type : OrganType.BODY) {
            COOKED.put(type, item("cooked_" + type.key, "烤" + type.cn, p -> ComponentItem.create(p.stacksTo(1)))
                    .lang("Cooked " + type.en)
                    .model((ctx, prov) -> prov.generated(ctx, GTOCore.id("item/organ/cooked/" + type.key)))
                    .onRegister(attach(new Food(Kind.COOKED, type)))
                    .register());
        }
        feast = item("organ_feast", "人体全席", p -> new FeastItem(p.stacksTo(1)))
                .lang("Full-Body Feast")
                .model((ctx, prov) -> prov.generated(ctx, GTOCore.id("item/organ/cooked/feast")))
                .onRegister(attach(new Food(Kind.FEAST, null)))
                .register();
    }

    public static Food raw(OrganType type) {
        return new Food(Kind.RAW, type);
    }

    private static final class FeastItem extends ComponentItem {

        private FeastItem(Properties properties) {
            super(properties);
        }

        @Override
        public @NotNull Component getName(@NotNull ItemStack stack) {
            var owner = owner(stack);
            return owner == null ? super.getName(stack) : Component.translatable(FEAST_NAMED, owner.name);
        }
    }

    // 配方

    public static void initRecipes() {
        var feastInputs = NonNullList.<Ingredient>create();
        for (var type : OrganType.BODY) {
            var raw = Ingredient.of(GTOOrganItems.tierOrgan(type, OrganTier.SPROUT));
            var cooked = COOKED.get(type).asStack();
            var smelting = GTOCore.id("smelting/cooked_" + type.key);
            var smoking = GTOCore.id("smoking/cooked_" + type.key);
            var campfire = GTOCore.id("campfire/cooked_" + type.key);
            GTRecipes.RECIPE_MAP.put(smelting, new Smelting(smelting, raw, cooked));
            GTRecipes.RECIPE_MAP.put(smoking, new Smoking(smoking, raw, cooked));
            GTRecipes.RECIPE_MAP.put(campfire, new Campfire(campfire, raw, cooked));
            feastInputs.add(Ingredient.of(COOKED.get(type)));
        }
        var feastId = GTOCore.id("shapeless/organ_feast");
        GTRecipes.RECIPE_MAP.put(feastId, new FeastRecipe(feastId, feast.asStack(), feastInputs));
    }

    private static final class Smelting extends SmeltingRecipe {

        private Smelting(ResourceLocation id, Ingredient input, ItemStack output) {
            super(id, "", CookingBookCategory.FOOD, input, output, 0.35F, 200);
        }

        @Override
        public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
            return carryOwner(container.getItem(0), super.assemble(container, registryAccess));
        }
    }

    private static final class Smoking extends SmokingRecipe {

        private Smoking(ResourceLocation id, Ingredient input, ItemStack output) {
            super(id, "", CookingBookCategory.FOOD, input, output, 0.35F, 100);
        }

        @Override
        public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
            return carryOwner(container.getItem(0), super.assemble(container, registryAccess));
        }
    }

    private static final class Campfire extends CampfireCookingRecipe {

        private Campfire(ResourceLocation id, Ingredient input, ItemStack output) {
            super(id, "", CookingBookCategory.FOOD, input, output, 0.35F, 600);
        }

        @Override
        public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
            return carryOwner(container.getItem(0), super.assemble(container, registryAccess));
        }
    }

    private static final class FeastRecipe extends ShapelessRecipe {

        private FeastRecipe(ResourceLocation id, ItemStack result, NonNullList<Ingredient> ingredients) {
            super(id, "", CraftingBookCategory.MISC, result, ingredients);
        }

        @Override
        public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
            if (!super.matches(container, level)) return false;
            ItemStack first = ItemStack.EMPTY;
            for (int i = 0; i < container.getContainerSize(); i++) {
                var stack = container.getItem(i);
                if (stack.isEmpty()) continue;
                if (first.isEmpty()) {
                    first = stack;
                } else if (!sameOwner(owner(first), owner(stack))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public @NotNull ItemStack assemble(@NotNull CraftingContainer container, @NotNull RegistryAccess registryAccess) {
            var result = super.assemble(container, registryAccess);
            for (int i = 0; i < container.getContainerSize(); i++) {
                var stack = container.getItem(i);
                if (!stack.isEmpty()) return carryOwner(stack, result);
            }
            return result;
        }

        private static boolean sameOwner(@Nullable Owner a, @Nullable Owner b) {
            return Objects.equals(a == null ? null : a.id, b == null ? null : b.id);
        }
    }

    // 食用

    private enum Kind {
        RAW,
        COOKED,
        FEAST
    }

    public static final class Food extends FoodStats {

        private final Kind kind;
        @Nullable
        private final OrganType type;

        private Food(Kind kind, @Nullable OrganType type) {
            super(properties(kind, type));
            this.kind = kind;
            this.type = type;
        }

        private static FoodProperties properties(Kind kind, @Nullable OrganType type) {
            var builder = new FoodProperties.Builder().meat();
            switch (kind) {
                case RAW -> builder.nutrition(DIETS.get(type).raw).saturationMod(RAW_SATURATION)
                        .effect(() -> new MobEffectInstance(MobEffects.POISON, RAW_POISON_TICKS), 1.0F);
                case COOKED -> {
                    var diet = DIETS.get(type);
                    builder.nutrition(diet.cooked).saturationMod(COOKED_SATURATION);
                    if (diet.buff != null) builder.effect(() -> new MobEffectInstance(diet.buff, diet.buffTicks), 1.0F);
                }
                case FEAST -> {
                    builder.nutrition(20).saturationMod(1.0F).alwaysEat();
                    var added = new ReferenceOpenHashSet<MobEffect>();
                    for (var diet : DIETS.values()) {
                        var buff = diet.buff;
                        if (buff == null || !added.add(buff)) continue;
                        builder.effect(() -> buff == MobEffects.REGENERATION ? new MobEffectInstance(buff, diet.buffTicks * 2, 1) : new MobEffectInstance(buff, FEAST_BUFF_TICKS), 1.0F);
                    }
                }
            }
            return builder.build();
        }

        private boolean curesPoison() {
            return kind == Kind.FEAST || (kind == Kind.COOKED && type == OrganType.LIVER);
        }

        private Component part() {
            return type == null ? Component.translatable(WHOLE_BODY) : Component.translatable(type.translationKey);
        }

        @Override
        public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag isAdvanced) {
            var owner = owner(stack);
            if (owner != null) lines.add(ArmorTooltips.info(INFO_OWNER, ArmorTooltips.value(owner.name)));
            if (curesPoison()) lines.add(ArmorTooltips.info(INFO_ANTIDOTE));
            super.appendTooltips(stack, level, lines, isAdvanced);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack food, Level level, LivingEntity livingEntity) {
            if (!(livingEntity instanceof ServerPlayer player)) return super.finishUsingItem(food, level, livingEntity);
            var owner = owner(food);
            Component name = kind == Kind.COOKED ? food.getItem().getDescription() : part();
            boolean triedAllBefore = triedAll(player);
            var result = super.finishUsingItem(food, level, livingEntity);
            if (curesPoison()) player.removeEffect(MobEffects.POISON);
            Component message;
            if (kind == Kind.FEAST) {
                message = isOwner(owner, player) ? Component.translatable(MESSAGE_FEAST_SELF, player.getDisplayName()) :
                        Component.translatable(MESSAGE_FEAST, player.getDisplayName(), ownerName(owner));
            } else {
                message = isOwner(owner, player) ? Component.translatable(MESSAGE_EATEN_SELF, player.getDisplayName(), red(name)) :
                        Component.translatable(MESSAGE_EATEN, player.getDisplayName(), ownerName(owner), red(name));
            }
            player.server.getPlayerList().broadcastSystemMessage(message, false);
            phantomPain(player.server, player, owner, part());
            recordEaten(player, owner, kind == Kind.FEAST ? OrganType.BODY.length : 1, triedAllBefore);
            return result;
        }
    }

    @Nullable
    private static Food food(ItemStack stack) {
        if (stack.getItem() instanceof ComponentItem item) {
            for (var component : item.getComponents()) {
                if (component instanceof Food food) return food;
            }
        }
        return null;
    }

    private static Component ownerName(@Nullable Owner owner) {
        return owner == null ? Component.translatable(OWNER_UNKNOWN) : Component.literal(owner.name);
    }

    private static Component red(Component component) {
        return component.copy().withStyle(ChatFormatting.RED);
    }

    private static void phantomPain(MinecraftServer server, @Nullable Player eater, @Nullable Owner owner, Component part) {
        if (owner == null || (eater != null && isOwner(owner, eater))) return;
        var victim = server.getPlayerList().getPlayer(owner.id);
        if (victim == null) return;
        victim.sendSystemMessage(Component.translatable(MESSAGE_PHANTOM, part).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, PHANTOM_TICKS));
    }

    // 统计与成就

    private static void recordEaten(ServerPlayer player, @Nullable Owner owner, int count, boolean triedAllBefore) {
        var stats = player.getStats();
        if (stats.getValue(Stats.CUSTOM.get(STAT_EATEN)) == 0) achieve(player, ACH_FIRST, ACH_FIRST_DESC);
        player.awardStat(STAT_EATEN, count);
        if (owner != null && !isOwner(owner, player)) {
            if (stats.getValue(Stats.CUSTOM.get(STAT_FOREIGN)) == 0) achieve(player, ACH_HANNIBAL, ACH_HANNIBAL_DESC);
            player.awardStat(STAT_FOREIGN, count);
        }
        if (!triedAllBefore && triedAll(player)) achieve(player, ACH_BUTCHER, ACH_BUTCHER_DESC);
    }

    private static boolean triedAll(ServerPlayer player) {
        var stats = player.getStats();
        if (stats.getValue(Stats.ITEM_USED.get(feast.get())) > 0) return true;
        for (var type : OrganType.BODY) {
            if (stats.getValue(Stats.ITEM_USED.get(GTOOrganItems.tierOrgan(type, OrganTier.SPROUT).get())) == 0 &&
                    stats.getValue(Stats.ITEM_USED.get(COOKED.get(type).get())) == 0) {
                return false;
            }
        }
        return true;
    }

    private static void achieve(ServerPlayer player, String title, String description) {
        var name = ComponentUtils.wrapInSquareBrackets(Component.translatable(title)
                .withStyle(s -> s.withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(description)))));
        player.server.getPlayerList().broadcastSystemMessage(Component.translatable(MESSAGE_ACHIEVEMENT, player.getDisplayName(), name), false);
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    // 事件

    public static void init(IEventBus modBus) {
        modBus.addListener(OrganFood::registerStats);
        MinecraftForge.EVENT_BUS.addListener(OrganFood::onFeedDog);
        MinecraftForge.EVENT_BUS.addListener(OrganFood::onBite);
        MinecraftForge.EVENT_BUS.addListener(OrganFood::onPlayerTick);
    }

    private static void registerStats(RegisterEvent event) {
        if (event.getRegistryKey() != Registries.CUSTOM_STAT) return;
        Registry.register(BuiltInRegistries.CUSTOM_STAT, STAT_EATEN, STAT_EATEN);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, STAT_FOREIGN, STAT_FOREIGN);
        Stats.CUSTOM.get(STAT_EATEN, StatFormatter.DEFAULT);
        Stats.CUSTOM.get(STAT_FOREIGN, StatFormatter.DEFAULT);
    }

    private static void onFeedDog(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof Wolf wolf) || !wolf.isTame()) return;
        var stack = event.getItemStack();
        var food = food(stack);
        if (food == null) return;
        int count = stack.getCount();
        var owner = owner(stack);
        Component name = food.kind == Kind.COOKED ? stack.getItem().getDescription() : food.part();
        player.server.tell(new TickTask(player.server.getTickCount(), () -> {
            if (stack.getCount() >= count) return;
            Component dog;
            if (wolf.hasCustomName()) {
                dog = wolf.getCustomName();
            } else {
                var wolfOwner = wolf.getOwner();
                dog = Component.translatable(DOG_OF, (wolfOwner == null ? player : wolfOwner).getDisplayName());
            }
            player.server.getPlayerList().broadcastSystemMessage(Component.translatable(MESSAGE_EATEN, dog, ownerName(owner), red(name)), false);
            phantomPain(player.server, null, owner, food.part());
        }));
    }

    private static void onBite(PlayerInteractEvent.RightClickItem event) {
        var stack = event.getItemStack();
        if (!(stack.getItem() instanceof TierOrganItem organ) || organ.getTier() == OrganTier.SPROUT) return;
        var player = event.getEntity();
        player.getCooldowns().addCooldown(organ, BITE_COOLDOWN);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.hurt(serverPlayer.damageSources().generic(), BITE_DAMAGE);
            serverPlayer.displayClientMessage(Component.translatable(MESSAGE_BITE, stack.getHoverName()), true);
            serverPlayer.level().playSound(null, serverPlayer, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % REJECTION_INTERVAL != 0) return;
        var organs = IEnhancedPlayer.of(player).getPlayerData().organs;
        for (var type : OrganType.BODY) {
            var owner = owner(organs.getStackInSlot(type.firstSlot()));
            if (owner == null || isOwner(owner, player) || player.getRandom().nextFloat() >= REJECTION_CHANCE) continue;
            player.addEffect(player.getRandom().nextBoolean() ? new MobEffectInstance(MobEffects.CONFUSION, 200) : new MobEffectInstance(MobEffects.WEAKNESS, 400));
            player.displayClientMessage(Component.translatable(MESSAGE_REJECTION, owner.name, Component.translatable(type.translationKey)).withStyle(ChatFormatting.RED), true);
            return;
        }
    }
}
