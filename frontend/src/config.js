function getServerHost() {
  if (window.electronAPI?.serverHost) {
    return window.electronAPI.serverHost
  }
  const stored = localStorage.getItem('serverHost')
  if (stored) {
    return stored
  }
  return 'localhost'
}

export const SERVER_HOST = getServerHost()
export const API_BASE = `http://${SERVER_HOST}:8080/api`
export const WS_HOST = SERVER_HOST
