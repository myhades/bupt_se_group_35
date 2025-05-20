package org.group35.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.StreamSupport;

public class CsvUtils {
    // Standard header variants for normalization
    private static final Map<String, List<String>> STANDARD_HEADERS = new HashMap<>();
    static {
        STANDARD_HEADERS.put("date", Arrays.asList("date", "transaction date", "posted date", "value date"));
        STANDARD_HEADERS.put("amount", Arrays.asList("amount", "transaction amount", "amt", "value"));
        STANDARD_HEADERS.put("payee", Arrays.asList("description", "payee", "narrative", "details"));
        STANDARD_HEADERS.put("balance", Arrays.asList("balance", "running balance", "account balance"));
        STANDARD_HEADERS.put("type", Arrays.asList("type", "transaction type", "debit/credit"));
        STANDARD_HEADERS.put("memo", Arrays.asList("memo", "notes", "narration"));
    }

    private final Map<String, String> headerMap;
    private final List<DateTimeFormatter> dateFormatters;

    public CsvUtils() {
        this.headerMap = buildHeaderMap();
        this.dateFormatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
    }

    public CsvUtils(Map<String, String> headerMap, List<DateTimeFormatter> dateFormatters) {
        this.headerMap = headerMap;
        this.dateFormatters = dateFormatters;
    }

    private Map<String, String> buildHeaderMap() {
        Map<String, String> inv = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : STANDARD_HEADERS.entrySet()) {
            String stdKey = entry.getKey();
            for (String alias : entry.getValue()) {
                inv.put(alias.toLowerCase(), stdKey);
            }
        }
        return inv;
    }

    public CSVFormat detectFormat(Path file, Charset encoding) throws IOException {
        String content = Files.readString(file, encoding);
        String sample = content.length() > 2048 ? content.substring(0, 2048) : content;
        char delim = detectDelimiter(sample);
        return CSVFormat.DEFAULT.builder()
                .setDelimiter(delim)
                .setTrim(true)
                .build();
    }

    private char detectDelimiter(String sample) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : new char[]{',', ';', '\t', '|'}) {
            counts.put(c, 0);
        }
        for (char c : sample.toCharArray()) {
            if (counts.containsKey(c)) {
                counts.put(c, counts.get(c) + 1);
            }
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(',');
    }

    public List<String> normalizeHeaders(String[] headers) {
        List<String> norm = new ArrayList<>();
        for (String h : headers) {
            String key = h.trim().toLowerCase();
            norm.add(headerMap.getOrDefault(key, key));
        }
        return norm;
    }

    public LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter fmt : dateFormatters) {
            try {
                return LocalDate.parse(dateStr, fmt);
            } catch (DateTimeParseException e) {
                // try next
            }
        }
        throw new IllegalArgumentException("Unknown date format: " + dateStr);
    }

    public BigDecimal parseAmount(String amtStr) {
        String cleaned = amtStr.replaceAll("[,\\$€]", "").trim();
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown amount format: " + amtStr, e);
        }
    }

    public List<Map<String, Object>> readTransactions(Path file, Charset encoding, Character delimiter) throws IOException {
        CSVFormat format = delimiter != null
                ? CSVFormat.DEFAULT.builder().setDelimiter(delimiter).setTrim(true).build()
                : detectFormat(file, encoding);
        List<Map<String, Object>> records = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file, encoding);
             CSVParser parser = new CSVParser(reader, format)) {
            Iterator<CSVRecord> it = parser.iterator();
            if (!it.hasNext()) return records;
            CSVRecord headerRecord = it.next();
            String[] rawHeaders = StreamSupport.stream(headerRecord.spliterator(), false)
                    .toArray(String[]::new);
            List<String> normHeaders = normalizeHeaders(rawHeaders);

            while (it.hasNext()) {
                CSVRecord rec = it.next();
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < normHeaders.size(); i++) {
                    String key = normHeaders.get(i);
                    String val = rec.get(i).trim();
                    switch (key) {
                        case "date":
                            map.put(key, parseDate(val));
                            break;
                        case "amount":
                            map.put(key, parseAmount(val));
                            break;
                        default:
                            map.put(key, val);
                    }
                }
                records.add(map);
            }
        }
        return records;
    }

    public void writeCsv(List<Map<String, Object>> records, Path file, List<String> headers, Charset encoding, char delimiter) throws IOException {
        if (headers == null || headers.isEmpty()) {
            headers = new ArrayList<>(records.isEmpty() ? Collections.emptyList() : records.get(0).keySet());
        }
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader(headers.toArray(new String[0]))
                .build();
        try (BufferedWriter writer = Files.newBufferedWriter(file, encoding);
             CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Map<String, Object> rec : records) {
                List<Object> row = new ArrayList<>();
                for (String h : headers) {
                    row.add(rec.getOrDefault(h, ""));
                }
                printer.printRecord(row);
            }
        }
    }
}