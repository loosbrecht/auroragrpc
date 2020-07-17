package com.loos.auroragrpc.entity;

import com.google.protobuf.DynamicMessage;

public abstract class Type {

    private final String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract DynamicMessage Build();
}
