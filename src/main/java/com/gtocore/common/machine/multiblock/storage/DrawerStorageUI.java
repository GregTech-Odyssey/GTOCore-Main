package com.gtocore.common.machine.multiblock.storage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

/**
 * 抽屉存储器的存储界面：表头是种类 / 每种类容量 / 密封等级 × 升级倍率，下面是每行一个键的文本
 * ——{@code 名称  数量 / 每种类容量}。
 * <p>
 * 全部是 {@link LabelWidget}：文本由服务端在内容变化时下发，客户端只管显示，既不自己算也不自己画。
 * 行数固定预建，超出可见区的靠滚动看；每行的文本在内容版本变化时重建一次，之后每 tick 只是字符串比较。
 */
public final class DrawerStorageUI {

    /// 内容宽度（与窗口内容区等宽）与位置
    public static final int WIDTH = 162;
    public static final int X = 7;
    public static final int Y = 5;

    /// 一行文本的高度
    private static final int ROW_HEIGHT = 10;
    /// 列表起始行与可见高度（12 行）
    private static final int LIST_TOP = 24;
    private static final int LIST_HEIGHT = ROW_HEIGHT * 12;
    /// 预建的行数，超出可见区的靠滚动看
    private static final int ROWS = 64;
    /// 名称超过这么多字符就截断，免得挤掉后面的数量
    private static final int NAME_LIMIT = 14;

    private DrawerStorageUI() {}

    /** 整块内容（表头 + 可滚动文本列表），位置为 {@link #X}/{@link #Y}。 */
    public static WidgetGroup create(DrawerStorageMachine machine) {
        var root = new WidgetGroup(X, Y, WIDTH, LIST_TOP + LIST_HEIGHT);
        addHeader(root, machine);
        var scroll = new DraggableScrollableWidgetGroup(0, LIST_TOP, WIDTH, LIST_HEIGHT)
                .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON)
                .setYScrollBarWidth(6);
        scroll.addWidget(new StorageListText(machine, WIDTH - 8));
        root.addWidget(scroll);
        return root;
    }

    /** 表头：种类 / 每种类容量 / 密封等级 × 升级倍率。 */
    private static void addHeader(WidgetGroup root, DrawerStorageMachine machine) {
        root.addWidget(new LabelWidget(0, 0, () -> Component.translatable(DrawerStorageMachine.TYPES,
                FormattingUtil.formatNumbers(machine.getKeyMap().size()),
                FormattingUtil.formatNumbers(machine.getTypes())).getString()));
        root.addWidget(new LabelWidget(0, 11, () -> Component.translatable(DrawerStorageMachine.CAPACITY_PER_TYPE,
                FormattingUtil.formatNumbers(machine.getPerTypeCapacity())).getString()));
        root.addWidget(new LabelWidget(90, 11, () -> Component.translatable(DrawerStorageMachine.HERMETIC_UPGRADE,
                FormattingUtil.formatNumbers(machine.getHermeticLevel()),
                FormattingUtil.formatNumbers(machine.getUpgradeMultiplier())).getString()));
    }

    /** 纯文本列表：每行一个 {@link LabelWidget}，内容版本变了才重建这一批文本。 */
    private static final class StorageListText extends WidgetGroup {

        private final DrawerStorageMachine machine;
        private final String[] lines = new String[ROWS];
        private int lastVersion = -1;

        private StorageListText(DrawerStorageMachine machine, int width) {
            super(0, 0, Math.max(1, width), ROWS * ROW_HEIGHT);
            this.machine = machine;
            for (int i = 0; i < ROWS; i++) {
                int index = i;
                addWidget(new LabelWidget(0, i * ROW_HEIGHT, () -> line(index)).setDropShadow(false));
            }
        }

        /** 第 {@code index} 行的文本；空存储时第一行给提示。 */
        private String line(int index) {
            if (machine.getDisplayVersion() != lastVersion) {
                lastVersion = machine.getDisplayVersion();
                rebuildLines();
            }
            return index >= 0 && index < ROWS ? lines[index] : "";
        }

        private void rebuildLines() {
            if (machine.getKeyMap().isEmpty()) {
                lines[0] = Component.translatable(DrawerStorageMachine.EMPTY).getString();
                for (int i = 1; i < ROWS; i++) {
                    lines[i] = "";
                }
                return;
            }
            long capacity = machine.getPerTypeCapacity();
            for (int i = 0; i < ROWS; i++) {
                var key = machine.displayKeyAt(i);
                if (key == null) {
                    lines[i] = "";
                    continue;
                }
                var name = key.getDisplayName().getString();
                if (name.length() > NAME_LIMIT) name = name.substring(0, NAME_LIMIT - 1) + "…";
                lines[i] = name + "  " + FormattingUtil.formatNumbers(machine.getKeyMap().getAmount(key)) +
                        " / " + FormattingUtil.formatNumbers(capacity);
            }
        }
    }
}
