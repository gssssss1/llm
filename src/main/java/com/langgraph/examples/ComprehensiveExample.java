package com.langgraph.examples;

import com.langgraph.checkpoint.CheckpointStorage;
import com.langgraph.checkpoint.InMemoryCheckpointStorage;
import com.langgraph.core.StateGraph;
import com.langgraph.execution.ExecutionConfig;
import com.langgraph.execution.ExecutionContext;
import com.langgraph.execution.ExecutionResult;
import com.langgraph.execution.GraphExecutor;
import com.langgraph.node.NodeResult;
import com.langgraph.plugin.GraphPlugin;
import com.langgraph.state.StateRecord;
import com.langgraph.viewer.GraphExecutionViewer;
import com.langgraph.viewer.GraphViewer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComprehensiveExample {
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPREHENSIVE JAVA LANGGRAPH EXAMPLE");
        System.out.println("=".repeat(80) + "\n");
        
        StateGraph<StateRecord> graph = buildOrderProcessingWorkflow();
        
        CheckpointStorage<StateRecord> checkpointStorage = new InMemoryCheckpointStorage<>();
        
        LoggingPlugin loggingPlugin = new LoggingPlugin();
        
        GraphExecutor<StateRecord> executor = new GraphExecutor<>(
            checkpointStorage,
            List.of(loggingPlugin)
        );
        
        System.out.println("Graph Statistics:");
        System.out.println(GraphViewer.getStats(graph));
        System.out.println();
        
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("orderId", "ORD-12345");
        initialData.put("items", List.of("Widget A", "Widget B", "Widget C"));
        initialData.put("totalAmount", 299.99);
        initialData.put("customerTier", "gold");
        
        StateRecord initialState = new StateRecord(initialData);
        
        ExecutionConfig config = new ExecutionConfig(
            true,
            true, 
            Duration.ofMinutes(5),
            100
        );
        
        GraphExecutionViewer<StateRecord> viewer = new GraphExecutionViewer<>(graph, executor);
        
        ExecutionResult<StateRecord> result = viewer.executeAndDisplay(initialState, config).get();
        
        if (result.isSuccessful()) {
            System.out.println("\n✓ Order processing completed successfully!");
            System.out.println("\nOrder Details:");
            System.out.println("  Order ID: " + result.finalState().get("orderId"));
            System.out.println("  Status: " + result.finalState().get("orderStatus"));
            System.out.println("  Discount Applied: " + result.finalState().get("discountApplied") + "%");
            System.out.println("  Final Amount: $" + result.finalState().get("finalAmount"));
            System.out.println("  Payment Method: " + result.finalState().get("paymentMethod"));
            System.out.println("  Tracking Number: " + result.finalState().get("trackingNumber"));
        } else {
            System.out.println("\n✗ Order processing failed!");
            if (result.error().isPresent()) {
                result.error().get().printStackTrace();
            }
        }
        
        executor.shutdown();
    }
    
    private static StateGraph<StateRecord> buildOrderProcessingWorkflow() {
        return StateGraph.<StateRecord>builder()
            .addNode("validate_order", state -> {
                System.out.println("  → Validating order...");
                String orderId = state.get("orderId", String.class);
                
                if (orderId == null || orderId.isEmpty()) {
                    throw new IllegalStateException("Invalid order ID");
                }
                
                return new NodeResult<>(
                    state.withData("validated", true).withData("validationTime", System.currentTimeMillis())
                );
            })
            
            .addNode("check_inventory", state -> {
                System.out.println("  → Checking inventory...");
                
                return new NodeResult<>(
                    state.withData("inventoryAvailable", true).withData("reservationId", "RES-" + System.currentTimeMillis())
                );
            })
            
            .addNode("apply_discount", state -> {
                System.out.println("  → Applying customer discount...");
                String tier = state.get("customerTier", String.class);
                Double amount = state.get("totalAmount", Double.class);
                
                int discount = switch (tier) {
                    case "platinum" -> 20;
                    case "gold" -> 15;
                    case "silver" -> 10;
                    default -> 5;
                };
                
                double finalAmount = amount * (1 - discount / 100.0);
                
                return new NodeResult<>(
                    state.withData("discountApplied", discount)
                         .withData("finalAmount", finalAmount)
                );
            })
            
            .addNode("process_payment", state -> {
                System.out.println("  → Processing payment...");
                Double amount = state.get("finalAmount", Double.class);
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                return new NodeResult<>(
                    state.withData("paymentProcessed", true)
                         .withData("paymentId", "PAY-" + System.currentTimeMillis())
                         .withData("paymentMethod", "credit_card")
                );
            })
            
            .addNode("prepare_shipment", state -> {
                System.out.println("  → Preparing shipment...");
                
                return new NodeResult<>(
                    state.withData("shipmentPrepared", true)
                         .withData("trackingNumber", "TRK-" + System.currentTimeMillis())
                );
            })
            
            .addNode("send_confirmation", state -> {
                System.out.println("  → Sending confirmation email...");
                
                return new NodeResult<>(
                    state.withData("confirmationSent", true)
                         .withData("emailSentAt", System.currentTimeMillis())
                );
            })
            
            .addNode("complete", state -> {
                System.out.println("  → Order processing complete!");
                
                return new NodeResult<>(
                    state.withData("orderStatus", "completed")
                         .withData("completedAt", System.currentTimeMillis())
                );
            })
            
            .addNode("handle_failure", state -> {
                System.out.println("  → Handling order failure...");
                
                return new NodeResult<>(
                    state.withData("orderStatus", "failed")
                         .withData("failureHandled", true)
                );
            })
            
            .setStartNode("validate_order")
            .setEndNode("complete")
            
            .addEdge("validate_order", "check_inventory")
            .addEdge("check_inventory", "apply_discount")
            .addEdge("apply_discount", "process_payment")
            
            .addConditionalEdge("process_payment", state -> {
                Boolean paymentProcessed = state.get("paymentProcessed", Boolean.class);
                return Boolean.TRUE.equals(paymentProcessed) ? "prepare_shipment" : "handle_failure";
            })
            
            .addParallelEdge("prepare_shipment", "send_confirmation", "complete")
            
            .build();
    }
    
    static class LoggingPlugin implements GraphPlugin<StateRecord> {
        private long startTime;
        
        @Override
        public void beforeExecution(ExecutionContext<StateRecord> context) {
            startTime = System.currentTimeMillis();
            System.out.println("\n[PLUGIN] Execution started: " + context.getExecutionId());
            System.out.println("[PLUGIN] Initial state: " + context.getCurrentState().data());
        }
        
        @Override
        public void afterExecution(ExecutionContext<StateRecord> context) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("\n[PLUGIN] Execution finished: " + context.getExecutionId());
            System.out.println("[PLUGIN] Duration: " + duration + "ms");
            System.out.println("[PLUGIN] Nodes executed: " + context.getExecutedNodes());
            System.out.println("[PLUGIN] Steps: " + context.getStepCount());
        }
    }
}
