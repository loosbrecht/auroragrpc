package com.loos.auroragrpc.protobuf;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.loos.auroragrpc.GrpcService;
import com.loos.auroragrpc.entity.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProtobufInterpreter {

    private final InputStream protobufInputStream;
    private String packageName;

    public ProtobufInterpreter(InputStream protobufInputStream) {
        this.protobufInputStream = protobufInputStream;
    }

    private static Message createMessage(DescriptorProtos.DescriptorProto messageDescriptor) {
        List<DescriptorProtos.FieldDescriptorProto> fields = messageDescriptor.getFieldList();
        String name = messageDescriptor.getName();
        List<Type> types = new ArrayList<>();
        for (DescriptorProtos.FieldDescriptorProto field : fields) {
            switch (field.getType()) {
                case TYPE_MESSAGE:
                    types.add(new MessagePlaceHolder(field.getTypeName()));
                case TYPE_ENUM:
                    //TODO implement me
                default:
                    types.add(new Field(field.getName(), field.getType().name()));
            }
        }
        return new Message(name, types);
    }

    public GrpcService parseProtobufFile() throws IOException, Descriptors.DescriptorValidationException {
        DynamicSchema dynamicSchema = DynamicSchema.parseFrom(protobufInputStream);
        // TODO make a better implementation for this
        DescriptorProtos.FileDescriptorSet fileDescriptorSet = dynamicSchema.getFileDescriptorSet();
        DescriptorProtos.FileDescriptorProto file = fileDescriptorSet.getFile(0);
        Descriptors.FileDescriptor[] files = new Descriptors.FileDescriptor[0];
        Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(file, files);
        DescriptorProtos.FileDescriptorProto descriptorProto = fileDescriptor.toProto();

        this.packageName = descriptorProto.getPackage();
        GrpcService service = parseProtobufDescriptor(descriptorProto);
        service.setSchema(dynamicSchema);
        service.getMessageList().forEach(m -> m.createBuilder(dynamicSchema));
        service.getMessageList().forEach(m -> m.setDescriptor(descriptorProto));
        return service;
    }

    private GrpcService parseProtobufDescriptor(DescriptorProtos.FileDescriptorProto descriptorProto) {

        DescriptorProtos.ServiceDescriptorProto serviceDescriptor = descriptorProto.getService(0);
        List<Message> messages = parseForMessages(descriptorProto);
        List<Method> methods = parseForMethods(serviceDescriptor.getMethodList(), messages);

        Service service = new Service(serviceDescriptor.getName(), descriptorProto.getPackage(), methods);
        return new GrpcService(service, messages);

    }

    //TODO improve this
    private List<Method> parseForMethods(List<DescriptorProtos.MethodDescriptorProto> methodList, List<Message> messages) {
        return methodList.stream().map(methodDescriptorProto -> {
            Optional<Message> optReq = findMessage(messages, methodDescriptorProto.getInputType());
            Optional<Message> optResp = findMessage(messages, methodDescriptorProto.getOutputType());
            Message req = null, resp = null;
            if (optReq.isPresent()) {
                req = optReq.get();
            }
            if (optResp.isPresent()) {
                resp = optResp.get();
            }

            return new Method(methodDescriptorProto.getName(), req, resp);

        }).collect(Collectors.toList());

    }

    private List<Message> parseForMessages(DescriptorProtos.FileDescriptorProto descriptorProto) {
        List<DescriptorProtos.DescriptorProto> messageTypeList = descriptorProto.getMessageTypeList();
        List<Message> messages = messageTypeList.stream().map(ProtobufInterpreter::createMessage).collect(Collectors.toList());
        for (Message message : messages) {
            List<Type> fields = message.getFields();
            for (int i = 0; i < fields.size(); i++) {
                Type field = fields.get(i);
                if (field instanceof MessagePlaceHolder) {
                    Optional<Message> first = findMessage(messages, field.getName());
                    if (first.isPresent()) {
                        fields.set(i, first.get());
                    }
                }
            }
        }
        return messages;
    }

    private Optional<Message> findMessage(List<Message> messages, String name) {
        String finalName = name.replace("." + packageName + ".", "");
        return messages.stream().filter(m -> finalName.equals(m.getName())).findFirst();
    }


}
