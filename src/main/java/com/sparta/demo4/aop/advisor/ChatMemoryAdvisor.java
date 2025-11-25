package com.sparta.demo4.aop.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChatMemoryAdvisor implements BaseAdvisor {
    private static final String MESSAGES_CONTEXT_KEY = "chat_memory_messages";
    private static final String USER_TEXT_KEY = "user_text";

    private final Map<String, List<Message>> conversationHistory;
    private final String conversationId;
    private final int maxMessages;

    public ChatMemoryAdvisor(String conversationId, int maxMessages) {
        this.conversationHistory = new ConcurrentHashMap<>();
        this.conversationId = conversationId;
        this.maxMessages = maxMessages;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain){
        log.info("[Memory Advisor] 대화 기록 추가 중 - conversationId: {}", conversationId);

        // 1. 이전 대화 기록 조회
        List<Message> history = conversationHistory.getOrDefault(conversationId, new ArrayList<>());
        log.info("[Memory Advisor] 기존 대화 기록 수: {}", history.size());

        // 2. 현재 프롬프트에서 사용자 메시지 추출
        String userText = extractUserTextFromPrompt(request.prompt());

        // 3. 대화 기록과 사용자 텍스트를 context에 저장
        return request.mutate()
                .context(MESSAGES_CONTEXT_KEY, new ArrayList<>(history))
                .context(USER_TEXT_KEY, userText)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        log.info("[Memory Advisor] 응답 저장 중...");

        try{
            // 1. 현재 대화 기록 조회
            List<Message> history = conversationHistory.computeIfAbsent(
                    conversationId, k -> new ArrayList<>()
            );

            // 2. context에서 사용자 메시지 추출
            String userText = (String) response.context().get(USER_TEXT_KEY);
            if(userText != null && !userText.isEmpty()){
                history.add(new UserMessage(userText));
                log.debug("[Memory Advisor] 사용자 메시지 저장: {}", userText);
            }

            // 3. 어시스턴트 응답 추가
            if(response.chatResponse() != null && response.chatResponse().getResult() != null &&
            response.chatResponse().getResult().getOutput() != null){
                String assistantText = response.chatResponse()
                        .getResult()
                        .getOutput()
                        .getText();

                if(assistantText != null && !assistantText.isEmpty()){
                    history.add(new AssistantMessage(assistantText));
                    log.debug("[Memory Advisor] 어시스턴트 응답 저장: {}", assistantText);
                }
            }

            // 4. 최대 메시지 수 제한(FIFO)
            while(history.size() > maxMessages){
                Message removed = history.remove(0);
                log.debug("[Memory Advisor] 오래된 메시지 제거: {}", removed.getClass().getSimpleName());
            }
            log.info("[Memory Advisor] 대화 기록 저장 완료 (총 {}개 메시지)", history.size());
        }catch(Exception e){
            log.error("[Memory Advisor] 응답 저장 중 오류 발생", e);
        }
        return response;
    }

    /**
     * Prompt에서 사용자 텍스트 추출
     */
    private String extractUserTextFromPrompt(Prompt prompt){
        try{
            if(prompt.getInstructions() != null && !prompt.getInstructions().isEmpty()){
                // 마지막 메시지가 UserMessage인 경우 추출
                Message lastMessage = prompt.getInstructions()
                        .get(prompt.getInstructions().size() - 1);

                if(lastMessage instanceof UserMessage userMessage){
                    return userMessage.getText();
                }

                // 일반 Message인 경우
                return lastMessage.getText();
            }
            log.warn("[Memory Advisor] Prompt에서 사용자 메시지를 찾을 수 없습니다.");
            return null;

        }catch(Exception e){
            log.error("[Memory Advisor] 사용자 텍스트 추출 실패", e);
            return null;
        }
    }

    /**
     * 대화 기록 조회
     */
    public List<Message> getHistory(){
        return new ArrayList<>(conversationHistory.getOrDefault(conversationId, new ArrayList<>()));
    }

    /**
     * 대화 기록 개수 조회
     */
    public int getHistorySize(){
        return conversationHistory.getOrDefault(conversationId, new ArrayList<>()).size();
    }

    /**
     * 특정 conversationId의 대화 기록만 초기화
     */
    public void clearHistory(){
        conversationHistory.remove(conversationId);
        log.info("[Memory Advisor] 대화 기록 초기화: {}", conversationId);
    }

    /**
     * 모든 대화 기록 초기화
     */
    public void clearAllHistory(){
        conversationHistory.clear();
        log.info("[Memory Advisor] 모든 대화 기록 초기화");
    }

    @Override
    public String getName() {
        return "ChatMemoryAdvisor";
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
