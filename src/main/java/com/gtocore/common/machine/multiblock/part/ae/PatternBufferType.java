package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.utils.register.MachineRegisterUtils;

import com.gtolib.api.registries.GTOMachineBuilder;
import com.gtolib.api.registries.GTORegistration;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 一种 ME 样板总成的声明：{@link Builder#register()} 注册并返回机器定义，槽数、无线、升级与清缓存按钮随定义一起登记。
 * 机器、升级器与提示都从这里读，不再各处硬编码。
 */
public final class PatternBufferType {

    private static final ObjectArrayList<PatternBufferType> ALL = new ObjectArrayList<>();
    private static final ObjectList<PatternBufferType> ALL_VIEW = ObjectLists.unmodifiable(ALL);
    private static final Reference2ObjectOpenHashMap<MachineDefinition, PatternBufferType> BY_DEFINITION = new Reference2ObjectOpenHashMap<>();

    private final String id;
    private final IntSupplier slots;
    private final boolean wireless;
    private final boolean upgradable;
    private final boolean recipeCacheButtons;
    private MachineDefinition definition;

    private PatternBufferType(Builder builder) {
        this.id = builder.id;
        this.slots = Objects.requireNonNull(builder.slots, builder.id);
        this.wireless = builder.wireless;
        this.upgradable = builder.upgradable;
        this.recipeCacheButtons = builder.recipeCacheButtons;
    }

    public static Builder builder(GTORegistration registrate, String id) {
        return new Builder(registrate, id);
    }

    public static ObjectList<PatternBufferType> all() {
        return ALL_VIEW;
    }

    @Nullable
    public static PatternBufferType of(MachineDefinition definition) {
        return BY_DEFINITION.get(definition);
    }

    public String getId() {
        return id;
    }

    public int getSlots() {
        return slots.getAsInt();
    }

    public boolean isWireless() {
        return wireless;
    }

    public boolean hasRecipeCacheButtons() {
        return recipeCacheButtons;
    }

    public MachineDefinition getDefinition() {
        return definition;
    }

    public boolean canUpgradeTo(PatternBufferType target) {
        return upgradable && target.upgradable && target.getSlots() > getSlots();
    }

    @Override
    public String toString() {
        return id;
    }

    public static final class Builder {

        private final GTORegistration registrate;
        private final String id;
        private String cn;
        private String en;
        private IntSupplier slots;
        private int tier = -1;
        private ResourceLocation texture = GTCEu.id("block/machine/part/me_pattern_buffer");
        private final ObjectArrayList<Supplier<List<Component>>> tooltips = new ObjectArrayList<>();
        private BiFunction<MetaMachineBlockEntity, PatternBufferType, MEPatternBufferPartMachine> machine = MEPatternBufferPartMachine::new;
        private boolean wireless = true;
        private boolean upgradable;
        private boolean recipeCacheButtons = true;

        private Builder(GTORegistration registrate, String id) {
            this.registrate = registrate;
            this.id = id;
        }

        public Builder name(String cn, String en) {
            this.cn = cn;
            this.en = en;
            return this;
        }

        public Builder slots(int slots) {
            this.slots = () -> slots;
            return this;
        }

        public Builder slots(IntSupplier slots) {
            this.slots = slots;
            return this;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder tooltips(Supplier<List<Component>> tooltips) {
            this.tooltips.add(tooltips);
            return this;
        }

        public Builder machine(BiFunction<MetaMachineBlockEntity, PatternBufferType, MEPatternBufferPartMachine> machine) {
            this.machine = machine;
            return this;
        }

        public Builder noWireless() {
            this.wireless = false;
            return this;
        }

        public Builder upgradable() {
            this.upgradable = true;
            return this;
        }

        public Builder noRecipeCacheButtons() {
            this.recipeCacheButtons = false;
            return this;
        }

        public MachineDefinition register() {
            Objects.requireNonNull(cn, id);
            Objects.requireNonNull(en, id);
            if (tier < 0) throw new IllegalStateException("Pattern buffer " + id + " has no tier");
            for (var registered : ALL) {
                if (registered.id.equals(id)) throw new IllegalStateException("Duplicate pattern buffer " + id);
            }
            var type = new PatternBufferType(this);
            var factory = machine;
            GTOMachineBuilder builder;
            if (registrate.gtm) {
                builder = registrate.machine(id, holder -> factory.apply(holder, type)).langValue(en).genLang(cn);
            } else {
                builder = MachineRegisterUtils.machine(id, cn, holder -> factory.apply(holder, type)).langValue(en);
            }
            int machineTier = tier;
            var model = texture;
            builder.tier(machineTier)
                    .allRotation()
                    .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, PartAbility.DUAL_INPUT)
                    .renderer(() -> new OverlayTieredMachineRenderer(machineTier, model));
            for (var tooltip : tooltips) builder.tooltips(tooltip);
            builder.tooltips(GTOMachineTooltips.MePatternHatchTooltips.invoke(type.getSlots()));
            if (wireless) builder.tooltips(GTOMachineTooltips.AutoConnectMETooltips);
            var definition = builder.register();
            type.definition = definition;
            BY_DEFINITION.put(definition, type);
            ALL.add(type);
            return definition;
        }
    }
}
