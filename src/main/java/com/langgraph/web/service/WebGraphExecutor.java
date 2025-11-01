package com.langgraph.web.service;

import com.langgraph.checkpoint.CheckpointStorage;
import com.langgraph.checkpoint.InMemoryCheckpointStorage;
import com.langgraph.core.StateGraph;
import com.langgraph.execution.ExecutionConfig;
import com.langgraph.execution.ExecutionResult;
import com.langgraph.execution.GraphExecutor;
import com.langgraph.node.Node;
import com.langgraph.node.NodeFunction;
import com.langgraph.node.NodeResult;
import com.langgraph.state.State;
import com.langgraph.web.model.ExecutionEvent;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class WebGraphExecutor {
    
    private final ExecutionMonitorService monitorService;
    private final CheckpointStorage<?> checkpointStorage;
    
    public WebGraphExecutor(ExecutionMonitorService monitorService) {
        this.monitorService = monitorService;
        this.checkpointStorage = new InMemoryCheckpointStorage<>();
    }
    
    public <T extends State> CompletableFuture<ExecutionResult<T>> execute(
        StateGraph<T> graph,
        T initialState,
        ExecutionConfig config
    ) {
        StateGraph<T> wrappedGraph = wrapGraphWithMonitoring(graph);
        
        GraphExecutor<T> executor = new GraphExecutor<>((CheckpointStorage<T>) checkpointStorage);
        
        CompletableFuture<ExecutionResult<T>> future = executor.execute(wrappedGraph, initialState, config);
        
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                monitorService.publishEvent(
                    ExecutionEvent.executionFailed(result != null ? result.executionId() : "unknown", 
                        throwable.getMessage())
                );
            } else if (result != null) {
                monitorService.publishEvent(
                    ExecutionEvent.executionCompleted(result.executionId(), result.finalState().data())
                );
            }
        });
        
        return future;
    }
    
    private <T extends State> StateGraph<T> wrapGraphWithMonitoring(StateGraph<T> originalGraph) {
        StateGraph.Builder<T> builder = StateGraph.builder();
        
        for (var entry : originalGraph.getNodes().entrySet()) {
            String nodeName = entry.getKey();
            Node<T> originalNode = entry.getValue();
            
            NodeFunction<T> wrappedFunction = state -> {
                String executionId = state.id();
                
                monitorService.publishEvent(
                    ExecutionEvent.nodeStarted(executionId, nodeName, state.data())
                );
                
                try {
                    NodeResult<T> result = originalNode.execute(state);
                    
                    monitorService.publishEvent(
                        ExecutionEvent.nodeCompleted(executionId, nodeName, result.updatedState().data())
                    );
                    
                    return result;
                } catch (Exception e) {
                    monitorService.publishEvent(
                        ExecutionEvent.nodeFailed(executionId, nodeName, e.getMessage())
                    );
                    throw e;
                }
            };
            
            builder.addNode(new Node<>(nodeName, wrappedFunction, originalNode.config()));
        }
        
        for (var edge : originalGraph.getEdges()) {
            if (edge instanceof com.langgraph.edge.StaticEdge<T> staticEdge) {
                builder.addEdge(staticEdge.from(), staticEdge.to());
            } else if (edge instanceof com.langgraph.edge.ConditionalEdge<T> conditionalEdge) {
                builder.addConditionalEdge(conditionalEdge.from(), conditionalEdge.condition());
            } else if (edge instanceof com.langgraph.edge.ParallelEdge<T> parallelEdge) {
                builder.addParallelEdge(parallelEdge.from(), parallelEdge.targets().toArray(new String[0]));
            }
        }
        
        builder.setStartNode(originalGraph.getStartNode());
        originalGraph.getEndNode().ifPresent(builder::setEndNode);
        
        return builder.build();
    }
}
