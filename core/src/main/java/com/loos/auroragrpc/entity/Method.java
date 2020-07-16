package com.loos.auroragrpc.entity;

public class Method {

    private final String name;
    private final Message request;
    private final Message response;

    public Method(String name, Message request, Message response) {
        this.name = name;
        this.request = request;
        this.response = response;
    }

}
