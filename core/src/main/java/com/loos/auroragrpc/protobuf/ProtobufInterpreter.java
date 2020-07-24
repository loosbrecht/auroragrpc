package com.loos.auroragrpc.protobuf;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.loos.auroragrpc.GrpcService;
import com.loos.auroragrpc.entity.Enum;
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
        service.getMessageList().forEach(Message::addAdditionalAttributes);
        return service;
    }

    private GrpcService parseProtobufDescriptor(DescriptorProtos.FileDescriptorProto descriptorProto) {

        DescriptorProtos.ServiceDescriptorProto serviceDescriptor = descriptorProto.getService(0);
        List<Enum> enums = parseForEnums(descriptorProto);
        List<Message> messages = parseForMessages(descriptorProto, enums);
        List<Method> methods = parseForMethods(serviceDescriptor.getMethodList(), messages);

        Service service = new Service(serviceDescriptor.getName(), descriptorProto.getPackage(), methods);
        return new GrpcService(service, messages, enums);

    }

    private List<Enum> parseForEnums(DescriptorProtos.FileDescriptorProto descriptorProto) {
        List<Enum> enums = new ArrayList<>();
        for (DescriptorProtos.EnumDescriptorProto enumDescr : descriptorProto.getEnumTypeList()) {
            String enumName = enumDescr.getName();
            List<DescriptorProtos.EnumValueDescriptorProto> valueList = enumDescr.getValueList();
            List<String> values = valueList.stream().map(DescriptorProtos.EnumValueDescriptorProto::getName).collect(Collectors.toList());
            enums.add(new Enum(enumName, values));
        }
        return enums;
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

    private List<Message> parseForMessages(DescriptorProtos.FileDescriptorProto descriptorProto, List<Enum> enums) {
        List<DescriptorProtos.DescriptorProto> messageTypeList = descriptorProto.getMessageTypeList();
        List<Message> messages = messageTypeList.stream().map(m -> createMessage(m, enums)).collect(Collectors.toList());
        replaceMessagePlaceHolder(messageTypeList, messages);
        return messages;
    }

    private void replaceMessagePlaceHolder(List<DescriptorProtos.DescriptorProto> messageTypeList, List<Message> messages) {
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            List<Type> fields = message.getFields();
            for (int j = 0; j < fields.size(); j++) {
                Type field = fields.get(j);
                if (field instanceof MessagePlaceHolder) {
                    Optional<Message> first = findMessage(messages, field.getName());
                    if (first.isPresent()) {
                        String innerName = messageTypeList.get(i).getField(j).getName();
                        Message innerMessage = new Message(first.get());
                        innerMessage.setInnerName(innerName);
                        fields.set(j, innerMessage);
                    }
                }
            }
        }
    }

    private Message createMessage(DescriptorProtos.DescriptorProto messageDescriptor, List<Enum> enums) {
        List<DescriptorProtos.FieldDescriptorProto> fields = messageDescriptor.getFieldList();
        String name = messageDescriptor.getName();
        List<Type> types = new ArrayList<>();
        for (DescriptorProtos.FieldDescriptorProto field : fields) {
            switch (field.getType()) {
                case TYPE_MESSAGE:
                    types.add(new MessagePlaceHolder(field.getTypeName()));
                    continue;
                case TYPE_ENUM:
                    for (Enum en : enums) {
                        String typeName = field.getTypeName();
                        typeName = typeName.replace("." + packageName + ".", "");
                        if (en.getName().equals(typeName)) {
                            Enum newEnum = new Enum(en);
                            newEnum.setInnerName(field.getName());
                            types.add(newEnum);
                        }
                    }
                    continue;
                default:
                    types.add(new Field(field.getName(), field.getType(), field.getType().name()));
            }
        }
        return new Message(name, "", types);
    }

    private Optional<Message> findMessage(List<Message> messages, String name) {
        String finalName = name.replace("." + packageName + ".", "");
        return messages.stream().filter(m -> finalName.equals(m.getName())).findFirst();
    }


}
