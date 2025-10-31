package com.langgraph.checkpoint;

import com.langgraph.state.StateRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointStorageTest {
    
    private CheckpointStorage<StateRecord> storage;
    
    @BeforeEach
    void setUp() {
        storage = new InMemoryCheckpointStorage<>();
    }
    
    @Test
    void testSaveAndLoad() {
        StateRecord state = new StateRecord().withData("key", "value");
        Checkpoint<StateRecord> checkpoint = new Checkpoint<>("exec1", state, "node1");
        
        storage.save(checkpoint);
        
        Optional<Checkpoint<StateRecord>> loaded = storage.load(checkpoint.id());
        assertTrue(loaded.isPresent());
        assertEquals(checkpoint.id(), loaded.get().id());
        assertEquals("exec1", loaded.get().executionId());
        assertEquals("node1", loaded.get().currentNode());
    }
    
    @Test
    void testLoadNonExistent() {
        Optional<Checkpoint<StateRecord>> loaded = storage.load("nonexistent");
        assertFalse(loaded.isPresent());
    }
    
    @Test
    void testLoadByExecution() {
        StateRecord state = new StateRecord();
        Checkpoint<StateRecord> cp1 = new Checkpoint<>("exec1", state, "node1");
        Checkpoint<StateRecord> cp2 = new Checkpoint<>("exec1", state, "node2");
        Checkpoint<StateRecord> cp3 = new Checkpoint<>("exec2", state, "node1");
        
        storage.save(cp1);
        storage.save(cp2);
        storage.save(cp3);
        
        List<Checkpoint<StateRecord>> checkpoints = storage.loadByExecution("exec1");
        assertEquals(2, checkpoints.size());
        
        List<Checkpoint<StateRecord>> checkpoints2 = storage.loadByExecution("exec2");
        assertEquals(1, checkpoints2.size());
    }
    
    @Test
    void testLoadLatest() {
        StateRecord state = new StateRecord();
        Checkpoint<StateRecord> cp1 = new Checkpoint<>("exec1", state, "node1");
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Checkpoint<StateRecord> cp2 = new Checkpoint<>("exec1", state, "node2");
        
        storage.save(cp1);
        storage.save(cp2);
        
        Optional<Checkpoint<StateRecord>> latest = storage.loadLatest("exec1");
        assertTrue(latest.isPresent());
        assertEquals("node2", latest.get().currentNode());
    }
    
    @Test
    void testDelete() {
        StateRecord state = new StateRecord();
        Checkpoint<StateRecord> checkpoint = new Checkpoint<>("exec1", state, "node1");
        
        storage.save(checkpoint);
        assertTrue(storage.load(checkpoint.id()).isPresent());
        
        storage.delete(checkpoint.id());
        assertFalse(storage.load(checkpoint.id()).isPresent());
    }
    
    @Test
    void testDeleteByExecution() {
        StateRecord state = new StateRecord();
        Checkpoint<StateRecord> cp1 = new Checkpoint<>("exec1", state, "node1");
        Checkpoint<StateRecord> cp2 = new Checkpoint<>("exec1", state, "node2");
        Checkpoint<StateRecord> cp3 = new Checkpoint<>("exec2", state, "node1");
        
        storage.save(cp1);
        storage.save(cp2);
        storage.save(cp3);
        
        storage.deleteByExecution("exec1");
        
        assertEquals(0, storage.loadByExecution("exec1").size());
        assertEquals(1, storage.loadByExecution("exec2").size());
    }
}
