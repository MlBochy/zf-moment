<template>
  <div class="moment-detail-container">
    <div v-if="moment" class="moment-detail">
      <!-- 动态内容 -->
      <div class="moment-item">
        <!-- 头部区域：包含返回按钮和更多操作 -->
        <div class="moment-header">
          <el-button link @click="$router.back()" class="back-button">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          
          <!-- 更多操作按钮 -->
          <el-dropdown v-if="moment.user?.id === userStore.currentUser?.id" @command="handleCommand" trigger="click">
            <el-button link class="more-button" @click.stop>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 用户头像 -->
        <div class="moment-avatar">
          <div class="user-info">
            <el-dropdown trigger="click" @command="handleUserCommand">
              <el-avatar 
                :size="40" 
                :src="moment.user?.avatarUrl || '/default-avatar.jpg'"
                class="clickable-avatar"
              />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ type: 'profile', userId: moment.user?.id }">
                    查看用户信息
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <div class="user-details">
              <span class="username">{{ moment.user?.nickName || '用户昵称' }}</span>
              <span class="time">{{ formatTime(moment.createTime) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 动态内容区域 -->
        <div class="moment-content">
          <!-- 文字内容 -->
          <div class="moment-text">{{ moment.content }}</div>
          
          <!-- 图片区域 -->
          <div v-if="moment.images?.length" 
               :class="['moment-images', `grid-${Math.min(moment.images.length, 9)}`]">
            <div v-for="(img, index) in moment.images" 
                 :key="index" 
                 class="image-wrapper"
                 @click="openViewer(moment.images, index)">
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
            <div class="moment-actions">
              <el-button 
                link
                :class="{ 'liked': moment.islike }"
                @click="handleLike(moment.id)"
              >
                <el-icon><Star /></el-icon>
                <span>{{ moment.liked || 0 }}</span>
              </el-button>
              <el-button 
                link
                @click="handleComment(moment)"
              >
                <el-icon><Comment /></el-icon>
              </el-button>
            </div>
          </div>
          
          <!-- 点赞和评论区域 -->
          <div v-if="(moment.liked > 0 || moment.comments?.length > 0)" class="interaction-area">
            <!-- 点赞列表 -->
            <div class="likes" v-if="moment.liked > 0">
              <el-button link type="primary" @click="showLikesDialog(moment)">
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
            <div class="comments-section">
              <!-- 评论输入框 -->
              <div class="comment-input">
                <el-input
                  v-model="commentContent"
                  placeholder="写下你的评论..."
                  :maxlength="200"
                  show-word-limit
                  @keyup.enter="submitComment(moment.id)"
                >
                  <template #append>
                    <el-button @click="submitComment(moment.id)">发送</el-button>
                  </template>
                </el-input>
              </div>
              
              <!-- 评论列表 -->
              <div v-if="moment.comments && moment.comments.length > 0" class="comments-list">
                <div v-for="comment in moment.comments" :key="comment.id" class="comment-item">
                  <div class="comment-user">
                    <el-avatar :size="32" :src="comment.user.avatarUrl" />
                    <span class="username">{{ comment.user.nickName }}</span>
                    <span class="time">{{ formatTime(comment.createTime) }}</span>
                    <el-button 
                      v-if="comment.user.id === userStore.currentUser?.id"
                      link 
                      type="danger" 
                      size="small"
                      @click.stop="handleDeleteComment(comment.id)"
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

    <!-- 编辑动态对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑动态"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" ref="editFormRef">
        <el-form-item prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="4"
            placeholder="分享新鲜事..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <!-- 图片上传区域 -->
        <el-form-item>
          <el-upload
            v-model:file-list="editForm.fileList"
            action="/upload"
            list-type="picture-card"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/users'
import { useMomentStore } from '@/stores/moment'
import { useCommentStore } from '@/stores/comment'
import { Star, Comment, ArrowLeft, More, Plus } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/date'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CommentAPI } from '@/api/comment'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const momentStore = useMomentStore()
const commentStore = useCommentStore()

const moment = ref(null)
const commentContent = ref('')

// 编辑相关
const editDialogVisible = ref(false)
const editForm = ref({
  content: '',
  fileList: [],
  images: []
})

// 上传相关配置
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

// 获取动态详情
const fetchMomentDetail = async () => {
  try {
    const response = await momentStore.getMomentDetail(route.params.id)
    if (response.code === 1) {
      moment.value = response.data
      // 获取评论列表
      const comments = await commentStore.getComments(moment.value.id)
      moment.value.comments = comments || []
    } else {
      ElMessage.error('获取动态详情失败')
    }
  } catch (error) {
    console.error('获取动态详情失败:', error)
    ElMessage.error('获取动态详情失败')
  }
}

// 处理点赞
const handleLike = async (momentId) => {
  try {
    await momentStore.toggleLike(momentId)
    // 重新获取动态详情以更新点赞状态
    await fetchMomentDetail()
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  
  try {
    const response = await CommentAPI.createComment(moment.value.id, {
      content: commentContent.value.trim(),
      momentId: moment.value.id
    })
    console.log('评论提交响应:', response) // 添加调试日志
    
    // 检查响应是否为对象
    if (response && typeof response === 'object' && response.code === 1) {
      // 添加新评论到列表末尾
      if (!moment.value.comments) {
        moment.value.comments = []
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
      moment.value.comments.push(newComment)
      // 重新排序评论列表，确保按时间顺序显示
      moment.value.comments.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
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
const handleDeleteComment = async (commentId) => {
  try {
    console.log('开始删除评论:', { momentId: moment.value.id, commentId }) // 添加调试日志
    const response = await CommentAPI.deleteComment(moment.value.id, commentId)
    console.log('删除评论响应:', response) // 添加调试日志
    
    if (response && response.code === 1) {
      // 更新评论列表
      moment.value.comments = moment.value.comments.filter(c => c.id !== commentId)
      ElMessage.success('删除评论成功')
    } else {
      console.error('删除评论失败:', response)
      ElMessage.error(response?.msg || '删除评论失败')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error(error.message || '删除评论失败')
  }
}

// 打开编辑对话框
const openEditDialog = () => {
  editForm.value.content = moment.value.content
  editForm.value.images = moment.value.images || []
  editForm.value.fileList = (moment.value.images || []).map((url, index) => ({
    name: `image-${index}`,
    url
  }))
  editDialogVisible.value = true
}

// 处理图片上传成功
const handleUploadSuccess = (response, file) => {
  if (response.code === 1) {
    editForm.value.images.push(response.data)
  } else {
    ElMessage.error('上传失败')
  }
}

// 处理图片移除
const handleRemove = (file) => {
  const index = editForm.value.fileList.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    editForm.value.images.splice(index, 1)
  }
}

// 上传前检查
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 提交编辑
const submitEdit = async () => {
  if (!editForm.value.content.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }

  try {
    await momentStore.updateMoment(moment.value.id, {
      content: editForm.value.content.trim(),
      images: editForm.value.images
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    // 重新获取动态详情
    await fetchMomentDetail()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

// 更新处理更多操作菜单的选择
const handleCommand = async (command) => {
  if (command === 'delete') {
    // 删除确认
    ElMessageBox.confirm(
      '确定要删除这条动态吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(async () => {
      try {
        await momentStore.deleteMoment(moment.value.id)
        ElMessage.success('删除成功')
        router.push('/') // 删除成功后返回首页
      } catch (error) {
        ElMessage.error(error.message || '删除失败')
      }
    }).catch(() => {
      // 取消删除
    })
  } else if (command === 'edit') {
    openEditDialog()
  }
}

// 在 script setup 中添加处理函数
const handleUserCommand = ({ type, userId }) => {
  if (type === 'profile') {
    router.push(`/user/${userId}/profile`)
  }
}

onMounted(() => {
  fetchMomentDetail()
})
</script>

<style scoped>
.moment-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
  font-size: 16px;
}

.moment-item {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.moment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.more-button {
  font-size: 20px;
}

/* 复用 HomeView.vue 中的其他样式 */
.moment-content {
  flex: 1;
}

.moment-username {
  font-weight: bold;
  margin-bottom: 10px;
  font-size: 16px;
}

.moment-text {
  margin-bottom: 15px;
  line-height: 1.6;
  font-size: 16px;
}

.moment-images {
  display: grid;
  gap: 10px;
  margin-bottom: 15px;
}

.grid-1 { grid-template-columns: repeat(1, 1fr); }
.grid-2, .grid-4 { grid-template-columns: repeat(2, 1fr); }
.grid-3, .grid-5, .grid-6, .grid-7, .grid-8, .grid-9 { grid-template-columns: repeat(3, 1fr); }

.image-wrapper {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 4px;
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
  margin-top: 15px;
  color: #999;
}

.moment-actions {
  display: flex;
  gap: 20px;
}

.interaction-area {
  margin-top: 20px;
  background: #f7f7f7;
  padding: 15px;
  border-radius: 8px;
}

.likes {
  margin-bottom: 15px;
}

.comments-section {
  margin-top: 20px;
}

.comment-input {
  margin-bottom: 20px;
}

.comments-list {
  max-height: none;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-content {
  margin: 10px 0 0 42px;
}

.no-comments {
  text-align: center;
  padding: 20px;
  color: #999;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

.clickable-avatar {
  cursor: pointer;
  transition: transform 0.2s;
}

.clickable-avatar:hover {
  transform: scale(1.1);
}

.moment-avatar {
  display: flex;
  align-items: center;
}
</style> 