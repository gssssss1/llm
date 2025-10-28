package com.jsonschema.examples;

import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaIgnore;
import com.jsonschema.annotations.SchemaProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SchemaDescription("A complex user object with nested structures")
public class ComplexUser {
    
    @SchemaProperty(required = true)
    @SchemaDescription("User's unique identifier")
    private String id;
    
    @SchemaProperty(required = true)
    @SchemaDescription("User's name")
    private String name;
    
    @SchemaDescription("User's primary address")
    private Address primaryAddress;
    
    @SchemaDescription("List of alternate addresses")
    private List<Address> alternateAddresses;
    
    @SchemaDescription("Set of user roles")
    private Set<String> roles;
    
    @SchemaDescription("User metadata as key-value pairs")
    private Map<String, String> metadata;
    
    @SchemaDescription("User's preferred tags")
    private String[] tags;
    
    @SchemaDescription("User type")
    private UserType userType;
    
    @SchemaIgnore
    private String internalToken;
    
    public ComplexUser() {}
    
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
    
    public Address getPrimaryAddress() {
        return primaryAddress;
    }
    
    public void setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = primaryAddress;
    }
    
    public List<Address> getAlternateAddresses() {
        return alternateAddresses;
    }
    
    public void setAlternateAddresses(List<Address> alternateAddresses) {
        this.alternateAddresses = alternateAddresses;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String[] getTags() {
        return tags;
    }
    
    public void setTags(String[] tags) {
        this.tags = tags;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
    public String getInternalToken() {
        return internalToken;
    }
    
    public void setInternalToken(String internalToken) {
        this.internalToken = internalToken;
    }
}
