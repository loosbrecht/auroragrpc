package com.loos.auroragrpc.entity;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message extends Type {

    private List<Type> fields;
    private DynamicMessage.Builder builder;
    private Descriptors.Descriptor descriptor;

    public Message(String name, String innerName, List<Type> fields) {
        super(name, innerName);
        this.fields = fields;
    }

    public Message(String name) {
        super(name, name);
    }


    public Message(Message original) {
        super(original.getName(), original.innerName);
        this.fields = original.getFields();
        this.builder = original.builder;
        this.descriptor = original.descriptor;
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

    public DynamicMessage build(Map<String, Object> input) throws InvalidValueException {
        Descriptors.Descriptor descr = builder.getDescriptorForType();
        for (Type field : this.fields) {
            Object fieldValue = null;
            Descriptors.FieldDescriptor fieldDescriptor = null;
            if (field instanceof Field && input.containsKey(field.getInnerName())) {
                fieldDescriptor = descr.findFieldByName(field.getName());
                fieldValue = input.get(field.getName());
            } else if (field instanceof Message && input.containsKey(field.getInnerName())) {
                Message msg = (Message) field;
                fieldDescriptor = descr.findFieldByName(msg.getInnerName());
                if (input.get(msg.getInnerName()) instanceof List) {
                    List<Object> lst = new ArrayList<>();
                    for (Object v : (List<?>) input.get(msg.getInnerName())) {
                        DynamicMessage fValue = msg.build((Map<String, Object>) v);
                        lst.add(fValue);
                    }
                    fieldValue = lst;
                } else {
                    Map<String, Object> value = (Map<String, Object>) input.get(msg.getInnerName());
                    fieldValue = msg.build(value);
                }
            } else if (field instanceof Enum && input.containsKey(field.getInnerName())) {
                Enum en = (Enum) field;
                fieldDescriptor = descr.findFieldByName(en.getInnerName());
                Descriptors.EnumDescriptor enumType = fieldDescriptor.getEnumType();
                String val = (String) input.get(en.getInnerName());
                if (!en.isValidValue(val)) {
                    throw new InvalidValueException(en.getInnerName());
                }
                Descriptors.EnumValueDescriptor enumValue = enumType.findValueByName(val);
                fieldValue = enumValue;
            }
            if (fieldDescriptor == null) {
                continue;
            }
            if (field.repeated) {
                if (fieldValue instanceof Object[]) {
                    for (Object v : (Object[]) fieldValue) {
                        builder.addRepeatedField(fieldDescriptor, v);
                    }
                } else if (fieldValue instanceof List) {
                    for (Object v : (List<?>) fieldValue) {
                        builder.addRepeatedField(fieldDescriptor, v);
                    }
                }
            } else {
                builder.setField(fieldDescriptor, fieldValue);
            }
        }
        return builder.build();
    }

    @Override
    public Map<String, Object> getMessageStructure() {
        Map<String, Object> map = new HashMap<>();
        for (Type field : this.getFields()) {
            Map<String, Object> messageStructure = field.getMessageStructure();
            Object value = null;
            if (field instanceof Field || field instanceof Enum) {
                value = messageStructure.get(field.getInnerName());
            } else {
                value = messageStructure;
            }
            if (field.repeated) {
                List<Object> lst = new ArrayList<>();
                lst.add(value);
                map.put(field.getInnerName(), lst);
            } else {
                map.put(field.getInnerName(), value);
            }
        }
        return map;
    }

    public void addAdditionalAttributes() {
        Descriptors.Descriptor descr = builder.getDescriptorForType();
        for (Type field : this.fields) {
            Descriptors.FieldDescriptor fieldDescr = descr.findFieldByName(field.getInnerName());
            if (field instanceof Message) {
                ((Message) field).addAdditionalAttributes();
            }
            field.repeated = fieldDescr.isRepeated();
        }
    }

    public String getJsonStructure() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(getMessageStructure());
    }
}
