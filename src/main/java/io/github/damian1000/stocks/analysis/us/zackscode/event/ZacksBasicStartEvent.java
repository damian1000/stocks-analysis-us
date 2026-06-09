package io.github.damian1000.stocks.analysis.us.zackscode.event;

import io.github.damian1000.stocks.event.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@Data
@ToString
public class ZacksBasicStartEvent implements Event {
    private LocalDate date;
}
