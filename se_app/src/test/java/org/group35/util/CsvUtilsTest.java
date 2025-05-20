package org.group35.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvUtilsTest {

    private static final String SAMPLE_CSV =
            "Transaction Date,Transaction Amount,Payee,Balance,Type,Memo\n" +
                    "2025-05-20,100.50,Acme Corp,1000.00,credit,Salary\n" +
                    "05/19/2025,-25.75,Coffee Shop,974.25,debit,Coffee purchase\n";

    @TempDir
    Path tempDir;

    @Test
    void testReadTransactionsWithDefaultDelimiter() throws IOException {
        // Write sample CSV to a temp file
        Path csvFile = tempDir.resolve("transactions.csv");
        Files.writeString(csvFile, SAMPLE_CSV, StandardCharsets.UTF_8);

        CsvUtils utils = new CsvUtils();
        List<Map<String, Object>> records = utils.readTransactions(csvFile, StandardCharsets.UTF_8, null);

        // Expect two records
        assertEquals(2, records.size());

        Map<String, Object> first = records.get(0);
        assertEquals(LocalDate.of(2025, 5, 20), first.get("date"));
        assertEquals(new BigDecimal("100.50"), first.get("amount"));
        assertEquals("Acme Corp", first.get("payee"));
        assertEquals("1000.00", first.get("balance"));
        assertEquals("credit", first.get("type"));
        assertEquals("Salary", first.get("memo"));

        Map<String, Object> second = records.get(1);
        assertEquals(LocalDate.of(2025, 5, 19), second.get("date"));
        assertEquals(new BigDecimal("-25.75"), second.get("amount"));
        assertEquals("Coffee Shop", second.get("payee"));
        assertEquals("974.25", second.get("balance"));
        assertEquals("debit", second.get("type"));
        assertEquals("Coffee purchase", second.get("memo"));
    }

    @Test
    void testWriteCsvAndRoundTrip(@TempDir Path tempDir) throws IOException {
        // Prepare records manually
        CsvUtils utils = new CsvUtils();
        List<Map<String, Object>> input = List.of(
                Map.of(
                        "date", LocalDate.of(2025,1,1),
                        "amount", new BigDecimal("50.00"),
                        "payee", "Grocery Store",
                        "balance", "1050.00",
                        "type", "debit",
                        "memo", "Weekly shopping"
                )
        );

        // Write to CSV
        Path outFile = tempDir.resolve("out.csv");
        utils.writeCsv(input, outFile, null, StandardCharsets.UTF_8, ',');

        // Read back
        List<Map<String, Object>> output = utils.readTransactions(outFile, StandardCharsets.UTF_8, ',');
        assertEquals(1, output.size());
        Map<String, Object> rec = output.get(0);

        // Verify round-trip values
        assertEquals(LocalDate.of(2025,1,1), rec.get("date"));
        assertEquals(new BigDecimal("50.00"), rec.get("amount"));
        assertEquals("Grocery Store", rec.get("payee"));
        assertEquals("1050.00", rec.get("balance"));
        assertEquals("debit", rec.get("type"));
        assertEquals("Weekly shopping", rec.get("memo"));
    }
}
