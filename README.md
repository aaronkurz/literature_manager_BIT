# Literature Manager - Simplified Local Research Tool

A streamlined AI-powered literature management system designed for local research use. This tool helps researchers manage academic papers, extract insights using **local Ollama LLM (Mistral 3B)**, and visualize relationships through a knowledge graph.

## Key Features

- **Simple PDF Upload**: Drag-and-drop interface, no manual data entry
- **Auto-Metadata Extraction**: AI automatically extracts title, authors, abstract, etc.
- **Local AI Processing**: Paper summarization using Ollama with Mistral 3B (3 billion parameter model)
- **Processing Status**: Real-time progress tracking with approval workflow
- **Knowledge Graph**: Visualize relationships between papers using Neo4j
- **No Authentication**: Single-user local tool, no login required

## Recent Improvements (Dec 2025)

**Fixed Issues:**
- ✅ **Ollama API Error**: Fixed model name (`mistral:3b` not `ministral:3b`)
- ✅ **Error Handling**: Added comprehensive logging and troubleshooting
- ✅ **Context Management**: Truncate large texts for 3B model (~16k chars max)

**New Features:**
- ✅ **Simplified Upload**: PDF-only, drag-and-drop, no manual fields
- ✅ **Processing Status Page**: Real-time progress with step-by-step updates
- ✅ **Approval Workflow**: Review extracted metadata before saving
- ✅ **No Attachments**: Removed attachment upload for simplicity

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

### 2. Pull the Mistral 3B Model

**IMPORTANT**: The correct model name is `mistral:3b` (not ministral)

After services are running:

```bash
./scripts/pull-ministral.sh
```

Or manually:

```bash
docker exec lm_ollama ollama pull mistral:3b
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

## Usage Workflow

1. **Upload PDF**: Drag-and-drop your PDF file
2. **Auto Processing**: System extracts metadata and generates summaries
3. **Review Results**: Check extracted title, authors, abstract
4. **Approve**: Confirm and save to database
5. **Explore**: View in knowledge graph

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
- ~2GB disk space for Mistral 3B model

---

## Troubleshooting

### Ollama 404 Error

**Problem**: `POST "/api/chat" - 404 Not Found`

**Solutions**:
1. Check model is pulled: `docker exec lm_ollama ollama list`
2. Pull correct model: `docker exec lm_ollama ollama pull mistral:3b`
3. Check Ollama is running: `docker ps | grep ollama`

### Backend Hangs During Processing

**Problem**: Processing freezes after "将文本上传给Ollama模型"

**Solutions**:
1. Check Ollama logs: `docker logs lm_ollama -f`
2. Verify model loaded: `docker exec lm_ollama ollama list`
3. Check paper size - very large PDFs may timeout
4. System will truncate text to ~16k chars automatically

### Out of Memory

- Increase Docker memory limit to at least 4GB
- Mistral 3B requires ~2GB RAM when loaded
- Consider closing other applications

### Frontend Shows Old UI

- Clear browser cache (Ctrl+Shift+R)
- Check Vue dev server is running
- Verify accessing http://localhost:8080

---

## API Endpoints

### Article Management
- `POST /article/upload` - Upload PDF (simplified, no manual fields)
- `GET /article/processing-status/:title` - Check processing status
- `POST /article/approve` - Approve and save metadata
- `POST /article/search` - Search papers
- `POST /article/summary/:title` - Get AI summary
- `POST /article/rebuild` - Rebuild knowledge graph

### Frontend Routes
- `/front/home` - Search and browse papers
- `/front/upload` - Upload new papers (simplified)
- `/front/processing/:title` - View processing status
- `/front/graph` - View knowledge graph
- `/front/settings` - System settings

---

## Configuration

Key configuration file: `springboot/src/main/java/com/example/utils/Config.java`

- **Ollama**: `OLLAMA_BASE_URL` (default: http://localhost:11434)
- **Model**: `OLLAMA_MODEL` (mistral:3b)
- **Context Window**: Auto-truncate to ~16k chars for 3B model
- **Metadata Extraction**: Specialized prompts for PDF parsing

---

## Architecture

- **Backend**: Spring Boot with MyBatis
- **Frontend**: Vue.js (simplified, no authentication)
- **Databases**: MySQL (metadata), Neo4j (knowledge graph)
- **AI/LLM**: Ollama with Mistral 3B (local, no API keys)
- **File Processing**: Python scripts for PDF/TXT conversion

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

### Pull Latest Model
```bash
docker exec lm_ollama ollama pull mistral:3b
```

---

## Data Persistence

All data is persisted in Docker volumes:
- `db_data` - MySQL database
- `neo4j_data` - Neo4j graph database
- `ollama_data` - Ollama models
- `uploads` - Uploaded paper files

---

## License

This is a research prototype tool for local use.
