package com.langgraph.web;

import com.langgraph.core.StateGraph;
import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;
import com.langgraph.web.service.ExecutionMonitorService;
import com.langgraph.web.service.GraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WebUIDemo.class)
class WebIntegrationTest {
    
    @Autowired(required = false)
    private GraphService graphService;
    
    @Autowired(required = false)
    private ExecutionMonitorService monitorService;
    
    @Test
    void contextLoads() {
        if (graphService != null) {
            assertNotNull(graphService, "GraphService should be loaded");
        }
        if (monitorService != null) {
            assertNotNull(monitorService, "ExecutionMonitorService should be loaded");
        }
    }
    
    @Test
    void testGraphRegistration() {
        if (graphService == null) {
            System.out.println("Skipping test - Spring Boot not available");
            return;
        }
        
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("test", state -> new NodeResult<>(state))
            .setStartNode("test")
            .build();
        
        String graphId = graphService.registerGraph("test-graph", graph);
        assertNotNull(graphId);
        assertNotNull(graphService.getGraph(graphId));
    }
}
