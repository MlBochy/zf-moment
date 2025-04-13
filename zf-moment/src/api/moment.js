import axios from '@/utils/request'

export const MomentAPI = {
  // 获取动态列表
  getMoments: () => axios.get('/moments'),

  // 获取动态详情
  getMomentDetail: (momentId) => axios.get(`/moments/${momentId}`),

  // 点赞/取消点赞
  likeMoment: (id) => axios.post(`/moments/${id}/like`, {}, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  }),

  // 获取点赞用户
  getLikes: (id) => axios.get(`/moments/${id}/likes`),

  // 创建动态
  createMoment: async (data) => {
    return await axios.post('/moments', data, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 取消点赞
  unlikeMoment: async (momentId) => {
    return await axios.delete(`/moments/${momentId}/like`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 删除动态
  deleteMoment: async (momentId) => {
    return await axios.delete(`/moments/${momentId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 更新动态
  updateMoment(momentId, data) {
    return axios.put(`/moments/${momentId}`, data)
  },

  // 获取用户动态列表
  async getUserMoments(userId) {
    return axios.get(`/moments/user/${userId}/moments`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  }
}