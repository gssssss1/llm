package com.langgraph.web.service;

import com.langgraph.web.model.ExecutionEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExecutionMonitorService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, List<ExecutionEvent>> executionLogs = new ConcurrentHashMap<>();
    
    public ExecutionMonitorService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    public void publishEvent(ExecutionEvent event) {
        executionLogs.computeIfAbsent(event.executionId(), k -> new ArrayList<>()).add(event);
        
        messagingTemplate.convertAndSend("/topic/executions/" + event.executionId(), event);
        
        messagingTemplate.convertAndSend("/topic/executions/all", event);
    }
    
    public List<ExecutionEvent> getExecutionLogs(String executionId) {
        return executionLogs.getOrDefault(executionId, new ArrayList<>());
    }
    
    public Map<String, List<ExecutionEvent>> getAllExecutionLogs() {
        return Map.copyOf(executionLogs);
    }
    
    public void clearLogs(String executionId) {
        executionLogs.remove(executionId);
    }
    
    public void clearAllLogs() {
        executionLogs.clear();
    }
}
