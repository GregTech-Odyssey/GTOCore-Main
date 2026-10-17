package com.gtocore.api.gui.widget;

import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.gtolib.api.emi.stack.EmiTagPrefixUtils;
import com.gtolib.api.emi.stack.EmiTagprefixStack;

import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.UICodecs;
import com.gregtechceu.gtceu.uipro.elements.ae.PhantomAEKeySlot;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.ConfigInventory;

import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import dev.emi.emi.api.stack.EmiIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** 将外部配方草稿接到 GTM 幽灵槽；只补充 GTO 通配符的 EMI 适配与中键清空。 */
public class WildcardAEKeyPhantomSlot extends PhantomAEKeySlot {

    private final RPC<Unit> clear;
    @Nullable
    private TagPrefixKey displayedPrefix;
    private long displayedAmount;
    @Nullable
    private Object displayedIngredient;

    public WildcardAEKeyPhantomSlot(Supplier<GenericStack> getter, Consumer<GenericStack> setter) {
        super(new BoundStacks(getter, setter), 0);
        clear = addRPC(UICodecs.UNIT, (player, ignored) -> {
            if (player != null && !player.isSpectator()) setter.accept(null);
        });
        xeiPhantom();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 2 || !isMouseOverElement(mouseX, mouseY)) return super.mouseClicked(mouseX, mouseY, button);
        if (!isDisabled()) clear.send(Unit.INSTANCE);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Target> getPhantomTargets(Object ingredient) {
        var stack = tagPrefixStack(ingredient);
        if (stack == null) return super.getPhantomTargets(ingredient);
        var targets = super.getPhantomTargets(stack);
        if (targets.isEmpty()) return targets;
        var target = targets.getFirst();
        return Collections.singletonList(new Target() {

            @Override
            public Rect2i getArea() {
                return target.getArea();
            }

            @Override
            public void accept(Object ingredient) {
                var stack = tagPrefixStack(ingredient);
                if (stack != null) target.accept(stack);
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        if (!(getKey() instanceof TagPrefixKey prefix)) return super.getXEIIngredientOverMouse(mouseX, mouseY);
        if (!isMouseOverElement(mouseX, mouseY)) return null;
        long amount = getAmount();
        if (displayedPrefix != prefix || displayedAmount != amount) {
            displayedPrefix = prefix;
            displayedAmount = amount;
            displayedIngredient = EmiTagPrefixUtils.toEmiStack(prefix, amount);
        }
        return displayedIngredient;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private static GenericStack tagPrefixStack(Object ingredient) {
        if (ingredient instanceof EmiTagprefixStack stack && !stack.isEmpty()) {
            var converted = EmiTagPrefixUtils.toGenericStack(stack);
            return converted.amount() > 0 ? converted : new GenericStack(converted.what(), 1);
        }
        if (ingredient instanceof EmiIngredient stacks && !stacks.getEmiStacks().isEmpty()) {
            var first = stacks.getEmiStacks().getFirst();
            if (first != ingredient) return tagPrefixStack(first);
        }
        return null;
    }

    private static final class BoundStacks extends ConfigInventory {

        private final Supplier<GenericStack> getter;
        private final Consumer<GenericStack> setter;

        private BoundStacks(Supplier<GenericStack> getter, Consumer<GenericStack> setter) {
            super(null, Mode.CONFIG_STACKS, 1, null, true);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public @Nullable GenericStack getStack(int slot) {
            return getter.get();
        }

        @Override
        public @Nullable AEKey getKey(int slot) {
            var stack = getStack(slot);
            return stack == null ? null : stack.what();
        }

        @Override
        public long getAmount(int slot) {
            var stack = getStack(slot);
            return stack == null ? 0 : stack.amount();
        }

        @Override
        public void setStack(int slot, @Nullable GenericStack stack) {
            setter.accept(stack == null || stack.amount() <= 0 ? null : stack);
        }
    }
}
