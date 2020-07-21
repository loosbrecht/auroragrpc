package com.loos.auroragrpc.entity;

public class InvalidValueException extends Exception {
    public InvalidValueException(String name) {
        super(name);
    }
}
