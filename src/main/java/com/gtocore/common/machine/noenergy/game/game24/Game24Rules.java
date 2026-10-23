package com.gtocore.common.machine.noenergy.game.game24;

import net.minecraft.util.RandomSource;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;

/**
 * 「24点」的规则：{@link #NUMBER_COUNT} 张 {@link #VALUE_MIN}~{@link #VALUE_MAX} 的数字牌，用 + − × ÷ 与括号把它们
 * 各用一次拼成 {@link #TARGET}。
 *
 * <p>
 * 玩家拼出来的表达式编码成 {@code int[]}：前 {@link #NUMBER_COUNT} 个编码是「第几张牌」的下标（0~3，所以两张同样的牌
 * 也能分清），其余是 {@link #ADD}~{@link #RIGHT}，符号字形见 {@link #TOKEN_TEXT}。
 *
 * <p>
 * 求值全程用精确有理数（{@link Frac}）而不是浮点：牌面与运算都只是若干个小整数的乘除加减，分子分母远不到 {@code long}
 * 的量级，所以既不会溢出，也不需要参考实现里那个 epsilon 容差。
 *
 * <p>
 * 纯逻辑，不持有机器状态；只在机器所在的服务端线程调用，不要求线程安全，临时容器按实例复用。
 */
public final class Game24Rules {

    /** 数字牌张数。 */
    public static final int NUMBER_COUNT = 4;
    /** 牌面下限。 */
    public static final int VALUE_MIN = 1;
    public static final int TARGET = 24;
    /** 表达式长度上限：4 张牌 + 3 个运算符 + 3 对括号。 */
    public static final int MAX_TOKENS = NUMBER_COUNT * 2 - 1 + 6;

    /**
     * 抽牌范围：1~10 只用小数字，1~13 是完整的 A~K。范围越大越容易摸到 11~13 这类不好凑 24 的牌，所以
     * {@link #solveReward()} 稍高一点。
     */
    public enum DrawRange {

        ONE_TO_TEN(10, 2),
        ONE_TO_THIRTEEN(13, 3);

        public static final DrawRange[] ALL = values();

        private final int max;
        private final long solveReward;

        DrawRange(int max, long solveReward) {
            this.max = max;
            this.solveReward = solveReward;
        }

        public int min() {
            return VALUE_MIN;
        }

        public int max() {
            return max;
        }

        /** 答对一题给多少游戏币（用过提示另有折扣）。 */
        public long solveReward() {
            return solveReward;
        }

        /** 按序数取范围，越界返回 {@code null}（存档与网络来的值一律校验）。 */
        public static @Nullable DrawRange of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    /** 运算符与括号的编码；{@code 0 ~ NUMBER_COUNT - 1} 留给数字牌下标。 */
    public static final int ADD = NUMBER_COUNT;
    public static final int SUB = NUMBER_COUNT + 1;
    public static final int MUL = NUMBER_COUNT + 2;
    public static final int DIV = NUMBER_COUNT + 3;
    public static final int LEFT = NUMBER_COUNT + 4;
    public static final int RIGHT = NUMBER_COUNT + 5;
    public static final int TOKEN_COUNT = NUMBER_COUNT + 6;

    /** 非数字 token 的字形，下标是 {@code token - ADD}；加减用真正的减号，和按钮上的字一致。 */
    private static final String[] TOKEN_TEXT = { "+", "−", "×", "÷", "(", ")" };

    private Game24Rules() {}

    /** 机器持有一份实例，临时容器跟着它复用。 */
    public static Game24Rules create() {
        return new Game24Rules();
    }

    /** 是否是合法 token。 */
    public static boolean isToken(int token) {
        return token >= 0 && token < TOKEN_COUNT;
    }

    /** 是否是数字牌（占用第 {@code token} 张牌）。 */
    public static boolean isNumberToken(int token) {
        return token >= 0 && token < NUMBER_COUNT;
    }

    public static boolean isOperator(int token) {
        return token >= ADD && token <= DIV;
    }

    /** 非数字 token 的字形，数字牌返回 {@code null}（牌面要按下标去牌堆里取）。 */
    public static @Nullable String tokenText(int token) {
        return isToken(token) && !isNumberToken(token) ? TOKEN_TEXT[token - ADD] : null;
    }

    /** 发一局：按 {@code range} 随机 4 张牌，重摇到有解为止（和参考实现一样保证玩家不会拿到死局）。 */
    public int[] deal(RandomSource random, DrawRange range) {
        int[] numbers = new int[NUMBER_COUNT];
        int span = range.max() - range.min() + 1;
        do {
            for (int index = 0; index < NUMBER_COUNT; index++) {
                numbers[index] = range.min() + random.nextInt(span);
            }
        } while (hint(numbers).isEmpty());
        return numbers;
    }

    /**
     * 语法：括号配对、运算符不在首尾也不连排、数字与括号不并列（不认隐式乘法）；空表达式不算数。
     */
    public static boolean isValidSyntax(int[] tokens) {
        if (tokens.length == 0 || tokens.length > MAX_TOKENS) return false;
        boolean expectOperand = true;
        int depth = 0;
        for (int token : tokens) {
            if (isNumberToken(token)) {
                if (!expectOperand) return false;
                expectOperand = false;
            } else if (token == LEFT) {
                if (!expectOperand) return false;
                depth++;
            } else if (token == RIGHT) {
                if (expectOperand || depth == 0) return false;
                depth--;
            } else if (isOperator(token)) {
                if (expectOperand) return false;
                expectOperand = true;
            } else {
                return false;
            }
        }
        return depth == 0 && !expectOperand;
    }

    /** 是不是本题的正确答案：语法过关、4 张牌各用一次、且精确等于 {@link #TARGET}。 */
    public boolean isSolution(int[] tokens, int[] numbers) {
        if (numbers.length != NUMBER_COUNT || !isValidSyntax(tokens)) return false;
        int used = 0;
        for (int token : tokens) {
            if (!isNumberToken(token)) continue;
            if ((used & (1 << token)) != 0) return false;
            used |= 1 << token;
        }
        if (used != (1 << NUMBER_COUNT) - 1) return false;
        Frac value = evaluate(tokens, numbers);
        return value != null && value.isTarget();
    }

    /**
     * 一个解法表达式（形如 {@code (3 + 5) × (4 − 1)}）；同一组牌每次给同一个解，无解时返回空串。
     *
     * <p>
     * 加减的结果上下都套括号，所以拼出来的字符串本身不受运算符优先级影响；乘除按参考实现的写法不套括号。
     */
    public String hint(int[] numbers) {
        if (numbers.length != NUMBER_COUNT) return "";
        Frac[] values = new Frac[NUMBER_COUNT];
        String[] texts = new String[NUMBER_COUNT];
        for (int index = 0; index < NUMBER_COUNT; index++) {
            values[index] = Frac.ofValue(numbers[index]);
            texts[index] = Integer.toString(numbers[index]);
        }
        return solve(values, texts, NUMBER_COUNT);
    }

    /**
     * 每次挑两张牌、按六种组合（a+b、a−b、b−a、a×b、a÷b、b÷a）合成一张再递归。
     *
     * <p>
     * 和参考实现一样把当前最后一张挪到 {@code j}、结果写回 {@code i}，递归只碰前 {@code count - 1} 张；
     * 每层退出前把 {@code i}、{@code j} 原位复原，因此同一层的其它组合看到的是原始牌面。
     */
    private static String solve(Frac[] values, String[] texts, int count) {
        if (count == 1) return values[0].isTarget() ? texts[0] : "";
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                Frac left = values[i];
                Frac right = values[j];
                String leftText = texts[i];
                String rightText = texts[j];
                values[j] = values[count - 1];
                texts[j] = texts[count - 1];
                String found = "";
                for (int combination = 0; combination < 6 && found.isEmpty(); combination++) {
                    Frac value = combine(left, right, combination);
                    if (value == null) continue;
                    values[i] = value;
                    texts[i] = combineText(leftText, rightText, combination);
                    found = solve(values, texts, count - 1);
                }
                values[i] = left;
                texts[i] = leftText;
                values[j] = right;
                texts[j] = rightText;
                if (!found.isEmpty()) return found;
            }
        }
        return "";
    }

    /** 两张牌的第 {@code combination} 种合成结果；除零（除数那侧是 0）返回 {@code null}。 */
    private static @Nullable Frac combine(Frac left, Frac right, int combination) {
        return switch (combination) {
            case 0 -> left.add(right);
            case 1 -> left.sub(right);
            case 2 -> right.sub(left);
            case 3 -> left.mul(right);
            case 4 -> left.div(right);
            default -> right.div(left);
        };
    }

    /** 与 {@link #combine} 对应的表达式片段：加减套括号，乘除不套（两边若本身带括号也仍然正确）。 */
    private static String combineText(String leftText, String rightText, int combination) {
        return switch (combination) {
            case 0 -> "(" + leftText + " + " + rightText + ")";
            case 1 -> "(" + leftText + " − " + rightText + ")";
            case 2 -> "(" + rightText + " − " + leftText + ")";
            case 3 -> leftText + " × " + rightText;
            case 4 -> leftText + " ÷ " + rightText;
            default -> rightText + " ÷ " + leftText;
        };
    }

    /** 中缀转后缀求值；结构或除零有问题时返回 {@code null}。 */
    private static @Nullable Frac evaluate(int[] tokens, int[] numbers) {
        ArrayDeque<Frac> values = new ArrayDeque<>(tokens.length);
        IntArrayList operators = new IntArrayList(tokens.length);
        for (int token : tokens) {
            if (isNumberToken(token)) {
                values.push(Frac.ofValue(numbers[token]));
            } else if (token == LEFT) {
                operators.add(token);
            } else if (token == RIGHT) {
                while (!operators.isEmpty() && operators.getInt(operators.size() - 1) != LEFT) {
                    Frac value = apply(operators.removeInt(operators.size() - 1), values);
                    if (value == null) return null;
                    values.push(value);
                }
                if (operators.isEmpty()) return null;
                operators.removeInt(operators.size() - 1);
            } else {
                while (!operators.isEmpty() && precedence(operators.getInt(operators.size() - 1)) >= precedence(token)) {
                    Frac value = apply(operators.removeInt(operators.size() - 1), values);
                    if (value == null) return null;
                    values.push(value);
                }
                operators.add(token);
            }
        }
        while (!operators.isEmpty()) {
            Frac value = apply(operators.removeInt(operators.size() - 1), values);
            if (value == null) return null;
            values.push(value);
        }
        return values.size() == 1 ? values.pop() : null;
    }

    private static @Nullable Frac apply(int operator, ArrayDeque<Frac> values) {
        if (values.size() < 2) return null;
        Frac right = values.pop();
        Frac left = values.pop();
        return switch (operator) {
            case ADD -> left.add(right);
            case SUB -> left.sub(right);
            case MUL -> left.mul(right);
            case DIV -> left.div(right);
            default -> null;
        };
    }

    private static int precedence(int operator) {
        if (operator == MUL || operator == DIV) return 2;
        return operator == LEFT ? 0 : 1;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long next = a % b;
            a = b;
            b = next;
        }
        return a;
    }

    /**
     * 精确有理数：构造时约分、分母恒正，所以「等于 24」就是 {@code num == TARGET * den}，除法除零返回 {@code null}。
     */
    private record Frac(long num, long den) {

        static Frac ofValue(long value) {
            return new Frac(value, 1);
        }

        static @Nullable Frac of(long num, long den) {
            if (den == 0) return null;
            if (den < 0) {
                num = -num;
                den = -den;
            }
            long divisor = gcd(Math.abs(num), den);
            return new Frac(num / divisor, den / divisor);
        }

        boolean isTarget() {
            return num == (long) TARGET * den;
        }

        Frac add(Frac other) {
            return of(num * other.den + other.num * den, den * other.den);
        }

        Frac sub(Frac other) {
            return of(num * other.den - other.num * den, den * other.den);
        }

        Frac mul(Frac other) {
            return of(num * other.num, den * other.den);
        }

        @Nullable
        Frac div(Frac other) {
            return other.num == 0 ? null : of(num * other.den, den * other.num);
        }
    }
}
