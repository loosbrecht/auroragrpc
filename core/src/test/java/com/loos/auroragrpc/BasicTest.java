package com.loos.auroragrpc;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class BasicTest {

    @Test
    void BasicUsage() throws IOException, Descriptors.DescriptorValidationException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("descriptor");
        assert url != null;
        File f = new File(url.getPath());
        InputStream protoFile = new FileInputStream(f);
        DynamicSchema dynamicSchema = DynamicSchema.parseFrom(protoFile);
        DescriptorProtos.FileDescriptorSet fileDescriptorSet = dynamicSchema.getFileDescriptorSet();
        DescriptorProtos.FileDescriptorProto file = fileDescriptorSet.getFile(0);
        Descriptors.FileDescriptor[] files = new Descriptors.FileDescriptor[0];
        Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(file, files);
        DescriptorProtos.FileDescriptorProto descriptorProto = fileDescriptor.toProto();

        List<DescriptorProtos.DescriptorProto> messageTypeList = descriptorProto.getMessageTypeList();
        List<DescriptorProtos.MethodDescriptorProto> methodList = descriptorProto.getService(0).getMethodList();
        DescriptorProtos.MethodDescriptorProto methodDescriptor = methodList.get(0);


        DynamicMessage.Builder innerRequest = dynamicSchema.newMessageBuilder("InnerRequest");
        Descriptors.Descriptor inDescr = innerRequest.getDescriptorForType();
        innerRequest.setField(inDescr.findFieldByName("inner"), "insideee");
        DynamicMessage build = innerRequest.build();


        DynamicMessage.Builder helloRequest = dynamicSchema.newMessageBuilder("HelloRequest");
        Descriptors.Descriptor descr = helloRequest.getDescriptorForType();
        helloRequest.setField(descr.findFieldByName("name"), "howdy");
        helloRequest.setField(descr.findFieldByName("inner"), build);
        DynamicMessage message = helloRequest.build();
        System.out.println(message);

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress("localhost", 8000);
        channelBuilder.usePlaintext();
        ManagedChannel channel = channelBuilder.build();

        Descriptors.Descriptor inputType = messageTypeList.get(0).getDescriptorForType();
        Descriptors.Descriptor outputType = messageTypeList.get(1).getDescriptorForType();

        MethodDescriptor.Marshaller<DynamicMessage> inputMarshaller = io.grpc.protobuf.ProtoUtils.marshaller(DynamicMessage.newBuilder(inputType).buildPartial());
        MethodDescriptor.Marshaller<DynamicMessage> outputMarshaller = io.grpc.protobuf.ProtoUtils.marshaller(DynamicMessage.newBuilder(outputType).buildPartial());
        String fullMethodName = MethodDescriptor.generateFullMethodName(descriptorProto.getPackage() + "." + descriptorProto.getService(0).getName(), methodDescriptor.getName());
        MethodDescriptor<DynamicMessage, DynamicMessage> methodBuild = MethodDescriptor.newBuilder(inputMarshaller, outputMarshaller).
                setType(MethodDescriptor.MethodType.UNKNOWN)
                .setFullMethodName(fullMethodName)
                .build();
        DynamicMessage result = ClientCalls.blockingUnaryCall(channel, methodBuild, CallOptions.DEFAULT, message);
        System.out.println(result);


    }
}
