package io.github.damian1000.stocks.exception;

public class DataRetrievalError extends Exception {
    public DataRetrievalError(Exception e) {
        super(e);
    }
}
