package com.gtocore.common.machine.multiblock.electric.nano;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.config.GTOConfig;

import com.gtolib.api.machine.dynamic.DynamicBlockPattern;
import com.gtolib.api.machine.dynamic.DynamicCollisionManager;
import com.gtolib.api.machine.dynamic.DynamicRotationState;
import com.gtolib.api.machine.dynamic.DynamicVisualManager;
import com.gtolib.api.machine.dynamic.IDynamicStructureMachine;
import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class NanoPhagocytosisPlantMachine extends CrossRecipeMultiblockMachine implements IDynamicStructureMachine {

    public static final String INNER_VERTICAL_RING = "inner_vertical_ring";
    public static final String OUTER_VERTICAL_RING = "outer_vertical_ring";
    public static final String HORIZONTAL_RING = "horizontal_ring";
    private static final String[] RINGS = { INNER_VERTICAL_RING, OUTER_VERTICAL_RING, HORIZONTAL_RING };
    private static final float INNER_VERTICAL_SPEED = -.1F;
    private static final float OUTER_VERTICAL_SPEED = .07F;
    private static final float HORIZONTAL_SPEED = -.13F;

    private TickableSubscription dynamicSubscription;
    private DynamicBlockPattern dynamicPattern;
    private final DynamicRotationState innerVerticalRotation = new DynamicRotationState(40);
    private final DynamicRotationState outerVerticalRotation = new DynamicRotationState(40);
    private final DynamicRotationState horizontalRotation = new DynamicRotationState(40);
    private boolean blocksHidden;
    private boolean collisionHidden;

    public NanoPhagocytosisPlantMachine(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
    }

    @Override
    public boolean isDynamicStructureEnabled() {
        return GTOConfig.INSTANCE.gamePlay.enableDynamicStructures;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!isDynamicStructureEnabled()) return;
        if (!isRemote()) {
            if (DynamicCollisionManager.isEntityCollisionEnabled()) {
                DynamicCollisionManager.register(this);
                dynamicSubscription = subscribeServerTick(dynamicSubscription, this::tickDynamicPart);
            } else {
                hideCollision();
            }
        }
    }

    @Override
    public void onStructureFormedClient() {
        super.onStructureFormedClient();
        if (!isDynamicStructureEnabled()) return;
        DynamicVisualManager.register(this);
        if (!DynamicCollisionManager.isEntityCollisionEnabled() && hideDynamicParts()) blocksHidden = true;
        dynamicSubscription = subscribeClientTick(dynamicSubscription, this::tickDynamicPart);
    }

    private void tickDynamicPart() {
        boolean collisionEnabled = DynamicCollisionManager.isEntityCollisionEnabled();
        if (isRemote() && !blocksHidden && (isActive() || !collisionEnabled)) {
            if (!hideDynamicParts()) return;
            blocksHidden = true;
        }
        if (isActive()) {
            tickRotations(true);
            if (!isRemote()) hideCollision();
        } else {
            tickRotations(false);
            if (!hasRotation() && (!isRemote() || collisionEnabled)) restoreDynamicPart();
        }
    }

    private void restoreDynamicPart() {
        if (isRemote()) {
            if (blocksHidden) {
                for (int index = 0; index < RINGS.length; index++) {
                    DynamicVisualManager.showPart(this, RINGS[index]);
                }
            }
            blocksHidden = false;
        } else {
            restoreCollision();
        }
    }

    private boolean hideDynamicParts() {
        int hidden = 0;
        for (String partName : RINGS) {
            if (DynamicVisualManager.hidePart(this, partName)) {
                hidden++;
                continue;
            }
            for (int index = 0; index < hidden; index++) {
                DynamicVisualManager.showPart(this, RINGS[index]);
            }
            return false;
        }
        return true;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        clearDynamicPart();
    }

    @Override
    public void onStructureInvalidClient() {
        super.onStructureInvalidClient();
        clearDynamicPart();
    }

    @Override
    public void onUnload() {
        clearDynamicPart();
        super.onUnload();
    }

    private void clearDynamicPart() {
        if (isRemote()) {
            DynamicVisualManager.unregister(this);
        } else {
            DynamicCollisionManager.unregister(this);
        }
        restoreDynamicPart();
        if (dynamicSubscription != null) {
            dynamicSubscription.unsubscribe();
            dynamicSubscription = null;
        }
        innerVerticalRotation.reset();
        outerVerticalRotation.reset();
        horizontalRotation.reset();
    }

    private void hideCollision() {
        if (collisionHidden) return;
        for (int index = 0; index < RINGS.length; index++) {
            DynamicCollisionManager.hidePart(this, RINGS[index]);
        }
        collisionHidden = true;
    }

    private void restoreCollision() {
        if (!collisionHidden) return;
        for (int index = 0; index < RINGS.length; index++) {
            DynamicCollisionManager.showPart(this, RINGS[index]);
        }
        collisionHidden = false;
    }

    @Override
    @Nullable
    public Level getDynamicLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getDynamicOrigin() {
        return getPos();
    }

    @Override
    public DynamicBlockPattern getDynamicPattern() {
        if (dynamicPattern != null) return dynamicPattern;
        BlockPattern pattern = getDefinition().getPatternFactory()[0].get();
        if (pattern instanceof DynamicBlockPattern result) return dynamicPattern = result;
        throw new IllegalStateException("Nano phagocytosis plant pattern has no dynamic parts");
    }

    @Override
    public boolean isDynamicPartVisible(String partName) {
        return isDynamicStructureEnabled() && isRing(partName) && isFormed() &&
                (!DynamicCollisionManager.isEntityCollisionEnabled() || hasRotation());
    }

    @Override
    @Nullable
    public BlockState getDynamicBlockState(String partName, char symbol) {
        if (!isRing(partName)) return null;
        Block block = switch (symbol) {
            case 'B' -> GTBlocks.HIGH_POWER_CASING.get();
            case 'C' -> GTOBlocks.NAQUADAH_REINFORCED_PLANT_CASING.get();
            case 'E' -> GTOBlocks.HYPER_MECHANICAL_CASING.get();
            case 'F' -> GTOBlocks.NEUTRONIUM_STABLE_CASING.get();
            case 'G' -> GTBlocks.FUSION_COIL.get();
            case 'H' -> GTOBlocks.IRIDIUM_CASING.get();
            case 'I' -> GTOBlocks.FUSION_COIL_MK2.get();
            case 'J' -> GTOBlocks.AMPROSIUM_ACTIVE_CASING.get();
            case 'L' -> GTOBlocks.AMPROSIUM_PIPE_CASING.get();
            case 'M' -> GTOBlocks.CONTAINMENT_FIELD_GENERATOR.get();
            case 'N' -> GTOBlocks.IMPROVED_SUPERCONDUCTOR_COIL.get();
            case 'Q' -> GTOBlocks.PRESSURE_CONTAINMENT_CASING.get();
            case 'U' -> GTOBlocks.HYPER_CORE.get();
            case 'V' -> GTOBlocks.QUANTUM_GLASS.get();
            default -> null;
        };
        return block == null ? null : block.defaultBlockState();
    }

    @Override
    public double getDynamicCollisionShapeMargin(String partName) {
        return 0;
    }

    @Override
    public float getDynamicMotionValue(String partName, float partialTicks) {
        DynamicRotationState rotation = getRotation(partName);
        return rotation == null ? 0 : rotation.getValue(partialTicks);
    }

    @Override
    public float getDynamicReturnProgress(String partName, float partialTicks) {
        DynamicRotationState rotation = getRotation(partName);
        return rotation == null ? 0 : rotation.getReturnProgress(partialTicks);
    }

    @Override
    public Matrix4f getDynamicTransform(String partName, float partialTicks, Matrix4f result) {
        return getDynamicPart(partName).transform(result, getFrontFacing(), getDynamicMotionValue(partName, partialTicks),
                getDynamicReturnProgress(partName, partialTicks));
    }

    @Nullable
    private DynamicRotationState getRotation(String partName) {
        if (INNER_VERTICAL_RING.equals(partName)) return innerVerticalRotation;
        if (OUTER_VERTICAL_RING.equals(partName)) return outerVerticalRotation;
        if (HORIZONTAL_RING.equals(partName)) return horizontalRotation;
        return null;
    }

    private void tickRotations(boolean active) {
        innerVerticalRotation.tick(active, INNER_VERTICAL_SPEED);
        outerVerticalRotation.tick(active, OUTER_VERTICAL_SPEED);
        horizontalRotation.tick(active, HORIZONTAL_SPEED);
    }

    private boolean hasRotation() {
        return !innerVerticalRotation.isAtOrigin() || !outerVerticalRotation.isAtOrigin() || !horizontalRotation.isAtOrigin();
    }

    private static boolean isRing(String partName) {
        return INNER_VERTICAL_RING.equals(partName) || OUTER_VERTICAL_RING.equals(partName) || HORIZONTAL_RING.equals(partName);
    }

    @Override
    public boolean recipeTypeAvailable(GTRecipeType type) {
        return formedAmount > 0 || type == GTORecipeTypes.MACERATOR_RECIPES;
    }
}
