package org.group35.service;

import org.group35.model.Transaction;
import org.group35.util.CsvUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for importing transactions from a CSV file.
 * Reads raw CSV records via CsvUtils and maps them to Transaction instances.
 */
public class CsvImport {
    private final CsvUtils csvUtils;

    public CsvImport() {
        this.csvUtils = new CsvUtils();
    }

    /**
     * Parse the CSV at the given file path into a list of Transaction objects.
     * @param filePath filesystem path to the CSV
     * @return list of Transaction instances
     * @throws IOException if the file cannot be read
     */
    public List<Transaction> parseTransactions(String filePath) throws IOException {
        Path path = Path.of(filePath);
        List<Map<String, Object>> rawRecords = csvUtils.readTransactions(path, StandardCharsets.UTF_8, null);
        return rawRecords.stream()
                .map(this::mapToTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Map a raw CSV-record map to a Transaction model.
     */
    private Transaction mapToTransaction(Map<String, Object> rec) {
        Transaction tx = new Transaction();
        // CSV header 'payee' → Transaction.name
        Object payee = rec.get("payee");
        if (payee != null) {
            tx.setName(payee.toString());
        }

        // CSV header 'date' → Transaction.timestamp
        Object dateObj = rec.get("date");
        if (dateObj instanceof LocalDate) {
            tx.setTimestamp(((LocalDate) dateObj).atStartOfDay());
        } else if (dateObj instanceof LocalDateTime) {
            tx.setTimestamp((LocalDateTime) dateObj);
        }

        // CSV header 'amount' → Transaction.amount
        Object amt = rec.get("amount");
        if (amt instanceof BigDecimal) {
            tx.setAmount((BigDecimal) amt);
        } else if (amt != null) {
            tx.setAmount(new BigDecimal(amt.toString()));
        }

        return tx;
    }
}
