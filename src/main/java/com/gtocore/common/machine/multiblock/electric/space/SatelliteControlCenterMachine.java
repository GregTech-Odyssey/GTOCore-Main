package com.gtocore.common.machine.multiblock.electric.space;

import com.gtocore.api.data.RocketFuels;
import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.client.hud.HUDConfigurator;
import com.gtocore.common.data.GTOItems;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.Dimension;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Stepper;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.V;

@DataGeneratorScanned
public final class SatelliteControlCenterMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder {

    @RegisterLanguage(en = "Target planet", cn = "目标星球")
    private static final String TARGET = "gtocore.satellite_control_center.target";

    @RegisterLanguage(en = "Rocket", cn = "所需火箭")
    private static final String ROCKET = "gtocore.satellite_control_center.rocket";

    @RegisterLanguage(en = "Fuel", cn = "所需燃料")
    private static final String FUEL = "gtocore.satellite_control_center.fuel";

    @RegisterLanguage(en = "Status", cn = "状态")
    private static final String STATE = "gtocore.satellite_control_center.state";

    @RegisterLanguage(en = "Standby", cn = "待命")
    private static final String STATE_IDLE = "gtocore.satellite_control_center.state.idle";

    @RegisterLanguage(en = "Launching", cn = "发射中")
    private static final String STATE_WORKING = "gtocore.satellite_control_center.state.working";

    @RegisterLanguage(en = "Structure not formed", cn = "结构未成形")
    private static final String STATE_UNFORMED = "gtocore.satellite_control_center.state.unformed";

    @RegisterLanguage(en = "Insufficient voltage", cn = "电压不足")
    private static final String STATE_LOW_VOLTAGE = "gtocore.satellite_control_center.state.low_voltage";

    @RegisterLanguage(en = "Requires an HV or higher energy hatch", cn = "需要 HV 及以上等级的能源仓")
    private static final String LOW_VOLTAGE_DETAIL = "gtocore.satellite_control_center.low_voltage";

    @RegisterLanguage(en = "No owner", cn = "未绑定所有者")
    private static final String STATE_NO_OWNER = "gtocore.satellite_control_center.state.no_owner";

    @RegisterLanguage(en = "Launch", cn = "发射")
    private static final String LAUNCH = "gtocore.satellite_control_center.launch";

    @RegisterLanguage(en = "Launch in progress", cn = "发射进行中")
    private static final String LAUNCH_IN_PROGRESS = "gtocore.satellite_control_center.launch_in_progress";

    @RegisterLanguage(cn = "建造空间站", en = "Build Space Station")
    private static final String BUILD_SPACE_STATION = "gtocore.satellite_control_center.emi.space_station";
    @RegisterLanguage(cn = "在该星球建造空间站时，", en = "When building a space station on this planet,")
    public static final String BUILD_SPACE_STATION_DESC_1 = "gtocore.satellite_control_center.emi.space_station.desc.1";
    @RegisterLanguage(cn = "需要将这些材料带入太空中。", en = "you need to bring these materials into space.")
    public static final String BUILD_SPACE_STATION_DESC_2 = "gtocore.satellite_control_center.emi.space_station.desc.2";

    private static final int FUEL_AMOUNT = 16000;
    private static final int STEPPER_VALUE_WIDTH = UISizes.CONTENT_WIDTH - UISizes.BUTTON_WIDTH - UISizes.GAP - Stepper.width(0);
    private static final Map<Item, ItemStack> ROCKET_ICONS = new IdentityHashMap<>();

    private boolean launch;

    @SaveToDisk(defaultValue = "0")
    private int index;

    public SatelliteControlCenterMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        HUDConfigurator c;
        configuratorPanel.attachConfigurators(
                c = new HUDConfigurator(GTOGuiTextures.HUD_ON, GTOGuiTextures.HUD_OFF));
        if (isRemote()) c.setHudInstance("adastra_hud");
    }

    @Override
    public Widget createUIWidget() {
        var status = new StatusPanel();
        status.addLine(ROCKET, () -> rocket().getDescription())
                .icon(() -> ROCKET_ICONS.computeIfAbsent(rocket(), ItemStack::new));
        status.addLine(FUEL, () -> new FluidStack(fuel(), FUEL_AMOUNT).getDisplayName().copy()
                .append(" " + FormattingUtil.formatNumbers(FUEL_AMOUNT) + " mB"));
        status.addLine(STATE, () -> Component.translatable(stateKey()))
                .level(this::stateLevel)
                .detail(() -> isFormed() && !isActive() && getTier() <= GTValues.MV ? Component.translatable(LOW_VOLTAGE_DETAIL) : Component.empty());

        var targets = RocketFuels.SATELLITE_TARGETS;
        var stepper = new Stepper(STEPPER_VALUE_WIDTH, this::targetPosition, this::selectTarget, 0, targets.length - 1, false,
                i -> Component.translatable(targets[i].getKey()).getString());
        stepper.setHoverTooltips(Component.translatable(TARGET));
        var launchButton = Button.translatable(UISizes.BUTTON_WIDTH, LAUNCH)
                .setVariant(UITheme.ButtonVariant.CONFIRM)
                .setOnServerClick(this::requestLaunch)
                .disabled(() -> getTier() <= GTValues.MV, LOW_VOLTAGE_DETAIL);
        var controls = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(stepper, launchButton)
                .disabled(this::isActive, LAUNCH_IN_PROGRESS);

        return ((UIElement) super.createUIWidget()).addChildren(status,
                UIElement.column(LayoutStyle.AUTO).addChild(controls).disabled(() -> !isFormed(), STATE_UNFORMED));
    }

    private Dimension target() {
        if (index >= 0 && index < RocketFuels.PLANETS.length) {
            var planet = RocketFuels.PLANETS[index];
            if (RocketFuels.getRocket(planet.getTier()) != null) return planet;
        }
        return RocketFuels.SATELLITE_TARGETS[0];
    }

    private Item rocket() {
        return RocketFuels.getRocket(target().getTier());
    }

    private Fluid fuel() {
        return RocketFuels.getFuel(target().getTier());
    }

    private int targetPosition() {
        var planet = target();
        var targets = RocketFuels.SATELLITE_TARGETS;
        for (int i = 0; i < targets.length; i++) {
            if (targets[i] == planet) return i;
        }
        return 0;
    }

    private void selectTarget(int position) {
        var targets = RocketFuels.SATELLITE_TARGETS;
        if (isActive() || position < 0 || position >= targets.length) return;
        var planets = RocketFuels.PLANETS;
        for (int i = 0; i < planets.length; i++) {
            if (planets[i] == targets[position]) {
                if (i != index) {
                    index = i;
                    onChanged();
                }
                return;
            }
        }
    }

    private void requestLaunch() {
        if (!isFormed() || isActive() || getTier() <= GTValues.MV) return;
        launch = true;
        getRecipeLogic().updateTickSubscription();
    }

    private String stateKey() {
        if (!isFormed()) return STATE_UNFORMED;
        if (isActive()) return STATE_WORKING;
        if (getTier() <= GTValues.MV) return STATE_LOW_VOLTAGE;
        if (getOwnerUUID() == null) return STATE_NO_OWNER;
        return STATE_IDLE;
    }

    private StatusLine.Level stateLevel() {
        return switch (stateKey()) {
            case STATE_WORKING -> StatusLine.Level.GOOD;
            case STATE_IDLE -> StatusLine.Level.NORMAL;
            default -> StatusLine.Level.ERROR;
        };
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (launch && getTier() > GTValues.MV && getOwnerUUID() != null) {
            launch = false;
            var planet = target();
            var rocket = RocketFuels.getRocket(planet.getTier());
            var fuel = RocketFuels.getFuel(planet.getTier());
            if (rocket == null || fuel == null) return null;
            return getRecipeBuilder()
                    .inputItems(GTOItems.PLANET_SCAN_SATELLITE.asStack())
                    .inputFluids(new FluidStack(fuel, FUEL_AMOUNT))
                    .inputItems(rocket)
                    .inputItems(GTOItems.PLANET_DATA_CHIP.asStack())
                    .outputItems(rocket)
                    .outputItems(GTOItems.PLANET_DATA_CHIP.get().getPlanetDataChip(getOwnerUUID(), planet.getLocation()))
                    .EUt(V[HV])
                    .duration(6000)
                    .build();
        } else if (getTier() <= GTValues.MV) {
            setIdleReason(IdleReason.VOLTAGE_TIER_NOT_SATISFIES);
        }
        return null;
    }
}
