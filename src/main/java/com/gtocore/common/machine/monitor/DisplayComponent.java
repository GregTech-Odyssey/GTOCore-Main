package com.gtocore.common.machine.monitor;

import com.gtocore.api.gui.graphic.impl.GTOLineChartClientComponent;
import com.gtocore.api.gui.graphic.impl.GTOLineChartToolTipComponent;
import com.gtocore.api.gui.graphic.impl.GTOProgressClientComponent;
import com.gtocore.api.gui.graphic.impl.GTOProgressToolTipComponent;
import com.gtocore.api.gui.helper.GuiIn3DHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.StackSizeRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DisplayComponent implements IDisplayComponent {

    private final ResourceLocation id;

    private DisplayComponent(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public static class Text extends DisplayComponent {

        private FormattedCharSequence text = FormattedCharSequence.EMPTY;

        private Text(ResourceLocation id) {
            super(id);
        }

        @Override
        public Text setInformation(Object... information) {
            if (information.length > 0 && information[0] instanceof FormattedCharSequence text0) {
                this.text = text0;
            } else {
                this.text = FormattedCharSequence.EMPTY; // Default to empty if no valid text provided
            }
            return this;
        }

        @Override
        public FormattedCharSequence getDisplayValue() {
            return text;
        }

        @Override
        public DisplayType getDisplayType() {
            return DisplayType.STYLED_TEXT;
        }

        @Override
        public int getVisualWidth() {
            var font = Minecraft.getInstance().font;
            return font.width(text);
        }

        @Override
        public int getVisualHeight() {
            var font = Minecraft.getInstance().font;
            return font.lineHeight;
        }
    }

    public static Text text(ResourceLocation id) {
        return new Text(id);
    }

    public static Text text(ResourceLocation id, FormattedCharSequence text) {
        return text(id).setInformation(text);
    }

    public static class ProgressBar extends DisplayComponent {

        private final GTOProgressToolTipComponent toolTipComponent;

        private ProgressBar(ResourceLocation id) {
            super(id);
            this.toolTipComponent = new GTOProgressToolTipComponent(0, "", "", GTOProgressToolTipComponent.DEFAULT_COLOR);
        }

        public ProgressBar setInformation(float progress, String text, int color, int height) {
            return setInformation(progress, height, text, color);
        }

        @Override
        public ProgressBar setInformation(Object... information) {
            if (information.length >= 2 && information[0] instanceof Float progressValue && information[1] instanceof Integer heightValue) {
                if (information.length >= 4 && information[2] instanceof String textValue && information[3] instanceof Integer colorValue) {
                    toolTipComponent.setLabel(textValue);
                    toolTipComponent.setColor(colorValue);
                }
                toolTipComponent.setPercentage(progressValue);
                toolTipComponent.setHeight(heightValue);
            } else {
                toolTipComponent.setPercentage(0);
            }
            return this;
        }

        @Override
        public int getVisualWidth() {
            return toolTipComponent.getWidth();
        }

        @Override
        public int getVisualHeight() {
            return toolTipComponent.getHeight();
        }

        @Override
        public DisplayType getDisplayType() {
            return DisplayType.CUSTOM_RENDERER;
        }

        @Override
        public void renderDisplay(
                                  Manager.GridNetwork network,
                                  BlockEntity blockEntity,
                                  float partialTicks,
                                  PoseStack stack,
                                  MultiBufferSource buffer,
                                  int combinedLight,
                                  int combinedOverlay,
                                  int lastLineX,
                                  int startY) {
            GuiIn3DHelper.renderIn3D(stack, (gui, pose) -> {
                pose.scale(1f, 1f, 0.01f);
                new GTOProgressClientComponent(toolTipComponent).renderImage(Minecraft.getInstance().font, 0, startY, gui);
            });
        }

        @Override
        public void renderGui(GuiGraphics graphics, int x, int y, int width, int height) {
            int previousWidth = toolTipComponent.getWidth();
            int previousHeight = toolTipComponent.getHeight();
            toolTipComponent.setWidth(width);
            toolTipComponent.setHeight(height);
            new GTOProgressClientComponent(toolTipComponent).renderImage(Minecraft.getInstance().font, x, y, graphics);
            toolTipComponent.setWidth(previousWidth);
            toolTipComponent.setHeight(previousHeight);
        }
    }

    public static ProgressBar progressBar(ResourceLocation id, float progress) {
        return progressBar(id, progress, 15);
    }

    public static ProgressBar progressBar(ResourceLocation id, float progress, int height) {
        return new ProgressBar(id).setInformation(progress, height);
    }

    public static ProgressBar progressBar(ResourceLocation id, float progress, String text) {
        return progressBar(id, progress, text, 15);
    }

    public static ProgressBar progressBar(ResourceLocation id, float progress, String text, int height) {
        return new ProgressBar(id).setInformation(progress, text, GTOProgressToolTipComponent.DEFAULT_COLOR, height);
    }

    public static ProgressBar progressBar(ResourceLocation id, float progress, String text, int color, int height) {
        return new ProgressBar(id).setInformation(progress, text, color, height);
    }

    public static class LineChart extends DisplayComponent {

        private final GTOLineChartToolTipComponent toolTipComponent;

        private LineChart(ResourceLocation id) {
            super(id);
            // 初始化一个空的图表数据容器
            this.toolTipComponent = new GTOLineChartToolTipComponent(
                    new ArrayList<>(), // 初始数据为空
                    0xFF2ECC71,      // 默认线条颜色
                    false            // 线图只画轴和曲线
            );
            this.toolTipComponent.setMonitorStyle(true);
        }

        @Override
        public LineChart setInformation(Object... information) {
            // 解析传入的数据
            if (information.length > 0 && information[0] instanceof List) {
                try {
                    // 进行类型安全的转换
                    @SuppressWarnings("unchecked")
                    List<BigInteger> dataList = (List<BigInteger>) information[0];
                    toolTipComponent.setData(dataList);
                } catch (ClassCastException e) {
                    // 如果传入的 List 不是 BigInteger 类型，则清空数据以避免错误
                    toolTipComponent.setData(new ArrayList<>());
                }
            } else {
                toolTipComponent.setData(new ArrayList<>());
            }
            // 未来可以扩展，从 information 中解析更多参数，如颜色、高度等
            return this;
        }

        @Override
        public int getVisualWidth() {
            return toolTipComponent.getWidth();
        }

        @Override
        public int getVisualHeight() {
            return toolTipComponent.getHeight();
        }

        @Override
        public DisplayType getDisplayType() {
            return DisplayType.CUSTOM_RENDERER;
        }

        @Override
        public void renderDisplay(
                                  Manager.GridNetwork network,
                                  BlockEntity blockEntity,
                                  float partialTicks,
                                  PoseStack stack,
                                  MultiBufferSource buffer,
                                  int combinedLight,
                                  int combinedOverlay,
                                  int lastLineX,
                                  int startY) {
            // 与 ProgressBar 类似, 创建渲染器并调用其渲染方法
            GuiIn3DHelper.renderIn3D(stack, (gui, pose) -> {
                pose.scale(1f, 1f, 0.01f);
                new GTOLineChartClientComponent(toolTipComponent).renderImage(Minecraft.getInstance().font, 0, startY, gui);
            });
        }

        @Override
        public void renderGui(GuiGraphics graphics, int x, int y, int width, int height) {
            int previousWidth = toolTipComponent.getWidth();
            int previousHeight = toolTipComponent.getHeight();
            toolTipComponent.setWidth(width);
            toolTipComponent.setHeight(height);
            new GTOLineChartClientComponent(toolTipComponent).renderImage(Minecraft.getInstance().font, x, y, graphics);
            toolTipComponent.setWidth(previousWidth);
            toolTipComponent.setHeight(previousHeight);
        }
    }

    /**
     * 静态工厂方法，用于方便地创建 LineChart 组件.
     */
    public static LineChart lineChart(ResourceLocation id, List<BigInteger> data, int sampleIntervalSeconds, double valueDivisor, String valueUnit, boolean milliValues) {
        var chart = new LineChart(id).setInformation(data);
        chart.toolTipComponent.setSampleIntervalSeconds(sampleIntervalSeconds);
        chart.toolTipComponent.setValueDivisor(valueDivisor);
        chart.toolTipComponent.setValueUnit(valueUnit);
        chart.toolTipComponent.setMilliValues(milliValues);
        return chart;
    }

    public static class TextWithStack extends Text {

        @Nullable
        private GenericStack genericStack; // Optional text to display on the AEGenericStack

        private TextWithStack(ResourceLocation id) {
            super(id);
        }

        @Override
        public TextWithStack setInformation(Object... information) {
            super.setInformation(information);
            if (information.length > 1 && information[1] instanceof GenericStack(AEKey what, long amount)) {
                this.genericStack = new GenericStack(what, amount);
            }
            return this;
        }

        @Override
        public void renderDisplay(Manager.GridNetwork network, BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, int lastLineX, int startY) {
            if (genericStack != null) {
                GuiIn3DHelper.renderIn3D(
                        stack, (guiGraphics, pose) -> {

                            pose.scale(1f, 1f, 1e-3f);
                            AEKeyRendering.drawInGui(
                                    Minecraft.getInstance(),
                                    guiGraphics,
                                    super.getVisualWidth() + 2,
                                    startY,
                                    genericStack.what());
                            pose.translate(0, 0, 1e-3f);
                            if (genericStack.amount() > 0) {
                                String amtText = genericStack.what().formatAmount(genericStack.amount(), AmountFormat.SLOT);
                                StackSizeRenderer.renderSizeLabel(guiGraphics, Minecraft.getInstance().font,
                                        super.getVisualWidth() + 2,
                                        startY, amtText, false);
                            }
                        });

            }
        }

        @Override
        public void renderGui(GuiGraphics graphics, int x, int y, int width, int height) {
            super.renderGui(graphics, x, y, width, height);
            if (genericStack != null) {
                AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, x + Math.max(0, width - 16), y, genericStack.what());
            }
        }

        @Override
        public DisplayType getDisplayType() {
            return DisplayType.BOTH;
        }

        @Override
        public int getVisualWidth() {
            return super.getVisualWidth() + (genericStack != null ? 18 : 0); // Add space for stack icon if present
        }

        @Override
        public int getVisualHeight() {
            var iconHeight = genericStack != null ? 16 : 0; // Icon height if stack is present
            return Math.max(super.getVisualHeight(), iconHeight);
        }
    }

    public static Text textWithStack(ResourceLocation id, FormattedCharSequence text, @Nullable GenericStack stack) {
        if (stack == null) {
            return text(id, text);
        }
        return new TextWithStack(id)
                .setInformation(text, stack);
    }

    public static IDisplayComponent withKey(IDisplayComponent component, AEKey key) {
        return new Keyed(component, key);
    }

    public static IDisplayComponent topology(ResourceLocation id, Component[] labels, int[] edges) {
        return new Topology(id, labels, edges);
    }

    public static IDisplayComponent machineStatus(ResourceLocation id, Component[] lines, float progress) {
        return new MachineStatus(id, lines, progress);
    }

    public static IDisplayComponent paragraph(ResourceLocation id, Component content, int fontSize, int horizontalAlignment, int verticalAlignment) {
        return new Paragraph(id, content, fontSize, horizontalAlignment, verticalAlignment);
    }

    private static final class Paragraph extends DisplayComponent {

        private final Component content;
        private final int fontSize;
        private final int horizontalAlignment;
        private final int verticalAlignment;
        private int wrappedWidth = -1;
        private List<FormattedCharSequence> lines = Collections.emptyList();

        private Paragraph(ResourceLocation id, Component content, int fontSize, int horizontalAlignment, int verticalAlignment) {
            super(id);
            this.content = content;
            this.fontSize = fontSize;
            this.horizontalAlignment = horizontalAlignment;
            this.verticalAlignment = verticalAlignment;
        }

        @Override
        public IDisplayComponent setInformation(Object... information) {
            return this;
        }

        @Override
        public DisplayType getDisplayType() {
            return DisplayType.CUSTOM_RENDERER;
        }

        @Override
        public int getVisualWidth() {
            return 160;
        }

        @Override
        public int getVisualHeight() {
            return 48;
        }

        @Override
        public void renderGui(GuiGraphics graphics, int x, int y, int width, int height) {
            var font = Minecraft.getInstance().font;
            float scale = fontSize / (float) font.lineHeight;
            int textWidth = Math.max(1, (int) (width / scale));
            int textHeight = (int) (height / scale);
            if (wrappedWidth != textWidth) {
                lines = font.split(content, textWidth);
                wrappedWidth = textWidth;
            }
            int lineSpacing = font.lineHeight + 2;
            int lineCount = Math.min(lines.size(), (textHeight + 2) / lineSpacing);
            if (lineCount == 0) return;
            int blockHeight = lineCount * lineSpacing - 2;
            int lineY = (textHeight - blockHeight) * verticalAlignment / 2;
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(scale, scale, 1);
            for (int i = 0; i < lineCount; i++) {
                var line = lines.get(i);
                int lineX = Math.max(0, textWidth - font.width(line)) * horizontalAlignment / 2;
                graphics.drawString(font, line, lineX, lineY, 0xFFFFFFFF, false);
                lineY += lineSpacing;
            }
            graphics.pose().popPose();
        }
    }

    private static final class MachineStatus extends DisplayComponent {

        private final Component[] lines;
        private final float progress;

        private MachineStatus(ResourceLocation id, Component[] lines, float progress) {
            super(id);
            this.lines = lines;
            this.progress = progress;
        }

        @Override
        public IDisplayComponent setInformation(Object... information) {
            return this;
        }

        @Override
        public DisplayType getDisplayType() {
            return DisplayType.CUSTOM_RENDERER;
        }

        @Override
        public int getVisualWidth() {
            return 200;
        }

        @Override
        public int getVisualHeight() {
            return 148;
        }

        @Override
        public void renderGui(GuiGraphics graphics, int x, int y, int width, int height) {
            var font = Minecraft.getInstance().font;
            int lineY = y;
            for (int i = 0; i < lines.length && lineY + font.lineHeight <= y + height; i++) {
                Component line = lines[i];
                if (line == null || line.getString().isEmpty()) continue;
                if (i == 2) {
                    graphics.fill(x, lineY, x + width, lineY + 14, 0xFF7D838A);
                    graphics.fill(x + 1, lineY + 1, x + width - 1, lineY + 13, 0xFF252A30);
                    graphics.fill(x + 1, lineY + 11, x + 1 + Math.round((width - 2) * Math.max(0, Math.min(1, progress))), lineY + 13, 0xFF61ABDF);
                    graphics.drawString(font, line, x + 3, lineY + 2, 0xFFE6E8EA, false);
                    lineY += 17;
                } else {
                    graphics.drawString(font, line, x + 2, lineY, 0xFFE6E8EA, false);
                    lineY += font.lineHeight + 3;
                }
            }
        }
    }

    private static final class Topology extends DisplayComponent {

        private final Component[] labels;
        private final int[] edges;

        private Topology(ResourceLocation id, Component[] labels, int[] edges) {
            super(id);
            this.labels = labels;
            this.edges = edges;
        }

        @Override
        public IDisplayComponent setInformation(Object... information) {
            return this;
        }

        @Override
        public DisplayType getDisplayType() {
            return DisplayType.CUSTOM_RENDERER;
        }

        @Override
        public int getVisualWidth() {
            return 200;
        }

        @Override
        public int getVisualHeight() {
            return 120;
        }

        @Override
        public void renderGui(GuiGraphics graphics, int x, int y, int width, int height) {
            if (labels.length == 0) return;
            int columns = (int) Math.ceil(Math.sqrt(labels.length * width / (double) Math.max(1, height)));
            columns = Math.max(1, columns);
            int rows = (labels.length + columns - 1) / columns;
            int cellWidth = Math.max(1, width / columns);
            int cellHeight = Math.max(1, height / rows);
            for (int i = 0; i + 1 < edges.length; i += 2) {
                int a = edges[i], b = edges[i + 1];
                if (a < 0 || b < 0 || a >= labels.length || b >= labels.length) continue;
                int ax = x + a % columns * cellWidth + cellWidth / 2;
                int ay = y + a / columns * cellHeight + cellHeight / 2;
                int bx = x + b % columns * cellWidth + cellWidth / 2;
                int by = y + b / columns * cellHeight + cellHeight / 2;
                int middle = (ax + bx) / 2;
                graphics.hLine(Math.min(ax, middle), Math.max(ax, middle), ay, 0xFF6389BA);
                graphics.vLine(middle, Math.min(ay, by), Math.max(ay, by), 0xFF6389BA);
                graphics.hLine(Math.min(middle, bx), Math.max(middle, bx), by, 0xFF6389BA);
            }
            var font = Minecraft.getInstance().font;
            for (int i = 0; i < labels.length; i++) {
                int nx = x + i % columns * cellWidth + cellWidth / 2;
                int ny = y + i / columns * cellHeight + cellHeight / 2;
                graphics.fill(nx - 3, ny - 3, nx + 4, ny + 4, 0xFF7EAAE8);
                if (cellWidth >= 48 && cellHeight >= 25) {
                    graphics.drawString(font, font.plainSubstrByWidth(labels[i].getString(), cellWidth - 2),
                            nx - cellWidth / 2 + 1, ny + 5, 0xFFE2E9F2, false);
                }
            }
        }
    }

    private static final class Keyed extends DisplayComponent {

        private final IDisplayComponent content;
        private final AEKey key;

        private Keyed(IDisplayComponent content, AEKey key) {
            super(content.getId());
            this.content = content;
            this.key = key;
        }

        @Override
        public IDisplayComponent setInformation(Object... information) {
            content.setInformation(information);
            return this;
        }

        @Override
        public DisplayType getDisplayType() {
            return DisplayType.CUSTOM_RENDERER;
        }

        @Override
        public int getVisualWidth() {
            return content.getVisualWidth() + 20;
        }

        @Override
        public int getVisualHeight() {
            return Math.max(16, content.getVisualHeight());
        }

        @Override
        public void renderGui(GuiGraphics graphics, int x, int y, int width, int height) {
            AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, x, y, key);
            graphics.flush();
            content.renderGui(graphics, x + 20, y, Math.max(1, width - 20), height);
        }
    }
}
