# Field Mapper — 数据库字段导出 Excel 工具

Spring Boot + Vue 项目，将数据库字段映射为 Excel 表头并导出。

## 功能

- 支持 PostgreSQL / Oracle 两种数据库
- 前端动态添加字段映射行
- 后端根据映射关系查询数据库并生成 `.xlsx` 文件
- 自动下载到浏览器

## 启动

### 后端

```bash
cd backend
mvn spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
```

打开 `http://localhost:5173` 即可使用。
