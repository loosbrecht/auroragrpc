package com.loos.auroragrpc.shell.entity;

import java.util.Map;

public class Executor {

    private String method;
    private Map<String,Object> input;
    private String name;

    public Executor(){

    }

    public Executor(String name, String method, Map<String, Object> input) {
        this.name = name;
        this.method = method;
        this.input = input;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }
}
