<template>
  <div class="contacts-page">
    <div class="contacts-header">
      <button class="back-btn" @click="$router.push('/home')">‹</button>
      <span>通讯录</span>
      <span class="add-friend" @click="showAddFriend = true">＋</span>
    </div>
    <div class="contacts-body">
      <div class="section">
        <div class="section-title">好友 ({{ friends.length }})</div>
        <div v-for="f in friends" :key="f.userId" class="contact-item"
             @click="$router.push(`/chat/${f.userId}`)">
          <div class="contact-avatar">{{ (f.remark || f.username)?.[0]?.toUpperCase() || '?' }}</div>
          <div class="contact-info">
            <div class="contact-name">{{ f.remark || f.username }}</div>
          </div>
        </div>
        <div v-if="friends.length === 0" class="empty-text">暂无好友</div>
      </div>

      <div class="section">
        <div class="section-title">群聊 ({{ groups.length }})</div>
        <div v-for="g in groups" :key="g.groupId" class="contact-item"
             @click="$router.push(`/group/${g.groupId}`)">
          <div class="contact-avatar group-avatar">{{ g.groupName?.[0]?.toUpperCase() || 'G' }}</div>
          <div class="contact-info">
            <div class="contact-name">{{ g.groupName }}</div>
          </div>
        </div>
        <div v-if="groups.length === 0" class="empty-text">暂无群聊</div>
      </div>
    </div>

    <div v-if="showAddFriend" class="modal-overlay" @click.self="showAddFriend = false">
      <div class="modal">
        <h3>添加好友</h3>
        <input v-model="friendIdInput" placeholder="好友用户ID" class="modal-input" type="number" />
        <input v-model="remarkInput" placeholder="备注（选填）" class="modal-input" />
        <div class="modal-actions">
          <button class="btn-cancel" @click="showAddFriend = false">取消</button>
          <button class="btn-confirm" @click="handleAddFriend" :disabled="!friendIdInput">添加</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store'
import { friendAPI, groupAPI } from '../api'

const store = useUserStore()
const friends = ref([])
const groups = ref([])
const showAddFriend = ref(false)
const friendIdInput = ref('')
const remarkInput = ref('')

async function loadData() {
  try {
    friends.value = await friendAPI.list() || []
    groups.value = await groupAPI.list() || []
  } catch (e) {
    console.error(e)
  }
}

async function handleAddFriend() {
  if (!friendIdInput.value) return
  try {
    await friendAPI.add(Number(friendIdInput.value), remarkInput.value)
    showAddFriend.value = false
    friendIdInput.value = ''
    remarkInput.value = ''
    await loadData()
  } catch (e) {
    alert(e.message)
  }
}

onMounted(loadData)
</script>

<style scoped>
.contacts-page { height: 100%; display: flex; flex-direction: column; background: #f5f5f5; }

.contacts-header {
  height: 56px;
  background: #2e2e2e;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  color: #eee;
  font-size: 16px;
  flex-shrink: 0;
}

.back-btn { background: none; border: none; color: #ccc; font-size: 24px; cursor: pointer; }
.add-friend { cursor: pointer; font-size: 22px; }

.contacts-body { flex: 1; overflow-y: auto; padding: 8px 0; }

.section { margin-bottom: 8px; }
.section-title {
  padding: 8px 16px;
  font-size: 13px;
  color: #888;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  background: #fff;
}

.contact-item:hover { background: #f0f0f0; }

.contact-avatar {
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

.group-avatar { background: #e67e22; }

.contact-name { color: #333; font-size: 15px; }

.empty-text { padding: 20px 16px; color: #999; text-align: center; }

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
  margin-bottom: 12px;
  outline: none;
}

.modal-actions { display: flex; justify-content: flex-end; gap: 8px; }
.btn-cancel, .btn-confirm { padding: 8px 20px; border: none; border-radius: 4px; cursor: pointer; }
.btn-cancel { background: #eee; color: #333; }
.btn-confirm { background: #1aad19; color: #fff; }
.btn-confirm:disabled { background: #a0d8a0; cursor: not-allowed; }
</style>
