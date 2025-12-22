package com.example.utils;

public class Config {
    // 千问配置
    public static final String QWEN_API_KEY = "sk-a10c3a06bb644fbea79f7de18dbf2b9e";
    public static final String QWEN_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    //qwq-plus
    //"qwen-turbo-2024-11-01"; // 前者百万 后者128k token qwen-turbo-2024-09-19//"qwen-plus";
    public static final String QWEN_MODEL1 = "qwen-turbo-2024-11-01";
    public static final String QWEN_MODEL2 = "qwen-turbo";
    public static final String QWEN_MODEL3 = "qwen-turbo-latest";
    public static final String QWEN_MODEL4 = "qwen-turbo-0919";
    public static final String QWEN_DOC_MODEL = "qwen-long";//

    // Kimi配置
    public static final String KIMI_API_KEY = "sk-RNK7LpsfjU304qEsQgtTszVdiVAHkarv0q8T3wdaiTAtgdX7";
    public static final String KIMI_BASE_URL = "https://api.moonshot.cn/v1";
    public static final String KIMI_MODEL = "moonshot-v1-8k";
    public static final String KIMI_DOC_MODEL = "moonshot-v1-32k";

    // DeepSeek配置
    public static final String DEEPSEEK_API_KEY = "sk-155b0b559665442884cea3b61b8fb64f";
    public static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com";
    public static final String DEEPSEEK_MODEL = "deepseek-chat";
    //星火配置
    public static final String SPARK_API_KEY = "bbAgcRTMbpXtBMPLJpqC:nFgXZPUFjYKblasjqmJt";
    public static final String SPARK_BASE_URL = "https://spark-api-open.xf-yun.com/v1";
    public static final String SPARK_MODEL = "lite";//
    //public static final String SPARK_MODEL = "generalv3";//


        // Mysql configuration — read from environment variables if available
        public static final String MYSQL_LINK = System.getenv().getOrDefault("MYSQL_URL", "jdbc:mysql://localhost:3306/manager?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true");
        public static final String MYSQL_USERNAME = System.getenv().getOrDefault("MYSQL_USERNAME", "root");
        public static final String MYSQL_PASSWORD = System.getenv().getOrDefault("MYSQL_PASSWORD", "123456");

        // Neo4j configuration — supports environment overrides
        public static final String NEO4J_LINK = System.getenv().getOrDefault("NEO4J_URI", System.getenv().getOrDefault("NEO4J_LINK", "bolt://localhost:7687"));
        public static final String NEO4J_USERNAME = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
        public static final String NEO4J_PASSWORD = System.getenv().getOrDefault("NEO4J_PASSWORD", "12345678");

        // File upload path (can be mapped to a Docker volume)
        public static final String UPLOAD_PATH = System.getenv().getOrDefault("UPLOAD_PATH", "/manager/upload"); //项目所在盘符下的upload文件夹
    public static final String PDF_PATH = UPLOAD_PATH;//"E:\\manager\\springboot\\pdfoutput";
    public static final String TXT_PATH = UPLOAD_PATH;//"E:\\manager\\springboot\\txtoutput";
    public static final String DOCX_PATH = UPLOAD_PATH;//"E:\\manager\\springboot\\docxoutput";
    public static final String CAJ_PATH = UPLOAD_PATH;//"E:\\manager\\springboot\\cajinput";

    //caj转pdf配置
    public static final String CAJ2PDF_CONVERTER_EXE = "E:\\manager\\springboot\\src\\main\\java\\com\\example\\utils\\Caj2pdf\\caj2pdf.exe";
    public static final String CAJ2PDF_MUTOOL_EXE = "E:\\manager\\springboot\\src\\main\\java\\com\\example\\utils\\Caj2pdf\\mutool.exe";

    //pdf转docx配置
    public static final String PDF2DOCX_PY_SCRIPT = System.getenv().getOrDefault("PDF2DOCX_PY_SCRIPT", "/app/scripts/pdf_converter.py");
    public static final int PDF2DOCX_TIMEOUT_MINUTES = Integer.parseInt(System.getenv().getOrDefault("PDF2DOCX_TIMEOUT_MINUTES", "30"));

    //pdf2txt配置
    public static final String PDF2TXT_PY_SCRIPT = System.getenv().getOrDefault("PDF2TXT_PY_SCRIPT", "/app/scripts/pdf_to_text.py");
    public static final int PDF2TXT_TIMEOUT_MINUTES = Integer.parseInt(System.getenv().getOrDefault("PDF2TXT_TIMEOUT_MINUTES", "30"));
    public static final String OCR_PATH = System.getenv().getOrDefault("OCR_PATH", "/usr/bin");
    public static final String LOG_PATH = System.getenv().getOrDefault("LOG_PATH", "/app/log");


    public static final String JSON = """
            {
            "short1": "第1种对论文摘要的总结凝练，30字左右",
            "short2": "第2种对论文摘要的总结凝练，30字左右",
            "short3": "第3种对论文摘要的总结凝练，30字左右",
            "short4": "第4种对论文摘要的总结凝练，30字左右",
            "short5": "第5种对论文摘要的总结凝练，30字左右",
            "short6": "第6种对论文摘要的总结凝练，30字左右，要用通俗易懂的语言",
            "mid1": "第1种对论文摘要的总结凝练，50字左右",
            "mid2": "第2种对论文摘要的总结凝练，50字左右",
            "mid3": "第3种对论文摘要的总结凝练，50字左右",
            "mid4": "第4种对论文摘要的总结凝练，50字左右",
            "mid5": "第5种对论文摘要的总结凝练，50字左右",
            "mid6": "第6种对论文摘要的总结凝练，50字左右，要用通俗易懂的语言",
            "long1": "第1种对论文摘要的总结凝练，100字左右",
            "long2": "第2种对论文摘要的总结凝练，100字左右",
            "long3": "第3种对论文摘要的总结凝练，100字左右",
            "long4": "第4种对论文摘要的总结凝练，100字左右",
            "long5": "第5种对论文摘要的总结凝练，100字左右",
            "long6": "第6种对论文摘要的总结凝练，100字左右，要用通俗易懂的语言",
            "target": "用通俗易懂的语言简述论文的研究动机",
            "algmid1": "第1种介绍本文用到的核心算法，50字左右",
            "algmid2": "第2种介绍本文用到的核心算法，50字左右",
            "algmid3": "第3种介绍本文用到的核心算法，50字左右",
            "algmid4": "第4种介绍本文用到的核心算法，50字左右，用通俗易懂的语言",
            "alglong1": "第1种介绍本文用到的核心算法，100字左右",
            "alglong2": "第2种介绍本文用到的核心算法，100字左右",
            "alglong3": "第3种介绍本文用到的核心算法，100字左右",
            "alglong4": "第4种介绍本文用到的核心算法，100字左右，用通俗易懂的语言",
            "environment": "详细介绍本论文的实验环境",
            "tools": "详细介绍本论文的实验工具",
            "datas": "详细介绍本论文的实验数据",
            "standard": "详细介绍本论文的实验指标",
            "result": "详细介绍本论文的实验结果",
            "future": "从不同角度尽可能详细介绍本论文对未来工作的总结与展望",
            "weekpoint": "从不同角度尽可能详细介绍本论文已有研究的不足之处",
            "keyword": "文本的关键词，用;分隔",
            "summary": "提取论文完整摘要"}""";
    public static final String JSON2 = """
            {
            "short1": "你认为第一个大模型的结果好还是第二个，填写1或者2",
            "short2": "填写1或者2",
            "short3": "填写1或者2",
            "short4": "填写1或者2",
            "short5": "填写1或者2",
            "short6": "填写1或者2",
            "mid1": "填写1或者2",
            "mid2": "填写1或者2",
            "mid3": "填写1或者2",
            "mid4": "填写1或者2",
            "mid5": "填写1或者2",
            "mid6": "填写1或者2",
            "long1": "填写1或者2",
            "long2": "填写1或者2",
            "long3": "填写1或者2",
            "long4": "填写1或者2",
            "long5": "填写1或者2",
            "long6": "填写1或者2",
            "target": "填写1或者2",
            "algmid1": "填写1或者2",
            "algmid2": "填写1或者2",
            "algmid3": "填写1或者2",
            "algmid4": "填写1或者2",
            "alglong1": "填写1或者2",
            "alglong2": "填写1或者2",
            "alglong3": "填写1或者2",
            "alglong4": "填写1或者2",
            "environment": "填写1或者2",
            "tools": "填写1或者2",
            "datas": "填写1或者2",
            "standard": "填写1或者2",
            "result": "填写1或者2",
            "future": "填写1或者2",
            "weekpoint": "填写1或者2",
            "keyword": "填写1或者2",
            "summary": "填写1或者2"}""";
}