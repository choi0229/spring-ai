package com.sparta.demo4.service;

import com.sparta.demo4.controller.dto.ChatResponseV2;
import com.sparta.demo4.controller.dto.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.naming.LimitExceededException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeChatService implements IChatService {

    private final ChatClient chatClient;  // application.yml -> spring.ai.claude -> AutoConfiguration
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    @Override
    public ChatResponseV2 chat(String question) {
        String response = prompt(question);
        return ChatResponseV2.of(response, UUID.randomUUID().toString());
    }

    @Override
    public ChatResponseV2 chatWithHistory(String question, String conversationId) throws LimitExceededException {
        if(conversationId == null || conversationId.isBlank()){
            conversationId = UUID.randomUUID().toString();
        }

        List<Message> history = conversations.getOrDefault(conversationId, new ArrayList<>());

        UserMessage userMessage = new UserMessage(question);

        org.springframework.ai.chat.model.ChatResponse response = promptWithHistory(question, history);

        String assistantResponse = response.getResult().getOutput().getText();
        if(assistantResponse == null || assistantResponse.isBlank()){
            throw new IllegalStateException("assistant response is null or blank");
        }

        AssistantMessage assistantMessage = new AssistantMessage(assistantResponse);
        history.add(userMessage);
        history.add(assistantMessage);
        conversations.put(conversationId, history);

        var metadata = response.getMetadata();
        TokenUsage tokenUsage = null;
        if(metadata != null && metadata.getUsage() != null){
            var usage = metadata.getUsage();
            tokenUsage = new TokenUsage(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        }
        return ChatResponseV2.of(assistantResponse, conversationId, tokenUsage);
    }

    @Override
    public Flux<String> chatStream(String question){
        return promptStream(question);
    }

    @Override
    public List<Message> getConversationHistory(String conversationId){
        return conversations.getOrDefault(conversationId, Collections.emptyList());
    }

    @Override
    public void clearConversationBy(String conversationId){
        conversations.remove(conversationId);
    }

    @Override
    public void clearAllConversations(){
        conversations.clear();
    }

    private String prompt(String question){
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    private org.springframework.ai.chat.model.ChatResponse promptWithHistory(String question, List<Message> history)throws LimitExceededException{
        try{
            return chatClient.prompt()
                    .messages(history)
                    .user(question)
                    .call()
                    .chatResponse();
        }catch(Exception e){
            throw new LimitExceededException(e.getMessage());
        }
    }

    private Flux<String> promptStream(String question){
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }

}
