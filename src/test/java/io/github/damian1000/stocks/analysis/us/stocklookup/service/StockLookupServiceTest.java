package io.github.damian1000.stocks.analysis.us.stocklookup.service;

import io.github.damian1000.stocks.analysis.us.zackscode.domain.ZacksCode;
import io.github.damian1000.stocks.analysis.us.zackscode.repository.ZacksBasicRepository;
import io.github.damian1000.stocks.analysis.us.stocklookup.domain.StockLookup;
import io.github.damian1000.stocks.analysis.us.stocklookup.event.StockLookupCompleteEvent;
import io.github.damian1000.stocks.analysis.us.stocklookup.event.StockLookupStartEvent;
import io.github.damian1000.stocks.analysis.us.stocklookup.repository.StockLookupRepository;
import io.github.damian1000.stocks.analysis.us.stocklookup.service.yahoo.YahooStockLookup;
import io.github.damian1000.stocks.exception.DataRetrievalError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StockLookupServiceTest {

    private ZacksBasicRepository zacksBasicRepository;
    private StockLookupRepository stockLookupRepository;
    private YahooStockLookup yahooStockLookup;
    private ApplicationEventPublisher eventPublisher;
    private StockLookupService service;

    @BeforeEach
    void setUp() {
        zacksBasicRepository = mock(ZacksBasicRepository.class);
        stockLookupRepository = mock(StockLookupRepository.class);
        yahooStockLookup = mock(YahooStockLookup.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new StockLookupService(
                zacksBasicRepository, stockLookupRepository, yahooStockLookup, eventPublisher);
        // Force throttle to a no-op; default min/max values from @Value won't be
        // wired up without a real ApplicationContext.
        ReflectionTestUtils.setField(service, "sleepTimeMin", 0);
        ReflectionTestUtils.setField(service, "sleepTimeMax", 0);
    }

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty("zacksDate");
    }

    @Test
    void looksUpEachNonExistingZacksCodeAndSaves() throws DataRetrievalError {
        LocalDate date = LocalDate.of(2024, 6, 1);

        ZacksCode code = newZacks("ACME");
        ZacksCode existingCode = newZacks("EXST");
        when(zacksBasicRepository.findByDate(date)).thenReturn(new LinkedHashSet<>(Set.of(code, existingCode)));

        StockLookup existing = StockLookup.builder().zacksCode("EXST").date(date).build();
        when(stockLookupRepository.findByDate(date)).thenReturn(Set.of(existing));

        StockLookup yahooResult = StockLookup.builder().company("Acme Inc").build();
        when(yahooStockLookup.lookup("ACME")).thenReturn(yahooResult);

        service.onStockLookupStartEvent(new StockLookupStartEvent(date));

        ArgumentCaptor<StockLookup> captor = ArgumentCaptor.forClass(StockLookup.class);
        verify(stockLookupRepository, times(1)).save(captor.capture());
        StockLookup saved = captor.getValue();
        assertEquals("ACME", saved.getZacksCode(), "ACME isn't in existing lookups so it's the only call");
        assertEquals(date, saved.getDate(), "service must stamp the event date");
        assertNotNull(saved.getId(), "service must assign a fresh id");

        verify(eventPublisher).publishEvent(any(StockLookupCompleteEvent.class));
    }

    @Test
    void yahooFailureRecordsAnErrorLookupAndContinues() throws DataRetrievalError {
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(zacksBasicRepository.findByDate(date)).thenReturn(Set.of(newZacks("OOPS")));
        when(stockLookupRepository.findByDate(date)).thenReturn(Set.of());

        when(yahooStockLookup.lookup("OOPS")).thenThrow(new DataRetrievalError(new java.io.IOException("yahoo down")));

        service.onStockLookupStartEvent(new StockLookupStartEvent(date));

        ArgumentCaptor<StockLookup> captor = ArgumentCaptor.forClass(StockLookup.class);
        verify(stockLookupRepository).save(captor.capture());
        StockLookup error = captor.getValue();
        assertEquals("OOPS", error.getZacksCode());
        org.junit.jupiter.api.Assertions.assertNotNull(error.getErrorMessage());

        verify(eventPublisher).publishEvent(any(StockLookupCompleteEvent.class));
    }

    @Test
    void truncatesLongErrorMessagesToTwoHundredChars() throws DataRetrievalError {
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(zacksBasicRepository.findByDate(date)).thenReturn(Set.of(newZacks("BIG")));
        when(stockLookupRepository.findByDate(date)).thenReturn(Set.of());

        // A 400-char message becomes <=200
        String giant = "x".repeat(400);
        when(yahooStockLookup.lookup("BIG")).thenThrow(new RuntimeException(giant));

        service.onStockLookupStartEvent(new StockLookupStartEvent(date));

        ArgumentCaptor<StockLookup> captor = ArgumentCaptor.forClass(StockLookup.class);
        verify(stockLookupRepository).save(captor.capture());
        assertEquals(200, captor.getValue().getErrorMessage().length());
    }

    @Test
    void zacksDateSystemPropertyOverridesEventDate() {
        LocalDate eventDate = LocalDate.of(2024, 6, 1);
        LocalDate zacksDate = LocalDate.of(2024, 5, 1);
        System.setProperty("zacksDate", zacksDate.toString());
        when(zacksBasicRepository.findByDate(zacksDate)).thenReturn(Set.of());
        when(stockLookupRepository.findByDate(eventDate)).thenReturn(Set.of());

        service.onStockLookupStartEvent(new StockLookupStartEvent(eventDate));

        verify(zacksBasicRepository).findByDate(zacksDate);
        verify(stockLookupRepository).findByDate(eventDate);
    }

    private static ZacksCode newZacks(String code) {
        ZacksCode z = new ZacksCode();
        z.setZacksCode(code);
        z.setIndustry("any");
        return z;
    }
}
