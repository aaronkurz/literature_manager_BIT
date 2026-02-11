package com.example.utils.pdf2docx;

import com.example.utils.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

public class Pdf2docx {
    private static final String PY_SCRIPT = Config.PDF2DOCX_PY_SCRIPT; // Python script path
    private static final String INPUT_DIR = Config.PDF_PATH; // PDF input directory
    private static final String OUTPUT_DIR = Config.DOCX_PATH; // DOCX output directory
    private static final int TIMEOUT_MINUTES = Config.PDF2DOCX_TIMEOUT_MINUTES; // Timeout

    public static void runPdfToDocx() {
        try {
            convertAllPdfFiles();
        } catch (Exception e) {
            LogUtil_pdf2docx.log("Program error: " + e.getMessage());
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
            LogUtil_pdf2docx.log("No PDF files found");
            return;
        }

        for (File pdfFile : pdfFiles) {
            processPdfFile(pdfFile, outputDir);
        }
    }

    private static void validateDirectories(File inputDir, File outputDir) throws IOException {
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new IOException("PDF input directory not found: " + inputDir.getAbsolutePath());
        }

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Failed to create output directory: " + outputDir.getAbsolutePath());
        }
    }

    private static void validatePythonScript() throws IOException {
        File script = new File(PY_SCRIPT);
        if (!script.exists()) {
            throw new IOException("Python script not found: " + script.getAbsolutePath());
        }
    }

    private static void processPdfFile(File pdfFile, File outputDir)
            throws IOException, InterruptedException {
        String docxName = pdfFile.getName().replaceAll("(?i)\\.pdf$", ".docx");
        File docxFile = new File(outputDir, docxName);

        if (docxFile.exists()) {
            LogUtil_pdf2docx.log("Skipping already converted file: " + pdfFile.getName());
            return;
        }

        LogUtil_pdf2docx.log("Processing: " + pdfFile.getName());

        // Call Python script with absolute path
        String[] command = {
                "python3",
                "-u", // Force unbuffered output
                PY_SCRIPT,
                pdfFile.getAbsolutePath(),
                docxFile.getAbsolutePath()
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Start thread to read output
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LogUtil_pdf2docx.log("[PY] " + line); // Write Python output to log
                }
            } catch (IOException e) {
                LogUtil_pdf2docx.log("Failed to read Python output: " + e.getMessage());
            }
        });
        outputThread.start();

        // Set timeout
        boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            LogUtil_pdf2docx.log("Conversion timeout: " + pdfFile.getName());
            return;
        }

        int exitCode = process.exitValue();
        if (exitCode == 0) {
            LogUtil_pdf2docx.log("Conversion successful: " + pdfFile.getName());
        } else {
            LogUtil_pdf2docx.log("Conversion failed: " + pdfFile.getName() + " (exit code: " + exitCode + ")");
        }
    }
}