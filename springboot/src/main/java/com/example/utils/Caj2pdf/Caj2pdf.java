package com.example.utils.Caj2pdf;



import com.example.utils.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Caj2pdf {
    private static final String INPUT_DIR = Config.CAJ_PATH;
    private static final String OUTPUT_DIR = Config.PDF_PATH;
    private static final String CONVERTER_EXE = Config.CAJ2PDF_CONVERTER_EXE;
    private static final String MUTOOL_EXE = Config.CAJ2PDF_MUTOOL_EXE;


    public static void runCajToPdf() throws IOException, InterruptedException {
        // Use absolute path directly
        File inputDir = new File(INPUT_DIR);
        File outputDir = new File(OUTPUT_DIR);

        validateDirectories(inputDir, outputDir);
        validateExecutables();

        File[] cajFiles = inputDir.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".caj")
        );

        if (cajFiles == null || cajFiles.length == 0) {
            LogUtil_caj2pdf.log("No CAJ files to process");
            return;
        }

        for (File cajFile : cajFiles) {
            processCajFile(cajFile, outputDir);
        }
    }

    private static void validateDirectories(File inputDir, File outputDir) throws IOException {
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            LogUtil_caj2pdf.log("CAJ folder not found: " + inputDir.getAbsolutePath());
        }

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            LogUtil_caj2pdf.log("Failed to create output folder: " + outputDir.getAbsolutePath());
        }
    }

    private static void validateExecutables() throws IOException {
        if (!new File(CONVERTER_EXE).exists()) {
            LogUtil_caj2pdf.log("caj2pdf.exe tool missing: " + CONVERTER_EXE);
        }

        if (!new File(MUTOOL_EXE).exists()) {
            LogUtil_caj2pdf.log("mutool.exe tool missing: " + MUTOOL_EXE);
        }
    }

    private static void processCajFile(File cajFile, File outputDir) throws IOException, InterruptedException {
        String pdfFileName = cajFile.getName().replaceAll("(?i)\\.caj$", ".pdf");
        File pdfFile = new File(outputDir, pdfFileName);

        if (pdfFile.exists()) {
            LogUtil_caj2pdf.log("Already converted, skipping: " + cajFile.getName());
            return;
        }

        LogUtil_caj2pdf.log("Processing: " + cajFile.getName());

        String[] command = {
                CONVERTER_EXE,
                "convert",
                "-m",
                MUTOOL_EXE,
                cajFile.getAbsolutePath(),
                "-o",
                pdfFile.getAbsolutePath()
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LogUtil_caj2pdf.log(line);
            }
        }

        if (exitCode == 0) {
            LogUtil_caj2pdf.log("Conversion successful: " + pdfFileName);
        } else {
            LogUtil_caj2pdf.log("Conversion failed: " + cajFile.getName() + " (Exit code: " + exitCode + ")");
        }
    }
}