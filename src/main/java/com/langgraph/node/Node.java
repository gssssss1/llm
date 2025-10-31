package com.langgraph.node;

import com.langgraph.state.State;

public record Node<T extends State>(
    String name,
    NodeFunction<T> function,
    NodeConfig config
) {
    public Node {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Node name cannot be null or blank");
        }
        if (function == null) {
            throw new IllegalArgumentException("Node function cannot be null");
        }
        if (config == null) {
            config = NodeConfig.DEFAULT;
        }
    }
    
    public Node(String name, NodeFunction<T> function) {
        this(name, function, NodeConfig.DEFAULT);
    }
    
    public NodeResult<T> execute(T state) throws Exception {
        return function.apply(state);
    }
}
