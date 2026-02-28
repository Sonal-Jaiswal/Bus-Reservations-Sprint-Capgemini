package com.bus.cleaning.repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvBookingReader implements BookingReader {
    @Override
    public List<Map<String, String>> readAll(String path) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            String headerLine = bufferedReader.readLine();
            if (headerLine == null) {
                return rows;
            }
            String[] headers = headerLine.split(",", -1);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",", -1);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String value = i < values.length ? values[i] : "";
                    row.put(headers[i].trim(), value);
                }
                rows.add(row);
            }
        }
        return rows;
    }
}