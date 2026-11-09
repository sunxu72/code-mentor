import http from './http'

function createSseParser(onData) {
  let buffer = ''

  return (chunk) => {
    buffer += chunk.replace(/\r\n/g, '\n')
    const events = buffer.split('\n\n')
    buffer = events.pop() ?? ''

    for (const event of events) {
      const data = event
        .split('\n')
        .filter((line) => line.startsWith('data:'))
        .map((line) => line.slice(5).replace(/^ /, ''))
        .join('\n')

      if (data && data !== '[DONE]') {
        onData(data)
      }
    }
  }
}

export function chatStream({ memoryId, message, onMessage, signal }) {
  let lastLength = 0
  const parse = createSseParser(onMessage)

  return http.get('/ai/chat', {
    adapter: 'xhr',
    responseType: 'text',
    signal,
    params: { memoryId, message },
    onDownloadProgress(event) {
      const responseText = event.event?.target?.responseText ?? ''
      const nextChunk = responseText.slice(lastLength)
      lastLength = responseText.length
      if (nextChunk) {
        parse(nextChunk)
      }
    },
  })
}

export function createMemoryId() {
  return Math.floor(Math.random() * 2_147_483_647) + 1
}
