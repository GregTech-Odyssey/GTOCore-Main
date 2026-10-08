package com.gtocore.api.gui;

import com.gtocore.common.cover.HeatInterfaceCover;

import com.gtolib.GTOCore;
import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.heat.HeatHandler;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.render.UIClip;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/** A UIPro window accessory; only open UIs sample and synchronize thermal telemetry. */
public final class HeatThermometer extends UIElement {

    public static final int WIDTH = 16;
    private static final int CAP_HEIGHT = 4;
    private static final int BULB_HEIGHT = 14;
    private static final ResourceLocation ATLAS = GTOCore.id("textures/gui/uipro/thermometer.png");
    private static final ResourceLocation SPECTRUM = GTOCore.id("textures/gui/uipro/thermometer_spectrum.png");

    private record Reading(boolean enabled, double temperature, double capacity, double transfer, double cooldown,
                           double ambient, double change, long heat, long maximum, long explosion) {

        private static final Reading HIDDEN = new Reading(false, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private static final StreamCodec<FriendlyByteBuf, Reading> CODEC = new StreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buffer, Reading value) {
            buffer.writeBoolean(value.enabled());
            if (!value.enabled()) return;
            buffer.writeDouble(value.temperature());
            buffer.writeDouble(value.capacity());
            buffer.writeDouble(value.transfer());
            buffer.writeDouble(value.cooldown());
            buffer.writeDouble(value.ambient());
            buffer.writeDouble(value.change());
            buffer.writeVarLong(value.heat());
            buffer.writeVarLong(value.maximum());
            buffer.writeVarLong(value.explosion());
        }

        @Override
        public Reading decode(FriendlyByteBuf buffer) {
            if (!buffer.readBoolean()) return Reading.HIDDEN;
            return new Reading(true, buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                    buffer.readDouble(), buffer.readDouble(), buffer.readVarLong(), buffer.readVarLong(), buffer.readVarLong());
        }
    };

    private final MachineWindow window;
    private final SyncValue<Reading> reading;
    private Reading sampled = Reading.HIDDEN;
    @Nullable
    private IHeatContainer sampledContainer;
    private int sampleTime;
    private double sampleTemperature;
    private double temperatureChange;
    @Nullable
    private Reading tooltipReading;
    private final ArrayList<Component> tooltip = new ArrayList<>(10);

    public HeatThermometer(MachineWindow window) {
        this.window = window;
        layout(l -> l.size(WIDTH, CAP_HEIGHT + BULB_HEIGHT).minHeight(24));
        setVisible(false);
        // Always attach the same widget on both sides, even when covers/thermal fields are not client-synchronized.
        reading = addSyncValue(SyncValue.of(this::sample, CODEC, Reading.HIDDEN).onChanged(value -> updateVisibility(value.enabled())));
    }

    private Reading sample() {
        var value = sampleReading();
        // SyncValue's initial write does not run onChanged on the server.
        updateVisibility(value.enabled());
        return value;
    }

    private void updateVisibility(boolean enabled) {
        if (isVisible() == enabled) return;
        setVisible(enabled);
        window.onContentResized(this);
    }

    private Reading sampleReading() {
        if (!(window.getCurrentPage() instanceof MetaMachine machine)) return hide();
        var heat = heatContainer(machine);
        if (heat == null || machine.getLevel() == null) return hide();
        double temperature = heat.getTemperature();
        if (!Double.isFinite(temperature)) temperature = 0;
        temperature = Math.max(0, temperature);
        int time = machine.getOffsetTimer();
        if (heat != sampledContainer || time < sampleTime) {
            sampledContainer = heat;
            sampleTime = time;
            sampleTemperature = temperature;
            temperatureChange = 0;
        } else if (time - sampleTime >= 20) {
            temperatureChange = (temperature - sampleTemperature) * 20 / (time - sampleTime);
            sampleTime = time;
            sampleTemperature = temperature;
        }
        double capacity = heat.getHeatCapacity(), transfer = heat.getBaseTransferRate(), cooldown = heat.getCooldownRate();
        double ambient = heat.getAmbientTemperature();
        long current = heat.getCurrentHeat(), maximum = heat.getMaxTemperature();
        // Solar multiblock controllers explicitly suppress HeatHandler.doExplosion().
        long explosion = heat instanceof HeatHandler h &&
                machine instanceof IExplosionMachine &&
                h.isAllowExplosion() ? Math.max(0, maximum) : 0;
        if (sampled.enabled() && sampled.temperature() == temperature && sampled.capacity() == capacity &&
                sampled.transfer() == transfer && sampled.cooldown() == cooldown && sampled.ambient() == ambient &&
                sampled.change() == temperatureChange && sampled.heat() == current && sampled.maximum() == maximum &&
                sampled.explosion() == explosion)
            return sampled;
        sampled = new Reading(true, temperature, capacity, transfer, cooldown, ambient, temperatureChange, current, maximum, explosion);
        return sampled;
    }

    private Reading hide() {
        sampledContainer = null;
        sampled = Reading.HIDDEN;
        return sampled;
    }

    @Nullable
    private static IHeatContainer heatContainer(MetaMachine machine) {
        if (machine instanceof IHeatContainerMachine thermal && thermal.testHeatCapability(null)) {
            return thermal.getHeatContainer();
        }
        // Cover capability lives on the holder; a machine's EMPTY container can mask it.
        var coverHeat = machine.getCoverContainer().getGTCapability(IHeatContainer.class, null);
        return coverHeat instanceof IHeatContainer heat ? heat : null;
    }

    private double scale(Reading value) {
        if (value.maximum() > 0) return value.maximum() * (value.explosion() > 0 ? 1.2 : 1.0);
        return Math.max(1000, value.temperature());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        var value = reading.getValue();
        if (!value.enabled()) return;
        int x = getPositionX(), y = getPositionY(), height = getSizeHeight();
        int shaftHeight = Math.max(1, height - CAP_HEIGHT - BULB_HEIGHT);
        int columnHeight = shaftHeight + 7;
        int columnTop = y + 3, columnBottom = columnTop + columnHeight;
        double scale = scale(value);
        int filled = (int) Math.ceil(columnHeight * Math.clamp(value.temperature() / scale, 0.0, 1.0));
        RenderSystem.enableBlend();
        drawShell(graphics, x, y, shaftHeight, 0);
        if (filled > 0) {
            UIClip.push(graphics, x + 5, columnBottom - filled, 6, filled);
            graphics.blit(SPECTRUM, x + 5, columnTop, 6, columnHeight, 0, 0, 6, 128, 8, 128);
            UIClip.pop(graphics);
            graphics.blit(ATLAS, x, y + CAP_HEIGHT + shaftHeight, WIDTH, BULB_HEIGHT, 0, 20, WIDTH, BULB_HEIGHT, 48, 34);
        }
        drawShell(graphics, x, y, shaftHeight, 16);
        if (value.explosion() > 0) {
            int marker = columnBottom - (int) Math.round(columnHeight * value.explosion() / scale);
            graphics.fill(x + 3, marker, x + 13, marker + 1, UITheme.STATUS_TEXT_ERROR);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawShell(GuiGraphics graphics, int x, int y, int shaftHeight, int u) {
        graphics.blit(ATLAS, x, y, WIDTH, CAP_HEIGHT, u, 0, WIDTH, CAP_HEIGHT, 48, 34);
        graphics.blit(ATLAS, x, y + CAP_HEIGHT, WIDTH, shaftHeight, u, CAP_HEIGHT, WIDTH, 1, 48, 34);
        graphics.blit(ATLAS, x, y + CAP_HEIGHT + shaftHeight, WIDTH, BULB_HEIGHT, u, 5, WIDTH, BULB_HEIGHT, 48, 34);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var value = reading.getValue();
        if (!value.enabled() || !isMouseOverElement(mouseX, mouseY)) return;
        if (tooltipReading != value) {
            tooltipReading = value;
            tooltip.clear();
            tooltip.add(Component.translatable("gtocore.machine.current_temperature", number(value.temperature())));
            tooltip.add(Component.translatable("gtocore.machine.thermometer.ambient", number(value.ambient())));
            tooltip.add(Component.translatable(HeatInterfaceCover.HEAT_CAPACITY, number(value.capacity())));
            tooltip.add(Component.translatable("gtocore.machine.thermometer.conductivity", number(value.transfer())));
            tooltip.add(Component.translatable("gtocore.machine.thermometer.cooldown", number(value.cooldown())));
            tooltip.add(Component.translatable("gtocore.machine.thermometer.change", number(value.change()))
                    .withStyle(value.change() > 0 ? ChatFormatting.RED : value.change() < 0 ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            tooltip.add(Component.translatable("gtocore.machine.thermometer.heat", FormattingUtil.formatNumbers(value.heat())));
            tooltip.add(Component.translatable("gtocore.machine.thermometer.scale", number(scale(value))));
            if (value.explosion() > 0) {
                tooltip.add(Component.translatable("gtocore.machine.thermometer.explosion", FormattingUtil.formatNumbers(value.explosion()))
                        .withStyle(ChatFormatting.RED));
            }
            setHoverTooltips(tooltip.toArray(Component[]::new));
        }
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    private static String number(double value) {
        return FormattingUtil.formatNumber2Places(value);
    }
}
