package com.jsonschema.llm.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jsonschema.llm.client.model.ChatResponse;
import com.jsonschema.llm.message.AssistantMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatResponseTest {
    
    @Test
    public void testParseOpenAIResponse() {
        String jsonResponse = "{" +
            "\"id\":\"chatcmpl-123\"," +
            "\"object\":\"chat.completion\"," +
            "\"created\":1677652288," +
            "\"model\":\"gpt-3.5-turbo\"," +
            "\"choices\":[{" +
            "\"index\":0," +
            "\"message\":{" +
            "\"role\":\"assistant\"," +
            "\"content\":\"Hello! How can I help you?\"" +
            "}," +
            "\"finish_reason\":\"stop\"" +
            "}]," +
            "\"usage\":{" +
            "\"prompt_tokens\":10," +
            "\"completion_tokens\":15," +
            "\"total_tokens\":25" +
            "}}";
        
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        ChatResponse response = ChatResponse.fromOpenAIJson(json);
        
        assertNotNull(response);
        assertEquals("chatcmpl-123", response.getId());
        assertEquals("gpt-3.5-turbo", response.getModel());
        assertEquals(1, response.getChoices().size());
        
        AssistantMessage message = response.getAssistantMessage();
        assertNotNull(message);
        assertEquals("Hello! How can I help you?", message.getContent());
        
        assertEquals(10, response.getUsage().getPromptTokens());
        assertEquals(15, response.getUsage().getCompletionTokens());
        assertEquals(25, response.getUsage().getTotalTokens());
    }
    
    @Test
    public void testParseOpenAIResponseWithToolCalls() {
        String jsonResponse = "{" +
            "\"id\":\"chatcmpl-456\"," +
            "\"object\":\"chat.completion\"," +
            "\"created\":1677652288," +
            "\"model\":\"gpt-4\"," +
            "\"choices\":[{" +
            "\"index\":0," +
            "\"message\":{" +
            "\"role\":\"assistant\"," +
            "\"content\":null," +
            "\"tool_calls\":[{" +
            "\"id\":\"call_abc123\"," +
            "\"type\":\"function\"," +
            "\"function\":{" +
            "\"name\":\"getWeather\"," +
            "\"arguments\":\"{\\\"city\\\":\\\"Paris\\\"}\"" +
            "}}]" +
            "}," +
            "\"finish_reason\":\"tool_calls\"" +
            "}]," +
            "\"usage\":{" +
            "\"prompt_tokens\":20," +
            "\"completion_tokens\":30," +
            "\"total_tokens\":50" +
            "}}";
        
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        ChatResponse response = ChatResponse.fromOpenAIJson(json);
        
        assertNotNull(response);
        assertEquals("chatcmpl-456", response.getId());
        
        AssistantMessage message = response.getAssistantMessage();
        assertTrue(message.hasToolCalls());
        assertEquals(1, message.getToolCalls().size());
        
        assertEquals("call_abc123", message.getToolCalls().get(0).getId());
        assertEquals("getWeather", message.getToolCalls().get(0).getName());
    }
    
    @Test
    public void testParseAnthropicResponse() {
        String jsonResponse = "{" +
            "\"id\":\"msg_123\"," +
            "\"type\":\"message\"," +
            "\"model\":\"claude-3-opus-20240229\"," +
            "\"content\":[{" +
            "\"type\":\"text\"," +
            "\"text\":\"Hello! I'm Claude.\"" +
            "}]," +
            "\"stop_reason\":\"end_turn\"," +
            "\"usage\":{" +
            "\"input_tokens\":10," +
            "\"output_tokens\":20" +
            "}}";
        
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        ChatResponse response = ChatResponse.fromAnthropicJson(json);
        
        assertNotNull(response);
        assertEquals("msg_123", response.getId());
        assertEquals("claude-3-opus-20240229", response.getModel());
        
        AssistantMessage message = response.getAssistantMessage();
        assertNotNull(message);
        assertEquals("Hello! I'm Claude.", message.getContent());
        
        assertEquals("stop", response.getFirstChoice().getFinishReason());
        assertEquals(10, response.getUsage().getPromptTokens());
        assertEquals(20, response.getUsage().getCompletionTokens());
    }
    
    @Test
    public void testParseAnthropicResponseWithToolUse() {
        String jsonResponse = "{" +
            "\"id\":\"msg_456\"," +
            "\"type\":\"message\"," +
            "\"model\":\"claude-3-opus-20240229\"," +
            "\"content\":[{" +
            "\"type\":\"tool_use\"," +
            "\"id\":\"toolu_abc123\"," +
            "\"name\":\"getWeather\"," +
            "\"input\":\"{\\\"city\\\":\\\"Tokyo\\\"}\"" +
            "}]," +
            "\"stop_reason\":\"tool_use\"," +
            "\"usage\":{" +
            "\"input_tokens\":15," +
            "\"output_tokens\":25" +
            "}}";
        
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        ChatResponse response = ChatResponse.fromAnthropicJson(json);
        
        assertNotNull(response);
        
        AssistantMessage message = response.getAssistantMessage();
        assertTrue(message.hasToolCalls());
        assertEquals(1, message.getToolCalls().size());
        
        assertEquals("toolu_abc123", message.getToolCalls().get(0).getId());
        assertEquals("getWeather", message.getToolCalls().get(0).getName());
        
        assertEquals("tool_calls", response.getFirstChoice().getFinishReason());
    }
}
