package com.langgraph.execution;

import com.langgraph.checkpoint.Checkpoint;
import com.langgraph.checkpoint.CheckpointStorage;
import com.langgraph.checkpoint.InMemoryCheckpointStorage;
import com.langgraph.core.StateGraph;
import com.langgraph.node.Node;
import com.langgraph.node.NodeResult;
import com.langgraph.plugin.GraphPlugin;
import com.langgraph.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class GraphExecutor<T extends State> {
    private static final Logger logger = LoggerFactory.getLogger(GraphExecutor.class);
    
    private final CheckpointStorage<T> checkpointStorage;
    private final List<GraphPlugin<T>> plugins;
    private final ExecutorService executorService;
    
    public GraphExecutor() {
        this(new InMemoryCheckpointStorage<>());
    }
    
    public GraphExecutor(CheckpointStorage<T> checkpointStorage) {
        this(checkpointStorage, Collections.emptyList());
    }
    
    public GraphExecutor(CheckpointStorage<T> checkpointStorage, List<GraphPlugin<T>> plugins) {
        this.checkpointStorage = checkpointStorage;
        this.plugins = plugins != null ? new ArrayList<>(plugins) : new ArrayList<>();
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }
    
    public CompletableFuture<ExecutionResult<T>> execute(
        StateGraph<T> graph,
        T initialState,
        ExecutionConfig config
    ) {
        ExecutionContext<T> context = new ExecutionContext<>(initialState);
        context.setCurrentNode(graph.getStartNode());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                MDC.put("executionId", context.getExecutionId());
                logger.info("Starting graph execution");
                
                plugins.forEach(plugin -> plugin.beforeExecution(context));
                
                T finalState = executeGraph(graph, context, config);
                
                ExecutionResult<T> result = new ExecutionResult<>(
                    context.getExecutionId(),
                    finalState,
                    context.isInterrupted() ? ExecutionStatus.INTERRUPTED : ExecutionStatus.COMPLETED,
                    context.getExecutedNodes(),
                    context.getStartTime(),
                    Instant.now(),
                    Optional.empty()
                );
                
                plugins.forEach(plugin -> plugin.afterExecution(context));
                
                logger.info("Graph execution completed successfully");
                return result;
                
            } catch (Exception e) {
                logger.error("Graph execution failed", e);
                
                ExecutionResult<T> result = new ExecutionResult<>(
                    context.getExecutionId(),
                    context.getCurrentState(),
                    ExecutionStatus.FAILED,
                    context.getExecutedNodes(),
                    context.getStartTime(),
                    Instant.now(),
                    Optional.of(e)
                );
                
                plugins.forEach(plugin -> plugin.afterExecution(context));
                
                return result;
            } finally {
                MDC.remove("executionId");
            }
        }, executorService);
    }
    
    private T executeGraph(StateGraph<T> graph, ExecutionContext<T> context, ExecutionConfig config) throws Exception {
        T currentState = context.getCurrentState();
        String currentNode = context.getCurrentNode();
        
        while (currentNode != null && !context.isInterrupted()) {
            if (context.getStepCount() >= config.maxSteps()) {
                logger.warn("Max steps reached: {}", config.maxSteps());
                throw new IllegalStateException("Max steps exceeded");
            }
            
            if (graph.getEndNode().isPresent() && currentNode.equals(graph.getEndNode().get())) {
                logger.info("Reached end node: {}", currentNode);
                break;
            }
            
            logger.debug("Executing node: {}", currentNode);
            
            Node<T> node = graph.getNode(currentNode);
            NodeResult<T> result = node.execute(currentState);
            
            currentState = result.updatedState();
            context.setCurrentState(currentState);
            context.addExecutedNode(currentNode);
            context.incrementStepCount();
            
            if (config.enableCheckpointing()) {
                Checkpoint<T> checkpoint = new Checkpoint<>(
                    context.getExecutionId(),
                    currentState,
                    currentNode
                );
                checkpointStorage.save(checkpoint);
                logger.debug("Checkpoint saved for node: {}", currentNode);
            }
            
            List<String> nextNodes = result.hasNextNodes() 
                ? result.nextNodes() 
                : graph.getNextNodes(currentNode, currentState);
            
            if (nextNodes.isEmpty()) {
                logger.info("No next nodes, execution complete");
                break;
            }
            
            if (nextNodes.size() > 1 && config.enableParallelExecution()) {
                currentState = executeNodesInParallel(graph, nextNodes, currentState, context, config);
                currentNode = null;
            } else {
                currentNode = nextNodes.get(0);
                context.setCurrentNode(currentNode);
            }
        }
        
        return currentState;
    }
    
    private T executeNodesInParallel(
        StateGraph<T> graph,
        List<String> nodeNames,
        T state,
        ExecutionContext<T> context,
        ExecutionConfig config
    ) throws Exception {
        logger.debug("Executing {} nodes in parallel", nodeNames.size());
        
        List<CompletableFuture<NodeResult<T>>> futures = nodeNames.stream()
            .map(nodeName -> CompletableFuture.supplyAsync(() -> {
                try {
                    Node<T> node = graph.getNode(nodeName);
                    return node.execute(state);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, executorService))
            .toList();
        
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        allOf.get(config.maxExecutionTime().toMillis(), TimeUnit.MILLISECONDS);
        
        NodeResult<T> lastResult = futures.get(futures.size() - 1).get();
        return lastResult.updatedState();
    }
    
    public CompletableFuture<ExecutionResult<T>> resume(
        StateGraph<T> graph,
        String executionId,
        ExecutionConfig config
    ) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Checkpoint<T>> checkpoint = checkpointStorage.loadLatest(executionId);
            
            if (checkpoint.isEmpty()) {
                throw new IllegalArgumentException("No checkpoint found for execution: " + executionId);
            }
            
            Checkpoint<T> cp = checkpoint.get();
            ExecutionContext<T> context = new ExecutionContext<>(
                executionId,
                cp.state(),
                cp.currentNode()
            );
            
            try {
                MDC.put("executionId", executionId);
                logger.info("Resuming graph execution from node: {}", cp.currentNode());
                
                T finalState = executeGraph(graph, context, config);
                
                return new ExecutionResult<>(
                    executionId,
                    finalState,
                    ExecutionStatus.COMPLETED,
                    context.getExecutedNodes(),
                    context.getStartTime(),
                    Instant.now(),
                    Optional.empty()
                );
            } catch (Exception e) {
                logger.error("Graph execution failed during resume", e);
                
                return new ExecutionResult<>(
                    executionId,
                    context.getCurrentState(),
                    ExecutionStatus.FAILED,
                    context.getExecutedNodes(),
                    context.getStartTime(),
                    Instant.now(),
                    Optional.of(e)
                );
            } finally {
                MDC.remove("executionId");
            }
        }, executorService);
    }
    
    public void interrupt(String executionId) {
        logger.info("Interrupting execution: {}", executionId);
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}
