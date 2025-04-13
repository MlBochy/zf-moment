import axios from '@/utils/request'

export const MomentAPI = {
  // ��ȡ��̬�б�
  getMoments: () => axios.get('/moments'),

  // ��ȡ��̬����
  getMomentDetail: (momentId) => axios.get(`/moments/${momentId}`),

  // ����/ȡ������
  likeMoment: (id) => axios.post(`/moments/${id}/like`, {}, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  }),

  // ��ȡ�����û�
  getLikes: (id) => axios.get(`/moments/${id}/likes`),

  // ������̬
  createMoment: async (data) => {
    return await axios.post('/moments', data, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // ȡ������
  unlikeMoment: async (momentId) => {
    return await axios.delete(`/moments/${momentId}/like`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // ɾ����̬
  deleteMoment: async (momentId) => {
    return await axios.delete(`/moments/${momentId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // ���¶�̬
  updateMoment(momentId, data) {
    return axios.put(`/moments/${momentId}`, data)
  },

  // ��ȡ�û���̬�б�
  async getUserMoments(userId) {
    return axios.get(`/moments/user/${userId}/moments`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  }
}