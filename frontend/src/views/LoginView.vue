<script setup>
import {computed, reactive, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {loginUser, registerUser} from '../api/auth'
import {setProfile} from '../stores/session'
import {resolveHomeRoute} from '../router'

const route = useRoute()
const router = useRouter()
const mode = ref('login')
const loading = ref(false)
const message = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  nickname: '',
  email: '',
  mobile: '',
  password: ''
})

const submitLabel = computed(() => {
  if (loading.value) {
    return '提交中...'
  }
  return mode.value === 'login' ? '进入控制台' : '创建并进入'
})

async function submit() {
  loading.value = true
  message.value = ''

  try {
    const data = mode.value === 'login'
        ? await loginUser(loginForm)
        : await registerUser(registerForm)
    setProfile(data.profile)
    router.replace(route.query.redirect || resolveHomeRoute())
  } catch (error) {
    message.value = error.message || '认证失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-scene">
    <section class="auth-hero">
      <p class="eyebrow">抽奖平台入口</p>
      <h1>幸运抽奖台</h1>
      <p class="hero-copy">
        登录后进入抽奖、奖池和账号中心。系统会根据你的角色自动生成可访问菜单，未登录用户会被留在当前入口页。
      </p>
      <div class="hero-badges">
        <span>统一网关鉴权</span>
        <span>角色菜单</span>
        <span>动态路由</span>
      </div>
    </section>

    <section class="auth-form-panel">
      <div class="auth-switcher">
        <button :class="{ active: mode === 'login' }" class="tab-btn" @click="mode = 'login'">登录</button>
        <button :class="{ active: mode === 'register' }" class="tab-btn" @click="mode = 'register'">注册</button>
      </div>

      <div class="auth-form-card">
        <template v-if="mode === 'login'">
          <label class="field-label">用户名</label>
          <input v-model.trim="loginForm.username" class="user-input" placeholder="请输入用户名" type="text"/>
          <label class="field-label">密码</label>
          <input v-model.trim="loginForm.password" class="user-input" placeholder="请输入密码" type="password"/>
        </template>

        <template v-else>
          <label class="field-label">用户名</label>
          <input v-model.trim="registerForm.username" class="user-input" placeholder="4-32 位字母数字下划线"
                 type="text"/>
          <label class="field-label">昵称</label>
          <input v-model.trim="registerForm.nickname" class="user-input" placeholder="请输入展示昵称" type="text"/>
          <label class="field-label">邮箱</label>
          <input v-model.trim="registerForm.email" class="user-input" placeholder="请输入邮箱" type="email"/>
          <label class="field-label">手机号</label>
          <input v-model.trim="registerForm.mobile" class="user-input" placeholder="可选" type="text"/>
          <label class="field-label">密码</label>
          <input v-model.trim="registerForm.password" class="user-input" placeholder="至少 8 位" type="password"/>
        </template>

        <button :disabled="loading" class="primary-btn auth-submit" @click="submit">{{ submitLabel }}</button>
        <p v-if="message" class="page-message">{{ message }}</p>
      </div>
    </section>
  </div>
</template>
