<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand">
      <div class="brand-content">
        <div class="logo-badge">{{ appTitle[0] || 'C' }}</div>
        <h1>{{ appTitle }}</h1>
        <p class="subtitle">{{ appSubtitle }}</p>
        <ul class="features">
          <li v-for="f in features" :key="f.title">
            <span class="feat-icon">{{ f.icon }}</span>
            <div><b>{{ f.title }}</b><span>{{ f.desc }}</span></div>
          </li>
        </ul>
      </div>
      <div class="brand-footer">{{ appFooter }}</div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-side">
      <div class="card">
        <h2>欢迎登录</h2>
        <p class="sub">管理后台 · 请使用分配的账号登录</p>

        <form @submit.prevent="handleLogin">
          <div class="field">
            <svg class="fi" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-6 8-6s8 2 8 6"/>
            </svg>
            <input v-model="form.username" type="text" placeholder="请输入用户名" autocomplete="username" />
          </div>
          <div class="field">
            <svg class="fi" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>
            </svg>
            <input v-model="form.password" :type="showPwd ? 'text' : 'password'" placeholder="请输入密码" autocomplete="current-password" />
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
          </div>
          <button class="btn-login" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <p class="tip">首次登录请使用管理员下发的初始密码，并及时修改</p>
      </div>
      <div class="footer">{{ appFooter }}</div>
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

// 品牌信息（通过环境变量配置，无需改代码）
const appTitle = import.meta.env.VITE_APP_TITLE || 'CLFW 管理平台'
const appSubtitle = import.meta.env.VITE_APP_SUBTITLE || '基础功能管理 · 系统配置 · 权限控制'
const appFooter = import.meta.env.VITE_APP_FOOTER || '© 2026 CLFW Platform'

const features = [
  { icon: '⚙', title: '系统管理', desc: '用户、角色、菜单、机构、字典一站式管理' },
  { icon: '🔐', title: '权限控制', desc: '细粒度权限标识 + 数据范围隔离' },
  { icon: '📊', title: '运维监控', desc: '操作日志、登录日志、在线用户实时掌控' }
]

const showPwd = ref(false)
const loading = ref(false)
const form = reactive({ username: '', password: '', remember: true, code: '' })
const captchaEnabled = ref(false)
const captchaImg = ref('')
const uuid = ref('')

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
    loadCaptcha()
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
</script>

<style scoped>
.login-page { display: flex; min-height: 100vh; }

/* ---------- 左侧品牌区 ---------- */
.brand {
  flex: 1.2; position: relative; overflow: hidden; color: #fff;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 45%, #0f3460 100%);
  display: flex; flex-direction: column; justify-content: space-between; padding: 48px 64px;
}
.brand::before, .brand::after { content: ''; position: absolute; border-radius: 50%; border: 1px solid rgba(255,255,255,.08); }
.brand::before { width: 560px; height: 560px; right: -160px; top: -160px; }
.brand::after  { width: 380px; height: 380px; right: -60px;  top: -60px; }

.brand-content { position: relative; z-index: 1; }
.logo-badge {
  width: 52px; height: 52px; border-radius: 12px; background: rgba(255,255,255,.15);
  border: 1px solid rgba(255,255,255,.25);
  display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 700;
  margin-bottom: 32px; backdrop-filter: blur(4px);
}
.brand-content h1 { font-size: 36px; font-weight: 700; letter-spacing: 1px; margin-bottom: 12px; }
.brand-content .subtitle { font-size: 15px; opacity: .75; letter-spacing: 2px; margin-bottom: 48px; }

.features { display: flex; flex-direction: column; gap: 20px; list-style: none; padding: 0; margin: 0; }
.features li { display: flex; gap: 14px; align-items: flex-start; }
.feat-icon {
  width: 38px; height: 38px; flex: none; border-radius: 10px;
  background: rgba(255,255,255,.1); border: 1px solid rgba(255,255,255,.2);
  display: flex; align-items: center; justify-content: center; font-size: 18px;
}
.features b { font-size: 15px; display: block; margin-bottom: 4px; }
.features span { font-size: 12.5px; opacity: .7; line-height: 1.6; }

.brand-footer { position: relative; z-index: 1; font-size: 12px; opacity: .5; letter-spacing: 1px; }

/* ---------- 右侧表单区 ---------- */
.form-side {
  flex: 1; background: #fff; display: flex; flex-direction: column;
  align-items: center; justify-content: center; padding: 48px; min-width: 460px;
}
.card { width: 380px; }
.card h2 { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.card .sub { margin-top: 8px; font-size: 13px; color: #6b7280; }

.field { position: relative; margin: 18px 0; }
.field .fi { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 18px; height: 18px; color: #9CA3AF; }
.field input {
  width: 100%; height: 46px; border: 1px solid #e5e7eb; border-radius: 8px;
  padding: 0 40px; font-size: 14px; outline: none; transition: .2s; background: #FAFBFC;
}
.field input:focus { border-color: #0f3460; background: #fff; box-shadow: 0 0 0 3px rgba(15,52,96,.08); }
.field .eye { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); cursor: pointer; font-size: 15px; }
.field.with-captcha input { padding-right: 128px; }
.field .captcha-img {
  position: absolute; right: 6px; top: 50%; transform: translateY(-50%);
  height: 38px; width: 112px; border-radius: 6px; cursor: pointer;
  object-fit: cover; border: 1px solid #e5e7eb; background: #fff;
}

.row { display: flex; justify-content: space-between; align-items: center; font-size: 13px; margin-bottom: 22px; }
.row label { display: flex; gap: 6px; align-items: center; color: #6b7280; cursor: pointer; }
.row label input { accent-color: #0f3460; }

.btn-login {
  width: 100%; height: 46px; border: none; border-radius: 8px; font-size: 16px; letter-spacing: 6px; color: #fff; cursor: pointer;
  background: linear-gradient(90deg, #1a1a2e, #0f3460);
  box-shadow: 0 6px 16px rgba(15,52,96,.25); transition: .2s;
}
.btn-login:hover:not(:disabled) { filter: brightness(1.15); }
.btn-login:disabled { opacity: .65; cursor: not-allowed; }

.tip { margin-top: 16px; font-size: 12px; color: #9CA3AF; text-align: center; }
.footer { margin-top: 56px; font-size: 12px; color: #B4B9C2; text-align: center; }
</style>
