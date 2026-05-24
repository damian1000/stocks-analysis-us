package com.dfh.stock.analysis.us._5analysis.event;

import com.dfh.event.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@Data
@ToString
public class AnalysisStockStartEvent implements Event {
    private LocalDate date;
}
