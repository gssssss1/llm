package com.langgraph.state;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StateRecordTest {
    
    @Test
    void testCreateEmptyState() {
        StateRecord state = new StateRecord();
        assertNotNull(state.id());
        assertNotNull(state.timestamp());
        assertNotNull(state.data());
        assertTrue(state.data().isEmpty());
    }
    
    @Test
    void testCreateStateWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", 42);
        
        StateRecord state = new StateRecord(data);
        assertEquals("value1", state.get("key1"));
        assertEquals(42, state.get("key2"));
    }
    
    @Test
    void testWithData() {
        StateRecord state = new StateRecord();
        StateRecord updated = state.withData("key", "value");
        
        assertNull(state.get("key"));
        assertEquals("value", updated.get("key"));
        assertEquals(state.id(), updated.id());
    }
    
    @Test
    void testWithAllData() {
        StateRecord state = new StateRecord();
        Map<String, Object> newData = Map.of("key1", "value1", "key2", "value2");
        
        StateRecord updated = state.withAllData(newData);
        
        assertEquals("value1", updated.get("key1"));
        assertEquals("value2", updated.get("key2"));
    }
    
    @Test
    void testImmutability() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        
        StateRecord state = new StateRecord(data);
        data.put("key2", "value2");
        
        assertNull(state.get("key2"));
    }
    
    @Test
    void testGetWithType() {
        StateRecord state = new StateRecord()
            .withData("string", "test")
            .withData("number", 42);
        
        assertEquals("test", state.get("string", String.class));
        assertEquals(42, state.get("number", Integer.class));
    }
}
