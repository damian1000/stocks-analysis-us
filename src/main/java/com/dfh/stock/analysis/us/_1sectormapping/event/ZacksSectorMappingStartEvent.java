package com.dfh.stock.analysis.us._1sectormapping.event;

import com.dfh.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@ToString
public class ZacksSectorMappingStartEvent implements Event {
    private LocalDate date;
}
