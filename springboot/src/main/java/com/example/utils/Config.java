package com.example.utils;

public class Config {
    // Ollama Configuration - Local LLM with Ministral-3 (3B model)
    // Note: Requires Ollama 0.13.1+ (pre-release)
    public static final String OLLAMA_BASE_URL = System.getenv().getOrDefault("OLLAMA_BASE_URL", "http://localhost:11434");
    public static final String OLLAMA_MODEL = "ministral-3:3b";  // 3GB model with 256K context window

    // MySQL configuration — read from environment variables if available
    public static final String MYSQL_LINK = System.getenv().getOrDefault("MYSQL_URL", "jdbc:mysql://localhost:3306/manager?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true");
    public static final String MYSQL_USERNAME = System.getenv().getOrDefault("MYSQL_USERNAME", "root");
    public static final String MYSQL_PASSWORD = System.getenv().getOrDefault("MYSQL_PASSWORD", "123456");

    // Neo4j configuration — supports environment overrides
    public static final String NEO4J_LINK = System.getenv().getOrDefault("NEO4J_URI", System.getenv().getOrDefault("NEO4J_LINK", "bolt://localhost:7687"));
    public static final String NEO4J_USERNAME = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
    public static final String NEO4J_PASSWORD = System.getenv().getOrDefault("NEO4J_PASSWORD", "12345678");

    // File upload path (can be mapped to a Docker volume)
    public static final String UPLOAD_PATH = System.getenv().getOrDefault("UPLOAD_PATH", "/manager/upload");
    public static final String PDF_PATH = UPLOAD_PATH;
    public static final String TXT_PATH = UPLOAD_PATH;
    public static final String DOCX_PATH = UPLOAD_PATH;
    public static final String CAJ_PATH = UPLOAD_PATH;

    // CAJ to PDF config
    public static final String CAJ2PDF_CONVERTER_EXE = "E:\\manager\\springboot\\src\\main\\java\\com\\example\\utils\\Caj2pdf\\caj2pdf.exe";
    public static final String CAJ2PDF_MUTOOL_EXE = "E:\\manager\\springboot\\src\\main\\java\\com\\example\\utils\\Caj2pdf\\mutool.exe";

    // PDF to DOCX config
    public static final String PDF2DOCX_PY_SCRIPT = System.getenv().getOrDefault("PDF2DOCX_PY_SCRIPT", "/app/scripts/pdf_converter.py");
    public static final int PDF2DOCX_TIMEOUT_MINUTES = Integer.parseInt(System.getenv().getOrDefault("PDF2DOCX_TIMEOUT_MINUTES", "30"));

    // PDF to TXT config
    public static final String PDF2TXT_PY_SCRIPT = System.getenv().getOrDefault("PDF2TXT_PY_SCRIPT", "/app/scripts/pdf_to_text.py");
    public static final int PDF2TXT_TIMEOUT_MINUTES = Integer.parseInt(System.getenv().getOrDefault("PDF2TXT_TIMEOUT_MINUTES", "30"));
    public static final String OCR_PATH = System.getenv().getOrDefault("OCR_PATH", "/usr/bin");
    public static final String LOG_PATH = System.getenv().getOrDefault("LOG_PATH", "/app/log");

    // Simplified JSON prompt for metadata extraction from academic papers
    public static final String METADATA_EXTRACTION_JSON = """
            {
            "title": "Full title of the paper",
            "author": "Author names, separated by semicolons",
            "organ": "Author institution/organization",
            "year": "Publication year, numbers only",
            "pubTime": "Publication date, format YYYY-MM-DD",
            "source": "Journal or conference name",
            "keyword": "Keywords, separated by semicolons",
            "summary": "Paper abstract, extract in full",
            "doi": "DOI number (if available)",
            "pageCount": "Page count"
            }""";

    // Simplified JSON prompt for single summary length (mid-length ~50 chars)
    public static final String SUMMARY_JSON = """
            {
            "summary1": "1st summary of paper abstract, ~50 words",
            "summary2": "2nd summary of paper abstract, ~50 words",
            "summary3": "3rd summary of paper abstract, ~50 words",
            "summary4": "4th summary of paper abstract, ~50 words",
            "summary5": "5th summary of paper abstract, ~50 words",
            "summary6": "6th summary of paper abstract, ~50 words, in plain language",
            "target": "Brief research motivation in plain language",
            "algorithm1": "1st description of core algorithms, ~50 words",
            "algorithm2": "2nd description of core algorithms, ~50 words",
            "algorithm3": "3rd description of core algorithms, ~50 words",
            "algorithm4": "4th description of core algorithms, ~50 words, in plain language",
            "environment": "Detailed description of experimental environment",
            "tools": "Detailed description of experimental tools",
            "datas": "Detailed description of experimental data",
            "standard": "Detailed description of evaluation metrics",
            "result": "Detailed description of experimental results",
            "future": "Detailed future work and outlook from multiple perspectives",
            "weekpoint": "Detailed limitations of existing research from multiple perspectives",
            "keyword": "Keywords of the text, separated by ;",
            "fullSummary": "Extract full paper abstract"}""";
}