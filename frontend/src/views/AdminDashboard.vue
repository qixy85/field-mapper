<template>
  <div class="admin-container">
    <div class="header">
      <h2>预算执行报告管理</h2>
      <div class="header-right">
        <span class="user-info">{{ store.username }} ({{ store.role }})</span>
        <button class="btn-logout" @click="logout">退出</button>
      </div>
    </div>

    <div class="tabs">
      <button :class="{ active: tab === 'users' }" @click="tab = 'users'">用户管理</button>
      <button :class="{ active: tab === 'budget' }" @click="tab = 'budget'">预算记录</button>
      <button :class="{ active: tab === 'audit' }" @click="tab = 'audit'">审计日志</button>
    </div>

    <div v-if="tab === 'users'" class="tab-content">
      <div class="toolbar"><button class="btn-add" @click="showAddUser = true">+ 添加用户</button></div>
      <table>
        <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>创建时间</th><th>创建人</th></tr></thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td><td>{{ u.username }}</td><td>{{ u.role }}</td>
            <td>{{ u.createdAt }}</td><td>{{ u.createdBy }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="showAddUser" class="modal-overlay" @click.self="showAddUser = false">
        <div class="modal">
          <h3>添加用户</h3>
          <div class="form-group"><label>用户名</label><input v-model="newUsername" /></div>
          <div class="form-group"><label>角色</label><select v-model="newRole"><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select></div>
          <p class="hint">默认密码: 123456</p>
          <div class="modal-actions">
            <button class="btn-cancel" @click="showAddUser = false">取消</button>
            <button class="btn-save" @click="addUser">确定</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="tab === 'budget'" class="tab-content">
      <table>
        <thead>
          <tr>
            <th>ID</th><th>去向</th><th>项目</th><th>片区名</th><th>具体费用</th><th>月份</th>
            <th>实际累计</th><th>账面累计</th><th>差异</th><th>备注</th><th>核对人</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in budgetItems" :key="item.id">
            <td>{{ item.id }}</td><td>{{ item.direction }}</td><td>{{ item.project }}</td>
            <td>{{ item.area }}</td><td>{{ item.specificCost }}</td><td>{{ item.month }}</td>
            <td>{{ item.actualCumulative }}</td><td>{{ item.bookCumulative }}</td>
            <td>{{ item.difference }}</td><td>{{ item.remark }}</td><td>{{ item.verifier }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="tab === 'audit'" class="tab-content">
      <table>
        <thead>
          <tr><th>ID</th><th>表名</th><th>记录ID</th><th>操作</th><th>字段</th><th>旧值</th><th>新值</th><th>操作人</th><th>时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="log in auditLogs" :key="log.id">
            <td>{{ log.id }}</td><td>{{ log.tableName }}</td><td>{{ log.recordId }}</td>
            <td>{{ log.actionType }}</td><td>{{ log.fieldName }}</td><td>{{ log.oldValue }}</td>
            <td>{{ log.newValue }}</td><td>{{ log.modifiedBy }}</td><td>{{ log.modifiedAt }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { useAuthStore } from '../store.js'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      store: useAuthStore(),
      tab: 'users',
      users: [],
      budgetItems: [],
      auditLogs: [],
      showAddUser: false,
      newUsername: '',
      newRole: 'USER',
    }
  },
  async mounted() {
    await this.loadUsers()
    await this.loadBudget()
    await this.loadAudit()
  },
  methods: {
    async loadUsers() { try { const r = await axios.get('/api/users'); this.users = r.data } catch {} },
    async loadBudget() { try { const r = await axios.get('/api/budget'); this.budgetItems = r.data } catch {} },
    async loadAudit() { try { const r = await axios.get('/api/audit/logs'); this.auditLogs = r.data } catch {} },
    async addUser() {
      if (!this.newUsername.trim()) return
      try {
        await axios.post('/api/users', { username: this.newUsername.trim(), role: this.newRole })
        this.showAddUser = false
        this.newUsername = ''
        this.newRole = 'USER'
        await this.loadUsers()
      } catch (e) { alert(e.response?.data?.error || '创建失败') }
    },
    logout() { this.store.logout(); this.$router.push('/login') },
  },
}
</script>

<style scoped>
.admin-container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header h2 { margin: 0; }
.header-right { display: flex; align-items: center; gap: 12px; }
.user-info { color: #555; font-size: 13px; }
.btn-logout { padding: 6px 16px; background: #ff4d4f; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.tabs { display: flex; gap: 4px; margin-bottom: 16px; }
.tabs button { padding: 8px 24px; border: 1px solid #d9d9d9; background: #fff; cursor: pointer; border-radius: 4px 4px 0 0; font-size: 14px; }
.tabs button.active { background: #1890ff; color: #fff; border-color: #1890ff; }
.tab-content { background: #fff; padding: 20px; border-radius: 0 4px 4px 4px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
.toolbar { margin-bottom: 12px; }
.btn-add { padding: 6px 16px; background: #1890ff; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th { background: #fafafa; padding: 8px 10px; text-align: left; border-bottom: 1px solid #e8e8e8; white-space: nowrap; }
td { padding: 6px 10px; border-bottom: 1px solid #f0f0f0; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 8px; padding: 24px; width: 360px; }
.modal h3 { margin-bottom: 16px; }
.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 13px; color: #555; margin-bottom: 4px; }
.form-group input, .form-group select { width: 100%; padding: 6px 10px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 13px; box-sizing: border-box; }
.hint { color: #888; font-size: 12px; margin-bottom: 12px; }
.modal-actions { display: flex; gap: 8px; justify-content: flex-end; }
.btn-cancel { padding: 6px 16px; background: #fff; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; }
.btn-save { padding: 6px 16px; background: #1890ff; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>
