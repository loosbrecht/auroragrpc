package com.loos.auroragrpc.shell.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.loos.auroragrpc.core.GrpcService;
import com.loos.auroragrpc.core.entity.InvalidValueException;
import com.loos.auroragrpc.core.entity.Message;
import com.loos.auroragrpc.core.protobuf.ProtobufInterpreter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    void build() throws InvalidValueException {
        Message message = grpcService.getMessageList().get(0);
        Map<String, Object> helloRequest = new HashMap<>();
        helloRequest.put("name", "hellooo");
        helloRequest.put("day", "MONDAY");
        Map<String, Object> inner = new HashMap<>();
        inner.put("inner", "boeh");
        inner.put("numbers", new Double[]{
                3.14, 22.23
        });
        inner.put("correct", true);

        Map<String, Object> inner2 = new HashMap<>();
        inner2.put("inner", "waaaaaah");
        inner2.put("numbers", new Double[]{
                3.14, 22.23, 8888.0, 444.2
        });
        List<Map<String, Object>> lst = new ArrayList<>();
        lst.add(inner);
        lst.add(inner2);
        helloRequest.put("repeatedInner", lst);
        DynamicMessage build = message.build(helloRequest);
        System.out.println(build);
    }

    @Test
    void buildFromString() throws InvalidValueException, JsonProcessingException {
        Message msg = grpcService.getMessageList().get(0);
        System.out.println(msg.getName());
        String input = "{\"repeatedInner\":[{\"correct\":true,\"numbers\":[1.4,34.2],\"inner\":\"dit is een experiment\"},{\"numbers\":[1.2,3.14],\"inner\":\"dit is een nog iets anders\"}],\"name\":\"dit is de naame\",\"day\":\"MONDAY\"}";
        Map<String, Object> mp = GetMessageInput(input);
        DynamicMessage build = msg.build(mp);
        System.out.println(build);
    }
    private Map<String, Object> GetMessageInput(String input) throws  JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(input, Map.class);
        return map;
    }
}