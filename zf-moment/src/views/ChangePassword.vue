<template>
    <div class="password-page">
      <el-page-header @back="$router.go(-1)">
        <template #content>
          <span class="title">修改密码</span>
        </template>
      </el-page-header>
  
      <div class="content">
        <el-form label-width="100px">
          <el-form-item label="原密码">
            <el-input type="password" v-model="form.oldPassword"/>
          </el-form-item>
          <el-form-item label="新密码">
            <el-input type="password" v-model="form.newPassword"/>
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input type="password" v-model="form.confirmPassword"/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submit">提交修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { reactive } from 'vue'
  import { ElMessage } from 'element-plus'
  
  const form = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  
  const submit = () => {
    if (form.newPassword !== form.confirmPassword) {
      ElMessage.error('两次密码输入不一致')
      return
    }
    ElMessage.success('密码修改成功')
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
  }
  </script>
  
  <style scoped>
  .password-page {
    max-width: 600px;
    margin: 20px auto;
    padding: 20px;
  }
  
  .content {
    margin-top: 30px;
    background: #fff;
    padding: 20px;
    border-radius: 8px;
  }
  </style>