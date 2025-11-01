package com.langgraph.web.controller;

import com.langgraph.core.StateGraph;
import com.langgraph.execution.ExecutionConfig;
import com.langgraph.execution.ExecutionResult;
import com.langgraph.state.State;
import com.langgraph.state.StateRecord;
import com.langgraph.web.model.ExecutionEvent;
import com.langgraph.web.model.GraphInfo;
import com.langgraph.web.service.ExecutionMonitorService;
import com.langgraph.web.service.GraphService;
import com.langgraph.web.service.WebGraphExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GraphController {
    
    private final GraphService graphService;
    private final WebGraphExecutor webGraphExecutor;
    private final ExecutionMonitorService monitorService;
    
    public GraphController(GraphService graphService, 
                          WebGraphExecutor webGraphExecutor,
                          ExecutionMonitorService monitorService) {
        this.graphService = graphService;
        this.webGraphExecutor = webGraphExecutor;
        this.monitorService = monitorService;
    }
    
    @GetMapping("/graphs")
    public ResponseEntity<Map<String, Object>> getAllGraphs() {
        Map<String, StateGraph<?>> graphs = graphService.getAllGraphs();
        Map<String, Object> result = Map.of(
            "count", graphs.size(),
            "graphs", graphs.keySet()
        );
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/graphs/{id}")
    public ResponseEntity<GraphInfo> getGraph(@PathVariable String id) {
        GraphInfo graphInfo = graphService.getGraphInfo(id);
        if (graphInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(graphInfo);
    }
    
    @GetMapping("/graphs/{id}/mermaid")
    public ResponseEntity<Map<String, String>> getMermaidDiagram(@PathVariable String id) {
        String mermaid = graphService.getMermaidDiagram(id);
        if (mermaid == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("diagram", mermaid));
    }
    
    @PostMapping("/graphs/{id}/execute")
    public ResponseEntity<Map<String, String>> executeGraph(
        @PathVariable String id,
        @RequestBody(required = false) Map<String, Object> initialData
    ) {
        StateGraph<?> graph = graphService.getGraph(id);
        if (graph == null) {
            return ResponseEntity.notFound().build();
        }
        
        StateRecord initialState = initialData != null 
            ? new StateRecord(initialData) 
            : new StateRecord();
        
        monitorService.publishEvent(
            ExecutionEvent.executionStarted(initialState.id(), initialState.data())
        );
        
        StateGraph<StateRecord> typedGraph = (StateGraph<StateRecord>) graph;
        webGraphExecutor.execute(typedGraph, initialState, ExecutionConfig.DEFAULT);
        
        return ResponseEntity.ok(Map.of(
            "executionId", initialState.id(),
            "status", "started"
        ));
    }
    
    @GetMapping("/executions/{executionId}/logs")
    public ResponseEntity<List<ExecutionEvent>> getExecutionLogs(@PathVariable String executionId) {
        List<ExecutionEvent> logs = monitorService.getExecutionLogs(executionId);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/executions/logs")
    public ResponseEntity<Map<String, List<ExecutionEvent>>> getAllExecutionLogs() {
        Map<String, List<ExecutionEvent>> logs = monitorService.getAllExecutionLogs();
        return ResponseEntity.ok(logs);
    }
    
    @DeleteMapping("/executions/{executionId}/logs")
    public ResponseEntity<Void> clearExecutionLogs(@PathVariable String executionId) {
        monitorService.clearLogs(executionId);
        return ResponseEntity.ok().build();
    }
}
