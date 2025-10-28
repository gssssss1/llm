package com.jsonschema.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.*;

public class JsonSchema {
    private String type;
    private String description;
    private Map<String, JsonSchema> properties;
    private List<String> required;
    private JsonSchema items;
    private JsonSchema additionalProperties;
    private String format;
    private String pattern;
    private Number minimum;
    private Number maximum;
    private Integer minLength;
    private Integer maxLength;
    private Integer minItems;
    private Integer maxItems;
    private List<Object> enumValues;
    private String ref;

    public JsonSchema() {
        this.properties = new LinkedHashMap<>();
        this.required = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, JsonSchema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, JsonSchema> properties) {
        this.properties = properties;
    }

    public void addProperty(String name, JsonSchema schema) {
        this.properties.put(name, schema);
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public void addRequired(String fieldName) {
        if (!this.required.contains(fieldName)) {
            this.required.add(fieldName);
        }
    }

    public JsonSchema getItems() {
        return items;
    }

    public void setItems(JsonSchema items) {
        this.items = items;
    }

    public JsonSchema getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(JsonSchema additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Number getMinimum() {
        return minimum;
    }

    public void setMinimum(Number minimum) {
        this.minimum = minimum;
    }

    public Number getMaximum() {
        return maximum;
    }

    public void setMaximum(Number maximum) {
        this.maximum = maximum;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMinItems() {
        return minItems;
    }

    public void setMinItems(Integer minItems) {
        this.minItems = minItems;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public List<Object> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<Object> enumValues) {
        this.enumValues = enumValues;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        
        JsonObject obj = new JsonObject();
        
        if (type != null) {
            obj.addProperty("type", type);
        }
        
        if (description != null && !description.isEmpty()) {
            obj.addProperty("description", description);
        }
        
        if (properties != null && !properties.isEmpty()) {
            JsonObject propsObj = new JsonObject();
            for (Map.Entry<String, JsonSchema> entry : properties.entrySet()) {
                propsObj.add(entry.getKey(), com.google.gson.JsonParser.parseString(entry.getValue().toJson()));
            }
            obj.add("properties", propsObj);
        }
        
        if (required != null && !required.isEmpty()) {
            JsonArray reqArray = new JsonArray();
            for (String req : required) {
                reqArray.add(req);
            }
            obj.add("required", reqArray);
        }
        
        if (items != null) {
            obj.add("items", com.google.gson.JsonParser.parseString(items.toJson()));
        }
        
        if (additionalProperties != null) {
            obj.add("additionalProperties", com.google.gson.JsonParser.parseString(additionalProperties.toJson()));
        }
        
        if (format != null && !format.isEmpty()) {
            obj.addProperty("format", format);
        }
        
        if (pattern != null && !pattern.isEmpty()) {
            obj.addProperty("pattern", pattern);
        }
        
        if (minimum != null) {
            obj.addProperty("minimum", minimum);
        }
        
        if (maximum != null) {
            obj.addProperty("maximum", maximum);
        }
        
        if (minLength != null) {
            obj.addProperty("minLength", minLength);
        }
        
        if (maxLength != null) {
            obj.addProperty("maxLength", maxLength);
        }
        
        if (minItems != null) {
            obj.addProperty("minItems", minItems);
        }
        
        if (maxItems != null) {
            obj.addProperty("maxItems", maxItems);
        }
        
        if (enumValues != null && !enumValues.isEmpty()) {
            JsonArray enumArray = new JsonArray();
            for (Object enumValue : enumValues) {
                enumArray.add(enumValue.toString());
            }
            obj.add("enum", enumArray);
        }
        
        if (ref != null && !ref.isEmpty()) {
            obj.addProperty("$ref", ref);
        }
        
        return gson.toJson(obj);
    }

    public JsonObject toJsonObject() {
        return com.google.gson.JsonParser.parseString(toJson()).getAsJsonObject();
    }
}
