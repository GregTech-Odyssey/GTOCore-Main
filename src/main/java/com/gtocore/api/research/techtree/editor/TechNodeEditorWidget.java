package com.gtocore.api.research.techtree.editor;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.integration.emi.research.TechNodeEmiStack;

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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

final class TechNodeEditorWidget extends DraggableScrollableWidgetGroup {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 166;
    private static final int LABEL_X = 6;
    private static final int INPUT_X = 96;
    private static final int INPUT_WIDTH = 190;
    private static final int ROW_HEIGHT = 20;
    private static final Pattern NODE_ID = Pattern.compile("[a-z0-9_./-]+");

    private static final int ACTION_SET_ICON = 20;
    private static final int ACTION_SET_EUREKA = 21;
    private static final int ACTION_ADD_PREREQUISITE = 22;

    private final EditorState state = new EditorState();

    TechNodeEditorWidget() {
        super(0, 0, WIDTH, HEIGHT);
        setYScrollBarWidth(4);
        initializeDefaults();
        rebuildWidgets();
    }

    private void initializeDefaults() {
        state.managerId = managerIds().stream().findFirst().orElse("");
        state.language = OutputLanguage.JAVA;
        state.eurekaProgress = 1.0F;
    }

    private void rebuildWidgets() {
        clearAllWidgets();
        int y = 5;

        addLabel(y, "Tech tree");
        addWidget(selector(y, managerIds(), state.managerId, value -> state.managerId = value));
        y += ROW_HEIGHT;

        addLabel(y, "Node ID");
        addWidget(textField(y, () -> state.nodeId, value -> state.nodeId = value, 128));
        y += ROW_HEIGHT;

        addLabel(y, "Chinese name");
        addWidget(textField(y, () -> state.chineseName, value -> state.chineseName = value, 256));
        y += ROW_HEIGHT;

        addLabel(y, "English name");
        addWidget(textField(y, () -> state.englishName, value -> state.englishName = value, 256));
        y += ROW_HEIGHT;

        addLabel(y, "Description CN");
        addWidget(textField(y, () -> state.chineseDescription, value -> state.chineseDescription = value, 512));
        y += ROW_HEIGHT;

        addLabel(y, "Description EN");
        addWidget(textField(y, () -> state.englishDescription, value -> state.englishDescription = value, 512));
        y += ROW_HEIGHT;

        addLabel(y, "Icon");
        addWidget(new IngredientGhostSlot(INPUT_X, y, true, () -> state.icon, this::setIconFromClient));
        addWidget(button(INPUT_X + 21, y, 18, 18, "X", ignored -> {
            state.icon = null;
            rebuildWidgets();
        }));
        addWidget(rollingText(INPUT_X + 44, y, INPUT_WIDTH - 44, displayIngredient(state.icon)));
        y += ROW_HEIGHT;

        addLabel(y, "Prerequisites");
        addWidget(new PrerequisiteDropWidget(INPUT_X, y, INPUT_WIDTH, 18, this::addPrerequisiteFromClient));
        y += ROW_HEIGHT;
        for (int index = 0; index < state.prerequisites.size(); index++) {
            int row = index;
            PrerequisiteEntry prerequisite = state.prerequisites.get(index);
            addWidget(rollingText(INPUT_X, y, INPUT_WIDTH - 21, prerequisite.managerId + "/" + prerequisite.nodeId));
            addWidget(button(INPUT_X + INPUT_WIDTH - 18, y, 18, 18, "X", ignored -> {
                state.prerequisites.remove(row);
                rebuildWidgets();
            }));
            y += ROW_HEIGHT;
        }

        addLabel(y, "CWU needed");
        TextFieldWidget cwuField = textField(y, () -> Long.toString(state.cwuNeeded), value -> state.cwuNeeded = parseLong(value, state.cwuNeeded), 20)
                .setNumbersOnly(0L, Long.MAX_VALUE);
        addWidget(cwuField);
        y += ROW_HEIGHT;

        addLabel(y, "Materials");
        addWidget(button(INPUT_X + INPUT_WIDTH - 18, y, 18, 18, "+", ignored -> addMaterial()));
        y += ROW_HEIGHT;
        List<String> tagNames = tagNames();
        for (int index = 0; index < state.materials.size(); index++) {
            int row = index;
            MaterialEntry material = state.materials.get(index);
            SelectorWidget tagSelector = new SelectorWidget(INPUT_X, y, 105, 18, tagNames, -1)
                    .setButtonBackground(GuiTextures.BUTTON)
                    .setMaxCount(8)
                    .setValue(material.tagName)
                    .setOnChanged(value -> material.tagName = value);
            addWidget(tagSelector);
            TextFieldWidget amountField = new TextFieldWidget(INPUT_X + 109, y, 59, 18,
                    () -> Long.toString(material.amount),
                    value -> material.amount = parseLong(value, material.amount))
                    .setCurrentString(material.amount)
                    .setMaxStringLength(20)
                    .setNumbersOnly(1L, Long.MAX_VALUE);
            addWidget(amountField);
            addWidget(button(INPUT_X + INPUT_WIDTH - 18, y, 18, 18, "X", ignored -> {
                state.materials.remove(row);
                rebuildWidgets();
            }));
            y += ROW_HEIGHT;
        }

        addLabel(y, "Eureka item");
        addWidget(new IngredientGhostSlot(INPUT_X, y, false, () -> state.eurekaItem, this::setEurekaFromClient));
        addWidget(button(INPUT_X + 21, y, 18, 18, "X", ignored -> {
            state.eurekaItem = null;
            rebuildWidgets();
        }));
        addWidget(rollingText(INPUT_X + 44, y, INPUT_WIDTH - 44, displayIngredient(state.eurekaItem)));
        y += ROW_HEIGHT;

        addLabel(y, "Eureka progress");
        TextFieldWidget progressField = textField(y, () -> Float.toString(state.eurekaProgress),
                value -> state.eurekaProgress = parseFloat(value, state.eurekaProgress), 16)
                .setNumbersOnly(0.0F, 1.0F);
        addWidget(progressField);
        y += ROW_HEIGHT;

        addLabel(y, "Output");
        addWidget(selector(y, List.of("Java", "Kotlin"), state.language.displayName,
                value -> state.language = OutputLanguage.fromDisplayName(value)));
        y += ROW_HEIGHT + 2;

        addWidget(button(INPUT_X, y, INPUT_WIDTH, 20, "Export to log", click -> {
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

    private SelectorWidget selector(int y, List<String> candidates, String value, Consumer<String> onChanged) {
        String selected = candidates.contains(value) ? value : candidates.stream().findFirst().orElse("");
        return new SelectorWidget(INPUT_X, y, INPUT_WIDTH, 18, candidates, -1)
                .setButtonBackground(GuiTextures.BUTTON)
                .setMaxCount(8)
                .setValue(selected)
                .setOnChanged(onChanged);
    }

    private TextFieldWidget textField(int y, Supplier<String> supplier, Consumer<String> responder, int maxLength) {
        return new TextFieldWidget(INPUT_X, y, INPUT_WIDTH, 18, supplier, responder)
                .setCurrentString(supplier.get())
                .setMaxStringLength(maxLength);
    }

    private ButtonWidget button(int x, int y, int width, int height, String text, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> action) {
        return new ButtonWidget(x, y, width, height,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(text)), action);
    }

    private ImageWidget rollingText(int x, int y, int width, String text) {
        return new ImageWidget(x, y, width, 18,
                new TextTexture(text, -1).setWidth(width).setType(TextTexture.TextType.ROLL));
    }

    private void addMaterial() {
        List<String> tags = tagNames();
        for (String tag : tags) {
            boolean used = state.materials.stream().anyMatch(entry -> entry.tagName.equals(tag));
            if (!used) {
                state.materials.add(new MaterialEntry(tag, 1L));
                rebuildWidgets();
                return;
            }
        }
    }

    private void setIconFromClient(IngredientValue value) {
        state.icon = value;
        rebuildWidgets();
        writeClientAction(ACTION_SET_ICON, buffer -> writeIngredient(buffer, value));
    }

    private void setEurekaFromClient(IngredientValue value) {
        state.eurekaItem = value;
        rebuildWidgets();
        writeClientAction(ACTION_SET_EUREKA, buffer -> writeIngredient(buffer, value));
    }

    private void addPrerequisiteFromClient(PrerequisiteEntry prerequisite) {
        if (!state.prerequisites.contains(prerequisite)) {
            state.prerequisites.add(prerequisite);
            rebuildWidgets();
        }
        writeClientAction(ACTION_ADD_PREREQUISITE, buffer -> {
            buffer.writeUtf(prerequisite.managerId);
            buffer.writeUtf(prerequisite.nodeId);
        });
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == ACTION_SET_ICON) {
            IngredientValue value = readIngredient(buffer);
            if (isRegistered(value, true)) {
                state.icon = value;
                rebuildWidgets();
            }
        } else if (id == ACTION_SET_EUREKA) {
            IngredientValue value = readIngredient(buffer);
            if (value.kind == IngredientKind.ITEM && isRegistered(value, false)) {
                state.eurekaItem = value;
                rebuildWidgets();
            }
        } else if (id == ACTION_ADD_PREREQUISITE) {
            PrerequisiteEntry prerequisite = new PrerequisiteEntry(buffer.readUtf(), buffer.readUtf());
            TechTreeManager manager = TechTreeManager.getManager(prerequisite.managerId);
            if (manager != null && manager.getNode(prerequisite.nodeId) != null && !state.prerequisites.contains(prerequisite)) {
                state.prerequisites.add(prerequisite);
                rebuildWidgets();
            }
        }
    }

    private void exportToLog() {
        String error = validate();
        if (error != null) {
            GTOCore.LOGGER.error("TechNode editor export failed: {}", error);
            return;
        }
        GTOCore.LOGGER.error("Generated TechNode {} code:\n{}", state.language.displayName, generateCode());
    }

    private @Nullable String validate() {
        if (TechTreeManager.getManager(state.managerId) == null) {
            return "a TechTree must be selected";
        }
        if (state.nodeId.isBlank()) {
            return "node ID is required";
        }
        if (!NODE_ID.matcher(state.nodeId).matches()) {
            return "node ID must match " + NODE_ID.pattern();
        }
        if (state.chineseName.isBlank()) {
            return "Chinese name is required";
        }
        return null;
    }

    private String generateCode() {
        boolean kotlin = state.language == OutputLanguage.KOTLIN;
        String indent = kotlin ? "    " : "        ";
        StringBuilder code = new StringBuilder();
        code.append(kotlin ? "val node = " : "var node = ")
                .append("TechTreeManager.getManager(\"").append(escape(state.managerId)).append("\")")
                .append(kotlin ? "!!\n" : "\n")
                .append(indent).append(".builder(\"").append(escape(state.nodeId)).append("\", \"")
                .append(escape(state.chineseName)).append("\", \"")
                .append(escape(state.englishName.isBlank() ? state.nodeId : state.englishName)).append("\")\n");

        if (!state.chineseDescription.isBlank() || !state.englishDescription.isBlank()) {
            String cn = state.chineseDescription.isBlank() ? state.englishDescription : state.chineseDescription;
            String en = state.englishDescription.isBlank() ? cn : state.englishDescription;
            code.append(indent).append(".description(\"").append(escape(cn)).append("\", \"")
                    .append(escape(en)).append("\")\n");
        }
        if (state.icon != null) {
            code.append(indent).append(".icon(").append(ingredientExpression(state.icon)).append(")\n");
        }
        appendPrerequisites(code, kotlin, indent);
        appendRequirements(code, kotlin, indent);
        code.append(indent).append(".build()");
        if (!kotlin) {
            code.append(';');
        }
        return code.toString();
    }

    private void appendPrerequisites(StringBuilder code, boolean kotlin, String indent) {
        if (state.prerequisites.isEmpty()) {
            return;
        }
        boolean sameTree = state.prerequisites.stream().allMatch(entry -> entry.managerId.equals(state.managerId));
        code.append(indent).append(".prerequisites(");
        for (int i = 0; i < state.prerequisites.size(); i++) {
            if (i > 0) {
                code.append(", ");
            }
            PrerequisiteEntry prerequisite = state.prerequisites.get(i);
            if (sameTree) {
                code.append('"').append(escape(prerequisite.nodeId)).append('"');
            } else {
                code.append("TechTreeManager.getManager(\"").append(escape(prerequisite.managerId)).append("\")");
                if (kotlin) {
                    code.append("!!");
                }
                code.append(".getNode(\"").append(escape(prerequisite.nodeId)).append("\")");
                if (kotlin) {
                    code.append("!!");
                }
            }
        }
        code.append(")\n");
    }

    private void appendRequirements(StringBuilder code, boolean kotlin, String indent) {
        if (!hasRequirements()) {
            return;
        }
        String nested = indent + "    ";
        if (kotlin) {
            code.append(indent).append(".requirements(\n")
                    .append(nested).append("ResearchRequirements.Builder()\n");
        } else {
            code.append(indent).append(".requirements(new ResearchRequirements.Builder()\n");
        }
        if (state.cwuNeeded > 0) {
            code.append(nested).append(indent).append(".setCWUNeeded(").append(state.cwuNeeded).append("L)\n");
        }
        for (MaterialEntry material : state.materials) {
            String tag = kotlin ? "ResearchTag.TAGS[\"" + escape(material.tagName) + "\"]!!" :
                    "ResearchTag.TAGS.get(\"" + escape(material.tagName) + "\")";
            code.append(nested).append(indent).append(".addMaterialNeeded(").append(tag).append(", ")
                    .append(material.amount).append("L)\n");
        }
        if (state.eurekaItem != null) {
            code.append(nested).append(indent).append(".setEurekaItem(")
                    .append(ingredientExpression(state.eurekaItem)).append(", ")
                    .append(Float.toString(state.eurekaProgress)).append("F)\n");
        }
        code.append(nested).append(indent).append(".build()")
                .append(kotlin ? "\n" + indent + ")\n" : ")\n");
    }

    private boolean hasRequirements() {
        return state.cwuNeeded > 0 || !state.materials.isEmpty() || state.eurekaItem != null;
    }

    private static String ingredientExpression(IngredientValue value) {
        String method = value.kind == IngredientKind.ITEM ? "getItem" : "getFluid";
        return "RegistriesUtils." + method + "(\"" + escape(value.id.toString()) + "\")";
    }

    private static String displayIngredient(@Nullable IngredientValue value) {
        return value == null ? "None" : value.id.toString();
    }

    private static List<String> managerIds() {
        return TechTreeManager.getManagers().stream()
                .map(TechTreeManager::getId)
                .sorted()
                .toList();
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

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
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

        private final boolean acceptFluids;
        private final Supplier<IngredientValue> valueSupplier;
        private final Consumer<IngredientValue> valueConsumer;

        private IngredientGhostSlot(int x, int y, boolean acceptFluids, Supplier<IngredientValue> valueSupplier,
                                    Consumer<IngredientValue> valueConsumer) {
            super(x, y, 18, 18);
            this.acceptFluids = acceptFluids;
            this.valueSupplier = valueSupplier;
            this.valueConsumer = valueConsumer;
            setBackground(GuiTextures.SLOT);
            IngredientValue value = valueSupplier.get();
            setHoverTooltips(Component.literal(value == null ?
                    (acceptFluids ? "Drop an item or fluid from EMI" : "Drop an item from EMI") : value.id.toString()));
        }

        @Override
        public List<Target> getPhantomTargets(Object ingredient) {
            IngredientValue value = ingredientValue(ingredient, acceptFluids);
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

    private final class PrerequisiteDropWidget extends Widget implements IGhostIngredientTarget {

        private final Consumer<PrerequisiteEntry> consumer;

        private PrerequisiteDropWidget(int x, int y, int width, int height, Consumer<PrerequisiteEntry> consumer) {
            super(x, y, width, height);
            this.consumer = consumer;
            setBackground(GuiTextures.SLOT);
            setHoverTooltips(Component.literal("Drop a TechNode from EMI to append it"));
        }

        @Override
        public List<Target> getPhantomTargets(Object ingredient) {
            if (!(ingredient instanceof TechNodeEmiStack stack)) {
                return List.of();
            }
            PrerequisiteEntry prerequisite = new PrerequisiteEntry(stack.data.getManager().getId(), stack.data.name);
            return List.of(new Target() {

                @Override
                public @NotNull Rect2i getArea() {
                    return toRectangleBox();
                }

                @Override
                public void accept(Object ignored) {
                    consumer.accept(prerequisite);
                }
            });
        }

        @Override
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            Position position = getPosition();
            graphics.drawString(Minecraft.getInstance().font, "+", position.x + 5, position.y + 5, 0xFFB8B8B8, false);
        }
    }

    private static @Nullable IngredientValue ingredientValue(Object ingredient, boolean acceptFluids) {
        if (!(ingredient instanceof EmiStack stack)) {
            return null;
        }
        if (stack.getKey() instanceof Item item) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            return id == null ? null : new IngredientValue(IngredientKind.ITEM, id);
        }
        if (acceptFluids && stack.getKey() instanceof Fluid fluid) {
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

    private static boolean isRegistered(IngredientValue value, boolean acceptFluids) {
        if (value.kind == IngredientKind.ITEM) {
            return ForgeRegistries.ITEMS.containsKey(value.id);
        }
        return acceptFluids && ForgeRegistries.FLUIDS.containsKey(value.id);
    }

    private enum IngredientKind {
        ITEM,
        FLUID
    }

    private enum OutputLanguage {

        JAVA("Java"),
        KOTLIN("Kotlin");

        private final String displayName;

        OutputLanguage(String displayName) {
            this.displayName = displayName;
        }

        private static OutputLanguage fromDisplayName(String name) {
            for (OutputLanguage language : values()) {
                if (language.displayName.equals(name)) {
                    return language;
                }
            }
            return JAVA;
        }
    }

    private static final class EditorState {

        private String managerId = "";
        private String nodeId = "";
        private String chineseName = "";
        private String englishName = "";
        private String chineseDescription = "";
        private String englishDescription = "";
        private @Nullable IngredientValue icon;
        private final List<PrerequisiteEntry> prerequisites = new ArrayList<>();
        private long cwuNeeded;
        private final List<MaterialEntry> materials = new ArrayList<>();
        private @Nullable IngredientValue eurekaItem;
        private float eurekaProgress;
        private OutputLanguage language;
    }

    private static final class MaterialEntry {

        private String tagName;
        private long amount;

        private MaterialEntry(String tagName, long amount) {
            this.tagName = Objects.requireNonNull(tagName);
            this.amount = amount;
        }
    }

    private record IngredientValue(IngredientKind kind, ResourceLocation id) {}

    private record PrerequisiteEntry(String managerId, String nodeId) {}
}
