<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand">
      <svg class="ecg" viewBox="0 0 1200 120" preserveAspectRatio="none">
        <polyline points="0,60 180,60 210,60 230,20 250,100 270,40 290,60 520,60 550,60 570,10 590,110 610,60 900,60 930,30 950,90 970,60 1200,60" fill="none" stroke="#fff" stroke-width="2"/>
      </svg>
      <div class="brand-top">
        <div class="logo-badge">✚</div>
        <div class="org">
          <b>济宁市公共卫生医疗中心</b>
          <span>济宁市第四人民医院</span>
        </div>
      </div>
      <div class="brand-center">
        <h1>慢病专病全周期<br>一体化健康守护体系</h1>
        <div class="slogan">一人一档 · 医防融合 · 智能干预 · 全程守护</div>
        <ul class="points">
          <li v-for="p in points" :key="p.title">
            <div class="ico">{{ p.icon }}</div>
            <div><b>{{ p.title }}</b><span>{{ p.desc }}</span></div>
          </li>
        </ul>
      </div>
      <div class="brand-bottom">地址：山东省济宁市任城区二十里铺街道晨阳路66号 ｜ 0537-3601111</div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-side">
      <div class="card">
        <h2>欢迎登录</h2>
        <p class="sub">管理后台 · 请使用院内分配的账号登录</p>

        <!-- 登录方式切换 -->
        <div class="tabs">
          <a :class="{ on: loginType === 'password' }" @click="loginType = 'password'">账号密码登录</a>
          <a :class="{ on: loginType === 'sms' }" @click="loginType = 'sms'">短信验证码登录</a>
        </div>

        <!-- 账号密码登录 -->
        <form v-if="loginType === 'password'" @submit.prevent="handleLogin">
          <div class="field">
            <svg class="fi" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-6 8-6s8 2 8 6"/>
            </svg>
            <input v-model="form.username" type="text" placeholder="请输入工号 / 用户名" />
          </div>
          <div class="field">
            <svg class="fi" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>
            </svg>
            <input v-model="form.password" :type="showPwd ? 'text' : 'password'" placeholder="请输入密码" />
            <span class="eye" @click="showPwd = !showPwd">{{ showPwd ? '👁' : '👁‍🗨' }}</span>
          </div>
          <div class="field with-captcha" v-if="captchaEnabled">
            <svg class="fi" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="7" width="18" height="12" rx="2"/><path d="M7 11h2m2 0h2m2 0h2M7 15h10"/>
            </svg>
            <input v-model="form.code" type="text" placeholder="请输入验证码" maxlength="4" autocomplete="off" />
            <img class="captcha-img" :src="captchaImg" alt="验证码" title="点击刷新" @click="loadCaptcha" />
          </div>
          <div class="row">
            <label><input v-model="form.remember" type="checkbox" />记住登录状态</label>
            <a @click="$router.push('/login?forgot=1')">忘记密码？</a>
          </div>
          <button class="btn-login" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <!-- 短信登录 -->
        <form v-else @submit.prevent="handleSmsLogin">
          <div class="field">
            <svg class="fi" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="5" y="2" width="14" height="20" rx="3"/><line x1="12" y1="18" x2="12" y2="18.01" stroke-width="3"/>
            </svg>
            <input v-model="smsForm.phone" type="tel" placeholder="请输入手机号" maxlength="11" />
          </div>
          <div class="field">
            <svg class="fi" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="6" width="18" height="14" rx="2"/><path d="M3 10h18"/>
            </svg>
            <input v-model="smsForm.code" type="text" placeholder="请输入验证码" maxlength="6" />
            <span class="code-btn" :class="{ disabled: smsCountdown > 0 }" @click="sendCode">
              {{ smsCountdown > 0 ? `${smsCountdown}s 后重发` : '获取验证码' }}
            </span>
          </div>
          <button class="btn-login" type="submit" :disabled="loading" style="margin-top:8px">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <p class="tip">首次登录请使用管理员下发的初始密码，并及时修改</p>

        <div class="other">其他登录方式</div>
        <div class="wx"><span title="企业微信扫码">⊞</span></div>
      </div>
      <div class="footer">
        © 2026 济宁市公共卫生医疗中心（济宁市第四人民医院）<br>
        鲁ICP备2020048633号-1 ｜ 鲁公网安备37081102000377
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCaptcha } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginType = ref('password')
const showPwd = ref(false)
const loading = ref(false)

const form = reactive({ username: '', password: '', remember: true, code: '' })
const captchaEnabled = ref(false)
const captchaImg = ref('')
const uuid = ref('')
const smsForm = reactive({ phone: '', code: '' })
const smsCountdown = ref(0)

const points = [
  { icon: '▣', title: '360° 健康画像', desc: '档案、专档、随访、设备数据多源归集，全生命周期管理' },
  { icon: '⚙', title: '智能干预引擎', desc: '随访、复诊、预警任务自动生成，健康处方时序化推送' },
  { icon: '⛨', title: '医共体协同', desc: '多机构分级管理、履约规则引擎，服务责任清晰可溯' }
]

async function handleLogin() {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  if (captchaEnabled.value && !form.code) return ElMessage.warning('请输入验证码')
  loading.value = true
  try {
    await userStore.login({ username: form.username, password: form.password, code: form.code, uuid: uuid.value })
    await userStore.getInfo()
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/dashboard')
  } catch (e) {
    console.error(e)
    loadCaptcha() // 登录失败刷新验证码（后端一次性消费）
  } finally {
    loading.value = false
  }
}

async function loadCaptcha() {
  try {
    const data = await getCaptcha()
    captchaEnabled.value = data.captchaEnabled !== false
    if (captchaEnabled.value) {
      captchaImg.value = data.img || ''
      uuid.value = data.uuid || ''
      form.code = ''
    }
  } catch (e) {
    console.error('加载验证码失败', e)
  }
}

onMounted(loadCaptcha)

async function handleSmsLogin() {
  ElMessage.info('短信验证码登录暂未开放，请使用账号密码登录')
}

async function sendCode() {
  ElMessage.info('短信验证码登录暂未开放')
}
</script>

<style scoped>
.login-page { display: flex; min-height: 100vh; }

/* ---------- 左侧品牌区 ---------- */
.brand {
  flex: 1.2; position: relative; overflow: hidden; color: #fff;
  background: linear-gradient(135deg, #00245e 0%, var(--color-primary-dark) 45%, var(--color-primary) 100%);
  display: flex; flex-direction: column; justify-content: space-between; padding: 48px 64px;
}
.brand::before, .brand::after { content: ''; position: absolute; border-radius: 50%; border: 1px solid rgba(255,255,255,.12); }
.brand::before { width: 560px; height: 560px; right: -160px; top: -160px; }
.brand::after  { width: 380px; height: 380px; right: -60px;  top: -60px; }

.ecg { position: absolute; left: 0; right: 0; bottom: 120px; opacity: .18; }

.brand-top { display: flex; align-items: center; gap: 14px; position: relative; z-index: 1; }
.logo-badge {
  width: 52px; height: 52px; border-radius: 50%; background: #fff; color: var(--color-primary-dark);
  display: flex; align-items: center; justify-content: center; font-size: 26px; font-weight: 700;
  box-shadow: 0 4px 14px rgba(0,0,0,.25);
}
.brand-top .org b { font-size: 17px; letter-spacing: 1px; display: block; }
.brand-top .org span { font-size: 12px; opacity: .75; letter-spacing: 2px; }

.brand-center { position: relative; z-index: 1; max-width: 640px; }
.brand-center h1 { font-size: 40px; line-height: 1.35; font-weight: 700; letter-spacing: 2px; text-shadow: 0 2px 12px rgba(0,0,0,.2); }
.brand-center .slogan { margin-top: 18px; font-size: 15px; letter-spacing: 4px; opacity: .85; }

.points { margin-top: 44px; display: flex; flex-direction: column; gap: 20px; }
.points li { list-style: none; display: flex; gap: 14px; align-items: flex-start; }
.points .ico {
  width: 38px; height: 38px; flex: none; border-radius: 10px;
  background: rgba(255,255,255,.14); border: 1px solid rgba(255,255,255,.25);
  display: flex; align-items: center; justify-content: center; font-size: 18px;
}
.points b { font-size: 15px; display: block; margin-bottom: 4px; }
.points span { font-size: 12.5px; opacity: .72; line-height: 1.6; }

.brand-bottom { position: relative; z-index: 1; font-size: 12px; opacity: .6; letter-spacing: 1px; }

/* ---------- 右侧表单区 ---------- */
.form-side {
  flex: 1; background: #fff; display: flex; flex-direction: column;
  align-items: center; justify-content: center; padding: 48px; min-width: 480px;
}
.card { width: 380px; }
.card h2 { font-size: 24px; font-weight: 700; color: var(--color-text); }
.card .sub { margin-top: 8px; font-size: 13px; color: var(--color-text-secondary); }

.tabs { display: flex; gap: 28px; margin: 32px 0 24px; border-bottom: 1px solid var(--color-border); }
.tabs a { font-size: 15px; color: var(--color-text-secondary); padding-bottom: 10px; cursor: pointer; position: relative; }
.tabs a.on { color: var(--color-primary); font-weight: 600; }
.tabs a.on::after { content: ''; position: absolute; left: 0; right: 0; bottom: -1px; height: 3px; border-radius: 2px; background: var(--color-primary); }

.field { position: relative; margin-bottom: 18px; }
.field .fi { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 18px; height: 18px; color: #9CA3AF; }
.field input {
  width: 100%; height: 46px; border: 1px solid var(--color-border); border-radius: 8px;
  padding: 0 40px; font-size: 14px; outline: none; transition: .2s; background: #FAFBFC;
}
.field input:focus { border-color: var(--color-primary); background: #fff; box-shadow: 0 0 0 3px var(--color-primary-light); }
.field .eye { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); cursor: pointer; font-size: 15px; }
.field .code-btn {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%); cursor: pointer;
  color: var(--color-primary); font-size: 13px; white-space: nowrap;
}
.field .code-btn.disabled { color: #9CA3AF; cursor: default; }
.field.with-captcha input { padding-right: 128px; }
.field .captcha-img {
  position: absolute; right: 6px; top: 50%; transform: translateY(-50%);
  height: 38px; width: 112px; border-radius: 6px; cursor: pointer;
  object-fit: cover; border: 1px solid var(--color-border); background: #fff;
}

.row { display: flex; justify-content: space-between; align-items: center; font-size: 13px; margin-bottom: 22px; }
.row label { display: flex; gap: 6px; align-items: center; color: var(--color-text-secondary); cursor: pointer; }
.row label input { accent-color: var(--color-primary); }
.row a { color: var(--color-primary); cursor: pointer; }

.btn-login {
  width: 100%; height: 46px; border: none; border-radius: 8px; font-size: 16px; letter-spacing: 6px; color: #fff; cursor: pointer;
  background: linear-gradient(90deg, var(--color-primary-dark), var(--color-primary));
  box-shadow: 0 6px 16px rgba(0,80,160,.3); transition: .2s;
}
.btn-login:hover:not(:disabled) { filter: brightness(1.08); }
.btn-login:disabled { opacity: .65; cursor: not-allowed; }

.tip { margin-top: 16px; font-size: 12px; color: var(--color-text-secondary); text-align: center; }

.other { margin-top: 28px; display: flex; align-items: center; gap: 12px; color: #C0C4CC; font-size: 12px; }
.other::before, .other::after { content: ''; flex: 1; height: 1px; background: var(--color-border); }

.wx { margin-top: 18px; display: flex; justify-content: center; }
.wx span {
  width: 42px; height: 42px; border-radius: 50%; border: 1px solid var(--color-border);
  display: flex; align-items: center; justify-content: center; cursor: pointer; color: #2BA471; font-size: 20px;
}

.footer { margin-top: 56px; font-size: 12px; color: #B4B9C2; text-align: center; line-height: 1.9; }
</style>
