package com.langgraph.node;

import com.langgraph.state.State;

@FunctionalInterface
public interface NodeFunction<T extends State> {
    NodeResult<T> apply(T currentState) throws Exception;
}
