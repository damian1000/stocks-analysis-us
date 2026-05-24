package com.dfh.exception;

public class DataRetrievalError extends Exception {
    public DataRetrievalError(Exception e) {
        super(e);
    }
}
