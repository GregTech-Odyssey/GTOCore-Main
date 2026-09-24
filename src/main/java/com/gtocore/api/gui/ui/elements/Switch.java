package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.data.SyncValue;
import com.gtocore.api.gui.ui.styletemplate.OreSprites;
import com.gtocore.api.gui.ui.styletemplate.UISizes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.BooleanSupplier;

/**
 * Ore UI 开关，对应 LDLib2 {@code Switch}：开为绿色"I"、关为深色"O"（{@link OreSprites#SWITCH_ON}/{@link OreSprites#SWITCH_OFF}）。
 * <p>
 * 标准尺寸 {@link #WIDTH} × {@link #HEIGHT}，即贴图原尺寸。状态以服务端为准下发；点击在服务端取反后写入 {@code setter}。
 */
public final class Switch extends Button {

    public static final int WIDTH = UISizes.SWITCH_WIDTH;
    public static final int HEIGHT = UISizes.CONTROL_HEIGHT;

    private final SyncValue<Boolean> state;

    private Switch(BooleanSupplier getter, BooleanConsumer setter) {
        super(WIDTH, HEIGHT, null, null);
        this.state = addSyncValue(SyncValue.of(getter::getAsBoolean, SyncValue.BOOLEAN, getter.getAsBoolean()));
        setOnServerClick(() -> setter.accept(!getter.getAsBoolean()));
    }

    public static Switch of(BooleanSupplier getter, BooleanConsumer setter) {
        return new Switch(getter, setter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var sprite = state.getValue() ? OreSprites.SWITCH_ON : OreSprites.SWITCH_OFF;
        if (isMouseOverElement(mouseX, mouseY)) sprite = sprite.tinted(OreSprites.PRESSED_TINT);
        sprite.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
    }
}
