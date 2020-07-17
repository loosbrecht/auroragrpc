package com.loos.auroragrpc.entity;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message extends Type implements Cloneable {

    private List<Type> fields;
    private DynamicMessage.Builder builder;
    private Descriptors.Descriptor descriptor;
    private String innerName;

    public Message(String name, String innerName, List<Type> fields) {
        super(name);
        this.fields = fields;
        this.innerName = innerName;
    }

    public Message(String name) {
        super(name);
    }

    public Message(Message original) {
        super(original.getName());
        this.fields = original.getFields();
        this.builder = original.builder;
        this.descriptor = original.descriptor;
        this.innerName = original.innerName;

    }

    public List<Type> getFields() {
        return fields;
    }

    public void createBuilder(DynamicSchema schema) {
        this.builder = schema.newMessageBuilder(this.getName());
        this.fields.stream().filter(f -> f instanceof Message).forEach(m -> ((Message) m).createBuilder(schema));
    }

    @Override
    public String toString() {
        return "Message{" +
                "fields=" + fields +
                ", builder=" + builder +
                '}';
    }

    public Descriptors.Descriptor getDescriptor() {
        return descriptor;
    }


    public void setDescriptor(DescriptorProtos.FileDescriptorProto descriptorProto) {
        List<DescriptorProtos.DescriptorProto> messageTypeList = descriptorProto.getMessageTypeList();
        for (DescriptorProtos.DescriptorProto proto : messageTypeList) {
            if (this.getName().equals(proto.getName())) {
                this.descriptor = proto.getDescriptorForType();
                return;
            }
        }
    }

    public void setInnerName(String innerName) {
        this.innerName = innerName;
    }

    @Override
    public DynamicMessage Build() {

        return null;
    }

    @Override
    public Map<String, Object> getMessageStructure() {
        Map<String, Object> map = new HashMap<>();
        for (Type field : this.getFields()) {
            Map<String, Object> messageStructure = field.getMessageStructure();
            if (field instanceof Field) {
                map.put(field.getName(), messageStructure.get(field.getName()));
            } else {
                map.put(((Message) field).innerName, messageStructure);
            }
        }
        return map;
    }

    public String getJsonStructure() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(getMessageStructure());
    }
}
