package io.github.bluelhf.calculator;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Calculator {
    public static final HashMap<Operator, Integer> OPERATORS = new HashMap<>(
        Map.of(
            new Operator() {
                @Override public char key() { return '^'; }
                @Override public double get(double a, double b) {
                    return Math.pow(a, b);
                }
            }, 3,
            new Operator() {
                @Override public char key() { return '*'; }
                @Override public double get(double a, double b) {
                    return a * b;
                }
            }, 2,
            new Operator() {
                @Override public char key() { return '/'; }
                @Override public double get(double a, double b) {
                    return a / b;
                }
            }, 2,
            new Operator() {
                @Override public char key() { return '+'; }
                @Override public double get(double a, double b) {
                    return a + b;
                }
            }, 1,
            new Operator() {
                @Override public char key() { return '-'; }
                @Override public double get(double a, double b) {
                    return a - b;
                }
            }, 1
        )
    );

    public static void main(String[] args) {
        Launcher.main();
    }

    private static int nextOperator(String expr) {
        HashMap<Integer, ArrayList<Integer>> opMap = new HashMap<>();

        for(int i = 0; i < expr.length(); i++) {
            Operator o = findOperator(expr.charAt(i));
            if (o == null) continue;

            int priority = OPERATORS.get(o);
            opMap.putIfAbsent(priority, new ArrayList<>());
            opMap.get(priority).add(i);
        }
        if (opMap.entrySet().size() == 0) return -1;
        int max = opMap.keySet().stream().max(Integer::compareTo).orElse(0);
        return opMap.get(max).stream().min(Integer::compareTo).orElse(0);
    }

    /**
     * This relies on the principle that for any opening bracket with depth <i>d</i>, the next closing bracket that is of depth <i>d-1</i> is the pair of the opening bracket.
     *
     * <br/>
     * <pre><code>
     *     Depth   1   2   101   0
     *     Text    (abc(def))(ghi)
     * </code></pre>
     * @return The indices of all upmost bracket pairs of the input string, in order.
     * @param expr The input string.
     * */
    private static int[] brackets(String expr) {
        int[] pairs = new int[0];

        int depth = 0;
        int lastOne = -1;
        for(int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') {
                depth++;
                if (depth == 1) lastOne = i;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    // pair is lastOne - i
                    int[] temp = pairs;
                    pairs = new int[pairs.length + 2];
                    System.arraycopy(temp, 0, pairs, 0, temp.length);
                    pairs[pairs.length - 2] = lastOne;
                    pairs[pairs.length - 1] = i;
                }
            }
        }

        return pairs;
    }

    private static int find(String expr, int idx, boolean left) {

        if (left) {
            for (int i = idx - 1; i >= 0; i--) {
                char c = expr.charAt(i);
                if (OPERATORS.keySet().stream().anyMatch(op -> op.key() == c)) {
                    return i + 1;
                }
            }
            return 0;
        } else {
            for (int i = idx + 1; i < expr.length(); i++) {
                char c = expr.charAt(i);
                if (OPERATORS.keySet().stream().anyMatch(op -> op.key() == c)) {
                    return i - 1;
                }
            }

            return expr.length() - 1;
        }
    }

    @Nullable
    public static Operator findOperator(char key) {
        for (Operator o : OPERATORS.keySet()) {
            if (o.key() == key) return o;
        }

        return null;
    }

    public static boolean isOperatorKey(char c) {
        for (Operator o : OPERATORS.keySet()) {
            if (o.key() == c) return true;
        }
        return false;
    }

    private static int count(String s, String regex) {
        return (" " + s + " ").split(regex).length - 1;
    }

    private static boolean containsDuplicateKeys(String expr) {
        for (Operator o : OPERATORS.keySet()) {
            String key = String.valueOf(o.key());
            if (expr.contains(key + key)) return true;
        }
        return false;
    }

    public static double parse(String expr) throws NumberFormatException {
        if (expr.equals("")) throw new NumberFormatException("Empty input");
        if (count(expr, "\\(") != count(expr, "\\)")) {
            throw new NumberFormatException("Mismatched brackets");
        }

        if (isOperatorKey(expr.charAt(0)))
            throw new NumberFormatException("Leading operator");
        if (isOperatorKey(expr.charAt(expr.length()-1)))
            throw new NumberFormatException("Trailing operator");
        if (containsDuplicateKeys(expr))
            throw new NumberFormatException("Duplicate operators");




        // Parse syntax sugar
        expr = expr.replace(" ", "");
        expr = expr.replace("+-", "-");

        for(int i = 1; i < expr.length(); i++) {
            char c = expr.charAt(i);
            char c2 = expr.charAt(i - 1);
            if (c == '(' && OPERATORS.keySet().stream().noneMatch(op -> op.key() == c2)) {
                expr = expr.substring(0, i) + "*" + expr.substring(i);
            }
        }

        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException ignored) {

        }

        int[] bracketPairs = brackets(expr);
        while (bracketPairs.length != 0) {
            int start = bracketPairs[0];
            int end = bracketPairs[1];

            String sub = expr.substring(start+1, end);
            expr = expr.substring(0, start) + parse(sub) + expr.substring(end + 1);
            bracketPairs = brackets(expr);
        }


        int i = nextOperator(expr);
        if (i == -1) return parse(expr);

        char key = expr.charAt(i);
        Operator operator = findOperator(key);

        int leftIndex = find(expr, i, true);
        int rightIndex = find(expr, i, false);


        double leftSide = parse(expr.substring(leftIndex, i));
        double rightSide = parse(expr.substring(i + 1, rightIndex + 1));

        //noinspection ConstantConditions - nextOperator will always return a valid operator index or -1.
        double simplify = operator.get(leftSide, rightSide);
        expr = expr.substring(0, leftIndex) + simplify + expr.substring(rightIndex + 1);
        return parse(expr);
    }
}
