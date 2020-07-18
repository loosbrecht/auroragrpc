package com.loos.auroragrpc.entity;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.loos.auroragrpc.GrpcService;
import com.loos.auroragrpc.protobuf.ProtobufInterpreter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

class MessageTest {


    InputStream protoFile;
    GrpcService grpcService;

    @BeforeEach
    void setUp() throws IOException, Descriptors.DescriptorValidationException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("descriptor");
        assert url != null;
        File f = new File(url.getPath());
        protoFile = new FileInputStream(f);
        ProtobufInterpreter protobufInterpreter = new ProtobufInterpreter(protoFile);
        grpcService = protobufInterpreter.parseProtobufFile();

    }

    @Test
    void build() {
        Message message = grpcService.getMessageList().get(0);
        Map<String, Object> helloRequest = new HashMap<>();
        helloRequest.put("name", "hellooo");
        Map<String, Object> inner = new HashMap<>();
        inner.put("inner", "boeh");
        inner.put("numbers", new Double[]{
                3.14, 22.23
        });
        inner.put("correct", true);
        helloRequest.put("inner", inner);
        DynamicMessage build = message.build(helloRequest);
        System.out.println(build);
    }
}