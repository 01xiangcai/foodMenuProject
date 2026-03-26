/**
 * AI 客服悬浮球 Composable
 * 使用方法：
 *   1. 将此文件复制到项目 src/composables/ 目录
 *   2. 在页面 script 中：
 *        import { useAiChat } from '@/composables/useAiChat'
 *        const { csOpen, csAppName, csTheme, csMessages, csInput, csTyping, csScrollId,
 *                openCs, closeCs, resetCs, csSend } = useAiChat('后端地址', 'AppKey')
 *   3. 在页面 template 最末尾（.page view 内部）粘贴悬浮球模板片段
 *      （见管理后台→机器人管理→接入说明→小程序端）
 */
import { ref, nextTick } from 'vue'

/**
 * @param {string} serverUrl  后端地址，例如 'http://192.168.1.100:9900'
 * @param {string} appKey     机器人 AppKey（从管理后台复制）
 */
export function useAiChat(serverUrl = 'http://127.0.0.1:9900', appKey = '') {
  const csOpen = ref(false)
  const csAppName = ref('AI 客服')
  const csTheme = ref('#7C3AED')
  const csWelcome = ref('你好！有什么可以帮您的吗？')
  const csMessages = ref([])
  const csInput = ref('')
  const csTyping = ref(false)
  const csScrollId = ref('')

  // 会话 ID（基于 AppKey 隔离，跨会话保持）
  let sessionId = uni.getStorageSync('aics_sid_' + appKey) || ''
  if (!sessionId) {
    sessionId = 'wx_' + Math.random().toString(36).substring(2, 10)
    uni.setStorageSync('aics_sid_' + appKey, sessionId)
  }

  let configLoaded = false

  // ==================== 核心方法 ====================

  const openCs = () => {
    csOpen.value = true
    if (!configLoaded) {
      _loadConfig()
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
          sessionId = 'wx_' + Math.random().toString(36).substring(2, 10)
          uni.setStorageSync('aics_sid_' + appKey, sessionId)
          csMessages.value = [{ role: 'assistant', content: csWelcome.value }]
        }
      }
    })
  }

  const csSend = () => {
    if (!csInput.value.trim() || csTyping.value) return
    const text = csInput.value
    csMessages.value.push({ role: 'user', content: text })
    csInput.value = ''
    csTyping.value = true
    _scrollToBottom()

    uni.request({
      url: `${serverUrl}/open/chat/${appKey}/message`,
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: { message: text, sessionId, userId: sessionId },
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

  const _loadConfig = () => {
    uni.request({
      url: `${serverUrl}/open/chat/${appKey}/info`,
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
    uni.request({
      url: `${serverUrl}/open/chat/${appKey}/history`,
      data: { sessionId, limit: 30 },
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
