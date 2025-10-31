package com.langgraph.execution;

import com.langgraph.core.StateGraph;
import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class GraphExecutorTest {
    
    private GraphExecutor<StateRecord> executor = new GraphExecutor<>();
    
    @AfterEach
    void tearDown() {
        executor.shutdown();
    }
    
    @Test
    void testSimpleExecution() throws ExecutionException, InterruptedException {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> new NodeResult<>(state.withData("executed", true)))
            .setStartNode("start")
            .build();
        
        StateRecord initialState = new StateRecord();
        ExecutionResult<StateRecord> result = executor.execute(
            graph,
            initialState,
            ExecutionConfig.DEFAULT
        ).get();
        
        assertTrue(result.isSuccessful());
        assertEquals(Boolean.TRUE, result.finalState().get("executed"));
        assertEquals(1, result.executedNodes().size());
        assertEquals("start", result.executedNodes().get(0));
    }
    
    @Test
    void testMultipleNodes() throws ExecutionException, InterruptedException {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("node1", state -> new NodeResult<>(state.withData("step1", true), "node2"))
            .addNode("node2", state -> new NodeResult<>(state.withData("step2", true), "node3"))
            .addNode("node3", state -> new NodeResult<>(state.withData("step3", true)))
            .setStartNode("node1")
            .build();
        
        ExecutionResult<StateRecord> result = executor.execute(
            graph,
            new StateRecord(),
            ExecutionConfig.DEFAULT
        ).get();
        
        assertTrue(result.isSuccessful());
        assertEquals(3, result.executedNodes().size());
        assertEquals(Boolean.TRUE, result.finalState().get("step1"));
        assertEquals(Boolean.TRUE, result.finalState().get("step2"));
        assertEquals(Boolean.TRUE, result.finalState().get("step3"));
    }
    
    @Test
    void testConditionalExecution() throws ExecutionException, InterruptedException {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> new NodeResult<>(state.withData("value", 10)))
            .addNode("high", state -> new NodeResult<>(state.withData("result", "high")))
            .addNode("low", state -> new NodeResult<>(state.withData("result", "low")))
            .setStartNode("start")
            .addConditionalEdge("start", state -> {
                Integer value = state.get("value", Integer.class);
                return value > 5 ? "high" : "low";
            })
            .build();
        
        ExecutionResult<StateRecord> result = executor.execute(
            graph,
            new StateRecord(),
            ExecutionConfig.DEFAULT
        ).get();
        
        assertTrue(result.isSuccessful());
        assertEquals("high", result.finalState().get("result"));
        assertTrue(result.executedNodes().contains("start"));
        assertTrue(result.executedNodes().contains("high"));
    }
    
    @Test
    void testExecutionWithEndNode() throws ExecutionException, InterruptedException {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> new NodeResult<>(state.withData("started", true), "end"))
            .addNode("end", state -> new NodeResult<>(state.withData("ended", true)))
            .setStartNode("start")
            .setEndNode("end")
            .build();
        
        ExecutionResult<StateRecord> result = executor.execute(
            graph,
            new StateRecord(),
            ExecutionConfig.DEFAULT
        ).get();
        
        assertTrue(result.isSuccessful());
        assertEquals(Boolean.TRUE, result.finalState().get("started"));
        assertEquals(Boolean.TRUE, result.finalState().get("ended"));
    }
    
    @Test
    void testExecutionWithError() throws ExecutionException, InterruptedException {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> {
                throw new RuntimeException("Test error");
            })
            .setStartNode("start")
            .build();
        
        ExecutionResult<StateRecord> result = executor.execute(
            graph,
            new StateRecord(),
            ExecutionConfig.DEFAULT
        ).get();
        
        assertTrue(result.hasFailed());
        assertTrue(result.error().isPresent());
        assertEquals("Test error", result.error().get().getCause().getMessage());
    }
    
    @Test
    void testMaxSteps() throws ExecutionException, InterruptedException {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("loop", state -> {
                int count = state.get("count", Integer.class) != null 
                    ? state.get("count", Integer.class) : 0;
                return new NodeResult<>(state.withData("count", count + 1), "loop");
            })
            .setStartNode("loop")
            .build();
        
        ExecutionConfig config = new ExecutionConfig(true, true, 
            ExecutionConfig.DEFAULT.maxExecutionTime(), 10);
        
        ExecutionResult<StateRecord> result = executor.execute(
            graph,
            new StateRecord(),
            config
        ).get();
        
        assertTrue(result.hasFailed());
    }
}
