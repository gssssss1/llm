package com.jsonschema.tool;

import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaProperty;

@SchemaDescription("Parameters for calculation")
public class CalculationRequest {
    
    @SchemaProperty(required = true)
    @SchemaDescription("First number")
    private double a;
    
    @SchemaProperty(required = true)
    @SchemaDescription("Second number")
    private double b;
    
    @SchemaProperty(required = true, enumValues = {"add", "subtract", "multiply", "divide"})
    @SchemaDescription("Operation to perform")
    private String operation;
    
    public CalculationRequest() {}
    
    public double getA() {
        return a;
    }
    
    public void setA(double a) {
        this.a = a;
    }
    
    public double getB() {
        return b;
    }
    
    public void setB(double b) {
        this.b = b;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
}
