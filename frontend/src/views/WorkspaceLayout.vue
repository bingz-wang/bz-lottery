<script setup>
import {computed, ref, watch} from 'vue'
import {RouterLink, RouterView, useRoute, useRouter} from 'vue-router'
import {routeCatalog} from '../router'
import {hasMenu, hasRole, sessionState, signOut} from '../stores/session'

const route = useRoute()
const router = useRouter()
const mobileNavOpen = ref(false)

const navItems = computed(() => routeCatalog.filter((item) => hasRole(item.meta.roles ?? []) && hasMenu(item.meta.menuCode)))

watch(
    () => route.fullPath,
    () => {
      mobileNavOpen.value = false
    }
)

async function logout() {
  await signOut()
  mobileNavOpen.value = false
  router.replace('/login')
}
</script>

<template>
  <div class="workspace-shell">
    <button
        v-if="navItems.length"
        :aria-expanded="mobileNavOpen"
        aria-controls="workspace-sidebar"
        class="mobile-menu-btn"
        type="button"
        @click="mobileNavOpen = true"
    >
      菜单
    </button>

    <div v-if="mobileNavOpen" class="sidebar-backdrop" @click="mobileNavOpen = false"></div>

    <aside id="workspace-sidebar" :class="{ open: mobileNavOpen }" class="workspace-sidebar">
      <div>
        <div class="sidebar-head">
          <div>
            <p class="eyebrow">RBAC Console</p>
            <h2>抽奖控制台</h2>
          </div>
          <button class="sidebar-close-btn" type="button" @click="mobileNavOpen = false">关闭</button>
        </div>
        <p class="sidebar-copy">导航会根据当前登录用户的角色动态注册与展示。</p>
      </div>

      <nav class="workspace-nav">
        <RouterLink
            v-for="item in navItems"
            :key="item.name"
            :class="{ active: route.name === item.name }"
            :to="`/workspace/${item.path}`"
            class="nav-link"
        >
          <span>{{ item.meta.navLabel }}</span>
          <small>{{ item.meta.roles.join(' / ') }}</small>
        </RouterLink>
      </nav>

      <div class="workspace-user-card">
        <span class="signed-label">当前登录</span>
        <strong>{{ sessionState.profile?.nickname }}</strong>
        <p>{{ sessionState.profile?.username }}</p>
        <p>{{ sessionState.roles.join(' · ') }}</p>
        <button class="ghost-btn full-width" @click="logout">退出登录</button>
      </div>
    </aside>

    <section class="workspace-main">
      <header class="workspace-header">
        <div>
          <p class="eyebrow">Protected Workspace</p>
          <h1>{{ route.meta.title || '工作台' }}</h1>
        </div>
      </header>

      <main class="workspace-content">
        <RouterView/>
      </main>
    </section>
  </div>
</template>
