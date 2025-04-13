import { defineStore } from 'pinia'
import { CommentAPI } from '@/api/comment'
import { ElMessage } from 'element-plus'
import { useMomentStore } from './moment'

export const useCommentStore = defineStore('comment', {
  state: () => ({
    comments: [],
    loading: false
  }),
  
  actions: {
    async createComment({ momentId, content }) {
      try {
        const response = await CommentAPI.createComment(momentId, { content })
        if (response.code === 1) {
          // 更新动态的评论列表
          const momentStore = useMomentStore()
          const moment = momentStore.moments.find(m => m.id === momentId)
          if (moment) {
            if (!moment.comments) {
              moment.comments = []
            }
            // 将新评论添加到列表开头
            moment.comments.unshift(response.data)
          }
          return response.data
        }
        throw new Error(response.msg || '评论失败')
      } catch (error) {
        console.error('创建评论失败:', error)
        throw error
      }
    },
    
    async getComments(momentId) {
      this.loading = true
      try {
        const response = await CommentAPI.getComments(momentId)
        if (response.code === 1) {
          this.comments = response.data
          return response.data
        }
        throw new Error(response.msg || '获取评论失败')
      } catch (error) {
        console.error('获取评论失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async deleteComment(commentId) {
      try {
        const response = await CommentAPI.deleteComment(commentId)
        if (response.code === 1) {
          // 从评论列表中移除
          this.comments = this.comments.filter(c => c.id !== commentId)
          // 从动态的评论列表中移除
          const momentStore = useMomentStore()
          for (const moment of momentStore.moments) {
            if (moment.comments) {
              const index = moment.comments.findIndex(c => c.id === commentId)
              if (index > -1) {
                moment.comments.splice(index, 1)
                break
              }
            }
          }
          return response
        } else {
          throw new Error(response.msg || '删除评论失败')
        }
      } catch (error) {
        throw error
      }
    }
  }
}) 