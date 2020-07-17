package com.loos.auroragrpc.entity;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.DynamicMessage;

import java.util.List;

public class Message extends Type {

    private List<Type> fields;
    private DynamicMessage.Builder builder;

    public Message(String name, List<Type> fields) {
        super(name);
        this.fields = fields;
    }

    public List<Type> getFields() {
        return fields;
    }

    public void createBuilder(DynamicSchema schema) {
        this.builder = schema.newMessageBuilder(this.getName());
    }

    @Override
    public String toString() {
        return "Message{" +
                "fields=" + fields +
                ", builder=" + builder +
                '}';
    }
}
