package com.loos.auroragrpc.entity;

import java.util.List;

public class Message extends Type {

    private List<Type> fields;

    public Message(String name, List<Type> fields) {
        super(name);
        this.fields = fields;
    }

    public List<Type> getFields() {
        return fields;
    }

}
