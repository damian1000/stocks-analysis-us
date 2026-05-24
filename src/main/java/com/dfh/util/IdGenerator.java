package com.dfh.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class IdGenerator {

    public String generateId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static void main(String[] main) {
        IdGenerator.generateId();
    }
}
