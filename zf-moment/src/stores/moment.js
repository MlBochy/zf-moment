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
          // ȷ��ÿ����̬���б�Ҫ���ֶ�
          this.moments = response.data.map(moment => ({
            ...moment,
            likes: moment.likes || [],
            islike: moment.islike || false,
            liked: moment.liked || 0,
            showComments: false
          }))
          console.log('Fetched moments:', this.moments)
        } else {
          throw new Error(response.msg || '��ȡ��̬ʧ��')
        }
      } catch (error) {
        console.error('��ȡ��̬ʧ��:', error)
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
          throw new Error(response.msg || '����ʧ��')
        }
      } catch (error) {
        throw error
      }
    },
    
    async toggleLike(momentId) {
      try {
        const response = await MomentAPI.likeMoment(momentId)
        if (response.code === 1) {
          // ���¶�̬�ĵ���״̬
          const moment = this.moments.find(m => m.id === momentId)
          if (moment) {
            // �л�����״̬
            moment.islike = !moment.islike
            // ���µ�����
            if (moment.islike) {
              moment.liked = (moment.liked || 0) + 1
            } else {
              moment.liked = Math.max(0, (moment.liked || 0) - 1)
            }
            // ���»�ȡ�����û��б�
            const likesResponse = await MomentAPI.getLikes(momentId)
            if (likesResponse.code === 1) {
              moment.likes = likesResponse.data
            }
          }
          return response
        } else {
          throw new Error(response.msg || '����ʧ��')
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
        ElMessage.error('��ȡ������Ϣʧ��')
      }
    },
    
    async getMomentDetail(momentId) {
      try {
        const response = await MomentAPI.getMomentDetail(momentId)
        if (response.code === 1) {
          return response
        } else {
          throw new Error(response.msg || '��ȡ��̬����ʧ��')
        }
      } catch (error) {
        console.error('��ȡ��̬����ʧ��:', error)
        throw error
      }
    },
    
    async deleteMoment(momentId) {
      try {
        const response = await MomentAPI.deleteMoment(momentId)
        if (response.code === 1) {
          // ���б����Ƴ���ɾ���Ķ�̬
          this.moments = this.moments.filter(m => m.id !== momentId)
          return response
        } else {
          throw new Error(response.msg || 'ɾ��ʧ��')
        }
      } catch (error) {
        console.error('ɾ����̬ʧ��:', error)
        throw error
      }
    },
    
    async updateMoment(momentId, data) {
      try {
        const response = await MomentAPI.updateMoment(momentId, data)
        if (response.code === 1) {
          // ���³ɹ�����±�������
          const index = this.moments.findIndex(m => m.id === momentId)
          if (index !== -1) {
            this.moments[index] = { ...this.moments[index], ...response.data }
          }
          return response
        } else {
          throw new Error(response.msg || '����ʧ��')
        }
      } catch (error) {
        console.error('���¶�̬ʧ��:', error)
        throw error
      }
    },
    
    // ��ȡ�û���̬�б�
    async getUserMoments(userId) {
      try {
        const response = await MomentAPI.getUserMoments(userId)
        if (response.code === 1) {
          // ȷ�� response.data ������
          const momentsData = Array.isArray(response.data) ? response.data : []
          // ����ÿ����̬�ĵ���״̬
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
        console.error('��ȡ�û���̬ʧ��:', error)
        throw error
      }
    }
  }
})