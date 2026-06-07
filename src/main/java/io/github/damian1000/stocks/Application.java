package io.github.damian1000.stocks;

import io.github.damian1000.stocks.event.Event;
import io.github.damian1000.stocks.analysis.us._1sectormapping.event.ZacksSectorMappingStartEvent;
import io.github.damian1000.stocks.analysis.us._2zacksindustry.event.ZacksListStartEvent;
import io.github.damian1000.stocks.analysis.us._3zackscode.event.ZacksBasicStartEvent;
import io.github.damian1000.stocks.analysis.us._4stocklookup.event.StockLookupStartEvent;
import io.github.damian1000.stocks.analysis.us._5analysis.event.AnalysisStockStartEvent;
import io.github.damian1000.stocks.analysis.us._6export.event.ExportStartEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Configuration
@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {
        String zacksDate = System.getProperty("zacksDate");
        String reutersCodeDate = System.getProperty("reutersCodeDate");
        String event = System.getProperty("event");
        String dateAsString = System.getProperty("date");

        log.info("System parameters zacksDate={}, reutersCodeDate={}, event={}, date={}", zacksDate, reutersCodeDate,
                event, dateAsString);

        LocalDate date;
        if (!StringUtils.isEmpty(dateAsString)) {
            date = LocalDate.parse(dateAsString);
        } else {
            date = LocalDate.now();
        }

        Event startEvent;
        if (!StringUtils.isEmpty(event)) {
            Map<String, Event> events = new HashMap<>();
            events.put(ZacksSectorMappingStartEvent.class.getSimpleName(), new ZacksSectorMappingStartEvent(date));
            events.put(ZacksListStartEvent.class.getSimpleName(), new ZacksListStartEvent(date));
            events.put(ZacksBasicStartEvent.class.getSimpleName(), new ZacksBasicStartEvent(date));
            events.put(StockLookupStartEvent.class.getSimpleName(), new StockLookupStartEvent(date));
            events.put(AnalysisStockStartEvent.class.getSimpleName(), new AnalysisStockStartEvent(date));
            events.put(ExportStartEvent.class.getSimpleName(), new ExportStartEvent(date));
            startEvent = events.get(event);
        } else {
            startEvent = new ZacksSectorMappingStartEvent(date);
        }

        log.info("Starting spring");
        ApplicationContext context = SpringApplication.run(Application.class, args);
        log.info("Completed starting spring");

        log.info("Publishing event {} ", startEvent);

        context.publishEvent(startEvent);
        log.info("Completed publishing event {} ", startEvent);

        System.exit(0);
    }

}
