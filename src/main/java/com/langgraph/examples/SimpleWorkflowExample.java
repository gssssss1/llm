package com.langgraph.examples;

import com.langgraph.core.StateGraph;
import com.langgraph.execution.ExecutionConfig;
import com.langgraph.execution.ExecutionResult;
import com.langgraph.execution.GraphExecutor;
import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;
import com.langgraph.viewer.GraphExecutionViewer;

import java.util.HashMap;
import java.util.Map;

public class SimpleWorkflowExample {
    
    public static void main(String[] args) throws Exception {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> {
                System.out.println("  [START] Initializing workflow...");
                return new NodeResult<>(
                    state.withData("step", "started").withData("count", 0),
                    "process"
                );
            })
            .addNode("process", state -> {
                System.out.println("  [PROCESS] Processing data...");
                int count = state.get("count", Integer.class);
                return new NodeResult<>(
                    state.withData("step", "processed").withData("count", count + 1),
                    "decision"
                );
            })
            .addNode("decision", state -> {
                System.out.println("  [DECISION] Making decision...");
                int count = state.get("count", Integer.class);
                String next = count < 3 ? "process" : "end";
                return new NodeResult<>(
                    state.withData("step", "decided"),
                    next
                );
            })
            .addNode("end", state -> {
                System.out.println("  [END] Workflow complete!");
                return new NodeResult<>(
                    state.withData("step", "completed")
                );
            })
            .setStartNode("start")
            .setEndNode("end")
            .build();
        
        GraphExecutor<StateRecord> executor = new GraphExecutor<>();
        GraphExecutionViewer<StateRecord> viewer = new GraphExecutionViewer<>(graph, executor);
        
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("workflow", "simple-example");
        StateRecord initialState = new StateRecord(initialData);
        
        ExecutionResult<StateRecord> result = viewer.executeAndDisplay(
            initialState,
            ExecutionConfig.DEFAULT
        ).get();
        
        System.out.println("Execution " + 
            (result.isSuccessful() ? "succeeded" : "failed") + "!");
        
        executor.shutdown();
    }
}
