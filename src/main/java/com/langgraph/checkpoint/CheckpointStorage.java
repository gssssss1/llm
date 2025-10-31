package com.langgraph.checkpoint;

import com.langgraph.state.State;

import java.util.List;
import java.util.Optional;

public interface CheckpointStorage<T extends State> {
    void save(Checkpoint<T> checkpoint);
    
    Optional<Checkpoint<T>> load(String checkpointId);
    
    List<Checkpoint<T>> loadByExecution(String executionId);
    
    Optional<Checkpoint<T>> loadLatest(String executionId);
    
    void delete(String checkpointId);
    
    void deleteByExecution(String executionId);
}
