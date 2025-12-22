# Literature Manager - Simplified Local Research Tool

A streamlined AI-powered literature management system designed for local research use. This tool helps researchers manage academic papers, extract insights using **local Ollama LLM (Ministral 3B)**, and visualize relationships through a knowledge graph.

## Key Features

- **Paper Management**: Upload and organize academic papers (PDF, CAJ, DOCX formats)
- **Local AI Processing**: Automatic paper summarization using Ollama with Ministral 3B (3 billion parameter model)
- **Knowledge Graph**: Visualize relationships between papers using Neo4j
- **Format Conversion**: Automatic conversion between PDF, DOCX, and TXT formats
- **Keyword Search**: Search papers by title, author, keywords, and other metadata
- **No Authentication**: Single-user local tool, no login required

## Recent Simplifications

This version has been simplified for local research use:

- ✅ **No User Management**: Removed login/registration - runs as a single-user local tool
- ✅ **Single Summary Length**: Simplified to one optimal summary length (~50 characters)
- ✅ **Local AI Only**: Uses Ollama with Ministral 3B instead of online APIs
- ✅ **No Notifications**: Removed notification system for cleaner experience
- ✅ **Simplified Frontend**: Settings button replaces user menu

---

## Quick Start

### 1. Start the Backend Services

From the project root:

```bash
docker compose up --build -d
```

This starts:
- MySQL (port 3306) - metadata storage
- Neo4j (ports 7474, 7687) - knowledge graph
- Ollama (port 11434) - local AI service
- Spring Boot backend (port 9090)

### 2. Pull the Ministral 3B Model

After services are running:

```bash
./scripts/pull-ministral.sh
```

Or manually:

```bash
docker exec lm_ollama ollama pull ministral:3b
```

### 3. Start the Frontend (Optional)

For development:

```bash
cd vue
npm install
npm run serve
```

Access at: http://localhost:8080

---

## Main Services & Ports

- **Backend API**: http://localhost:9090
- **Frontend** (dev): http://localhost:8080  
- **MySQL**: localhost:3306 (root / 123456, database: `manager`)
- **Neo4j Browser**: http://localhost:7474 (neo4j / 12345678)
- **Ollama API**: http://localhost:11434

---

## System Requirements

- Docker and Docker Compose
- At least 4GB RAM (for Ollama + services)
- Node.js 14+ and npm (for frontend development)
- ~2GB disk space for Ministral 3B model

---

## Configuration

Key configuration file: `springboot/src/main/java/com/example/utils/Config.java`

- **Ollama**: Configured for local deployment (http://ollama:11434 in Docker)
- **Database**: MySQL and Neo4j connection settings
- **File Storage**: Upload path configuration
- **Model**: Ministral 3B (ministral:3b)

---

## Architecture

- **Backend**: Spring Boot with MyBatis
- **Frontend**: Vue.js (simplified, no authentication)
- **Databases**: MySQL (metadata), Neo4j (knowledge graph)
- **AI/LLM**: Ollama with Ministral 3B (local, no API keys needed)
- **File Processing**: Python scripts for PDF/DOCX/TXT conversion

---

## Data Persistence

All data is persisted in Docker volumes:
- `db_data` - MySQL database
- `neo4j_data` - Neo4j graph database
- `ollama_data` - Ollama models
- `uploads` - Uploaded paper files

---

## API Endpoints

### Article Management
- `POST /article/upload` - Upload paper
- `POST /article/search` - Search papers
- `GET /article/file-paths/{title}` - Get file paths
- `POST /article/summary/{title}` - Get AI summary
- `POST /article/rebuild` - Rebuild knowledge graph

### Frontend Routes
- `/front/home` - Search and browse papers
- `/front/upload` - Upload new papers
- `/front/graph` - View knowledge graph
- `/front/settings` - System settings

---

## Troubleshooting

**Ollama model not found?**
```bash
docker exec lm_ollama ollama pull ministral:3b
```

**Frontend 404 error on load?**
- This has been fixed - authentication removed from frontend

**Out of memory?**
- Increase Docker memory limit to at least 4GB
- Consider using a smaller model if needed

**Knowledge graph not updating?**
- Use `/article/rebuild` endpoint to manually rebuild

---

## Development

### Backend
```bash
cd springboot
mvn clean install
mvn spring-boot:run
```

### Frontend
```bash
cd vue
npm install
npm run serve
```

### Build for Production
```bash
cd vue
npm run build
# Dist files can be served via Spring Boot static resources
```

---

## License

This is a research prototype tool for local use.