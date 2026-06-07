package io.github.damian1000.stocks.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BasicEvent implements Event {

    private LocalDate date;

    @Override
    public LocalDate getDate() {
        return date;
    }
}
