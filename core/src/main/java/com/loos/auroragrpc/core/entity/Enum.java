package com.loos.auroragrpc.core.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Enum extends Type {

    private final List<String> values;
    private String innerName;

    public Enum(String name, List<String> values) {
        super(name, name);
        this.values = values;
    }

    public Enum(Enum other) {
        super(other.getName(), other.innerName);
        this.values = other.values;
    }

    @Override
    public Map<String, Object> getMessageStructure() {
        Map<String, Object> map = new HashMap<>();
        map.put(this.getInnerName(), "");
        return map;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isValidValue(String val) {
        return values.contains(val);
    }

    public String getInnerName() {
        return innerName;
    }

    public void setInnerName(String innerName) {
        this.innerName = innerName;
    }
}
