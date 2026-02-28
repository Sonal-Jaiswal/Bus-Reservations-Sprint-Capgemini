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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

public class CleaningPipeline {
    private static final Logger logger = LogManager.getLogger(CleaningPipeline.class);

    private final BookingReader reader;
    private final BookingWriter writer;
    private final DuplicateService duplicateService;
    private final AggregationService aggregationService;
    private final Set<String> announcedUseCases = new HashSet<>();

    public CleaningPipeline(BookingReader reader, BookingWriter writer, DuplicateService duplicateService, AggregationService aggregationService) {
        this.reader = reader;
        this.writer = writer;
        this.duplicateService = duplicateService;
        this.aggregationService = aggregationService;
    }

    public void run(String rawPath, String cleanedPath, String invalidPath, String aggregatePath) throws IOException {
        logger.info("Starting cleaning pipeline");
        logger.info("Input file: {}", rawPath);

        Map<String, Integer> passCount = new LinkedHashMap<>();
        Map<String, Integer> failCount = new LinkedHashMap<>();
        initializeCounters(passCount, failCount);

        printUseCase("Use Case 1: Remove Duplicates");
        List<Map<String, String>> rows = reader.readAll(rawPath);
        logger.info("Loaded {} raw records", rows.size());

        List<Booking> cleaned = new ArrayList<>();
        List<String> invalid = new ArrayList<>();

        for (Map<String, String> row : rows) {
            String id = value(row, "booking_id");
            logger.info("Processing booking_id={}", id);

            if (duplicateService.isDuplicate(id)) {
                invalid.add(id + ",DUPLICATE_ID");
                logger.warn("booking_id={} rejected: DUPLICATE_ID", id);
                increment(failCount, "Use Case 1: Remove Duplicates");
                continue;
            }
            increment(passCount, "Use Case 1: Remove Duplicates");

            Booking booking = new Booking();
            booking.setBookingId(id.trim().toUpperCase());
            booking.setPassengerName(value(row, "passenger_name"));
            booking.setAge(parseInt(value(row, "age")));
            booking.setSeats(parseInt(value(row, "seats")));
            booking.setAmount(parseDouble(value(row, "amount")));
            booking.setBusCode(value(row, "bus_code"));
            booking.setStatus(value(row, "status"));
            booking.setRoute(value(row, "route").trim().toUpperCase());

            printUseCase("Use Case 2: Normalize Names");
            if (!new NameNormalizationRule().apply(booking)) {
                invalid.add(id + ",INVALID_NAME");
                logger.warn("booking_id={} rejected: INVALID_NAME", id);
                increment(failCount, "Use Case 2: Normalize Names");
                continue;
            }
            logger.info("booking_id={} name normalized", id);
            increment(passCount, "Use Case 2: Normalize Names");

            printUseCase("Use Case 3: Fix Numeric Fields");
            if (!new NumericValidationRule().apply(booking)) {
                invalid.add(id + ",INVALID_NUMERIC");
                logger.warn("booking_id={} rejected: INVALID_NUMERIC", id);
                increment(failCount, "Use Case 3: Fix Numeric Fields");
                continue;
            }
            logger.info("booking_id={} numeric fields validated", id);
            increment(passCount, "Use Case 3: Fix Numeric Fields");

            printUseCase("Use Case 4: Standardize Dates");
            if (!new DateStandardizationRule(value(row, "travel_date")).apply(booking)) {
                invalid.add(id + ",INVALID_DATE");
                logger.warn("booking_id={} rejected: INVALID_DATE", id);
                increment(failCount, "Use Case 4: Standardize Dates");
                continue;
            }
            logger.info("booking_id={} date standardized to {}", id, booking.getTravelDate());
            increment(passCount, "Use Case 4: Standardize Dates");

            printUseCase("Use Case 5: Map Codes");
            if (!new CodeMappingRule().apply(booking)) {
                invalid.add(id + ",INVALID_CODE");
                logger.warn("booking_id={} rejected: INVALID_CODE", id);
                increment(failCount, "Use Case 5: Map Codes");
                continue;
            }
            logger.info("booking_id={} code mapped to bus_type={}", id, booking.getBusType());
            increment(passCount, "Use Case 5: Map Codes");

            printUseCase("Use Case 6: Validate Status");
            if (!new StatusNormalizationRule().apply(booking)) {
                invalid.add(id + ",INVALID_STATUS");
                logger.warn("booking_id={} rejected: INVALID_STATUS", id);
                increment(failCount, "Use Case 6: Validate Status");
                continue;
            }
            logger.info("booking_id={} status normalized to {}", id, booking.getStatus());
            increment(passCount, "Use Case 6: Validate Status");

            printUseCase("Use Case 8: Derived Fields");
            if (!new DerivedFieldsRule().apply(booking)) {
                invalid.add(id + ",DERIVED_FIELDS_FAILED");
                logger.warn("booking_id={} rejected: DERIVED_FIELDS_FAILED", id);
                increment(failCount, "Use Case 8: Derived Fields");
                continue;
            }
            logger.info("booking_id={} derived fields created amount_per_seat={} age_category={}",
                    id, booking.getAmountPerSeat(), booking.getAgeCategory());
            increment(passCount, "Use Case 8: Derived Fields");

            printUseCase("Use Case 10: Data Categorization");
            increment(passCount, "Use Case 10: Data Categorization");

            cleaned.add(booking);
            logger.info("booking_id={} accepted as CLEAN", id);
        }

        printUseCase("Use Case 7: Flag Invalid Records");
        passCount.put("Use Case 7: Flag Invalid Records", invalid.size());
        failCount.put("Use Case 7: Flag Invalid Records", 0);

        writer.writeCleaned(cleanedPath, cleaned);
        logger.info("Wrote cleaned file: {} with {} records", cleanedPath, cleaned.size());

        writer.writeInvalid(invalidPath, invalid);
        logger.info("Wrote invalid file: {} with {} records", invalidPath, invalid.size());

        printUseCase("Use Case 9: Aggregation");
        increment(passCount, "Use Case 9: Aggregation");
        writeAggregates(aggregatePath, cleaned);
        logger.info("Wrote aggregate file: {}", aggregatePath);
        logger.info("Pipeline completed. raw={}, clean={}, invalid={}", rows.size(), cleaned.size(), invalid.size());

        printSummary(passCount, failCount);
    }

    private void initializeCounters(Map<String, Integer> passCount, Map<String, Integer> failCount) {
        String[] useCases = {
                "Use Case 1: Remove Duplicates",
                "Use Case 2: Normalize Names",
                "Use Case 3: Fix Numeric Fields",
                "Use Case 4: Standardize Dates",
                "Use Case 5: Map Codes",
                "Use Case 6: Validate Status",
                "Use Case 7: Flag Invalid Records",
                "Use Case 8: Derived Fields",
                "Use Case 9: Aggregation",
                "Use Case 10: Data Categorization"
        };
        for (String useCase : useCases) {
            passCount.put(useCase, 0);
            failCount.put(useCase, 0);
        }
    }

    private void increment(Map<String, Integer> counters, String key) {
        counters.put(key, counters.getOrDefault(key, 0) + 1);
    }

    private void printUseCase(String useCase) {
        if (!announcedUseCases.add(useCase)) {
            return;
        }
        String message = "Running " + useCase;
        System.out.println(message);
        logger.info(message);
    }

    private void printSummary(Map<String, Integer> passCount, Map<String, Integer> failCount) {
        System.out.println("\n==== USE CASE EXECUTION SUMMARY ====");
        logger.info("==== USE CASE EXECUTION SUMMARY ====");
        for (String useCase : passCount.keySet()) {
            String line = useCase + " -> passed=" + passCount.get(useCase) + ", failed=" + failCount.get(useCase);
            System.out.println(line);
            logger.info(line);
        }
        System.out.println("====================================");
        logger.info("====================================");
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