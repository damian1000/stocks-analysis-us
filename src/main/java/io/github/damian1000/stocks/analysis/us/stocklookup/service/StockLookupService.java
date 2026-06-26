package io.github.damian1000.stocks.analysis.us.stocklookup.service;

import io.github.damian1000.stocks.analysis.us.zackscode.domain.ZacksCode;
import io.github.damian1000.stocks.analysis.us.zackscode.repository.ZacksBasicRepository;
import io.github.damian1000.stocks.analysis.us.stocklookup.domain.StockLookup;
import io.github.damian1000.stocks.analysis.us.stocklookup.event.StockLookupCompleteEvent;
import io.github.damian1000.stocks.analysis.us.stocklookup.event.StockLookupStartEvent;
import io.github.damian1000.stocks.analysis.us.stocklookup.repository.StockLookupRepository;
import io.github.damian1000.stocks.analysis.us.stocklookup.service.yahoo.YahooStockLookup;
import io.github.damian1000.stocks.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockLookupService {

    private final ZacksBasicRepository zacksBasicRepository;
    private final StockLookupRepository stockLookupRepository;
    private final YahooStockLookup yahooStockLookup;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${stocks.analysis.us.stocklookup.sleeptime.min}")
    private int sleepTimeMin;

    @Value("${stocks.analysis.us.stocklookup.sleeptime.max}")
    private int sleepTimeMax;

    @Value("${stocks.analysis.us.stocklookup.concurrency:8}")
    private int lookupConcurrency;

    @EventListener
    public void onStockLookupStartEvent(StockLookupStartEvent event) {
        String zacksDateString = System.getProperty("zacksDate");
        LocalDate zacksDate;
        if (zacksDateString != null) {
            zacksDate = LocalDate.parse(zacksDateString);
        } else {
            zacksDate = event.getDate();
        }
        log.info("Retrieving zacks basic for date {}", zacksDate);

        Set<ZacksCode> zacksCodeList = zacksBasicRepository.findByDate(zacksDate);
        log.info("Retrieved a total of zacks basic {} for date {}", zacksCodeList.size(), zacksDate);

        Set<StockLookup> existingStockLookup = stockLookupRepository.findByDate(event.getDate());

        log.info("Number of existing stock lookups {} for date {}", existingStockLookup.size(), event.getDate());

        Map<String, StockLookup> zacksCodeMap = existingStockLookup.stream().collect(
                Collectors.toMap(StockLookup::getZacksCode, Function.identity()));

        zacksCodeList = zacksCodeList.stream().filter(z -> !zacksCodeMap.containsKey(z.getZacksCode())).collect(
                Collectors.toSet());

        log.info("Number of zacks code {} after filtering out existing codes", zacksCodeList.size());

        AtomicInteger atomicInteger = new AtomicInteger();
        int count = zacksCodeList.size();
        // Yahoo tolerates concurrent requests well, so run lookups through a bounded pool
        // instead of one-at-a-time: throughput scales with the pool while each request stays
        // a normal call. Pool size is capped at the work size so tiny runs don't oversize it.
        int threads = Math.max(1, Math.min(lookupConcurrency, count));
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Void>> tasks = zacksCodeList.stream()
                    .map(c -> (Callable<Void>) () -> {
                        lookupAndSave(c, event.getDate(), atomicInteger, count);
                        return null;
                    })
                    .collect(Collectors.toList());
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while looking up stocks", e);
        } finally {
            pool.shutdown();
        }

        eventPublisher.publishEvent(new StockLookupCompleteEvent(event.getDate()));
    }

    private void lookupAndSave(ZacksCode c, LocalDate date, AtomicInteger counter, int count) {
        try {
            int i = counter.incrementAndGet();
            log.info("{} out of {} Performing Yahoo lookup for {}", i, count, c);
            StockLookup stockLookup = yahooStockLookup.lookup(c.getZacksCode());
            stockLookup.setDate(date);
            stockLookup.setId(IdGenerator.generateId());
            stockLookup.setZacksCode(c.getZacksCode());
            stockLookupRepository.save(stockLookup);
        } catch (Exception e) {
            log.error("An exception has occurred while performing Yahoo stock lookup for {}", c.getZacksCode(), e);
            stockLookupRepository.save(errorLookup(date, c.getZacksCode(), e));
        }
        sleepBetweenLookups();
    }

    private StockLookup errorLookup(LocalDate date, String zacksCode, Exception e) {
        StockLookup stockLookup = new StockLookup();
        stockLookup.setDate(date);
        stockLookup.setId(IdGenerator.generateId());
        stockLookup.setZacksCode(zacksCode);
        stockLookup.setErrorMessage(truncate(e.getMessage()));
        return stockLookup;
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 200 ? message.substring(0, 200) : message;
    }

    private void sleepBetweenLookups() {
        int min = Math.max(0, sleepTimeMin);
        int max = Math.max(min, sleepTimeMax);
        if (max == 0) {
            return;
        }
        int seconds = ThreadLocalRandom.current().nextInt(min, max + 1);
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while throttling stock lookups", e);
        }
    }

}
