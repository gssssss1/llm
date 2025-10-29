package com.jsonschema.tool;

import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaProperty;

@SchemaDescription("Weather query parameters")
public class WeatherRequest {
    
    @SchemaProperty(required = true)
    @SchemaDescription("The city name to get weather for")
    private String city;
    
    @SchemaDescription("Country code (optional)")
    private String country;
    
    @SchemaProperty(enumValues = {"celsius", "fahrenheit"})
    @SchemaDescription("Temperature unit")
    private String unit = "celsius";
    
    public WeatherRequest() {}
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
