<template>
  <div class="profile-container">
    <div class="back-button">
      <el-button 
        @click="goBack" 
        class="back-button-style"
      >
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div class="profile-header" :style="headerStyle">
      <div class="user-info">
        <el-avatar 
          :size="100" 
          :src="userInfo?.avatarUrl || '/default-avatar.jpg'"
          class="profile-avatar"
        />
        <div class="user-details">
          <h2 class="nickname">{{ userInfo?.nickName || '用户昵称' }}</h2>
          <p class="bio">{{ userInfo?.bio || '这个人很懒，什么都没写~' }}</p>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <div class="stats">
        <div class="stat-item">
          <span class="count">{{ userInfo?.momentCount || 0 }}</span>
          <span class="label">动态</span>
        </div>
      </div>

      <div class="moments-list">
        <div v-loading="loading">
          <div v-for="moment in moments" :key="moment.id" class="moment-item">
            <!-- 用户头像 -->
            <div class="moment-avatar">
              <el-avatar 
                :size="40" 
                :src="moment.user?.avatarUrl || '/default-avatar.jpg'"
                class="clickable-avatar"
              />
            </div>
            
            <!-- 动态内容区域 -->
            <div class="moment-content">
              <!-- 用户名 -->
              <div class="moment-username">{{ moment.user?.nickName || '用户昵称' }}</div>
              
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
                
                <div class="likes" v-if="moment.liked > 0">
                  <el-button link type="primary" @click.stop="showLikes(moment)">
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
                        <span class="username">{{ comment.user?.nickName || comment.user?.username }}</span>
                        <span class="time">{{ formatTime(comment.createTime) }}</span>
                        <el-button 
                          v-if="comment.userId === userStore.currentUser?.id"
                          link 
                          type="danger" 
                          size="small"
                          @click.stop="deleteComment(moment, comment)"
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatTime } from '@/utils/date'
import { getUserProfileService } from '@/api/user'
import { ArrowLeft, Star, Comment } from '@element-plus/icons-vue'
import { useMomentStore } from '@/stores/moment'
import { useCommentStore } from '@/stores/comment'
import { useUserStore } from '@/stores/user'
import { useTokenStore } from '@/stores/token'
import { 
  userBackgroundUpdateService, 
  userInfoService, 
  likeUserMomentService
} from '@/api/user'
import { CommentAPI } from '@/api/comment'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userId = route.params.id
const userInfo = ref({})
const moments = ref([])
const loading = ref(false)
const momentStore = useMomentStore()
const commentStore = useCommentStore()
const userStore = useUserStore()
const commentContent = ref('')
const showLikesDialog = ref(false)
const currentLikes = ref([])

const headerStyle = computed(() => ({
  backgroundImage: `url(${userInfo.value?.backgroundUrl || '/default-bg.jpg'})`
}))

const goBack = () => {
  router.back()
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    loading.value = true
    const response = await userStore.fetchUserProfile(userId)
    if (response.code === 1) {
      userInfo.value = response.data
    } else {
      ElMessage.error(response.msg || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 获取用户动态列表
const fetchMoments = async () => {
  try {
    loading.value = true
    const response = await userStore.fetchUserMoments(userId)
    if (response.code === 1) {
      // 确保每个动态都有必要的字段
      moments.value = response.data.map(moment => ({
        ...moment,
        likes: moment.likes || [],
        islike: moment.islike || false,
        liked: moment.liked || 0,
        showComments: false,
        comments: moment.comments || [],
        user: {
          ...moment.user,
          nickName: moment.user?.nickName || '用户昵称',
          avatarUrl: moment.user?.avatarUrl || '/default-avatar.jpg'
        }
      }))
      console.log('Fetched moments:', moments.value)
    } else {
      ElMessage.error(response.msg || '获取动态列表失败')
    }
  } catch (error) {
    console.error('获取动态列表失败:', error)
    ElMessage.error('获取动态列表失败')
  } finally {
    loading.value = false
  }
}

// 处理点赞
const handleLike = async (momentId) => {
  try {
    const response = await likeUserMomentService(userId, momentId)
    if (response.code === 1) {
      // 更新本地状态
      const moment = moments.value.find(m => m.id === momentId)
      if (moment) {
        moment.islike = !moment.islike
        moment.liked = moment.islike ? (moment.liked || 0) + 1 : Math.max(0, (moment.liked || 0) - 1)
      }
      ElMessage.success(moment.islike ? '点赞成功' : '取消点赞成功')
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理评论
const handleComment = async (moment) => {
  moment.showComments = !moment.showComments
  if (moment.showComments && (!moment.comments || moment.comments.length === 0)) {
    try {
      const response = await CommentAPI.getUserMomentComments(userId, moment.id)
      console.log('评论响应:', response) // 添加调试日志
      
      // 检查响应格式
      if (response && response.code === 1 && Array.isArray(response.data)) {
        // 确保每个评论都有必要的字段
        moment.comments = response.data.map(comment => ({
          ...comment,
          user: {
            ...comment.user,
            id: comment.user?.id || comment.userId, // 添加 userId 作为备选
            nickName: comment.user?.nickName || comment.user?.username,
            avatarUrl: comment.user?.avatarUrl || '/default-avatar.jpg'
          }
        })).sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
        console.log('处理后的评论:', moment.comments) // 添加调试日志
      } else {
        moment.comments = []
        ElMessage.error(response?.msg || '获取评论失败：响应格式错误')
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
  
  try {
    const response = await CommentAPI.createUserMomentComment(
      userId,
      moment.id,
      commentContent.value.trim()
    )
    console.log('评论提交响应:', response) // 添加调试日志
    
    // 检查响应格式
    if (response && response.code === 1 && response.data) {
      if (!moment.comments) {
        moment.comments = []
      }
      // 确保新评论有必要的字段
      const newComment = {
        ...response.data,
        user: {
          ...response.data.user,
          id: response.data.user?.id || response.data.userId, // 添加 userId 作为备选
          nickName: response.data.user?.nickName || response.data.user?.username,
          avatarUrl: response.data.user?.avatarUrl || '/default-avatar.jpg'
        }
      }
      moment.comments.push(newComment)
      moment.comments.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
      commentContent.value = ''
      ElMessage.success('评论成功')
    } else {
      ElMessage.error(response?.msg || '评论失败：响应格式错误')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error(error.message || '评论失败')
  }
}

// 删除评论
const deleteComment = async (moment, comment) => {
  try {
    const response = await CommentAPI.deleteComment(moment.id, comment.id)
    console.log('删除评论响应:', response) // 添加调试日志
    
    if (response && response.code === 1) {
      moment.comments = moment.comments.filter(c => c.id !== comment.id)
      ElMessage.success('删除评论成功')
    } else {
      ElMessage.error(response?.msg || '删除评论失败')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('删除评论失败')
  }
}

// 显示点赞对话框
const showLikes = (moment) => {
  currentLikes.value = moment.likes || []
  showLikesDialog.value = true
}

// 打开图片预览
const openViewer = (images, index) => {
  // 使用 el-image 的预览功能，不需要额外实现
}

// 在组件挂载时获取数据
onMounted(async () => {
  await fetchUserInfo()
  await fetchMoments()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background-color: #f7f7f7;
}

.profile-header {
  height: 300px;
  position: relative;
  background-size: cover;
  background-position: center;
}

.user-info {
  position: absolute;
  bottom: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  border: 4px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.user-details {
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.nickname {
  font-size: 24px;
  margin: 0 0 8px 0;
}

.bio {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.profile-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.stats {
  display: flex;
  justify-content: space-around;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.count {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.label {
  color: #666;
  font-size: 14px;
}

.moments-list {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.moment-item {
  position: relative;
  padding: 15px;
  border-bottom: 1px solid #eee;
  background: #fff;
  margin-bottom: 10px;
  border-radius: 8px;
}

.moment-item:last-child {
  border-bottom: none;
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

.comments-section {
  margin-top: 10px;
}

.comment-input {
  margin-bottom: 10px;
}

.comments-list {
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-weight: bold;
  font-size: 14px;
}

.time {
  color: #999;
  font-size: 12px;
}

.comment-content {
  margin: 5px 0 0 40px;
  font-size: 14px;
}

.comment-actions {
  margin-top: 5px;
  margin-left: 40px;
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

.back-button {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 100;
}

.back-button-style {
  background-color: transparent;
  border: none;
  color: #fff;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
}

.back-button-style:hover {
  background-color: rgba(255, 255, 255, 0.1);
}
</style> 