package io.github.damian1000.stocks.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class IdGenerator {

    public String generateId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
