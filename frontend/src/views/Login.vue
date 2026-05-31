<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo">
        <div class="logo-icon">E</div>
        <h1>EasyChat</h1>
        <p>仿微信即时通讯系统</p>
      </div>
      <div class="form">
        <div class="input-group">
          <input v-model="username" type="text" placeholder="用户名" @keyup.enter="handleLogin" />
        </div>
        <div class="input-group">
          <input v-model="password" type="password" placeholder="密码" @keyup.enter="handleLogin" />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button class="login-btn" @click="handleLogin" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
        <p class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store'
import { wsService } from '../utils/websocket'

const router = useRouter()
const store = useUserStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await store.login(username.value, password.value)
    router.push('/home')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1aad19 0%, #0d8b0d 100%);
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 380px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}

.logo { text-align: center; margin-bottom: 30px; }

.logo-icon {
  width: 60px;
  height: 60px;
  background: #1aad19;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  font-weight: bold;
  margin: 0 auto 12px;
}

.logo h1 { font-size: 24px; color: #333; margin-bottom: 4px; }
.logo p { font-size: 13px; color: #999; }

.input-group {
  margin-bottom: 16px;
}

.input-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}

.input-group input:focus {
  border-color: #1aad19;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: #1aad19;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 8px;
}

.login-btn:hover { background: #169016; }
.login-btn:disabled { background: #a0d8a0; cursor: not-allowed; }

.error-msg { color: #e74c3c; font-size: 13px; margin-bottom: 8px; text-align: center; }

.register-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.register-link a { color: #1aad19; text-decoration: none; }
.register-link a:hover { text-decoration: underline; }
</style>
