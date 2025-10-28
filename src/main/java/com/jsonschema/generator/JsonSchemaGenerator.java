package com.jsonschema.generator;

import com.google.gson.*;
import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaIgnore;
import com.jsonschema.annotations.SchemaProperty;
import com.jsonschema.model.JsonSchema;

import java.lang.reflect.*;
import java.util.*;

public class JsonSchemaGenerator {
    
    private static final String SCHEMA_VERSION = "http://json-schema.org/draft-07/schema#";
    
    public JsonSchema generateSchema(Class<?> clazz) {
        JsonSchema schema = new JsonSchema();
        schema.setType("object");
        
        SchemaDescription classDescription = clazz.getAnnotation(SchemaDescription.class);
        if (classDescription != null) {
            schema.setDescription(classDescription.value());
        }
        
        Set<Class<?>> processingPath = new HashSet<>();
        processClass(clazz, schema, processingPath);
        
        return schema;
    }
    
    public String generateSchemaJson(Class<?> clazz) {
        JsonSchema schema = generateSchema(clazz);
        return toFormattedJson(schema);
    }
    
    public JsonObject generateSchemaForFunctionCalling(Class<?> clazz, String functionName, String functionDescription) {
        JsonSchema parametersSchema = generateSchema(clazz);
        
        JsonObject functionSchema = new JsonObject();
        functionSchema.addProperty("name", functionName);
        functionSchema.addProperty("description", functionDescription);
        
        JsonObject parameters = convertToJsonObject(parametersSchema);
        functionSchema.add("parameters", parameters);
        
        return functionSchema;
    }
    
    private void processClass(Class<?> clazz, JsonSchema schema, Set<Class<?>> processingPath) {
        if (processingPath.contains(clazz)) {
            return;
        }
        
        processingPath.add(clazz);
        try {
            Field[] fields = getAllFields(clazz);
            
            for (Field field : fields) {
                if (shouldIgnoreField(field)) {
                    continue;
                }
                
                String fieldName = getFieldName(field);
                JsonSchema fieldSchema = generateSchemaForField(field, processingPath);
                schema.addProperty(fieldName, fieldSchema);
                
                if (isRequired(field)) {
                    schema.addRequired(fieldName);
                }
            }
        } finally {
            processingPath.remove(clazz);
        }
    }
    
    private Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        
        return fields.toArray(new Field[0]);
    }
    
    private boolean shouldIgnoreField(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
            return true;
        }
        
        return field.isAnnotationPresent(SchemaIgnore.class);
    }
    
    private String getFieldName(Field field) {
        SchemaProperty property = field.getAnnotation(SchemaProperty.class);
        if (property != null && !property.name().isEmpty()) {
            return property.name();
        }
        return field.getName();
    }
    
    private boolean isRequired(Field field) {
        SchemaProperty property = field.getAnnotation(SchemaProperty.class);
        return property != null && property.required();
    }
    
    private JsonSchema generateSchemaForField(Field field, Set<Class<?>> processingPath) {
        JsonSchema fieldSchema = new JsonSchema();
        
        SchemaDescription description = field.getAnnotation(SchemaDescription.class);
        if (description != null) {
            fieldSchema.setDescription(description.value());
        }
        
        SchemaProperty property = field.getAnnotation(SchemaProperty.class);
        if (property != null) {
            applyPropertyAnnotation(fieldSchema, property);
        }
        
        Class<?> fieldType = field.getType();
        Type genericType = field.getGenericType();
        
        processFieldType(fieldType, genericType, fieldSchema, processingPath);
        
        return fieldSchema;
    }
    
    private void applyPropertyAnnotation(JsonSchema schema, SchemaProperty property) {
        if (!property.format().isEmpty()) {
            schema.setFormat(property.format());
        }
        
        if (!property.pattern().isEmpty()) {
            schema.setPattern(property.pattern());
        }
        
        if (property.minimum() != Double.NEGATIVE_INFINITY) {
            schema.setMinimum(property.minimum());
        }
        
        if (property.maximum() != Double.POSITIVE_INFINITY) {
            schema.setMaximum(property.maximum());
        }
        
        if (property.minLength() >= 0) {
            schema.setMinLength(property.minLength());
        }
        
        if (property.maxLength() >= 0) {
            schema.setMaxLength(property.maxLength());
        }
        
        if (property.minItems() >= 0) {
            schema.setMinItems(property.minItems());
        }
        
        if (property.maxItems() >= 0) {
            schema.setMaxItems(property.maxItems());
        }
        
        if (property.enumValues().length > 0) {
            schema.setEnumValues(Arrays.asList(property.enumValues()));
        }
    }
    
    private void processFieldType(Class<?> fieldType, Type genericType, JsonSchema schema, Set<Class<?>> processingPath) {
        if (fieldType.isPrimitive()) {
            processPrimitiveType(fieldType, schema);
        } else if (isWrapperType(fieldType)) {
            processWrapperType(fieldType, schema);
        } else if (fieldType == String.class) {
            schema.setType("string");
        } else if (fieldType.isArray()) {
            processArrayType(fieldType, schema, processingPath);
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            processCollectionType(genericType, schema, processingPath);
        } else if (Map.class.isAssignableFrom(fieldType)) {
            processMapType(genericType, schema, processingPath);
        } else if (fieldType.isEnum()) {
            processEnumType(fieldType, schema);
        } else {
            processObjectType(fieldType, schema, processingPath);
        }
    }
    
    private void processPrimitiveType(Class<?> type, JsonSchema schema) {
        if (type == int.class || type == long.class || type == short.class || type == byte.class) {
            schema.setType("integer");
        } else if (type == float.class || type == double.class) {
            schema.setType("number");
        } else if (type == boolean.class) {
            schema.setType("boolean");
        } else if (type == char.class) {
            schema.setType("string");
        }
    }
    
    private boolean isWrapperType(Class<?> type) {
        return type == Integer.class || type == Long.class || type == Short.class ||
               type == Byte.class || type == Float.class || type == Double.class ||
               type == Boolean.class || type == Character.class;
    }
    
    private void processWrapperType(Class<?> type, JsonSchema schema) {
        if (type == Integer.class || type == Long.class || type == Short.class || type == Byte.class) {
            schema.setType("integer");
        } else if (type == Float.class || type == Double.class) {
            schema.setType("number");
        } else if (type == Boolean.class) {
            schema.setType("boolean");
        } else if (type == Character.class) {
            schema.setType("string");
        }
    }
    
    private void processArrayType(Class<?> arrayType, JsonSchema schema, Set<Class<?>> processingPath) {
        schema.setType("array");
        Class<?> componentType = arrayType.getComponentType();
        JsonSchema itemSchema = new JsonSchema();
        processFieldType(componentType, componentType, itemSchema, processingPath);
        schema.setItems(itemSchema);
    }
    
    private void processCollectionType(Type genericType, JsonSchema schema, Set<Class<?>> processingPath) {
        schema.setType("array");
        
        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] typeArgs = paramType.getActualTypeArguments();
            
            if (typeArgs.length > 0) {
                Type elementType = typeArgs[0];
                JsonSchema itemSchema = new JsonSchema();
                
                if (elementType instanceof Class) {
                    processFieldType((Class<?>) elementType, elementType, itemSchema, processingPath);
                } else if (elementType instanceof ParameterizedType) {
                    Class<?> rawType = (Class<?>) ((ParameterizedType) elementType).getRawType();
                    processFieldType(rawType, elementType, itemSchema, processingPath);
                }
                
                schema.setItems(itemSchema);
            }
        } else {
            JsonSchema itemSchema = new JsonSchema();
            schema.setItems(itemSchema);
        }
    }
    
    private void processMapType(Type genericType, JsonSchema schema, Set<Class<?>> processingPath) {
        schema.setType("object");
        
        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] typeArgs = paramType.getActualTypeArguments();
            
            if (typeArgs.length == 2) {
                Type valueType = typeArgs[1];
                JsonSchema valueSchema = new JsonSchema();
                
                if (valueType instanceof Class) {
                    processFieldType((Class<?>) valueType, valueType, valueSchema, processingPath);
                } else if (valueType instanceof ParameterizedType) {
                    Class<?> rawType = (Class<?>) ((ParameterizedType) valueType).getRawType();
                    processFieldType(rawType, valueType, valueSchema, processingPath);
                }
                
                schema.setAdditionalProperties(valueSchema);
            }
        } else {
            JsonSchema valueSchema = new JsonSchema();
            schema.setAdditionalProperties(valueSchema);
        }
    }
    
    private void processEnumType(Class<?> enumType, JsonSchema schema) {
        schema.setType("string");
        Object[] constants = enumType.getEnumConstants();
        List<Object> enumValues = new ArrayList<>();
        for (Object constant : constants) {
            enumValues.add(constant.toString());
        }
        schema.setEnumValues(enumValues);
    }
    
    private void processObjectType(Class<?> objectType, JsonSchema schema, Set<Class<?>> processingPath) {
        schema.setType("object");
        
        SchemaDescription classDescription = objectType.getAnnotation(SchemaDescription.class);
        if (classDescription != null) {
            schema.setDescription(classDescription.value());
        }
        
        processClass(objectType, schema, processingPath);
    }
    
    private String toFormattedJson(JsonSchema schema) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(JsonSchema.class, new JsonSchemaSerializer())
                .create();
        return gson.toJson(schema);
    }
    
    private JsonObject convertToJsonObject(JsonSchema schema) {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(JsonSchema.class, new JsonSchemaSerializer())
                .create();
        String json = gson.toJson(schema);
        return JsonParser.parseString(json).getAsJsonObject();
    }
    
    private static class JsonSchemaSerializer implements JsonSerializer<JsonSchema> {
        @Override
        public JsonElement serialize(JsonSchema src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            
            if (src.getType() != null) {
                obj.addProperty("type", src.getType());
            }
            
            if (src.getDescription() != null && !src.getDescription().isEmpty()) {
                obj.addProperty("description", src.getDescription());
            }
            
            if (src.getProperties() != null && !src.getProperties().isEmpty()) {
                JsonObject properties = new JsonObject();
                for (Map.Entry<String, JsonSchema> entry : src.getProperties().entrySet()) {
                    properties.add(entry.getKey(), serialize(entry.getValue(), typeOfSrc, context));
                }
                obj.add("properties", properties);
            }
            
            if (src.getRequired() != null && !src.getRequired().isEmpty()) {
                JsonArray required = new JsonArray();
                for (String req : src.getRequired()) {
                    required.add(req);
                }
                obj.add("required", required);
            }
            
            if (src.getItems() != null) {
                obj.add("items", serialize(src.getItems(), typeOfSrc, context));
            }
            
            if (src.getAdditionalProperties() != null) {
                obj.add("additionalProperties", serialize(src.getAdditionalProperties(), typeOfSrc, context));
            }
            
            if (src.getFormat() != null && !src.getFormat().isEmpty()) {
                obj.addProperty("format", src.getFormat());
            }
            
            if (src.getPattern() != null && !src.getPattern().isEmpty()) {
                obj.addProperty("pattern", src.getPattern());
            }
            
            if (src.getMinimum() != null) {
                obj.addProperty("minimum", src.getMinimum());
            }
            
            if (src.getMaximum() != null) {
                obj.addProperty("maximum", src.getMaximum());
            }
            
            if (src.getMinLength() != null) {
                obj.addProperty("minLength", src.getMinLength());
            }
            
            if (src.getMaxLength() != null) {
                obj.addProperty("maxLength", src.getMaxLength());
            }
            
            if (src.getMinItems() != null) {
                obj.addProperty("minItems", src.getMinItems());
            }
            
            if (src.getMaxItems() != null) {
                obj.addProperty("maxItems", src.getMaxItems());
            }
            
            if (src.getEnumValues() != null && !src.getEnumValues().isEmpty()) {
                JsonArray enumArray = new JsonArray();
                for (Object enumValue : src.getEnumValues()) {
                    enumArray.add(enumValue.toString());
                }
                obj.add("enum", enumArray);
            }
            
            if (src.getRef() != null && !src.getRef().isEmpty()) {
                obj.addProperty("$ref", src.getRef());
            }
            
            return obj;
        }
    }
}
