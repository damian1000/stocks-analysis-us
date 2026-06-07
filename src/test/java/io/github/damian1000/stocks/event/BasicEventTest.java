package io.github.damian1000.stocks.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicEventTest {

    @Test
    void exposesTheDateViaInterface() {
        LocalDate date = LocalDate.of(2024, 7, 4);
        Event event = new BasicEvent(date);
        assertEquals(date, event.getDate());
    }
}
