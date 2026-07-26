package com.gtocore.api.research.techtree;

import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.techtree.ui.TechTreeAutoLayout;
import com.gtocore.api.research.techtree.ui.TechTreeLayout;

import com.gtolib.GTOCore;
import com.gtolib.api.lang.CNEN;
import com.gtolib.utils.AEChemicalHelper;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistry;

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
import com.gto.datasynclib.util.Registry;
import com.gto.fastcollection.O2OOpenCacheHashMap;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;

public final class TechTreeManager extends GTRegistry.Str<TechNode> implements IOStreamCodec<TechTree> {

    public static final Map<String, CNEN> NODE_LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
    public static final Map<String, CNEN> TREE_LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
    public static final Registry<String, TechTreeManager> REGISTRY = new GTRegistry.Str<>(GTOCore.id("techtree_manager"));

    @Getter
    private final String id;
    @Getter
    private final IGuiTexture icon;
    private @Nullable TechTreeLayout layout;

    public TechTreeManager(String id,
                           String cn,
                           String en,
                           IGuiTexture icon) {
        super(GTOCore.id(id));
        this.id = Objects.requireNonNull(id, "id");
        this.icon = Objects.requireNonNull(icon, "icon");
        if (TREE_LANG != null) {
            TREE_LANG.put("gtocore.techtree." + id, new CNEN(cn, en));
        }
        REGISTRY.register(id, this);
    }

    public static TechTreeManager getManager(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<TechTreeManager> getManagers() {
        return REGISTRY.values();
    }

    public Builder builder(String name, String cn, String en) {
        return new Builder(this, name, cn, en);
    }

    public TechTreeLayout getLayout() {
        TechTreeLayout currentLayout = layout;
        if (currentLayout == null) {
            currentLayout = TechTreeAutoLayout.create(this.values());
            layout = currentLayout;
        }
        return currentLayout;
    }

    TechNode register(Builder builder) {
        String name = builder.name;
        var definition = new TechNode(
                this,
                name,
                builder.icon,
                builder.requirements,
                builder.prerequisites, builder.tier);
        this.register(name, definition);
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
    public TechTree decode(DataIOStream dis, int dataVersion) throws IOException {
        var tree = new TechTree(this);
        var n = dis.readVarInt();
        for (int i = 0; i < n; i++) {
            var d = this.get(dis.readUTF());
            if (d != null) {
                tree.addUnlockedNode(d);
            }
        }
        return tree;
    }

    @Override
    public void encode(DataIOStream stream, TechTree obj) throws IOException {
        var endNodes = obj.getEndNodes();
        stream.writeVarInt(endNodes.size());
        for (var node : endNodes) {
            stream.writeUTF(node.name);
        }
    }

    public TechNode getNode(String name) {
        return this.get(name);
    }

    public Collection<TechNode> getAllNodes() {
        return this.values();
    }

    public static final class Builder {

        private final TechTreeManager manager;
        private final String name;
        private final String cn;
        private final String en;
        private String cnDesc;
        private String enDesc;
        private @Nullable AEKey icon;
        private ResearchRequirements requirements = ResearchRequirements.NO_REQUIREMENTS;
        private ImmutableList<TechNode> prerequisites = ImmutableList.of();
        private int tier = 0;

        private Builder(TechTreeManager manager, String name, String cn, String en) {
            this.manager = Objects.requireNonNull(manager, "manager");
            this.name = Objects.requireNonNull(name, "name");
            this.cn = Objects.requireNonNull(cn, "cn");
            this.en = Objects.requireNonNull(en, "en");
        }

        public Builder icon(@Nullable AEKey icon) {
            this.icon = icon;
            return this;
        }

        public Builder icon(TagPrefix tagPrefix, Material material) {
            this.icon = AEChemicalHelper.getKey(tagPrefix, material);
            return this;
        }

        public Builder icon(ItemLike icon) {
            this.icon = AEItemKey.of(icon);
            return this;
        }

        public Builder icon(ItemStack icon) {
            this.icon = AEItemKey.of(icon);
            return this;
        }

        public Builder icon(Fluid icon) {
            this.icon = AEFluidKey.of(icon);
            return this;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        public Builder requirements(ResearchRequirements requirements) {
            this.requirements = Objects.requireNonNull(requirements, "requirements");
            return this;
        }

        public Builder description(String cnDesc, String enDesc) {
            this.cnDesc = Objects.requireNonNull(cnDesc, "cnDesc");
            this.enDesc = Objects.requireNonNull(enDesc, "enDesc");
            return this;
        }

        public Builder prerequisites(UnaryOperator<ImmutableList.Builder<TechNode>> prerequisites) {
            Objects.requireNonNull(prerequisites, "prerequisites");
            ImmutableList.Builder<TechNode> builder = ImmutableList.builder();
            prerequisites.apply(builder);
            this.prerequisites = builder.build();
            return this;
        }

        @SafeVarargs
        public final Builder prerequisites(TechNode... prerequisites) {
            this.prerequisites = ImmutableList.copyOf(prerequisites);
            return this;
        }

        @SafeVarargs
        public final Builder prerequisites(String... prerequisites) {
            return prerequisites(builder -> {
                for (String name : prerequisites) {
                    var node = manager.getNode(name);
                    if (node == null) {
                        throw new IllegalArgumentException("Prerequisite node " + name + " not found");
                    }
                    builder.add(node);
                }
                return builder;
            });
        }

        public TechNode build() {
            var t = manager.register(this);

            if (requirements instanceof ResearchRequirements r && r.getEurekaItem() != null) {
                ResearchRequirements.registerEurekaRequirement(r.getEurekaItem(), t);
            }
            return t;
        }
    }

    public static MutableComponent getNodeName(TechNode node) {
        return Component.translatable("gtocore.technode." + node.name);
    }

    public static @Nullable MutableComponent getNodeDesc(TechNode node) {
        var key = "gtocore.technode." + node.name + ".desc";
        if (Language.getInstance().has(key)) {
            return Component.translatable("gtocore.technode." + node.name + ".desc");
        }
        return null;
    }

    public static MutableComponent getTreeName(TechTreeManager manager) {
        return Component.translatable("gtocore.techtree." + manager.id);
    }

    public void triggerAllResearchUnlock(UUID team) {
        for (var node : this.values()) {
            TechTreeSavedData.unlock(team, node);
        }
    }
}
