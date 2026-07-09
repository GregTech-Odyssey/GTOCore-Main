package com.gtocore.api.techtree;

import com.gtocore.api.techtree.ui.TechTreeAutoLayout;
import com.gtocore.api.techtree.ui.TechTreeLayout;

import com.gtolib.api.lang.CNEN;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import com.google.common.collect.ImmutableList;
import com.gto.fastcollection.O2OOpenCacheHashMap;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public final class TechTreeManager<T> implements IOStreamCodec<TechTree<T>> {

    public static final Map<String, CNEN> NODE_LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
    public static final Map<String, CNEN> TREE_LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
    private static final Map<String, TechTreeManager<?>> REGISTRY = new O2OOpenCacheHashMap<>();

    @Getter
    private final String id;
    @Getter
    private final IGuiTexture icon;
    final Map<String, TechNode<T>> definitions = new O2OOpenCacheHashMap<>();
    private @Nullable TechTreeLayout<T> layout;

    public TechTreeManager(String id,
                           String cn,
                           String en,
                           IGuiTexture icon) {
        this.id = Objects.requireNonNull(id, "id");
        this.icon = Objects.requireNonNull(icon, "icon");
        if (TREE_LANG != null) {
            TREE_LANG.put("gtocore.techtree." + id, new CNEN(cn, en));
        }
        synchronized (REGISTRY) {
            var previous = REGISTRY.putIfAbsent(id, this);
            if (previous != null && previous != this) {
                throw new IllegalArgumentException("TechTreeManager with id " + id + " already registered");
            }
        }
    }

    public static TechTreeManager<?> getManager(String id) {
        synchronized (REGISTRY) {
            return REGISTRY.get(id);
        }
    }

    public static Collection<TechTreeManager<?>> getManagers() {
        synchronized (REGISTRY) {
            return java.util.List.copyOf(REGISTRY.values());
        }
    }

    public Builder<T> builder(String name, String cn, String en) {
        return new Builder<>(this, name, cn, en);
    }

    public TechTreeLayout<T> getLayout() {
        TechTreeLayout<T> currentLayout = layout;
        if (currentLayout == null) {
            currentLayout = TechTreeAutoLayout.create(definitions.values());
            layout = currentLayout;
        }
        return currentLayout;
    }

    TechNode<T> register(Builder<T> builder) {
        String name = builder.name;
        if (definitions.containsKey(name)) {
            throw new IllegalArgumentException("Node with name " + name + " already registered");
        }
        var definition = new TechNode<>(
                this,
                name,
                builder.icon,
                builder.requirements,
                builder.prerequisites);
        definitions.put(name, definition);
        layout = null;
        if (NODE_LANG != null) {
            NODE_LANG.put("gtocore.technode." + name, new CNEN(builder.cn, builder.en));
            if (builder.cnDesc != null && builder.enDesc != null) {
                NODE_LANG.put("gtocore.technode." + name + ".desc", new CNEN(builder.cnDesc, builder.enDesc));
            }
        }
        return definition;
    }

    @Override
    public TechTree<T> decode(DataIOStream dis, int dataVersion) throws IOException {
        var tree = new TechTree<>(this);
        var n = dis.readVarInt();
        for (int i = 0; i < n; i++) {
            var d = definitions.get(dis.readUTF());
            if (d != null) {
                tree.addUnlockedNode(d);
            }
        }
        return tree;
    }

    @Override
    public void encode(DataIOStream stream, TechTree<T> obj) throws IOException {
        var endNodes = obj.getEndNodes();
        stream.writeVarInt(endNodes.size());
        for (var node : endNodes) {
            stream.writeUTF(node.name);
        }
    }

    public static final class Builder<T> {

        private final TechTreeManager<T> manager;
        private final String name;
        private final String cn;
        private final String en;
        private String cnDesc;
        private String enDesc;
        private @Nullable AEKey icon;
        private BiFunction<T, UUID, ActionResult> requirements = (ign, ored) -> ActionResult.SUCCESS;
        private ImmutableList<TechNode<T>> prerequisites = ImmutableList.of();

        private Builder(TechTreeManager<T> manager, String name, String cn, String en) {
            this.manager = Objects.requireNonNull(manager, "manager");
            this.name = Objects.requireNonNull(name, "name");
            this.cn = Objects.requireNonNull(cn, "cn");
            this.en = Objects.requireNonNull(en, "en");
        }

        public Builder<T> icon(@Nullable AEKey icon) {
            this.icon = icon;
            return this;
        }

        public Builder<T> icon(ItemLike icon) {
            this.icon = AEItemKey.of(icon);
            return this;
        }

        public Builder<T> icon(ItemStack icon) {
            this.icon = AEItemKey.of(icon);
            return this;
        }

        public Builder<T> icon(Fluid icon) {
            this.icon = AEFluidKey.of(icon);
            return this;
        }

        public Builder<T> requirements(BiFunction<T, UUID, ActionResult> requirements) {
            this.requirements = Objects.requireNonNull(requirements, "requirements");
            return this;
        }

        public Builder<T> description(String cnDesc, String enDesc) {
            this.cnDesc = Objects.requireNonNull(cnDesc, "cnDesc");
            this.enDesc = Objects.requireNonNull(enDesc, "enDesc");
            return this;
        }

        public Builder<T> prerequisites(UnaryOperator<ImmutableList.Builder<TechNode<T>>> prerequisites) {
            Objects.requireNonNull(prerequisites, "prerequisites");
            ImmutableList.Builder<TechNode<T>> builder = ImmutableList.builder();
            prerequisites.apply(builder);
            this.prerequisites = builder.build();
            return this;
        }

        @SafeVarargs
        public final Builder<T> prerequisites(TechNode<T>... prerequisites) {
            this.prerequisites = ImmutableList.copyOf(prerequisites);
            return this;
        }

        public TechNode<T> build() {
            return manager.register(this);
        }
    }

    public static MutableComponent getNodeName(TechNode<?> node) {
        return Component.translatable("gtocore.technode." + node.name);
    }

    public static @Nullable MutableComponent getNodeDesc(TechNode<?> node) {
        var key = "gtocore.technode." + node.name + ".desc";
        if (Language.getInstance().has(key)) {
            return Component.translatable("gtocore.technode." + node.name + ".desc");
        }
        return null;
    }

    public static MutableComponent getTreeName(TechTreeManager<?> manager) {
        return Component.translatable("gtocore.techtree." + manager.id);
    }
}
