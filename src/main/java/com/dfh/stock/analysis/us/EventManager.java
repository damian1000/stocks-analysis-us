package com.dfh.stock.analysis.us;

import com.dfh.event.Event;
import com.dfh.stock.analysis.us._1sectormapping.event.ZacksSectorMappingCompleteEvent;
import com.dfh.stock.analysis.us._2zacksindustry.event.ZacksListCompleteEvent;
import com.dfh.stock.analysis.us._2zacksindustry.event.ZacksListStartEvent;
import com.dfh.stock.analysis.us._3zackscode.event.ZacksBasicCompleteEvent;
import com.dfh.stock.analysis.us._3zackscode.event.ZacksBasicStartEvent;
import com.dfh.stock.analysis.us._4stocklookup.event.StockLookupCompleteEvent;
import com.dfh.stock.analysis.us._4stocklookup.event.StockLookupStartEvent;
import com.dfh.stock.analysis.us._5analysis.event.AnalysisStockCompleteEvent;
import com.dfh.stock.analysis.us._5analysis.event.AnalysisStockStartEvent;
import com.dfh.stock.analysis.us._6export.event.ExportCompleteEvent;
import com.dfh.stock.analysis.us._6export.event.ExportStartEvent;
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

