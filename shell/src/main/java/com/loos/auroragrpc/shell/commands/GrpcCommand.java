package com.loos.auroragrpc.shell.commands;

import com.google.protobuf.Descriptors;
import com.loos.auroragrpc.core.GrpcService;
import com.loos.auroragrpc.core.entity.Service;
import com.loos.auroragrpc.core.protobuf.ProtobufInterpreter;
import com.loos.auroragrpc.shell.config.ShellHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@ShellComponent
public class GrpcCommand {

    @Autowired
    ShellHelper shellHelper;

    GrpcService grpcService;
    Service service;


    @ShellMethod("Provide path to a proto descriptor file or a folder")
    public void loadDescriptor(@ShellOption({"-P", "--path"}) String path) {
        File f = new File(path);
        if (f.isDirectory()) {
            //do something
        } else {
            try {
                grpcService = new ProtobufInterpreter(new FileInputStream(f)).parseProtobufFile();
                service = grpcService.getService();
                shellHelper.printSuccess("Descriptor loaded");
            } catch (IOException | Descriptors.DescriptorValidationException e) {
                shellHelper.printError(e.getMessage());
            }
        }
    }

    @ShellMethod("show the Grpc service with all the methods")
    public void service() {
        shellHelper.printSuccess(service.getName());
        service.getMethods().
                stream().
                map(method -> {
                    return String.format("\t%s(%s)%s",
                            method.getName(),
                            method.getRequest().getName(),
                            method.getResponse().getName());
                }).forEach(shellHelper::printInfo);
    }

    @ShellMethod("show all the messages that are provided by the descriptor")
    public void messages() {
        grpcService.getMessageList().stream().
                forEach(msg -> {
                    shellHelper.printSuccess(msg.getName());
                    String json = msg.getJsonStructure();
                    json = "\t" + json.replaceAll("\\n", "\n\t");
                    shellHelper.printInfo(json);
                });

    }


}
