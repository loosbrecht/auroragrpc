package com.loos.auroragrpc.protobuf;

import com.google.protobuf.Descriptors;
import com.loos.auroragrpc.GrpcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;

class ProtobufInterpreterTest {

    InputStream protoFile;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("descriptor");
        assert url != null;
        File f = new File(url.getPath());
        protoFile = new FileInputStream(f);
    }

    @Test
    void parseProtobufFile() throws IOException, Descriptors.DescriptorValidationException {
        ProtobufInterpreter protobufInterpreter = new ProtobufInterpreter(protoFile);
        GrpcService grpcService = protobufInterpreter.parseProtobufFile();
        System.out.println(grpcService);
    }
}