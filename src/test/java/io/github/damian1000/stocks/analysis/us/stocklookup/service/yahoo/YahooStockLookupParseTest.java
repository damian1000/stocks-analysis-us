package io.github.damian1000.stocks.analysis.us.stocklookup.service.yahoo;

import io.github.damian1000.stocks.analysis.us.stocklookup.domain.StockLookup;
import io.github.damian1000.stocks.exception.DataRetrievalError;
import io.github.damian1000.stocks.html.HtmlParser;
import io.github.damian1000.stocks.html.HtmlResponse;
import io.github.damian1000.stocks.html.HtmlRetriever;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YahooStockLookupParseTest {

    private HtmlRetriever htmlRetriever;
    private HtmlParser htmlParser;
    private YahooStockLookup yahooStockLookup;

    @BeforeEach
    void setUp() {
        htmlRetriever = mock(HtmlRetriever.class);
        htmlParser = mock(HtmlParser.class);
        yahooStockLookup = new YahooStockLookup(htmlRetriever, htmlParser);
    }

    @Test
    void parsesPriceSummaryFinancialEarningsAndHistoryFromMockedHtml() throws DataRetrievalError {
        HtmlResponse response = new HtmlResponse();
        response.rawHtml = "ignored — htmlParser is mocked";
        when(htmlRetriever.getHtml(anyString())).thenReturn(response);

        // The service wraps the extracted text as `{"<extracted-minus-last-char>}}`
        // so we craft the extracted block to start with `QuoteSummaryStore":` and
        // end with a trailing `,` that the service strips.
        String extracted =
                "QuoteSummaryStore\":{" +
                "\"price\":{\"marketCap\":{\"raw\":1000},\"currency\":\"USD\",\"longName\":\"Acme Inc\"}," +
                "\"summaryDetail\":{\"previousClose\":{\"raw\":100.5},\"beta\":{\"raw\":1.2}," +
                "  \"currency\":\"USD\",\"trailingPE\":{\"raw\":15.7}}," +
                "\"financialData\":{\"targetMeanPrice\":{\"raw\":120.3},\"recommendationMean\":{\"raw\":2.5}}," +
                "\"earningsTrend\":{\"trend\":[" +
                "  {\"period\":\"0y\",\"earningsEstimate\":{\"avg\":{\"raw\":5.1},\"yearAgoEps\":{\"raw\":4.0}}}," +
                "  {\"period\":\"+1y\",\"earningsEstimate\":{\"avg\":{\"raw\":6.2}}}," +
                "  {\"period\":\"+5y\"}]}," +
                "\"earningsHistory\":{\"history\":[" +
                "  {\"epsDifference\":{\"raw\":0.5}}," +
                "  {\"epsDifference\":{\"raw\":-0.2}}," +
                "  {\"epsDifference\":{\"raw\":0.3}}]},";
        when(htmlParser.extractTextFromStartString(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(extracted);

        StockLookup result = yahooStockLookup.lookup("ACME.O");

        // replaceAll("\\.", "") strips the dot but keeps the suffix character.
        assertEquals("ACMEO", result.getZacksCode());
        assertEquals("Acme Inc", result.getCompany());
        assertEquals(new BigDecimal("1000"), result.getMarketCap());
        assertEquals("USD", result.getCurrency());

        assertEquals(new BigDecimal("100.5"), result.getPrice());
        assertEquals(new BigDecimal("1.2"), result.getBeta());
        assertEquals(new BigDecimal("15.7"), result.getLastYearPE());

        assertEquals(new BigDecimal("120.3"), result.getTargetPrice());
        assertEquals(new BigDecimal("2.5"), result.getRecommendationRating());

        assertEquals(new BigDecimal("5.1"), result.getThisYearEstimateEPS());
        assertEquals(new BigDecimal("4.0"), result.getLastYearEPS());
        assertEquals(new BigDecimal("6.2"), result.getNextYearEstimateEPS());

        // 2 of 3 history entries are above zero
        assertEquals("2 out of 3 above estimated eps", result.getEarningAboveEstimates());

        assertNotNull(result.getId());
        assertNotNull(result.getDate());
    }

    @Test
    void quoteSummaryStoreWithOnlyEmptyPriceLeavesAllFieldsNull() throws DataRetrievalError {
        HtmlResponse response = new HtmlResponse();
        response.rawHtml = "ignored";
        when(htmlRetriever.getHtml(anyString())).thenReturn(response);

        // The service strips the trailing char (comma here) and wraps as
        // `{"<content>}}`, so QuoteSummaryStore must stay un-closed in the
        // fixture (the wrapping supplies its closing brace).
        when(htmlParser.extractTextFromStartString(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("QuoteSummaryStore\":{\"price\":{},");

        StockLookup result = yahooStockLookup.lookup("BLNK");

        assertEquals("BLNK", result.getZacksCode());
        assertNull(result.getPrice());
        assertNull(result.getBeta());
        assertNull(result.getCompany());
    }

    @Test
    void presentSectionsWithEmptyInnerObjectsLeaveFieldsUnset() throws DataRetrievalError {
        HtmlResponse response = new HtmlResponse();
        response.rawHtml = "ignored — htmlParser is mocked";
        when(htmlRetriever.getHtml(anyString())).thenReturn(response);
        when(htmlParser.extractTextFromStartString(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("QuoteSummaryStore\":{\"price\":{},\"summaryDetail\":{},\"financialData\":{}," +
                        "\"earningsTrend\":{\"trend\":[]},\"earningsHistory\":{\"history\":[]},");

        StockLookup result = yahooStockLookup.lookup("EMPTY");

        assertNull(result.getMarketCap());
        assertNull(result.getPrice());
        assertNull(result.getTargetPrice());
        assertNull(result.getRecommendationRating());
        assertEquals("0 out of 0 above estimated eps", result.getEarningAboveEstimates());
    }

    @Test
    void trendAndHistoryEntriesWithNullInnerValuesAreSkipped() throws DataRetrievalError {
        HtmlResponse response = new HtmlResponse();
        response.rawHtml = "ignored — htmlParser is mocked";
        when(htmlRetriever.getHtml(anyString())).thenReturn(response);
        when(htmlParser.extractTextFromStartString(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("QuoteSummaryStore\":{" +
                        "\"summaryDetail\":{\"previousClose\":{\"raw\":50}}," +
                        "\"earningsTrend\":{\"trend\":[" +
                        "  {\"period\":\"0y\",\"earningsEstimate\":{}}," +
                        "  {\"period\":\"+1y\",\"earningsEstimate\":{}}," +
                        "  {\"period\":\"0y\",\"earningsEstimate\":null}," +
                        "  null]}," +
                        "\"earningsHistory\":{\"history\":[{\"epsDifference\":null},{\"epsDifference\":{}},null]},");

        StockLookup result = yahooStockLookup.lookup("NULLS");

        // previousClose present but no currency -> price stays unset
        assertNull(result.getPrice());
        assertNull(result.getThisYearEstimateEPS());
        assertNull(result.getNextYearEstimateEPS());
        assertNull(result.getLastYearEPS());
        // one history record counted, but its eps difference is null -> none above estimate
        assertEquals("0 out of 1 above estimated eps", result.getEarningAboveEstimates());
    }

    @Test
    void missingQuoteSummaryStoreThrows() throws DataRetrievalError {
        HtmlResponse response = new HtmlResponse();
        response.rawHtml = "ignored — htmlParser is mocked";
        when(htmlRetriever.getHtml(anyString())).thenReturn(response);
        when(htmlParser.extractTextFromStartString(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        assertThrows(DataRetrievalError.class, () -> yahooStockLookup.lookup("GONE"));
    }
}
