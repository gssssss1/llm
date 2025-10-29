package com.jsonschema.llm.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jsonschema.llm.message.AssistantMessage;
import com.jsonschema.llm.message.ToolCall;

import java.util.ArrayList;
import java.util.List;

public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    
    public ChatResponse() {
        this.choices = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public long getCreated() {
        return created;
    }
    
    public void setCreated(long created) {
        this.created = created;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<Choice> getChoices() {
        return choices;
    }
    
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
    
    public Usage getUsage() {
        return usage;
    }
    
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
    
    public Choice getFirstChoice() {
        return choices.isEmpty() ? null : choices.get(0);
    }
    
    public AssistantMessage getAssistantMessage() {
        Choice choice = getFirstChoice();
        if (choice == null) {
            return null;
        }
        return choice.getMessage();
    }
    
    public static ChatResponse fromOpenAIJson(JsonObject json) {
        ChatResponse response = new ChatResponse();
        
        if (json.has("id")) {
            response.setId(json.get("id").getAsString());
        }
        
        if (json.has("object")) {
            response.setObject(json.get("object").getAsString());
        }
        
        if (json.has("created")) {
            response.setCreated(json.get("created").getAsLong());
        }
        
        if (json.has("model")) {
            response.setModel(json.get("model").getAsString());
        }
        
        if (json.has("choices")) {
            JsonArray choicesArray = json.getAsJsonArray("choices");
            for (int i = 0; i < choicesArray.size(); i++) {
                JsonObject choiceJson = choicesArray.get(i).getAsJsonObject();
                Choice choice = Choice.fromOpenAIJson(choiceJson);
                response.choices.add(choice);
            }
        }
        
        if (json.has("usage")) {
            response.setUsage(Usage.fromJson(json.getAsJsonObject("usage")));
        }
        
        return response;
    }
    
    public static ChatResponse fromAnthropicJson(JsonObject json) {
        ChatResponse response = new ChatResponse();
        
        if (json.has("id")) {
            response.setId(json.get("id").getAsString());
        }
        
        if (json.has("type")) {
            response.setObject(json.get("type").getAsString());
        }
        
        if (json.has("model")) {
            response.setModel(json.get("model").getAsString());
        }
        
        Choice choice = new Choice();
        choice.setIndex(0);
        
        if (json.has("stop_reason")) {
            String stopReason = json.get("stop_reason").getAsString();
            if ("end_turn".equals(stopReason)) {
                choice.setFinishReason("stop");
            } else if ("tool_use".equals(stopReason)) {
                choice.setFinishReason("tool_calls");
            } else {
                choice.setFinishReason(stopReason);
            }
        }
        
        AssistantMessage message = new AssistantMessage();
        
        if (json.has("content")) {
            JsonArray contentArray = json.getAsJsonArray("content");
            for (int i = 0; i < contentArray.size(); i++) {
                JsonObject contentItem = contentArray.get(i).getAsJsonObject();
                String type = contentItem.get("type").getAsString();
                
                if ("text".equals(type)) {
                    message.setContent(contentItem.get("text").getAsString());
                } else if ("tool_use".equals(type)) {
                    String id = contentItem.get("id").getAsString();
                    String name = contentItem.get("name").getAsString();
                    String input = contentItem.get("input").toString();
                    message.addToolCall(new ToolCall(id, name, input));
                }
            }
        }
        
        choice.setMessage(message);
        response.choices.add(choice);
        
        if (json.has("usage")) {
            response.setUsage(Usage.fromJson(json.getAsJsonObject("usage")));
        }
        
        return response;
    }
    
    public static class Choice {
        private int index;
        private AssistantMessage message;
        private String finishReason;
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public AssistantMessage getMessage() {
            return message;
        }
        
        public void setMessage(AssistantMessage message) {
            this.message = message;
        }
        
        public String getFinishReason() {
            return finishReason;
        }
        
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
        
        public static Choice fromOpenAIJson(JsonObject json) {
            Choice choice = new Choice();
            
            if (json.has("index")) {
                choice.setIndex(json.get("index").getAsInt());
            }
            
            if (json.has("finish_reason") && !json.get("finish_reason").isJsonNull()) {
                choice.setFinishReason(json.get("finish_reason").getAsString());
            }
            
            if (json.has("message")) {
                JsonObject messageJson = json.getAsJsonObject("message");
                AssistantMessage message = new AssistantMessage();
                
                if (messageJson.has("content") && !messageJson.get("content").isJsonNull()) {
                    message.setContent(messageJson.get("content").getAsString());
                }
                
                if (messageJson.has("tool_calls")) {
                    JsonArray toolCallsArray = messageJson.getAsJsonArray("tool_calls");
                    for (int i = 0; i < toolCallsArray.size(); i++) {
                        JsonObject toolCallJson = toolCallsArray.get(i).getAsJsonObject();
                        String id = toolCallJson.get("id").getAsString();
                        JsonObject functionJson = toolCallJson.getAsJsonObject("function");
                        String name = functionJson.get("name").getAsString();
                        String arguments = functionJson.get("arguments").getAsString();
                        message.addToolCall(new ToolCall(id, name, arguments));
                    }
                }
                
                choice.setMessage(message);
            }
            
            return choice;
        }
    }
    
    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        
        public int getPromptTokens() {
            return promptTokens;
        }
        
        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }
        
        public int getCompletionTokens() {
            return completionTokens;
        }
        
        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }
        
        public int getTotalTokens() {
            return totalTokens;
        }
        
        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
        
        public static Usage fromJson(JsonObject json) {
            Usage usage = new Usage();
            
            if (json.has("prompt_tokens")) {
                usage.setPromptTokens(json.get("prompt_tokens").getAsInt());
            } else if (json.has("input_tokens")) {
                usage.setPromptTokens(json.get("input_tokens").getAsInt());
            }
            
            if (json.has("completion_tokens")) {
                usage.setCompletionTokens(json.get("completion_tokens").getAsInt());
            } else if (json.has("output_tokens")) {
                usage.setCompletionTokens(json.get("output_tokens").getAsInt());
            }
            
            if (json.has("total_tokens")) {
                usage.setTotalTokens(json.get("total_tokens").getAsInt());
            } else {
                usage.setTotalTokens(usage.getPromptTokens() + usage.getCompletionTokens());
            }
            
            return usage;
        }
        
        @Override
        public String toString() {
            return "Usage{" +
                    "promptTokens=" + promptTokens +
                    ", completionTokens=" + completionTokens +
                    ", totalTokens=" + totalTokens +
                    '}';
        }
    }
}
