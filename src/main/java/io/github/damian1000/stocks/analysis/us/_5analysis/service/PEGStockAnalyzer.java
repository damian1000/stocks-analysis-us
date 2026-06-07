package io.github.damian1000.stocks.analysis.us._5analysis.service;

import io.github.damian1000.stocks.analysis.us._4stocklookup.domian.StockLookup;
import io.github.damian1000.stocks.analysis.us._5analysis.domain.PEGStock;
import io.github.damian1000.stocks.util.NumberUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static io.github.damian1000.stocks.util.NumberUtils.diffAsPercentage;
import static io.github.damian1000.stocks.util.NumberUtils.divide;

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

        BigDecimal thisYearEstimatePE = NumberUtils.divide(price, thisYearEstimateEPS);
        BigDecimal nextYearEstimatePE = NumberUtils.divide(price, nextYearEstimateEPS);

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
