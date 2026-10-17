package com.gtocore.client.hud;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.uipro.data.ClientOnly;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.gto.registrate.util.nullness.NonNullBiConsumer.noop;

public class HUDConfigurator extends IFancyConfiguratorButton.Toggle {

    private final ClientOnly<IMoveableHUD> hud = ClientOnly.empty();
    private final IGuiTexture on;
    private final IGuiTexture off;
    @Getter
    @Setter
    private boolean isConfigurationMode = false;

    public HUDConfigurator(IGuiTexture on, IGuiTexture off) {
        super(on, off, () -> false, noop());
        this.on = on;
        this.off = off;
        setTooltipsSupplier(b -> List.of(
                Component.translatable(isEnabled() ? IMoveableHUD.HUD_TOGGLE_ON : IMoveableHUD.HUD_TOGGLE_OFF),
                Component.translatable(IMoveableHUD.HUD_DRAG)));
    }

    @Override
    public IGuiTexture getIcon() {
        return isEnabled() ? on : off;
    }

    @OnlyIn(Dist.CLIENT)
    public void setHudInstance(String hudID) {
        hud.set(IMoveableHUD.REGISTERED_HUDS.get(hudID));
    }

    @Override
    public void onClick(ClickData clickData) {
        if (clickData.isRemote && hud.isPresent()) clientClick(clickData);
    }

    @Override
    public boolean isLatched() {
        return isEnabled();
    }

    private boolean isEnabled() {
        return hud.isPresent() && clientEnabled();
    }

    @OnlyIn(Dist.CLIENT)
    private boolean clientEnabled() {
        return hud.get().isEnabled();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientClick(ClickData clickData) {
        var instance = hud.get();
        if (clickData.button == 1) {
            if (!IMoveableHUD.addActiveHud(instance)) IMoveableHUD.removeActiveHud(instance);
            return;
        }
        instance.toggleEnabled();
    }
}
