package com.jsonschema.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jsonschema.examples.*;
import com.jsonschema.model.JsonSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaGeneratorTest {
    
    private JsonSchemaGenerator generator;
    
    @BeforeEach
    public void setUp() {
        generator = new JsonSchemaGenerator();
    }
    
    @Test
    public void testSimpleUserSchema() {
        JsonSchema schema = generator.generateSchema(SimpleUser.class);
        
        assertNotNull(schema);
        assertEquals("object", schema.getType());
        assertEquals("A simple user object", schema.getDescription());
        
        assertTrue(schema.getProperties().containsKey("id"));
        assertTrue(schema.getProperties().containsKey("name"));
        assertTrue(schema.getProperties().containsKey("email"));
        assertTrue(schema.getProperties().containsKey("age"));
        assertTrue(schema.getProperties().containsKey("active"));
        
        assertTrue(schema.getRequired().contains("id"));
        assertTrue(schema.getRequired().contains("name"));
        
        JsonSchema nameSchema = schema.getProperties().get("name");
        assertEquals("string", nameSchema.getType());
        assertEquals(2, nameSchema.getMinLength());
        assertEquals(50, nameSchema.getMaxLength());
        
        JsonSchema emailSchema = schema.getProperties().get("email");
        assertEquals("email", emailSchema.getFormat());
        
        JsonSchema ageSchema = schema.getProperties().get("age");
        assertEquals("integer", ageSchema.getType());
        assertEquals(0, ageSchema.getMinimum().intValue());
        assertEquals(150, ageSchema.getMaximum().intValue());
        
        JsonSchema activeSchema = schema.getProperties().get("active");
        assertEquals("boolean", activeSchema.getType());
    }
    
    @Test
    public void testAddressSchema() {
        JsonSchema schema = generator.generateSchema(Address.class);
        
        assertNotNull(schema);
        assertEquals("object", schema.getType());
        assertEquals("Physical address information", schema.getDescription());
        
        assertEquals(3, schema.getRequired().size());
        assertTrue(schema.getRequired().contains("street"));
        assertTrue(schema.getRequired().contains("city"));
        assertTrue(schema.getRequired().contains("country"));
        
        JsonSchema zipCodeSchema = schema.getProperties().get("zipCode");
        assertNotNull(zipCodeSchema.getPattern());
        assertEquals("^\\d{5}(-\\d{4})?$", zipCodeSchema.getPattern());
    }
    
    @Test
    public void testComplexUserWithNestedObjects() {
        JsonSchema schema = generator.generateSchema(ComplexUser.class);
        
        assertNotNull(schema);
        assertEquals("object", schema.getType());
        
        assertTrue(schema.getProperties().containsKey("primaryAddress"));
        JsonSchema addressSchema = schema.getProperties().get("primaryAddress");
        assertEquals("object", addressSchema.getType());
        assertTrue(addressSchema.getProperties().containsKey("street"));
        assertTrue(addressSchema.getProperties().containsKey("city"));
    }
    
    @Test
    public void testListType() {
        JsonSchema schema = generator.generateSchema(ComplexUser.class);
        
        assertTrue(schema.getProperties().containsKey("alternateAddresses"));
        JsonSchema alternateAddressesSchema = schema.getProperties().get("alternateAddresses");
        assertEquals("array", alternateAddressesSchema.getType());
        
        JsonSchema itemSchema = alternateAddressesSchema.getItems();
        assertNotNull(itemSchema);
        assertEquals("object", itemSchema.getType());
        assertTrue(itemSchema.getProperties().containsKey("street"));
    }
    
    @Test
    public void testSetType() {
        JsonSchema schema = generator.generateSchema(ComplexUser.class);
        
        assertTrue(schema.getProperties().containsKey("roles"));
        JsonSchema rolesSchema = schema.getProperties().get("roles");
        assertEquals("array", rolesSchema.getType());
        
        JsonSchema itemSchema = rolesSchema.getItems();
        assertNotNull(itemSchema);
        assertEquals("string", itemSchema.getType());
    }
    
    @Test
    public void testArrayType() {
        JsonSchema schema = generator.generateSchema(ComplexUser.class);
        
        assertTrue(schema.getProperties().containsKey("tags"));
        JsonSchema tagsSchema = schema.getProperties().get("tags");
        assertEquals("array", tagsSchema.getType());
        
        JsonSchema itemSchema = tagsSchema.getItems();
        assertNotNull(itemSchema);
        assertEquals("string", itemSchema.getType());
    }
    
    @Test
    public void testMapType() {
        JsonSchema schema = generator.generateSchema(ComplexUser.class);
        
        assertTrue(schema.getProperties().containsKey("metadata"));
        JsonSchema metadataSchema = schema.getProperties().get("metadata");
        assertEquals("object", metadataSchema.getType());
        
        JsonSchema additionalProps = metadataSchema.getAdditionalProperties();
        assertNotNull(additionalProps);
        assertEquals("string", additionalProps.getType());
    }
    
    @Test
    public void testEnumType() {
        JsonSchema schema = generator.generateSchema(ComplexUser.class);
        
        assertTrue(schema.getProperties().containsKey("userType"));
        JsonSchema userTypeSchema = schema.getProperties().get("userType");
        assertEquals("string", userTypeSchema.getType());
        
        assertNotNull(userTypeSchema.getEnumValues());
        assertEquals(4, userTypeSchema.getEnumValues().size());
        assertTrue(userTypeSchema.getEnumValues().contains("ADMIN"));
        assertTrue(userTypeSchema.getEnumValues().contains("REGULAR"));
        assertTrue(userTypeSchema.getEnumValues().contains("GUEST"));
        assertTrue(userTypeSchema.getEnumValues().contains("PREMIUM"));
    }
    
    @Test
    public void testSchemaIgnore() {
        JsonSchema schema = generator.generateSchema(ComplexUser.class);
        
        assertFalse(schema.getProperties().containsKey("internalToken"));
    }
    
    @Test
    public void testGenerateSchemaJson() {
        String json = generator.generateSchemaJson(SimpleUser.class);
        
        assertNotNull(json);
        assertFalse(json.isEmpty());
        
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        assertEquals("object", jsonObject.get("type").getAsString());
        assertTrue(jsonObject.has("properties"));
        assertTrue(jsonObject.has("required"));
    }
    
    @Test
    public void testFunctionCallingSchema() {
        JsonObject functionSchema = generator.generateSchemaForFunctionCalling(
            SimpleUser.class,
            "create_user",
            "Create a new user in the system"
        );
        
        assertNotNull(functionSchema);
        assertEquals("create_user", functionSchema.get("name").getAsString());
        assertEquals("Create a new user in the system", functionSchema.get("description").getAsString());
        
        assertTrue(functionSchema.has("parameters"));
        JsonObject parameters = functionSchema.getAsJsonObject("parameters");
        assertEquals("object", parameters.get("type").getAsString());
        assertTrue(parameters.has("properties"));
        assertTrue(parameters.has("required"));
        
        JsonObject properties = parameters.getAsJsonObject("properties");
        assertTrue(properties.has("id"));
        assertTrue(properties.has("name"));
        assertTrue(properties.has("email"));
        
        JsonArray required = parameters.getAsJsonArray("required");
        assertEquals(2, required.size());
    }
    
    @Test
    public void testPrimitiveTypes() {
        JsonSchema schema = generator.generateSchema(SimpleUser.class);
        
        JsonSchema activeSchema = schema.getProperties().get("active");
        assertEquals("boolean", activeSchema.getType());
    }
    
    @Test
    public void testDescriptions() {
        JsonSchema schema = generator.generateSchema(SimpleUser.class);
        
        JsonSchema idSchema = schema.getProperties().get("id");
        assertEquals("User's unique identifier", idSchema.getDescription());
        
        JsonSchema nameSchema = schema.getProperties().get("name");
        assertEquals("User's full name", nameSchema.getDescription());
    }
    
    @Test
    public void testConstraints() {
        JsonSchema schema = generator.generateSchema(SimpleUser.class);
        
        JsonSchema nameSchema = schema.getProperties().get("name");
        assertEquals(2, nameSchema.getMinLength());
        assertEquals(50, nameSchema.getMaxLength());
        
        JsonSchema ageSchema = schema.getProperties().get("age");
        assertEquals(0, ageSchema.getMinimum().intValue());
        assertEquals(150, ageSchema.getMaximum().intValue());
    }
}
