<template>
  <div class="workspace-grid two-columns">
    <section class="panel poster-panel">
      <p class="eyebrow">仅管理员可见</p>
      <h2>运营权限台</h2>
      <p class="hero-copy">
        这是一个只对 `LOTTERY_ADMIN` 开放的动态路由页面，用来展示典型 RBAC 中管理角色看到的额外菜单与受保护能力。
      </p>
    </section>

    <section class="panel metrics-panel">
      <div class="metric-item">
        <span>可访问接口</span>
        <strong>/lottery-award/**</strong>
      </div>
      <div class="metric-item">
        <span>可访问接口</span>
        <strong>/lottery-lottery/**</strong>
      </div>
      <div class="metric-item">
        <span>当前模型</span>
        <strong>RBAC 路由元信息</strong>
      </div>
    </section>
  </div>
</template>
