import { defineStore } from 'pinia'
import { MomentAPI } from '@/api/moment'
import { ElMessage } from 'element-plus'

export const useMomentStore = defineStore('moment', {
  state: () => ({
    moments: [],
    loading: false
  }),
  
  actions: {
    async fetchMoments() {
      this.loading = true
      try {
        const response = await MomentAPI.getMoments()
        if (response.code === 1) {
          // 确保每个动态都有必要的字段
          this.moments = response.data.map(moment => ({
            ...moment,
            likes: moment.likes || [],
            islike: moment.islike || false,
            liked: moment.liked || 0,
            showComments: false
          }))
          console.log('Fetched moments:', this.moments)
        } else {
          throw new Error(response.msg || '获取动态失败')
        }
      } catch (error) {
        console.error('获取动态失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async createMoment(data) {
      try {
        const response = await MomentAPI.createMoment(data)
        if (response.code === 1) {
          this.moments.unshift(response.data)
          return response
        } else {
          throw new Error(response.msg || '发布失败')
        }
      } catch (error) {
        throw error
      }
    },
    
    async toggleLike(momentId) {
      try {
        const response = await MomentAPI.likeMoment(momentId)
        if (response.code === 1) {
          // 更新动态的点赞状态
          const moment = this.moments.find(m => m.id === momentId)
          if (moment) {
            // 切换点赞状态
            moment.islike = !moment.islike
            // 更新点赞数
            if (moment.islike) {
              moment.liked = (moment.liked || 0) + 1
            } else {
              moment.liked = Math.max(0, (moment.liked || 0) - 1)
            }
            // 重新获取点赞用户列表
            const likesResponse = await MomentAPI.getLikes(momentId)
            if (likesResponse.code === 1) {
              moment.likes = likesResponse.data
            }
          }
          return response
        } else {
          throw new Error(response.msg || '操作失败')
        }
      } catch (error) {
        throw error
      }
    },
    
    async fetchMomentLikers(momentId) {
      try {
        const response = await MomentAPI.getLikes(momentId)
        const moment = this.moments.find(m => m.id === momentId)
        if (moment) {
          moment.topLikers = response.data
        }
      } catch (error) {
        ElMessage.error('获取点赞信息失败')
      }
    },
    
    async getMomentDetail(momentId) {
      try {
        const response = await MomentAPI.getMomentDetail(momentId)
        if (response.code === 1) {
          return response
        } else {
          throw new Error(response.msg || '获取动态详情失败')
        }
      } catch (error) {
        console.error('获取动态详情失败:', error)
        throw error
      }
    },
    
    async deleteMoment(momentId) {
      try {
        const response = await MomentAPI.deleteMoment(momentId)
        if (response.code === 1) {
          // 从列表中移除被删除的动态
          this.moments = this.moments.filter(m => m.id !== momentId)
          return response
        } else {
          throw new Error(response.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除动态失败:', error)
        throw error
      }
    },
    
    async updateMoment(momentId, data) {
      try {
        const response = await MomentAPI.updateMoment(momentId, data)
        if (response.code === 1) {
          // 更新成功后更新本地数据
          const index = this.moments.findIndex(m => m.id === momentId)
          if (index !== -1) {
            this.moments[index] = { ...this.moments[index], ...response.data }
          }
          return response
        } else {
          throw new Error(response.msg || '更新失败')
        }
      } catch (error) {
        console.error('更新动态失败:', error)
        throw error
      }
    },
    
    // 获取用户动态列表
    async getUserMoments(userId) {
      try {
        const response = await MomentAPI.getUserMoments(userId)
        if (response.code === 1) {
          // 确保 response.data 是数组
          const momentsData = Array.isArray(response.data) ? response.data : []
          // 处理每个动态的点赞状态
          const moments = momentsData.map(moment => ({
            ...moment,
            islike: moment.likes?.some(like => like.id === this.currentUserId) || false,
            liked: moment.liked || 0,
            showComments: false
          }))
          return { code: 1, data: moments }
        }
        return response
      } catch (error) {
        console.error('获取用户动态失败:', error)
        throw error
      }
    }
  }
})