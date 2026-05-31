import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userAPI } from '../api/user'
import { wsService } from '../utils/websocket'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const contacts = ref([])
  const groups = ref([])
  const conversations = ref([])
  const messages = ref({})

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await userAPI.login(username, password)
    token.value = res.token
    userInfo.value = res.user
    localStorage.setItem('token', res.token)
    localStorage.setItem('userInfo', JSON.stringify(res.user))
    wsService.connect(res.token)
    return res
  }

  async function register(username, password, phone) {
    const res = await userAPI.register(username, password, phone)
    token.value = res.token
    userInfo.value = res.user
    localStorage.setItem('token', res.token)
    localStorage.setItem('userInfo', JSON.stringify(res.user))
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    contacts.value = []
    groups.value = []
    conversations.value = []
    messages.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    wsService.disconnect()
  }

  function addMessage(convKey, msg) {
    if (!messages.value[convKey]) {
      messages.value[convKey] = []
    }
    messages.value[convKey].push(msg)
  }

  function setMessages(convKey, msgs) {
    messages.value[convKey] = msgs
  }

  return {
    token, userInfo, contacts, groups, conversations, messages, isLoggedIn,
    login, register, logout, addMessage, setMessages
  }
})
