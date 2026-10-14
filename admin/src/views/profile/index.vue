<template>
  <div class="hg-page profile-page">
    <el-row :gutter="16">
      <!-- 左侧：账号概览 -->
      <el-col :span="7">
        <el-card shadow="never" class="profile-card">
          <div class="avatar-box">
            <el-upload class="avatar-uploader" :show-file-list="false" accept="image/*"
              :before-upload="beforeAvatarUpload" :http-request="handleAvatarUpload">
              <div class="avatar-wrap">
                <el-avatar :size="80" :src="form.avatar">{{ (form.nickname || form.username || '用')[0] }}</el-avatar>
                <div class="avatar-mask">{{ avatarUploading ? '上传中' : '更换头像' }}</div>
              </div>
            </el-upload>
            <div class="uname">{{ form.nickname || form.username }}</div>
            <div class="urole">{{ roleText }}</div>
          </div>
          <el-divider />
          <ul class="info-list">
            <li><span>用户名</span><b>{{ form.username || '—' }}</b></li>
            <li><span>手机号</span><b>{{ form.phone || '—' }}</b></li>
            <li><span>邮箱</span><b>{{ form.email || '—' }}</b></li>
            <li><span>所属机构</span><b>{{ orgName || '—' }}</b></li>
            <li><span>最后登录</span><b>{{ form.loginDate || '—' }}</b></li>
          </ul>
        </el-card>
      </el-col>

      <!-- 右侧：编辑区 -->
      <el-col :span="17">
        <el-card shadow="never" class="profile-card">
          <el-tabs v-model="activeTab">
            <!-- 基本资料 -->
            <el-tab-pane label="基本资料" name="basic">
              <el-form ref="basicRef" :model="form" :rules="basicRules" label-width="90px" style="max-width: 480px">
                <el-form-item label="用户名">
                  <el-input v-model="form.username" disabled />
                </el-form-item>
                <el-form-item label="姓名" prop="nickname">
                  <el-input v-model="form.nickname" placeholder="请输入姓名" maxlength="30" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="form.sex">
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="2">女</el-radio>
                    <el-radio :value="0">未知</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="savingBasic" @click="handleSaveBasic">保存</el-button>
                  <el-button @click="loadProfile">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="pwd">
              <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="90px" style="max-width: 480px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="savingPwd" @click="handleSavePwd">修改密码</el-button>
                  <el-button @click="resetPwdForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProfile, updateProfile, updatePwd } from '@/api/auth'
import { uploadFile } from '@/api/system/file'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const roleNameMap = {
  super_admin: '超级管理员', org_admin: '机构管理员', dept_director: '科室主任',
  doctor: '医生', nurse: '护士', health_manager: '健康管理师', auditor: '审核人员'
}
const roleText = computed(() =>
  userStore.isSuperAdmin ? '超级管理员' : (roleNameMap[userStore.roles?.[0]] || userStore.roles?.[0] || '—')
)
const orgName = computed(() => form.orgName || '')

const activeTab = ref(route.query.tab === 'pwd' ? 'pwd' : 'basic')
watch(() => route.query.tab, (tab) => {
  if (route.path === '/profile') activeTab.value = tab === 'pwd' ? 'pwd' : 'basic'
})

const form = reactive({ username: '', nickname: '', phone: '', email: '', sex: 0, avatar: '', loginDate: '', orgName: '' })
const basicRef = ref()
const savingBasic = ref(false)
const avatarUploading = ref(false)

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRef = ref()
const savingPwd = ref(false)

const basicRules = {
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function loadProfile() {
  const data = await getProfile()
  Object.assign(form, {
    username: data.username || '',
    nickname: data.nickname || '',
    phone: data.phone || '',
    email: data.email || '',
    sex: data.sex ?? 0,
    avatar: data.avatar || '',
    loginDate: data.loginDate || '',
    orgName: data.orgName || ''
  })
}

async function handleSaveBasic() {
  await basicRef.value.validate()
  savingBasic.value = true
  try {
    await updateProfile({
      nickname: form.nickname,
      phone: form.phone,
      email: form.email,
      sex: form.sex
    })
    ElMessage.success('保存成功')
    await userStore.getInfo()
    await loadProfile()
  } finally {
    savingBasic.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 5MB')
    return false
  }
  return true
}

async function handleAvatarUpload({ file }) {
  avatarUploading.value = true
  try {
    const data = await uploadFile(file)
    form.avatar = data.url
    await updateProfile({ avatar: data.url })
    await userStore.getInfo()
    ElMessage.success('头像已更新')
  } finally {
    avatarUploading.value = false
  }
}

function resetPwdForm() {
  pwdRef.value?.resetFields()
  Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
}

async function handleSavePwd() {
  await pwdRef.value.validate()
  savingPwd.value = true
  try {
    await updatePwd({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    resetPwdForm()
    await ElMessageBox.confirm('密码已修改，需要重新登录', '提示', { type: 'success', confirmButtonText: '重新登录' })
    userStore.resetState()
    window.location.href = '/login'
  } finally {
    savingPwd.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page { padding: 0; }
.profile-card { border-radius: 8px; }
.avatar-box { text-align: center; padding: 8px 0; }
.avatar-uploader { display: inline-block; }
.avatar-wrap { position: relative; width: 80px; height: 80px; border-radius: 50%; overflow: hidden; cursor: pointer; }
.avatar-mask {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,.45); color: #fff; font-size: 12px; opacity: 0; transition: opacity .2s;
}
.avatar-wrap:hover .avatar-mask { opacity: 1; }
.avatar-box .uname { margin-top: 12px; font-size: 16px; font-weight: 600; color: var(--color-text); }
.avatar-box .urole { margin-top: 4px; font-size: 12px; color: var(--color-text-secondary); }
.info-list { list-style: none; margin: 0; padding: 0; font-size: 13px; }
.info-list li { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed var(--color-border); }
.info-list li:last-child { border-bottom: none; }
.info-list li span { color: var(--color-text-secondary); }
.info-list li b { color: var(--color-text); font-weight: 500; }
</style>
