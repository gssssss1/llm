package com.langgraph.examples;

import com.langgraph.core.StateGraph;
import com.langgraph.execution.ExecutionResult;
import com.langgraph.execution.GraphExecutor;
import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;
import com.langgraph.viewer.GraphExecutionViewer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ConditionalWorkflowExample {
    
    public static void main(String[] args) throws Exception {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("analyze", state -> {
                System.out.println("  [ANALYZE] Analyzing input...");
                Random random = new Random();
                String type = random.nextBoolean() ? "simple" : "complex";
                return new NodeResult<>(
                    state.withData("analysis", "complete").withData("type", type)
                );
            })
            .addNode("simple_processing", state -> {
                System.out.println("  [SIMPLE] Processing simple task...");
                return new NodeResult<>(
                    state.withData("processing", "simple_done")
                );
            })
            .addNode("complex_processing", state -> {
                System.out.println("  [COMPLEX] Processing complex task...");
                return new NodeResult<>(
                    state.withData("processing", "complex_done")
                );
            })
            .addNode("finalize", state -> {
                System.out.println("  [FINALIZE] Finalizing results...");
                return new NodeResult<>(
                    state.withData("status", "completed")
                );
            })
            .setStartNode("analyze")
            .setEndNode("finalize")
            .addConditionalEdge("analyze", state -> {
                String type = state.get("type", String.class);
                return "simple".equals(type) ? "simple_processing" : "complex_processing";
            })
            .addEdge("simple_processing", "finalize")
            .addEdge("complex_processing", "finalize")
            .build();
        
        GraphExecutor<StateRecord> executor = new GraphExecutor<>();
        GraphExecutionViewer<StateRecord> viewer = new GraphExecutionViewer<>(graph, executor);
        
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("input", "test-data");
        StateRecord initialState = new StateRecord(initialData);
        
        ExecutionResult<StateRecord> result = viewer.executeAndDisplay(initialState).get();
        
        System.out.println("Task type was: " + result.finalState().get("type"));
        System.out.println("Processing result: " + result.finalState().get("processing"));
        
        executor.shutdown();
    }
}
