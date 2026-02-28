package com.bus.cleaning.service;

import java.util.HashSet;
import java.util.Set;

public class DuplicateService {
    private final Set<String> seenIds = new HashSet<>();

    public boolean isDuplicate(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            return true;
        }
        return !seenIds.add(bookingId.trim().toUpperCase());
    }
}