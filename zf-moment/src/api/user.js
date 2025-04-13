//导入request.js请求工具
import request from '@/utils/request.js'

//提供调用注册接口的函数
export const userRegisterService = (registerData) => {
    return request.post('/user/register', {
        username: registerData.username,
        password: registerData.password
    });
}

//提供调用登录接口的函数
export const userLoginService = (loginData) => {
    return request.post('/user/login', {
        username: loginData.username,
        password: loginData.password
    });
}

//获取用户详细信息
export const userInfoService = () => {
    console.log('Fetching user info...');
    return request.get('/user/userInfo', {
        params: {
            _t: new Date().getTime()  // 添加时间戳防止缓存
        }
    }).then(response => {
        console.log('User info response:', response);
        return response;
    }).catch(error => {
        console.error('Error fetching user info:', error);
        throw error;
    });
}

//获取用户个人资料
export const getUserProfileService = (userId) => {
    return request.get(`/moments/user/${userId}`);
}

//获取用户朋友圈
export const getUserMomentsService = (userId) => {
    console.log('Fetching user moments for ID:', userId);
    return request.get(`/moments/user/${userId}/moments`, {
        params: {
            _t: new Date().getTime()  // 添加时间戳防止缓存
        }
    }).then(response => {
        console.log('User moments response:', response);
        return response;
    }).catch(error => {
        console.error('Error fetching user moments:', error);
        throw error;
    });
}

//修改个人信息
export const userInfoUpdateService = (userInfoData) => {
    return request.put('/user/update', userInfoData);
}

//修改头像
export const userAvatarUpdateService = (avatarUrl) => {
    return request.patch('/user/updateAvatar', { avatarUrl });
}

//修改背景图片
export const userBackgroundUpdateService = (backgroundUrl) => {
    return request.patch('/user/updateBackground', { backgroundUrl });
}

// 点赞用户动态
export const likeUserMomentService = (userId, momentId) => {
  return request.post(`/moments/user/${userId}/${momentId}/like`, {}, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 获取用户动态评论
export const getUserMomentCommentsService = (userId, momentId) => {
  return request.get(`/moments/user/${userId}/${momentId}/comments`, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 创建用户动态评论
export const createUserMomentCommentService = (userId, momentId, content) => {
  return request.post(`/moments/user/${userId}/${momentId}/comments`, {
    content
  }, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}
