package com.loos.auroragrpc.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.loos.auroragrpc.GrpcService;
import com.loos.auroragrpc.entity.InvalidValueException;
import com.loos.auroragrpc.entity.Message;
import com.loos.auroragrpc.entity.Method;
import com.loos.auroragrpc.entity.Service;
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

class GrpcClientTest {


    InputStream protoFile;
    GrpcService grpcService;
    DynamicMessage dynMsg;

    @BeforeEach
    void setUp() throws IOException, Descriptors.DescriptorValidationException, InvalidValueException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("descriptor");
        assert url != null;
        File f = new File(url.getPath());
        protoFile = new FileInputStream(f);
        ProtobufInterpreter protobufInterpreter = new ProtobufInterpreter(protoFile);
        grpcService = protobufInterpreter.parseProtobufFile();

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
        helloRequest.put("inner", inner);
        dynMsg = message.build(helloRequest);
    }


    @Test
    public void TestGrpcCall() {
        Service service = grpcService.getService();
        Method method = service.getMethods().get(0);
        DynamicMessage response = new GrpcClient("localhost", 8000).
                AddService(service).AddMethod(method).
                Do(dynMsg);
        System.out.println(response);

    }

    @Test
    public void TestGetJsonStructure() {
        System.out.println(grpcService.getMessageList().get(0).getJsonStructure());
    }

}