import { STICKER_LIST } from './emoji-data'

/**
 * 解析评论内容，将文本和表情包代码分离
 * @param {String} text 原始评论文本
 * @returns {Array} 解析后的片段数组
 * 示例输出:
 * [
 *   { type: 'text', content: '这道菜真好吃 ' },
 *   { type: 'image', url: '...', code: '[表情:点赞]' },
 *   { type: 'text', content: ' 强烈推荐' }
 * ]
 */
export function parseCommentContent(text) {
    if (!text) return []

    // 正则匹配 [表情:xxx]
    // 懒惰匹配，确保不包含多余字符
    const regex = /\[表情:([\u4e00-\u9fa5a-zA-Z0-9]+)\]/g

    const segments = []
    let lastIndex = 0
    let match

    while ((match = regex.exec(text)) !== null) {
        // 1. 添加匹配位置之前的文本 (如果有)
        if (match.index > lastIndex) {
            segments.push({
                type: 'text',
                content: text.slice(lastIndex, match.index)
            })
        }

        // 2. 查找匹配的表情包
        const code = match[0]
        const sticker = STICKER_LIST.find(s => s.code === code)

        if (sticker) {
            // 找到了对应表情包，作为 image 类型添加
            segments.push({
                type: 'image',
                url: sticker.url,
                code: code
            })
        } else {
            // 没找到对应表情包（可能是无效代码），作为普通文本处理
            segments.push({
                type: 'text',
                content: code
            })
        }

        lastIndex = regex.lastIndex
    }

    // 3. 添加剩余的文本 (如果有)
    if (lastIndex < text.length) {
        segments.push({
            type: 'text',
            content: text.slice(lastIndex)
        })
    }

    return segments
}
