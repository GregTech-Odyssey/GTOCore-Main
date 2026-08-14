package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.api.machine.dynamic.DynamicBlockPattern;
import com.gtocore.api.machine.dynamic.DynamicCollisionManager;
import com.gtocore.api.machine.dynamic.IDynamicStructureMachine;
import com.gtocore.api.machine.dynamic.RotationMotion;
import com.gtocore.api.pattern.GTOFactoryBlockPattern;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.client.DynamicVisualManager;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.multiblock.NoEnergyMultiblockMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gtocore.common.block.BlockMap.GRAVITONFLOWMAP;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class GodForgeMachine extends NoEnergyMultiblockMachine implements ITierCasingMachine, ICustomRecipeLogicHolder, IDynamicStructureMachine {

    public static final String OUTER_RING = "outer_ring";
    private static final double DYNAMIC_RENDER_DISTANCE = 256;
    private static final double DYNAMIC_RESTART_DISTANCE = 240;

    @SyncToClient
    @SaveToDisk(defaultValue = "0")
    public float color;
    private boolean isRemoved = false;
    private boolean collisionRemoved = false;
    @Getter
    private boolean dynamicRenderEnabled = true;
    public long rotation;
    public int timer;
    @SyncToClient
    @SaveToDisk(defaultValue = "0")
    public int tier;

    private TickableSubscription rotationSubscription;
    private DynamicBlockPattern dynamicPattern;

    private final TierCasingTrait tierCasingTrait;

    public GodForgeMachine(MetaMachineBlockEntity holder) {
        super(holder);
        tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.GRAVITON_FLOW_TIER);
    }

    @Override
    public Reference2IntMap<TierDataKey> getCasingTiers() {
        return tierCasingTrait.getCasingTiers();
    }

    @Override
    public void onStructureFormedClient() {
        super.onStructureFormedClient();
        dynamicRenderEnabled = true;
        DynamicVisualManager.register(this);
        DynamicCollisionManager.register(this);
        rotationSubscription = subscribeClientTick(rotationSubscription, this::rotation);
    }

    private void rotation() {
        boolean active = isActive();
        if (isRemote()) active = updateDynamicRenderState();
        boolean keepStarting = timer > rotation && (!isRemote() || !isActive());
        if (active || keepStarting) {
            if (isRemote() && !isRemoved) {
                if (!removeBlockFromWorld()) return;
                isRemoved = true;
            }
            this.rotation++;
            this.timer = 20;
            if (!isRemote() && !collisionRemoved) {
                updateCollision(true);
                collisionRemoved = true;
            }
        } else {
            this.timer = 0;
            if (this.rotation > 0) {
                this.rotation %= 180;
                this.rotation = Math.max(0, this.rotation - getDynamicReturnSpeed());
            }
            if (this.rotation == 0) {
                if (isRemote()) {
                    if (isRemoved && addBlockToWorld()) isRemoved = false;
                } else if (collisionRemoved) {
                    updateCollision(false);
                    collisionRemoved = false;
                }
            }
        }
    }

    private boolean updateDynamicRenderState() {
        if (!isActive()) {
            dynamicRenderEnabled = false;
        } else if (dynamicRenderEnabled) {
            dynamicRenderEnabled = DynamicVisualManager.isInRange(getPos(), DYNAMIC_RENDER_DISTANCE);
        } else {
            dynamicRenderEnabled = DynamicVisualManager.isInRange(getPos(), DYNAMIC_RESTART_DISTANCE);
        }
        return dynamicRenderEnabled;
    }

    public int getDynamicReturnSpeed() {
        return isRemote() && isActive() && !dynamicRenderEnabled ? 4 : 1;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        color = 1 - 0.1F * getCasingTier(GTORecipeDataKeys.GRAVITON_FLOW_TIER);
        tier = getCasingTier(GTORecipeDataKeys.GRAVITON_FLOW_TIER);
        if (!isRemote()) {
            DynamicCollisionManager.register(this);
            rotationSubscription = subscribeServerTick(rotationSubscription, this::rotation);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        DynamicCollisionManager.unregister(this);
        clearCollision();
        unsubscribeRotation();
        rotation = 0;
        timer = 0;
    }

    @Override
    public void onStructureInvalidClient() {
        super.onStructureInvalidClient();
        DynamicVisualManager.unregister(this);
        DynamicCollisionManager.unregister(this);
        unsubscribeRotation();
        if (isRemoved && addBlockToWorld()) isRemoved = false;
        dynamicRenderEnabled = false;
        rotation = 0;
        timer = 0;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        DynamicCollisionManager.unregister(this);
        if (isRemote()) {
            DynamicVisualManager.unregister(this);
            if (isRemoved && addBlockToWorld()) isRemoved = false;
            dynamicRenderEnabled = false;
        } else {
            clearCollision();
        }
        unsubscribeRotation();
        rotation = 0;
        timer = 0;
    }

    private void unsubscribeRotation() {
        if (rotationSubscription == null) return;
        rotationSubscription.unsubscribe();
        rotationSubscription = null;
    }

    private void clearCollision() {
        if (!collisionRemoved) return;
        updateCollision(false);
        collisionRemoved = false;
    }

    private void updateCollision(boolean hide) {
        if (hide) DynamicCollisionManager.hidePart(this, OUTER_RING);
        else DynamicCollisionManager.showPart(this, OUTER_RING);
    }

    @Override
    public DynamicBlockPattern getDynamicPattern() {
        if (dynamicPattern != null) return dynamicPattern;
        BlockPattern pattern = getDefinition().getPatternFactory()[0].get();
        if (pattern instanceof DynamicBlockPattern result) return dynamicPattern = result;
        throw new IllegalStateException("God forge pattern has no dynamic parts");
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
    public boolean isDynamicPartVisible(String partName) {
        return OUTER_RING.equals(partName) && isFormed() && rotation > 0;
    }

    @Nullable
    private Block getDynamicBlock(char letter) {
        return switch (letter) {
            case 'B' -> GTOBlocks.SINGULARITY_REINFORCED_STELLAR_SHIELDING_CASING.get();
            case 'C' -> GTOBlocks.CELESTIAL_MATTER_GUIDANCE_CASING.get();
            case 'D' -> GTOBlocks.BOUNDLESS_GRAVITATIONALLY_SEVERED_STRUCTURE_CASING.get();
            case 'E' -> GTOBlocks.TRANSCENDENTALLY_AMPLIFIED_MAGNETIC_CONFINEMENT_CASING.get();
            case 'F' -> GTOBlocks.STELLAR_ENERGY_SIPHON_CASING.get();
            case 'G', '1' -> switch (tier) {
                case 2 -> GTOBlocks.MEDIAL_GRAVITON_FLOW_MODULATOR.get();
                case 3 -> GTOBlocks.CENTRAL_GRAVITON_FLOW_MODULATOR.get();
                default -> GTOBlocks.REMOTE_GRAVITON_FLOW_MODULATOR.get();
            };
            case '2' -> GTOBlocks.MEDIAL_GRAVITON_FLOW_MODULATOR.get();
            case '3' -> GTOBlocks.CENTRAL_GRAVITON_FLOW_MODULATOR.get();
            case 'H' -> GTOBlocks.SPATIALLY_TRANSCENDENT_GRAVITATIONAL_LENS_BLOCK.get();
            default -> null;
        };
    }

    @Override
    @Nullable
    public BlockState getDynamicBlockState(String partName, char symbol) {
        if (!OUTER_RING.equals(partName)) return null;
        Block block = getDynamicBlock(symbol);
        return block == null ? null : block.defaultBlockState();
    }

    @Override
    public float getDynamicMotionValue(String partName, float partialTicks) {
        if (!OUTER_RING.equals(partName)) return 0;
        float angle = dynamicRenderEnabled || (!isActive() && timer > rotation) ? rotation + partialTicks : rotation - partialTicks * getDynamicReturnSpeed();
        return angle % 360;
    }

    private boolean removeBlockFromWorld() {
        return DynamicVisualManager.hidePart(this, OUTER_RING);
    }

    private boolean addBlockToWorld() {
        DynamicVisualManager.showPart(this, OUTER_RING);
        return true;
    }

    @Override
    public Supplier<BlockPattern>[] getPattern() {
        return new Supplier[] { () -> getBlockPattern(getDefinition()) };
    }

    public static BlockPattern getBlockPattern(MultiblockMachineDefinition definition) {
        return GTOFactoryBlockPattern.fromFile(definition)
                .where('~', Predicates.controller(definition))
                .where(' ', Predicates.any())
                .where('A', Predicates.blocks(GTOBlocks.TRANSCENDENTALLY_AMPLIFIED_MAGNETIC_CONFINEMENT_CASING.get()).or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1)))
                .where('B', Predicates.blocks(GTOBlocks.SINGULARITY_REINFORCED_STELLAR_SHIELDING_CASING.get()))
                .where('C', Predicates.blocks(GTOBlocks.CELESTIAL_MATTER_GUIDANCE_CASING.get()))
                .where('D', Predicates.blocks(GTOBlocks.BOUNDLESS_GRAVITATIONALLY_SEVERED_STRUCTURE_CASING.get()))
                .where('E', Predicates.blocks(GTOBlocks.TRANSCENDENTALLY_AMPLIFIED_MAGNETIC_CONFINEMENT_CASING.get()))
                .where('F', Predicates.blocks(GTOBlocks.STELLAR_ENERGY_SIPHON_CASING.get()))
                .where('G', GTOPredicates.tierBlock(GRAVITONFLOWMAP, GTORecipeDataKeys.GRAVITON_FLOW_TIER))
                .where('H', Predicates.blocks(GTOBlocks.SPATIALLY_TRANSCENDENT_GRAVITATIONAL_LENS_BLOCK.get()))
                .dynamicPart(OUTER_RING, part -> part
                        .selectAisles(0, 127)
                        .pivot(121.5F, .5F, .5F)
                        .motion(RotationMotion.aroundX()))
                .build();
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        return getRecipeBuilder().inputFluids(Fluids.WATER, 100).duration(20).build();
    }
}
