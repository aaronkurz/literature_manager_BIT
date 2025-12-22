# Docker setup for literature_manager

This directory contains Docker Compose setup to run the full stack locally:
- MySQL (with init SQL)
- Neo4j
- Spring Boot backend (built with Maven in image)

## Quick start

From the project root run:

```bash
# build and start services
docker-compose up --build -d

# view logs
docker-compose logs -f app
```

The Spring Boot app will be available at http://localhost:9090

## Data persistence

- MySQL data is stored in Docker volume `db_data` (by default managed by Docker)
- Neo4j data is stored in Docker volume `neo4j_data`
- Uploaded files are stored in Docker volume `uploads` and are mounted at `/manager/upload` inside the app container.

If you want to map the uploads volume to a host directory instead, edit `docker-compose.yml` for the `app` service volumes entry, for example:

```yaml
    volumes:
      - ./data/uploads:/manager/upload
```

## Notes

- The MySQL init script `docker/mysql/init.sql` runs on first start to create the `manager` database and required tables and an initial `admin` user.
- On startup, the app waits for MySQL and Neo4j to be reachable, then starts and triggers `/article/rebuild` to populate the Neo4j graph from the database (if there are rows in `article_info`).
- To change DB/Neo4j credentials, update the `environment` block in `docker-compose.yml` or set appropriate environment variables.
