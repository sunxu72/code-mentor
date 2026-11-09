<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { chatStream, createMemoryId } from './api/chat'

const memoryId = ref(createMemoryId())
const input = ref('')
const sending = ref(false)
const listRef = ref(null)
const messages = ref([
  {
    id: 'welcome',
    role: 'ai',
    content: '你好，我是 AI 编程小助手。可以问我编程学习、代码理解和求职面试相关的问题，我会逐步给出建议。',
  },
])

function scrollToBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

async function sendMessage() {
  const text = input.value.trim()
  if (!text || sending.value) {
    return
  }

  messages.value.push({
    id: `user-${Date.now()}`,
    role: 'user',
    content: text,
  })

  const aiMessage = {
    id: `ai-${Date.now()}`,
    role: 'ai',
    content: '',
  }
  messages.value.push(aiMessage)

  input.value = ''
  sending.value = true
  scrollToBottom()

  try {
    await chatStream({
      memoryId: memoryId.value,
      message: text,
      onMessage(chunk) {
        aiMessage.content += chunk
        scrollToBottom()
      },
    })

    if (!aiMessage.content) {
      aiMessage.content = '暂时没有收到回复，请稍后再试。'
    }
  } catch (error) {
    aiMessage.content = aiMessage.content || `请求失败：${error.message || '请检查后端服务是否已启动'}`
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

function displayContent(item) {
  if (item.content) {
    return item.content
  }
  const last = messages.value[messages.value.length - 1]
  if (sending.value && item.role === 'ai' && last?.id === item.id) {
    return '正在思考...'
  }
  return ''
}

function onKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<template>
  <div class="page">
    <header class="header">
      <div>
        <h1>AI 编程小助手</h1>
        <p>编程学习 · 代码答疑 · 求职面试建议</p>
      </div>
      <div class="room">会话 ID {{ memoryId }}</div>
    </header>

    <main ref="listRef" class="messages">
      <div
        v-for="item in messages"
        :key="item.id"
        class="row"
        :class="item.role"
      >
        <div class="avatar">{{ item.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble">
          <pre>{{ displayContent(item) }}</pre>
        </div>
      </div>
    </main>

    <footer class="composer">
      <textarea
        v-model="input"
        :disabled="sending"
        placeholder="输入编程或面试相关问题，Enter 发送，Shift + Enter 换行"
        rows="3"
        @keydown="onKeydown"
      />
      <button :disabled="sending || !input.trim()" type="button" @click="sendMessage">
        {{ sending ? '回复中' : '发送' }}
      </button>
    </footer>
  </div>
</template>
