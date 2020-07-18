package com.loos.auroragrpc.entity;

import java.util.Map;

public abstract class Type {

    private final String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract Map<String, Object> getMessageStructure();
}
