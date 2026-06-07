package io.github.damian1000.stocks.analysis.us.zacksindustry.service;

import io.github.damian1000.stocks.analysis.us.zacksindustry.domain.ZacksList;
import io.github.damian1000.stocks.analysis.us.zacksindustry.event.ZacksListCompleteEvent;
import io.github.damian1000.stocks.analysis.us.zacksindustry.event.ZacksListStartEvent;
import io.github.damian1000.stocks.analysis.us.zacksindustry.repository.ZacksListRepository;
import io.github.damian1000.stocks.exception.DataRetrievalError;
import io.github.damian1000.stocks.html.HtmlResponse;
import io.github.damian1000.stocks.html.HtmlRetriever;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZacksListRetrieverServiceTest {

    private HtmlRetriever htmlRetriever;
    private ZacksListRepository repository;
    private ApplicationEventPublisher eventPublisher;
    private ZacksListRetrieverService service;

    @BeforeEach
    void setUp() {
        htmlRetriever = mock(HtmlRetriever.class);
        repository = mock(ZacksListRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new ZacksListRetrieverService(htmlRetriever, repository, eventPublisher);
    }

    @Test
    void parsesJsonDataArrayAndSavesEntries() throws DataRetrievalError {
        String json = "{\"data\":[" +
                "{\"industry_id\":\"5\"," +
                "  \"industry_name\":\"<a href='zacks.com/x'>Banking</a>\"," +
                "  \"no_of_stocks\":\"42\"," +
                "  \"junk_array\":[1,2,3]," +
                "  \"junk_object\":{\"k\":\"v\"}," +
                "  \"ignored_scalar\":\"hello\"}," +
                "{\"industry_id\":\"2\"," +
                "  \"industry_name\":\"<a href='zacks.com/y'>Tech</a>\"," +
                "  \"no_of_stocks\":\"7\"}" +
                "],\"meta\":{\"k\":\"v\"}}";
        HtmlResponse response = new HtmlResponse();
        response.parsedHtml = json;
        when(htmlRetriever.getHtml(anyString())).thenReturn(response);

        LocalDate date = LocalDate.of(2024, 6, 1);
        service.onZacksListStartEvent(new ZacksListStartEvent(date));

        ArgumentCaptor<ZacksList> captor = ArgumentCaptor.forClass(ZacksList.class);
        verify(repository, times(2)).save(captor.capture());
        // Sorted ascending by industry index
        assertEquals("2", captor.getAllValues().get(0).getIndex());
        assertEquals("Tech", captor.getAllValues().get(0).getIndustry());
        assertEquals("5", captor.getAllValues().get(1).getIndex());
        assertEquals("Banking", captor.getAllValues().get(1).getIndustry());
        assertEquals("42", captor.getAllValues().get(1).getTotal());
        assertEquals(date, captor.getAllValues().get(1).getDate());

        verify(repository).deleteByDate(date);
        verify(eventPublisher).publishEvent(any(ZacksListCompleteEvent.class));
    }

    @Test
    void retrieverFailureSurfacesAsIllegalState() throws DataRetrievalError {
        when(htmlRetriever.getHtml(anyString())).thenThrow(new DataRetrievalError(new IOException("net")));
        assertThrows(IllegalStateException.class,
                () -> service.onZacksListStartEvent(new ZacksListStartEvent(LocalDate.now())));
    }
}
