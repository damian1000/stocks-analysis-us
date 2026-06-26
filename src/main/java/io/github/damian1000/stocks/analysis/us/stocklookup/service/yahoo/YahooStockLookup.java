package io.github.damian1000.stocks.analysis.us.stocklookup.service.yahoo;

import io.github.damian1000.stocks.exception.DataRetrievalError;
import io.github.damian1000.stocks.analysis.us.stocklookup.domain.StockLookup;
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

    private final YahooFinanceClient yahooFinanceClient;

    public StockLookup lookup(String zacksCode) throws DataRetrievalError {
        zacksCode = zacksCode.replaceAll("\\.", "");

        StockLookup stockLookup = new StockLookup();
        stockLookup.setId(IdGenerator.generateId());
        stockLookup.setDate(LocalDate.now());
        stockLookup.setZacksCode(zacksCode);

        String json = yahooFinanceClient.fetchQuoteSummary(zacksCode);
        QuoteSummary quoteSummary = new Gson().fromJson(json, QuoteSummary.class);
        if (quoteSummary == null
                || quoteSummary.getQuoteSummary() == null
                || quoteSummary.getQuoteSummary().getResult() == null
                || quoteSummary.getQuoteSummary().getResult().isEmpty()) {
            throw new DataRetrievalError(String.format(
                    "Yahoo response for %s contained no quoteSummary result — symbol may be unknown or the API changed",
                    zacksCode));
        }
        {

            QuoteSummaryStore quoteSummaryStore = quoteSummary.getQuoteSummary().getResult().get(0);

            Price price = quoteSummaryStore.getPrice();
            if (price != null) {
                stockLookup.setCurrency(price.getCurrency());
                Raw priceMarketCap = price.getMarketCap();
                if (priceMarketCap != null) {
                    stockLookup.setMarketCap(priceMarketCap.getRaw());
                }
                stockLookup.setCompany(price.getLongName());
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

}
