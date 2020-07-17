package com.loos.auroragrpc.entity;

public class Field extends Type {

    private String typeName;


    public Field(String name,String typeName) {
        super(name);
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "Field{" +
                "typeName='" + typeName + '\'' +
                '}';
    }
}
