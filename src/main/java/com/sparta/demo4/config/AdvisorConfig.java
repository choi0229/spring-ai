package com.sparta.demo4.config;

import com.sparta.demo4.aop.advisor.McpPromptAdvisor;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AdvisorConfig {
    @Bean
    public McpPromptAdvisor mcpPromptAdvisor(List<McpSyncClient> mcpClients) {
        return McpPromptAdvisor.builder()  // 일반 클래스의 인스턴스 생성
                .mcpClients(mcpClients)
                .enableCache(true)
                .cacheTtl(10 * 60 * 1000L)
                .order(0)
                .build();  // 이 반환값이 Spring Bean으로 등록됨
    }

    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

}
