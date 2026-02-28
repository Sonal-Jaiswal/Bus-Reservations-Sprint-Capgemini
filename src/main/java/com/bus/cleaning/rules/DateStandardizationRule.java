package com.bus.cleaning.rules;

import com.bus.cleaning.model.Booking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DateStandardizationRule implements CleaningRule {
    private static final List<DateTimeFormatter> FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    );

    private final String rawDate;

    public DateStandardizationRule(String rawDate) {
        this.rawDate = rawDate;
    }

    @Override
    public boolean apply(Booking booking) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return false;
        }
        String candidate = rawDate.trim();
        for (DateTimeFormatter formatter : FORMATS) {
            try {
                LocalDate parsed = LocalDate.parse(candidate, formatter);
                booking.setTravelDate(parsed);
                return true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }
}