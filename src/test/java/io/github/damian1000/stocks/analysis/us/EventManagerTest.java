package io.github.damian1000.stocks.analysis.us;

import io.github.damian1000.stocks.analysis.us.sectormapping.event.ZacksSectorMappingCompleteEvent;
import io.github.damian1000.stocks.analysis.us.zacksindustry.event.ZacksListCompleteEvent;
import io.github.damian1000.stocks.analysis.us.zacksindustry.event.ZacksListStartEvent;
import io.github.damian1000.stocks.analysis.us.zackscode.event.ZacksBasicCompleteEvent;
import io.github.damian1000.stocks.analysis.us.zackscode.event.ZacksBasicStartEvent;
import io.github.damian1000.stocks.analysis.us.stocklookup.event.StockLookupCompleteEvent;
import io.github.damian1000.stocks.analysis.us.stocklookup.event.StockLookupStartEvent;
import io.github.damian1000.stocks.analysis.us.analysis.event.AnalysisStockCompleteEvent;
import io.github.damian1000.stocks.analysis.us.analysis.event.AnalysisStockStartEvent;
import io.github.damian1000.stocks.analysis.us.export.event.ExportCompleteEvent;
import io.github.damian1000.stocks.analysis.us.export.event.ExportStartEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class EventManagerTest {

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final EventManager manager = new EventManager(publisher);

    private static final LocalDate DATE = LocalDate.of(2026, 1, 15);

    @Test
    void sectorMappingCompleteTriggersZacksListStart() {
        manager.onZacksSectorMappingCompleteEvent(new ZacksSectorMappingCompleteEvent(DATE));
        assertCascades(ZacksListStartEvent.class);
    }

    @Test
    void zacksListCompleteTriggersZacksBasicStart() {
        manager.onZacksListCompleteEvent(new ZacksListCompleteEvent(DATE));
        assertCascades(ZacksBasicStartEvent.class);
    }

    @Test
    void zacksBasicCompleteTriggersStockLookupStart() {
        manager.onZacksBasicCompleteEvent(new ZacksBasicCompleteEvent(DATE));
        assertCascades(StockLookupStartEvent.class);
    }

    @Test
    void stockLookupCompleteTriggersAnalysisStockStart() {
        manager.onStockLookupCompleteEvent(new StockLookupCompleteEvent(DATE));
        assertCascades(AnalysisStockStartEvent.class);
    }

    @Test
    void analysisStockCompleteTriggersExportStart() {
        manager.onAnalysisStockCompleteEvent(new AnalysisStockCompleteEvent(DATE));
        assertCascades(ExportStartEvent.class);
    }

    @Test
    void exportCompleteIsTerminalAndPublishesNothing() {
        manager.onExportCompleteEvent(new ExportCompleteEvent(DATE));
        verifyNoInteractions(publisher);
    }

    private <T> void assertCascades(Class<T> expected) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        assertInstanceOf(expected, captor.getValue());
    }
}
