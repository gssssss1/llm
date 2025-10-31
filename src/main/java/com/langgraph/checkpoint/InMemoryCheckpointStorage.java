package com.langgraph.checkpoint;

import com.langgraph.state.State;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryCheckpointStorage<T extends State> implements CheckpointStorage<T> {
    private final Map<String, Checkpoint<T>> checkpoints = new ConcurrentHashMap<>();
    
    @Override
    public void save(Checkpoint<T> checkpoint) {
        checkpoints.put(checkpoint.id(), checkpoint);
    }
    
    @Override
    public Optional<Checkpoint<T>> load(String checkpointId) {
        return Optional.ofNullable(checkpoints.get(checkpointId));
    }
    
    @Override
    public List<Checkpoint<T>> loadByExecution(String executionId) {
        return checkpoints.values().stream()
            .filter(cp -> cp.executionId().equals(executionId))
            .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Checkpoint<T>> loadLatest(String executionId) {
        return loadByExecution(executionId).stream()
            .findFirst();
    }
    
    @Override
    public void delete(String checkpointId) {
        checkpoints.remove(checkpointId);
    }
    
    @Override
    public void deleteByExecution(String executionId) {
        checkpoints.entrySet().removeIf(entry -> 
            entry.getValue().executionId().equals(executionId));
    }
}
