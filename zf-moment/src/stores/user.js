import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserProfileService, getUserMomentsService, userInfoUpdateService, userInfoService } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref(null)
  const loading = ref(false)

  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    try {
      loading.value = true
      const response = await userInfoService()
      if (response.code === 1) {
        currentUser.value = response.data
        return response.data
      } else {
        throw new Error(response.msg || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取用户信息
  const fetchUserProfile = async (userId) => {
    try {
      loading.value = true
      const response = await getUserProfileService(userId)
      if (response.code === 1) {
        return response
      } else {
        throw new Error(response.msg || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取用户动态列表
  const fetchUserMoments = async (userId) => {
    try {
      loading.value = true
      const response = await getUserMomentsService(userId)
      if (response.code === 1) {
        return response
      } else {
        throw new Error(response.msg || '获取用户动态列表失败')
      }
    } catch (error) {
      console.error('获取用户动态列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新用户信息
  const updateUserInfo = async (userInfoData) => {
    try {
      loading.value = true
      const response = await userInfoUpdateService(userInfoData)
      if (response.code === 1) {
        // 更新成功后，更新本地用户信息
        if (currentUser.value) {
          currentUser.value = { ...currentUser.value, ...userInfoData }
        }
        return response
      } else {
        throw new Error(response.msg || '更新用户信息失败')
      }
    } catch (error) {
      console.error('更新用户信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新头像
  const updateAvatar = async (avatarUrl) => {
    try {
      loading.value = true
      const response = await userAvatarUpdateService(avatarUrl)
      if (response.code === 1) {
        // 更新成功后，更新本地用户头像
        if (currentUser.value) {
          currentUser.value.avatarUrl = avatarUrl
        }
        return response
      } else {
        throw new Error(response.msg || '更新头像失败')
      }
    } catch (error) {
      console.error('更新头像失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新背景图片
  const updateBackground = async (backgroundUrl) => {
    try {
      loading.value = true
      const response = await userBackgroundUpdateService(backgroundUrl)
      if (response.code === 1) {
        // 更新成功后，更新本地用户背景图片
        if (currentUser.value) {
          currentUser.value.backgroundUrl = backgroundUrl
        }
        return response
      } else {
        throw new Error(response.msg || '更新背景图片失败')
      }
    } catch (error) {
      console.error('更新背景图片失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 退出登录
  const logout = () => {
    currentUser.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    currentUser,
    loading,
    fetchCurrentUser,
    fetchUserProfile,
    fetchUserMoments,
    updateUserInfo,
    updateAvatar,
    updateBackground,
    logout
  }
}) 