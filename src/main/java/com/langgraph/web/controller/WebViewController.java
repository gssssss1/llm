package com.langgraph.web.controller;

import com.langgraph.web.service.GraphService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebViewController {
    
    private final GraphService graphService;
    
    public WebViewController(GraphService graphService) {
        this.graphService = graphService;
    }
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("graphs", graphService.getAllGraphs().keySet());
        return "index";
    }
    
    @GetMapping("/graph/{id}")
    public String graphView(@PathVariable String id, Model model) {
        model.addAttribute("graphId", id);
        return "graph-view";
    }
    
    @GetMapping("/executions")
    public String executionsView() {
        return "executions";
    }
}
