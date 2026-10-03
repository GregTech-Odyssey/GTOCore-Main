package com.gtocore.common.machine.multiblock.part.maintenance;

import com.gtocore.common.data.GTOMachines;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.heat.HeatHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import earth.terrarium.adastra.api.systems.GravityApi;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@DataGeneratorScanned
public class ModularHatchPartMachine extends ACMHatchPartMachine implements IModularMaintenance, IMachineModifyDrops {

    @RegisterLanguage(cn = "功能模块", en = "Function Modules")
    private static final String MODULES = "gtocore.machine.modular_maintenance.modules";

    @SaveToDisk
    private final NotifiableInventory<AEItemKey> temperatureModuleInv;
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> gravityModuleInv;
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> vacuumModuleInv;
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> cleanroomModuleInv;
    @SaveToDisk(defaultValue = "293")
    private int activeTemperature = 293;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int currentGravity = 0;
    @Getter
    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean vacuumMode = false;
    @Getter
    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean gravityMode = false;
    /// indicates whether player could set temperature
    /// notice that even if temperatureMode is false, the temperature could be set passively by other machines
    @Getter
    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean temperatureMode = false;

    @Getter
    @SaveToDisk
    private final HeatHandler heatContainer;

    public ModularHatchPartMachine(MetaMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId);

        temperatureModuleInv = NotifiableInventory.items(this, KeyInventory.items(1, 1, true), IO.NONE, IO.BOTH);
        temperatureModuleInv.setFilter(key -> key instanceof AEItemKey itemKey && itemKey.getItem() == Wrapper.TEMPERATURE_CHECK);
        temperatureModuleInv.addChangedListener(this::onConditionChange);

        gravityModuleInv = NotifiableInventory.items(this, KeyInventory.items(1, 1, true), IO.NONE, IO.BOTH);
        gravityModuleInv.setFilter(key -> key instanceof AEItemKey itemKey && itemKey.getItem() == Wrapper.GRAVITY_CHECK);
        gravityModuleInv.addChangedListener(this::onConditionChange);

        vacuumModuleInv = NotifiableInventory.items(this, KeyInventory.items(1, 1, true), IO.NONE, IO.BOTH);
        vacuumModuleInv.setFilter(key -> key instanceof AEItemKey itemKey && itemKey.getItem() == Wrapper.VACUUM_CHECK);
        vacuumModuleInv.addChangedListener(this::onConditionChange);

        cleanroomModuleInv = NotifiableInventory.items(this, KeyInventory.items(1, 1, true), IO.NONE, IO.BOTH);
        cleanroomModuleInv.setFilter(key -> key instanceof AEItemKey itemKey && Wrapper.CLEAN_CHECK.containsKey(itemKey.getItem()));
        cleanroomModuleInv.addChangedListener(this::onConditionChange);
        heatContainer = new HeatHandler(holder, MAX_TEMPERATURE, 4, 8, 0);
        heatContainer.setAllowExplosion(false);
        heatContainer.setSideIOCondition(s -> s == getFrontFacing());
        heatContainer.addChangedListener(() -> {
            if (temperatureMode) applyActiveTemperature();
            for (var c : getControllers()) {
                if (c instanceof IRecipeLogicMachine machine) machine.getRecipeLogic().updateTickSubscription();
            }
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        heatContainer.onLoad();
        if (temperatureMode && !isRemote()) applyActiveTemperature();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        heatContainer.onUnLoad();
    }

    @Override
    public int getCurrentGravity() {
        if (gravityMode) return currentGravity;
        var level = getLevel();
        return level == null ? MAX_GRAVITY : Mth.clamp(Math.round(GravityApi.API.getGravity(level) * MAX_GRAVITY), MIN_GRAVITY, MAX_GRAVITY);
    }

    @Override
    public int getVacuumTier() {
        return vacuumMode ? 4 : IModularMaintenance.super.getVacuumTier();
    }

    @Override
    public @NotNull MetaMachine self() {
        return this;
    }

    private static final int MIN_TEMPERATURE = 273;
    private static final int MAX_TEMPERATURE = 4800;
    private static final int MIN_GRAVITY = 0;
    private static final int MAX_GRAVITY = 100;

    @Override
    public @NotNull Widget createUIWidget() {
        var temperatureSlot = ItemSlot.of(temperatureModuleInv.storage, 0).setGhosts(Wrapper.TEMPERATURE_CHECK.getDefaultInstance());
        temperatureSlot.tooltips(Component.translatable(TOOLTIP_KEY, Wrapper.TEMPERATURE_CHECK.getDefaultInstance().getDisplayName(), Component.translatable(TEMPERATURE_FUNC)));
        var gravitySlot = ItemSlot.of(gravityModuleInv.storage, 0).setGhosts(Wrapper.GRAVITY_CHECK.getDefaultInstance());
        gravitySlot.tooltips(Component.translatable(TOOLTIP_KEY, Wrapper.GRAVITY_CHECK.getDefaultInstance().getDisplayName(), Component.translatable(GRAVITY_FUNC)));
        var vacuumSlot = ItemSlot.of(vacuumModuleInv.storage, 0).setGhosts(Wrapper.VACUUM_CHECK.getDefaultInstance());
        vacuumSlot.tooltips(Component.translatable(TOOLTIP_KEY, Wrapper.VACUUM_CHECK.getDefaultInstance().getDisplayName(), Component.translatable(VACUUM_TIER_4)));
        var cleanroomSlot = ItemSlot.of(cleanroomModuleInv.storage, 0).setGhosts(GTOMachines.CLEANING_CONFIGURATION_MAINTENANCE_HATCH.asStack(),
                GTOMachines.STERILE_CONFIGURATION_CLEANING_MAINTENANCE_HATCH.asStack(), GTOMachines.LAW_CONFIGURATION_CLEANING_MAINTENANCE_HATCH.asStack());
        cleanroomSlot.tooltips(Component.translatable(TOOLTIP_KEY_CLEANROOM));
        var controls = ControlPanel.of(this);
        controls.addSlots(MODULES, temperatureSlot, gravitySlot, vacuumSlot, cleanroomSlot);
        addMaintenanceControls(controls);
        return MachineDisplay.page(this, this::addDisplayText, null, ScrollerView.heightFor(6, UISizes.STATUS_LINE_HEIGHT, 0) + UISizes.TEXT_PADDING).addChild(controls.build());
    }

    @Override
    protected void addMaintenanceControls(ControlPanel controls) {
        super.addMaintenanceControls(controls);
        controls.addInt(TEMPERATURE_CONFIG, this::getActiveTemperature, this::setActiveTemperature, MIN_TEMPERATURE, MAX_TEMPERATURE)
                .disabled(() -> !temperatureMode, TEMPERATURE_REQUIRED);
        controls.addInt(GRAVITY_CONFIG, this::getCurrentGravity, this::setCurrentGravity, MIN_GRAVITY, MAX_GRAVITY)
                .disabled(() -> !gravityMode, GRAVITY_REQUIRED);
    }

    private void addDisplayText(List<Component> list) {
        list.add(getTextWidgetText(this::getDurationMultiplier));
        list.add(Component.translatable("gtocore.machine.current_temperature", FormattingUtil.formatNumber2Places(getHeatContainer().getTemperature())));
        if (!temperatureMode) {
            list.add(Component.translatable(TOOLTIP_REQUIRED_KEY, getDisplayName(TEMPERATURE_SHORT_NAME)));
        }
        list.add(Component.translatable("forge.entity_gravity").append(": %s".formatted(getCurrentGravity())));
        if (!gravityMode) {
            list.add(Component.translatable(TOOLTIP_REQUIRED_KEY, getDisplayName(GRAVITY_SHORT_NAME)));
        }
        list.add(Component.translatable("gtocore.recipe.vacuum.tier", getVacuumTier()));
        if (!vacuumMode) {
            list.add(Component.translatable(TOOLTIP_REQUIRED_KEY, getDisplayName(VACUUM_SHORT_NAME)));
        }
        list.add(Component.translatable(CURRENT_CLEANROOM));
        list.add(getCurrentCleanroom().withStyle(ChatFormatting.GREEN));
        if (cleanroomModuleInv.storage.amountAt(0) == 0) {
            list.add(Component.translatable(TOOLTIP_REQUIRED_KEY_CLEANROOM, getDisplayName(CLEANROOM_SHORT_NAME)));
        }
    }

    private static MutableComponent getDisplayName(String key) {
        return Component.literal("[")
                .append(Component.translatable(key))
                .append(Component.literal("]"));
    }

    private MutableComponent getCurrentCleanroom() {
        if (!getControllers().isEmpty() &&
                getController() instanceof ICleanroomReceiver receiver) {
            if (receiver.getCleanroom() != null) {
                List<MutableComponent> cleanroomTypes = receiver.getCleanroom().getTypes().stream()
                        .map(type -> Component.translatable(type.getTranslationKey()))
                        .toList();
                if (cleanroomTypes.isEmpty()) {
                    return Component.translatable(CLEANROOM_NOT_SET);
                }
                MutableComponent result = Component.empty();
                for (int i = 0; i < cleanroomTypes.size(); i++) {
                    result.append(cleanroomTypes.get(i));
                    if (i < cleanroomTypes.size() - 1) {
                        result.append(", ");
                    }
                }
                return result;

            } else {
                return Component.translatable(CLEANROOM_NOT_SET);
            }
        }
        return Component.translatable(CLEANROOM_NOT_APPLICABLE);
    }

    private int getActiveTemperature() {
        return activeTemperature;
    }

    private void setActiveTemperature(int activeTemperature) {
        this.activeTemperature = Mth.clamp(activeTemperature, MIN_TEMPERATURE, MAX_TEMPERATURE);
        if (temperatureMode) applyActiveTemperature();
    }

    private void applyActiveTemperature() {
        heatContainer.setCurrentHeat(Math.round(activeTemperature * heatContainer.getHeatCapacity()));
    }

    @Override
    public void onDrops(List<ItemStack> drops) {
        clearInventory(temperatureModuleInv);
        clearInventory(gravityModuleInv);
        clearInventory(vacuumModuleInv);
        clearInventory(cleanroomModuleInv);
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        onConditionChange();
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        if (controller instanceof ICleanroomReceiver receiver) {
            receiver.setCleanroom(null);
        }
    }

    private void onConditionChange() {
        temperatureMode = temperatureModuleInv.storage.amountAt(0) > 0;
        if (temperatureMode && !isRemote()) applyActiveTemperature();
        gravityMode = gravityModuleInv.storage.amountAt(0) > 0;
        vacuumMode = vacuumModuleInv.storage.amountAt(0) > 0;
        var cleanroomKey = cleanroomModuleInv.storage.keyAt(0);
        var cleanroom = cleanroomKey == null ? null : Wrapper.CLEAN_CHECK.get(cleanroomKey.getItem());
        if (getController() instanceof ICleanroomReceiver receiver && receiver.getCleanroom() != cleanroom) {
            receiver.setCleanroom(cleanroom);
        }
    }

    private void setCurrentGravity(int gravity) {
        currentGravity = Mth.clamp(gravity, MIN_GRAVITY, MAX_GRAVITY);
    }

    private static class Wrapper {

        private static final Item VACUUM_CHECK = GTOMachines.VACUUM_CONFIGURATION_HATCH.asItem();
        private static final Item GRAVITY_CHECK = GTOMachines.GRAVITY_CONFIGURATION_HATCH.asItem();
        private static final Item TEMPERATURE_CHECK = GTOMachines.ELECTRIC_HEATER.asItem();
        private static final Map<Item, ICleanroomProvider> CLEAN_CHECK = Map.of(
                GTOMachines.CLEANING_CONFIGURATION_MAINTENANCE_HATCH.asItem(), CMHatchPartMachine.DUMMY_CLEANROOM,
                GTOMachines.STERILE_CONFIGURATION_CLEANING_MAINTENANCE_HATCH.asItem(), CMHatchPartMachine.STERILE_DUMMY_CLEANROOM,
                GTOMachines.LAW_CONFIGURATION_CLEANING_MAINTENANCE_HATCH.asItem(), CMHatchPartMachine.LAW_DUMMY_CLEANROOM);
    }

    @RegisterLanguage(cn = "在槽位放入%s以启用%s功能", en = "Place %s in the corresponding slot to enable %s functionality")
    private static final String TOOLTIP_KEY = "gtocore.machine.modular_maintenance.tooltip";
    @RegisterLanguage(cn = "在槽位放入超净可配置维护仓以启用对应等级的超净环境", en = "Place a Cleanroom Configurable Maintenance Hatch in the slot to enable the corresponding level of cleanroom environment")
    private static final String TOOLTIP_KEY_CLEANROOM = "gtocore.machine.modular_maintenance.tooltip.cleanroom";
    @RegisterLanguage(cn = "放入%s以启用调节", en = "Insert %s\nto enable adjustment")
    private static final String TOOLTIP_REQUIRED_KEY = "gtocore.machine.modular_maintenance.required.tooltip";
    @RegisterLanguage(cn = "放入%s以启用超净环境", en = "Insert %s\nto enable cleanroom environment")
    private static final String TOOLTIP_REQUIRED_KEY_CLEANROOM = "gtocore.machine.modular_maintenance.required.tooltip.cleanroom";
    @RegisterLanguage(cn = "电力加热器", en = "Electric Heater")
    private static final String TEMPERATURE_SHORT_NAME = "gtocore.machine.modular_maintenance.temperature.short_name";
    @RegisterLanguage(cn = "可配置重力维护仓", en = "Gravity Configuration Hatch")
    private static final String GRAVITY_SHORT_NAME = "gtocore.machine.modular_maintenance.gravity.short_name";
    @RegisterLanguage(cn = "可配置真空维护仓", en = "Vacuum Configuration Hatch")
    private static final String VACUUM_SHORT_NAME = "gtocore.machine.modular_maintenance.vacuum.short_name";
    @RegisterLanguage(cn = "超净可配置维护仓", en = "Cleanroom Configuration Hatch")
    private static final String CLEANROOM_SHORT_NAME = "gtocore.machine.modular_maintenance.cleanroom.short_name";
    @RegisterLanguage(cn = "控制重力", en = "Control Gravity")
    private static final String GRAVITY_CONFIG = "gtocore.machine.modular_maintenance.gravity_config";
    @RegisterLanguage(cn = "调节温度（K）", en = "Adjust Temperature (K)")
    private static final String TEMPERATURE_CONFIG = "gtocore.machine.modular_maintenance.temperature_config";
    @RegisterLanguage(cn = "需要在槽位放入电力加热器", en = "Requires an Electric Heater in the slot")
    private static final String TEMPERATURE_REQUIRED = "gtocore.machine.modular_maintenance.temperature_required";
    @RegisterLanguage(cn = "需要在槽位放入可配置重力维护仓", en = "Requires a Gravity Configuration Hatch in the slot")
    private static final String GRAVITY_REQUIRED = "gtocore.machine.modular_maintenance.gravity_required";
    @RegisterLanguage(cn = "未设置超净环境", en = "Cleanroom Not Set")
    public static final String CLEANROOM_NOT_SET = "gtocore.machine.modular_maintenance.no_cleanroom";
    @RegisterLanguage(cn = "无控制器或不接受超净", en = "No Controller or Not Accepting Cleanroom")
    private static final String CLEANROOM_NOT_APPLICABLE = "gtocore.machine.modular_maintenance.no_controller";
    @RegisterLanguage(cn = "当前的超净环境：", en = "Current Cleanroom: ")
    public static final String CURRENT_CLEANROOM = "gtocore.machine.modular_maintenance.current_cleanroom";
    @RegisterLanguage(cn = "4级真空", en = "Tier 4 Vacuum")
    private static final String VACUUM_TIER_4 = "gtocore.machine.modular_maintenance.vacuum_tier_4";
    @RegisterLanguage(cn = "可控温度", en = "Controllable Temperature")
    private static final String TEMPERATURE_FUNC = "gtocore.machine.modular_maintenance.temperature_check";
    @RegisterLanguage(cn = "可控重力", en = "Controllable Gravity")
    private static final String GRAVITY_FUNC = "gtocore.machine.modular_maintenance.gravity_check";
}
