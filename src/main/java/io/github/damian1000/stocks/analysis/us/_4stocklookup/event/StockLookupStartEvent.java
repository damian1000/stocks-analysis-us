package io.github.damian1000.stocks.analysis.us._4stocklookup.event;

import io.github.damian1000.stocks.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@ToString
public class StockLookupStartEvent implements Event {
    private LocalDate date;
}
