package com.gtocore.api.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberColor;
import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractiveImageWidget extends Widget {

    private static final int UPDATE_BORDER = 1;
    private static final int UPDATE_TEXT = 2;
    private static final int ACTION_IMAGE_CLICK = 3;

    @Getter
    @NumberRange(range = { -100.0, 100.0 })
    private int border;

    @Getter
    @NumberColor
    private int borderColor = 0xFF000000;

    protected @Nullable Consumer<List<Component>> textSupplier;

    @Getter
    protected List<Component> lastText = new ArrayList<>();

    @OnlyIn(Dist.CLIENT)
    private Supplier<IGuiTexture> textureSupplier;

    @OnlyIn(Dist.CLIENT)
    private IGuiTexture currentImage;

    protected BiConsumer<String, ClickData> clickHandler;

    public InteractiveImageWidget() {
        this(0, 0, 50, 50, new ResourceTexture());
    }

    public InteractiveImageWidget(int x, int y, int width, int height, IGuiTexture image) {
        super(x, y, width, height);
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            this.currentImage = image;
            this.textureSupplier = () -> image;
            return null;
        });
    }

    public InteractiveImageWidget(int x, int y, int width, int height, Supplier<IGuiTexture> textureSupplier) {
        super(x, y, width, height);
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            this.textureSupplier = textureSupplier;
            this.currentImage = textureSupplier.get();
            return null;
        });
    }

    @OnlyIn(Dist.CLIENT)
    public InteractiveImageWidget setImage(IGuiTexture image) {
        this.currentImage = image;
        this.textureSupplier = () -> image;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public InteractiveImageWidget setImageSupplier(Supplier<IGuiTexture> supplier) {
        this.textureSupplier = supplier;
        this.currentImage = supplier.get();
        return this;
    }

    public InteractiveImageWidget setBorder(int border, int color) {
        if (this.border != border || this.borderColor != color) {
            this.border = border;
            this.borderColor = color;
            if (!isRemote()) {
                writeUpdateInfo(UPDATE_BORDER, this::writeBorderData);
            }
        }
        return this;
    }

    public InteractiveImageWidget textSupplier(@Nullable Consumer<List<Component>> textSupplier) {
        this.textSupplier = textSupplier;
        refreshTextOnServer();
        return this;
    }

    public InteractiveImageWidget clickHandler(BiConsumer<String, ClickData> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        writeBorderData(buffer);
        writeTextData(buffer);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        readBorderData(buffer);
        readTextData(buffer);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        refreshTextOnServer();
    }

    private void refreshTextOnServer() {
        if (textSupplier != null && !isRemote()) {
            lastText.clear();
            textSupplier.accept(lastText);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (textSupplier == null || isRemote()) {
            return;
        }

        var newText = new ArrayList<Component>();
        textSupplier.accept(newText);
        if (lastText.equals(newText)) {
            return;
        }
        lastText = newText;
        writeUpdateInfo(UPDATE_TEXT, this::writeTextData);
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        switch (id) {
            case UPDATE_BORDER -> readBorderData(buffer);
            case UPDATE_TEXT -> readTextData(buffer);
            default -> super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id != ACTION_IMAGE_CLICK) {
            return;
        }
        ClickData clickData = ClickData.readFromBuf(buffer);
        String componentData = buffer.readUtf();
        if (clickHandler != null) {
            clickHandler.accept(componentData, clickData);
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    private void writeTextData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(lastText.size());
        for (Component text : lastText) {
            buffer.writeComponent(text);
        }
    }

    private void readTextData(FriendlyByteBuf buffer) {
        lastText.clear();
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            lastText.add(buffer.readComponent());
        }
    }

    private void writeBorderData(FriendlyByteBuf buffer) {
        buffer.writeInt(border);
        buffer.writeInt(borderColor);
    }

    private void readBorderData(FriendlyByteBuf buffer) {
        border = buffer.readInt();
        borderColor = buffer.readInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position pos = getPosition();
        Size size = getSize();

        if (currentImage != null) {
            currentImage.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
        }

        if (border > 0) {
            DrawerHelper.drawBorder(graphics, pos.x, pos.y, size.width, size.height, borderColor, border);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOver(mouseX, mouseY) && !lastText.isEmpty()) {
            updateScreen();
            this.gui.getModularUIGui().setHoverTooltip(lastText, ItemStack.EMPTY, null, null);
            return;
        }
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver((int) mouseX, (int) mouseY)) {
            String data = "image_click";
            ClickData clickData = new ClickData();

            if (clickHandler != null) {
                clickHandler.accept(data, clickData);
            }

            writeClientAction(ACTION_IMAGE_CLICK, buf -> {
                clickData.writeToBuf(buf);
                buf.writeUtf(data);
            });

            playButtonClickSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isMouseOver(int mouseX, int mouseY) {
        Position absolutePos = getPosition();
        Size size = getSize();
        return mouseX >= absolutePos.x && mouseX <= absolutePos.x + size.width
                && mouseY >= absolutePos.y && mouseY <= absolutePos.y + size.height;
    }
}
