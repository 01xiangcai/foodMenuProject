import { ref, nextTick } from 'vue'

/**
 * AI 客服悬浮球 Composable
 * 支持动态从后端拉取配置
 */
export function useAiChat() {
  const serverUrl = ref('http://127.0.0.1:9900')
  const appKey = ref('')
  
  const csOpen = ref(false)
  const csAppName = ref('AI 客服')
  const csTheme = ref('#7C3AED')
  const csWelcome = ref('你好！有什么可以帮您的吗？')
  const csMessages = ref([])
  const csInput = ref('')
  const csTyping = ref(false)
  const csScrollId = ref('')

  let sessionId = ref('')
  let configLoaded = false
  let backendLoading = false

  // ==================== 核心方法 ====================

  /**
   * 初始化配置并开启
   */
  const openCs = async () => {
    csOpen.value = true
    if (!configLoaded && !backendLoading) {
      await _fetchBackendConfig()
    }
  }

  const closeCs = () => {
    csOpen.value = false
  }

  const resetCs = () => {
    uni.showModal({
      title: '重置对话',
      content: '将清空本次对话，是否继续？',
      success: (r) => {
        if (r.confirm) {
          sessionId.value = 'wx_' + Math.random().toString(36).substring(2, 10)
          uni.setStorageSync('aics_sid_' + appKey.value, sessionId.value)
          csMessages.value = [{ role: 'assistant', content: csWelcome.value }]
        }
      }
    })
  }

  const csSend = () => {
    if (!csInput.value.trim() || csTyping.value || !appKey.value) return
    const text = csInput.value
    csMessages.value.push({ role: 'user', content: text })
    csInput.value = ''
    csTyping.value = true
    _scrollToBottom()

    uni.request({
      url: `${serverUrl.value}/open/chat/${appKey.value}/message`,
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: { message: text, sessionId: sessionId.value, userId: sessionId.value },
      success: (res) => {
        csMessages.value.push({
          role: 'assistant',
          content: (res.data?.code === 200 && res.data?.data)
            ? res.data.data
            : '暂时无法回复，请稍后重试'
        })
      },
      fail: (err) => {
        console.error('[AI客服] 发送失败', err)
        csMessages.value.push({ role: 'assistant', content: '网络错误，请检查连接' })
      },
      complete: () => {
        csTyping.value = false
        _scrollToBottom()
      }
    })
  }

  // ==================== 私有方法 ====================

  /**
   * 从后端获取最新的 AI 配置
   */
  const _fetchBackendConfig = () => {
    return new Promise((resolve) => {
      backendLoading = true
      // 获取环境变量中的 API 地址
      const ENV_API_URL = JSON.parse(JSON.stringify(import.meta.env.VITE_API_URL || ''))
      let baseUrl = ENV_API_URL
      // #ifdef H5
      if (!baseUrl) baseUrl = '/api'
      // #endif

      uni.request({
        url: `${baseUrl}/public/ai/config`,
        method: 'GET',
        success: (res) => {
          if (res.data && (res.data.code === 1 || res.data.code === 200) && res.data.data) {
            appKey.value = res.data.data.appKey
            serverUrl.value = res.data.data.baseUrl || serverUrl.value
            console.log('[AI客服] 成功加载后端配置:', { appKey: appKey.value, serverUrl: serverUrl.value })
            
            // 初始化会话 ID
            sessionId.value = uni.getStorageSync('aics_sid_' + appKey.value) || ''
            if (!sessionId.value) {
              sessionId.value = 'wx_' + Math.random().toString(36).substring(2, 10)
              uni.setStorageSync('aics_sid_' + appKey.value, sessionId.value)
            }
            
            // 继续加载配置
            _loadAiServiceConfig()
          }
        },
        fail: (err) => {
          console.error('[AI客服] 拉取后端配置失败:', err)
        },
        complete: () => {
          backendLoading = false
          resolve()
        }
      })
    })
  }

  /**
   * 从 AI 服务加载 UI 配置
   */
  const _loadAiServiceConfig = () => {
    if (!appKey.value) return
    uni.request({
      url: `${serverUrl.value}/open/chat/${appKey.value}/info`,
      method: 'GET',
      success: (res) => {
        if (res.data?.code === 200 && res.data?.data) {
          const d = res.data.data
          csAppName.value = d.appName || csAppName.value
          csTheme.value = d.themeColor || csTheme.value
          csWelcome.value = d.welcomeMsg || csWelcome.value
        }
        _loadHistory()
        configLoaded = true
      },
      fail: () => {
        _loadHistory()
        configLoaded = true
      }
    })
  }

  const _loadHistory = () => {
    if (!appKey.value) return
    uni.request({
      url: `${serverUrl.value}/open/chat/${appKey.value}/history`,
      data: { sessionId: sessionId.value, limit: 30 },
      method: 'GET',
      success: (res) => {
        if (res.data?.code === 200 && res.data?.data?.length > 0) {
          csMessages.value = res.data.data.map(m => ({ role: m.role, content: m.content }))
        } else {
          csMessages.value = [{ role: 'assistant', content: csWelcome.value }]
        }
        _scrollToBottom()
      },
      fail: () => {
        csMessages.value = [{ role: 'assistant', content: csWelcome.value }]
      }
    })
  }

  const _scrollToBottom = () => {
    nextTick(() => {
      csScrollId.value = csTyping.value
        ? 'cs-typing'
        : 'cs-msg-' + (csMessages.value.length - 1)
    })
  }

  return {
    csOpen, csAppName, csTheme, csWelcome,
    csMessages, csInput, csTyping, csScrollId,
    openCs, closeCs, resetCs, csSend
  }
}
