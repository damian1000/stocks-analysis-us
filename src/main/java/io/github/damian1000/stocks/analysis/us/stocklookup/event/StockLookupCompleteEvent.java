package io.github.damian1000.stocks.analysis.us.stocklookup.event;

import io.github.damian1000.stocks.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@ToString
public class StockLookupCompleteEvent implements Event {
    private LocalDate date;
}
