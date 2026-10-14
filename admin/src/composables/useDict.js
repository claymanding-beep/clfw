import { ref } from 'vue'
import { dictDataByType } from '@/api/system/dict'

// 简单内存缓存，避免同一字典类型重复请求
const cache = {}

/**
 * 加载字典数据。
 * 用法：const { sys_user_sex, sys_common_status } = useDict('sys_user_sex', 'sys_common_status')
 * 返回以字典类型为键的 ref 数组，每项形如 { dictLabel, dictValue, listClass }
 */
export function useDict(...types) {
  const result = {}
  types.forEach(type => {
    const list = ref(cache[type] || [])
    if (!cache[type]) {
      dictDataByType(type)
        .then(data => {
          cache[type] = data || []
          list.value = cache[type]
        })
        .catch(() => {})
    }
    result[type] = list
  })
  return result
}

/** 根据字典列表与值取标签 */
export function dictLabel(list, value, defaultText = '—') {
  if (value === null || value === undefined || value === '') return defaultText
  const hit = (list || []).find(d => String(d.dictValue) === String(value))
  return hit ? hit.dictLabel : defaultText
}

/** 根据字典列表与值取 tag 样式类 */
export function dictClass(list, value) {
  const hit = (list || []).find(d => String(d.dictValue) === String(value))
  return hit?.listClass || 'info'
}
