package com.example.utils.docling;

import com.example.utils.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Runs the docling Python extractor to produce a condensed JSON view of a PDF.
 */
public class DoclingExtractor {
    private static final String PY_SCRIPT = Config.DOCLING_PY_SCRIPT;
    private static final int TIMEOUT_MINUTES = Config.DOCLING_TIMEOUT_MINUTES;

    /**
     * Convert a single PDF to a docling JSON at the same base path (e.g., foo.pdf -> foo.docling.json).
     * Returns the JSON path if successful, otherwise null.
     */
    public static String runDocling(String pdfPath) {
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            log("PDF not found: " + pdfPath);
            return null;
        }

        String jsonPath = pdfPath.replaceAll("(?i)\\.pdf$", ".docling.json");
        String[] command = new String[]{
                "python3", "-u", PY_SCRIPT,
                "--input_pdf", pdfFile.getAbsolutePath(),
                "--output_json", jsonPath
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            logProcessOutput(process);
            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                log("Docling extraction timed out");
                return null;
            }
            if (process.exitValue() != 0) {
                log("Docling extraction failed with exit code " + process.exitValue());
                return null;
            }
            return jsonPath;
        } catch (Exception e) {
            log("Docling extraction error: " + e.getMessage());
            return null;
        }
    }

    private static void logProcessOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log("[docling] " + line);
                }
            } catch (IOException e) {
                log("[docling] output read error: " + e.getMessage());
            }
        }).start();
    }

    private static void log(String msg) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "] " + msg);
    }
}
