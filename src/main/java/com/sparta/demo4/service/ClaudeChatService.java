package com.sparta.demo4.service;

import com.sparta.demo4.controller.dto.ChatResponseV2;
import com.sparta.demo4.controller.dto.TokenUsage;
import jakarta.annotation.PostConstruct;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeChatService implements IChatService {

    private ChatClient chatClient;  // application.yml -> spring.ai.claude -> AutoConfiguration
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
    //private final Set<ChatClient> chatClients; // Set<ChatClient>로 해주면 Spring이 자동으로 ChatClient 타입으로 되어있는 Bean을 Set에 주입해줌
    private final Map<String, ChatClient> chatClients;
    private Map<String, ChatClient> chatClientMap = new HashMap<>();

    @PostConstruct
    public void init(){
        chatClientMap = chatClients.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().replace("ChatClient", ""),
                        Map.Entry::getValue
                ));
        log.info("Initialized chat clients: {}", chatClientMap.keySet());
    }
//
//    @PostConstruct
//    public void init() {
//        for(ChatClient chatClient : chatClients){
//            String modelName = chatClient.getClass().getSimpleName();
//            chatClientMap.put(modelName, chatClient);
//            }
//    }

    @Override
    public ChatResponseV2 chat(String question, String modelName) {
        ChatClient chatClient = chatClientMap.get(modelName);
        String response = prompt(question, chatClient);
        return ChatResponseV2.of(response, UUID.randomUUID().toString(), modelName);
    }

    @Override
    public ChatResponseV2 chatWithHistory(String question, String conversationId, String modelName) throws LimitExceededException {
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
        return ChatResponseV2.of(assistantResponse, conversationId, tokenUsage, modelName);
    }

    @Override
    public Flux<String> chatStream(String question, String modelName){
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

    private String prompt(String question, ChatClient chatClient){
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
