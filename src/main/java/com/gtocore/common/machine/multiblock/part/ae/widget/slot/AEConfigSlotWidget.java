package com.gtocore.common.machine.multiblock.part.ae.widget.slot;

import com.gtocore.common.machine.multiblock.part.ae.widget.ConfigWidget;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.uipro.ElementState;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;

import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget.drawSelectionOverlay;

@DataGeneratorScanned
public class AEConfigSlotWidget extends Widget implements IIngredientSlot {

    @RegisterLanguage(cn = "配置由 ME 自动拉取管理", en = "The configuration is managed by ME auto-pull")
    private static final String CONFIG_MANAGED = "gtocore.gui.ae_config_slot.config_managed";
    @RegisterLanguage(cn = "库存由 ME 网络自动补充，不能直接取放", en = "The stock is filled from the ME network and cannot be taken or placed directly")
    private static final String STOCK_MANAGED = "gtocore.gui.ae_config_slot.stock_managed";

    final ConfigWidget parentWidget;
    final int index;
    static final int REMOVE_ID = 1000;
    static final int UPDATE_ID = 1001;
    static final int AMOUNT_CHANGE_ID = 1002;
    static final int SLOT_CLICK_ID = 1003;
    static final int SLOT_DROP_ID = 1004;
    @Setter
    boolean select = false;

    AEConfigSlotWidget(Position pos, Size size, ConfigWidget widget, int index) {
        super(pos, size);
        this.parentWidget = widget;
        this.index = index;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        boolean overConfig = mouseOverConfig(mouseX, mouseY);
        boolean overStock = !overConfig && mouseOverStock(mouseX, mouseY);
        if (!overConfig && !overStock) return;
        List<Component> lines = new ArrayList<>();
        GenericStack stack = overConfig ? slot.getConfig() : slot.getStock();
        if (stack != null) {
            lines.addAll(Screen.getTooltipFromItem(Minecraft.getInstance(), GenericStack.wrapInItemStack(stack)));
        } else if (overConfig) {
            lines.add(Component.translatable("gtceu.gui.config_slot"));
            if (!isConfigDisabled()) {
                if (!parentWidget.isStocking()) {
                    lines.add(Component.translatable("gtceu.gui.config_slot.set"));
                    lines.add(Component.translatable("gtceu.gui.config_slot.scroll"));
                } else {
                    lines.add(Component.translatable("gtceu.gui.config_slot.set_only"));
                }
                lines.add(Component.translatable("gtceu.gui.config_slot.remove"));
            }
        }
        // 禁用格（画了斜纹）：与标准控件一样先"禁止操作"、再原因
        if (overConfig && isConfigDisabled()) ElementState.appendDisabledLines(lines, Component.translatable(CONFIG_MANAGED));
        if (overStock && isStockDisabled()) ElementState.appendDisabledLines(lines, Component.translatable(STOCK_MANAGED));
        setHoverTooltips(lines);
    }

    /// 上格（配置）禁用：自动拉取时配置由机器管理，点不了
    boolean isConfigDisabled() {
        return parentWidget.isAutoPull();
    }

    /// 下格（库存）禁用：库存模式下存货来自网络，点不了
    boolean isStockDisabled() {
        return parentWidget.isStocking();
    }

    /// 上格可从 EMI 拖入（LDLib2 {@code xeiPhantom}）：未禁用时
    boolean isXeiPhantom() {
        return !isConfigDisabled();
    }

    /// 服务端再判一次禁用：禁用的格子不接受设置、清除、改数量（上格）或取放（下格）请求，客户端可以伪造
    boolean rejectsDisabledAction(int id) {
        if (id == REMOVE_ID || id == UPDATE_ID || id == AMOUNT_CHANGE_ID) return isConfigDisabled();
        if (id == SLOT_CLICK_ID || id == SLOT_DROP_ID) return isStockDisabled();
        return false;
    }

    /**
     * 上下两格的底图：{@code slot} 是物品槽或流体槽的标准底图；上格可从 EMI 拖入时画下箭头标记（{@link UITheme#drawXeiPhantom}）。
     * 点不了的格子不压暗，保持原色、叠统一的禁用斜纹（{@link UITheme#drawDisabled}）。
     */
    @OnlyIn(Dist.CLIENT)
    void drawSlots(GuiGraphics graphics, int mouseX, int mouseY, IGuiTexture slot, boolean darkSlot) {
        Position position = getPosition();
        slot.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        if (isXeiPhantom()) UITheme.drawXeiPhantom(graphics, position.x, position.y, 18, 18, darkSlot);
        slot.draw(graphics, mouseX, mouseY, position.x, position.y + 18, 18, 18);
        if (this.select) UITheme.drawSelection(graphics, position.x, position.y, 18, 18);
    }

    /** 内容画完后：只读格叠斜纹；可操作的格子悬停时高亮。 */
    @OnlyIn(Dist.CLIENT)
    void drawStates(GuiGraphics graphics, int mouseX, int mouseY) {
        Position position = getPosition();
        if (isConfigDisabled()) UITheme.drawDisabled(graphics, position.x, position.y, 18, 18);
        if (isStockDisabled()) UITheme.drawDisabled(graphics, position.x, position.y + 18, 18, 18);
        if (mouseOverConfig(mouseX, mouseY) && !isConfigDisabled()) {
            drawSelectionOverlay(graphics, position.x + 1, position.y + 1, 16, 16);
        } else if (mouseOverStock(mouseX, mouseY) && !isStockDisabled()) {
            drawSelectionOverlay(graphics, position.x + 1, position.y + 19, 16, 16);
        }
    }

    boolean mouseOverConfig(double mouseX, double mouseY) {
        Position position = getPosition();
        return !parentWidget.isAmountPanelOver(mouseX, mouseY) && isMouseOver(position.x, position.y, 18, 18, mouseX, mouseY);
    }

    boolean mouseOverStock(double mouseX, double mouseY) {
        Position position = getPosition();
        return !parentWidget.isAmountPanelOver(mouseX, mouseY) && isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY);
    }

    boolean isStackValidForSlot(GenericStack stack) {
        if (stack == null || stack.amount() < 0) return true;
        if (!parentWidget.isStocking()) return true;
        return !parentWidget.hasStackInConfig(stack);
    }

    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (slot == null) {
            return null;
        }
        GenericStack stack = null;
        if (this.mouseOverConfig(mouseX, mouseY)) {
            stack = slot.getConfig();
        } else if (this.mouseOverStock(mouseX, mouseY)) {
            stack = slot.getStock();
        }

        if (stack == null || stack.what() == null) {
            return null;
        }

        EmiStack emiStack = EmiStackHelper.toEmiStack(stack);
        if (emiStack != null) {
            if (emiStack.getAmount() == 0L) {
                emiStack.setAmount(1L);
            }

            return new EmiStackInteraction(emiStack, null, false);
        }
        return null;
    }
}
