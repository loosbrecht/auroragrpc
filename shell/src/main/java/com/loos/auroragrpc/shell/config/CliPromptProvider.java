package com.loos.auroragrpc.shell.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;


@Component
public class CliPromptProvider implements PromptProvider {
    @Override
    public AttributedString getPrompt() {
        return new AttributedString("aurora-grpc:>",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE)
        );    }
}
