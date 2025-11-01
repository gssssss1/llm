package com.langgraph.web;

import com.langgraph.LangGraphWebApplication;
import com.langgraph.core.StateGraph;
import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;
import com.langgraph.web.service.GraphService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.langgraph")
public class WebUIDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(WebUIDemo.class, args);
    }
    
    @Bean
    CommandLineRunner initGraphs(GraphService graphService) {
        return args -> {
            createSimpleWorkflow(graphService);
            createConditionalWorkflow(graphService);
            createParallelWorkflow(graphService);
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("LangGraph Web UI Started Successfully!");
            System.out.println("=".repeat(80));
            System.out.println("\nAccess the Web UI at: http://localhost:8080");
            System.out.println("\nAvailable Graphs:");
            graphService.getAllGraphs().keySet().forEach(id -> 
                System.out.println("  - " + id + ": http://localhost:8080/graph/" + id));
            System.out.println("\n" + "=".repeat(80) + "\n");
        };
    }
    
    private void createSimpleWorkflow(GraphService graphService) {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> {
                System.out.println("[DEMO] Starting simple workflow...");
                try { Thread.sleep(500); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("step", "started").withData("count", 0),
                    "process"
                );
            })
            .addNode("process", state -> {
                System.out.println("[DEMO] Processing data...");
                try { Thread.sleep(500); } catch (InterruptedException e) { }
                int count = state.get("count", Integer.class);
                return new NodeResult<>(
                    state.withData("step", "processed").withData("count", count + 1),
                    "decision"
                );
            })
            .addNode("decision", state -> {
                System.out.println("[DEMO] Making decision...");
                try { Thread.sleep(300); } catch (InterruptedException e) { }
                int count = state.get("count", Integer.class);
                String next = count < 3 ? "process" : "end";
                return new NodeResult<>(
                    state.withData("step", "decided"),
                    next
                );
            })
            .addNode("end", state -> {
                System.out.println("[DEMO] Workflow complete!");
                return new NodeResult<>(
                    state.withData("step", "completed")
                );
            })
            .setStartNode("start")
            .setEndNode("end")
            .build();
        
        graphService.registerGraph("simple-workflow", graph);
    }
    
    private void createConditionalWorkflow(GraphService graphService) {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("analyze", state -> {
                System.out.println("[DEMO] Analyzing...");
                try { Thread.sleep(500); } catch (InterruptedException e) { }
                int value = (int) (Math.random() * 100);
                return new NodeResult<>(
                    state.withData("value", value).withData("analyzed", true)
                );
            })
            .addNode("high_value", state -> {
                System.out.println("[DEMO] Processing high value...");
                try { Thread.sleep(400); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("result", "high_processed"),
                    "finalize"
                );
            })
            .addNode("low_value", state -> {
                System.out.println("[DEMO] Processing low value...");
                try { Thread.sleep(400); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("result", "low_processed"),
                    "finalize"
                );
            })
            .addNode("finalize", state -> {
                System.out.println("[DEMO] Finalizing...");
                try { Thread.sleep(300); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("status", "completed")
                );
            })
            .setStartNode("analyze")
            .setEndNode("finalize")
            .addConditionalEdge("analyze", state -> {
                Integer value = state.get("value", Integer.class);
                return value > 50 ? "high_value" : "low_value";
            })
            .build();
        
        graphService.registerGraph("conditional-workflow", graph);
    }
    
    private void createParallelWorkflow(GraphService graphService) {
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("prepare", state -> {
                System.out.println("[DEMO] Preparing for parallel execution...");
                try { Thread.sleep(300); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("prepared", true)
                );
            })
            .addNode("task_a", state -> {
                System.out.println("[DEMO] Executing Task A...");
                try { Thread.sleep(600); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("task_a", "completed"),
                    "merge"
                );
            })
            .addNode("task_b", state -> {
                System.out.println("[DEMO] Executing Task B...");
                try { Thread.sleep(700); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("task_b", "completed"),
                    "merge"
                );
            })
            .addNode("task_c", state -> {
                System.out.println("[DEMO] Executing Task C...");
                try { Thread.sleep(500); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("task_c", "completed"),
                    "merge"
                );
            })
            .addNode("merge", state -> {
                System.out.println("[DEMO] Merging results...");
                try { Thread.sleep(300); } catch (InterruptedException e) { }
                return new NodeResult<>(
                    state.withData("merged", true).withData("status", "all_complete")
                );
            })
            .setStartNode("prepare")
            .setEndNode("merge")
            .addParallelEdge("prepare", "task_a", "task_b", "task_c")
            .build();
        
        graphService.registerGraph("parallel-workflow", graph);
    }
}
