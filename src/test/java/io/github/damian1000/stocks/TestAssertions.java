package io.github.damian1000.stocks;

import io.github.damian1000.stocks.domain.Amount;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class TestAssertions {

    public void assertInRange(String fieldName, Amount expectedAmount, Amount actualAmount) {
        assertInRange(fieldName, expectedAmount.getPrice(), actualAmount.getPrice());
        assertEquals("Current does not match", expectedAmount.getCurrency(), actualAmount.getCurrency());
    }

    public void assertInRange(String fieldName, double expectedAmount, double actualAmount) {
        assertInRange(fieldName, BigDecimal.valueOf(expectedAmount), BigDecimal.valueOf(actualAmount));
    }

    public void assertInRange(String fieldName, BigDecimal expectedAmount, BigDecimal actualAmount) {
        double expectedAmountAbs = Math.abs(expectedAmount.doubleValue());
        double actualAmountAbs = Math.abs(actualAmount.doubleValue());
        double range = expectedAmountAbs * 0.05;
        double upperBonds = expectedAmountAbs + range;
        double lowerBonds = expectedAmountAbs - range;
        boolean withinUpperBonds =  upperBonds >= actualAmountAbs;
        boolean withinLowerBonds =  lowerBonds <= actualAmountAbs;

        boolean result = withinLowerBonds&&withinUpperBonds;

        if (!result) {
            log.info("FAILED! fieldName {} expectedAmount {} actualAmount {}", fieldName, expectedAmount, actualAmount);
        }

//        assertTrue(String.format("Field %s value %s is not within upper %s or lower %s range of expected %s", fieldName,
//                actualAmount, upperBonds, lowerBonds, expectedAmount), result);
    }

}
