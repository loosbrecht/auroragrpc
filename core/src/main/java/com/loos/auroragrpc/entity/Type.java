package com.loos.auroragrpc.entity;

public abstract class Type {

    private String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
