package com.loos.auroragrpc.entity;

import java.util.Map;

public abstract class Type {

    protected final String name;
    protected boolean repeated;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    //TODO fix so that repeated objects are shown
    public abstract Map<String, Object> getMessageStructure();


}
