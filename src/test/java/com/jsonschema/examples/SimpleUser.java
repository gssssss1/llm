package com.jsonschema.examples;

import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaProperty;

@SchemaDescription("A simple user object")
public class SimpleUser {
    
    @SchemaProperty(required = true)
    @SchemaDescription("User's unique identifier")
    private String id;
    
    @SchemaProperty(required = true, minLength = 2, maxLength = 50)
    @SchemaDescription("User's full name")
    private String name;
    
    @SchemaProperty(format = "email")
    @SchemaDescription("User's email address")
    private String email;
    
    @SchemaProperty(minimum = 0, maximum = 150)
    @SchemaDescription("User's age in years")
    private Integer age;
    
    @SchemaDescription("Whether the user is active")
    private boolean active;
    
    public SimpleUser() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
