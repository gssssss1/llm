package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.core.tool.Tool;
import com.llm.core.tool.ToolCall;
import com.llm.core.tool.ToolExecutor;
import com.llm.core.tool.ToolRegistry;
import com.llm.core.tool.ToolResult;
import com.llm.provider.ProviderType;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates advanced tool execution with timeout and batch support.
 */
public class ToolExecutionExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();
        ToolRegistry registry = new ToolRegistry();
        ToolExecutor executor = new ToolExecutor(registry, Duration.ofSeconds(10));

        registry.register(new CalculatorTool());

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-4o")
                .systemPrompt("You can call tools to perform calculations.")
                .build()) {

            // Simulate tool calls
            ToolCall addCall = new ToolCall("call-1", "calculator", Map.of(
                    "operation", "add",
                    "a", 40,
                    "b", 2
            ));

            ToolCall multiplyCall = new ToolCall("call-2", "calculator", Map.of(
                    "operation", "multiply",
                    "a", 6,
                    "b", 7
            ));

            Map<ToolCall, ToolResult> results = executor.executeBatch(List.of(addCall, multiplyCall));

            results.forEach((call, result) -> {
                System.out.println("Tool: " + call.getName());
                System.out.println("Result: " + result.getContent());
                System.out.println();
            });

            session.send(UserMessage.of("Use the calculator tool to compute 40 + 2."));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Simple calculator tool supporting basic operations.
     */
    static class CalculatorTool implements Tool {

        @Override
        public String getName() {
            return "calculator";
        }

        @Override
        public String getDescription() {
            return "Performs basic arithmetic operations";
        }

        @Override
        public Map<String, Object> getParameterSchema() {
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");
            schema.put("required", List.of("operation", "a", "b"));
            schema.put("properties", Map.of(
                    "operation", Map.of("type", "string"),
                    "a", Map.of("type", "number"),
                    "b", Map.of("type", "number")
            ));
            return schema;
        }

        @Override
        public ToolResult execute(Map<String, Object> arguments) {
            String operation = arguments.get("operation").toString();
            double a = Double.parseDouble(arguments.get("a").toString());
            double b = Double.parseDouble(arguments.get("b").toString());

            double result;
            switch (operation) {
                case "add":
                    result = a + b;
                    break;
                case "subtract":
                    result = a - b;
                    break;
                case "multiply":
                    result = a * b;
                    break;
                case "divide":
                    result = a / b;
                    break;
                default:
                    return ToolResult.builder()
                            .content("Unsupported operation: " + operation)
                            .success(false)
                            .build();
            }

            return ToolResult.builder()
                    .content("Result: " + result)
                    .success(true)
                    .build();
        }
    }
}
