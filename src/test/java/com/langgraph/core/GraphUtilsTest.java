package com.langgraph.core;

import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphUtilsTest {
    
    @Test
    void testIsAcyclic_LinearGraph() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("a", state -> new NodeResult<>(state))
            .addNode("b", state -> new NodeResult<>(state))
            .addNode("c", state -> new NodeResult<>(state))
            .addEdge("a", "b")
            .addEdge("b", "c")
            .setStartNode("a")
            .build();
        
        assertTrue(GraphUtils.isAcyclic(graph));
    }
    
    @Test
    void testIsAcyclic_CyclicGraph() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("a", state -> new NodeResult<>(state))
            .addNode("b", state -> new NodeResult<>(state))
            .addNode("c", state -> new NodeResult<>(state))
            .addEdge("a", "b")
            .addEdge("b", "c")
            .addEdge("c", "a")
            .setStartNode("a")
            .build();
        
        assertFalse(GraphUtils.isAcyclic(graph));
    }
    
    @Test
    void testTopologicalSort() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("a", state -> new NodeResult<>(state))
            .addNode("b", state -> new NodeResult<>(state))
            .addNode("c", state -> new NodeResult<>(state))
            .addEdge("a", "b")
            .addEdge("a", "c")
            .addEdge("b", "c")
            .setStartNode("a")
            .build();
        
        var sorted = GraphUtils.topologicalSort(graph);
        
        int aIndex = sorted.indexOf("a");
        int bIndex = sorted.indexOf("b");
        int cIndex = sorted.indexOf("c");
        
        assertTrue(aIndex < bIndex);
        assertTrue(aIndex < cIndex);
        assertTrue(bIndex < cIndex);
    }
    
    @Test
    void testFindReachableNodes() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("a", state -> new NodeResult<>(state))
            .addNode("b", state -> new NodeResult<>(state))
            .addNode("c", state -> new NodeResult<>(state))
            .addNode("d", state -> new NodeResult<>(state))
            .addEdge("a", "b")
            .addEdge("b", "c")
            .setStartNode("a")
            .build();
        
        Set<String> reachable = GraphUtils.findReachableNodes(graph, "a");
        
        assertTrue(reachable.contains("a"));
        assertTrue(reachable.contains("b"));
        assertTrue(reachable.contains("c"));
        assertFalse(reachable.contains("d"));
    }
    
    @Test
    void testFindUnreachableNodes() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("a", state -> new NodeResult<>(state))
            .addNode("b", state -> new NodeResult<>(state))
            .addNode("c", state -> new NodeResult<>(state))
            .addNode("unreachable", state -> new NodeResult<>(state))
            .addEdge("a", "b")
            .addEdge("b", "c")
            .setStartNode("a")
            .build();
        
        Set<String> unreachable = GraphUtils.findUnreachableNodes(graph);
        
        assertEquals(1, unreachable.size());
        assertTrue(unreachable.contains("unreachable"));
    }
    
    @Test
    void testCalculateGraphDepth() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("a", state -> new NodeResult<>(state))
            .addNode("b", state -> new NodeResult<>(state))
            .addNode("c", state -> new NodeResult<>(state))
            .addNode("d", state -> new NodeResult<>(state))
            .addEdge("a", "b")
            .addEdge("b", "c")
            .addEdge("c", "d")
            .setStartNode("a")
            .build();
        
        int depth = GraphUtils.calculateGraphDepth(graph);
        assertEquals(4, depth);
    }
    
    @Test
    void testCalculateNodeDepths() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("a", state -> new NodeResult<>(state))
            .addNode("b", state -> new NodeResult<>(state))
            .addNode("c", state -> new NodeResult<>(state))
            .addEdge("a", "b")
            .addEdge("a", "c")
            .setStartNode("a")
            .build();
        
        Map<String, Integer> depths = GraphUtils.calculateNodeDepths(graph);
        
        assertEquals(0, depths.get("a"));
        assertEquals(1, depths.get("b"));
        assertEquals(1, depths.get("c"));
    }
    
    @Test
    void testValidateGraph_Valid() {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("a", state -> new NodeResult<>(state))
            .addNode("b", state -> new NodeResult<>(state))
            .addEdge("a", "b")
            .setStartNode("a")
            .build();
        
        assertTrue(GraphUtils.validateGraph(graph));
    }
}
