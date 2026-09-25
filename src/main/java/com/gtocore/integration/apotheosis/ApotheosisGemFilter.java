package com.gtocore.integration.apotheosis;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.IconToggle;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.cover.CoverUIs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.vfyjxf.taffy.style.FlexWrap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@DataGeneratorScanned
public class ApotheosisGemFilter implements ItemFilter {

    @Getter
    protected boolean isBlackList;
    @Getter
    protected LootRarity rarity;
    @Getter
    protected DynamicHolder<Gem> gemType;
    protected Consumer<ItemFilter> itemWriter = filter -> {};
    protected Consumer<ItemFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Override
    public int testItemCount(ItemStack itemStack) {
        return test(itemStack) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public Widget createConfigUI() {
        var rarities = UIElement.column(LayoutStyle.AUTO).layout(l -> l.row().gapAll(UISizes.GAP).flexWrap(FlexWrap.WRAP));
        for (var holder : RarityRegistry.INSTANCE.getOrderedRarities()) {
            var r = holder.get();
            var toggle = IconToggle.of(new ItemStackTexture(r.getMaterial()), () -> rarity == r, on -> setRarity(on ? r : null));
            toggle.setHoverTooltips(r.toComponent());
            rarities.addChild(toggle);
        }
        var typeSlot = new PhantomItemSlot(new GemTypeSlot(), 0).xeiPhantom();
        typeSlot.setMaxStackSize(1);
        typeSlot.setClearSlotOnRightClick(true);
        typeSlot.setHoverTooltips(Component.translatable(TYPE_DESC));
        var typeRow = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.GAP).alignCenter())
                .addChildren(CoverUIs.label(TYPE_FILTER_DESC, TYPE_DESC), typeSlot);
        return UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                CoverUIs.controlRow("cover.filter.blacklist.enabled", Switch.of(this::isBlackList, this::setBlackList)),
                TextLine.translatable(LayoutStyle.AUTO, RARITY_DESC).setColor(UITheme.PANEL_TEXT),
                rarities,
                typeRow);
    }

    private void setRarity(@Nullable LootRarity rarity) {
        this.rarity = rarity;
        onUpdated.accept(this);
    }

    private final class GemTypeSlot extends ItemStackTransfer {

        @Nullable
        private DynamicHolder<Gem> shownType;
        private ItemStack shown = ItemStack.EMPTY;

        private GemTypeSlot() {
            super(1);
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (shownType != gemType) {
                shownType = gemType;
                shown = gemType == null || !gemType.isBound() ? ItemStack.EMPTY : GemRegistry.createGemStack(gemType.get(), gemType.get().getMinRarity());
            }
            return shown;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            DynamicHolder<Gem> type = null;
            if (!stack.isEmpty()) {
                var gem = GemItem.getGem(stack);
                if (!gem.isBound()) return;
                type = gem;
            }
            shownType = type;
            shown = type == null ? ItemStack.EMPTY : stack.copyWithCount(1);
            if (type == null ? gemType == null : type.equals(gemType)) return;
            gemType = type;
            onUpdated.accept(ApotheosisGemFilter.this);
        }
    }

    @Override
    public CompoundTag saveFilter() {
        if (!isBlackList && rarity == null && gemType == null) {
            return null;
        }
        var tag = new CompoundTag();
        tag.putBoolean("isBlackList", isBlackList);
        if (rarity != null) {
            tag.putInt("rarity", rarity.ordinal());
        }
        if (gemType != null) {
            tag.putString("gemType", gemType.getId().toString());
        }
        return tag;
    }

    public void setBlackList(boolean blackList) {
        isBlackList = blackList;
        onUpdated.accept(this);
    }

    public static ApotheosisGemFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    private static ApotheosisGemFilter loadFilter(CompoundTag tag, Consumer<ItemFilter> itemWriter) {
        var handler = new ApotheosisGemFilter();
        handler.itemWriter = itemWriter;
        handler.isBlackList = tag.getBoolean("isBlackList");
        if (tag.contains("rarity")) {
            handler.rarity = RarityRegistry.byOrdinal(tag.getInt("rarity")).get();
        }
        if (tag.contains("gemType")) {
            handler.gemType = GemRegistry.INSTANCE.holder(RLUtils.parse(tag.getString("gemType")));
            if (!handler.gemType.isBound()) {
                handler.gemType = null;
            }
        }
        return handler;
    }

    @Override
    public void setOnUpdated(Consumer<ItemFilter> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @Override
    public boolean test(ItemStack gemStack) {
        DynamicHolder<Gem> gem = GemItem.getGem(gemStack);
        if (!gem.isBound()) {
            return isBlackList;
        }
        if (gemType != null && !gemType.equals(gem)) {
            return isBlackList;
        }
        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(gemStack);
        if (this.rarity != null && this.rarity != rarity.get()) {
            return isBlackList;
        }
        return !isBlackList;
    }

    @RegisterLanguage(cn = "过滤稀有度", en = "Filter by rarity")
    public static final String RARITY_DESC = "gtocore.apotheosis_gem_filter.rarity_desc";
    @RegisterLanguage(cn = "过滤宝石品种", en = "Filter by gem type")
    public static final String TYPE_FILTER_DESC = "gtocore.apotheosis_gem_filter.type_filter_desc";
    @RegisterLanguage(cn = "放入宝石来过滤品种，清空则不过滤", en = "Put in a gem to filter by type, or leave empty to not filter by type")
    public static final String TYPE_DESC = "gtocore.apotheosis_gem_filter.type_desc";
}
