# 部分页面
登录注册
![图片文字描述](https://github.com/beomyo/literature_manager/blob/main/files/2025-03-13_11-02-27.jpg)
关键词检索，搜索结果可高亮显示
![图片文字描述](https://github.com/beomyo/literature_manager/blob/main/files/2025-03-13_11-01-17.jpg)
具体论文详情，原件和附件来自用户，pdf和docx由服务器处理转换得到，调用python库实现
论文过长会调用单个模型，较短论文调用三个模型，2+1，2提取1评判，高亮最优结果
![图片文字描述](https://github.com/beomyo/literature_manager/blob/main/files/2025-03-13_11-01-46.jpg)
知识图谱，使用Neovis.js渲染，重建图谱会先清除全部节点，重新创建，数据来自mysql
![图片文字描述](https://github.com/beomyo/literature_manager/blob/main/files/2025-03-13_11-01-56.jpg)
论文上传
![图片文字描述](https://github.com/beomyo/literature_manager/blob/main/files/2025-03-13_11-02-00.jpg)
嵌入neo4j，因为安全性无法直接使用iframe标签嵌入，采用Nginx反向代理neo4j，再将Nginx代理后的地址嵌入，解除限制
![图片文字描述](https://github.com/beomyo/literature_manager/blob/main/files/2025-03-13_11-02-11.jpg)
用户论文信息管理，只能看到自己上传的论文
![图片文字描述](https://github.com/beomyo/literature_manager/blob/main/files/2025-03-13_11-02-17.jpg)
管理员后台，公告、用户、论文管理
![图片文字描述](https://github.com/beomyo/literature_manager/blob/main/files/2025-03-13_11-02-37.jpg)

---
---

## Run the backend (Docker) 🔧

A full guide for running the backend with Docker Compose (MySQL, Neo4j, Spring Boot) is available at `docker/README.md`.

Quick overview:

- Start (from the project root):
	- `docker compose up --build -d` — builds and starts MySQL, Neo4j, and the Spring Boot backend.
- Main services & ports:
	- Backend (Spring Boot): http://localhost:9090
	- MySQL: 3306 (root / 123456, database `manager`; init script: `docker/mysql/init.sql`)
	- Neo4j: 7474 (HTTP), 7687 (Bolt); default account `neo4j` / `12345678`
- Data persistence:
	- Uploads are stored at `/manager/upload` inside the app container (mapped to Docker volume `uploads` by default). To view files directly on the host, map that volume to a host path in `docker-compose.yml`.

See `docker/README.md` for details on changing credentials, viewing logs, and triggering the knowledge-graph rebuild.

---

## Run the frontend (local dev) 🖥️

Quick steps to run the Vue frontend locally (for development):

- Prerequisites: Node.js and npm (or yarn).
- From the project root:
  1. cd vue
  2. npm install
  3. npm run serve
- Open the URL shown in the terminal (the dev server usually runs on http://localhost:8080).

Notes:
- The frontend expects the backend API at `http://localhost:9090` and Neo4j at HTTP `7474` / Bolt `7687` for features such as the Knowledge Graph. You can start the backend and databases with `docker compose up --build -d` (see the Docker section above).
- The Neo4j Browser requires login (prototype credentials: `neo4j` / `12345678`).
- For production builds run `npm run build` inside `vue` and serve the generated `dist` with a static server or integrate into the Spring Boot static resources.

