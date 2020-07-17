package com.loos.auroragrpc.entity;

import java.util.List;

public class Service {
    private final String name;
    private final String packageName;
    private final List<Method> methods;

    public Service(String name, String packageName, List<Method> methods) {
        this.name = name;
        this.packageName = packageName;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", methods=" + methods +
                '}';
    }

    public String GetFullServiceName() {
        return packageName + "." + name;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<Method> getMethods() {
        return methods;
    }
}
