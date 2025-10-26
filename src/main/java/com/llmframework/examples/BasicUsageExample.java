package com.llmframework.examples;

import com.llmframework.chat.ChatInput;
import com.llmframework.chat.ChatModel;
import com.llmframework.core.message.Message;
import com.llmframework.core.model.ModelOptions;

import java.util.List;

/**
 * Basic usage examples for the LLM Framework
 * 
 * Note: These examples require actual API keys and model implementations.
 * This is a demonstration of the API design.
 */
public class BasicUsageExample {
    
    public static void main(String[] args) {
        // Example 1: Simple chat
        // ChatModel chatModel = createChatModel();
        // simpleChat(chatModel);
        
        // Example 2: Multi-turn conversation
        // multiTurnChat(chatModel);
        
        // Example 3: Streaming
        // streamingChat(chatModel);
        
        // Example 4: Structured output
        // structuredOutput(chatModel);
        
        System.out.println("Examples are ready to run with a real model implementation.");
    }
    
    // Uncomment when OpenAI adapter is implemented
    /*
    private static ChatModel createChatModel() {
        return OpenAIChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-4")
            .build();
    }
    */
    
    private static void simpleChat(ChatModel chatModel) {
        String response = chatModel.chat("What is the capital of France?").block();
        System.out.println("Response: " + response);
    }
    
    private static void multiTurnChat(ChatModel chatModel) {
        List<Message> messages = List.of(
            Message.system("You are a helpful assistant."),
            Message.user("What is Java?"),
            Message.assistant("Java is a programming language..."),
            Message.user("When was it created?")
        );
        
        ChatInput input = ChatInput.builder()
            .messages(messages)
            .options(opts -> opts
                .temperature(0.7)
                .maxTokens(500))
            .build();
        
        chatModel.call(input)
            .subscribe(
                output -> System.out.println("Response: " + output.content()),
                error -> System.err.println("Error: " + error.getMessage())
            );
    }
    
    private static void streamingChat(ChatModel chatModel) {
        ChatInput input = ChatInput.of("Write a short poem about programming");
        
        chatModel.stream(input)
            .subscribe(
                chunk -> System.out.print(chunk.output().content()),
                error -> System.err.println("\nError: " + error.getMessage()),
                () -> System.out.println("\n[Streaming complete]")
            );
    }
    
    private static void structuredOutput(ChatModel chatModel) {
        record Person(String name, int age, String occupation) {}
        
        chatModel.chatStructured(
            "Extract information: Alice is a 28-year-old data scientist",
            Person.class
        ).subscribe(
            person -> System.out.println("Parsed: " + person),
            error -> System.err.println("Error: " + error.getMessage())
        );
    }
}
