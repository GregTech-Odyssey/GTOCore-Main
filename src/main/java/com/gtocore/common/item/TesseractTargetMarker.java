package com.gtocore.common.item;

import com.gtocore.common.machine.tesseract.ITesseractMarkerInteractable;
import com.gtocore.common.machine.tesseract.TesseractDirectedTarget;
import com.gtocore.common.machine.tesseract.TesseractUI;

import com.gtolib.api.network.NetworkPack;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.gtocore.common.machine.tesseract.ITesseractMarkerInteractable.IMPORT_SUCCESS_TEXT;

@Mod.EventBusSubscriber
public class TesseractTargetMarker implements IItemUIFactory {

    public static final int LEFT_TAIL = 0;
    public static final int LEFT_REMOVE = 1;
    public static final int FACE_CLICKED = 0;
    public static final int FACE_OPPOSITE = 1;
    private static final String LEFT_MODE = "left_mode";
    private static final String FACE_MODE = "face_mode";

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        return TesseractUI.markerUI(holder, player);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        if (isTesseractTargetMarker(itemStack)) {
            var player = context.getPlayer();
            var level = context.getLevel();
            var pos = context.getClickedPos();
            var face = context.getClickedFace();
            if (level.isClientSide() || player == null) {
                return InteractionResult.PASS;
            }
            if (player.isShiftKeyDown()) {
                if (!removePatternFace(itemStack, level.dimension(), pos, face)) return InteractionResult.PASS;
            } else {
                addPatternFace(itemStack, level.dimension(), pos, recordedFace(itemStack, face), true);
            }
            return InteractionResult.SUCCESS;
        }
        return IItemUIFactory.super.onItemUseFirst(itemStack, context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (rayTrace(level, player).getType() != HitResult.Type.MISS || !isTesseractTargetMarker(itemStack)) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (player.isShiftKeyDown()) {
            clearAllPatternFaces(itemStack);
            return InteractionResultHolder.success(itemStack);
        }
        return IItemUIFactory.super.use(item, level, player, usedHand);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack itemStack = event.getEntity().getMainHandItem();

        if (isTesseractTargetMarker(itemStack)) {
            var player = event.getEntity();
            var level = event.getLevel();
            var pos = event.getPos();
            var face = event.getFace();
            event.setCanceled(true);
            if (level.isClientSide() || player == null) {
                event.setCancellationResult(InteractionResult.PASS);
                return;
            }
            if (player.isShiftKeyDown()) {
                if (ILevel.getCachedBlockEntity(level, pos) instanceof MetaMachineBlockEntity mbe &&
                        mbe.getMetaMachine() instanceof ITesseractMarkerInteractable interactable &&
                        interactable.onMarkerInteract(player, getOrderedTargets(itemStack))) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                } else {
                    event.setCancellationResult(InteractionResult.PASS);
                }
                return;
            } else if (getLeftMode(itemStack) == LEFT_REMOVE) {
                removeBlock(itemStack, level.dimension(), pos);
            } else {
                addPatternFace(itemStack, level.dimension(), pos, recordedFace(itemStack, face), false);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    public static boolean isTesseractTargetMarker(ItemStack stack) {
        if (stack.getItem() instanceof ComponentItem c) {
            return c.getComponents().stream().anyMatch(comp -> comp instanceof TesseractTargetMarker);
        }
        return false;
    }

    private static List<PatternFaceUnindexed> getFromNBT(ItemStack stack, boolean positive) {
        String key = positive ? "positive" : "negative";
        var nbt = stack.getOrCreateTag();
        if (nbt.contains(key)) {
            var list = nbt.getList(key, 10);
            List<PatternFaceUnindexed> result = new ArrayList<>();
            for (var i = 0; i < list.size(); i++) {
                var entry = list.getCompound(i);
                var dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(entry.getString("dim")));
                var pos = BlockPos.of(entry.getLong("pos"));
                var face = Direction.from3DDataValue(entry.getInt("face"));
                result.add(new PatternFaceUnindexed(GlobalPos.of(dim, pos), face));
            }
            return result;
        }
        return new ArrayList<>();
    }

    private static void putToNBT(ItemStack stack, boolean positive, List<PatternFaceUnindexed> patternFaces) {
        String key = positive ? "positive" : "negative";
        var nbt = stack.getOrCreateTag();
        var list = new net.minecraft.nbt.ListTag();
        for (var pf : patternFaces) {
            var entry = new net.minecraft.nbt.CompoundTag();
            entry.putString("dim", pf.pos().dimension().location().toString());
            entry.putLong("pos", pf.pos().pos().asLong());
            entry.putInt("face", pf.face().get3DDataValue());
            list.add(entry);
        }
        nbt.put(key, list);
    }

    public static void addPatternFace(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos, Direction face, boolean addToPositive) {
        var globalPos = GlobalPos.of(dimension, pos);
        var patternFace = new PatternFaceUnindexed(globalPos, face);
        var patternFaces = getFromNBT(stack, addToPositive);
        var patternFacesF = getFromNBT(stack, !addToPositive);
        if (!patternFaces.contains(patternFace) && !patternFacesF.contains(patternFace)) {
            patternFaces.add(patternFace);
            putToNBT(stack, addToPositive, patternFaces);
        }
    }

    public static boolean removePatternFace(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos, Direction face) {
        var globalPos = GlobalPos.of(dimension, pos);
        var patternFace = new PatternFaceUnindexed(globalPos, face);
        var patternFacesPositive = getFromNBT(stack, true);
        var patternFacesNegative = getFromNBT(stack, false);
        var removed = patternFacesPositive.remove(patternFace) || patternFacesNegative.remove(patternFace);
        if (removed) {
            putToNBT(stack, true, patternFacesPositive);
            putToNBT(stack, false, patternFacesNegative);
        }
        return removed;
    }

    public static void clearAllPatternFaces(ItemStack stack) {
        putToNBT(stack, true, new ArrayList<>());
        putToNBT(stack, false, new ArrayList<>());
    }

    public static List<TesseractDirectedTarget> getOrderedTargets(ItemStack stack) {
        var ordered = ordered(stack);
        if (ordered.isEmpty()) return Collections.emptyList();
        var result = new ArrayList<TesseractDirectedTarget>(ordered.size());
        for (int i = 0; i < ordered.size(); i++) {
            var face = ordered.get(i);
            result.add(new TesseractDirectedTarget(face.pos(), face.face(), i + 1));
        }
        return result;
    }

    public static int count(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return 0;
        return tag.getList("positive", 10).size() + tag.getList("negative", 10).size();
    }

    public static int signature(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return 0;
        return 31 * tag.getList("positive", 10).hashCode() + tag.getList("negative", 10).hashCode();
    }

    public static boolean isTail(ItemStack stack, int index) {
        var tag = stack.getTag();
        return tag != null && index >= tag.getList("positive", 10).size();
    }

    @Nullable
    public static Direction getFace(ItemStack stack, int index) {
        var ordered = ordered(stack);
        return index >= 0 && index < ordered.size() ? ordered.get(index).face() : null;
    }

    public static void setFace(ItemStack stack, int index, Direction face) {
        var ordered = ordered(stack);
        if (index < 0 || index >= ordered.size()) return;
        var replaced = new PatternFaceUnindexed(ordered.get(index).pos(), face);
        if (ordered.contains(replaced)) return;
        ordered.set(index, replaced);
        writeOrdered(stack, ordered);
    }

    public static void move(ItemStack stack, int index, int delta) {
        var ordered = ordered(stack);
        int target = index + delta;
        if (index < 0 || index >= ordered.size() || target < 0 || target >= ordered.size()) return;
        Collections.swap(ordered, index, target);
        writeOrdered(stack, ordered);
    }

    public static void remove(ItemStack stack, int index) {
        var ordered = ordered(stack);
        if (index < 0 || index >= ordered.size()) return;
        ordered.remove(index);
        writeOrdered(stack, ordered);
    }

    public static void removeBlock(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos) {
        var globalPos = GlobalPos.of(dimension, pos);
        var positive = getFromNBT(stack, true);
        var negative = getFromNBT(stack, false);
        boolean removedPositive = positive.removeIf(f -> f.pos().equals(globalPos));
        boolean removedNegative = negative.removeIf(f -> f.pos().equals(globalPos));
        if (removedPositive || removedNegative) {
            putToNBT(stack, true, positive);
            putToNBT(stack, false, negative);
        }
    }

    public static int getLeftMode(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null ? LEFT_TAIL : tag.getInt(LEFT_MODE);
    }

    public static void setLeftMode(ItemStack stack, int mode) {
        setMode(stack, LEFT_MODE, mode == LEFT_REMOVE ? LEFT_REMOVE : LEFT_TAIL);
    }

    public static int getFaceMode(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null ? FACE_CLICKED : tag.getInt(FACE_MODE);
    }

    public static void setFaceMode(ItemStack stack, int mode) {
        setMode(stack, FACE_MODE, mode == FACE_OPPOSITE ? FACE_OPPOSITE : FACE_CLICKED);
    }

    private static void setMode(ItemStack stack, String key, int mode) {
        if (mode == 0) {
            CompoundTag tag = stack.getTag();
            if (tag != null) tag.remove(key);
        } else {
            stack.getOrCreateTag().putInt(key, mode);
        }
    }

    private static Direction recordedFace(ItemStack stack, Direction clicked) {
        return getFaceMode(stack) == FACE_OPPOSITE ? clicked.getOpposite() : clicked;
    }

    private static List<PatternFaceUnindexed> ordered(ItemStack stack) {
        var result = getFromNBT(stack, true);
        var negative = getFromNBT(stack, false);
        for (int i = negative.size() - 1; i >= 0; i--) result.add(negative.get(i));
        return result;
    }

    private static void writeOrdered(ItemStack stack, List<PatternFaceUnindexed> ordered) {
        putToNBT(stack, true, ordered);
        putToNBT(stack, false, new ArrayList<>());
    }

    public static void copyConfigFrom(ITesseractMarkerInteractable source, ItemStack target) {
        putToNBT(target, true, source.getMarkerTargets().stream().map(t -> new PatternFaceUnindexed(t.pos(), t.face())).toList());
    }

    public static void sendCopyConfigPacket(Level level, Player player) {
        COPY_CONFIG_C2S.send(buf -> buf.writeBlockPos(rayTrace(level, player).getBlockPos()));
    }

    private static BlockHitResult rayTrace(Level level, Player player) {
        Vec3 playerPos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().normalize();
        double range = player.getAttributeValue(ForgeMod.BLOCK_REACH.get());
        Vec3 toPos = playerPos.add(lookVec.scale(range));

        ClipContext clipCtx = new ClipContext(playerPos, toPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null);
        return level.clip(clipCtx);
    }

    public static final NetworkPack COPY_CONFIG_C2S = NetworkPack.registerC2S("copy_tesseract_marker_config", (pl, buf) -> {
        var pos = buf.readBlockPos();
        if (isTesseractTargetMarker(pl.getMainHandItem()) &&
                ILevel.getCachedBlockEntity(pl.level(), pos) instanceof MetaMachineBlockEntity mbe &&
                mbe.getMetaMachine() instanceof ITesseractMarkerInteractable interactable) {
            copyConfigFrom(interactable, pl.getMainHandItem());
            pl.displayClientMessage(Component.translatable(IMPORT_SUCCESS_TEXT), true);
        }
    });

    private record PatternFaceUnindexed(GlobalPos pos, Direction face) implements Comparable<PatternFaceUnindexed> {

        @Override
        public int compareTo(@NotNull TesseractTargetMarker.PatternFaceUnindexed o) {
            var i = pos.pos().compareTo(o.pos.pos());
            if (i != 0) return i;
            return Integer.compare(face.ordinal(), o.face.ordinal());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PatternFaceUnindexed that = (PatternFaceUnindexed) o;
            return Objects.equals(pos(), that.pos()) && face() == that.face();
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos(), face());
        }
    }
}
