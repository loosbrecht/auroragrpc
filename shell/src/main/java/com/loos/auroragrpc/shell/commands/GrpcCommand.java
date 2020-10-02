package com.loos.auroragrpc.shell.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.loos.auroragrpc.core.GrpcService;
import com.loos.auroragrpc.core.entity.InvalidValueException;
import com.loos.auroragrpc.core.entity.Method;
import com.loos.auroragrpc.core.entity.Service;
import com.loos.auroragrpc.core.grpc.GrpcClient;
import com.loos.auroragrpc.core.protobuf.ProtobufInterpreter;
import com.loos.auroragrpc.shell.config.ProgressCounter;
import com.loos.auroragrpc.shell.config.ShellHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@ShellComponent
public class GrpcCommand {

    @Autowired
    ShellHelper shellHelper;

    GrpcService grpcService;
    Service service;
    @Autowired
    private ProgressCounter progressCounter;


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

    @ShellMethod("execute a method and provide the request with a seperate json file")
    public void doRequest(@ShellOption({"-h", "--host"}) String host,
                          @ShellOption({"-m", "--method"}) String method,
                          @ShellOption({"-i", "--input"}) String input) {
        Method m = this.service.findMethod(method);
        if (m == null) {
            shellHelper.printError("Method not found");
            return;
        }
        DynamicMessage msg = null;
        try {
            FileInputStream inputStream = new FileInputStream(new File(input));
            byte[] bytes = inputStream.readAllBytes();
            Map<String, Object> inputMap = GetMessageInput(new String(bytes));
            msg = m.getRequest().build(inputMap);

        } catch (JsonProcessingException e) {
            shellHelper.printError("Can't parse input\n error: " + e.getMessage());
            return;
        } catch (InvalidValueException e) {
            shellHelper.printError("Input can't be mapped on " + m.getName() + "\nerror: " + e.getMessage());
            return;
        } catch (FileNotFoundException e) {
            shellHelper.printError("Input file not found");
            return;
        } catch (IOException e) {
            shellHelper.printError("Input can't be read");
            return;
        }
        DynamicMessage finalMsg = msg;
        final DynamicMessage[] resp = {null};
        final boolean[] done = {false};

        //TODO refactor threading
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                resp[0] = new GrpcClient(host).
                        AddService(service).
                        AddMethod(m).
                        Do(finalMsg);
                done[0] = true;
            }
        });
        thread.start();

        try {
            while (!done[0]){
                progressCounter.display();
                Thread.sleep(100);
            }
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressCounter.reset();
        shellHelper.printInfo("Response: ");
        shellHelper.printSuccess(resp[0].toString());
    }

    private Map<String, Object> GetMessageInput(String input) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(input, Map.class);
        return map;
    }


}
