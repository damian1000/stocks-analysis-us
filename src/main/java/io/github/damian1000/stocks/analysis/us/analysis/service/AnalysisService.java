package io.github.damian1000.stocks.analysis.us.analysis.service;

import io.github.damian1000.stocks.analysis.us.sectormapping.domain.ZacksSectorMapping;
import io.github.damian1000.stocks.analysis.us.sectormapping.repository.ZacksSectorMappingRepository;
import io.github.damian1000.stocks.analysis.us.zackscode.domain.ZacksCode;
import io.github.damian1000.stocks.analysis.us.zackscode.repository.ZacksBasicRepository;
import io.github.damian1000.stocks.analysis.us.stocklookup.domain.StockLookup;
import io.github.damian1000.stocks.analysis.us.stocklookup.repository.StockLookupRepository;
import io.github.damian1000.stocks.analysis.us.analysis.domain.AnalysisStock;
import io.github.damian1000.stocks.analysis.us.analysis.domain.PEGStock;
import io.github.damian1000.stocks.analysis.us.analysis.event.AnalysisStockCompleteEvent;
import io.github.damian1000.stocks.analysis.us.analysis.event.AnalysisStockStartEvent;
import io.github.damian1000.stocks.analysis.us.analysis.repository.AnalysisRepository;
import io.github.damian1000.stocks.util.IdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.damian1000.stocks.util.NumberUtils.format;

@AllArgsConstructor
@Component
@Slf4j
public class AnalysisService {

    private final StockLookupRepository stockLookupRepository;
    private final AnalysisRepository analysisRepository;
    private final ZacksBasicRepository zacksBasicRepository;
    private final ZacksSectorMappingRepository zacksSectorMappingRepository;
    private final PEGStockAnalyzer stockAnalyzer;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void onAnalysisServiceEvent(AnalysisStockStartEvent event) {
        log.info("Retrieving stock lookup for date {}", event.getDate());
        Set<StockLookup> stockLookupList = stockLookupRepository.findByDate(event.getDate());
        log.info("Retrieved a total of stock lookup {} for date {}", stockLookupList.size(), event.getDate());

        log.info("Deleting analysis for date {}", event.getDate());
        analysisRepository.deleteByDate(event.getDate());

        String zacksDateString = System.getProperty("zacksDate");
        LocalDate zacksDate;
        if (zacksDateString != null) {
            zacksDate = LocalDate.parse(zacksDateString);
        } else {
            zacksDate = event.getDate();
        }
        log.info("Retrieving zacks basic for date {}", zacksDate);

        Set<ZacksCode> zacksCodeSet = zacksBasicRepository.findByDate(zacksDate);
        Map<String, ZacksCode> zacksBasicMap = zacksCodeSet.stream().collect(
                Collectors.toMap(ZacksCode::getZacksCode, Function.identity()));

        log.info("Retrieving zacks sector mapping for date {}",zacksDate);
        List<ZacksSectorMapping> zacksSectorMappingList = zacksSectorMappingRepository.findByDate(zacksDate);
        Map<String, ZacksSectorMapping> zacksSectorMappingMap = zacksSectorMappingList.stream().collect(
                Collectors.toMap(zacksSectorMapping -> zacksSectorMapping.getIndustry().toUpperCase(), Function.identity()));

        List<AnalysisStock> analysisStocks = stockLookupList.stream().map(stockLookup -> {
            AnalysisStock.AnalysisStockBuilder analysisStockBuilder = AnalysisStock.builder();
            analysisStockBuilder.date(event.getDate());
            analysisStockBuilder.zacksCode(stockLookup.getZacksCode());
            analysisStockBuilder.company(stockLookup.getCompany());
            analysisStockBuilder.currency(stockLookup.getCurrency());
            analysisStockBuilder.marketCap(stockLookup.getMarketCap());
            analysisStockBuilder.yearEnding(stockLookup.getYearEnding());
            analysisStockBuilder.beta(stockLookup.getBeta());
            analysisStockBuilder.price(stockLookup.getPrice());
            analysisStockBuilder.targetPrice(stockLookup.getTargetPrice());
            analysisStockBuilder.lastYearEPS(stockLookup.getLastYearEPS());
            analysisStockBuilder.lastYearPE(stockLookup.getLastYearPE());
            analysisStockBuilder.thisYearEstimateEPS(stockLookup.getThisYearEstimateEPS());
            analysisStockBuilder.nextYearEstimateEPS(stockLookup.getNextYearEstimateEPS());;
            analysisStockBuilder.earningAboveEstimates(stockLookup.getEarningAboveEstimates());
            analysisStockBuilder.recommendationRating(stockLookup.getRecommendationRating());
            analysisStockBuilder.errorMessage(stockLookup.getErrorMessage());

            ZacksCode zacksCode = zacksBasicMap.get(stockLookup.getZacksCode());
            if (zacksCode != null) {
                analysisStockBuilder.zacksCompany(zacksCode.getCompany());
                String industry = zacksCode.getIndustry();
                ZacksSectorMapping zacksSectorMapping = zacksSectorMappingMap.get(industry.toUpperCase());
                if (zacksSectorMapping != null) {
                    analysisStockBuilder.sectorGroup(zacksSectorMapping.getSectorGroup());
                    analysisStockBuilder.mediumIndustryGroup(zacksSectorMapping.getMediumIndustryGroup());
                    analysisStockBuilder.industry(zacksSectorMapping.getIndustry());
                } else {
                    log.error("Could not find sector mapping for "+industry);
                }
            } else {
                log.error("Could not find basic zacks for "+stockLookup.getZacksCode());
            }

            PEGStock pegStock = stockAnalyzer.analyzeStocks(stockLookup);

            analysisStockBuilder.thisYearEstimatePE(format(pegStock.getThisYearEstimatePE()));
            analysisStockBuilder.nextYearEstimatePE(format(pegStock.getNextYearEstimatePE()));
            analysisStockBuilder.thisYearEPSGrowth(format(pegStock.getThisYearEPSGrowth()));
            analysisStockBuilder.nextYearEPSGrowth(format(pegStock.getNextYearEPSGrowth()));
            analysisStockBuilder.thisYearPEG(format(pegStock.getThisYearPEG()));
            analysisStockBuilder.nextYearPEG(format(pegStock.getNextYearPEG()));
            analysisStockBuilder.category(pegStock.getCategory());

            return analysisStockBuilder.build();
        }).collect(Collectors.toList());

        log.info("Persisting {} number of analysis stock", analysisStocks.size());
        analysisRepository.saveAll(analysisStocks);
        log.info("Complete persisting {} number of analysis stock", analysisStocks.size());

        eventPublisher.publishEvent(new AnalysisStockCompleteEvent(event.getDate()));
    }

}
