package com.nodecraft.nodesystem.nodes.math.compare;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class CompareUtils {
    private static final double EPSILON = 1.0e-10d;

    private CompareUtils() {
    }

    static Relation compare(@Nullable Object left, @Nullable Object right) {
        Integer ordering = compareOrder(left, right);
        boolean equal = ordering != null
            ? ordering == 0
            : equalValues(left, right);
        return new Relation(
            equal,
            ordering != null && ordering > 0,
            ordering != null && ordering < 0
        );
    }

    static boolean equalValues(@Nullable Object left, @Nullable Object right) {
        if (left == null || right == null) {
            return left == right;
        }

        Double leftNumber = resolveNumber(left);
        Double rightNumber = resolveNumber(right);
        if (leftNumber != null && rightNumber != null) {
            return compareNumbers(leftNumber, rightNumber) == 0;
        }

        if (left instanceof String || right instanceof String) {
            return Objects.toString(left, "").equals(Objects.toString(right, ""));
        }
        return Objects.equals(left, right);
    }

    static boolean modeResult(Relation relation, int mode) {
        return switch (mode) {
            case 0 -> relation.equal();
            case 1 -> !relation.equal();
            case 2 -> relation.greater();
            case 3 -> relation.less();
            case 4 -> relation.greater() || relation.equal();
            case 5 -> relation.less() || relation.equal();
            default -> relation.equal();
        };
    }

    private static @Nullable Integer compareOrder(@Nullable Object left, @Nullable Object right) {
        if (left == null || right == null) {
            return null;
        }

        Double leftNumber = resolveNumber(left);
        Double rightNumber = resolveNumber(right);
        if (leftNumber != null && rightNumber != null) {
            return compareNumbers(leftNumber, rightNumber);
        }

        if (left instanceof String leftString && right instanceof String rightString) {
            return Integer.signum(leftString.compareTo(rightString));
        }
        return null;
    }

    private static @Nullable Double resolveNumber(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String string) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static int compareNumbers(double left, double right) {
        if (Double.isFinite(left) && Double.isFinite(right) && Math.abs(left - right) < EPSILON) {
            return 0;
        }
        return Integer.signum(Double.compare(left, right));
    }

    record Relation(boolean equal, boolean greater, boolean less) {
    }
}
