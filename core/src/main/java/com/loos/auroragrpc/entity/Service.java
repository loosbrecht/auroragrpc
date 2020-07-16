package com.loos.auroragrpc.entity;

import java.util.List;

public class Service {
    private String name;
    private String packageName;
    private List<Method> methods;

    public Service(String name, String packageName, List<Method> methods) {
        this.name = name;
        this.packageName = packageName;
        this.methods = methods;
    }
}
