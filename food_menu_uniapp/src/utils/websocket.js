/**
 * WebSocket聊天工具类
 * 封装WebSocket连接、心跳、重连、消息收发
 */

// 获取环境变量
const ENV_API_URL = import.meta.env.VITE_API_URL || ''

/**
 * 构建WebSocket URL
 * 使用运行时检测来区分不同平台
 */
function getWebSocketUrl() {
    let wsBaseUrl = ''

    // 检测是否在H5环境（有window对象）
    if (typeof window !== 'undefined' && window.location) {
        // H5环境：基于当前页面协议和域名
        wsBaseUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}`
        console.log('[WebSocket] H5环境, baseUrl:', wsBaseUrl)
    } else {
        // 小程序环境：使用配置的API地址
        if (ENV_API_URL) {
            wsBaseUrl = ENV_API_URL.replace('http://', 'ws://').replace('https://', 'wss://')
        } else {
            // 默认地址（注意：小程序真机测试需要用局域网IP）
            wsBaseUrl = 'ws://localhost:8080'
        }
        console.log('[WebSocket] 小程序环境, ENV_API_URL:', ENV_API_URL, 'baseUrl:', wsBaseUrl)
    }

    // 去掉末尾的斜杠
    if (wsBaseUrl.endsWith('/')) {
        wsBaseUrl = wsBaseUrl.slice(0, -1)
    }

    return wsBaseUrl
}

/**
 * 消息类型常量
 */
export const MessageType = {
    PING: 'PING',
    PONG: 'PONG',
    SEND_MESSAGE: 'SEND_MESSAGE',
    NEW_MESSAGE: 'NEW_MESSAGE',
    MESSAGE_REVOKED: 'MESSAGE_REVOKED',
    READ_ACK: 'READ_ACK',
    ERROR: 'ERROR',
    CONNECTED: 'CONNECTED'
}

class ChatWebSocket {
    constructor() {
        this.socketTask = null           // WebSocket任务
        this.isConnected = false         // 是否已连接
        this.isConnecting = false        // 是否正在连接
        this.reconnectCount = 0          // 重连次数
        this.maxReconnect = 5            // 最大重连次数
        this.heartbeatTimer = null       // 心跳定时器
        this.heartbeatInterval = 30000   // 心跳间隔（30秒）
        this.reconnectTimer = null       // 重连定时器
        this.listeners = new Map()       // 事件监听器
        this.messageQueue = []           // 消息队列（连接断开时缓存）
    }

    /**
     * 建立WebSocket连接
     */
    connect() {
        if (this.isConnected || this.isConnecting) {
            console.log('WebSocket已连接或正在连接中')
            return
        }

        const token = uni.getStorageSync('fm_token')
        if (!token) {
            console.error('WebSocket连接失败: 未登录')
            return
        }

        this.isConnecting = true
        const wsBaseUrl = getWebSocketUrl()
        // 完整的WebSocket路径：/ws/chat
        const wsUrl = `${wsBaseUrl}/ws/chat?token=${token}`
        console.log('[WebSocket] 连接URL:', wsUrl)

        this.socketTask = uni.connectSocket({
            url: wsUrl,
            success: () => {
                console.log('WebSocket连接请求已发送')
            },
            fail: (err) => {
                console.error('WebSocket连接请求失败', err)
                this.isConnecting = false
                this.tryReconnect()
            }
        })

        // 监听连接打开
        this.socketTask.onOpen(() => {
            console.log('WebSocket连接已打开')
            this.isConnected = true
            this.isConnecting = false
            this.reconnectCount = 0
            this.startHeartbeat()
            this.flushMessageQueue()
            this.emit('open')
        })

        // 监听消息
        this.socketTask.onMessage((res) => {
            this.handleMessage(res.data)
        })

        // 监听连接关闭
        this.socketTask.onClose((res) => {
            console.log('WebSocket连接已关闭', res)
            this.handleClose()
        })

        // 监听错误
        this.socketTask.onError((err) => {
            console.error('WebSocket错误', err)
            this.emit('error', err)
        })
    }

    /**
     * 处理接收到的消息
     */
    handleMessage(data) {
        try {
            const message = JSON.parse(data)
            console.log('收到WebSocket消息:', message)

            switch (message.type) {
                case MessageType.PONG:
                    // 心跳响应
                    break
                case MessageType.CONNECTED:
                    console.log('WebSocket连接成功, userId:', message.data)
                    this.emit('connected', message.data)
                    break
                case MessageType.NEW_MESSAGE:
                    this.emit('newMessage', message.data)
                    break
                case MessageType.MESSAGE_REVOKED:
                    this.emit('messageRevoked', message.data)
                    break
                case MessageType.ERROR:
                    console.error('服务端错误:', message.data)
                    this.emit('error', message.data)
                    break
                default:
                    console.log('未知消息类型:', message.type)
            }
        } catch (e) {
            console.error('解析WebSocket消息失败', e)
        }
    }

    /**
     * 处理连接关闭
     */
    handleClose() {
        this.isConnected = false
        this.isConnecting = false
        this.stopHeartbeat()
        this.emit('close')
        this.tryReconnect()
    }

    /**
     * 尝试重连
     */
    tryReconnect() {
        if (this.reconnectCount >= this.maxReconnect) {
            console.log('WebSocket重连次数已达上限')
            this.emit('reconnectFailed')
            return
        }

        // 清除之前的重连定时器
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer)
        }

        // 递增延迟重连（1s, 2s, 4s, 8s, 16s）
        const delay = Math.min(1000 * Math.pow(2, this.reconnectCount), 16000)
        console.log(`WebSocket将在${delay}ms后尝试第${this.reconnectCount + 1}次重连`)

        this.reconnectTimer = setTimeout(() => {
            this.reconnectCount++
            this.connect()
        }, delay)
    }

    /**
     * 开始心跳
     */
    startHeartbeat() {
        this.stopHeartbeat()
        this.heartbeatTimer = setInterval(() => {
            if (this.isConnected) {
                this.send({ type: MessageType.PING })
            }
        }, this.heartbeatInterval)
    }

    /**
     * 停止心跳
     */
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer)
            this.heartbeatTimer = null
        }
    }

    /**
     * 发送消息
     */
    send(data) {
        if (!this.isConnected) {
            console.warn('WebSocket未连接，消息已加入队列')
            this.messageQueue.push(data)
            return false
        }

        try {
            this.socketTask.send({
                data: JSON.stringify(data),
                success: () => {
                    console.log('WebSocket消息发送成功')
                },
                fail: (err) => {
                    console.error('WebSocket消息发送失败', err)
                }
            })
            return true
        } catch (e) {
            console.error('发送消息异常', e)
            return false
        }
    }

    /**
     * 发送聊天消息
     */
    sendChatMessage(conversationId, content, type = 1, extra = null, replyToId = null) {
        return this.send({
            type: MessageType.SEND_MESSAGE,
            data: {
                conversationId,
                content,
                type,
                extra,
                replyToId
            }
        })
    }

    /**
     * 发送已读确认
     */
    sendReadAck(conversationId, messageId) {
        return this.send({
            type: MessageType.READ_ACK,
            data: {
                conversationId,
                messageId
            }
        })
    }

    /**
     * 清空消息队列
     */
    flushMessageQueue() {
        while (this.messageQueue.length > 0) {
            const msg = this.messageQueue.shift()
            this.send(msg)
        }
    }

    /**
     * 关闭连接
     */
    close() {
        this.stopHeartbeat()
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer)
        }
        if (this.socketTask) {
            this.socketTask.close()
            this.socketTask = null
        }
        this.isConnected = false
        this.isConnecting = false
        this.reconnectCount = 0
    }

    /**
     * 添加事件监听
     */
    on(event, callback) {
        if (!this.listeners.has(event)) {
            this.listeners.set(event, [])
        }
        this.listeners.get(event).push(callback)
    }

    /**
     * 移除事件监听
     */
    off(event, callback) {
        if (!this.listeners.has(event)) return
        if (!callback) {
            this.listeners.delete(event)
        } else {
            const callbacks = this.listeners.get(event)
            const index = callbacks.indexOf(callback)
            if (index > -1) {
                callbacks.splice(index, 1)
            }
        }
    }

    /**
     * 触发事件
     */
    emit(event, data) {
        if (this.listeners.has(event)) {
            this.listeners.get(event).forEach(callback => {
                try {
                    callback(data)
                } catch (e) {
                    console.error('事件回调执行错误', e)
                }
            })
        }
    }
}

// 导出单例
export default new ChatWebSocket()
