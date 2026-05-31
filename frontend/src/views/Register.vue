<template>
  <div class="register-page">
    <div class="register-card">
      <h1>注册 EasyChat</h1>
      <div class="form">
        <div class="input-group">
          <input v-model="username" type="text" placeholder="用户名" />
        </div>
        <div class="input-group">
          <input v-model="phone" type="text" placeholder="手机号（选填）" />
        </div>
        <div class="input-group">
          <input v-model="password" type="password" placeholder="密码" />
        </div>
        <div class="input-group">
          <input v-model="confirmPassword" type="password" placeholder="确认密码" />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button class="register-btn" @click="handleRegister" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
        <p class="login-link">
          已有账号？<router-link to="/login">去登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store'

const router = useRouter()
const store = useUserStore()

const username = ref('')
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  if (!username.value || !password.value) {
    error.value = '请填写完整信息'
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = '两次密码不一致'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码长度至少6位'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await store.register(username.value, password.value, phone.value || undefined)
    router.push('/home')
  } catch (e) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1aad19 0%, #0d8b0d 100%);
}

.register-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 380px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}

.register-card h1 {
  text-align: center;
  font-size: 22px;
  color: #333;
  margin-bottom: 24px;
}

.input-group { margin-bottom: 16px; }

.input-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 15px;
  outline: none;
}

.input-group input:focus { border-color: #1aad19; }

.register-btn {
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

.register-btn:hover { background: #169016; }
.register-btn:disabled { background: #a0d8a0; cursor: not-allowed; }

.error-msg { color: #e74c3c; font-size: 13px; margin-bottom: 8px; text-align: center; }

.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.login-link a { color: #1aad19; text-decoration: none; }
</style>
