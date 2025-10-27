package com.llm.core.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Executes tools called by the LLM with timeout and parallel execution support.
 */
public class ToolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ToolExecutor.class);

    private final ToolRegistry registry;
    private final ToolValidator validator;
    private final ExecutorService executorService;
    private final Duration defaultTimeout;

    public ToolExecutor(ToolRegistry registry) {
        this(registry, Duration.ofSeconds(30));
    }

    public ToolExecutor(ToolRegistry registry, Duration defaultTimeout) {
        this.registry = registry;
        this.validator = new ToolValidator();
        this.executorService = Executors.newCachedThreadPool();
        this.defaultTimeout = defaultTimeout;
    }

    public ToolResult execute(ToolCall toolCall) {
        return execute(toolCall, defaultTimeout);
    }

    public ToolResult execute(ToolCall toolCall, Duration timeout) {
        Tool tool = registry.get(toolCall.getName());
        if (tool == null) {
            logger.warn("Tool not found: {}", toolCall.getName());
            return ToolResult.builder()
                    .content("Tool not found: " + toolCall.getName())
                    .success(false)
                    .build();
        }

        try {
            validator.validateArguments(tool, toolCall.getArguments());

            Future<ToolResult> future = executorService.submit(() -> tool.execute(toolCall.getArguments()));
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.error("Tool execution timed out: " + toolCall.getName());
            return ToolResult.builder()
                    .content("Tool execution timed out")
                    .success(false)
                    .build();
        } catch (Exception e) {
            logger.error("Error executing tool: " + toolCall.getName(), e);
            return ToolResult.builder()
                    .content("Error: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    public Map<ToolCall, ToolResult> executeBatch(List<ToolCall> toolCalls) {
        Map<ToolCall, Future<ToolResult>> futures = new ConcurrentHashMap<>();

        for (ToolCall call : toolCalls) {
            futures.put(call, executorService.submit(() -> execute(call)));
        }

        return futures.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                return entry.getValue().get();
                            } catch (Exception e) {
                                logger.error("Error in batch execution", e);
                                return ToolResult.builder()
                                        .content("Batch execution error: " + e.getMessage())
                                        .success(false)
                                        .build();
                            }
                        }
                ));
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}
