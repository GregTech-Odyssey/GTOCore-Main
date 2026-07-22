package com.gtocore.api.research.scanning.editor;

import com.gtocore.api.research.ResearchTag;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

final class DataScanningEditorWidget extends DraggableScrollableWidgetGroup {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 166;
    private static final int LABEL_X = 6;
    private static final int INPUT_X = 96;
    private static final int INPUT_WIDTH = 190;
    private static final int ROW_HEIGHT = 20;

    private static final int ACTION_SET_TARGET = 20;

    private final EditorState state = new EditorState();

    DataScanningEditorWidget() {
        super(0, 0, WIDTH, HEIGHT);
        setYScrollBarWidth(4);
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearAllWidgets();
        int y = 5;

        addLabel(y, "Scan target");
        addWidget(new IngredientGhostSlot(INPUT_X, y, () -> state.target, this::setTargetFromClient));
        addWidget(button(INPUT_X + 21, y, 18, 18, "X", ignored -> {
            state.target = null;
            rebuildWidgets();
        }));
        addWidget(rollingText(INPUT_X + 44, y, INPUT_WIDTH - 44, displayIngredient(state.target)));
        y += ROW_HEIGHT;

        addLabel(y, "Research points");
        addWidget(button(INPUT_X + INPUT_WIDTH - 18, y, 18, 18, "+", ignored -> addResearchPoint()));
        y += ROW_HEIGHT;

        List<String> tagNames = tagNames();
        for (int index = 0; index < state.points.size(); index++) {
            int row = index;
            PointEntry point = state.points.get(index);
            SelectorWidget tagSelector = new SelectorWidget(INPUT_X, y, 105, 18, tagNames, -1)
                    .setButtonBackground(GuiTextures.BUTTON)
                    .setMaxCount(8)
                    .setValue(point.tagName)
                    .setOnChanged(value -> point.tagName = value);
            addWidget(tagSelector);
            TextFieldWidget amountField = new TextFieldWidget(INPUT_X + 109, y, 59, 18,
                    () -> Long.toString(point.amount),
                    value -> point.amount = parseLong(value, point.amount))
                    .setCurrentString(point.amount)
                    .setMaxStringLength(20)
                    .setNumbersOnly(1L, Long.MAX_VALUE);
            addWidget(amountField);
            addWidget(button(INPUT_X + INPUT_WIDTH - 18, y, 18, 18, "X", ignored -> {
                state.points.remove(row);
                rebuildWidgets();
            }));
            y += ROW_HEIGHT;
        }

        y += 2;
        addWidget(button(INPUT_X, y, INPUT_WIDTH, 20, "Export Java to log", click -> {
            if (!click.isRemote) {
                exportToLog();
            }
        }));
        y += 25;

        addWidget(new Widget(0, y, WIDTH - 5, 1));
    }

    private void addLabel(int y, String text) {
        addWidget(new LabelWidget(LABEL_X, y + 5, text));
    }

    private ButtonWidget button(int x, int y, int width, int height, String text, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> action) {
        return new ButtonWidget(x, y, width, height,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(text)), action);
    }

    private ImageWidget rollingText(int x, int y, int width, String text) {
        return new ImageWidget(x, y, width, 18,
                new TextTexture(text, -1).setWidth(width).setType(TextTexture.TextType.ROLL));
    }

    private void addResearchPoint() {
        for (String tag : tagNames()) {
            boolean used = state.points.stream().anyMatch(entry -> entry.tagName.equals(tag));
            if (!used) {
                state.points.add(new PointEntry(tag, 1L));
                rebuildWidgets();
                return;
            }
        }
    }

    private void setTargetFromClient(IngredientValue value) {
        state.target = value;
        rebuildWidgets();
        writeClientAction(ACTION_SET_TARGET, buffer -> writeIngredient(buffer, value));
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == ACTION_SET_TARGET) {
            IngredientValue value = readIngredient(buffer);
            if (isRegistered(value)) {
                state.target = value;
                rebuildWidgets();
            }
        }
    }

    private void exportToLog() {
        String error = validate();
        if (error != null) {
            GTOCore.LOGGER.error("Data scanning editor export failed: {}", error);
            return;
        }
        GTOCore.LOGGER.error("Generated data scanning Java code:\n{}", generateCode());
    }

    private @Nullable String validate() {
        if (state.target == null || !isRegistered(state.target)) {
            return "an item or fluid scan target is required";
        }
        if (state.points.isEmpty()) {
            return "at least one ResearchPoint entry is required";
        }
        var tags = new HashSet<String>();
        for (PointEntry point : state.points) {
            if (!ResearchTag.TAGS.containsKey(point.tagName)) {
                return "unknown ResearchTag: " + point.tagName;
            }
            if (!tags.add(point.tagName)) {
                return "duplicate ResearchTag: " + point.tagName;
            }
            if (point.amount <= 0) {
                return "ResearchPoint amounts must be positive";
            }
        }
        return null;
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder("DataScanningManager.registerDataScanning(")
                .append(ingredientExpression(state.target))
                .append(", ResearchPoints.of(");
        for (PointEntry point : state.points) {
            code.append("ResearchTag.")
                    .append(escape(point.tagName).toUpperCase())
                    .append(", ")
                    .append(point.amount)
                    .append("L, ");
        }
        return code.delete(code.length() - 2, code.length()).append("));").toString();
    }

    private static String ingredientExpression(IngredientValue value) {
        String method = value.kind == IngredientKind.ITEM ? "getItem" : "getFluid";
        return "RegistriesUtils." + method + "(\"" + escape(value.id.toString()) + "\")";
    }

    private static String displayIngredient(@Nullable IngredientValue value) {
        return value == null ? "None" : value.id.toString();
    }

    private static List<String> tagNames() {
        return ResearchTag.TAGS.keySet().stream().sorted().toList();
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static void writeIngredient(FriendlyByteBuf buffer, IngredientValue value) {
        buffer.writeEnum(value.kind);
        buffer.writeResourceLocation(value.id);
    }

    private static IngredientValue readIngredient(FriendlyByteBuf buffer) {
        return new IngredientValue(buffer.readEnum(IngredientKind.class), buffer.readResourceLocation());
    }

    private final class IngredientGhostSlot extends Widget implements IGhostIngredientTarget {

        private final java.util.function.Supplier<IngredientValue> valueSupplier;
        private final Consumer<IngredientValue> valueConsumer;

        private IngredientGhostSlot(int x, int y, java.util.function.Supplier<IngredientValue> valueSupplier,
                                    Consumer<IngredientValue> valueConsumer) {
            super(x, y, 18, 18);
            this.valueSupplier = valueSupplier;
            this.valueConsumer = valueConsumer;
            setBackground(GuiTextures.SLOT);
            IngredientValue value = valueSupplier.get();
            setHoverTooltips(Component.literal(value == null ? "Drop an item or fluid from EMI" : value.id.toString()));
        }

        @Override
        public List<Target> getPhantomTargets(Object ingredient) {
            IngredientValue value = ingredientValue(ingredient);
            if (value == null) {
                return List.of();
            }
            return List.of(new Target() {

                @Override
                public @NotNull Rect2i getArea() {
                    return toRectangleBox();
                }

                @Override
                public void accept(Object ignored) {
                    valueConsumer.accept(value);
                }
            });
        }

        @Override
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            Position position = getPosition();
            AEKey key = toAEKey(valueSupplier.get());
            if (key != null) {
                AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, position.x + 1, position.y + 1, key);
            } else {
                graphics.drawString(Minecraft.getInstance().font, "+", position.x + 6, position.y + 5, 0xFFFFFFFF, false);
            }
        }
    }

    private static @Nullable IngredientValue ingredientValue(Object ingredient) {
        if (!(ingredient instanceof EmiStack stack)) {
            return null;
        }
        if (stack.getKey() instanceof Item item) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            return id == null ? null : new IngredientValue(IngredientKind.ITEM, id);
        }
        if (stack.getKey() instanceof Fluid fluid) {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
            return id == null ? null : new IngredientValue(IngredientKind.FLUID, id);
        }
        return null;
    }

    private static @Nullable AEKey toAEKey(@Nullable IngredientValue value) {
        if (value == null) {
            return null;
        }
        if (value.kind == IngredientKind.ITEM) {
            Item item = ForgeRegistries.ITEMS.getValue(value.id);
            return item == null ? null : AEItemKey.of(item);
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(value.id);
        return fluid == null ? null : AEFluidKey.of(fluid);
    }

    private static boolean isRegistered(IngredientValue value) {
        if (value.kind == IngredientKind.ITEM) {
            return ForgeRegistries.ITEMS.containsKey(value.id);
        }
        return ForgeRegistries.FLUIDS.containsKey(value.id);
    }

    private enum IngredientKind {
        ITEM,
        FLUID
    }

    private static final class EditorState {

        private @Nullable IngredientValue target;
        private final List<PointEntry> points = new ArrayList<>();
    }

    private static final class PointEntry {

        private String tagName;
        private long amount;

        private PointEntry(String tagName, long amount) {
            this.tagName = tagName;
            this.amount = amount;
        }
    }

    private record IngredientValue(IngredientKind kind, ResourceLocation id) {}
}
