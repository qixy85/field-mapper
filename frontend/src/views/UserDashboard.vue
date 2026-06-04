<template>
  <div class="user-container">
    <div class="header">
      <h2>预算执行报告系统</h2>
      <div class="header-right">
        <span class="user-info">{{ store.username }}</span>
        <button class="btn-logout" @click="logout">退出</button>
      </div>
    </div>

    <div class="toolbar">
      <button class="btn-add" @click="addRow">+ 添加行</button>
      <button class="btn-save-all" @click="saveAll" :disabled="saving">{{ saving ? '保存中...' : '保存所有修改' }}</button>
      <span v-if="saveMsg" :class="saveMsg.type">{{ saveMsg.text }}</span>
    </div>

    <div class="table-wrapper">
      <table>
        <thead>
          <tr class="title-row">
            <th colspan="11" class="title-cell">预算执行情况</th>
          </tr>
          <tr>
            <th>去向</th>
            <th>项目</th>
            <th>片区名</th>
            <th>具体费用</th>
            <th>月份</th>
            <th>实际累计金额</th>
            <th>当月账面累计金额</th>
            <th>当月差异</th>
            <th>当月备注/服务期限</th>
            <th>数据核对人</th>
            <th style="width:50px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in rows" :key="row._key">
            <td><input v-model="row.direction" /></td>
            <td><input v-model="row.project" /></td>
            <td><input v-model="row.area" /></td>
            <td><input v-model="row.specificCost" /></td>
            <td><input v-model="row.month" /></td>
            <td><input v-model.number="row.actualCumulative" type="number" step="0.01" /></td>
            <td><input v-model.number="row.bookCumulative" type="number" step="0.01" /></td>
            <td><input v-model.number="row.difference" type="number" step="0.01" /></td>
            <td><input v-model="row.remark" /></td>
            <td><input v-model="row.verifier" /></td>
            <td><button class="btn-del" @click="removeRow(idx)" :disabled="rows.length <= 1">✕</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { useAuthStore } from '../store.js'

let keyCounter = 0

export default {
  name: 'UserDashboard',
  data() {
    return {
      store: useAuthStore(),
      rows: [],
      saving: false,
      saveMsg: '',
    }
  },
  async mounted() {
    await this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const r = await axios.get('/api/budget')
        this.rows = (r.data || []).map(item => ({ ...item, _key: ++keyCounter, _id: item.id }))
        if (this.rows.length === 0) this.addRow()
      } catch { this.addRow() }
    },
    addRow() {
      this.rows.push({
        _key: ++keyCounter,
        _new: true,
        direction: '', project: '', area: '', specificCost: '', month: '',
        actualCumulative: null, bookCumulative: null, difference: null,
        remark: '', verifier: '',
      })
    },
    async removeRow(idx) {
      const row = this.rows[idx]
      if (row._id && !row._new) {
        if (!confirm('确定删除第 ' + (idx+1) + ' 行？')) return
        try {
          await axios.delete('/api/budget/' + row._id)
        } catch (e) {
          alert('删除失败: ' + (e.response?.data?.error || e.message))
          return
        }
      }
      this.rows.splice(idx, 1)
    },
    async saveAll() {
      this.saving = true
      this.saveMsg = ''
      let success = 0
      for (const row of this.rows) {
        try {
          if (row._new) {
            const payload = { ...row }
            delete payload._key; delete payload._new; delete payload._id
            const r = await axios.post('/api/budget', payload)
            row._new = false
            row._id = r.data.id
            row.id = r.data.id
          } else if (row._id) {
            const payload = { ...row }
            delete payload._key; delete payload._new; delete payload._id
            await axios.put('/api/budget/' + row._id, payload)
          }
          success++
        } catch (e) {
          console.error('Save failed for row', row._key, e)
        }
      }
      this.saving = false
      if (success === this.rows.length) {
        this.saveMsg = { type: 'success', text: '全部保存成功' }
      } else {
        this.saveMsg = { type: 'error', text: `成功 ${success}/${this.rows.length}` }
      }
      setTimeout(() => { this.saveMsg = '' }, 3000)
    },
    logout() { this.store.logout(); this.$router.push('/login') },
  },
}
</script>

<style scoped>
.user-container { max-width: 1400px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header h2 { margin: 0; }
.header-right { display: flex; align-items: center; gap: 12px; }
.user-info { color: #555; font-size: 13px; }
.btn-logout { padding: 6px 16px; background: #ff4d4f; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.toolbar { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.btn-add { padding: 6px 16px; background: #1890ff; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-save-all { padding: 6px 16px; background: #52c41a; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-save-all:disabled { background: #b7eb8f; cursor: not-allowed; }
.btn-del { background: #ff4d4f; color: #fff; border: none; border-radius: 4px; width: 26px; height: 26px; cursor: pointer; }
.btn-del:disabled { background: #d9d9d9; cursor: not-allowed; }
.success { color: #52c41a; font-size: 13px; margin-left: 8px; }
.error { color: #ff4d4f; font-size: 13px; margin-left: 8px; }
.table-wrapper { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,.08); overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 13px; min-width: 1200px; }
.title-row th { background: #e6f7ff; text-align: center; font-size: 16px; font-weight: bold; padding: 12px; border-bottom: 2px solid #91d5ff; }
th { background: #fafafa; padding: 8px 6px; text-align: center; border-bottom: 1px solid #e8e8e8; white-space: nowrap; }
td { padding: 4px 4px; border-bottom: 1px solid #f0f0f0; }
td input { width: 100%; padding: 4px 6px; border: 1px solid #d9d9d9; border-radius: 3px; font-size: 12px; outline: none; box-sizing: border-box; }
td input:focus { border-color: #1890ff; }
</style>
