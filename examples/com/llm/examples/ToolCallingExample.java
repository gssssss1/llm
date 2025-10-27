package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.core.tool.Tool;
import com.llm.core.tool.ToolRegistry;
import com.llm.core.tool.ToolResult;
import com.llm.provider.ProviderType;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates registering and using a custom tool.
 */
public class ToolCallingExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();

        ToolRegistry registry = new ToolRegistry();
        registry.register(new WeatherTool());

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .systemPrompt("You can call tools to fetch information.")
                .build()) {

            session.send(UserMessage.of("What's the weather in Tokyo tomorrow?"));
        }
    }

    /**
     * Simple weather lookup tool returning predefined responses.
     */
    static class WeatherTool implements Tool {

        @Override
        public String getName() {
            return "weather_lookup";
        }

        @Override
        public String getDescription() {
            return "Looks up the weather for a given city";
        }

        @Override
        public Map<String, Object> getParameterSchema() {
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");
            schema.put("properties", Map.of("city", Map.of("type", "string")));
            return schema;
        }

        @Override
        public ToolResult execute(Map<String, Object> arguments) {
            String city = (String) arguments.get("city");
            return ToolResult.builder()
                    .content("The weather in " + city + " tomorrow is sunny with 25°C.")
                    .build();
        }
    }
}
