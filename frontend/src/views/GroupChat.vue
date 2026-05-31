<template>
  <div class="chat-page">
    <div class="chat-header">
      <button class="back-btn" @click="$router.push('/home')">‹</button>
      <div class="header-info">
        <div class="chat-avatar group-avatar">{{ groupName?.[0]?.toUpperCase() || 'G' }}</div>
        <span class="chat-title">{{ groupName }}</span>
        <span class="member-count">({{ members.length }}人)</span>
      </div>
      <button class="info-btn" @click="showMembers = !showMembers">ℹ</button>
    </div>

    <div class="chat-body">
      <div class="messages" ref="messagesRef">
        <div v-for="msg in messageList" :key="msg.msgId" class="message"
             :class="{ 'self': msg.fromId === store.userInfo?.userId }">
          <div class="msg-avatar" v-if="msg.fromId !== store.userInfo?.userId">
            {{ msg.fromUsername?.[0]?.toUpperCase() || '?' }}
          </div>
          <div class="msg-body">
            <div class="msg-sender" v-if="msg.fromId !== store.userInfo?.userId">
              {{ msg.fromUsername }}
            </div>
            <div class="msg-bubble" :class="{ 'self-bubble': msg.fromId === store.userInfo?.userId }">
              <div class="msg-content">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.sendTime) }}</div>
            </div>
          </div>
        </div>
        <div v-if="messageList.length === 0" class="empty-msg">暂无群消息</div>
      </div>

      <div v-if="showMembers" class="members-panel">
        <div class="members-title">群成员</div>
        <div v-for="m in members" :key="m.userId" class="member-item">
          <div class="member-avatar">{{ m.username?.[0]?.toUpperCase() || '?' }}</div>
          <span class="member-name">{{ m.username }}</span>
        </div>
      </div>
    </div>

    <div class="input-area">
      <input v-model="inputText" class="msg-input" placeholder="输入群消息..."
             @keyup.enter="sendMessage" />
      <button class="send-btn" @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../store'
import { messageAPI, groupAPI } from '../api'
import { wsService } from '../utils/websocket'

const route = useRoute()
const store = useUserStore()

const groupId = computed(() => Number(route.params.groupId))
const groupName = ref('群聊')
const members = ref([])
const inputText = ref('')
const messageList = ref([])
const showMembers = ref(false)

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
    type: 'GROUP_SEND',
    groupId: groupId.value,
    content: text
  })
}

function handleIncoming(data) {
  const msg = data.message
  if (msg.msgType === 2 && msg.toId === groupId.value) {
    messageList.value.push(msg)
  }
}

async function loadData() {
  try {
    const grps = await groupAPI.list() || []
    const g = grps.find(g => g.groupId === groupId.value)
    if (g) groupName.value = g.groupName

    members.value = await groupAPI.members(groupId.value) || []
    const msgs = await messageAPI.groupMessages(groupId.value, 0, 50)
    messageList.value = msgs || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
  wsService.on('NEW_MESSAGE', handleIncoming)
})

onUnmounted(() => {
  wsService.off('NEW_MESSAGE', handleIncoming)
})
</script>

<style scoped>
.chat-page { height: 100%; display: flex; flex-direction: column; background: #f0f0f0; }

.chat-header {
  height: 56px;
  background: #2e2e2e;
  display: flex;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
  justify-content: space-between;
}

.back-btn { background: none; border: none; color: #ccc; font-size: 24px; cursor: pointer; margin-right: 8px; }
.header-info { display: flex; align-items: center; color: #eee; flex: 1; }
.chat-title { font-size: 16px; }
.member-count { font-size: 12px; color: #888; margin-left: 6px; }

.group-avatar { width: 32px; height: 32px; border-radius: 50%; background: #e67e22; color: #fff; display: flex; align-items: center; justify-content: center; margin-right: 10px; font-weight: bold; font-size: 14px; }

.info-btn { background: none; border: none; color: #ccc; cursor: pointer; font-size: 20px; }

.chat-body { flex: 1; display: flex; overflow: hidden; }

.messages { flex: 1; padding: 16px; overflow-y: auto; display: flex; flex-direction: column; gap: 12px; }

.message { display: flex; gap: 8px; max-width: 70%; }
.message.self { align-self: flex-end; flex-direction: row-reverse; }

.msg-avatar { width: 36px; height: 36px; border-radius: 50%; background: #1aad19; color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 13px; font-weight: bold; }

.msg-body { }
.msg-sender { font-size: 12px; color: #888; margin-bottom: 2px; }

.msg-bubble { background: #fff; padding: 10px 14px; border-radius: 8px; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }
.self-bubble { background: #95ec69; }

.msg-content { font-size: 15px; color: #333; line-height: 1.4; }
.msg-time { font-size: 11px; color: #999; margin-top: 4px; text-align: right; }

.empty-msg { text-align: center; color: #999; padding: 40px; }

.members-panel {
  width: 200px;
  background: #fff;
  border-left: 1px solid #ddd;
  padding: 12px;
  overflow-y: auto;
  flex-shrink: 0;
}

.members-title { font-size: 14px; font-weight: bold; color: #333; margin-bottom: 12px; }

.member-item { display: flex; align-items: center; padding: 6px 0; gap: 8px; }

.member-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #1aad19;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.member-name { font-size: 14px; color: #333; }

.input-area { display: flex; padding: 12px 16px; background: #fff; border-top: 1px solid #ddd; flex-shrink: 0; }

.msg-input { flex: 1; padding: 10px 14px; border: 1px solid #ddd; border-radius: 6px; outline: none; font-size: 15px; margin-right: 8px; }

.send-btn { padding: 10px 24px; background: #1aad19; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: 15px; }
.send-btn:hover { background: #169016; }
</style>
