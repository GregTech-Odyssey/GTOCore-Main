package com.gtocore.api.research.techtree.editor;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.ui.TechTreeView;
import com.gtocore.integration.emi.research.TechNodeEmiStack;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.PhantomFluidSlot;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.Stepper;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 科技节点编辑器（开发工具，只在开发环境或开启自定义配方时出现）：填好节点的各项数据，按"导出"在服务端日志里生成
 * {@code TechTreeManager.builder(...)} 的 Java / Kotlin 代码。
 * <p>
 * 编辑中的数据属于这一个打开的界面，放在服务端的 {@link State} 里；输入框、槽位、按钮都走框架的同步（服务端校验后写入）。
 * 前置节点、研究点数是可变长的列表：预先建好 {@link #MAX_ROWS} 行，按服务端下发的行数在客户端显隐，两端控件树不变。
 * 界面文字是开发用的英文字面量，不进语言文件。
 */
public final class TechNodeEditorPage {

    private static final Pattern NODE_ID = Pattern.compile("[a-z0-9_./-]+");
    /// 前置节点、研究点数各自最多几行
    private static final int MAX_ROWS = 12;
    private static final int WIDTH = 2 * UISizes.SLOT_ROW_WIDTH;
    private static final int VIEW_HEIGHT = 8 * UISizes.SLOT;
    /// 客户端请求：追加前置节点（参数：节点编码）；避开 WidgetGroup 自用的 1、2 与同步值的 0x5A00 段
    private static final int ACTION_ADD_PREREQUISITE = 0x5B10;

    private TechNodeEditorPage() {}

    /** 编辑器页面（两端都会执行）。 */
    public static Widget create() {
        var state = new State();
        var column = UIElement.column(WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));

        var managers = managers();
        column.addChild(section("Tech tree", ButtonGroup.singleIcons(managers.size(), i -> managers.get(i).getIcon(),
                i -> TechTreeManager.getTreeName(managers.get(i)), () -> state.manager, i -> state.manager = i)));

        column.addChild(section("Node ID", text(() -> state.nodeId, v -> state.nodeId = v)));
        column.addChild(section("Name (CN / EN)", text(() -> state.chineseName, v -> state.chineseName = v),
                text(() -> state.englishName, v -> state.englishName = v)));
        column.addChild(section("Description (CN / EN)", text(() -> state.chineseDescription, v -> state.chineseDescription = v),
                text(() -> state.englishDescription, v -> state.englishDescription = v)));

        // 图标：物品或流体，有流体时用流体
        var icon = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChildren(
                new PhantomItemSlot(state.icon, 0).xeiPhantom(),
                new PhantomFluidSlot(null, 0, () -> state.iconFluid, v -> state.iconFluid = v).xeiPhantom(),
                TextLine.constant(LayoutStyle.AUTO, Component.literal("Item, or fluid (fluid wins)")).layout(l -> l.flex(1)));
        column.addChild(section("Icon", icon));

        var prerequisites = section("Prerequisites", new PrerequisiteDrop(state));
        for (int i = 0; i < MAX_ROWS; i++) prerequisites.addChild(prerequisiteRow(prerequisites, state, i));
        column.addChild(prerequisites);

        column.addChild(section("CWU needed", new NumberField(LayoutStyle.AUTO, () -> state.cwuNeeded, v -> state.cwuNeeded = v,
                () -> 0, () -> Long.MAX_VALUE, 1, 64, 4096, 262144)));

        var tags = tagNames();
        var add = Button.text(LayoutStyle.AUTO, () -> "Add research points").setOnServerClick(() -> addMaterial(state, tags))
                .disabled(() -> state.materials.size() >= Math.min(MAX_ROWS, tags.size()), null);
        var materials = section("Research points", add);
        for (int i = 0; i < MAX_ROWS; i++) materials.addChild(materialRow(materials, state, tags, i));
        column.addChild(materials);

        var eureka = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChildren(
                new PhantomItemSlot(state.eureka, 0).xeiPhantom(),
                TextLine.constant(LayoutStyle.AUTO, Component.literal("Progress %")).layout(l -> l.flex(1)),
                new Stepper(UISizes.VALUE_WIDTH, () -> state.eurekaPercent, v -> state.eurekaPercent = v, 0, 100, false, v -> v + "%"));
        column.addChild(section("Eureka item", eureka));

        column.addChild(section("Output", ButtonGroup.single(2, i -> Component.literal(i == 0 ? "Java" : "Kotlin"),
                () -> state.kotlin ? 1 : 0, i -> state.kotlin = i == 1).horizontal()));
        column.addChild(Button.text(LayoutStyle.AUTO, () -> "Export to log").setVariant(UITheme.ButtonVariant.CONFIRM)
                .setOnServerClick(() -> exportToLog(state)));

        var scroller = new ScrollerView("techtree.editor", WIDTH + ScrollerView.SCROLL_BAR_SPACE, VIEW_HEIGHT);
        scroller.addScrollViewChild(column);
        return scroller;
    }

    private static UIElement section(String title, Widget... children) {
        var section = UIElement.section().addChild(TextLine.constant(LayoutStyle.AUTO, Component.literal(title)));
        section.addChildren(children);
        return section;
    }

    private static TextField text(Supplier<String> getter, Consumer<String> setter) {
        return new TextField(LayoutStyle.AUTO, getter, value -> setter.accept(value.length() > 512 ? value.substring(0, 512) : value));
    }

    /** 第 {@code index} 行前置节点：名称 + 删除；行数由服务端下发，客户端据此显隐。 */
    private static UIElement prerequisiteRow(UIElement parent, State state, int index) {
        var row = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
        var name = TextLine.of(LayoutStyle.AUTO, () -> index < state.prerequisites.size() ?
                Component.literal(state.prerequisites.get(index)) : Component.empty()).layout(l -> l.flex(1));
        var remove = Button.glyph("×").setVariant(UITheme.ButtonVariant.DANGER).setOnServerClick(() -> {
            if (index < state.prerequisites.size()) state.prerequisites.remove(index);
        });
        row.addChildren(name, remove);
        bindDisplay(parent, row, () -> index < state.prerequisites.size());
        return row;
    }

    /** 第 {@code index} 行研究点数：领域（步进切换）+ 数量 + 删除。 */
    private static UIElement materialRow(UIElement parent, State state, List<String> tags, int index) {
        var row = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
        var tag = new Stepper(3 * UISizes.BUTTON_WIDTH / 2, () -> index < state.materials.size() ? Math.max(0, tags.indexOf(state.materials.get(index).tag)) : 0,
                v -> {
                    if (index < state.materials.size() && v >= 0 && v < tags.size()) state.materials.get(index).tag = tags.get(v);
                }, 0, Math.max(0, tags.size() - 1), true, tags::get);
        var amount = new NumberField(LayoutStyle.AUTO, () -> index < state.materials.size() ? state.materials.get(index).amount : 1,
                v -> {
                    if (index < state.materials.size()) state.materials.get(index).amount = v;
                }, () -> 1, () -> Long.MAX_VALUE).layout(l -> l.flex(1));
        var remove = Button.glyph("×").setVariant(UITheme.ButtonVariant.DANGER).setOnServerClick(() -> {
            if (index < state.materials.size()) state.materials.remove(index);
        });
        row.addChildren(tag, amount, remove);
        bindDisplay(parent, row, () -> index < state.materials.size());
        return row;
    }

    /**
     * 行的显隐：服务端判定、下发，两端各自显隐（控件树不变）。同步值挂在父元素上——隐藏会停掉该行自己的同步，
     * 挂在行上就再也收不到"重新显示"。
     */
    private static void bindDisplay(UIElement parent, UIElement row, BooleanSupplier shown) {
        row.setDisplay(false);
        parent.addSyncValue(SyncValue.of(shown::getAsBoolean, SyncValue.BOOLEAN, false).onChanged(row::setDisplay));
    }

    private static void addMaterial(State state, List<String> tags) {
        if (state.materials.size() >= MAX_ROWS) return;
        for (var tag : tags) {
            boolean used = false;
            for (var entry : state.materials) used |= entry.tag.equals(tag);
            if (!used) {
                state.materials.add(new Material(tag, 1));
                return;
            }
        }
    }

    /** 从 EMI 拖入科技节点追加为前置（客户端请求，服务端校验节点编码后追加）。 */
    private static final class PrerequisiteDrop extends UIElement implements IGhostIngredientTarget {

        private final State state;

        private PrerequisiteDrop(State state) {
            this.state = state;
            layout(l -> l.height(UISizes.SLOT));
            setBackground(UITheme.ITEM_SLOT);
            setHoverTooltips(Component.literal("Drop a tech node from EMI to append it"));
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public List<Target> getPhantomTargets(Object ingredient) {
            if (!(ingredient instanceof TechNodeEmiStack stack)) return Collections.emptyList();
            int code = TechTreeView.encodeNode(stack.data);
            return List.of(new Target() {

                @Override
                public @NotNull Rect2i getArea() {
                    return toRectangleBox();
                }

                @Override
                public void accept(@NotNull Object ignored) {
                    writeClientAction(ACTION_ADD_PREREQUISITE, buf -> buf.writeVarInt(code));
                }
            });
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id != ACTION_ADD_PREREQUISITE) {
                super.handleClientAction(id, buffer);
                return;
            }
            TechNode node = TechTreeView.decodeNode(buffer.readVarInt());
            if (node == null || state.prerequisites.size() >= MAX_ROWS) return;
            String entry = node.getManager().getId() + "/" + node.name;
            if (!state.prerequisites.contains(entry)) state.prerequisites.add(entry);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            UITheme.drawCenteredText(graphics, "+ drop tech node", getPositionX() + getSizeWidth() / 2, getPositionY() + 5,
                    getSizeWidth() - 4, UITheme.TEXT, false);
            UITheme.drawXeiPhantom(graphics, getPositionX(), getPositionY(), UISizes.SLOT, UISizes.SLOT, false);
        }
    }

    // ==================== 导出 ====================

    private static void exportToLog(State state) {
        String error = validate(state);
        if (error != null) {
            GTOCore.LOGGER.error("TechNode editor export failed: {}", error);
            return;
        }
        GTOCore.LOGGER.error("Generated TechNode {} code:\n{}", state.kotlin ? "Kotlin" : "Java", generateCode(state));
    }

    private static @Nullable String validate(State state) {
        if (managerId(state) == null) return "a TechTree must be selected";
        if (state.nodeId.isBlank()) return "node ID is required";
        if (!NODE_ID.matcher(state.nodeId).matches()) return "node ID must match " + NODE_ID.pattern();
        if (state.chineseName.isBlank()) return "Chinese name is required";
        return null;
    }

    private static @Nullable String managerId(State state) {
        var managers = managers();
        return state.manager >= 0 && state.manager < managers.size() ? managers.get(state.manager).getId() : null;
    }

    private static String generateCode(State state) {
        boolean kotlin = state.kotlin;
        String managerId = managerId(state);
        String indent = kotlin ? "    " : "        ";
        StringBuilder code = new StringBuilder();
        code.append(kotlin ? "val node = " : "var node = ")
                .append("TechTreeManager.getManager(\"").append(escape(managerId)).append("\")")
                .append(kotlin ? "!!\n" : "\n")
                .append(indent).append(".builder(\"").append(escape(state.nodeId)).append("\", \"")
                .append(escape(state.chineseName)).append("\", \"")
                .append(escape(state.englishName.isBlank() ? state.nodeId : state.englishName)).append("\")\n");
        if (!state.chineseDescription.isBlank() || !state.englishDescription.isBlank()) {
            String cn = state.chineseDescription.isBlank() ? state.englishDescription : state.chineseDescription;
            String en = state.englishDescription.isBlank() ? cn : state.englishDescription;
            code.append(indent).append(".description(\"").append(escape(cn)).append("\", \"").append(escape(en)).append("\")\n");
        }
        String icon = iconExpression(state);
        if (icon != null) code.append(indent).append(".icon(").append(icon).append(")\n");
        appendPrerequisites(state, code, kotlin, indent, managerId);
        appendRequirements(state, code, kotlin, indent);
        code.append(indent).append(".build()");
        if (!kotlin) code.append(';');
        return code.toString();
    }

    private static @Nullable String iconExpression(State state) {
        if (!state.iconFluid.isEmpty()) {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(state.iconFluid.getFluid());
            if (id != null) return "RegistriesUtils.getFluid(\"" + escape(id.toString()) + "\")";
        }
        var stack = state.icon.getStackInSlot(0);
        if (stack.isEmpty()) return null;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? null : "RegistriesUtils.getItem(\"" + escape(id.toString()) + "\")";
    }

    private static void appendPrerequisites(State state, StringBuilder code, boolean kotlin, String indent, String managerId) {
        if (state.prerequisites.isEmpty()) return;
        boolean sameTree = true;
        for (var entry : state.prerequisites) sameTree &= entry.startsWith(managerId + "/");
        code.append(indent).append(".prerequisites(");
        for (int i = 0; i < state.prerequisites.size(); i++) {
            if (i > 0) code.append(", ");
            String entry = state.prerequisites.get(i);
            int slash = entry.indexOf('/');
            String tree = entry.substring(0, slash), node = entry.substring(slash + 1);
            if (sameTree) {
                code.append('"').append(escape(node)).append('"');
            } else {
                code.append("TechTreeManager.getManager(\"").append(escape(tree)).append("\")");
                if (kotlin) code.append("!!");
                code.append(".getNode(\"").append(escape(node)).append("\")");
                if (kotlin) code.append("!!");
            }
        }
        code.append(")\n");
    }

    private static void appendRequirements(State state, StringBuilder code, boolean kotlin, String indent) {
        var eureka = state.eureka.getStackInSlot(0);
        if (state.cwuNeeded <= 0 && state.materials.isEmpty() && eureka.isEmpty()) return;
        String nested = indent + "    ";
        if (kotlin) {
            code.append(indent).append(".requirements(\n").append(nested).append("ResearchRequirements.Builder()\n");
        } else {
            code.append(indent).append(".requirements(new ResearchRequirements.Builder()\n");
        }
        if (state.cwuNeeded > 0) code.append(nested).append(indent).append(".setCWUNeeded(").append(state.cwuNeeded).append("L)\n");
        for (var material : state.materials) {
            String tag = kotlin ? "ResearchTag.TAGS[\"" + escape(material.tag) + "\"]!!" : "ResearchTag.TAGS.get(\"" + escape(material.tag) + "\")";
            code.append(nested).append(indent).append(".addMaterialNeeded(").append(tag).append(", ").append(material.amount).append("L)\n");
        }
        if (!eureka.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(eureka.getItem());
            if (id != null) {
                code.append(nested).append(indent).append(".setEurekaItem(RegistriesUtils.getItem(\"").append(escape(id.toString()))
                        .append("\"), ").append(state.eurekaPercent / 100f).append("F)\n");
            }
        }
        code.append(nested).append(indent).append(".build()").append(kotlin ? "\n" + indent + ")\n" : ")\n");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    private static List<TechTreeManager> managers() {
        var all = TechTreeManager.getManagers();
        var list = new ArrayList<TechTreeManager>(all.size());
        for (int id = 0; id < all.size(); id++) list.add(TechTreeManager.REGISTRY.get(id));
        return list;
    }

    private static List<String> tagNames() {
        var names = new ArrayList<>(ResearchTag.TAGS.keys());
        Collections.sort(names);
        return names;
    }

    /** 编辑中的数据（服务端为准；两端各有一份，客户端那份只用于显示同步下来的值）。 */
    private static final class State {

        private int manager;
        private String nodeId = "";
        private String chineseName = "";
        private String englishName = "";
        private String chineseDescription = "";
        private String englishDescription = "";
        private final ItemStackTransfer icon = new ItemStackTransfer(1);
        private FluidStack iconFluid = FluidStack.EMPTY;
        /// "树 id/节点名"
        private final List<String> prerequisites = new ArrayList<>();
        private long cwuNeeded;
        private final List<Material> materials = new ArrayList<>();
        private final ItemStackTransfer eureka = new ItemStackTransfer(1);
        private int eurekaPercent = 100;
        private boolean kotlin;
    }

    private static final class Material {

        private String tag;
        private long amount;

        private Material(String tag, long amount) {
            this.tag = tag;
            this.amount = amount;
        }
    }
}
