<template>
  <view class="page">
    <view class="placeholder">
      <text class="icon">🔔</text>
      <text class="title">消息</text>
      <text class="desc">待办任务与系统通知</text>
    </view>
  </view>
</template>

<script setup>
// TODO: 实现消息页面
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #F5F7FA;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder {
  text-align: center;
}

.icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  display: block;
  margin-bottom: 8px;
}

.desc {
  font-size: 13px;
  color: #6B7280;
}
</style>
