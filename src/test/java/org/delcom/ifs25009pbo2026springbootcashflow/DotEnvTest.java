package org.delcom.ifs25009pbo2026springbootcashflow;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DotEnvTest {

    @Test
    void loadsKeyValuePairsAndIgnoresCommentsAndBlankLines() throws Exception {
        Path file = Files.createTempFile("cashflow", ".env");
        try {
            Files.writeString(file, String.join("\n",
                    "# komentar",
                    "",
                    "CASHFLOW_TEST_A=123",
                    "export CASHFLOW_TEST_B = \"dengan spasi\"",
                    "CASHFLOW_TEST_C='kutip tunggal'",
                    "tanpa-pemisah",
                    "=tanpa-kunci"));

            DotEnv.load(file);

            assertEquals("123", System.getProperty("CASHFLOW_TEST_A"));
            assertEquals("dengan spasi", System.getProperty("CASHFLOW_TEST_B"));
            assertEquals("kutip tunggal", System.getProperty("CASHFLOW_TEST_C"));
        } finally {
            Files.deleteIfExists(file);
            System.clearProperty("CASHFLOW_TEST_A");
            System.clearProperty("CASHFLOW_TEST_B");
            System.clearProperty("CASHFLOW_TEST_C");
        }
    }

    @Test
    void doesNotOverrideExistingSystemProperty() throws Exception {
        Path file = Files.createTempFile("cashflow", ".env");
        System.setProperty("CASHFLOW_TEST_D", "asli");
        try {
            Files.writeString(file, "CASHFLOW_TEST_D=baru");

            DotEnv.load(file);

            assertEquals("asli", System.getProperty("CASHFLOW_TEST_D"));
        } finally {
            Files.deleteIfExists(file);
            System.clearProperty("CASHFLOW_TEST_D");
        }
    }

    @Test
    void missingFileIsIgnored() {
        DotEnv.load(Path.of("file-yang-tidak-ada.env"));
        DotEnv.load();
        assertNull(System.getProperty("CASHFLOW_TEST_TIDAK_ADA"));
    }
}
