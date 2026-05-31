import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Chat from '../views/Chat.vue'
import Contacts from '../views/Contacts.vue'
import GroupChat from '../views/GroupChat.vue'
import Profile from '../views/Profile.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/home', component: Home },
  { path: '/chat/:userId', component: Chat, props: true },
  { path: '/group/:groupId', component: GroupChat, props: true },
  { path: '/contacts', component: Contacts },
  { path: '/profile', component: Profile }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
