# Field Mapper — 数据库字段导出 Excel 工具

Spring Boot + Vue 项目，将数据库字段映射为 Excel 表头并导出。

## 功能

- 数据源在 `application.yml` 中预定义（支持 PostgreSQL / Oracle）
- 前端选择数据源、输入表名、动态添加字段映射行
- 后端根据映射关系查询数据库并生成 `.xlsx` 文件
- 自动下载到浏览器

## 配置数据源

编辑 `backend/src/main/resources/application.yml` 中的 `datasources` 节点：

```yaml
datasources:
  my-pg:
    type: postgresql
    host: localhost
    port: 5432
    database: mydb
    schema: public
    username: postgres
    password: postgres

  my-oracle:
    type: oracle
    host: localhost
    port: 1521
    database: FREEPDB1
    schema: HISSAAS
    username: HISSAAS
    password: hissaas123
```

## 启动

```bash
# 后端（端口 8080）
cd backend && mvn spring-boot:run

# 前端（端口 5173）
cd frontend && npm install && npm run dev
```

打开 `http://localhost:5173` 即可使用。
