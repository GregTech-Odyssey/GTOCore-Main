package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@DataGeneratorScanned
public final class TechNodeLine extends StatusLine {

    @RegisterLanguage(cn = "点击在科技树中定位", en = "Click to locate it in the Tech Tree")
    public static final String LOCATE = "gtocore.techtree.link.locate";
    private static final Component NONE = Component.literal("—");

    private final SyncValue<Integer> code;
    @Nullable
    private final Consumer<TechNode> clientTarget;

    private TechNodeLine(String labelKey, Supplier<@Nullable TechNode> node, @Nullable Consumer<TechNode> clientTarget,
                         @Nullable BiConsumer<Player, TechNode> serverTarget) {
        super(LayoutStyle.AUTO, Component.translatable(labelKey), nameOf(node));
        this.clientTarget = clientTarget;
        this.code = addSyncValue(SyncValue.ofInt(() -> {
            var current = node.get();
            return current == null ? -1 : TechTreeView.encodeNode(current);
        }, -1));
        onClick(Component.translatable(LOCATE).withStyle(ChatFormatting.GRAY), () -> node.get() != null, player -> {
            var current = node.get();
            if (current != null && serverTarget != null) serverTarget.accept(player, current);
        });
    }

    public static TechNodeLine client(String labelKey, Supplier<@Nullable TechNode> node, Consumer<TechNode> navigate) {
        return new TechNodeLine(labelKey, node, navigate, null);
    }

    public static TechNodeLine server(String labelKey, Supplier<@Nullable TechNode> node, BiConsumer<Player, TechNode> open) {
        return new TechNodeLine(labelKey, node, null, open);
    }

    private static Supplier<Component> nameOf(Supplier<@Nullable TechNode> node) {
        var last = new TechNode[1];
        var text = new Component[] { NONE };
        return () -> {
            var current = node.get();
            if (current != last[0]) {
                last[0] = current;
                text[0] = current == null ? NONE : current.getDisplayName();
            }
            return text[0];
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clientTarget != null && button == 0 && isMouseOverElement(mouseX, mouseY)) {
            var node = TechTreeView.decodeNode(code.getValue());
            if (node != null) {
                playButtonClickSound();
                clientTarget.accept(node);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
