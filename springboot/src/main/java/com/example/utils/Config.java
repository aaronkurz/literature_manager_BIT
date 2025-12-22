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

    //caj转pdf配置
    public static final String CAJ2PDF_CONVERTER_EXE = "E:\\manager\\springboot\\src\\main\\java\\com\\example\\utils\\Caj2pdf\\caj2pdf.exe";
    public static final String CAJ2PDF_MUTOOL_EXE = "E:\\manager\\springboot\\src\\main\\java\\com\\example\\utils\\Caj2pdf\\mutool.exe";

    //pdf转docx配置
    public static final String PDF2DOCX_PY_SCRIPT = System.getenv().getOrDefault("PDF2DOCX_PY_SCRIPT", "/app/scripts/pdf_converter.py");
    public static final int PDF2DOCX_TIMEOUT_MINUTES = Integer.parseInt(System.getenv().getOrDefault("PDF2DOCX_TIMEOUT_MINUTES", "30"));

    //pdf2txt配置
    public static final String PDF2TXT_PY_SCRIPT = System.getenv().getOrDefault("PDF2TXT_PY_SCRIPT", "/app/scripts/pdf_to_text.py");
    public static final int PDF2TXT_TIMEOUT_MINUTES = Integer.parseInt(System.getenv().getOrDefault("PDF2TXT_TIMEOUT_MINUTES", "30"));
        public static final String DOCLING_PY_SCRIPT = System.getenv().getOrDefault("DOCLING_PY_SCRIPT", "/app/scripts/docling_extract.py");
        public static final int DOCLING_TIMEOUT_MINUTES = Integer.parseInt(System.getenv().getOrDefault("DOCLING_TIMEOUT_MINUTES", "10"));
    public static final String OCR_PATH = System.getenv().getOrDefault("OCR_PATH", "/usr/bin");
    public static final String LOG_PATH = System.getenv().getOrDefault("LOG_PATH", "/app/log");

    // Simplified JSON prompt for metadata extraction from academic papers
    public static final String METADATA_EXTRACTION_JSON = """
            {
            "title": "论文的完整标题",
            "author": "作者姓名，多个作者用分号分隔",
            "organ": "作者单位/机构",
            "year": "发表年份，仅数字",
            "pubTime": "发表时间，格式YYYY-MM-DD",
            "source": "期刊或会议名称",
            "keyword": "关键词，用分号分隔",
            "summary": "论文摘要，完整提取",
            "doi": "DOI编号（如果有）",
            "pageCount": "页数"
            }""";

    // Simplified JSON prompt for single summary length (mid-length ~50 chars)
    public static final String SUMMARY_JSON = """
            {
            "summary1": "第1种对论文摘要的总结凝练，50字左右",
            "summary2": "第2种对论文摘要的总结凝练，50字左右",
            "summary3": "第3种对论文摘要的总结凝练，50字左右",
            "summary4": "第4种对论文摘要的总结凝练，50字左右",
            "summary5": "第5种对论文摘要的总结凝练，50字左右",
            "summary6": "第6种对论文摘要的总结凝练，50字左右，要用通俗易懂的语言",
            "target": "用通俗易懂的语言简述论文的研究动机",
            "algorithm1": "第1种介绍本文用到的核心算法，50字左右",
            "algorithm2": "第2种介绍本文用到的核心算法，50字左右",
            "algorithm3": "第3种介绍本文用到的核心算法，50字左右",
            "algorithm4": "第4种介绍本文用到的核心算法，50字左右，用通俗易懂的语言",
            "environment": "详细介绍本论文的实验环境",
            "tools": "详细介绍本论文的实验工具",
            "datas": "详细介绍本论文的实验数据",
            "standard": "详细介绍本论文的实验指标",
            "result": "详细介绍本论文的实验结果",
            "future": "从不同角度尽可能详细介绍本论文对未来工作的总结与展望",
            "weekpoint": "从不同角度尽可能详细介绍本论文已有研究的不足之处",
            "keyword": "文本的关键词，用;分隔",
            "fullSummary": "提取论文完整摘要"}""";
}