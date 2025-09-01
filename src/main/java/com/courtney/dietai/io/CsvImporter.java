package com.courtney.dietai.io;

import com.courtney.dietai.model.DietEntry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvImporter {

    // Supported headers (case-insensitive) plus common synonyms
    private static final Set<String> H_DATE = set("date", "log date", "entry date", "day", "timestamp", "datetime");
    private static final Set<String> H_ITEM = set("item", "food", "description", "name", "food name");
    private static final Set<String> H_QTY = set("quantity", "quantity / duration", "qty", "serving", "servings", "amount", "duration");
    private static final Set<String> H_CAL = set("calories", "kcal", "energy (kcal)", "calories (kcal)");
    private static final Set<String> H_CAR = set("carbohydrates", "carbs", "carbohydrate (g)", "carbs (g)");
    private static final Set<String> H_PRO = set("protein", "protein (g)");
    private static final Set<String> H_FAT = set("fat", "total fat", "fat (g)");
    private static final Set<String> H_SOD = set("sodium", "sodium (mg)", "sodium (g)", "salt", "salt (mg)", "salt (g)");
    private static final Set<String> H_FIB = set("fiber", "dietary fiber", "fiber (g)");
    private static final Set<String> H_MEAL = set("meal", "meal type", "meal_name", "category");
    private static final Set<String> H_NOTES = set("notes", "note", "comment", "comments");

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    public static List<DietEntry> importFile(File file) throws Exception {
        List<DietEntry> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreSurroundingSpaces(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            Map<String, String> headerMap = normalizeHeaderMap(parser.getHeaderMap().keySet());
            for (CSVRecord record : parser) {
                try {
                    DietEntry e = parseRecord(record, headerMap);
                    if (e != null) result.add(e);
                } catch (Exception ex) {
                    System.err.println("Skipping row " + record.getRecordNumber() + ": " + ex.getMessage());
                }
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("No valid entries found. Check the CSV headers and values.");
        }
        return result;
    }

    private static DietEntry parseRecord(CSVRecord r, Map<String, String> hm) {
        LocalDate date = parseDate(get(r, hm, H_DATE));
        if (date == null) {
            date = tryAnyDate(r);
            if (date == null) throw new IllegalArgumentException("Unparseable date.");
        }

        String meal = orDefault(get(r, hm, H_MEAL), "Meal");
        String item = orDefault(get(r, hm, H_ITEM), "Item");
        String qty = orDefault(get(r, hm, H_QTY), "");

        double calories = parseDoubleOrZero(get(r, hm, H_CAL));
        double carbs = parseDoubleOrZero(get(r, hm, H_CAR));
        double protein = parseDoubleOrZero(get(r, hm, H_PRO));
        double fat = parseDoubleOrZero(get(r, hm, H_FAT));
        double fiber = parseDoubleOrZero(get(r, hm, H_FIB));

        double sodium = parseSodium(r, hm);

        String notes = orDefault(get(r, hm, H_NOTES), "");

        return new DietEntry(date, meal, item, qty, calories, carbs, protein, fat, sodium, fiber, notes);
    }

    private static double parseSodium(CSVRecord r, Map<String, String> hm) {
        String raw = get(r, hm, H_SOD);
        if (raw == null) return 0;
        String matchedKey = matchedHeaderKey(hm, H_SOD);
        boolean headerSuggestsG = matchedKey != null && matchedKey.toLowerCase().contains("(g)");
        boolean headerSuggestsMg = matchedKey != null && matchedKey.toLowerCase().contains("(mg)");
        double val = parseDoubleOrZero(raw);
        if (headerSuggestsG) return val * 1000.0;
        if (headerSuggestsMg) return val;
        if (val > 50) return val; // likely mg
        return val; // treat as mg
    }

    private static String matchedHeaderKey(Map<String, String> hm, Set<String> candidates) {
        for (Map.Entry<String, String> e : hm.entrySet()) {
            if (candidates.contains(e.getValue())) return e.getKey();
        }
        return null;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        String v = s.trim();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try { return LocalDate.parse(v, fmt); } catch (Exception ignore) {}
        }
        if (v.length() >= 10) {
            String first10 = v.substring(0, 10);
            for (DateTimeFormatter fmt : DATE_FORMATS) {
                try { return LocalDate.parse(first10, fmt); } catch (Exception ignore) {}
            }
        }
        return null;
    }

    private static LocalDate tryAnyDate(CSVRecord r) {
        for (String val : r) {
            LocalDate d = parseDate(val);
            if (d != null) return d;
        }
        return null;
    }

    private static String get(CSVRecord r, Map<String, String> headerMap, Set<String> keys) {
        for (Map.Entry<String, String> e : headerMap.entrySet()) {
            if (keys.contains(e.getValue())) {
                String v = safeGet(r, e.getKey());
                if (v != null) return v;
            }
        }
        return null;
    }

    private static String safeGet(CSVRecord r, String key) {
        try {
            if (!r.isMapped(key)) return null;
            String v = r.get(key);
            if (v == null) return null;
            v = v.trim();
            return v.isEmpty() ? null : v;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String orDefault(String s, String def) {
        return (s == null || s.isBlank()) ? def : s.trim();
    }

    private static double parseDoubleOrZero(String raw) {
        if (raw == null || raw.isBlank()) return 0.0;
        String s = raw.trim().replaceAll("[^0-9,.-]", "");
        if (s.isBlank()) return 0.0;
        try {
            if (s.contains(",") && !s.contains(".")) s = s.replace(',', '.');
            else s = s.replace(",", "");
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            try { return NumberFormat.getNumberInstance().parse(s).doubleValue(); }
            catch (ParseException ex) { return 0.0; }
        }
    }

    private static Map<String, String> normalizeHeaderMap(Set<String> headers) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String h : headers) map.put(h, normalize(h));
        return map;
    }

    private static String normalize(String s) {
        String v = s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
        v = v.replaceAll("\\s+", " ");
        return v;
    }

    private static Set<String> set(String... items) {
        return new HashSet<>(Arrays.asList(items));
    }
}