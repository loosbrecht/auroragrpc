package com.loos.auroragrpc.entity;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;

import java.util.HashMap;
import java.util.Map;

public class Field extends Type {

    private final String typeName;
    private final DescriptorProtos.FieldDescriptorProto.Type descriptorType;


    public Field(String name, DescriptorProtos.FieldDescriptorProto.Type descriptorType, String typeName) {
        super(name);
        this.descriptorType = descriptorType;
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "Field{" +
                "typeName='" + typeName + '\'' +
                '}';
    }

    @Override
    public Map<String, Object> getMessageStructure() {
        Map<String, Object> map = new HashMap<>();
        map.put(this.getName(), this.GetDefaultValueForType());
        return map;
    }

    public Object GetDefaultValueForType() {
        switch (descriptorType) {
            case TYPE_DOUBLE:
                return 0.0;
            case TYPE_FLOAT:
                return (float) 0.0;
            case TYPE_INT64:
            case TYPE_UINT64:
            case TYPE_FIXED64:
            case TYPE_SINT64:
            case TYPE_SFIXED64:
                return (long) 0.0;
            case TYPE_INT32:
            case TYPE_UINT32:
            case TYPE_SINT32:
            case TYPE_SFIXED32:
            case TYPE_FIXED32:
                return 0;
            case TYPE_BOOL:
                return false;
            case TYPE_STRING:
                return "";
            case TYPE_MESSAGE:
                return null; //Should not be possible here
            case TYPE_BYTES:
                return new ByteString[0];
            case TYPE_ENUM:
                return null; //should not be possible here
        }
        return null;
    }

}
