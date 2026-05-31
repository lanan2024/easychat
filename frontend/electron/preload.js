const { contextBridge, ipcRenderer } = require('electron')

const serverHostArg = process.argv.find(a => a.startsWith('--server-host='))
const serverHost = serverHostArg ? serverHostArg.split('=')[1] : ''

contextBridge.exposeInMainWorld('electronAPI', {
  serverHost,
  minimize: () => ipcRenderer.send('window-minimize'),
  maximize: () => ipcRenderer.send('window-maximize'),
  close: () => ipcRenderer.send('window-close'),
  isMaximized: () => ipcRenderer.invoke('window-is-maximized'),
  onMaximizeChange: (callback) => {
    ipcRenderer.on('window-maximize-change', (_, isMaximized) => callback(isMaximized))
  }
})
