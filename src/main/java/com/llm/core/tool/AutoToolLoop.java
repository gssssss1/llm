package com.llm.core.tool;

import com.llm.core.message.AssistantMessage;
import com.llm.core.message.MultiModalContent;
import com.llm.core.message.ToolMessage;
import com.llm.provider.common.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Automatic loop for processing tool calls triggered by the model.
 */
public class AutoToolLoop {

    public enum Strategy {
        AUTO,
        CONFIRM,
        MANUAL
    }

    private final Strategy strategy;
    private final ToolExecutor toolExecutor;

    public AutoToolLoop(Strategy strategy, ToolExecutor toolExecutor) {
        this.strategy = strategy;
        this.toolExecutor = toolExecutor;
    }

    /**
     * Processes tool calls contained in the response using the configured strategy.
     *
     * @param response        the response that may contain tool calls
     * @param toolResponder   function used to send tool responses back to the model
     * @return final assistant message after tool processing
     */
    public AssistantMessage process(Response response,
                                    BiFunction<List<ToolMessage>, List<MultiModalContent>, Response> toolResponder) {
        if (response.getToolCalls().isEmpty()) {
            return response.getMessage();
        }

        switch (strategy) {
            case MANUAL:
                return response.getMessage();
            case CONFIRM:
                // Confirmation strategy would require user confirmation, not available in headless mode
                return response.getMessage();
            case AUTO:
            default:
                List<ToolMessage> toolMessages = new ArrayList<>();
                for (ToolCall call : response.getToolCalls()) {
                    ToolResult result = toolExecutor.execute(call);
                    toolMessages.add(ToolMessage.builder()
                            .toolCallId(call.getId())
                            .toolName(call.getName())
                            .text(result.getContent())
                            .build());
                }
                Response followUp = toolResponder.apply(toolMessages, response.getMessage().getContents());
                return followUp.getMessage();
        }
    }
}
