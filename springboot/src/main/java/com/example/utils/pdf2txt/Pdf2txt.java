package com.example.utils.pdf2txt;

import com.example.utils.Config;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

import static com.example.utils.Config.*;

public class Pdf2txt {
    private static final String PY_SCRIPT = Config.PDF2TXT_PY_SCRIPT; // PY_SCRIPT is an absolute path
    private static final int TIMEOUT_MINUTES = Config.PDF2TXT_TIMEOUT_MINUTES;

    public static void runpdf2txt() {
        String logDir = null;
        try {
            logDir = LOG_PATH;  // Log directory

            // Use absolute paths directly
            File inputDirAbs = new File(PDF_PATH);
            File outputDirAbs = new File(TXT_PATH);
            File pytesseractDirAbs = new File(OCR_PATH);
            File logDirAbs = new File(logDir);

            // Validate directory structure
            validateDirectory(inputDirAbs, "Input directory");
            createDirectory(outputDirAbs, "Output directory");
            createDirectory(logDirAbs, "Log directory");
            validatePythonScript();

            // Build Python command
            String[] command = buildPythonCommand(inputDirAbs, outputDirAbs, pytesseractDirAbs);

            // Execute conversion
            executeConversion(command, logDirAbs);

        } catch (Exception e) {
            log(logDir, "Program error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void validateDirectory(File dir, String dirName) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException(dirName + " not found: " + dir.getAbsolutePath());
        }
    }

    private static void createDirectory(File dir, String dirName) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create " + dirName + ": " + dir.getAbsolutePath());
        }
    }

    private static void validatePythonScript() throws IOException {
        File script = new File(PY_SCRIPT); // Use PY_SCRIPT absolute path
        if (!script.exists()) {
            throw new IOException("Python script not found: " + script.getAbsolutePath());
        }
    }

    private static String[] buildPythonCommand(File inputDir, File outputDir, File pytesseractDir) {
        return new String[]{
                "python3",
                "-u",
                PY_SCRIPT, // Use PY_SCRIPT absolute path
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
            log(logDir.getAbsolutePath(), "Conversion timeout");
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
                log(logDir.getAbsolutePath(), "Output read error: " + e.getMessage());
            }
        }).start();
    }

    private static void logExitStatus(Process process, File logDir) {
        int exitCode = process.exitValue();
        String status = exitCode == 0 ? "Conversion completed successfully" : "Conversion failed, exit code: " + exitCode;
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
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}