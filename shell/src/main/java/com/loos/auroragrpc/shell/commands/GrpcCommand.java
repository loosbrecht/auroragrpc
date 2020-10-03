package com.loos.auroragrpc.shell.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
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
import com.loos.auroragrpc.shell.entity.Executor;
import com.loos.auroragrpc.shell.entity.JsonInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FileInputStream;
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
    public void loadDescriptor(@ShellOption({"-d", "--descriptor"}) String descriptor) {
        File f = new File(descriptor);
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
        DynamicMessage msg = prepareMessage(m, input);
        if (msg == null) {
            return;
        }

        final DynamicMessage[] resp = executeMethod(host, m, msg);
        shellHelper.printInfo("Response: ");
        shellHelper.printSuccess(resp[0].toString());
    }

    private DynamicMessage[] executeMethod(String host, Method m, DynamicMessage msg) {
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
            while (!done[0]) {
                progressCounter.display();
                Thread.sleep(100);
            }
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        progressCounter.reset();
        return resp;
    }

    @ShellMethod("execute all the methods in the input json. This will not load the descriptor for reuse")
    public void executeJson(@ShellOption({"-d", "--descriptor"}) String descriptor, @ShellOption({"-i", "--input"}) String input) throws IOException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        loadDescriptor(descriptor);

        FileInputStream inputStream = new FileInputStream(new File(input));
        byte[] bytes = inputStream.readAllBytes();
        String s = new String(bytes);
        JsonInput jsonInput = new Gson().fromJson(s, JsonInput.class);

        shellHelper.printInfo("Start executing for service: " + jsonInput.getService());
        for (Executor executor : jsonInput.getExecute()) {
            shellHelper.printInfo("Execute method: " + executor.getName());
            Method method = this.service.findMethod(executor.getMethod());
            try {
                DynamicMessage msg = method.getRequest().build(executor.getInput());
                DynamicMessage[] resp = executeMethod(jsonInput.getHost(), method, msg);
                shellHelper.printInfo("Response: ");
                shellHelper.printSuccess(resp[0].toString());

            } catch (InvalidValueException e) {
                e.printStackTrace();
            }


        }

        reset();
    }

    @ShellMethod("reset the environment")
    public void reset() {
        this.service = null;
        this.grpcService = null;
    }

    private DynamicMessage prepareMessage(Method m, String inputPath) {
        try {
            Map<String, Object> inputMap = getMessageInputFromFile(inputPath);
            if (inputMap == null) {
                return null;
            }
            return m.getRequest().build(inputMap);

        } catch (InvalidValueException e) {
            shellHelper.printError("Input can't be mapped on " + m.getName() + "\nerror: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> getMessageInputFromFile(String inputPath) {
        try {
            FileInputStream inputStream = new FileInputStream(new File(inputPath));
            byte[] bytes = inputStream.readAllBytes();
            String s = new String(bytes);
            return getMessageInput(s);

        } catch (IOException e) {
            shellHelper.printError("Error processing input");
            return null;
        }
    }

    private Map<String, Object> getMessageInput(String input) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(input, Map.class);
        return map;
    }


}
