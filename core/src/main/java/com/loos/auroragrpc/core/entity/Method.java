package com.loos.auroragrpc.core.entity;

public class Method {

    private final String name;
    private final Message request;
    private final Message response;

    public Method(String name, Message request, Message response) {
        this.name = name;
        this.request = request;
        this.response = response;
    }

    public Message getRequest() {
        return request;
    }

    public Message getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "Method{" +
                "name='" + name + '\'' +
                ", request=" + request +
                ", response=" + response +
                '}';
    }

    public String getName() {
        return name;
    }
}
