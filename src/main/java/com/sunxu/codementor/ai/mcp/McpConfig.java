package com.sunxu.codementor.ai.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱 BigModel 联网搜索 MCP。
 * 默认不启用：需要在 application.yml 配置 bigmodel.mcp.enabled=true，
 * 且 bigmodel.api-key 必须是智谱开放平台（open.bigmodel.cn）GLM Coding Plan 的 API Key。
 */
@Configuration
@ConditionalOnProperty(name = "bigmodel.mcp.enabled", havingValue = "true")
public class McpConfig {

    @Value("${bigmodel.api-key}")
    private String apiKey;

    @Bean
    public McpToolProvider mcpToolProvider() {
        // 和 MCP 服务通讯（官方 SSE 端点为 web_search_prime）
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("https://open.bigmodel.cn/api/mcp/web_search_prime/sse?Authorization=" + apiKey)
                .logRequests(true) // 开启日志，查看更多信息
                .logResponses(true)
                .build();
        // 创建 MCP 客户端
        McpClient mcpClient = new DefaultMcpClient.Builder()
                .key("codeMentorMcpClient")
                .transport(transport)
                .build();
        // 从 MCP 客户端获取工具
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();
        return toolProvider;
    }
}
