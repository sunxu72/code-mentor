package com.sunxu.codementor.ai;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import com.sunxu.codementor.ai.tools.InterviewQuestionTool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeHelperServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    // MCP 为可选能力：配置了有效的智谱 key（bigmodel.mcp.enabled=true）才启用，否则跳过
    @Resource
    private ObjectProvider<McpToolProvider> mcpToolProviderProvider;
    @Resource
    private StreamingChatModel qwenStreamingChatModel;
    @Bean
    public AiCodeHelperService aiCodeHelperService() {
        // 会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 构造 AI Service
        return AiServices.builder(AiCodeHelperService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
                .contentRetriever(contentRetriever) // RAG 检索增强生成
                .tools(new InterviewQuestionTool()) // 工具调用
                .toolProvider(mcpToolProviderProvider.getIfAvailable()) // MCP 工具调用（可选，有智谱 key 才启用）
                .build();
    }


}

