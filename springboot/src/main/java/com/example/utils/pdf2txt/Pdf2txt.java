package com.example.utils.pdf2txt;

import com.example.utils.Config;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

import static com.example.utils.Config.*;

public class Pdf2txt {
    private static final String PY_SCRIPT = Config.PDF2TXT_PY_SCRIPT; // 假设 PY_SCRIPT 已经是绝对路径
    private static final int TIMEOUT_MINUTES = Config.PDF2TXT_TIMEOUT_MINUTES;

    public static void runpdf2txt() {
        String logDir = null;
        try {
            logDir = LOG_PATH;  // 日志目录

            // 直接使用绝对路径，不再需要 resolvePath
            File inputDirAbs = new File(PDF_PATH);
            File outputDirAbs = new File(TXT_PATH);
            File pytesseractDirAbs = new File(OCR_PATH);
            File logDirAbs = new File(logDir);

            // 验证目录结构
            validateDirectory(inputDirAbs, "输入目录");
            createDirectory(outputDirAbs, "输出目录");
            createDirectory(logDirAbs, "日志目录");
            validatePythonScript();

            // 构建Python命令
            String[] command = buildPythonCommand(inputDirAbs, outputDirAbs, pytesseractDirAbs);

            // 执行转换过程
            executeConversion(command, logDirAbs);

        } catch (Exception e) {
            log(logDir, "程序异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void validateDirectory(File dir, String dirName) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException(dirName + "不存在: " + dir.getAbsolutePath());
        }
    }

    private static void createDirectory(File dir, String dirName) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("无法创建" + dirName + ": " + dir.getAbsolutePath());
        }
    }

    private static void validatePythonScript() throws IOException {
        File script = new File(PY_SCRIPT); // 直接使用 PY_SCRIPT 绝对路径
        if (!script.exists()) {
            throw new IOException("Python脚本未找到: " + script.getAbsolutePath());
        }
    }

    private static String[] buildPythonCommand(File inputDir, File outputDir, File pytesseractDir) {
        return new String[]{
                "python3",
                "-u",
                PY_SCRIPT, // 直接使用 PY_SCRIPT 绝对路径
                "--input_dir", inputDir.getAbsolutePath(),
                "--output_dir", outputDir.getAbsolutePath(),
                "--pytesseract_dir", pytesseractDir.getAbsolutePath()
        };
    }

    private static void executeConversion(String[] command, File logDir)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        startOutputLogger(process, logDir);

        boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            log(logDir.getAbsolutePath(), "转换超时");
            return;
        }

        logExitStatus(process, logDir);
    }

    private static void startOutputLogger(Process process, File logDir) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log(logDir.getAbsolutePath(), "[PY] " + line);
                }
            } catch (IOException e) {
                log(logDir.getAbsolutePath(), "输出读取错误: " + e.getMessage());
            }
        }).start();
    }

    private static void logExitStatus(Process process, File logDir) {
        int exitCode = process.exitValue();
        String status = exitCode == 0 ? "转换成功完成" : "转换失败，退出码: " + exitCode;
        log(logDir.getAbsolutePath(), status);
    }

    private static void log(String logDir, String message) {
        try {
            File dir = new File(logDir);
            if (!dir.exists()) dir.mkdirs();

            File logFile = new File(dir, "pdf2txt.log");
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.printf("[%s] %s%n", timestamp, message);
            }
        } catch (IOException e) {
            System.err.println("日志写入失败: " + e.getMessage());
        }
    }
}