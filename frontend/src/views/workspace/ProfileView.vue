<script setup>
import {useRouter} from 'vue-router'
import {sessionState, signOut} from '../../stores/session'

const router = useRouter()

async function logout() {
  await signOut()
  router.replace('/login')
}
</script>

<template>
  <section class="panel wide-panel">
    <div class="section-header">
      <h3>账号与权限</h3>
      <button class="ghost-btn" @click="logout">退出登录</button>
    </div>

    <div class="profile-grid">
      <div>
        <span class="signed-label">昵称</span>
        <strong>{{ sessionState.profile?.nickname }}</strong>
      </div>
      <div>
        <span class="signed-label">用户名</span>
        <strong>{{ sessionState.profile?.username }}</strong>
      </div>
      <div>
        <span class="signed-label">邮箱</span>
        <strong>{{ sessionState.profile?.email }}</strong>
      </div>
      <div>
        <span class="signed-label">认证来源</span>
        <strong>{{ sessionState.profile?.authSource }}</strong>
      </div>
      <div class="profile-wide">
        <span class="signed-label">当前角色</span>
        <div class="hero-badges">
          <span v-for="role in sessionState.roles" :key="role">{{ role }}</span>
        </div>
      </div>
    </div>
  </section>
</template>
