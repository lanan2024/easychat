import axios from 'axios'
import { API_BASE } from '../config'

const http = axios.create({
  baseURL: API_BASE,
  timeout: 10000
})

http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

http.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) return data
    throw new Error(message)
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.hash = '#/login'
    }
    throw new Error(error.response?.data?.message || '网络错误')
  }
)

export const userAPI = {
  login: (username, password) => http.post('/user/login', { username, password }),
  register: (username, password, phone) => http.post('/user/register', { username, password, phone }),
  getUserInfo: () => http.get('/user/info'),
  getUserInfoById: (userId) => http.get(`/user/info/${userId}`),
  updateInfo: (data) => http.put('/user/info', data),
  logout: () => http.post('/user/logout')
}

export const friendAPI = {
  add: (friendId, remark) => http.post('/friend/add', { friendId, remark }),
  delete: (friendId) => http.delete(`/friend/${friendId}`),
  list: () => http.get('/friend/list')
}

export const groupAPI = {
  create: (groupName, ownerId) => http.post('/group/create', { groupName, ownerId }),
  join: (groupId) => http.post(`/group/join/${groupId}`),
  quit: (groupId) => http.post(`/group/quit/${groupId}`),
  setAdmin: (groupId, targetUserId) => http.post(`/group/set-admin/${groupId}/${targetUserId}`),
  list: () => http.get('/group/list'),
  members: (groupId) => http.get(`/group/members/${groupId}`)
}

export const messageAPI = {
  conversation: (targetId, page, size) => http.get(`/message/conversation/${targetId}`, { params: { page, size } }),
  groupMessages: (groupId, page, size) => http.get(`/message/group/${groupId}`, { params: { page, size } })
}
