package com.gtocore.common.machine.multiblock.electric.space;

import com.gtocore.api.data.RocketFuels;
import com.gtocore.client.hud.HUDConfigurator;
import com.gtocore.common.data.GTOItems;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.FluidStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.V;

@DataGeneratorScanned
public final class SatelliteControlCenterMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder {

    @RegisterLanguage(en = "Selected planet: ", cn = "已选择的星球：")
    private static final String PLANET = "gtocore.satellite_control_center.planet";

    @RegisterLanguage(en = "The required rocket: ", cn = "需要的火箭：")
    private static final String ROCKET = "gtocore.satellite_control_center.rocket";

    @RegisterLanguage(en = "The required fuel: ", cn = "需要的燃料：")
    private static final String FUEL = "gtocore.satellite_control_center.fuel";

    @RegisterLanguage(cn = "建造空间站", en = "Build Space Station")
    private static final String BUILD_SPACE_STATION = "gtocore.satellite_control_center.emi.space_station";
    @RegisterLanguage(cn = "在该星球建造空间站时，", en = "When building a space station on this planet,")
    public static final String BUILD_SPACE_STATION_DESC_1 = "gtocore.satellite_control_center.emi.space_station.desc.1";
    @RegisterLanguage(cn = "需要将这些材料带入太空中。", en = "you need to bring these materials into space.")
    public static final String BUILD_SPACE_STATION_DESC_2 = "gtocore.satellite_control_center.emi.space_station.desc.2";

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
                c = new HUDConfigurator(GuiTextures.LIGHT_ON, GuiTextures.LIGHT_OFF));
        if (isRemote()) c.setHudInstance("adastra_hud");
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        var buttonText = Component.translatable(PLANET).append(Component.translatable(RocketFuels.PLANETS[index].getKey()));
        buttonText.append(" ");
        buttonText.append(ComponentPanelWidget.withButton(Component.literal("[-]"), "sub"));
        buttonText.append(" ");
        buttonText.append(ComponentPanelWidget.withButton(Component.literal("[+]"), "add"));
        textList.add(buttonText);
        textList.add(Component.translatable("ars_nouveau.tier", RocketFuels.PLANETS[index].getTier()));
        Item item = RocketFuels.ROCKET[RocketFuels.PLANETS[index].getTier() - 1];
        if (item != null) {
            textList.add(Component.translatable(ROCKET).append(item.getDescription()));
            textList.add(Component.translatable(FUEL).append(new FluidStack(RocketFuels.FUEL[RocketFuels.PLANETS[index].getTier() - 1], 16000).getDisplayName()));
            textList.add(ComponentPanelWidget.withButton(Component.translatable("gtocore.machine.space_elevator.set_out"), "set_out"));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (clickData.isRemote) return;
        if ("set_out".equals(componentData)) {
            launch = true;
            getRecipeLogic().updateTickSubscription();
        } else if (!isActive()) {
            index = Mth.clamp(index + (componentData.equals("add") ? 1 : -1), 0, RocketFuels.PLANETS.length - 1);
        }
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (launch && getTier() > GTValues.MV && getOwnerUUID() != null) {
            launch = false;
            Item item = RocketFuels.ROCKET[RocketFuels.PLANETS[index].getTier() - 1];
            if (item == null) return null;
            return getRecipeBuilder()
                    .inputItems(GTOItems.PLANET_SCAN_SATELLITE.asStack())
                    .inputFluids(new FluidStack(RocketFuels.FUEL[RocketFuels.PLANETS[index].getTier() - 1], 16000))
                    .inputItems(item)
                    .inputItems(GTOItems.PLANET_DATA_CHIP.asStack())
                    .outputItems(item)
                    .outputItems(GTOItems.PLANET_DATA_CHIP.get().getPlanetDataChip(getOwnerUUID(), RocketFuels.PLANETS[index].getLocation()))
                    .EUt(V[HV])
                    .duration(6000)
                    .build();
        } else if (getTier() <= GTValues.MV) {
            setIdleReason(IdleReason.VOLTAGE_TIER_NOT_SATISFIES);
        }
        return null;
    }
}
