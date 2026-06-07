package io.github.damian1000.stocks.analysis.us._4stocklookup.service.yahoo;

import io.github.damian1000.stocks.exception.DataRetrievalError;
import io.github.damian1000.stocks.html.HtmlParser;
import io.github.damian1000.stocks.html.HtmlResponse;
import io.github.damian1000.stocks.html.HtmlRetriever;
import io.github.damian1000.stocks.analysis.us._4stocklookup.domain.StockLookup;
import io.github.damian1000.stocks.util.IdGenerator;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class YahooStockLookup {

    private final HtmlRetriever htmlRetriever;
    private final HtmlParser htmlParser;

    public StockLookup lookup(String zacksCode) throws DataRetrievalError {
        zacksCode = zacksCode.replaceAll("\\.", "");

        StockLookup stockLookup = new StockLookup();
        stockLookup.setId(IdGenerator.generateId());
        stockLookup.setDate(LocalDate.now());
        stockLookup.setZacksCode(zacksCode);

        HtmlResponse overviewHtmlResponse = htmlRetriever.getHtml(String.format("https://uk.finance.yahoo.com/quote/%s/analysis?p=%s", zacksCode, zacksCode));
        String overview = overviewHtmlResponse.rawHtml;
        String quoteSummaryRaw = htmlParser.extractTextFromStartString(overview, "QuoteSummaryStore", "summaryDetail", "\"symbol\":");
        quoteSummaryRaw = "{\""+quoteSummaryRaw.substring(0, quoteSummaryRaw.length()-1)+"}}";

        Gson g = new Gson();
        QuoteSummary quoteSummary = g.fromJson(quoteSummaryRaw, QuoteSummary.class);
        if (quoteSummary.getQuoteSummaryStore() != null) {

            QuoteSummaryStore quoteSummaryStore = quoteSummary.getQuoteSummaryStore();

            if (quoteSummaryStore.getPrice() != null) {
                Price price = quoteSummaryStore.getPrice();
                if (price != null) {
                    stockLookup.setCurrency(price.getCurrency());
                    Raw priceMarketCap = price.getMarketCap();
                    if (priceMarketCap != null) {
                        stockLookup.setMarketCap(priceMarketCap.getRaw());
                    }
                    stockLookup.setCompany(price.getLongName());
                }
            }

            SummaryDetail summaryDetail = quoteSummaryStore.getSummaryDetail();
            if (summaryDetail != null) {
                Raw betaSummary = summaryDetail.getBeta();
                if (betaSummary != null) {
                    stockLookup.setBeta(betaSummary.getRaw());
                }

                String currency = summaryDetail.getCurrency();
                Raw previousClose = summaryDetail.getPreviousClose();
                if (previousClose != null) {
                    BigDecimal raw = previousClose.getRaw();
                    if (currency != null && raw != null) {
                        stockLookup.setPrice(raw);
                    }
                }

                Raw trailingPE = summaryDetail.getTrailingPE();
                if (trailingPE != null) {
                    stockLookup.setLastYearPE(trailingPE.getRaw());
                }
            }

            FinancialData financialData = quoteSummaryStore.getFinancialData();
            if (financialData != null) {
                Raw targetMeanPrice = financialData.getTargetMeanPrice();
                if (targetMeanPrice != null) {
                    stockLookup.setTargetPrice(targetMeanPrice.getRaw());
                }
                Raw recommendationMean = financialData.getRecommendationMean();
                if (recommendationMean != null) {
                    stockLookup.setRecommendationRating(recommendationMean.getRaw());
                }
            }

            EarningsTrends earningsTrend = quoteSummaryStore.getEarningsTrend();
            if (earningsTrend != null) {
                List<EarningTrend> earningsTrendList = earningsTrend.getTrend();
                if (earningsTrendList != null) {
                    for (EarningTrend trend : earningsTrendList) {
                        if (trend != null) {
                            if ("0y".equalsIgnoreCase(trend.getPeriod())) {
                                EarningsEstimate earningsEstimate = trend.getEarningsEstimate();
                                if (earningsEstimate != null) {
                                    Raw average = earningsEstimate.getAvg();
                                    if (average != null) {
                                        stockLookup.setThisYearEstimateEPS(average.getRaw());
                                    }

                                    Raw yearAgoEps = earningsEstimate.getYearAgoEps();
                                    if (yearAgoEps != null) {
                                        stockLookup.setLastYearEPS(yearAgoEps.getRaw());
                                    }
                                }

                            }
                            if ("+1y".equalsIgnoreCase(trend.getPeriod())) {
                                EarningsEstimate earningsEstimate = trend.getEarningsEstimate();
                                if (earningsEstimate != null) {
                                    Raw average = earningsEstimate.getAvg();
                                    if (average != null) {
                                        stockLookup.setNextYearEstimateEPS(average.getRaw());
                                    }
                                }
                            }
                        }
                    }
                }

            }

            EarningsHistory earningsHistory = quoteSummaryStore.getEarningsHistory();
            if (earningsHistory != null) {
                List<History> historyList = earningsHistory.getHistory();
                if (historyList != null) {
                    int numberOfHistoryRecords = 0;
                    int aboveEstimatedEps = 0;
                    for (History history: historyList) {
                        if (history != null) {
                            Raw epsDifference = history.getEpsDifference();
                            if (epsDifference != null) {
                                numberOfHistoryRecords++;
                                BigDecimal diff = epsDifference.getRaw();
                                if (diff != null && diff.compareTo(BigDecimal.ZERO) > 0) {
                                    aboveEstimatedEps++;
                                }
                            }
                        }
                    }
                    stockLookup.setEarningAboveEstimates(String.format("%s out of %s above estimated eps",
                            aboveEstimatedEps, numberOfHistoryRecords));
                }
            }

        }
        return stockLookup;
    }

    public static void main(String[] args) {
        HtmlRetriever htmlRetriever = new HtmlRetriever();
        HtmlParser htmlParser = new HtmlParser();
        YahooStockLookup yahooStockLookup = new YahooStockLookup(htmlRetriever, htmlParser);
        try {
            yahooStockLookup.lookup("");
        } catch (DataRetrievalError dataRetrievalError) {
            log.error("An exception has occurred", dataRetrievalError);
        }

    }

}
