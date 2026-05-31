<template>
  <div class="app-container">
    <div class="title-bar" v-if="isElectron">
      <span class="title-text">EasyChat</span>
      <div class="window-controls">
        <button class="ctrl-btn" @click="minimize">─</button>
        <button class="ctrl-btn" @click="toggleMaximize">☐</button>
        <button class="ctrl-btn close-btn" @click="closeWindow">✕</button>
      </div>
    </div>
    <div class="app-content" :class="{ 'with-title-bar': isElectron }">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const isElectron = ref(!!window.electronAPI)

function minimize() {
  window.electronAPI?.minimize()
}
function toggleMaximize() {
  window.electronAPI?.maximize()
}
function closeWindow() {
  window.electronAPI?.close()
}
</script>

<style>
.app-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.title-bar {
  height: 32px;
  background: #2e2e2e;
  display: flex;
  align-items: center;
  justify-content: space-between;
  -webkit-app-region: drag;
  padding: 0 8px;
  flex-shrink: 0;
}

.title-text {
  color: #ccc;
  font-size: 13px;
}

.window-controls {
  -webkit-app-region: no-drag;
  display: flex;
}

.ctrl-btn {
  background: none;
  border: none;
  color: #999;
  width: 36px;
  height: 32px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ctrl-btn:hover { background: #404040; color: #fff; }
.close-btn:hover { background: #e81123; color: #fff; }

.app-content {
  flex: 1;
  overflow: hidden;
}

.app-content.with-title-bar {
  height: calc(100% - 32px);
}
</style>
