package com.gtocore.api.pattern;

import com.gtocore.api.machine.dynamic.DynamicBlockPattern;
import com.gtocore.api.machine.dynamic.DynamicPartBuilder;
import com.gtocore.api.machine.dynamic.DynamicPartDefinition;

import com.gtolib.utils.MultiBlockFileReader;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class GTOFactoryBlockPattern {

    private final FactoryBlockPattern pattern;
    private final List<String[]> aisles = new ArrayList<>();
    private final List<int[]> repetitions = new ArrayList<>();
    private final Map<String, DynamicPartBuilder> dynamicParts = new LinkedHashMap<>();

    private GTOFactoryBlockPattern(FactoryBlockPattern pattern) {
        this.pattern = pattern;
    }

    public static GTOFactoryBlockPattern start(MultiblockMachineDefinition definition) {
        return new GTOFactoryBlockPattern(FactoryBlockPattern.start(definition));
    }

    public static GTOFactoryBlockPattern start(MultiblockMachineDefinition definition, RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
        return new GTOFactoryBlockPattern(FactoryBlockPattern.start(definition, charDir, stringDir, aisleDir));
    }

    public static GTOFactoryBlockPattern fromFile(MultiblockMachineDefinition definition) {
        return fromFile(definition, definition.getId().getPath());
    }

    public static GTOFactoryBlockPattern fromFile(MultiblockMachineDefinition definition, String name) {
        String path = "pattern/" + name + ".mbs";
        InputStream input = GTOFactoryBlockPattern.class.getClassLoader().getResourceAsStream(path);
        if (input == null) throw new IllegalArgumentException("Missing multiblock pattern: " + path);
        try (input) {
            var data = MultiBlockFileReader.load(input);
            GTOFactoryBlockPattern pattern = start(definition, data.charDir(), data.stringDir(), data.aisleDir());
            for (String[] aisle : data.pattern()) pattern.aisle(aisle);
            return pattern;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public GTOFactoryBlockPattern aisle(String... aisle) {
        if (!dynamicParts.isEmpty()) throw new IllegalStateException("Main aisles must be declared before dynamic parts");
        pattern.aisle(aisle);
        aisles.add(aisle.clone());
        repetitions.add(new int[] { 1, 1 });
        return this;
    }

    public GTOFactoryBlockPattern aisleRepeatable(int minRepeat, int maxRepeat, String... aisle) {
        if (!dynamicParts.isEmpty()) throw new IllegalStateException("Main aisles must be declared before dynamic parts");
        pattern.aisleRepeatable(minRepeat, maxRepeat, aisle);
        aisles.add(aisle.clone());
        repetitions.add(new int[] { minRepeat, maxRepeat });
        return this;
    }

    public GTOFactoryBlockPattern setRepeatable(int minRepeat, int maxRepeat) {
        if (!dynamicParts.isEmpty()) throw new IllegalStateException("Main aisles must be declared before dynamic parts");
        pattern.setRepeatable(minRepeat, maxRepeat);
        repetitions.set(repetitions.size() - 1, new int[] { minRepeat, maxRepeat });
        return this;
    }

    public GTOFactoryBlockPattern setRepeatable(int repeatCount) {
        return setRepeatable(repeatCount, repeatCount);
    }

    public GTOFactoryBlockPattern where(char symbol, TraceabilityPredicate predicate) {
        pattern.where(symbol, predicate);
        return this;
    }

    public GTOFactoryBlockPattern condition(Predicate<MultiblockState> condition) {
        pattern.condition(condition);
        return this;
    }

    public GTOFactoryBlockPattern condition(Predicate<MultiblockState> condition, Component reason) {
        pattern.condition(condition, reason);
        return this;
    }

    public GTOFactoryBlockPattern info(Component info) {
        pattern.info(info);
        return this;
    }

    public GTOFactoryBlockPattern dynamicPart(String name, Consumer<DynamicPartBuilder> consumer) {
        if (dynamicParts.containsKey(name)) throw new IllegalArgumentException("Duplicate dynamic part: " + name);
        DynamicPartBuilder builder = new DynamicPartBuilder(List.copyOf(aisles), List.copyOf(repetitions));
        consumer.accept(builder);
        dynamicParts.put(name, builder);
        return this;
    }

    public BlockPattern build() {
        BlockPattern result = pattern.build();
        if (dynamicParts.isEmpty()) return result;
        Map<String, DynamicPartDefinition> parts = new LinkedHashMap<>();
        dynamicParts.forEach((name, builder) -> parts.put(name, builder.build(name).bind(result)));
        return new DynamicBlockPattern(result, parts);
    }
}
