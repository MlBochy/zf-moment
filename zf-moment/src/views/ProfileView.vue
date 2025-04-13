<template>
    <div class="profile">
      <el-page-header @back="$router.go(-1)">
        <template #content>
          <span class="title">个人资料</span>
        </template>
      </el-page-header>
  
      <div class="content">
        <el-form label-width="80px">
          <el-form-item label="头像">
            <el-upload
              class="avatar-uploader"
              action="http://localhost:8080/upload"
              :show-file-list="false"
              :on-success="handleAvatarUploadSuccess"
              :on-error="handleAvatarUploadError"
              :before-upload="beforeAvatarUpload"
              :auto-upload="true"
              :headers="uploadHeaders"
            >
              <el-avatar 
                :size="100" 
                :src="avatarPreview || userStore.currentUser?.avatarUrl || '/default-avatar.jpg'"
              />
            </el-upload>
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="userStore.currentUser.username" disabled/>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="userStore.currentUser.nickName"/>
          </el-form-item>
          <el-form-item label="简介">
            <el-input v-model="userStore.currentUser.bio" type="textarea"/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave()">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useUserStore } from '@/stores/users'
  import { userInfoUpdateService, userAvatarUpdateService, userInfoService } from '@/api/user'
  import { ElMessage, ElLoading } from 'element-plus'
  
  const userStore = useUserStore()
  const avatarPreview = ref('')
  
  // 上传相关配置
  const uploadHeaders = {
    Authorization: `Bearer ${localStorage.getItem('token')}`
  }
  
  // 加载用户信息
  const loadUserInfo = async () => {
    try {
      const userInfo = await userInfoService()
      if (userInfo.code === 1) {
        userStore.updateUserInfo(userInfo.data)
      } else {
        ElMessage.error('获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    }
  }
  
  // 在组件挂载时加载用户信息
  onMounted(() => {
    loadUserInfo()
  })
  
  // 处理头像上传成功
  const handleAvatarUploadSuccess = (response, file) => {
    console.log('头像上传响应:', response)
    if (response.code === 1) {
      // 设置预览图片
      avatarPreview.value = response.data
      // 更新用户头像
      userStore.currentUser.avatarUrl = response.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.msg || '头像上传失败')
    }
  }
  
  // 处理头像上传失败
  const handleAvatarUploadError = (error) => {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
  
  // 上传前的验证
  const beforeAvatarUpload = (file) => {
    const isImage = file.type.startsWith('image/')
    const isSizeValid = file.size <= 1048576 // 1MB
    if (!isImage) {
      ElMessage.error('请上传图片格式')
    }
    if (!isSizeValid) {
      ElMessage.error('图片大小不能超过1MB')
    }
    return isImage && isSizeValid
  }
  
  const handleSave = async () => {
    try {
      // 如果有新的头像预览，使用预览的URL
      const avatarUrl = avatarPreview.value || userStore.currentUser.avatarUrl
      
      console.log('Saving user info:', {
        id: userStore.currentUser.id,
        nickName: userStore.currentUser.nickName,
        bio: userStore.currentUser.bio,
        avatarUrl: avatarUrl,
      });
      
      const result = await userInfoUpdateService({
        id: userStore.currentUser.id,
        nickName: userStore.currentUser.nickName,
        bio: userStore.currentUser.bio,
        avatarUrl: avatarUrl,
      });
      
      console.log('Update response:', result);
      
      if (result.code === 1) {
        // 更新成功后，清除预览
        avatarPreview.value = ''
        ElMessage.success('保存成功')
      } else {
        ElMessage.error(result.msg || '保存失败')
      }
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败')
    }
  }
  </script>
  
  <style scoped>
  .profile {
    max-width: 600px;
    margin: 20px auto;
    padding: 20px;
  }
  
  .title {
    font-size: 18px;
  }
  
  .content {
    margin-top: 30px;
    background: #fff;
    padding: 20px;
    border-radius: 8px;
  }
  
  .avatar-uploader {
    width: 100px;
    height: 100px;
    border: 1px dashed #d9d9d9;
    border-radius: 50%;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
  }
  
  .avatar-uploader:hover {
    border-color: var(--el-color-primary);
  }
  </style>