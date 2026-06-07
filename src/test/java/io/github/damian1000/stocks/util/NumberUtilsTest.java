package io.github.damian1000.stocks.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NumberUtilsTest {

    @Test
    void compareHandlesNullsConsistently() {
        assertEquals(0, NumberUtils.compare(null, null));
        assertEquals(-1, NumberUtils.compare(null, BigDecimal.ONE));
        assertEquals(1, NumberUtils.compare(BigDecimal.ONE, null));
    }

    @Test
    void compareDelegatesToBigDecimal() {
        assertEquals(-1, NumberUtils.compare(BigDecimal.ONE, BigDecimal.TEN));
        assertEquals(0, NumberUtils.compare(BigDecimal.ONE, BigDecimal.ONE));
        assertEquals(1, NumberUtils.compare(BigDecimal.TEN, BigDecimal.ONE));
    }

    @Test
    void divideReturnsNullWhenEitherOperandIsNullOrZero() {
        assertNull(NumberUtils.divide(null, BigDecimal.ONE));
        assertNull(NumberUtils.divide(BigDecimal.ONE, null));
        assertNull(NumberUtils.divide(BigDecimal.ONE, BigDecimal.ZERO));
    }

    @Test
    void divideUsesDecimal128Precision() {
        BigDecimal result = NumberUtils.divide(BigDecimal.ONE, new BigDecimal("3"));
        // 1/3 to MathContext.DECIMAL128 has 34 significant digits.
        org.junit.jupiter.api.Assertions.assertTrue(result.toPlainString().startsWith("0.33333"));
    }

    @Test
    void diffAsPercentageGivesPercentChangeFromOneToTwo() {
        // From 100 to 110 = +10%.
        BigDecimal pct = NumberUtils.diffAsPercentage(new BigDecimal("100"), new BigDecimal("110"));
        assertEquals(0, pct.compareTo(new BigDecimal("10")));
    }

    @Test
    void diffAsPercentageReturnsNullOnNullInputsOrZeroBase() {
        assertNull(NumberUtils.diffAsPercentage(null, BigDecimal.ONE));
        assertNull(NumberUtils.diffAsPercentage(BigDecimal.ONE, null));
        assertNull(NumberUtils.diffAsPercentage(BigDecimal.ZERO, BigDecimal.ONE));
    }

    @Test
    void formatRoundsTo4DecimalPlacesHalfUp() {
        assertEquals(new BigDecimal("1.2346"), NumberUtils.format(new BigDecimal("1.23456789")));
        assertNull(NumberUtils.format(null));
    }
}
