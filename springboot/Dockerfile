# Build stage
FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package -DskipITs

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Install utilities needed by entrypoint and runtime dependencies (Python, Tesseract)
RUN apt-get update && apt-get install -y --no-install-recommends netcat curl python3 python3-pip tesseract-ocr libtesseract-dev && rm -rf /var/lib/apt/lists/*
# Install Python packages needed by PDF tools (prefer cached wheels if present)
COPY requirements.txt /app/requirements.txt
COPY wheels /app/wheels
ENV PIP_DEFAULT_TIMEOUT=120
RUN if [ -d /app/wheels ] && [ "$(ls -A /app/wheels)" ]; then \
      pip3 install --no-cache-dir --no-index --find-links=/app/wheels -r /app/requirements.txt || \
      (n=0; until [ $n -ge 3 ]; do pip3 --default-timeout=120 install --no-cache-dir -r /app/requirements.txt && break; n=$((n+1)); echo "pip retry $n"; sleep 5; done); \
    else \
      n=0; until [ $n -ge 3 ]; do pip3 --default-timeout=120 install --no-cache-dir -r /app/requirements.txt && break; n=$((n+1)); echo "pip retry $n"; sleep 5; done; \
    fi
# Copy Python scripts and set env defaults for their paths
COPY src/main/resources/scripts /app/scripts
RUN chmod +x /app/scripts/*.py
ENV PDF2TXT_PY_SCRIPT=/app/scripts/pdf_to_text.py \
    PDF2DOCX_PY_SCRIPT=/app/scripts/pdf_converter.py \
    OCR_PATH=/usr/bin \
    LOG_PATH=/app/log

COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh
EXPOSE 9090
ENTRYPOINT ["/app/docker-entrypoint.sh"]
