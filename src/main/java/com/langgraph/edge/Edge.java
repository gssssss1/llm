package com.langgraph.edge;

import com.langgraph.state.State;

public sealed interface Edge<T extends State> permits ConditionalEdge, StaticEdge, ParallelEdge {
    String from();
    String to();
}
