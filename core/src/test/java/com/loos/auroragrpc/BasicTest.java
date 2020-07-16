package com.loos.auroragrpc;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class BasicTest {

    @Test
    void BasicUsage() throws IOException, Descriptors.DescriptorValidationException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("descriptor");
        assert url != null;
        File file = new File(url.getPath());
       InputStream protoFile = new FileInputStream(file);
        DynamicSchema dynamicSchema = DynamicSchema.parseFrom(protoFile);

        DynamicMessage.Builder innerRequest = dynamicSchema.newMessageBuilder("InnerRequest");
        Descriptors.Descriptor inDescr = innerRequest.getDescriptorForType();
        innerRequest.setField(inDescr.findFieldByName("inner"),"insideee");
        DynamicMessage build = innerRequest.build();


        DynamicMessage.Builder helloRequest = dynamicSchema.newMessageBuilder("HelloRequest");
        Descriptors.Descriptor descr = helloRequest.getDescriptorForType();
        helloRequest.setField(descr.findFieldByName("name"),"howdy");
        helloRequest.setField(descr.findFieldByName("inner"),build);
        DynamicMessage helloRequestBuilder = helloRequest.build();
        System.out.println(helloRequestBuilder);


    }
}
