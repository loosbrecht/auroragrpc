package com.loos.auroragrpc.entity;

import com.google.protobuf.DynamicMessage;

import java.util.HashMap;
import java.util.Map;

public class Enum extends Type {

    public Enum(String name) {
        super(name);
    }

    @Override
    public DynamicMessage Build() {
        return null;
    }

    @Override
    public Map<String, Object> getMessageStructure() {
        Map<String, Object> map = new HashMap<>();
        map.put(this.getName(), "");
        return map;
    }
}
