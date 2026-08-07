package io.github.damian1000.stocks.analysis.us.analysis.service;

import io.github.damian1000.stocks.analysis.us.stocklookup.domain.StockLookup;
import io.github.damian1000.stocks.analysis.us.analysis.domain.PEGStock;
import io.github.damian1000.stocks.util.Decimals;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static io.github.damian1000.stocks.util.Decimals.diffAsPercentage;
import static io.github.damian1000.stocks.util.Decimals.divide;

@Component
public class PEGStockAnalyzer {

    public PEGStock analyzeStocks(StockLookup stockLookup) {
        if (!stockLookup.isValid()) {
            return PEGStock.builder().category("20 Reuters Lookup Invalid").build();
        }

        BigDecimal price = stockLookup.getPrice();

        BigDecimal lastYearEPS = stockLookup.getLastYearEPS();
        BigDecimal thisYearEstimateEPS = stockLookup.getThisYearEstimateEPS();
        BigDecimal nextYearEstimateEPS = stockLookup.getNextYearEstimateEPS();

        BigDecimal thisYearEstimatePE = Decimals.divide(price, thisYearEstimateEPS);
        BigDecimal nextYearEstimatePE = Decimals.divide(price, nextYearEstimateEPS);

        BigDecimal thisYearEPSGrowth = diffAsPercentage(lastYearEPS, thisYearEstimateEPS);
        BigDecimal nextYearEPSGrowth = diffAsPercentage(thisYearEstimateEPS, nextYearEstimateEPS);

        BigDecimal thisYearPEG = divide(thisYearEstimatePE, thisYearEPSGrowth);
        BigDecimal nextYearPEG = divide(nextYearEstimatePE, nextYearEPSGrowth);

        PEGStock.PEGStockBuilder stockBuilder = PEGStock.builder();
        stockBuilder.zacksCode(stockLookup.getZacksCode());
        stockBuilder.thisYearEstimatePE(thisYearEstimatePE);
        stockBuilder.nextYearEstimatePE(nextYearEstimatePE);
        stockBuilder.thisYearEPSGrowth(thisYearEPSGrowth);
        stockBuilder.nextYearEPSGrowth(nextYearEPSGrowth);
        stockBuilder.thisYearPEG(thisYearPEG);
        stockBuilder.nextYearPEG(nextYearPEG);

        if (thisYearPEG == null && nextYearPEG == null) {
            stockBuilder.category("10 Missing Stats");
        } else {
            stockBuilder.category("00 Good");
        }
        return stockBuilder.build();
    }

}
