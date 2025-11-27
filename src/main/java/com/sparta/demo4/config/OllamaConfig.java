package com.sparta.demo4.config;

import com.sparta.demo4.aop.advisor.AdvancedRagAdvisor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.ai.ollama")
@Setter
public class OllamaConfig {

    private String baseUrl;

    /**
     * OllamaApi 생성 (Builder 패턴)
     * 1.1.0-M2에서는 Builder를 사용해야 함
     */
    @Bean
    public OllamaApi ollamaApi() {
        log.info("=== OllamaApi 생성 ===");
        log.info("Base URL: {}", baseUrl);

        // ✅ Builder 패턴 사용
        return OllamaApi.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * OllamaChatModel 생성
     */
    // @Primary
    @Bean(name = "ollamaChatModel")
    public OllamaChatModel ollamaChatModel(
            OllamaApi ollamaApi) {
        log.info("=== OllamaChatModel 생성 ===");

        // 기본 옵션 설정
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("qwen2.5:3b")              // 모델 이름
                .temperature(0.7)                 // 창의성 (0.0 ~ 1.0)
                .numPredict(1000)                 // 최대 생성 토큰 수
                .topK(40)                         // Top-K 샘플링
                .topP(0.9)                        // Top-P (nucleus) 샘플링
                .repeatPenalty(1.1)               // 반복 방지
                .build();


        log.info("Ollama 모델: {}", options.getModel());
        log.info("Temperature: {}", options.getTemperature());

        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * Ollama ChatClient 생성
     */
//    @Primary
//    @Bean(name = "ollamaChatClient")
//    public ChatClient ollamaChatClient(ChatClient.Builder chatClientBuilder) {
//        return chatClientBuilder
//                .defaultSystem("""
//                        당신은 친절하고 전문적인 AI 어시스턴트입니다.
//
//                        다음 원칙을 따라 응답해주세요:
//                        1. 정확하고 사실에 기반한 정보를 제공합니다
//                        2. 명확하고 이해하기 쉬운 언어를 사용합니다
//                        3. 필요한 경우 단계별로 설명합니다
//                        4. 불확실한 정보는 명시적으로 표시합니다
//                        5. 사용자의 질문 의도를 정확히 파악하여 답변합니다
//
//                        한국어로 답변하며, 존댓말을 사용합니다.
//                        """)
//                .build();
//    }

    // ✅ ollamaChatModel을 명시적으로 주입
    // @Primary  // 기본 ChatClient로 사용
    @Bean(name = "ollamaChatClient")
    public ChatClient ollamaChatClient(
            @Qualifier("ollamaChatModel") OllamaChatModel chatModel,
            VectorStore vectorStore) {

        RagConfig config = RagConfig.builder()
                .topK(10)
                .similarityThreshold(0.75)
                .requireDocuments(false)
                .appendSources(true)
                .build();

        return ChatClient.builder(chatModel)  // 특정 모델 지정
                .defaultAdvisors(
                        new AdvancedRagAdvisor(vectorStore, config)
                )
                .defaultSystem("""
                        당신은 친절하고 전문적인 AI 어시스턴트입니다.
                        
                        다음 원칙을 따라 응답해주세요:
                        1. 정확하고 사실에 기반한 정보를 제공합니다
                        2. 명확하고 이해하기 쉬운 언어를 사용합니다
                        3. 필요한 경우 단계별로 설명합니다
                        4. 불확실한 정보는 명시적으로 표시합니다
                        5. 사용자의 질문 의도를 정확히 파악하여 답변합니다
                        
                        한국어로 답변하며, 존댓말을 사용합니다.
                        """)
                .build();
    }
}