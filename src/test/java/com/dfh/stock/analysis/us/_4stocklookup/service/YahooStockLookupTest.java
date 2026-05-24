package com.dfh.stock.analysis.us._4stocklookup.service;

import com.dfh.TestAssertions;
import com.dfh.exception.DataRetrievalError;
import com.dfh.html.HtmlParser;
import com.dfh.html.HtmlRetriever;
import com.dfh.stock.analysis.us._4stocklookup.domian.StockLookup;
import com.dfh.stock.analysis.us._4stocklookup.service.yahoo.YahooStockLookup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YahooStockLookupTest {

    private YahooStockLookup yahooStockLookup;
    private TestAssertions testAssertions;

    @BeforeEach
    public void setup() {
        testAssertions = new TestAssertions();
        HtmlRetriever htmlRetriever = new HtmlRetriever();
        HtmlParser htmlParser = new HtmlParser();
        yahooStockLookup = new YahooStockLookup(htmlRetriever, htmlParser);
    }

    @Test
    @Disabled
    public void testTeslaLookup() throws DataRetrievalError {
        StockLookup stock = yahooStockLookup.lookup("TSLA.O");
        testAssertions.assertInRange("MarketCap",BigDecimal.valueOf(59558), stock.getMarketCap());
        assertEquals("Dec-19", stock.getYearEnding());
        testAssertions.assertInRange("Price",BigDecimal.valueOf(319.88), stock.getPrice());
        testAssertions.assertInRange("LastYearEPS",BigDecimal.valueOf(-5.857), stock.getLastYearEPS());
        testAssertions.assertInRange("ThisYearEstimateEPS",BigDecimal.valueOf(5.62), stock.getThisYearEstimateEPS());
        testAssertions.assertInRange("NextYearEstimateEPS",BigDecimal.valueOf(9.56), stock.getNextYearEstimateEPS());
        testAssertions.assertInRange("LastYearPE", BigDecimal.valueOf(-54.615), stock.getLastYearPE());
    }

    @Test
    @Disabled
    public void testNestleLookup() throws DataRetrievalError {
        StockLookup stock = yahooStockLookup.lookup("NESN.S");
        testAssertions.assertInRange("MarketCap",BigDecimal.valueOf(257998), stock.getMarketCap());
        assertEquals("Dec-19", stock.getYearEnding());
        testAssertions.assertInRange("Price",BigDecimal.valueOf(90.36), stock.getPrice());
        testAssertions.assertInRange("LastYearEPS",BigDecimal.valueOf(3.354), stock.getLastYearEPS());
        testAssertions.assertInRange("ThisYearEstimateEPS",BigDecimal.valueOf(4.29), stock.getThisYearEstimateEPS());
        testAssertions.assertInRange("NextYearEstimateEPS",BigDecimal.valueOf(4.7), stock.getNextYearEstimateEPS());
        testAssertions.assertInRange("LastYearPE", BigDecimal.valueOf(26.941), stock.getLastYearPE());
    }

    @Test
    @Disabled
    public void testHSBCHoldingsLookup() throws DataRetrievalError {
        StockLookup stock = yahooStockLookup.lookup("HSBA.L");
        testAssertions.assertInRange("MarketCap",BigDecimal.valueOf(152447.41), stock.getMarketCap());
        assertEquals("Dec-19", stock.getYearEnding());
        testAssertions.assertInRange("Price",BigDecimal.valueOf(612.9), stock.getPrice());
        testAssertions.assertInRange("LastYearEPS",BigDecimal.valueOf(0.6207), stock.getLastYearEPS());
        testAssertions.assertInRange("ThisYearEstimateEPS",BigDecimal.valueOf(0.5526), stock.getThisYearEstimateEPS());
        testAssertions.assertInRange("NextYearEstimateEPS",BigDecimal.valueOf(0.5753), stock.getNextYearEstimateEPS());
        testAssertions.assertInRange("LastYearPE", BigDecimal.valueOf(987.4335), stock.getLastYearPE());
    }

    @Test
    @Disabled
    public void testEarningAboveEstimates() throws DataRetrievalError {
        StockLookup stock = yahooStockLookup.lookup("AZPN.O");
        assertEquals("4 from 5", stock.getEarningAboveEstimates());
    }

    @Test
    @Disabled
    public void testInvalidMicrosoftLookup() throws DataRetrievalError {
        StockLookup stock = yahooStockLookup.lookup("MSFT.N");
        assertEquals(0.0, stock.getBeta().doubleValue(), 0.01);
    }

    @Test
    @Disabled
    public void test22ndCenturyGroupLookup() throws DataRetrievalError {
        StockLookup stock = yahooStockLookup.lookup("XXII.N");
        assertEquals(1.66, stock.getBeta().doubleValue(), 0.01);
    }

    @Test
    @Disabled
    public void testOrchidsPaperProductsLookup() throws DataRetrievalError {
        StockLookup stock = yahooStockLookup.lookup("TIS.N");
        assertEquals(0.0, stock.getBeta().doubleValue(), 0.01);
    }

    @Test
    @Disabled
    public void testInvalidSummitTherapeuticsLookup() throws DataRetrievalError {
        StockLookup stock = yahooStockLookup.lookup("SMMT.O");
        assertEquals(0.0, stock.getBeta().doubleValue(), 0.01);
    }

}
