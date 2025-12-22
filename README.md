# Literature Manager - Simplified Local Research Tool

A streamlined AI-powered literature management system designed for local research use. This tool helps researchers manage academic papers, extract insights using Qwen LLM, and visualize relationships through a knowledge graph.

## Key Features

- **Paper Management**: Upload and organize academic papers (PDF, CAJ, DOCX formats)
- **AI-Powered Analysis**: Automatic paper summarization and insight extraction using Qwen LLM
- **Knowledge Graph**: Visualize relationships between papers using Neo4j
- **Format Conversion**: Automatic conversion between PDF, DOCX, and TXT formats
- **Keyword Search**: Search papers by title, author, keywords, and other metadata

## Recent Simplifications

This version has been simplified for local research use:

- ✅ **No User Management**: Removed login/registration - runs as a single-user local tool
- ✅ **Single Summary Length**: Simplified to one optimal summary length (~50 characters)
- ✅ **Qwen LLM Only**: Streamlined to use only Qwen for consistency
- ✅ **No Notifications**: Removed notification system for cleaner experience

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
- The Neo4j Browser requires login (credentials: `neo4j` / `12345678`).
- For production builds run `npm run build` inside `vue` and serve the generated `dist` with a static server or integrate into the Spring Boot static resources.

---

## System Requirements

- Docker and Docker Compose (for backend services)
- Node.js 14+ and npm (for frontend development)
- Qwen API key (configured in `Config.java`)

## Configuration

Key configuration file: `springboot/src/main/java/com/example/utils/Config.java`

- **Qwen API**: Configure your Qwen API key for LLM features
- **Database**: MySQL and Neo4j connection settings
- **File Storage**: Upload path configuration

## Architecture

- **Backend**: Spring Boot with MyBatis
- **Frontend**: Vue.js (optional, can run headless)
- **Databases**: MySQL (metadata), Neo4j (knowledge graph)
- **AI/LLM**: Qwen for paper analysis and summarization
- **File Processing**: Python scripts for PDF/DOCX/TXT conversion

