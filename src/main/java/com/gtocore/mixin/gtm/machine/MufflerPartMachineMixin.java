package com.gtocore.mixin.gtm.machine;

import com.gtocore.common.item.ItemMap;
import com.gtocore.common.machine.mana.multiblock.PulseMachineMaintenancePedestal;
import com.gtocore.common.pipe.muffler.IMufflerConduction;

import com.gtolib.api.GTOValues;
import com.gtolib.api.machine.feature.IAirScrubberInteractor;
import com.gtolib.api.machine.feature.IDroneInteractionMachine;
import com.gtolib.api.machine.feature.IGTOMufflerMachine;
import com.gtolib.api.machine.feature.multiblock.IDroneControlCenterMachine;
import com.gtolib.api.misc.Drone;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredPartMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.transfer.forge.MenuItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.common.data.GTParticleTypes;
import com.gregtechceu.gtceu.common.machine.electric.AirScrubberMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MufflerPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import earth.terrarium.adastra.api.planets.PlanetApi;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MufflerPartMachine.class)
public abstract class MufflerPartMachineMixin extends WorkableTieredPartMachine implements IGTOMufflerMachine, IDroneInteractionMachine, IAirScrubberInteractor, IMufflerConduction {

    @Unique
    @SaveToDisk(defaultValue = "false")
    private boolean gtocore$isWorkingEnabled;
    @Shadow(remap = false)
    @Final
    private KeyInventory<AEItemKey> inventory;
    @Shadow(remap = false)
    @Nullable
    protected TickableSubscription particleSubs;

    @OnlyIn(Dist.CLIENT)
    @Shadow(remap = false)
    protected abstract void particlesTick();

    @Unique
    private IDroneControlCenterMachine gtolib$cache;
    @Unique
    private AirScrubberMachine gtolib$airScrubberCache;
    @Unique
    private PulseMachineMaintenancePedestal gto$manaCenter;
    @Unique
    private int gto$chanceOfNotProduceAsh = 100;
    @Unique
    @SyncToClient
    private boolean gtolib$lastFrontFaceFree = true;
    @Unique
    @SyncToClient
    private BlockPos gtolib$pollutionPos;
    @Unique
    @SyncToClient
    private Direction gtolib$pollutionFacing;
    @Unique
    private long gtocore$refresh = 0;

    protected MufflerPartMachineMixin(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Unique
    public AirScrubberMachine getAirScrubberMachineCache() {
        return gtolib$airScrubberCache;
    }

    @Unique
    public void setAirScrubberMachineCache(AirScrubberMachine cache) {
        gtolib$airScrubberCache = cache;
    }

    @Unique
    public IDroneControlCenterMachine getNetMachineCache() {
        return gtolib$cache;
    }

    @Unique
    public void setNetMachineCache(IDroneControlCenterMachine cache) {
        gtolib$cache = cache;
        var oldManaCenter = gto$manaCenter;
        if (oldManaCenter != null) {
            oldManaCenter.removeProblem(this);
        }
        gto$manaCenter = cache instanceof PulseMachineMaintenancePedestal m ? m : null;
        if (gto$manaCenter != null) {
            gto$manaCenter.addProblem(this, this::gto$clear4dusts);
        }
    }

    @Unique
    private void gto$clear4dusts() {
        long remainingClears = 4;
        for (int i = 0; i < inventory.size(); i++) {
            long count = inventory.amountAt(i);
            if (count > 0) {
                long toClear = Math.min(count, remainingClears);
                inventory.extract(i, inventory.keyAt(i), toClear, false);
                remainingClears -= toClear;
                if (remainingClears <= 0) break;
            }
        }
    }

    @Unique
    private void gtolib$push_drone() {
        IDroneControlCenterMachine centerMachine = getNetMachine();
        if (centerMachine == null) return;

        var eu = inventory.size() << 4;
        Drone drone = getFirstUsableDrone(d -> d.getCharge() >= eu);
        if (drone == null || !drone.start(4, eu, GTOValues.REMOVING_ASH)) return;

        for (int i = 0; i < inventory.size(); i++) {
            long count = inventory.amountAt(i);
            if (count > 0) {
                var key = inventory.keyAt(i);
                inventory.set(i, null, 0);
                ((IRecipeHandlerHolder) centerMachine).output(key, count);
            }
        }
    }

    @Override
    public boolean hasOnWorkingMethod() {
        return true;
    }

    @Override
    public boolean hasModifyRecipeMethod() {
        return true;
    }

    @Override
    public boolean hasAfterWorkingMethod() {
        return false;
    }

    @Override
    public boolean hasBeforeWorkingMethod() {
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        gto$chanceOfNotProduceAsh = Math.clamp(gto$chanceOfNotProduceAsh, 0, getTier() * 10);
        if (isRemote()) {
            gto$subParticle();
        }
    }

    @Unique
    private void gto$subParticle() {
        particleSubs = subscribeClientTick(particleSubs, this::particlesTick);
    }

    @Unique
    private void gto$unsubParticle() {
        particleSubs = ITickSubscription.unsubscribe(particleSubs);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        gtolib$airScrubberCache = null;
        removeNetMachineCache();
        gto$unsubParticle();
    }

    @Override
    public boolean isWorkingEnabled() {
        return gtocore$isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        gtocore$isWorkingEnabled = isWorkingAllowed;
    }

    @Override
    public boolean isFrontFaceFree() {
        if (isRemote()) return gtolib$lastFrontFaceFree;
        var time = getOffsetTimer();
        if (time > gtocore$refresh) {
            boolean free = !PlanetApi.API.isSpace(getLevel());
            BlockPos pos = self().getPos();
            for (int i = 0; i < 3; i++) {
                pos = pos.relative(this.self().getFrontFacing());
                if (!self().getLevel().getBlockState(pos).isAir()) {
                    free = false;
                }
            }
            gtolib$pollutionPos = getPos();
            gtolib$pollutionFacing = getFrontFacing();
            if (!free) {
                var output = IMufflerConduction.getMufflerPipeNetOutput(this);
                if (output != null && output.shouldWorkAsMufflerSource()) {
                    free = true;
                    gtolib$pollutionPos = output.self().getPos();
                    gtolib$pollutionFacing = output.self().getFrontFacing();
                    requestSync();
                }
            }
            if (free != gtolib$lastFrontFaceFree) {
                gtolib$lastFrontFaceFree = free;
                requestSync();
            }
            gtocore$refresh = time + 100;
        }
        return gtolib$lastFrontFaceFree;
    }

    @Unique
    public boolean gtolib$checkAshFull() {
        int last = inventory.size() - 1;
        var key = inventory.keyAt(last);
        if (key == null) return false;
        return inventory.amountAt(last) == 64 || key.getItem() != ItemMap.ASH.getItem();
    }

    @Override
    public int gtolib$getRecoveryChance() {
        return gto$chanceOfNotProduceAsh;
    }

    @Override
    public void recoverItemsTable(ItemStack recoveryItems) {
        AirScrubberMachine machine = getAirScrubberMachine();
        var key = AEItemKey.of(recoveryItems);
        if (machine != null && GTValues.RNG.nextInt(machine.getTier() << 1 + 1) > 1) {
            if (key != null) machine.output(key, recoveryItems.getCount());
            return;
        }
        if (key != null) {
            var item = key.getItem();
            long amount = recoveryItems.getCount();
            for (int slot = 0; slot < inventory.size() && amount > 0; slot++) {
                long count = inventory.amountAt(slot);
                if (count < 64 && (count == 0 || ((AEItemKey) inventory.rawKeyAt(slot)).getItem() == item)) {
                    amount -= inventory.insert(slot, key, amount, false);
                }
            }
        }
        if (inventory.amountAt(inventory.size() - this.tier - 1) > 0) gtolib$push_drone();
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void gtolib$init(MetaMachineBlockEntity holder, int tier, CallbackInfo ci) {
        inventory.setOnChanged(() -> {
            for (var controller : getControllers()) {
                if (controller instanceof IRecipeLogicMachine recipeLogicMachine) {
                    recipeLogicMachine.getRecipeLogic().updateTickSubscription();
                }
            }
        });
        gtocore$isWorkingEnabled = false;
    }

    @Inject(method = "createUI", at = @At("RETURN"), remap = false, cancellable = true)
    private void gtolib$createUI(Player entityPlayer, CallbackInfoReturnable<ModularUI> cir) {
        ConfiguratorPanel configuratorPanel;
        var originUI = cir.getReturnValue();
        int rowSize = (int) Math.sqrt(inventory.size());
        int xOffset = Math.max(0, rowSize - 9) * 9;
        if (GTCEu.isDev() || rowSize > 9) {
            var modular = new ModularUI(176 + xOffset * 2, 18 + 18 * rowSize + 94, this, entityPlayer).background(GuiTextures.BACKGROUND).widget(new LabelWidget(10, 5, getBlockState().getBlock().getDescriptionId())).widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset, 18 + 18 * rowSize + 12, true));
            var slots = new MenuItemAdapter(inventory);
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    modular.widget(new SlotWidget(slots, index, (88 - rowSize * 9 + x * 18) + xOffset, 18 + y * 18, true, true).setBackgroundTexture(GuiTextures.SLOT));
                }
            }
            originUI = modular;
        }
        cir.setReturnValue(originUI.widget(configuratorPanel = new ConfiguratorPanel(-(24 + 2), originUI.getHeight())));
        attachConfigurators(configuratorPanel);
        configuratorPanel.setSelfPosition(new Position(-24 - 2, originUI.getHeight() - configuratorPanel.getSize().height - 4));
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return super.createMainPage(widget);
    }

    @Override
    public void gtolib$addMufflerEffect() {
        List<LivingEntity> entities = self().getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(self().getPos().relative(this.self().getFrontFacing())));
        entities.forEach(e -> {
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2));
            e.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 1));
        });
    }

    @Override
    public boolean firstTestMachine(IDroneControlCenterMachine machine) {
        Level level = machine.self().getLevel();
        if (level == null || !testUUID(machine)) return false;
        if (testMachine(machine) && machine.hasDrone(self().getPos(), d -> d.getCharge() > 0)) {
            return true;
        }
        return machine instanceof PulseMachineMaintenancePedestal p &&
                p.inRange(getPos());
    }

    @Override
    public boolean isMufflerSource() {
        return true;
    }

    @Override
    public void emitPollutionParticles() {
        if (PlanetApi.API.isSpace(getLevel())) return;

        var pos = gtolib$pollutionPos != null ? gtolib$pollutionPos : self().getPos();
        var facing = gtolib$pollutionFacing != null ? gtolib$pollutionFacing : self().getFrontFacing();

        var center = pos.getCenter();
        var offset = .75f;
        var xPos = (float) (center.x + facing.getStepX() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);
        var yPos = (float) (center.y + facing.getStepY() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);
        var zPos = (float) (center.z + facing.getStepZ() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);

        var ySpd = facing.getStepY() + (GTValues.RNG.nextFloat() - .15f) * .5f;
        var xSpd = facing.getStepX() + (GTValues.RNG.nextFloat() - .5f) * .5f;
        var zSpd = facing.getStepZ() + (GTValues.RNG.nextFloat() - .5f) * .5f;

        self().getLevel().addParticle(GTParticleTypes.MUFFLER_PARTICLE.get(),
                xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }
}
