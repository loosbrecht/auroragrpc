package com.loos.auroragrpc.core.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.loos.auroragrpc.core.entity.Message;
import com.loos.auroragrpc.core.entity.Method;
import com.loos.auroragrpc.core.entity.Service;
import io.grpc.CallOptions;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;


public class GrpcClient {

    private final io.grpc.Channel channel;
    private Service service;
    private MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor;

    public GrpcClient(String host, int port) {
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(host, port);
        builder.usePlaintext();
        this.channel = builder.build();
    }

    public GrpcClient AddService(Service service) {
        this.service = service;
        return this;
    }

    public GrpcClient AddMethod(Method method) {
        Message request = method.getRequest();
        Message response = method.getResponse();

        Descriptors.Descriptor inputType = request.getDescriptor();
        Descriptors.Descriptor outputType = response.getDescriptor();

        MethodDescriptor.Marshaller<DynamicMessage> inputMarshaller = io.grpc.protobuf.ProtoUtils.marshaller(DynamicMessage.newBuilder(inputType).buildPartial());
        MethodDescriptor.Marshaller<DynamicMessage> outputMarshaller = io.grpc.protobuf.ProtoUtils.marshaller(DynamicMessage.newBuilder(outputType).buildPartial());
        String fullMethodName = MethodDescriptor.generateFullMethodName(service.GetFullServiceName(), method.getName());
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
