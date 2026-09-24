package com.gtocore.common.machine.multiblock.part.ae.widget;

import com.gtocore.utils.AdvMathExpParser;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringSized;

class AmountSetWidget extends Widget {

    static final int WIDTH = 80;
    static final int HEIGHT = 30;
    /// 浮层的高度层：高于格子里的物品（约 150）与数量文字（200）
    private static final int FLOAT_Z = 250;

    private int index = -1;
    @Getter
    private final TextFieldWidget amountText;
    private final ConfigWidget parentWidget;

    private static final Integer COLOR_DEFAULT = 0xFFFFFFFF;
    private static final Integer COLOR_ERROR = 0xFFDF0000;

    AmountSetWidget(int x, int y, ConfigWidget widget) {
        super(x, y, WIDTH, HEIGHT);
        this.parentWidget = widget;
        // 面板浮在配置网格上：面板和输入框都抬到 FLOAT_Z，盖住下面格子里的物品与数量（物品约 150、数量 200）
        this.amountText = (TextFieldWidget) new TextFieldWidget(x + 8, y + 12, 60, 13, this::getAmountStr, this::setNewAmount) {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, FLOAT_Z + 1);
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                graphics.pose().popPose();
            }
        }
                .setValidator(this::amountTextValidator)
                .setMaxStringLength(24) // Long.MAX_VALUE 19 digits
                .appendHoverTooltips(Component.translatable("gtocore.gui.widget.amount_set.hover_tooltip"));
    }

    @OnlyIn(Dist.CLIENT)
    public void setSlotIndexClient(int slotIndex) {
        this.index = slotIndex;
        writeClientAction(0, buf -> buf.writeVarInt(this.index));
    }

    public void setSlotIndex(int slotIndex) {
        this.index = slotIndex;
    }

    private String getAmountStr() {
        if (this.index < 0) {
            return "0";
        }
        IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
        if (slot.getConfig() != null) {
            if (this.amountText.getCurrentString().isEmpty() || this.amountText.getCurrentString().equals("0")) {
                return String.valueOf(slot.getConfig().amount());
            }
            return this.amountText.getCurrentString();
        }
        return "0";
    }

    // We have two parses here, but it's acceptable for better UX. <= 1μs
    // Otherwise, we cannot support complex expressions as it updates char by char.
    private void setNewAmount(String amount) {
        try {
            if (this.index < 0) {
                return;
            }

            long newAmount = 0;
            if (!amount.isEmpty()) {
                newAmount = AdvMathExpParser.parse(amount).longValue();
            }

            IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
            if (newAmount > 0 && slot.getConfig() != null) {
                slot.setConfig(new GenericStack(slot.getConfig().what(), newAmount));
            }
        } catch (IllegalArgumentException | ArithmeticException ignore) {}
    }

    private String amountTextValidator(String text) {
        try {
            long value = AdvMathExpParser.parse(text).longValue();
            if (value < 0) {
                throw new IllegalArgumentException("Amount cannot be negative");
            }
            this.amountText.setTextColor(COLOR_DEFAULT);
        } catch (IllegalArgumentException | ArithmeticException e) {
            this.amountText.setTextColor(COLOR_ERROR);
        }
        return text;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 0) {
            this.amountText.setCurrentString("");
            this.index = buffer.readVarInt();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, FLOAT_Z);
        UITheme.WINDOW.draw(graphics, mouseX, mouseY, position.x, position.y, WIDTH, HEIGHT);
        drawStringSized(graphics, I18n.get("ldlib.gui.editor.configurator.amount"), position.x + 25, position.y + 3, UITheme.TEXT, false, 1.0F, false);
        graphics.pose().popPose();
    }
}
