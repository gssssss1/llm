package com.jsonschema.examples;

import com.google.gson.JsonObject;
import com.jsonschema.generator.JsonSchemaGenerator;
import com.jsonschema.model.JsonSchema;

public class UsageExample {
    
    public static void main(String[] args) {
        JsonSchemaGenerator generator = new JsonSchemaGenerator();
        
        System.out.println("=== Example 1: Simple User Schema ===");
        String simpleUserSchema = generator.generateSchemaJson(SimpleUser.class);
        System.out.println(simpleUserSchema);
        System.out.println();
        
        System.out.println("=== Example 2: Complex User Schema ===");
        String complexUserSchema = generator.generateSchemaJson(ComplexUser.class);
        System.out.println(complexUserSchema);
        System.out.println();
        
        System.out.println("=== Example 3: Function Calling Schema (OpenAI Format) ===");
        JsonObject functionSchema = generator.generateSchemaForFunctionCalling(
            SimpleUser.class,
            "create_user",
            "Create a new user in the system with the provided information"
        );
        System.out.println(functionSchema.toString());
        System.out.println();
        
        System.out.println("=== Example 4: Address Schema ===");
        JsonSchema addressSchema = generator.generateSchema(Address.class);
        System.out.println(addressSchema.toJson());
        System.out.println();
        
        System.out.println("=== Example 5: Programmatic Schema Access ===");
        JsonSchema schema = generator.generateSchema(SimpleUser.class);
        System.out.println("Schema Type: " + schema.getType());
        System.out.println("Schema Description: " + schema.getDescription());
        System.out.println("Required Fields: " + schema.getRequired());
        System.out.println("Number of Properties: " + schema.getProperties().size());
        
        schema.getProperties().forEach((name, propSchema) -> {
            System.out.println("  - " + name + ": " + propSchema.getType() + 
                             (propSchema.getDescription() != null ? " (" + propSchema.getDescription() + ")" : ""));
        });
    }
}
