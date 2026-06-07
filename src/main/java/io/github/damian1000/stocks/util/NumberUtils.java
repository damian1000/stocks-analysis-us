package io.github.damian1000.stocks.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class NumberUtils {

    public static int compare(BigDecimal one, BigDecimal two) {
        if (one == null && two == null) {
            return 0;
        }
        if (one == null) {
            return -1;
        }
        if (two == null) {
            return 1;
        }
        return one.compareTo(two);
    }

    public static BigDecimal divide(BigDecimal one, BigDecimal two) {
        if (one != null && two != null) {
            if (two.compareTo(BigDecimal.ZERO) != 0) {
                return one.divide(two, MathContext.DECIMAL128);
            }
        }
        return null;
    }

    public static BigDecimal diffAsPercentage(BigDecimal one, BigDecimal two) {
        if (one != null && two != null && one.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal diff = two.subtract(one);
            return diff.divide(one, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(100));
        }
        return null;
    }

    public static BigDecimal format(BigDecimal bd) {
        if (bd != null) {
            return bd.setScale(4, RoundingMode.HALF_UP);
        }
        return null;
    }
}
