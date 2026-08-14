package com.gtocore.api.machine.dynamic;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class DynamicPartBuilder {

    private final List<String[]> source;
    private final List<int[]> repetitions;
    private final List<String[]> masks = new ArrayList<>();
    private final BitSet selectedAisles = new BitSet();
    private float pivotX;
    private float pivotY;
    private float pivotZ;
    private DynamicMotion motion = DynamicMotion.NONE;

    public DynamicPartBuilder(List<String[]> source, List<int[]> repetitions) {
        this.source = source;
        this.repetitions = repetitions;
    }

    public DynamicPartBuilder aisle(String... aisle) {
        return aisle(masks.size(), aisle);
    }

    public DynamicPartBuilder aisle(int index, String... aisle) {
        if (index < 0 || index >= source.size()) throw new IllegalArgumentException("Invalid dynamic aisle: " + index);
        while (masks.size() <= index) masks.add(null);
        if (masks.get(index) != null) throw new IllegalArgumentException("Duplicate dynamic aisle: " + index);
        masks.set(index, aisle.clone());
        return this;
    }

    public DynamicPartBuilder selectAisles(int fromInclusive, int toExclusive) {
        if (fromInclusive < 0 || fromInclusive >= toExclusive || toExclusive > source.size()) {
            throw new IllegalArgumentException("Invalid dynamic aisle range: " + fromInclusive + ".." + toExclusive);
        }
        selectedAisles.set(fromInclusive, toExclusive);
        return this;
    }

    public DynamicPartBuilder selectRows(int fromAisle, int toAisle, int fromRow, int toRow) {
        validateAisles(fromAisle, toAisle);
        for (int aisle = fromAisle; aisle < toAisle; aisle++) {
            String[] sourceAisle = source.get(aisle);
            if (fromRow < 0 || fromRow >= toRow || toRow > sourceAisle.length) {
                throw new IllegalArgumentException("Invalid dynamic row range: " + fromRow + ".." + toRow);
            }
            for (int row = fromRow; row < toRow; row++) {
                for (int column = 0; column < sourceAisle[row].length(); column++) {
                    if (sourceAisle[row].charAt(column) != ' ') select(aisle, row, column);
                }
            }
        }
        return this;
    }

    public DynamicPartBuilder selectSymbols(int fromAisle, int toAisle, int fromRow, int toRow, int fromColumn, int toColumn, char... symbols) {
        validateAisles(fromAisle, toAisle);
        if (symbols.length == 0) throw new IllegalArgumentException("Missing dynamic symbols");
        for (int aisle = fromAisle; aisle < toAisle; aisle++) {
            String[] sourceAisle = source.get(aisle);
            if (fromRow < 0 || fromRow >= toRow || toRow > sourceAisle.length) {
                throw new IllegalArgumentException("Invalid dynamic row range: " + fromRow + ".." + toRow);
            }
            for (int row = fromRow; row < toRow; row++) {
                String sourceRow = sourceAisle[row];
                if (fromColumn < 0 || fromColumn >= toColumn || toColumn > sourceRow.length()) {
                    throw new IllegalArgumentException("Invalid dynamic column range: " + fromColumn + ".." + toColumn);
                }
                for (int column = fromColumn; column < toColumn; column++) {
                    char symbol = sourceRow.charAt(column);
                    for (char selected : symbols) {
                        if (symbol != selected || symbol == ' ') continue;
                        select(aisle, row, column);
                        break;
                    }
                }
            }
        }
        return this;
    }

    private void validateAisles(int fromAisle, int toAisle) {
        if (fromAisle < 0 || fromAisle >= toAisle || toAisle > source.size()) {
            throw new IllegalArgumentException("Invalid dynamic aisle range: " + fromAisle + ".." + toAisle);
        }
    }

    private void select(int aisle, int row, int column) {
        while (masks.size() <= aisle) masks.add(null);
        String[] mask = masks.get(aisle);
        if (mask == null) {
            String[] sourceAisle = source.get(aisle);
            mask = new String[sourceAisle.length];
            for (int index = 0; index < sourceAisle.length; index++) {
                mask[index] = " ".repeat(sourceAisle[index].length());
            }
            masks.set(aisle, mask);
        }
        StringBuilder selected = new StringBuilder(mask[row]);
        selected.setCharAt(column, source.get(aisle)[row].charAt(column));
        mask[row] = selected.toString();
    }

    public DynamicPartBuilder pivot(float x, float y, float z) {
        pivotX = x;
        pivotY = y;
        pivotZ = z;
        return this;
    }

    public DynamicPartBuilder motion(DynamicMotion motion) {
        this.motion = motion;
        return this;
    }

    public DynamicPartDefinition build(String name) {
        boolean[][][] selected = new boolean[source.size()][][];
        int minAisle = Integer.MAX_VALUE, minRow = Integer.MAX_VALUE, minColumn = Integer.MAX_VALUE;
        int maxAisle = -1, maxRow = -1, maxColumn = -1;

        for (int aisle = 0; aisle < source.size(); aisle++) {
            String[] sourceAisle = source.get(aisle);
            selected[aisle] = new boolean[sourceAisle.length][];
            String[] mask = aisle < masks.size() ? masks.get(aisle) : null;
            if (mask != null && mask.length != sourceAisle.length) {
                throw new IllegalArgumentException("Dynamic aisle " + aisle + " has a different height");
            }
            for (int row = 0; row < sourceAisle.length; row++) {
                String sourceRow = sourceAisle[row];
                selected[aisle][row] = new boolean[sourceRow.length()];
                if (mask != null && mask[row].length() != sourceRow.length()) {
                    throw new IllegalArgumentException("Dynamic aisle " + aisle + " row " + row + " has a different width");
                }
                for (int column = 0; column < sourceRow.length(); column++) {
                    boolean include = selectedAisles.get(aisle) && sourceRow.charAt(column) != ' ';
                    if (mask != null && mask[row].charAt(column) != ' ') {
                        if (sourceRow.charAt(column) == ' ') {
                            throw new IllegalArgumentException("Dynamic part " + name + " selects an empty block");
                        }
                        include = true;
                    }
                    if (!include) continue;
                    if (repetitions.get(aisle)[0] != 1 || repetitions.get(aisle)[1] != 1) {
                        throw new IllegalArgumentException("Dynamic parts do not support repeatable aisles");
                    }
                    selected[aisle][row][column] = true;
                    minAisle = Math.min(minAisle, aisle);
                    minRow = Math.min(minRow, row);
                    minColumn = Math.min(minColumn, column);
                    maxAisle = Math.max(maxAisle, aisle);
                    maxRow = Math.max(maxRow, row);
                    maxColumn = Math.max(maxColumn, column);
                }
            }
        }
        if (maxAisle < 0) throw new IllegalArgumentException("Dynamic part " + name + " is empty");

        String[][] structure = new String[maxAisle - minAisle + 1][];
        for (int aisle = minAisle; aisle <= maxAisle; aisle++) {
            structure[aisle - minAisle] = new String[maxRow - minRow + 1];
            for (int row = minRow; row <= maxRow; row++) {
                StringBuilder result = new StringBuilder(maxColumn - minColumn + 1);
                for (int column = minColumn; column <= maxColumn; column++) {
                    result.append(selected[aisle][row][column] ? source.get(aisle)[row].charAt(column) : ' ');
                }
                structure[aisle - minAisle][row - minRow] = result.toString();
            }
        }
        return new DynamicPartDefinition(name, structure, minAisle, minRow, minColumn, pivotX, pivotY, pivotZ, motion);
    }
}
