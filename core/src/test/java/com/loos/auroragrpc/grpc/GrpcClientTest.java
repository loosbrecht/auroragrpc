package com.loos.auroragrpc.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.loos.auroragrpc.GrpcService;
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

class GrpcClientTest {


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
    public void TestGrpcCall() {
        Service service = grpcService.getService();
        Method method = service.getMethods().get(0);
        DynamicMessage msg = method.getRequest().Build();
        DynamicMessage response = new GrpcClient("localhost", 8000).
                AddService(service).AddMethod(method).
                Do(msg);
        System.out.println(response);

    }

    @Test
    public void TestGetJsonStructure() {
        System.out.println(grpcService.getMessageList().get(0).getJsonStructure());
    }

}