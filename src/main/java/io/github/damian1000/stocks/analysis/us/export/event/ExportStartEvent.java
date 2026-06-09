package io.github.damian1000.stocks.analysis.us.export.event;

import io.github.damian1000.stocks.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@ToString
public class ExportStartEvent implements Event {
    private LocalDate date;
}
