import request from '@/utils/request'

export const CommentAPI = {
  // 创建评论
  createComment: (momentId, data) => {
    return request({
      url: `/moments/${momentId}/comments`,
      method: 'post',
      data,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 获取评论列表
  getComments: (momentId) => {
    return request({
      url: `/moments/${momentId}/comments`,
      method: 'get',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 删除评论
  deleteComment: (momentId, commentId) => {
    return request({
      url: `/moments/${momentId}/comments/${commentId}`,
      method: 'delete',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 获取用户动态的评论
  getUserMomentComments: (userId, momentId) => {
    return request({
      url: `/moments/user/${userId}/${momentId}/comments`,
      method: 'get',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 创建用户动态的评论
  createUserMomentComment: (userId, momentId, content) => {
    return request({
      url: `/moments/user/${userId}/${momentId}/comments`,
      method: 'post',
      data: { content },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 更新评论
  updateComment: (momentId, commentId, content) => {
    return request({
      url: `/moments/${momentId}/comments/${commentId}`,
      method: 'put',
      data: { content },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  },

  // 获取评论详情
  getCommentDetail: (momentId, commentId) => {
    return request({
      url: `/moments/${momentId}/comments/${commentId}`,
      method: 'get',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
  }
} 