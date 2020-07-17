package com.loos.auroragrpc.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.loos.auroragrpc.GrpcService;
import com.loos.auroragrpc.entity.Method;
import com.loos.auroragrpc.entity.Service;
import io.grpc.CallOptions;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;


public class GrpcClient {

    private io.grpc.Channel channel;
    private String packageName;
    private Service service;
    private MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor;

    public GrpcClient(String host, String packageName) {

        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress("localhost", 8000);
        builder.usePlaintext();
        this.channel = builder.build();
        this.packageName = packageName;

    }

    public GrpcClient AddService(Service service) {
        this.service = service;
        return this;
    }

    public GrpcService AddMethod(Method method) {
        Message input = (Message) method.getInput();
        Message output = (Message) method.getOutput();

        Descriptors.Descriptor inputType = input.getDescriptor().getDescriptorForType();
        Descriptors.Descriptor outputType = output.getDescriptor().getDescriptorForType();

        MethodDescriptor.Marshaller<DynamicMessage> inputMarshaller = io.grpc.protobuf.ProtoUtils.marshaller(DynamicMessage.newBuilder(inputType).buildPartial());
        MethodDescriptor.Marshaller<DynamicMessage> outputMarshaller = io.grpc.protobuf.ProtoUtils.marshaller(DynamicMessage.newBuilder(outputType).buildPartial());
        String fullMethodName = MethodDescriptor.generateFullMethodName(service.GetFullServiceName(), method.getMethodDescriptorProto().getName());
        this.methodDescriptor = MethodDescriptor.newBuilder(inputMarshaller, outputMarshaller).
                setType(MethodDescriptor.MethodType.UNKNOWN)
                .setFullMethodName(fullMethodName)
                .build();
        return this;
    }

    public DynamicMessage Do(DynamicMessage request) {
        return ClientCalls.blockingUnaryCall(channel, methodDescriptor, CallOptions.DEFAULT, request);
    }
}
