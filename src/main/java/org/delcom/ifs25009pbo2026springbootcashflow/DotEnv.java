package org.delcom.ifs25009pbo2026springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Pemuat sederhana file {@code .env} ke System properties.
 * Dipanggil sebelum Spring Boot start agar placeholder seperti ${APP_PORT:8080}
 * pada application.yaml dapat terisi. Variabel environment OS dan System property
 * yang sudah ada tidak akan ditimpa.
 */
public final class DotEnv {
    private DotEnv() {
    }

    /** Memuat file {@code .env} dari direktori kerja saat ini (diabaikan jika tidak ada). */
    public static void load() {
        load(Path.of(".env"));
    }

    /** Memuat file env pada path tertentu (diabaikan jika tidak ada atau tidak terbaca). */
    static void load(Path file) {
        if (!Files.isRegularFile(file)) {
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            return;
        }
        for (String rawLine : lines) {
            parseLine(rawLine);
        }
    }

    private static void parseLine(String rawLine) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        if (line.startsWith("export ")) {
            line = line.substring("export ".length()).trim();
        }
        int separator = line.indexOf('=');
        if (separator <= 0) {
            return;
        }
        String key = line.substring(0, separator).trim();
        String value = unquote(line.substring(separator + 1).trim());
        if (key.isEmpty()) {
            return;
        }
        if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
