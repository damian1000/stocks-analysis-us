package io.github.damian1000.stocks.analysis.us._5analysis.event;

import io.github.damian1000.stocks.event.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@Data
@ToString
public class AnalysisStockCompleteEvent implements Event {
    private LocalDate date;
}
