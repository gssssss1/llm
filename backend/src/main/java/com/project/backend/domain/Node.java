package com.project.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a node in the graph domain.
 * This is a sample domain model to demonstrate the structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    private String id;
    private String type;
    private String label;

}
