package com.sparta.demo4.aop.advisor;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class McpPromptAdvisor implements BaseAdvisor {

    private final List<McpSyncClient> mcpClients;
    private final Map<String, PromptCache> promptCache;
    private final boolean enableCache;
    private final int order;

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * PromptCache - 프롬프트 캐싱을 위한 내부 클래스
     */
    private static class PromptCache {
        final String content;
        final long timestamp;
        final long ttlMillis;
        PromptCache(String content, long ttlMillis) {
            this.content = content;
            this.timestamp = System.currentTimeMillis();
            this.ttlMillis = ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() -timestamp > ttlMillis;
        }
    }

    /**
     * 기본 생성자 (캐시 활성화, TTL 5분)
     */
    public McpPromptAdvisor(List<McpSyncClient> mcpClients, boolean enableCache, long cacheTtlMillis, int order) {
        this.mcpClients = mcpClients != null ? mcpClients : Collections.emptyList();
        this.enableCache = enableCache;
        this.promptCache = enableCache ? new HashMap<>() : null;
        this.order = order;

        log.info("McpPromptAdvisor initialized with {} MCP clients, cache: {}, order: {}",
                mcpClients.size(), enableCache, order);

        // 초기화 시 사용 가능한 프롬프트 목록 로깅
        listAvailablePrompts();
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        // 1. 사용자 메시지에서 프롬프트 태그 추출
        String userMessage = request.prompt().getUserMessage().getText();
        PromptInstruction instruction = extractPromptInstruction(userMessage);

        if(instruction == null) {
            // 프롬프트 태그가 없으면 원본 요청 그대로 반환
            return request;
        }

        log.debug("Detected MCP prompt instruction: {}", instruction);

        try{
            // 2. MCP 서버에서 프롬프트 가져오기
            String promptTemplate = getPromptFromMcp(instruction.promptName, instruction.arguments);

            if(promptTemplate == null) {
                log.warn("Prompt not found: {}", instruction.promptName);
                return request;
            }

            // 3. 프롬프트 적용
            String enhancedMessage = applyPrompt(promptTemplate, instruction.userContent, instruction.arguments);

            // 4. 수정된 요청으로 ChatClientRequest 재생성
            log.debug("Applied MCP prompt '{}', original length: {}, enhanced length: {}",
                    instruction.promptName, userMessage.length(), enhancedMessage.length());

            return request.mutate()
                    .prompt(request.prompt().augmentUserMessage(enhancedMessage))
                    .context("original_user_message", userMessage)
                    .context("applied_prompt", instruction.promptName)
                    .build();

        } catch(Exception e) {
            log.error("Error applying MCP prompt: {}", instruction.promptName, e);
            return request;
        }

    }

    /**
     * BaseAdvisor의 after 메소드 구현
     * 응답 후에 실행되지만, 이 Advisor에서는 응답을 수정하지 않습니다.
     */
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        // 응답은 수정하지 않고 그대로 반환
        return response;
    }

    /**
     * Tag Parser: [PROMPT:name] 추출
     * 사용자 메시지에서 프롬프트 지시사항 추출
     *
     * 지원 형식:
     * 1. [PROMPT:prompt_name] - 파라미터 없음
     * 2. [PROMPT:prompt_name arg1=value1 arg2=value2] - 파라미터 있음
     * 3. [PROMPT:prompt_name]내용[/PROMPT] - 블록 형식
     */
    @Nullable
    private PromptInstruction extractPromptInstruction(String message) {
        if(message == null || message.trim().isEmpty()) {
            return null;
        }

        // 패턴 1
        String blockPattern = "\\[PROMPT:([^\\]\\s]+)([^\\]]*)\\](.+?)\\[/PROMPT\\]";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(blockPattern, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(message);

        if(matcher.find()) {
            String promptName = matcher.group(1).trim();
            String argsStr = matcher.group(2).trim();
            String content = matcher.group(3).trim();

            // 나머지 메시지 (프롬프트 블록 제외)
            String remainingMessage = message.substring(0, matcher.start()) + message.substring(matcher.end());

            return new PromptInstruction(
                    promptName, parseArguments(argsStr), content + "\n\n" + remainingMessage
            );
        }

        // 패턴 2: [PROMPT: name args]
        String inlinePattern = "\\[PROMPT:([^\\]\\s]+)([^\\]]*)\\]";
        pattern = java.util.regex.Pattern.compile(inlinePattern);
        matcher = pattern.matcher(message);

        if(matcher.find()) {
            String promptName = matcher.group(1).trim();
            String argsStr = matcher.group(2).trim();

            // 프롬프트 태그를 제거한 나머지 메시지
            String content = message.substring(0, matcher.start()) + message.substring(matcher.end());

            return new PromptInstruction(
                    promptName, parseArguments(argsStr), content.trim()
            );
        }

        return null;
    }

    /**
     * 인자 문자열 파싱
     * 형식: arg1=value1 arg2="value with space" arg3=value3
     */
    private Map<String, String> parseArguments(String argsStr) {
        Map<String, String> args = new HashMap<>();

        if(argsStr == null || argsStr.trim().isEmpty()) {
            return args;
        }

        // 간단한 key=value 파싱 (quoted strings 지원)
        String pattern = "(\\w+)=(\"[^\"]+\"|\\S+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(argsStr);

        while(m.find()) {
            String key = m.group(1);
            String value = m.group(2);
            // 따옴표 제거
            if(value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            args.put(key, value);
        }

        return args;
    }

    /**
     * Prompt Fetcher: MCP 서버에서 조회 + 캐싱
     * MCP 서버에서 프롬프트 가져오기
     */
    @Nullable
    private String getPromptFromMcp(String promptName, Map<String, String> arguments) {
        // 캐시 확인
        if (enableCache && promptCache != null) {
            String cacheKey = promptName + "_" + arguments.hashCode();
            PromptCache cached = promptCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                log.debug("Using cached prompt: {}", promptName);
                return cached.content;
            }
        }

        // MCP 클라이언트에서 프롬프트 조회
        for (McpSyncClient client : mcpClients) {
            try {
                // GetPrompt 요청 생성 - 빌더 패턴 없음
                Map<String, Object> args = arguments != null ? new HashMap<>(arguments) : null;
                McpSchema.GetPromptRequest request = new McpSchema.GetPromptRequest(promptName, args);

                McpSchema.GetPromptResult result = client.getPrompt(request);

                if (result != null && result.messages() != null && !result.messages().isEmpty()) {
                    // 첫 번째 메시지의 content 추출
                    McpSchema.PromptMessage firstMessage = result.messages().get(0);

                    if (firstMessage.content() instanceof McpSchema.TextContent textContent) {
                        String content = textContent.text();

                        // 캐시에 저장
                        if (enableCache && promptCache != null) {
                            String cacheKey = promptName + "_" + arguments.hashCode();
                            promptCache.put(cacheKey, new PromptCache(content, 5 * 60 * 1000L));
                        }

                        log.debug("Retrieved prompt '{}' from MCP server", promptName);
                        return content;
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to get prompt '{}' from MCP client: {}",
                        promptName, e.getMessage());
                // 다음 클라이언트 시도
            }
        }

        log.warn("Prompt '{}' not found in any MCP server", promptName);
        return null;
    }

    /**
     * 프롬프트 템플릿 적용
     */
    private String applyPrompt(String promptTemplate, String userContent, Map<String, String> arguments) {
        String result = promptTemplate;

        // 사용자 콘텐츠가 있으면 추가
        if (userContent != null && !userContent.isEmpty()) {
            result = result + "\n\n## 입력 내용\n" + userContent;
        }

        // 인자 치환 (간단한 템플릿 엔진 역할)
        if (arguments != null && !arguments.isEmpty()) {
            for (Map.Entry<String, String> entry : arguments.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                result = result.replace(placeholder, entry.getValue());
            }
        }

        return result;
    }

    /**
     * 사용 가능한 프롬프트 목록 조회 (디버깅용)
     */
    public void listAvailablePrompts() {
        log.info("=== Available MCP Prompts ===");

        for (int i = 0; i < mcpClients.size(); i++) {
            McpSyncClient client = mcpClients.get(i);
            try {
                McpSchema.ListPromptsResult result = client.listPrompts();
                if (result != null && result.prompts() != null) {
                    log.info("MCP Client #{}: {} prompts available", i + 1, result.prompts().size());

                    result.prompts().forEach(prompt -> {
                        String argsInfo = "";
                        if (prompt.arguments() != null && !prompt.arguments().isEmpty()) {
                            argsInfo = " (args: " + prompt.arguments().stream()
                                    .map(McpSchema.PromptArgument::name)
                                    .collect(Collectors.joining(", ")) + ")";
                        }
                        log.info("  - {} : {}{}",
                                prompt.name(),
                                prompt.description() != null ? prompt.description() : "No description",
                                argsInfo);
                    });
                }
            } catch (Exception e) {
                log.error("Failed to list prompts from MCP client #{}: {}", i + 1, e.getMessage());
            }
        }

        log.info("============================");
    }

    /**
     * 프롬프트 지시사항 내부 클래스
     */
    private static class PromptInstruction {
        final String promptName;
        final Map<String, String> arguments;
        final String userContent;

        PromptInstruction(String promptName, Map<String, String> arguments, String userContent) {
            this.promptName = promptName;
            this.arguments = arguments;
            this.userContent = userContent;
        }

        @Override
        public String toString() {
            return "PromptInstruction{" +
                    "name='" + promptName + '\'' +
                    ", args=" + arguments +
                    ", contentLength=" + (userContent != null ? userContent.length() : 0) +
                    '}';
        }
    }

    /**
     * Builder 패턴
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<McpSyncClient> mcpClients;
        private boolean enableCache = true;
        private long cacheTtlMillis = 5 * 60 * 1000L; // 5분
        private int order = 0;

        public Builder mcpClients(List<McpSyncClient> mcpClients) {
            this.mcpClients = mcpClients;
            return this;
        }

        public Builder enableCache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        public Builder cacheTtl(long ttlMillis) {
            this.cacheTtlMillis = ttlMillis;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public McpPromptAdvisor build() {
            return new McpPromptAdvisor(mcpClients, enableCache, cacheTtlMillis, order);
        }
    }
}
