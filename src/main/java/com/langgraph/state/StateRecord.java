package com.langgraph.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record StateRecord(
    @JsonProperty("id") String id,
    @JsonProperty("timestamp") Instant timestamp,
    @JsonProperty("data") Map<String, Object> data
) implements State {
    
    @JsonCreator
    public StateRecord {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (data == null) {
            data = new HashMap<>();
        } else {
            data = Map.copyOf(data);
        }
    }
    
    public StateRecord() {
        this(null, null, new HashMap<>());
    }
    
    public StateRecord(Map<String, Object> data) {
        this(null, null, data);
    }
    
    public StateRecord withData(String key, Object value) {
        Map<String, Object> newData = new HashMap<>(this.data);
        newData.put(key, value);
        return new StateRecord(this.id, this.timestamp, newData);
    }
    
    public StateRecord withAllData(Map<String, Object> additionalData) {
        Map<String, Object> newData = new HashMap<>(this.data);
        newData.putAll(additionalData);
        return new StateRecord(this.id, this.timestamp, newData);
    }
    
    public StateRecord withNewTimestamp() {
        return new StateRecord(this.id, Instant.now(), this.data);
    }
}
