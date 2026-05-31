<template>
  <div class="profile-page">
    <div class="profile-header">
      <button class="back-btn" @click="$router.push('/home')">‹</button>
      <span>个人中心</span>
    </div>
    <div class="profile-body">
      <div class="avatar-section">
        <div class="big-avatar">{{ store.userInfo?.username?.[0]?.toUpperCase() || 'U' }}</div>
        <div class="nickname">{{ store.userInfo?.username }}</div>
      </div>
      <div class="info-card">
        <div class="info-row">
          <span class="label">用户ID</span>
          <span class="value">{{ store.userInfo?.userId }}</span>
        </div>
        <div class="info-row">
          <span class="label">用户名</span>
          <span class="value">{{ store.userInfo?.username }}</span>
        </div>
        <div class="info-row">
          <span class="label">手机号</span>
          <span class="value">{{ store.userInfo?.phone || '未设置' }}</span>
        </div>
        <div class="info-row">
          <span class="label">在线状态</span>
          <span class="value" :class="{ online: store.userInfo?.status === 1 }">
            {{ store.userInfo?.status === 1 ? '在线' : '离线' }}
          </span>
        </div>
      </div>
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../store'
import { userAPI } from '../api'

const router = useRouter()
const store = useUserStore()

async function handleLogout() {
  try {
    await userAPI.logout()
  } catch (e) {
    // ignore
  }
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-page { height: 100%; display: flex; flex-direction: column; background: #f5f5f5; }

.profile-header {
  height: 56px;
  background: #2e2e2e;
  display: flex;
  align-items: center;
  padding: 0 16px;
  color: #eee;
  font-size: 16px;
  flex-shrink: 0;
}

.back-btn { background: none; border: none; color: #ccc; font-size: 24px; cursor: pointer; margin-right: 12px; }

.profile-body { flex: 1; overflow-y: auto; }

.avatar-section {
  text-align: center;
  padding: 32px 16px;
  background: linear-gradient(135deg, #1aad19, #0d8b0d);
}

.big-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  font-size: 32px;
  font-weight: bold;
}

.nickname { font-size: 20px; color: #fff; font-weight: bold; }

.info-card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 8px;
  overflow: hidden;
}

.info-row {
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child { border-bottom: none; }

.label { color: #666; font-size: 15px; }
.value { color: #333; font-size: 15px; }
.value.online { color: #1aad19; }

.logout-btn {
  display: block;
  margin: 24px 16px;
  padding: 12px;
  background: #fff;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  width: calc(100% - 32px);
}

.logout-btn:hover { background: #e74c3c; color: #fff; }
</style>
