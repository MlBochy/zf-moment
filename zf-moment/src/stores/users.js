import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: {
      id: null,
      username: '',
      nickName: '',
      avatarUrl: '/default-avatar.jpg',
      backgroundUrl: '/default-bg.jpg',
      bio: ''
    }
  }),
  actions: {
    register(userData) {
      this.currentUser = {
        ...this.currentUser,
        ...userData,
        id: Date.now()
      }
      localStorage.setItem('user', JSON.stringify(this.currentUser))
    },
    login(userData) {
      this.currentUser = {
        ...this.currentUser,
        ...userData
      }
      localStorage.setItem('user', JSON.stringify(this.currentUser))
    },
    logout() {
      this.currentUser = {
        id: null,
        username: '',
        nickName: '',
        avatarUrl: '/default-avatar.jpg',
        backgroundUrl: '/default-bg.jpg',
        bio: ''
      }
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },
    updateUserInfo(userInfo) {
      this.currentUser = {
        ...this.currentUser,
        ...userInfo
      }
      localStorage.setItem('user', JSON.stringify(this.currentUser))
    }
  }
})