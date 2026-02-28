package com.bus.cleaning.rules;

import com.bus.cleaning.model.Booking;

import java.util.Locale;
import java.util.Set;

public class StatusNormalizationRule implements CleaningRule {
    private static final Set<String> ALLOWED = Set.of("CONFIRMED", "PENDING", "CANCELLED");

    @Override
    public boolean apply(Booking booking) {
        String raw = booking.getStatus();
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        normalized = switch (normalized) {
            case "CNF", "CONFIRM", "CONFIRMED" -> "CONFIRMED";
            case "PEN", "WAITING", "PENDING" -> "PENDING";
            case "CAN", "CANCEL", "CANCELLED" -> "CANCELLED";
            default -> normalized;
        };
        if (!ALLOWED.contains(normalized)) {
            return false;
        }
        booking.setStatus(normalized);
        return true;
    }
}