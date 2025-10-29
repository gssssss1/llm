package com.jsonschema.llm.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class StreamingTest {
    
    @Test
    public void testStreamCallbackInterface() {
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicInteger chunkCount = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        StreamCallback callback = new StreamCallback() {
            @Override
            public void onStart() {
                started.set(true);
            }
            
            @Override
            public void onChunk(String content) {
                chunkCount.incrementAndGet();
            }
            
            @Override
            public void onComplete(String fullContent) {
                completed.set(true);
            }
            
            @Override
            public void onError(Exception error) {
                fail("Should not error");
            }
        };
        
        callback.onStart();
        callback.onChunk("test1");
        callback.onChunk("test2");
        callback.onComplete("test1test2");
        
        assertTrue(started.get());
        assertEquals(2, chunkCount.get());
        assertTrue(completed.get());
    }
    
    @Test
    public void testStreamCallbackErrorHandling() {
        AtomicBoolean errorOccurred = new AtomicBoolean(false);
        
        StreamCallback callback = new StreamCallback() {
            @Override
            public void onStart() {
            }
            
            @Override
            public void onChunk(String content) {
            }
            
            @Override
            public void onComplete(String fullContent) {
            }
            
            @Override
            public void onError(Exception error) {
                errorOccurred.set(true);
                assertNotNull(error);
            }
        };
        
        callback.onError(new RuntimeException("Test error"));
        assertTrue(errorOccurred.get());
    }
    
    @Test
    public void testStreamCallbackWithLatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder fullContent = new StringBuilder();
        
        StreamCallback callback = new StreamCallback() {
            @Override
            public void onStart() {
                System.out.println("Stream started");
            }
            
            @Override
            public void onChunk(String content) {
                fullContent.append(content);
            }
            
            @Override
            public void onComplete(String content) {
                System.out.println("Stream completed");
                latch.countDown();
            }
            
            @Override
            public void onError(Exception error) {
                latch.countDown();
            }
        };
        
        callback.onStart();
        callback.onChunk("Hello ");
        callback.onChunk("World");
        callback.onComplete("Hello World");
        
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        assertTrue(completed);
        assertEquals("Hello World", fullContent.toString());
    }
}
