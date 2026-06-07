package io.github.damian1000.stocks.analysis.us._5analysis.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PEGStockTest {

    @Test
    void builderAndAccessorsRoundTrip() {
        PEGStock s = PEGStock.builder()
                .zacksCode("ZCS-1")
                .thisYearPEG(BigDecimal.valueOf(1.1))
                .nextYearPEG(BigDecimal.valueOf(0.9))
                .category("B")
                .build();
        assertEquals("ZCS-1", s.getZacksCode());
        assertEquals(BigDecimal.valueOf(1.1), s.getThisYearPEG());
        assertEquals("B", s.getCategory());
        assertTrue(s.toString().contains("ZCS-1"));
    }

    @Test
    void compareToOrdersByCategoryThenByNextYearPegAscending() {
        PEGStock a = PEGStock.builder().category("A").nextYearPEG(BigDecimal.valueOf(0.5)).build();
        PEGStock aHigher = PEGStock.builder().category("A").nextYearPEG(BigDecimal.valueOf(1.5)).build();
        PEGStock b = PEGStock.builder().category("B").nextYearPEG(BigDecimal.valueOf(0.1)).build();

        List<PEGStock> list = new ArrayList<>(List.of(b, aHigher, a));
        Collections.sort(list);
        assertEquals(a, list.get(0));         // category A, lower PEG first
        assertEquals(aHigher, list.get(1));
        assertEquals(b, list.get(2));
    }
}
