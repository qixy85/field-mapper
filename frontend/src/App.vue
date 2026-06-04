<template>
  <div class="app">
    <h1>数据库字段 → Excel 导出工具</h1>

    <div class="panel">
      <h2>数据源</h2>
      <div class="form-row">
        <label>选择数据源</label>
        <select v-model="selectedDs">
          <option value="" disabled>-- 请选择 --</option>
          <option v-for="(info, key) in datasources" :key="key" :value="key">
            {{ key }} ({{ info.type }} — {{ info.host }}:{{ info.port }}/{{ info.database }})
          </option>
        </select>
      </div>
      <div v-if="selectedDs" class="ds-detail">
        <span class="tag">类型: {{ currentDs.type }}</span>
        <span class="tag">主机: {{ currentDs.host }}:{{ currentDs.port }}</span>
        <span class="tag">数据库: {{ currentDs.database }}</span>
        <span class="tag">模式: {{ currentDs.schema }}</span>
        <span class="tag">用户: {{ currentDs.username }}</span>
      </div>
      <div class="form-row" style="margin-top:12px">
        <label>表名</label>
        <input v-model="tableName" placeholder="要查询的表或视图" class="wide" />
      </div>
    </div>

    <div class="panel">
      <h2>字段映射
        <button class="btn-add" @click="addRow">+ 添加行</button>
      </h2>
      <table>
        <thead>
          <tr>
            <th style="width:40px">#</th>
            <th>数据库字段名</th>
            <th>Excel 表头</th>
            <th style="width:60px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in mappings" :key="idx">
            <td>{{ idx + 1 }}</td>
            <td><input v-model="row.dbField" placeholder="例如: user_name" /></td>
            <td><input v-model="row.header" placeholder="例如: 用户名" /></td>
            <td><button class="btn-del" @click="removeRow(idx)" :disabled="mappings.length <= 1">✕</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="actions">
      <button class="btn-submit" @click="submit" :disabled="loading || !selectedDs">
        {{ loading ? '导出中...' : '导出 Excel' }}
      </button>
      <span v-if="error" class="error">{{ error }}</span>
      <span v-if="success" class="success">导出成功！文件已下载</span>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'App',
  data() {
    return {
      datasources: {},
      selectedDs: '',
      tableName: '',
      mappings: [
        { dbField: '', header: '' },
      ],
      loading: false,
      error: '',
      success: false,
    }
  },
  computed: {
    currentDs() {
      return this.datasources[this.selectedDs] || {}
    },
  },
  async mounted() {
    try {
      const resp = await axios.get('/api/datasources')
      this.datasources = resp.data
      const keys = Object.keys(this.datasources)
      if (keys.length === 1) this.selectedDs = keys[0]
    } catch {
      this.error = '无法加载数据源列表，请确认后端已启动'
    }
  },
  methods: {
    addRow() {
      this.mappings.push({ dbField: '', header: '' })
    },
    removeRow(idx) {
      if (this.mappings.length > 1) this.mappings.splice(idx, 1)
    },
    async submit() {
      this.error = ''
      this.success = false

      if (!this.tableName) { this.error = '请输入表名'; return }
      const valid = this.mappings.filter(m => m.dbField.trim() && m.header.trim())
      if (valid.length === 0) { this.error = '请至少填写一行有效的字段映射'; return }

      this.loading = true
      try {
        const resp = await axios.post('/api/export', {
          datasource: this.selectedDs,
          tableName: this.tableName.trim(),
          mappings: valid.map(m => ({ dbField: m.dbField.trim(), header: m.header.trim() })),
        }, { responseType: 'blob' })

        const disposition = resp.headers['content-disposition']
        let filename = 'export.xlsx'
        if (disposition) {
          const match = disposition.match(/filename\*?=(?:UTF-8'')?([^;\s]+)/)
          if (match) filename = decodeURIComponent(match[1])
        }

        const url = window.URL.createObjectURL(new Blob([resp.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.success = true
      } catch (e) {
        this.error = '导出失败: ' + (e.response?.data?.message || e.message || '未知错误')
      } finally {
        this.loading = false
      }
    },
  },
}
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif; background: #f0f2f5; color: #333; }
.app { max-width: 900px; margin: 0 auto; padding: 24px 16px; }
h1 { text-align: center; margin-bottom: 24px; color: #1a1a2e; font-size: 22px; }
h2 { font-size: 16px; margin-bottom: 12px; color: #1a1a2e; display: flex; align-items: center; justify-content: space-between; }
.panel { background: #fff; border-radius: 8px; padding: 20px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
.form-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.form-row label { min-width: 70px; font-size: 13px; color: #555; }
.form-row input, .form-row select { flex: 1; min-width: 120px; padding: 6px 10px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 13px; outline: none; transition: border .2s; }
.form-row input:focus, .form-row select:focus { border-color: #1890ff; box-shadow: 0 0 0 2px rgba(24,144,255,.15); }
.form-row input.wide { flex: 2; }
.ds-detail { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
.tag { background: #e6f7ff; border: 1px solid #91d5ff; border-radius: 4px; padding: 2px 10px; font-size: 12px; color: #0050b3; }
table { width: 100%; border-collapse: collapse; }
th { background: #fafafa; padding: 8px 10px; font-size: 13px; text-align: left; border-bottom: 1px solid #e8e8e8; }
td { padding: 6px 10px; border-bottom: 1px solid #f0f0f0; }
td input { width: 100%; padding: 6px 8px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 13px; outline: none; }
td input:focus { border-color: #1890ff; }
.btn-add { background: #1890ff; color: #fff; border: none; border-radius: 4px; padding: 4px 14px; font-size: 13px; cursor: pointer; }
.btn-add:hover { background: #40a9ff; }
.btn-del { background: #ff4d4f; color: #fff; border: none; border-radius: 4px; width: 26px; height: 26px; font-size: 14px; cursor: pointer; line-height: 1; }
.btn-del:disabled { background: #d9d9d9; cursor: not-allowed; }
.btn-del:hover:not(:disabled) { background: #ff7875; }
.actions { text-align: center; margin-top: 16px; }
.btn-submit { background: #52c41a; color: #fff; border: none; border-radius: 6px; padding: 10px 40px; font-size: 16px; cursor: pointer; transition: background .2s; }
.btn-submit:hover { background: #73d13d; }
.btn-submit:disabled { background: #b7eb8f; cursor: not-allowed; }
.error { color: #ff4d4f; margin-left: 12px; font-size: 13px; }
.success { color: #52c41a; margin-left: 12px; font-size: 13px; }
</style>
