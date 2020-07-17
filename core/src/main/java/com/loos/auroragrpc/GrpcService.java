package com.loos.auroragrpc;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.loos.auroragrpc.entity.Message;
import com.loos.auroragrpc.entity.Service;

import java.util.List;

public class GrpcService {

    //TODO support multiple services
    private final Service service;
    private final List<Message> messageList;
    private DynamicSchema schema;


    public GrpcService(DynamicSchema schema, Service service, List<Message> messageList) {
        this.schema = schema;
        this.service = service;
        this.messageList = messageList;
    }

    public GrpcService(Service service, List<Message> messageList) {
        this.service = service;
        this.messageList = messageList;
    }

    public DynamicSchema getSchema() {
        return schema;
    }

    public void setSchema(DynamicSchema schema) {
        this.schema = schema;
    }

    public Service getService() {
        return service;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    @Override
    public String toString() {
        return "GrpcService{" +
                "schema=" + schema +
                ", service=" + service +
                ", messageList=" + messageList +
                '}';
    }
}
