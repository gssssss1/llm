package com.jsonschema.examples;

import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaProperty;

@SchemaDescription("Physical address information")
public class Address {
    
    @SchemaProperty(required = true)
    @SchemaDescription("Street address")
    private String street;
    
    @SchemaProperty(required = true)
    @SchemaDescription("City name")
    private String city;
    
    @SchemaDescription("State or province")
    private String state;
    
    @SchemaProperty(pattern = "^\\d{5}(-\\d{4})?$")
    @SchemaDescription("Postal code")
    private String zipCode;
    
    @SchemaProperty(required = true)
    @SchemaDescription("Country name")
    private String country;
    
    public Address() {}
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
}
