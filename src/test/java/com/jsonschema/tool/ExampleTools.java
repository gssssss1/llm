package com.jsonschema.tool;

import com.jsonschema.annotations.Tool;
import com.jsonschema.examples.SimpleUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExampleTools {
    
    @Tool(description = "Get current weather information for a specified city")
    public String getWeather(WeatherRequest request) {
        String location = request.getCity();
        if (request.getCountry() != null && !request.getCountry().isEmpty()) {
            location += ", " + request.getCountry();
        }
        
        Random random = new Random();
        int temperature = 15 + random.nextInt(20);
        
        if ("fahrenheit".equals(request.getUnit())) {
            temperature = (temperature * 9 / 5) + 32;
        }
        
        String[] conditions = {"Sunny", "Cloudy", "Rainy", "Partly Cloudy"};
        String condition = conditions[random.nextInt(conditions.length)];
        
        return String.format("Weather in %s: %s, Temperature: %d°%s", 
            location, 
            condition, 
            temperature, 
            "celsius".equals(request.getUnit()) ? "C" : "F");
    }
    
    @Tool(name = "search", description = "Search for information in the database")
    public Map<String, Object> search(SearchRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("query", request.getQuery());
        result.put("limit", request.getLimit());
        result.put("offset", request.getOffset());
        
        List<Map<String, String>> results = new ArrayList<>();
        for (int i = 0; i < Math.min(request.getLimit(), 5); i++) {
            Map<String, String> item = new HashMap<>();
            item.put("id", "result_" + (request.getOffset() + i + 1));
            item.put("title", "Result for: " + request.getQuery());
            item.put("description", "This is a sample result #" + (i + 1));
            results.add(item);
        }
        
        result.put("results", results);
        result.put("total", 42);
        
        return result;
    }
    
    @Tool(description = "Get the current date and time")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Current time: " + now.format(formatter);
    }
    
    @Tool(description = "Create a new user in the system")
    public String createUser(SimpleUser user) {
        return String.format("User created successfully! ID: %s, Name: %s, Email: %s, Age: %d, Active: %s",
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge(),
            user.isActive());
    }
    
    @Tool(name = "calculate", description = "Perform a simple calculation")
    public double calculate(CalculationRequest request) {
        double a = request.getA();
        double b = request.getB();
        
        switch (request.getOperation()) {
            case "add":
                return a + b;
            case "subtract":
                return a - b;
            case "multiply":
                return a * b;
            case "divide":
                if (b == 0) {
                    throw new IllegalArgumentException("Cannot divide by zero");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("Unknown operation: " + request.getOperation());
        }
    }
}
