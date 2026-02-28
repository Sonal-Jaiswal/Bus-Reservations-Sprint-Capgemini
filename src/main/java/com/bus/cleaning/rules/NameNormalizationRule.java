package com.bus.cleaning.rules;

import com.bus.cleaning.model.Booking;

public class NameNormalizationRule implements CleaningRule {
    @Override
    public boolean apply(Booking booking) {
        String raw = booking.getPassengerName();
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        String[] parts = raw.trim().replaceAll("\\s+", " ").toLowerCase().split(" ");
        StringBuilder proper = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            String word = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
            if (proper.length() > 0) {
                proper.append(' ');
            }
            proper.append(word);
        }
        booking.setPassengerName(proper.toString());
        return !booking.getPassengerName().isBlank();
    }
}