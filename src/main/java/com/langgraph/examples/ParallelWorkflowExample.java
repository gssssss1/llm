package com.langgraph.examples;

import com.langgraph.core.StateGraph;
import com.langgraph.execution.ExecutionResult;
import com.langgraph.execution.GraphExecutor;
import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;
import com.langgraph.viewer.GraphExecutionViewer;

import java.util.HashMap;
import java.util.Map;

public class ParallelWorkflowExample {
    
    public static void main(String[] args) throws Exception {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("prepare", state -> {
                System.out.println("  [PREPARE] Preparing data for parallel processing...");
                return new NodeResult<>(
                    state.withData("prepared", true).withData("timestamp", System.currentTimeMillis())
                );
            })
            .addNode("task_a", state -> {
                System.out.println("  [TASK_A] Processing task A...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new NodeResult<>(
                    state.withData("task_a_result", "completed"),
                    "merge"
                );
            })
            .addNode("task_b", state -> {
                System.out.println("  [TASK_B] Processing task B...");
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new NodeResult<>(
                    state.withData("task_b_result", "completed"),
                    "merge"
                );
            })
            .addNode("task_c", state -> {
                System.out.println("  [TASK_C] Processing task C...");
                try {
                    Thread.sleep(120);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new NodeResult<>(
                    state.withData("task_c_result", "completed"),
                    "merge"
                );
            })
            .addNode("merge", state -> {
                System.out.println("  [MERGE] Merging results from parallel tasks...");
                return new NodeResult<>(
                    state.withData("merged", true).withData("final", "all_tasks_complete")
                );
            })
            .setStartNode("prepare")
            .setEndNode("merge")
            .addParallelEdge("prepare", "task_a", "task_b", "task_c")
            .build();
        
        GraphExecutor<StateRecord> executor = new GraphExecutor<>();
        GraphExecutionViewer<StateRecord> viewer = new GraphExecutionViewer<>(graph, executor);
        
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("workflow", "parallel-example");
        StateRecord initialState = new StateRecord(initialData);
        
        System.out.println("\nDemonstrating parallel execution with 3 tasks...\n");
        
        long startTime = System.currentTimeMillis();
        ExecutionResult<StateRecord> result = viewer.executeAndDisplay(initialState).get();
        long endTime = System.currentTimeMillis();
        
        System.out.println("Parallel execution time: " + (endTime - startTime) + "ms");
        System.out.println("(Note: If tasks ran sequentially, it would take ~370ms)");
        System.out.println("\nFinal state data:");
        result.finalState().data().forEach((key, value) -> 
            System.out.println("  " + key + " = " + value));
        
        executor.shutdown();
    }
}
