package io.github.damian1000.stocks.analysis.us._5analysis.service;

import io.github.damian1000.stocks.analysis.us._1sectormapping.domain.ZacksSectorMapping;
import io.github.damian1000.stocks.analysis.us._1sectormapping.repository.ZacksSectorMappingRepository;
import io.github.damian1000.stocks.analysis.us._3zackscode.domain.ZacksCode;
import io.github.damian1000.stocks.analysis.us._3zackscode.repository.ZacksBasicRepository;
import io.github.damian1000.stocks.analysis.us._4stocklookup.domain.StockLookup;
import io.github.damian1000.stocks.analysis.us._4stocklookup.repository.StockLookupRepository;
import io.github.damian1000.stocks.analysis.us._5analysis.domain.AnalysisStock;
import io.github.damian1000.stocks.analysis.us._5analysis.domain.PEGStock;
import io.github.damian1000.stocks.analysis.us._5analysis.event.AnalysisStockCompleteEvent;
import io.github.damian1000.stocks.analysis.us._5analysis.event.AnalysisStockStartEvent;
import io.github.damian1000.stocks.analysis.us._5analysis.repository.AnalysisRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalysisServiceTest {

    private StockLookupRepository stockLookupRepository;
    private AnalysisRepository analysisRepository;
    private ZacksBasicRepository zacksBasicRepository;
    private ZacksSectorMappingRepository zacksSectorMappingRepository;
    private PEGStockAnalyzer pegStockAnalyzer;
    private ApplicationEventPublisher eventPublisher;
    private AnalysisService service;

    @BeforeEach
    void setUp() {
        stockLookupRepository = mock(StockLookupRepository.class);
        analysisRepository = mock(AnalysisRepository.class);
        zacksBasicRepository = mock(ZacksBasicRepository.class);
        zacksSectorMappingRepository = mock(ZacksSectorMappingRepository.class);
        pegStockAnalyzer = mock(PEGStockAnalyzer.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new AnalysisService(
                stockLookupRepository,
                analysisRepository,
                zacksBasicRepository,
                zacksSectorMappingRepository,
                pegStockAnalyzer,
                eventPublisher);
    }

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty("zacksDate");
    }

    @Test
    void joinsLookupZacksAndSectorMappingAndPersistsAnalysisStocks() {
        LocalDate date = LocalDate.of(2024, 5, 1);
        StockLookup lookup = StockLookup.builder()
                .id("L1").date(date).zacksCode("ZC1").company("Acme")
                .currency("USD").marketCap(BigDecimal.TEN).yearEnding("12")
                .price(BigDecimal.valueOf(50)).lastYearEPS(BigDecimal.ONE)
                .thisYearEstimateEPS(BigDecimal.valueOf(2))
                .nextYearEstimateEPS(BigDecimal.valueOf(3))
                .build();
        when(stockLookupRepository.findByDate(date)).thenReturn(Set.of(lookup));

        ZacksCode zacksCode = new ZacksCode();
        zacksCode.setZacksCode("ZC1");
        zacksCode.setCompany("Acme Zacks Co");
        zacksCode.setIndustry("Software");
        when(zacksBasicRepository.findByDate(date)).thenReturn(Set.of(zacksCode));

        ZacksSectorMapping mapping = new ZacksSectorMapping();
        mapping.setIndustry("Software");
        mapping.setSectorGroup("Tech");
        mapping.setMediumIndustryGroup("Apps");
        when(zacksSectorMappingRepository.findByDate(date)).thenReturn(List.of(mapping));

        when(pegStockAnalyzer.analyzeStocks(lookup)).thenReturn(PEGStock.builder()
                .zacksCode("ZC1")
                .thisYearEstimatePE(BigDecimal.valueOf(25)).nextYearEstimatePE(BigDecimal.valueOf(16.67))
                .thisYearEPSGrowth(BigDecimal.valueOf(50)).nextYearEPSGrowth(BigDecimal.valueOf(33.33))
                .thisYearPEG(BigDecimal.valueOf(0.5)).nextYearPEG(BigDecimal.valueOf(0.5))
                .category("00 Good")
                .build());

        service.onAnalysisServiceEvent(new AnalysisStockStartEvent(date));

        verify(analysisRepository).deleteByDate(date);
        ArgumentCaptor<List<AnalysisStock>> captor = ArgumentCaptor.forClass(List.class);
        verify(analysisRepository).saveAll(captor.capture());
        List<AnalysisStock> persisted = captor.getValue();
        assertEquals(1, persisted.size());
        AnalysisStock built = persisted.get(0);
        assertEquals("Tech", built.getSectorGroup());
        assertEquals("Apps", built.getMediumIndustryGroup());
        assertEquals("Software", built.getIndustry());
        assertEquals("Acme Zacks Co", built.getZacksCompany());
        assertEquals("00 Good", built.getCategory());
        verify(eventPublisher).publishEvent(any(AnalysisStockCompleteEvent.class));
    }

    @Test
    void missingZacksAndSectorMappingPersistsAStockWithoutThoseFields() {
        LocalDate date = LocalDate.of(2024, 5, 1);
        StockLookup lookup = StockLookup.builder()
                .id("L2").date(date).zacksCode("UNKNOWN").company("Mystery").build();
        when(stockLookupRepository.findByDate(date)).thenReturn(Set.of(lookup));
        when(zacksBasicRepository.findByDate(date)).thenReturn(Set.of()); // no zacks
        when(zacksSectorMappingRepository.findByDate(date)).thenReturn(List.of());
        when(pegStockAnalyzer.analyzeStocks(lookup)).thenReturn(PEGStock.builder()
                .category("20 Reuters Lookup Invalid").build());

        service.onAnalysisServiceEvent(new AnalysisStockStartEvent(date));

        ArgumentCaptor<List<AnalysisStock>> captor = ArgumentCaptor.forClass(List.class);
        verify(analysisRepository).saveAll(captor.capture());
        AnalysisStock built = captor.getValue().get(0);
        // No zacks/sector found -> these stay null but the row still gets saved
        org.junit.jupiter.api.Assertions.assertNull(built.getSectorGroup());
        org.junit.jupiter.api.Assertions.assertNull(built.getZacksCompany());
        assertEquals("20 Reuters Lookup Invalid", built.getCategory());
    }

    @Test
    void zacksDateSystemPropertyOverridesEventDate() {
        LocalDate eventDate = LocalDate.of(2024, 5, 1);
        LocalDate zacksDate = LocalDate.of(2024, 4, 1);
        System.setProperty("zacksDate", zacksDate.toString());

        when(stockLookupRepository.findByDate(eventDate)).thenReturn(Set.of());
        when(zacksBasicRepository.findByDate(zacksDate)).thenReturn(Set.of());
        when(zacksSectorMappingRepository.findByDate(zacksDate)).thenReturn(List.of());

        service.onAnalysisServiceEvent(new AnalysisStockStartEvent(eventDate));

        verify(zacksBasicRepository).findByDate(zacksDate);
        verify(zacksSectorMappingRepository).findByDate(zacksDate);
    }
}
