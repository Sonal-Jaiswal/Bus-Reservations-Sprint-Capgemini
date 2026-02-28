package com.bus.cleaning.rules;

import com.bus.cleaning.model.Booking;

public interface CleaningRule {
    boolean apply(Booking booking);
}