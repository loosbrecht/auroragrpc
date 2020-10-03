package com.loos.auroragrpc.shell.entity;

import java.util.List;
import java.util.Map;

public class JsonInput {

    private String service;
    private String host;
    private List<Executor> execute;

    public JsonInput(){

    }

    public JsonInput(String service,String host, List<Executor> execute) {
        this.service = service;
        this.execute = execute;
        this.host = host;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<Executor> getExecute() {
        return execute;
    }

    public void setExecute(List<Executor> execute) {
        this.execute = execute;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
