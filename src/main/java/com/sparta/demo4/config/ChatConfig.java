package com.sparta.demo4.config;

import com.sparta.demo4.aop.advisor.AdvancedRagAdvisor;
import com.sparta.demo4.aop.advisor.McpPromptAdvisor;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.anthropic")
@Slf4j
@Setter
public class ChatConfig {
    private String apiKey;

    @PostConstruct
    public void init() {
        log.debug("ClaudeChatService init");
        log.debug("Claude apiKey: " + apiKey);
    }

    @Bean
    public AnthropicApi anthropicApi() {
        AnthropicApi.Builder builder = new AnthropicApi.Builder();
        builder.apiKey(apiKey);
        return builder.build();
    }

    @Primary
    @Bean(name = "claudeChatModel")
    public AnthropicChatModel anthropicChatModel(AnthropicApi anthropicApi) {
        log.info("=== AnthropicChatModel 생성 ===");
        String modelName = "claude-sonnet-4-20250514";
        return AnthropicChatModel.builder()
                .anthropicApi(anthropicApi)
                .defaultOptions(
                        AnthropicChatOptions.builder()
                                .model(modelName)
                                .temperature(0.5)
                                .maxTokens(2048)
                                .build())
                .build();
    }

//    @Bean
//    public ChatClient anthropicChatClient(
//            ChatClient.Builder chatClientBuilder) {
//
//        return chatClientBuilder
//                .defaultSystem("""
//                당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
//                사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
//                """)
//                .build();
//    }
//
    @Primary
    @Bean(name = "anthropicChatClient")
    public ChatClient anthropicChatClient(
        @Qualifier("claudeChatModel") AnthropicChatModel chatModel,
        VectorStore vectorStore) {

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .temperature(0.7)
                .maxTokens(4096)
                .toolCallbacks(List.of())
                .build();

        RagConfig config = RagConfig.builder()
                .topK(10)
                .similarityThreshold(0.75)
                .requireDocuments(false)
                .appendSources(true)
                .build();

        return ChatClient.builder(chatModel)  // 특정 모델 지정
                .defaultAdvisors(
                        new AdvancedRagAdvisor(vectorStore, config),
                        new SimpleLoggerAdvisor()
                )
                .defaultOptions(options)
                .defaultSystem("""
                당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
                사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
                """)
            .build();
    }

    @Bean(name = "claudeWithMcpToolsChatClient")
    public ChatClient anthropicWithMcpToolsChatClient(
            ChatClient.Builder chatClientBuilder,
            List<McpSyncClient> mcpClients,
            McpPromptAdvisor mcpPromptAdvisor,
            SimpleLoggerAdvisor simpleLoggerAdvisor) {

        // MCP Client들로부터 Tools를 가져와서 ChatClient에 등록
        return chatClientBuilder
                .defaultSystem("""
                        당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
                        사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
                        """)
                .defaultAdvisors(
                        mcpPromptAdvisor,
                        simpleLoggerAdvisor
                )
                .defaultToolCallbacks(
                        SyncMcpToolCallbackProvider.builder()
                                .mcpClients(mcpClients)
                                .build())
                .build();
    }

}