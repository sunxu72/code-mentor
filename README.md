# code-mentor · AI 编程学习助手

面向编程学习与求职场景的对话助手：回答学习路线、项目建议、简历与投递技巧、面试题，并支持知识库检索与联网搜索。

> 本项目基于开源教程（程序员鱼皮）学习实现，之后做了包结构、配置与工程化方面的整理，见文末「我做过的整理」。

## 功能

- **声明式 AI Service**：`AiServices` + `@SystemMessage` 定义助手人设，无需手写 Prompt 拼装
- **多会话记忆**：`@MemoryId` + `MessageWindowChatMemory`，每个会话保留最近 10 轮上下文
- **RAG 检索**：文档按段落切分（1000 字 / 200 重叠）→ 向量化 → 检索增强回答
- **工具调用**：`@Tool` 封装面试题检索，模型按需自主调用
- **MCP 联网搜索**：接入智谱 BigModel 的 MCP 服务，条件装配、可随时关闭
- **输入护栏**：`InputGuardrail` 在调用模型前拦截敏感输入，避免无效请求消耗 token
- **流式输出**：`Flux` + `ServerSentEvent`，后端逐段推送、前端逐字渲染
- **结构化输出**：`record Report` 直接映射成学习报告对象
- **可观测**：`ChatModelListener` 统一记录请求 / 响应 / 异常

## 技术栈

| 层 | 选型 |
|---|---|
| 框架 | Java 21、Spring Boot 4.1 |
| AI | LangChain4j 1.1、通义千问 qwen-max、text-embedding-v4 |
| 扩展 | MCP（langchain4j-mcp）、Jsoup |
| 前端 | Vite + 原生 JS |

## 快速开始

前置：JDK 21、Maven 3.9+、通义千问（DashScope）API Key

```bash
# 1. 配置环境变量（不要写进 application.yml）
export DASHSCOPE_API_KEY=sk-xxxx        # Windows PowerShell: $env:DASHSCOPE_API_KEY="sk-xxxx"

# 2. 启动后端（默认 8081，上下文路径 /api）
mvn spring-boot:run

# 3. 调用（流式返回）
curl "http://localhost:8081/api/ai/chat?memoryId=1&message=Java%E5%AD%A6%E4%B9%A0%E8%B7%AF%E7%BA%BF"
```

启动前端（可选）：

```bash
cd code-mentor-frontend
npm install
npm run dev
```

启用联网搜索 MCP（可选）：

```bash
export BIGMODEL_API_KEY=your-key
export BIGMODEL_MCP_ENABLED=true
```

## 添加知识库文档

把 `.md` / `.txt` / `.pdf` 等文件放进 `src/main/resources/docs/`，启动时会自动切分、向量化并进入检索库。

目录不存在时会跳过检索、不影响启动（空目录不会被 git 跟踪，clone 下来默认没有这个目录，需要自己创建）。

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ai/chat?memoryId={id}&message={text}` | 流式对话，`text/event-stream` |

返回是 SSE，每个事件一段增量文本：

```
data: Java 学习可以分三个阶段

data: ：语法基础、项目实战、源码进阶

data: ...
```

`memoryId` 相同即视为同一个会话，服务端按 `memoryId` 隔离记忆。

## 环境变量

| 变量 | 必填 | 说明 |
|---|---|---|
| `DASHSCOPE_API_KEY` | ✅ | 通义千问 API Key，未设置时调用会失败 |
| `QWEN_MODEL` | | 对话模型，默认 `qwen-max` |
| `QWEN_EMBEDDING_MODEL` | | 向量模型，默认 `text-embedding-v4` |
| `BIGMODEL_API_KEY` | | 智谱 Key，仅在启用 MCP 时需要 |
| `BIGMODEL_MCP_ENABLED` | | 是否启用联网搜索 MCP，默认 `false` |

## 目录结构

```
src/main/java/com/sunxu/codementor
├── AiCodeHelperApplication.java
├── ai/
│   ├── AiCodeHelperService.java        # 声明式 AI Service 接口
│   ├── AiCodeHelperServiceFactory.java # AiServices 装配：记忆 / RAG / 工具 / MCP
│   ├── controller/AiController.java    # SSE 流式接口
│   ├── rag/RagConfig.java              # 文档加载、切分、向量化、检索
│   ├── tools/InterviewQuestionTool.java# 面试题检索工具
│   ├── mcp/McpConfig.java              # 智谱 MCP 客户端（条件装配）
│   ├── guardrail/SafeInputGuardrail.java # 输入护栏
│   └── listener/ChatModelListenerConfig.java # 模型调用监听
└── ...
```

## 已知限制

- 对话记忆与向量库都在内存里，**重启即清空**，也没有多实例共享；生产要换成 Redis 会话 + ES/PGVector
- 没有用户体系与鉴权，接口是公开的
- 单元测试只覆盖了少量场景

## 我做过的整理

- **包结构重构**：`org.mybatis.aicodehelper` → `com.sunxu.codementor`，去掉脚手架默认 groupId
- **配置修正**：`application.yml` 的 `server` 原本被缩进到 `spring` 下，导致端口 8081 与 `/api` 前缀都没生效
- **密钥外置**：模型密钥、MCP 开关全部改为环境变量，仓库不留任何 key
- **启动健壮性**：RAG 文档目录不存在时降级为空检索，避免 clone 后直接启动失败
- **代码清理**：移除生产类中误引入的 `org.junit.jupiter.api.Test`
- **工程化**：补充 `.gitignore`、`.env.example` 与本 README

## 致谢

原教程项目作者：程序员鱼皮。
