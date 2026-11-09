package com.sunxu.codementor.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import com.sunxu.codementor.ai.guardrail.SafeInputGuardrail;
import reactor.core.publisher.Flux;

import java.util.List;
@InputGuardrails(SafeInputGuardrail.class)
public interface AiCodeHelperService {

    @SystemMessage("你是一位编程小助手")
    String chat(String userMessage);

    @SystemMessage("你是一位编程小助手")
    Report chatForReport(String userMessage);



    // 学习报告
    record Report(String name, List<String> suggestionList){}
    @SystemMessage("你是一位编程小助手")
    Result<String> chatWithRag(String userMessage);

    // 流式对话
    @SystemMessage("你是一位编程小助手")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);

}


