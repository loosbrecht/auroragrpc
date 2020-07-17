package com.loos.auroragrpc.entity;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

import java.util.List;

public class Message extends Type {

    private List<Type> fields;
    private DynamicMessage.Builder builder;
    private Descriptors.Descriptor descriptor;

    public Message(String name, List<Type> fields) {
        super(name);
        this.fields = fields;
    }

    public Message(String name) {
        super(name);
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

    @Override
    public DynamicMessage Build() {

        return null;
    }
}
