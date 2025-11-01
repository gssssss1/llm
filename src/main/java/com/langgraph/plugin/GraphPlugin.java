package com.langgraph.plugin;

import com.langgraph.execution.ExecutionContext;
import com.langgraph.state.State;

public interface GraphPlugin<T extends State> {
    void beforeExecution(ExecutionContext<T> context);
    
    void afterExecution(ExecutionContext<T> context);
}
