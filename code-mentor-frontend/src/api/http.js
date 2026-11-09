import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 0,
  headers: {
    Accept: 'text/event-stream',
  },
})

export default http
