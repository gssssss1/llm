package com.langgraph.viewer;

import com.langgraph.core.StateGraph;
import com.langgraph.execution.ExecutionConfig;
import com.langgraph.execution.ExecutionResult;
import com.langgraph.execution.GraphExecutor;
import com.langgraph.state.State;

import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

public class GraphExecutionViewer<T extends State> {
    private final StateGraph<T> graph;
    private final GraphExecutor<T> executor;
    private final PrintStream output;
    
    public GraphExecutionViewer(StateGraph<T> graph, GraphExecutor<T> executor) {
        this(graph, executor, System.out);
    }
    
    public GraphExecutionViewer(StateGraph<T> graph, GraphExecutor<T> executor, PrintStream output) {
        this.graph = graph;
        this.executor = executor;
        this.output = output;
    }
    
    public void displayGraph() {
        output.println("\n" + "=".repeat(80));
        output.println("GRAPH VISUALIZATION");
        output.println("=".repeat(80) + "\n");
        
        output.println(GraphViewer.toAscii(graph));
        
        output.println("\n" + GraphViewer.getStats(graph));
        
        output.println("\n" + "=".repeat(80));
        output.println("MERMAID DIAGRAM");
        output.println("=".repeat(80) + "\n");
        output.println(GraphViewer.toMermaid(graph));
        
        output.println("\n" + "=".repeat(80) + "\n");
    }
    
    public CompletableFuture<ExecutionResult<T>> executeAndDisplay(T initialState, ExecutionConfig config) {
        displayGraph();
        
        output.println("EXECUTING GRAPH...");
        output.println("=".repeat(80) + "\n");
        output.println("Initial State: " + initialState);
        output.println();
        
        CompletableFuture<ExecutionResult<T>> future = executor.execute(graph, initialState, config);
        
        return future.thenApply(result -> {
            output.println("\n" + "=".repeat(80));
            output.println("EXECUTION RESULT");
            output.println("=".repeat(80) + "\n");
            
            output.println("Status: " + result.status());
            output.println("Execution ID: " + result.executionId());
            output.println("Duration: " + result.duration().toMillis() + "ms");
            output.println("Nodes Executed: " + result.executedNodes());
            output.println("\nFinal State: " + result.finalState());
            
            if (result.error().isPresent()) {
                output.println("\nError:");
                result.error().get().printStackTrace(output);
            }
            
            output.println("\n" + "=".repeat(80) + "\n");
            
            return result;
        });
    }
    
    public CompletableFuture<ExecutionResult<T>> executeAndDisplay(T initialState) {
        return executeAndDisplay(initialState, ExecutionConfig.DEFAULT);
    }
}
