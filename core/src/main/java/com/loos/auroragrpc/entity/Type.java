package com.loos.auroragrpc.entity;

import java.util.Map;

public abstract class Type {

    protected final String name;
    protected  String innerName;
    protected boolean repeated;


    public Type(String name, String innerName) {
        this.name = name;
        this.innerName = innerName;
    }

    public String getName() {
        return name;
    }

    public String getInnerName() {
        return innerName;
    }

    //TODO fix so that repeated objects are shown
    public abstract Map<String, Object> getMessageStructure();


}
