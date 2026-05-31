<template>
  <div class="home-page">
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="user-avatar" @click="$router.push('/profile')">
          {{ store.userInfo?.username?.[0]?.toUpperCase() || 'U' }}
        </div>
        <div class="header-icons">
          <span class="icon-btn" @click="$router.push('/contacts')" title="通讯录">👥</span>
          <span class="icon-btn" @click="showCreateGroup = true" title="创建群聊">➕</span>
        </div>
      </div>
      <div class="search-box">
        <input v-model="searchText" type="text" placeholder="搜索..." />
      </div>
      <div class="chat-list">
        <div v-for="conv in filteredConversations" :key="conv.key" class="chat-item"
             @click="openChat(conv)">
          <div class="chat-avatar">{{ conv.name?.[0]?.toUpperCase() || '?' }}</div>
          <div class="chat-info">
            <div class="chat-name">{{ conv.name }}</div>
            <div class="chat-preview">{{ conv.lastMsg || '暂无消息' }}</div>
          </div>
        </div>
        <div v-if="filteredConversations.length === 0" class="empty-state">
          暂无会话
        </div>
      </div>
    </div>
    <div class="main-area">
      <div class="welcome">
        <div class="welcome-icon">💬</div>
        <h2>欢迎使用 EasyChat</h2>
        <p>选择一个好友或群聊开始聊天</p>
      </div>
    </div>

    <div v-if="showCreateGroup" class="modal-overlay" @click.self="showCreateGroup = false">
      <div class="modal">
        <h3>创建群聊</h3>
        <input v-model="groupName" placeholder="群名称" class="modal-input" />
        <div class="modal-actions">
          <button class="btn-cancel" @click="showCreateGroup = false">取消</button>
          <button class="btn-confirm" @click="handleCreateGroup">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store'
import { friendAPI, groupAPI, messageAPI } from '../api'
import { wsService } from '../utils/websocket'

const router = useRouter()
const store = useUserStore()

const searchText = ref('')
const showCreateGroup = ref(false)
const groupName = ref('')

const conversations = computed(() => store.conversations)

const filteredConversations = computed(() => {
  if (!searchText.value) return conversations.value
  return conversations.value.filter(c =>
    c.name.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

async function loadData() {
  try {
    const friends = await friendAPI.list()
    const grps = await groupAPI.list()

    const convs = []
    friends.forEach(f => {
      convs.push({
        key: `user_${f.userId}`,
        type: 'user',
        id: f.userId,
        name: f.remark || f.username,
        lastMsg: ''
      })
    })
    grps.forEach(g => {
      convs.push({
        key: `group_${g.groupId}`,
        type: 'group',
        id: g.groupId,
        name: g.groupName,
        lastMsg: ''
      })
    })
    store.conversations.splice(0, store.conversations.length, ...convs)
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

function openChat(conv) {
  if (conv.type === 'user') {
    router.push(`/chat/${conv.id}`)
  } else {
    router.push(`/group/${conv.id}`)
  }
}

async function handleCreateGroup() {
  if (!groupName.value) return
  try {
    await groupAPI.create(groupName.value, store.userInfo.userId)
    showCreateGroup.value = false
    groupName.value = ''
    await loadData()
  } catch (e) {
    alert(e.message)
  }
}

function handleNewMessage(data) {
  const msg = data.message
  const convKey = msg.msgType === 1 ? `user_${msg.fromId}` : `group_${msg.toId}`
  const conv = store.conversations.find(c => c.key === convKey)
  if (conv) {
    conv.lastMsg = msg.content
  }
}

onMounted(() => {
  loadData()
  wsService.on('NEW_MESSAGE', handleNewMessage)
})

onUnmounted(() => {
  wsService.off('NEW_MESSAGE', handleNewMessage)
})
</script>

<style scoped>
.home-page { display: flex; height: 100%; }

.sidebar {
  width: 300px;
  background: #2e2e2e;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #3a3a3a;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #1aad19;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-weight: bold;
}

.header-icons { display: flex; gap: 12px; }
.icon-btn { cursor: pointer; font-size: 18px; }

.search-box { padding: 8px 12px; }
.search-box input {
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  background: #3a3a3a;
  color: #ccc;
  outline: none;
}
.search-box input::placeholder { color: #666; }

.chat-list { flex: 1; overflow-y: auto; }

.chat-item {
  display: flex;
  padding: 12px 16px;
  cursor: pointer;
  align-items: center;
  border-bottom: 1px solid #3a3a3a;
}
.chat-item:hover { background: #3a3a3a; }

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #1aad19;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  font-weight: bold;
}

.chat-info { flex: 1; min-width: 0; }
.chat-name { color: #eee; font-size: 15px; margin-bottom: 4px; }
.chat-preview { color: #888; font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.main-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.welcome { text-align: center; }
.welcome-icon { font-size: 64px; margin-bottom: 16px; }
.welcome h2 { color: #333; margin-bottom: 8px; }
.welcome p { color: #999; }

.empty-state {
  text-align: center;
  color: #666;
  padding: 40px 16px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  width: 320px;
}

.modal h3 { margin-bottom: 16px; }
.modal-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-bottom: 16px;
  outline: none;
}

.modal-actions { display: flex; justify-content: flex-end; gap: 8px; }
.btn-cancel, .btn-confirm {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-cancel { background: #eee; color: #333; }
.btn-confirm { background: #1aad19; color: #fff; }
</style>
