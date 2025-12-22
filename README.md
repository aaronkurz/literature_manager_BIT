# Literature Manager - Simplified Local Research Tool

A streamlined AI-powered literature management system designed for local research use. This tool helps researchers manage academic papers, extract insights using **local Ollama LLM (Ministral-3)**, and visualize relationships through a knowledge graph.

## Key Features

- **Simple PDF Upload**: Drag-and-drop interface, no manual data entry
- **Auto-Metadata Extraction**: AI automatically extracts title, authors, abstract, etc.
- **Local AI Processing**: Paper summarization using Ollama with **Ministral-3 (3B)** - 256K context window
- **Processing Status**: Real-time progress tracking with approval workflow
- **Knowledge Graph**: Visualize relationships between papers using Neo4j
- **No Authentication**: Single-user local tool, no login required
- **Vision Support**: Ministral-3 can analyze images in addition to text
- **Multilingual**: Supports English, Chinese, Japanese, Korean, and dozens of other languages

## Recent Improvements (Dec 2025)

**Fixed Issues:**
- ✅ **Ollama API Error**: Fixed model name (`ministral-3:3b` is correct)
- ✅ **Error Handling**: Added comprehensive logging and troubleshooting
- ✅ **Context Management**: Optimized for 256K context window, truncate at 32K chars for efficiency

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

### 2. Pull the Ministral-3 Model

**IMPORTANT**: The correct model name is `ministral-3:3b` (requires Ollama 0.13.1+)

After services are running:

```bash
./scripts/pull-ministral.sh
```

Or manually:

```bash
docker exec lm_ollama ollama pull ministral-3:3b
```

**Model Specifications:**
- **Size**: 3GB (3 billion parameters)
- **Context Window**: 256K tokens
- **Capabilities**: Vision, multilingual, function calling, JSON output
- **License**: Apache 2.0

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
- ~3GB disk space for Ministral-3 (3B) model
- **Ollama 0.13.1+** (pre-release required for Ministral-3)

---

## Troubleshooting

### Ollama 404 Error

**Problem**: `POST "/api/chat" - 404 Not Found`

**Solutions**:
1. Ensure Ollama 0.13.1+ is installed (Ministral-3 requirement)
2. Check model is pulled: `docker exec lm_ollama ollama list`
3. Pull correct model: `docker exec lm_ollama ollama pull ministral-3:3b`
4. Check Ollama is running: `docker ps | grep ollama`

### Backend Hangs During Processing

**Problem**: Processing freezes after "将文本上传给Ollama模型"

**Solutions**:
1. Check Ollama logs: `docker logs lm_ollama -f`
2. Verify model loaded: `docker exec lm_ollama ollama list`
3. Check paper size - system truncates at 32K chars for efficiency
4. Ensure sufficient RAM (4GB+ recommended)

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
- **Model**: `OLLAMA_MODEL` (ministral-3:3b)
- **Context Window**: 256K tokens (truncated to 32K chars for efficiency)
- **Metadata Extraction**: Specialized prompts for PDF parsing
- **Vision Support**: Can analyze images in PDFs (future enhancement)

---

## Architecture

- **Backend**: Spring Boot with MyBatis
- **Frontend**: Vue.js (simplified, no authentication)
- **Databases**: MySQL (metadata), Neo4j (knowledge graph)
- **AI/LLM**: Ollama with Ministral-3 (3B) - local, no API keys, 256K context
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
docker exec lm_ollama ollama pull ministral-3:3b
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
