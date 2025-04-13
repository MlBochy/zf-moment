<template>
  <div class="moments-container">
    <!-- 固定在右上角的用户头像下拉菜单 -->
    <div class="fixed-user-menu">
      <el-dropdown trigger="click">
        <el-avatar 
          :size="40" 
          :src="userStore.currentUser?.avatarUrl || '/default-avatar.jpg'"
          class="user-avatar"
        />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="showPostDialog = true">
              <el-icon><Plus /></el-icon> 发布朋友圈
            </el-dropdown-item>
            <el-dropdown-item @click="$router.push('/profile')">
              <el-icon><User /></el-icon> 个人信息
            </el-dropdown-item>
            <el-dropdown-item @click="$router.push('/change-password')">
              <el-icon><Lock /></el-icon> 修改密码
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 朋友圈顶部背景和用户信息 -->
    <div class="moments-header" :style="headerStyle" @click="showBackgroundDialog = true">
      <div class="cover-image">
        <div class="user-info" @click.stop>
          <h2 class="nickname">{{ userStore.currentUser?.nickName || '用户昵称' }}</h2>
          <el-avatar 
            :size="70" 
            :src="userStore.currentUser?.avatarUrl || '/default-avatar.jpg'"
            class="header-avatar"
          />
        </div>
      </div>
    </div>

    <!-- 朋友圈动态列表 -->
    <div class="moments-list">
      <!-- 动态列表 -->
      <div v-loading="momentStore.loading">
        <div v-for="moment in momentStore.moments" :key="moment.id" class="moment-item">
          <!-- 用户头像 -->
          <div class="moment-avatar">
            <el-dropdown trigger="click" @command="handleUserCommand">
              <el-avatar 
                :size="40" 
                :src="moment.user.avatarUrl || '/default-avatar.jpg'"
                @click.stop="showDropdown(moment)"
                class="clickable-avatar"
              />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ type: 'profile', userId: moment.user.id }">
                    查看用户信息
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <!-- 动态内容区域 -->
          <div class="moment-content" @click="$router.push(`/moment/${moment.id}`)">
            <!-- 用户名 -->
            <div class="moment-username">{{ moment.user.nickName }}</div>
            
            <!-- 文字内容 -->
            <div class="moment-text">{{ moment.content }}</div>
            
            <!-- 图片区域 -->
            <div v-if="moment.images?.length" 
                 :class="['moment-images', `grid-${Math.min(moment.images.length, 9)}`]">
              <div v-for="(img, index) in moment.images" 
                   :key="index" 
                   class="image-wrapper"
                   @click.stop="openViewer(moment.images, index)">
                <el-image 
                  :src="img" 
                  fit="cover"
                  :preview-src-list="moment.images"
                  :initial-index="index"
                />
              </div>
            </div>
            
            <!-- 时间和操作区域 -->
            <div class="moment-footer">
              <span class="moment-time">{{ formatTime(moment.createTime) }}</span>
              <div class="moment-actions">
                <el-button 
                  link
                  :class="{ 'liked': moment.islike }"
                  @click.stop="handleLike(moment.id)"
                >
                  <el-icon><Star /></el-icon>
                  <span>{{ moment.liked || 0 }}</span>
                </el-button>
                <el-button 
                  link
                  @click.stop="handleComment(moment)"
                >
                  <el-icon><Comment /></el-icon>
                </el-button>
              </div>
            </div>
            
            <!-- 点赞和评论区域 -->
            <div v-if="(moment.liked > 0 || moment.comments?.length > 0)" 
                 class="interaction-area"
                 @click.stop>
              <!-- 点赞列表 -->
              <div class="likes" v-if="moment.liked > 0">
                <el-button link type="primary" @click.stop="showLikesDialog(moment)">
                  <el-icon><Star /></el-icon>
                  <span v-if="moment.likes && moment.likes.length > 0">
                    <template v-if="moment.liked <= 5">
                      {{ moment.likes.map(like => like.nickName).join('、') }} 已点赞
                    </template>
                    <template v-else>
                      {{ moment.likes.slice(0, 5).map(like => like.nickName).join('、') }} 等{{ moment.liked }}人已点赞
                    </template>
                  </span>
                  <span v-else>
                    {{ moment.liked }}人已点赞
                  </span>
                </el-button>
              </div>
              
              <!-- 评论列表 -->
              <div v-if="moment.showComments" class="comments-section">
                <!-- 评论输入框 -->
                <div class="comment-input">
                  <el-input
                    v-model="commentContent"
                    placeholder="写下你的评论..."
                    :maxlength="200"
                    show-word-limit
                    @keyup.enter="submitComment(moment)"
                  >
                    <template #append>
                      <el-button @click="submitComment(moment)">发送</el-button>
                    </template>
                  </el-input>
                </div>
                
                <!-- 评论列表 -->
                <div v-if="moment.comments && moment.comments.length > 0" class="comments-list">
                  <div v-for="comment in moment.comments" :key="comment.id" class="comment-item">
                    <div class="comment-user">
                      <el-avatar :size="32" :src="comment.user?.avatarUrl || '/default-avatar.jpg'" />
                      <span class="username">{{ comment.user?.nickName || '未知用户' }}</span>
                      <span class="time">{{ formatTime(comment.createTime) }}</span>
                      <el-button 
                        v-if="comment.user?.id === userStore.currentUser?.id"
                        link 
                        type="danger" 
                        size="small"
                        @click.stop="handleDeleteComment(moment.id, comment.id)"
                      >
                        删除
                      </el-button>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                  </div>
                </div>
                <div v-else class="no-comments">
                  暂无评论，快来发表第一条评论吧！
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布朋友圈对话框 -->
    <el-dialog
      v-model="showPostDialog"
      title="发布朋友圈"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="newPost" class="post-form">
        <el-form-item>
          <el-input
            v-model="newPost.content"
            type="textarea"
            :rows="4"
            placeholder="分享新鲜事..."
          />
        </el-form-item>
        <el-form-item>
          <el-upload
            v-model:file-list="newPost.images"
            multiple
            :limit="9"
            accept="image/*"
            list-type="picture-card"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :auto-upload="true"
            :on-change="handleFileChange"
            :before-upload="beforeAvatarUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPostDialog = false">取消</el-button>
          <el-button type="primary" @click="handlePost">发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改背景对话框 -->
    <el-dialog
      v-model="showBackgroundDialog"
      title="修改背景"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-upload
        class="background-uploader"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        accept="image/*"
        :on-success="handleBackgroundUploadSuccess"
        :on-error="handleBackgroundUploadError"
        :before-upload="beforeAvatarUpload"
        :auto-upload="true"
      >
        <img v-if="backgroundPreview" :src="backgroundPreview" class="background-preview"/>
        <el-icon v-else class="background-uploader-icon"><Plus /></el-icon>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBackgroundDialog = false">取消</el-button>
          <el-button type="primary" @click="handleBackgroundUpdate">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改头像对话框 -->
    <el-dialog
      v-model="showAvatarDialog"
      title="修改头像"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-upload
        class="avatar-uploader"
        :action="uploadUrl"
        :show-file-list="false"
        :on-success="handleAvatarUploadSuccess"
        :on-error="handleAvatarUploadError"
        :before-upload="beforeAvatarUpload"
        :auto-upload="true"
        :headers="uploadHeaders"
      >
        <img v-if="avatarPreview" :src="avatarPreview" class="avatar" />
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarDialog = false">取消</el-button>
          <el-button type="primary" @click="handleAvatarUpdate">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 评论对话框 -->
    <el-dialog
      v-model="showCommentDialog"
      title="发表评论"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="newComment" class="comment-form">
        <el-form-item>
          <el-input
            v-model="newComment.content"
            type="textarea"
            :rows="3"
            placeholder="说点什么..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCommentDialog = false">取消</el-button>
          <el-button type="primary" @click="submitComment">发送</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useMomentStore } from '@/stores/moment'
import { useCommentStore } from '@/stores/comment'
import { useTokenStore } from '@/stores/token'
import { Star, Comment, User, Lock, Plus, More, SwitchButton, ChatDotRound } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/date'
import { ElMessage, ElLoading } from 'element-plus'
import { useRouter } from 'vue-router'
import { userBackgroundUpdateService, userInfoService, getUserProfileService } from '@/api/user'
import { CommentAPI } from '@/api/comment'

const router = useRouter()
const userStore = useUserStore()
const momentStore = useMomentStore()
const commentStore = useCommentStore()
const tokenStore = useTokenStore()

// 发布朋友圈相关
const showPostDialog = ref(false)
const newPost = ref({
  content: '',
  images: []
})

// 修改背景相关
const showBackgroundDialog = ref(false)
const backgroundPreview = ref('')
const backgroundFile = ref(null)

// 修改头像相关
const showAvatarDialog = ref(false)
const avatarPreview = ref('')
const avatarFile = ref(null)

// 评论相关
const showCommentDialog = ref(false)
const newComment = ref({
  content: '',
  momentId: null
})
const commentContent = ref('')

// 上传相关配置
const uploadUrl = '/upload'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('未登录或登录已过期，请重新登录')
    router.push('/login')
    return {}
  }
  return {
    Authorization: `Bearer ${token}`
  }
})

// 计算属性
const headerStyle = computed(() => ({
  backgroundImage: `url(${userStore.currentUser?.backgroundUrl || '/default-bg.jpg'})`
}))

// 添加一个用于调试的计算属性
const debugLikes = computed(() => {
  if (momentStore.moments) {
    momentStore.moments.forEach(moment => {
      console.log('Debug - moment:', {
        id: moment.id,
        liked: moment.liked,
        likesCount: moment.likes?.length,
        likes: moment.likes
      })
    })
  }
  return null
})

// 生命周期钩子
onMounted(async () => {
  try {
    // 使用 userInfoService 获取当前用户信息
    const response = await userInfoService()
    if (response.code === 1) {
      userStore.currentUser = response.data
    } else {
      throw new Error(response.msg || '获取用户信息失败')
    }
    // 获取朋友圈动态
    await momentStore.fetchMoments()
  } catch (error) {
    console.error('初始化用户信息失败:', error)
    ElMessage.error('初始化用户信息失败')
  }
})

// 方法
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 处理点赞
const handleLike = async (momentId) => {
  try {
    await momentStore.toggleLike(momentId)
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理评论
const handleComment = async (moment) => {
  // 切换评论区域的显示状态
  moment.showComments = !moment.showComments
  
  // 如果显示评论区域且还没有加载过评论，则加载评论
  if (moment.showComments && (!moment.comments || moment.comments.length === 0)) {
    try {
      const response = await commentStore.getComments(moment.id)
      console.log('评论响应:', response) // 添加调试日志
      
      // 检查响应是否为数组
      if (Array.isArray(response)) {
        // 按创建时间升序排序，最早的评论在最上面
        moment.comments = response.sort((a, b) => 
          new Date(a.createTime) - new Date(b.createTime)
        )
        console.log('处理后的评论:', moment.comments) // 添加调试日志
      } else {
        moment.comments = []
        ElMessage.error('获取评论失败：响应格式错误')
      }
    } catch (error) {
      moment.comments = []
      console.error('获取评论失败:', error)
      ElMessage.error('获取评论失败')
    }
  }
}

// 提交评论
const submitComment = async (moment) => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  
  if (!moment || !moment.id) {
    ElMessage.error('动态信息不完整，无法发表评论')
    return
  }
  
  try {
    const response = await CommentAPI.createComment(moment.id, {
      content: commentContent.value.trim(),
      momentId: moment.id
    })
    console.log('评论提交响应:', response) // 添加调试日志
    
    // 检查响应是否为对象
    if (response && typeof response === 'object' && response.code === 1) {
      // 添加新评论到列表末尾
      if (!moment.comments) {
        moment.comments = []
      }
      // 确保新评论包含完整的用户信息
      const newComment = {
        id: response.data.id, // 使用后端返回的真实 ID
        content: commentContent.value.trim(),
        createTime: response.data.createTime || new Date().toISOString(),
        user: {
          id: userStore.currentUser.id,
          nickName: userStore.currentUser.nickName,
          avatarUrl: userStore.currentUser.avatarUrl
        }
      }
      moment.comments.push(newComment)
      // 重新排序评论列表，确保按时间顺序显示
      moment.comments.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
      commentContent.value = ''
      ElMessage.success('评论成功')
    } else {
      ElMessage.error('评论失败：响应格式错误')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error(error.message || '评论失败')
  }
}

// 删除评论
const handleDeleteComment = async (momentId, commentId) => {
  try {
    console.log('开始删除评论:', { momentId, commentId }) // 添加调试日志
    const response = await CommentAPI.deleteComment(momentId, commentId)
    console.log('删除评论响应:', response) // 添加调试日志
    
    if (response && response.code === 1) {
      // 找到对应的动态并更新评论列表
      const moment = momentStore.moments.find(m => m.id === momentId)
      if (moment && moment.comments) {
        moment.comments = moment.comments.filter(c => c.id !== commentId)
        ElMessage.success('删除评论成功')
      } else {
        console.error('未找到对应的动态或评论列表:', { momentId, moment })
        ElMessage.error('删除评论失败：未找到对应的动态')
      }
    } else {
      console.error('删除评论失败:', response)
      ElMessage.error(response?.msg || '删除评论失败')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error(error.message || '删除评论失败')
  }
}

// 修改发布动态的处理方法
const handlePost = async () => {
  if (!newPost.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  try {
    // 获取所有已上传图片的URL
    const imageUrls = newPost.value.images
      .filter(file => file.response?.data || file.url)
      .map(file => file.response?.data || file.url)
    
    console.log('发布动态:', {
      content: newPost.value.content,
      images: imageUrls
    })
    
    // 调用 store 的 createMoment 方法
    const response = await momentStore.createMoment({
      content: newPost.value.content,
      images: imageUrls
    })

    if (response.code === 1) {
      showPostDialog.value = false
      newPost.value = { content: '', images: [] }
      ElMessage.success('发布成功')
    } else {
      ElMessage.error(response.msg || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error(error.message || '发布失败')
  }
}

const openViewer = (images, index) => {
  // 使用 el-image 的预览功能，不需要额外实现
}

const handleBackgroundChange = async (file) => {
  // 只创建预览，不发送请求
  backgroundPreview.value = URL.createObjectURL(file.raw)
  backgroundFile.value = file.raw
}

const handleBackgroundUpdate = async () => {
  if (!backgroundFile.value) {
    ElMessage.warning('请选择背景图片')
    return
  }

  try {
    // 更新用户背景
    const response = await userBackgroundUpdateService(backgroundFile.value)
    // 检查响应状态
    if (response.code === 1) {
      // 更新用户信息
      userStore.currentUser.backgroundUrl = backgroundFile.value
      // 关闭对话框
      showBackgroundDialog.value = false
      // 清理预览
      backgroundPreview.value = ''
      backgroundFile.value = null
      // 刷新页面
      window.location.reload()
      ElMessage.success('背景修改成功')
    } else {
      ElMessage.error(response.msg || '背景修改失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('背景修改失败')
  }
}

// 处理上传成功
const handleUploadSuccess = (response, file) => {
  console.log('上传响应:', response)
  if (response.code === 1) {
    // 更新文件的 url 属性
    file.url = response.data
    // 更新文件列表中的文件
    const index = newPost.value.images.findIndex(f => f.uid === file.uid)
    if (index > -1) {
      newPost.value.images[index] = {
        ...newPost.value.images[index],
        url: response.data,
        response: response
      }
    }
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '图片上传失败')
  }
}

// 处理上传失败
const handleUploadError = (error) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败')
}

// 处理文件变化
const handleFileChange = (file, fileList) => {
  console.log('文件变化:', file, fileList)
  // 更新文件列表，保留已上传文件的 url
  newPost.value.images = fileList.map(f => ({
    ...f,
    url: f.response?.data || f.url
  }))
}

// 处理背景图片上传成功
const handleBackgroundUploadSuccess = (response, file) => {
  console.log('背景上传响应:', response)
  if (response.code === 1) {
    backgroundFile.value = response.data
    backgroundPreview.value = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '图片上传失败')
  }
}

// 处理背景图片上传失败
const handleBackgroundUploadError = (error) => {
  console.error('背景上传失败:', error)
  ElMessage.error('图片上传失败')
}

// 处理头像上传成功
const handleAvatarUploadSuccess = (response, file) => {
  console.log('头像上传响应:', response)
  if (response.code === 1) {
    avatarFile.value = response.data
    avatarPreview.value = response.data
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

// 处理头像更新
const handleAvatarUpdate = async () => {
  if (!avatarFile.value) {
    ElMessage.warning('请选择头像')
    return
  }

  try {
    // 显示加载中
    const loading = ElLoading.service({
      lock: true,
      text: '正在更新头像，请稍候...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    // 更新用户头像
    const response = await userAvatarUpdateService(avatarFile.value)
    
    // 关闭加载中
    loading.close()
    
    // 检查响应状态
    if (response.code === 1) {
      // 更新用户信息
      userStore.currentUser.avatarUrl = avatarFile.value
      // 关闭对话框
      showAvatarDialog.value = false
      // 清理预览
      avatarPreview.value = ''
      avatarFile.value = null
      // 刷新页面
      window.location.reload()
      ElMessage.success('头像修改成功')
    } else {
      ElMessage.error(response.msg || '头像修改失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接后重试')
    } else {
      ElMessage.error('头像修改失败')
    }
  }
}

const beforeAvatarUpload = (file) => {
  // 实现头像上传前的验证逻辑
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

// 处理用户头像下拉菜单
const handleUserCommand = async ({ type, userId }) => {
  if (type === 'profile') {
    try {
      // 获取用户信息
      const response = await getUserProfileService(userId)
      if (response.code === 1) {
        // 将用户信息存储到 localStorage，以便在 UserProfileView 中使用
        localStorage.setItem('userProfile', JSON.stringify(response.data))
        // 跳转到用户信息页面
        router.push(`/user/${userId}/profile`)
      } else {
        ElMessage.error(response.msg || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    }
  }
}

const showDropdown = (event, moment) => {
  if (event && event.stopPropagation) {
    event.stopPropagation()
  }
}

const hideDropdown = () => {
  dropdownVisible.value = false
  currentMoment.value = null
}

onMounted(() => {
  document.addEventListener('click', hideDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', hideDropdown)
})

const handleAvatarClick = (userId) => {
  // 如果是当前用户，显示下拉菜单
  if (userId === userStore.userInfo.id) {
    return
  }
  // 如果是其他用户，跳转到用户信息页面
  router.push(`/user/${userId}/profile`)
}

// 更新头像成功后的处理
const handleAvatarSuccess = async (response) => {
  if (response.code === 1) {
    try {
      await userStore.updateAvatar(response.data)
      ElMessage.success('头像更新成功')
    } catch (error) {
      console.error('更新头像失败:', error)
      ElMessage.error('更新头像失败')
    }
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 更新背景图片成功后的处理
const handleBackgroundSuccess = async (response) => {
  if (response.code === 1) {
    try {
      await userStore.updateBackground(response.data)
      ElMessage.success('背景图片更新成功')
    } catch (error) {
      console.error('更新背景图片失败:', error)
      ElMessage.error('更新背景图片失败')
    }
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}
</script>

<style scoped>
.moments-container {
  min-height: 100vh;
  background-color: #f7f7f7;
}

.fixed-user-menu {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.2s;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-avatar:hover {
  transform: scale(1.1);
}

.moments-header {
  height: 300px;
  position: relative;
  margin-bottom: 10px;
  background-size: cover;
  background-position: center;
  cursor: pointer;
  transition: opacity 0.3s;
}

.moments-header:hover {
  opacity: 0.9;
}

.cover-image {
  height: 100%;
  position: relative;
}

.user-info {
  position: absolute;
  bottom: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.nickname {
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  font-size: 18px;
}

.header-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.moments-list {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 15px;
}

.moment-item {
  position: relative;
  padding: 15px;
  border-bottom: 1px solid #eee;
  background: #fff;
  margin-bottom: 10px;
  border-radius: 8px;
}

.moment-avatar {
  position: absolute;
  top: 15px;
  left: 15px;
  z-index: 1;
}

.moment-content {
  margin-left: 55px; /* 头像宽度 + 间距 */
  width: calc(100% - 55px);
}

.moment-username {
  font-weight: bold;
  margin-bottom: 5px;
}

.moment-text {
  margin-bottom: 10px;
  line-height: 1.5;
}

.moment-images {
  display: grid;
  gap: 5px;
  margin-bottom: 10px;
}

.grid-1 { grid-template-columns: repeat(1, 1fr); }
.grid-2, .grid-4 { grid-template-columns: repeat(2, 1fr); }
.grid-3, .grid-5, .grid-6, .grid-7, .grid-8, .grid-9 { grid-template-columns: repeat(3, 1fr); }

.image-wrapper {
  aspect-ratio: 1;
  overflow: hidden;
}

.el-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.moment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  color: #999;
  font-size: 14px;
}

.moment-actions {
  display: flex;
  gap: 20px;
}

.moment-actions .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  height: auto;
}

.moment-actions .el-button .el-icon {
  font-size: 16px;
}

.moment-actions .liked {
  color: #e64340;
}

.interaction-area {
  margin-top: 10px;
  background: #f7f7f7;
  padding: 10px;
  border-radius: 4px;
}

.likes {
  color: #576b95;
  margin-bottom: 5px;
  display: block;
}

.likes .el-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #576b95;
}

.likes .el-button .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.comments-section {
  margin-top: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.comment-input {
  margin-bottom: 16px;
}

.comments-list {
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-user {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-user .username {
  margin-left: 8px;
  font-weight: 500;
  color: #333;
}

.comment-user .time {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}

.comment-content {
  margin-left: 40px;
  color: #333;
  line-height: 1.5;
}

.comment-actions {
  margin-left: 40px;
  margin-top: 8px;
}

/* 发布朋友圈对话框样式 */
.post-form {
  margin-top: 20px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.background-uploader {
  width: 100%;
  height: 300px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.background-uploader:hover {
  border-color: var(--el-color-primary);
}

.background-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.background-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 修改头像对话框样式 */
.avatar-uploader {
  width: 100%;
  height: 300px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.comment-form {
  margin-top: 20px;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 20px 0;
  font-size: 14px;
}

.clickable-avatar {
  cursor: pointer;
  transition: transform 0.2s;
}

.clickable-avatar:hover {
  transform: scale(1.1);
}
</style>