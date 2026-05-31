import { useUserStore } from '../store'
import { WS_HOST } from '../config'

class WebSocketService {
  constructor() {
    this.ws = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.listeners = new Map()
  }

  connect(token) {
    if (this.ws?.readyState === WebSocket.OPEN) return
    if (this._connecting) return

    this._connecting = true
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = `${WS_HOST}:9090`
    const ws = new WebSocket(`${protocol}//${host}/ws`)
    this.ws = ws

    ws.onopen = () => {
      if (this.ws !== ws) return
      this._connecting = false
      this.reconnectAttempts = 0
      ws.send(JSON.stringify({ type: 'AUTH', token }))
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this._notify(data.type, data)
      } catch (e) {
        console.error('WebSocket message parse error:', e)
      }
    }

    ws.onclose = () => {
      if (this.ws !== ws) return
      this._connecting = false
      this.ws = null
      this._scheduleReconnect(token)
    }

    ws.onerror = () => {
      if (this.ws !== ws) return
      ws.close()
    }

    this._startHeartbeat()
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
    this.reconnectAttempts = this.maxReconnectAttempts
    this.ws?.close()
    this.ws = null
  }

  send(data) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    }
  }

  on(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, [])
    }
    this.listeners.get(type).push(callback)
  }

  off(type, callback) {
    const cbs = this.listeners.get(type)
    if (cbs) {
      this.listeners.set(type, cbs.filter(cb => cb !== callback))
    }
  }

  _notify(type, data) {
    const cbs = this.listeners.get(type)
    if (cbs) {
      cbs.forEach(cb => cb(data))
    }
    const globalCbs = this.listeners.get('*')
    if (globalCbs) {
      globalCbs.forEach(cb => cb(data))
    }
  }

  _scheduleReconnect(token) {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return
    this.reconnectAttempts++
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts - 1), 30000)
    this.reconnectTimer = setTimeout(() => this.connect(token), delay)
  }

  _startHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
    }
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'PING' })
    }, 15000)
  }
}

export const wsService = new WebSocketService()
