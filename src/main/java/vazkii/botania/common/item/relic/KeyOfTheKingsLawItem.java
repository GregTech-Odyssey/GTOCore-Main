package vazkii.botania.common.item.relic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.entity.BabylonWeaponEntity;

/**
 * 王者之匙：持续使用会环绕玩家生成巴比伦武器，松手后武器齐射。
 */
public class KeyOfTheKingsLawItem extends Item {

    private static final String TAG_WEAPONS_SPAWNED = "weaponsSpawned";
    private static final String TAG_CHARGING = "charging";

    public static final int WEAPON_TYPES = 12;

    public KeyOfTheKingsLawItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        setCharging(stack, true);
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity living, int time) {
        if (getWeaponsSpawned(stack) == 20) {
            setCharging(stack, false);
            setWeaponsSpawned(stack, 0);
        }
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity living, @NotNull ItemStack stack, int count) {
        int spawned = getWeaponsSpawned(stack);
        if (count == getUseDuration(stack) || spawned >= 20 || level.isClientSide) {
            return;
        }
        Vec3 look = living.getLookAngle().multiply(1, 0, 1);
        double playerRot = Math.toRadians(living.getYRot() + 90);
        if (look.x == 0 && look.z == 0) {
            look = new Vec3(Math.cos(playerRot), 0, Math.sin(playerRot));
        }
        look = look.normalize().scale(-2);

        int div = spawned / 5;
        int mod = spawned % 5;
        Vec3 center = living.getBoundingBox().getCenter();
        Vec3 pl = look.add(center).add(0, 1.6, div * 0.1);

        var rand = level.random;
        Vec3 axis = look.normalize().cross(new Vec3(-1, 0, -1)).normalize();
        double rot = mod * Math.PI / 4 - Math.PI / 2;
        Vec3 axis1 = axis.scale(div * 3.5 + 5).yRot((float) rot);
        if (axis1.y < 0) {
            axis1 = axis1.multiply(1, -1, 1);
        }
        Vec3 end = pl.add(axis1);

        BabylonWeaponEntity weapon = new BabylonWeaponEntity(living, level);
        weapon.setPos(end.x, end.y, end.z);
        weapon.setYRot(living.getYRot());
        weapon.setVariety(rand.nextInt(WEAPON_TYPES));
        weapon.setDelay(spawned);
        weapon.setRotation(Mth.wrapDegrees(-living.getYRot() + 180));
        level.addFreshEntity(weapon);
        setWeaponsSpawned(stack, spawned + 1);
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrCreateTag();
    }

    public static boolean isCharging(ItemStack stack) {
        return tag(stack).getBoolean(TAG_CHARGING);
    }

    public static int getWeaponsSpawned(ItemStack stack) {
        return tag(stack).getInt(TAG_WEAPONS_SPAWNED);
    }

    public static void setCharging(ItemStack stack, boolean charging) {
        tag(stack).putBoolean(TAG_CHARGING, charging);
    }

    public static void setWeaponsSpawned(ItemStack stack, int count) {
        tag(stack).putInt(TAG_WEAPONS_SPAWNED, count);
    }
}
