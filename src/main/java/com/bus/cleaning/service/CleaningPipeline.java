package com.bus.cleaning.service;

import com.bus.cleaning.model.Booking;
import com.bus.cleaning.repository.BookingReader;
import com.bus.cleaning.repository.BookingWriter;
import com.bus.cleaning.rules.CodeMappingRule;
import com.bus.cleaning.rules.DateStandardizationRule;
import com.bus.cleaning.rules.DerivedFieldsRule;
import com.bus.cleaning.rules.NameNormalizationRule;
import com.bus.cleaning.rules.NumericValidationRule;
import com.bus.cleaning.rules.StatusNormalizationRule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CleaningPipeline {
    private final BookingReader reader;
    private final BookingWriter writer;
    private final DuplicateService duplicateService;
    private final AggregationService aggregationService;

    public CleaningPipeline(BookingReader reader, BookingWriter writer, DuplicateService duplicateService, AggregationService aggregationService) {
        this.reader = reader;
        this.writer = writer;
        this.duplicateService = duplicateService;
        this.aggregationService = aggregationService;
    }

    public void run(String rawPath, String cleanedPath, String invalidPath, String aggregatePath) throws IOException {
        List<Map<String, String>> rows = reader.readAll(rawPath);
        List<Booking> cleaned = new ArrayList<>();
        List<String> invalid = new ArrayList<>();

        for (Map<String, String> row : rows) {
            String id = value(row, "booking_id");
            if (duplicateService.isDuplicate(id)) {
                invalid.add(id + ",DUPLICATE_ID");
                continue;
            }

            Booking booking = new Booking();
            booking.setBookingId(id.trim().toUpperCase());
            booking.setPassengerName(value(row, "passenger_name"));
            booking.setAge(parseInt(value(row, "age")));
            booking.setSeats(parseInt(value(row, "seats")));
            booking.setAmount(parseDouble(value(row, "amount")));
            booking.setBusCode(value(row, "bus_code"));
            booking.setStatus(value(row, "status"));
            booking.setRoute(value(row, "route").trim().toUpperCase());

            if (!new NameNormalizationRule().apply(booking)) {
                invalid.add(id + ",INVALID_NAME");
                continue;
            }
            if (!new NumericValidationRule().apply(booking)) {
                invalid.add(id + ",INVALID_NUMERIC");
                continue;
            }
            if (!new DateStandardizationRule(value(row, "travel_date")).apply(booking)) {
                invalid.add(id + ",INVALID_DATE");
                continue;
            }
            if (!new CodeMappingRule().apply(booking)) {
                invalid.add(id + ",INVALID_CODE");
                continue;
            }
            if (!new StatusNormalizationRule().apply(booking)) {
                invalid.add(id + ",INVALID_STATUS");
                continue;
            }
            if (!new DerivedFieldsRule().apply(booking)) {
                invalid.add(id + ",DERIVED_FIELDS_FAILED");
                continue;
            }

            cleaned.add(booking);
        }

        writer.writeCleaned(cleanedPath, cleaned);
        writer.writeInvalid(invalidPath, invalid);
        writeAggregates(aggregatePath, cleaned);
    }

    private void writeAggregates(String aggregatePath, List<Booking> cleaned) throws IOException {
        Map<String, Long> byRoute = aggregationService.countByRoute(cleaned);
        Map<String, Long> byStatus = aggregationService.countByStatus(cleaned);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(aggregatePath))) {
            writer.write("metric,key,count");
            writer.newLine();
            for (Map.Entry<String, Long> entry : byRoute.entrySet()) {
                writer.write("ROUTE," + entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
            for (Map.Entry<String, Long> entry : byStatus.entrySet()) {
                writer.write("STATUS," + entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String value(Map<String, String> row, String key) {
        return row.getOrDefault(key, "");
    }
}