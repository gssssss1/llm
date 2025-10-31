package com.langgraph.core;

import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateGraphTest {
    
    @Test
    void testBuildSimpleGraph() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> new NodeResult<>(state))
            .addNode("end", state -> new NodeResult<>(state))
            .addEdge("start", "end")
            .setStartNode("start")
            .build();
        
        assertNotNull(graph);
        assertEquals("start", graph.getStartNode());
        assertEquals(2, graph.getNodes().size());
        assertEquals(1, graph.getEdges().size());
    }
    
    @Test
    void testGetNode() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("test", state -> new NodeResult<>(state))
            .setStartNode("test")
            .build();
        
        assertNotNull(graph.getNode("test"));
        assertEquals("test", graph.getNode("test").name());
    }
    
    @Test
    void testGetNodeNotFound() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("test", state -> new NodeResult<>(state))
            .setStartNode("test")
            .build();
        
        assertThrows(IllegalArgumentException.class, () -> graph.getNode("nonexistent"));
    }
    
    @Test
    void testGetEdgesFrom() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("node1", state -> new NodeResult<>(state))
            .addNode("node2", state -> new NodeResult<>(state))
            .addNode("node3", state -> new NodeResult<>(state))
            .addEdge("node1", "node2")
            .addEdge("node1", "node3")
            .setStartNode("node1")
            .build();
        
        assertEquals(2, graph.getEdgesFrom("node1").size());
        assertEquals(0, graph.getEdgesFrom("node2").size());
    }
    
    @Test
    void testGetNextNodes() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("node1", state -> new NodeResult<>(state))
            .addNode("node2", state -> new NodeResult<>(state))
            .addEdge("node1", "node2")
            .setStartNode("node1")
            .build();
        
        StateRecord state = new StateRecord();
        assertEquals(1, graph.getNextNodes("node1", state).size());
        assertEquals("node2", graph.getNextNodes("node1", state).get(0));
    }
    
    @Test
    void testConditionalEdge() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> new NodeResult<>(state))
            .addNode("nodeA", state -> new NodeResult<>(state))
            .addNode("nodeB", state -> new NodeResult<>(state))
            .addConditionalEdge("start", state -> {
                Object value = state.get("condition");
                return "A".equals(value) ? "nodeA" : "nodeB";
            })
            .setStartNode("start")
            .build();
        
        StateRecord stateA = new StateRecord().withData("condition", "A");
        assertEquals("nodeA", graph.getNextNodes("start", stateA).get(0));
        
        StateRecord stateB = new StateRecord().withData("condition", "B");
        assertEquals("nodeB", graph.getNextNodes("start", stateB).get(0));
    }
    
    @Test
    void testMissingStartNode() {
        assertThrows(IllegalStateException.class, () -> 
            StateGraph.<StateRecord>builder()
                .addNode("node", state -> new NodeResult<>(state))
                .build()
        );
    }
    
    @Test
    void testInvalidStartNode() {
        assertThrows(IllegalStateException.class, () ->
            StateGraph.<StateRecord>builder()
                .addNode("node", state -> new NodeResult<>(state))
                .setStartNode("nonexistent")
                .build()
        );
    }
    
    @Test
    void testInvalidEdge() {
        assertThrows(IllegalStateException.class, () ->
            StateGraph.<StateRecord>builder()
                .addNode("node1", state -> new NodeResult<>(state))
                .addEdge("node1", "nonexistent")
                .setStartNode("node1")
                .build()
        );
    }
}
