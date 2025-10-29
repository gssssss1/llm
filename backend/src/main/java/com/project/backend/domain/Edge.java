package com.project.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an edge connecting two nodes in the graph domain.
 * This is a sample domain model to demonstrate the structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Edge {

    private String id;
    private String sourceNodeId;
    private String targetNodeId;
    private String type;

}
