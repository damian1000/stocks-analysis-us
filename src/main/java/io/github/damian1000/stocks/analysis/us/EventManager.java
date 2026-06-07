package io.github.damian1000.stocks.analysis.us;

import io.github.damian1000.stocks.event.Event;
import io.github.damian1000.stocks.analysis.us._1sectormapping.event.ZacksSectorMappingCompleteEvent;
import io.github.damian1000.stocks.analysis.us._2zacksindustry.event.ZacksListCompleteEvent;
import io.github.damian1000.stocks.analysis.us._2zacksindustry.event.ZacksListStartEvent;
import io.github.damian1000.stocks.analysis.us._3zackscode.event.ZacksBasicCompleteEvent;
import io.github.damian1000.stocks.analysis.us._3zackscode.event.ZacksBasicStartEvent;
import io.github.damian1000.stocks.analysis.us._4stocklookup.event.StockLookupCompleteEvent;
import io.github.damian1000.stocks.analysis.us._4stocklookup.event.StockLookupStartEvent;
import io.github.damian1000.stocks.analysis.us._5analysis.event.AnalysisStockCompleteEvent;
import io.github.damian1000.stocks.analysis.us._5analysis.event.AnalysisStockStartEvent;
import io.github.damian1000.stocks.analysis.us._6export.event.ExportCompleteEvent;
import io.github.damian1000.stocks.analysis.us._6export.event.ExportStartEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class EventManager {

    private ApplicationEventPublisher eventPublisher;
    
    @EventListener
    public void onZacksSectorMappingCompleteEvent(ZacksSectorMappingCompleteEvent event) {
        log.info("onZacksSectorMappingCompleteEvent {}", event);
        Event nextEvent = new ZacksListStartEvent(event.getDate());
        log.info("publishing next event {}", nextEvent);
        eventPublisher.publishEvent(nextEvent);
    }

    @EventListener
    public void onZacksListCompleteEvent(ZacksListCompleteEvent event) {
        log.info("onZacksListCompleteEvent {}", event);
        Event nextEvent = new ZacksBasicStartEvent(event.getDate());
        log.info("publishing next event {}", nextEvent);
        eventPublisher.publishEvent(nextEvent);
    }
    
    @EventListener
    public void onZacksBasicCompleteEvent(ZacksBasicCompleteEvent event) {
        log.info("onZacksBasicCompleteEvent {}", event);
        Event nextEvent = new StockLookupStartEvent(event.getDate());
        log.info("publishing next event {}", nextEvent);
        eventPublisher.publishEvent(nextEvent);
    }

    @EventListener
    public void onStockLookupCompleteEvent(StockLookupCompleteEvent event) {
        log.info("onStockLookupCompleteEvent {}", event);
        Event nextEvent = new AnalysisStockStartEvent(event.getDate());
        log.info("publishing next event {}", nextEvent);
        eventPublisher.publishEvent(nextEvent);
    }
    
    @EventListener
    public void onAnalysisStockCompleteEvent(AnalysisStockCompleteEvent event) {
        log.info("onAnalysisStockCompleteEvent {}", event);
        Event nextEvent = new ExportStartEvent(event.getDate());
        log.info("publishing next event {}", nextEvent);
        eventPublisher.publishEvent(nextEvent);
    }
    
    @EventListener
    public void onExportCompleteEvent(ExportCompleteEvent event) {
       log.info("onExportCompleteEvent {}", event);
    }

}

