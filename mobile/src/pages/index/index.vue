<template>
  <view class="home-page">
    <!-- 头部 -->
    <view class="header">
      <view class="status-bar"></view>
      <view class="header-content">
        <view class="hello">
          <view class="hello-left">
            <text class="hello-name">您好，王先生</text>
            <text class="hello-date">2026年8月27日 · 周四 · 济宁 晴 31°C</text>
          </view>
          <view class="member-switch" @tap="switchMember">
            <text class="member-icon">👤</text>
            <text>切换成员</text>
            <text class="arrow"></text>
          </view>
        </view>
        <!-- 实名引导条 -->
        <view class="auth-bar" v-if="!isRealNameAuthed" @tap="goRealNameAuth">
          <text>尚未完成实名认证，部分服务受限</text>
          <text class="auth-link">去认证 ›</text>
        </view>
      </view>
    </view>

    <!-- 签约服务卡 -->
    <view class="contract-card">
      <view class="contract-header">
        <text class="contract-title">高血压管理服务包</text>
        <text class="contract-status">服务中</text>
      </view>
      <view class="contract-info">
        <text>签约团队：<text class="highlight">慢病管理中心·健康管理团队</text> ｜ 责任医生：<text class="highlight">崔松 主任医师</text></text>
        <text>服务周期：2026-01-15 ~ 2026-12-31 ｜ 含随访 12 次 · 宣教 24 篇</text>
      </view>
      <view class="progress-bar">
        <view class="progress-fill" :style="{ width: progress + '%' }"></view>
      </view>
      <view class="progress-info">
        <text>年度服务进度 {{ progress }}%</text>
        <text class="detail-link" @tap="viewContractDetail">查看详情 ›</text>
      </view>
    </view>

    <!-- 功能宫格 -->
    <view class="grid-section">
      <view class="grid-item" v-for="item in gridItems" :key="item.name" @tap="handleGridTap(item)">
        <view class="grid-icon" :class="item.bgClass">
          <text class="icon-text">{{ item.icon }}</text>
        </view>
        <text class="grid-name">{{ item.name }}</text>
      </view>
    </view>

    <!-- 今日待办 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">今日待办</text>
        <text class="section-more" @tap="viewAllTasks">全部任务 ›</text>
      </view>
      <view class="todo-list">
        <view class="todo-item" v-for="task in todoTasks" :key="task.id" @tap="handleTask(task)">
          <view class="todo-icon" :class="task.iconBg">
            <text class="icon-text">{{ task.icon }}</text>
          </view>
          <view class="todo-content">
            <text class="todo-title">{{ task.title }}</text>
            <text class="todo-desc">{{ task.desc }}</text>
          </view>
          <view class="todo-action" :class="task.urgent ? 'urgent' : ''">
            <text>{{ task.actionText }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 健康指标 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">我的健康指标</text>
        <text class="section-more" @tap="viewMetricsHistory">历史趋势 ›</text>
      </view>
      <view class="metric-card">
        <view class="metric-header">
          <text class="metric-label">收缩压（近7次）</text>
          <view class="metric-value">
            <text class="value-text">135/85</text>
            <text class="value-unit">mmHg · 轻度偏高</text>
          </view>
        </view>
        <view class="spark-chart">
          <view class="spark-bar" v-for="(h, i) in sparkData" :key="i" :class="{ active: i === sparkData.length - 1 }" :style="{ height: h + '%' }"></view>
        </view>
        <view class="spark-labels">
          <text>08-15</text>
          <text>08-18</text>
          <text>08-21</text>
          <text>08-24</text>
          <text>08-27</text>
        </view>
      </view>
    </view>

    <!-- 健康宣教 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">健康宣教</text>
        <text class="section-more" @tap="viewMoreEducation">更多 ›</text>
      </view>
      <view class="education-item">
        <view class="education-img">
          <text class="edu-icon"></text>
        </view>
        <view class="education-content">
          <text class="edu-title">夏季高血压患者如何科学补水与限盐？</text>
          <text class="edu-desc">慢病管理中心 · 已推送至您的健康处方</text>
        </view>
      </view>
    </view>

    <!-- 底部占位 -->
    <view class="bottom-padding"></view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
// import { getHomeData, getTodoTasks, getHealthMetrics } from '@/api/index'

const isRealNameAuthed = ref(false)
const progress = ref(46)

const gridItems = ref([
  { icon: '🩺', name: '记录血压', bgClass: 'bg-blue', action: 'recordBP' },
  { icon: '🩸', name: '记录血糖', bgClass: 'bg-red', action: 'recordBG' },
  { icon: '', name: '上传报告', bgClass: 'bg-orange', action: 'uploadReport' },
  { icon: '📊', name: '风险评估', bgClass: 'bg-green', action: 'riskAssess' },
  { icon: '📋', name: '健康处方', bgClass: 'bg-green', action: 'prescription' },
  { icon: '💊', name: '用药提醒', bgClass: 'bg-blue', action: 'medication' },
  { icon: '', name: '互联网医院', bgClass: 'bg-orange', action: 'internetHospital' },
  { icon: '📅', name: '预约挂号', bgClass: 'bg-red', action: 'appointment' }
])

const todoTasks = ref([
  { id: 1, icon: '', iconBg: 'bg-blue', title: '测量并记录晨起血压', desc: '高血压随访计划 · 每日 07:00-09:00', actionText: '去完成', urgent: true },
  { id: 2, icon: '📝', iconBg: 'bg-orange', title: '填写高血压随访问卷', desc: '责任医生 崔松 已下发 · 截止今日', actionText: '去填写', urgent: true },
  { id: 3, icon: '💊', iconBg: 'bg-green', title: '服用 苯磺酸氨氯地平片 5mg', desc: '用药提醒 · 每日 1 次 · 早餐后', actionText: '已服用', urgent: false }
])

const sparkData = ref([52, 60, 48, 70, 64, 58, 66])

// 页面显示时刷新数据
onShow(() => {
  // TODO: 后端完成后调用接口
  // loadHomeData()
})

// 下拉刷新
onPullDownRefresh(() => {
  // TODO: 后端完成后调用接口刷新数据
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})

// 方法
function switchMember() {
  uni.showActionSheet({
    itemList: ['本人', '父亲', '母亲'],
    success: (res) => {
      console.log('切换成员:', res.tapIndex)
    }
  })
}

function goRealNameAuth() {
  // TODO: 跳转到实名认证页
  uni.showToast({ title: '跳转实名认证', icon: 'none' })
}

function viewContractDetail() {
  // TODO: 跳转到签约详情
  uni.showToast({ title: '查看签约详情', icon: 'none' })
}

function handleGridTap(item) {
  // TODO: 根据 action 跳转对应页面
  uni.showToast({ title: item.name, icon: 'none' })
}

function handleTask(task) {
  // TODO: 跳转到任务执行页
  uni.showToast({ title: task.title, icon: 'none' })
}

function viewAllTasks() {
  // TODO: 跳转到全部任务页
  uni.showToast({ title: '全部任务', icon: 'none' })
}

function viewMetricsHistory() {
  // TODO: 跳转到指标历史页
  uni.showToast({ title: '历史趋势', icon: 'none' })
}

function viewMoreEducation() {
  // TODO: 跳转到宣教列表
  uni.showToast({ title: '更多宣教', icon: 'none' })
}

// TODO: 后端完成后实现数据加载
// async function loadHomeData() {
//   try {
//     const data = await getHomeData()
//     // 更新页面数据
//   } catch (e) {
//     console.error(e)
//   }
// }
</script>

<style scoped>
.home-page {
  background: #F5F7FA;
  min-height: 100vh;
}

/* 头部 */
.header {
  background: linear-gradient(160deg, #003080, #0050A0 70%, #2f7cc4);
  padding-bottom: 64px;
}

.status-bar {
  height: 44px;
}

.header-content {
  padding: 0 16px;
}

.hello {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 44px;
}

.hello-left {
  display: flex;
  flex-direction: column;
}

.hello-name {
  color: #fff;
  font-size: 19px;
  font-weight: 600;
}

.hello-date {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  margin-top: 4px;
}

.member-switch {
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  padding: 6px 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
}

.member-icon {
  font-size: 14px;
}

.arrow {
  font-size: 10px;
}

/* 实名引导条 */
.auth-bar {
  margin-top: 12px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px dashed rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  padding: 8px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  font-size: 12px;
}

.auth-link {
  color: #FFD98A;
  font-weight: 600;
}

/* 签约服务卡 */
.contract-card {
  margin: -48px 16px 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(20, 40, 80, 0.1);
  padding: 14px 16px;
  position: relative;
  z-index: 2;
}

.contract-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contract-title {
  font-size: 14px;
  font-weight: 600;
}

.contract-status {
  color: #2BA471;
  background: #E9F6F0;
  border-radius: 10px;
  padding: 2px 10px;
  font-size: 11px;
}

.contract-info {
  margin-top: 8px;
  font-size: 12px;
  color: #6B7280;
  line-height: 1.7;
}

.highlight {
  color: #0050A0;
}

.progress-bar {
  margin-top: 10px;
  height: 6px;
  background: #EEF1F5;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0050A0, #4D8FD1);
  border-radius: 3px;
  transition: width 0.3s;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #6B7280;
  margin-top: 5px;
}

.detail-link {
  color: #0050A0;
}

/* 功能宫格 */
.grid-section {
  margin: 14px 16px 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px 8px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px 0;
}

.grid-item {
  text-align: center;
  font-size: 11.5px;
  color: #1F2937;
}

.grid-icon {
  width: 44px;
  height: 44px;
  margin: 0 auto 6px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-text {
  font-size: 20px;
}

.bg-blue { background: #EAF3FC; }
.bg-red { background: #FDECEC; }
.bg-orange { background: #FDF5E8; }
.bg-green { background: #E9F6F0; }

.grid-name {
  font-size: 11.5px;
}

/* 区块 */
.section {
  margin: 14px 16px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 13px;
  background: #0050A0;
  border-radius: 2px;
  margin-right: 7px;
  vertical-align: -2px;
}

.section-more {
  font-size: 12px;
  color: #6B7280;
}

/* 待办列表 */
.todo-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid #F2F4F7;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 13px;
  font-weight: 500;
  display: block;
}

.todo-desc {
  font-size: 11px;
  color: #6B7280;
  margin-top: 2px;
  display: block;
}

.todo-action {
  font-size: 12px;
  color: #0050A0;
  background: #EAF3FC;
  padding: 5px 12px;
  border-radius: 14px;
}

.todo-action.urgent {
  color: #D54941;
  background: #FDECEC;
}

/* 指标卡 */
.metric-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.metric-label {
  font-size: 13px;
  color: #6B7280;
  font-weight: 500;
}

.metric-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.value-text {
  font-size: 20px;
  font-weight: 700;
  color: #1F2937;
}

.value-unit {
  font-size: 11px;
  color: #6B7280;
}

.spark-chart {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 56px;
  margin-top: 12px;
}

.spark-bar {
  flex: 1;
  border-radius: 3px 3px 0 0;
  background: #CFE3F7;
  transition: height 0.3s;
}

.spark-bar.active {
  background: #0050A0;
}

.spark-labels {
  display: flex;
  justify-content: space-between;
  font-size: 10.5px;
  color: #B4BAC4;
  margin-top: 6px;
}

/* 宣教 */
.education-item {
  background: #fff;
  border-radius: 12px;
  padding: 12px 14px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.education-img {
  width: 64px;
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(135deg, #CFE3F7, #EAF3FC);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.edu-icon {
  font-size: 20px;
}

.education-content {
  flex: 1;
}

.edu-title {
  font-size: 13px;
  font-weight: 500;
  display: block;
  line-height: 1.5;
}

.edu-desc {
  font-size: 11px;
  color: #6B7280;
  margin-top: 2px;
  display: block;
}

/* 底部占位 */
.bottom-padding {
  height: 16px;
}
</style>
