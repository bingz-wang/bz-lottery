<script setup>
import {onMounted, ref} from 'vue'
import {fetchPrizeList} from '../../api/lottery'

const prizes = ref([])

onMounted(async () => {
  prizes.value = await fetchPrizeList()
})
</script>

<template>
  <section class="panel wide-panel">
    <div class="section-header">
      <h3>奖池中心</h3>
      <span>{{ prizes.length }} 个奖项</span>
    </div>
    <div class="route-list three-columns">
      <article v-for="prize in prizes" :key="prize.id" class="route-card prize-route-card">
        <small>{{ prize.prizeLevel }}</small>
        <strong>{{ prize.prizeName }}</strong>
        <p>库存 {{ prize.availableStock }} / 概率 {{ prize.probability }}</p>
      </article>
    </div>
  </section>
</template>
