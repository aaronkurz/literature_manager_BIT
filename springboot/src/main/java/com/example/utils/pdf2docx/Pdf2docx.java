package com.example.utils.pdf2docx;

import com.example.utils.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

public class Pdf2docx {
    private static final String PY_SCRIPT = Config.PDF2DOCX_PY_SCRIPT; // Python脚本路径
    private static final String INPUT_DIR = Config.PDF_PATH; // PDF输入目录
    private static final String OUTPUT_DIR = Config.DOCX_PATH; // DOCX输出目录
    private static final int TIMEOUT_MINUTES = Config.PDF2DOCX_TIMEOUT_MINUTES; // 超时时间

    public static void runPdfToDocx() {
        try {
            convertAllPdfFiles();
        } catch (Exception e) {
            LogUtil_pdf2docx.log("程序异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void convertAllPdfFiles() throws IOException, InterruptedException {
        File inputDir = new File(INPUT_DIR);
        File outputDir = new File(OUTPUT_DIR);

        validateDirectories(inputDir, outputDir);
        validatePythonScript();

        File[] pdfFiles = inputDir.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".pdf")
        );

        if (pdfFiles == null || pdfFiles.length == 0) {
            LogUtil_pdf2docx.log("未找到PDF文件");
            return;
        }

        for (File pdfFile : pdfFiles) {
            processPdfFile(pdfFile, outputDir);
        }
    }

    private static void validateDirectories(File inputDir, File outputDir) throws IOException {
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new IOException("PDF输入目录不存在: " + inputDir.getAbsolutePath());
        }

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("无法创建输出目录: " + outputDir.getAbsolutePath());
        }
    }

    private static void validatePythonScript() throws IOException {
        File script = new File(PY_SCRIPT);
        if (!script.exists()) {
            throw new IOException("Python脚本未找到: " + script.getAbsolutePath());
        }
    }

    private static void processPdfFile(File pdfFile, File outputDir)
            throws IOException, InterruptedException {
        String docxName = pdfFile.getName().replaceAll("(?i)\\.pdf$", ".docx");
        File docxFile = new File(outputDir, docxName);

        if (docxFile.exists()) {
            LogUtil_pdf2docx.log("跳过已转换文件: " + pdfFile.getName());
            return;
        }

        LogUtil_pdf2docx.log("开始处理: " + pdfFile.getName());

        // 使用绝对路径调用Python脚本
        String[] command = {
                "python3",
                "-u", // 强制无缓冲输出
                PY_SCRIPT,
                pdfFile.getAbsolutePath(),
                docxFile.getAbsolutePath()
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 启动独立线程读取输出
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LogUtil_pdf2docx.log("[PY] " + line); // 将Python输出写入日志
                }
            } catch (IOException e) {
                LogUtil_pdf2docx.log("读取Python输出失败: " + e.getMessage());
            }
        });
        outputThread.start();

        // 设置超时控制
        boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            LogUtil_pdf2docx.log("转换超时: " + pdfFile.getName());
            return;
        }

        int exitCode = process.exitValue();
        if (exitCode == 0) {
            LogUtil_pdf2docx.log("转换成功: " + pdfFile.getName());
        } else {
            LogUtil_pdf2docx.log("转换失败: " + pdfFile.getName() + " (退出码: " + exitCode + ")");
        }
    }
}