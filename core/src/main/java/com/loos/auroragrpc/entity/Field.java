package com.loos.auroragrpc.entity;

import com.google.protobuf.DynamicMessage;

public class Field extends Type {

    private final String typeName;


    public Field(String name, String typeName) {
        super(name);
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "Field{" +
                "typeName='" + typeName + '\'' +
                '}';
    }

    @Override
    public DynamicMessage Build() {

        return null;
    }
}
