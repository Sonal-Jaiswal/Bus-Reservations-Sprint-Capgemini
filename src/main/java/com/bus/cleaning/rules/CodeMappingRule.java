package com.bus.cleaning.rules;

import com.bus.cleaning.model.Booking;

import java.util.Locale;
import java.util.Map;

public class CodeMappingRule implements CleaningRule {
    private static final Map<String, String> BUS_MAP = Map.of(
            "AC", "Air Conditioned",
            "NAC", "Non Air Conditioned",
            "SL", "Sleeper",
            "VOL", "Volvo"
    );

    @Override
    public boolean apply(Booking booking) {
        String raw = booking.getBusCode();
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        String mapped = BUS_MAP.get(normalized);
        if (mapped == null) {
            return false;
        }
        booking.setBusCode(normalized);
        booking.setBusType(mapped);
        return true;
    }
}