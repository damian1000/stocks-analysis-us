package com.dfh.stock.analysis.us._3zackscode.event;

import com.dfh.event.Event;
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