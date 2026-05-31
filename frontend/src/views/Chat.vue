<template>
  <div class="chat-page">
    <div class="chat-header">
      <button class="back-btn" @click="$router.push('/home')">‹</button>
      <div class="header-info">
        <div class="chat-avatar">{{ friendName?.[0]?.toUpperCase() || '?' }}</div>
        <span class="chat-title">{{ friendName }}</span>
      </div>
    </div>
    <div class="messages" ref="messagesRef">
      <div v-for="msg in messageList" :key="msg.msgId" class="message"
           :class="{ 'self': msg.fromId === store.userInfo?.userId }">
        <div class="msg-avatar" v-if="msg.fromId !== store.userInfo?.userId">
          {{ msg.fromUsername?.[0]?.toUpperCase() || '?' }}
        </div>
        <div class="msg-bubble" :class="{ 'self-bubble': msg.fromId === store.userInfo?.userId }">
          <div class="msg-content">{{ msg.content }}</div>
          <div class="msg-time">{{ formatTime(msg.sendTime) }}</div>
        </div>
      </div>
      <div v-if="messageList.length === 0" class="empty-msg">暂无消息，开始聊天吧</div>
    </div>
    <div class="input-area">
      <input v-model="inputText" class="msg-input" placeholder="输入消息..."
             @keyup.enter="sendMessage" />
      <button class="send-btn" @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../store'
import { messageAPI } from '../api'
import { wsService } from '../utils/websocket'

const route = useRoute()
const store = useUserStore()

const targetUserId = computed(() => Number(route.params.userId))
const friendName = ref('')
const inputText = ref('')
const messagesRef = ref(null)
const messageList = ref([])

const convKey = computed(() => `user_${targetUserId.value}`)

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''

  wsService.send({
    type: 'SINGLE_SEND',
    toId: targetUserId.value,
    content: text
  })

  messageList.value.push({
    msgId: Date.now(),
    fromId: store.userInfo.userId,
    toId: targetUserId.value,
    content: text,
    sendTime: new Date().toISOString(),
    msgType: 1,
    fromUsername: store.userInfo.username
  })
}

function handleIncoming(data) {
  const msg = data.message
  if (msg.msgType === 1 && (msg.fromId === targetUserId.value || msg.toId === targetUserId.value)) {
    messageList.value.push(msg)
  }
}

async function loadHistory() {
  try {
    const msgs = await messageAPI.conversation(targetUserId.value, 0, 50)
    messageList.value = msgs || []
    const storeMsgs = store.messages[convKey.value]
    if (storeMsgs) {
      messageList.value.push(...storeMsgs)
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(async () => {
  if (targetUserId.value) {
    try {
      const { userAPI } = await import('../api')
      const info = await userAPI.getUserInfoById(targetUserId.value)
      friendName.value = info.username
    } catch (e) {
      friendName.value = '用户'
    }
    await loadHistory()
    wsService.on('NEW_MESSAGE', handleIncoming)
  }
})

onUnmounted(() => {
  wsService.off('NEW_MESSAGE', handleIncoming)
})
</script>

<style scoped>
.chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f0f0f0;
}

.chat-header {
  height: 56px;
  background: #2e2e2e;
  display: flex;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
}

.back-btn {
  background: none;
  border: none;
  color: #ccc;
  font-size: 24px;
  cursor: pointer;
  margin-right: 12px;
}

.header-info { display: flex; align-items: center; color: #eee; }
.chat-title { font-size: 16px; }

.messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 70%;
}

.message.self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #1aad19;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: bold;
}

.msg-bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.self-bubble {
  background: #95ec69;
}

.msg-content { font-size: 15px; color: #333; line-height: 1.4; }
.msg-time { font-size: 11px; color: #999; margin-top: 4px; text-align: right; }

.empty-msg { text-align: center; color: #999; padding: 40px; }

.input-area {
  display: flex;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #ddd;
  flex-shrink: 0;
}

.msg-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  outline: none;
  font-size: 15px;
  margin-right: 8px;
}

.send-btn {
  padding: 10px 24px;
  background: #1aad19;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
}

.send-btn:hover { background: #169016; }
</style>
