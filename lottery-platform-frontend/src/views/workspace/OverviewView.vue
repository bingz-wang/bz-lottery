<script setup>
import {computed} from 'vue'
import {routeCatalog} from '../../router'
import {hasRole, sessionState} from '../../stores/session'

const availableRoutes = computed(() => routeCatalog.filter((item) => hasRole(item.meta.roles ?? [])))
</script>

<template>
  <div class="workspace-grid two-columns">
    <section class="panel poster-panel">
      <p class="eyebrow">Role Driven Access</p>
      <h2>欢迎回来，{{ sessionState.profile?.nickname }}</h2>
      <p class="hero-copy">
        你的菜单、页面与可访问功能是根据当前角色实时生成的。普通用户默认拥有抽奖与个人中心，管理员会额外看到运营台。
      </p>
    </section>

    <section class="panel metrics-panel">
      <div class="metric-item">
        <span>用户 ID</span>
        <strong>{{ sessionState.profile?.id }}</strong>
      </div>
      <div class="metric-item">
        <span>角色数</span>
        <strong>{{ sessionState.roles.length }}</strong>
      </div>
      <div class="metric-item">
        <span>认证来源</span>
        <strong>{{ sessionState.profile?.authSource }}</strong>
      </div>
    </section>

    <section class="panel wide-panel">
      <div class="section-header">
        <h3>已注册动态路由</h3>
        <span>{{ availableRoutes.length }} 个页面</span>
      </div>
      <div class="route-list">
        <article v-for="item in availableRoutes" :key="item.name" class="route-card">
          <strong>{{ item.meta.title }}</strong>
          <p>{{ item.meta.navLabel }}</p>
          <small>{{ item.meta.roles.join(' / ') }}</small>
        </article>
      </div>
    </section>
  </div>
</template>
