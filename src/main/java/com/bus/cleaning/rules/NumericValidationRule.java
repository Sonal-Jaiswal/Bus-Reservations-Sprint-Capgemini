package com.bus.cleaning.rules;

import com.bus.cleaning.model.Booking;

public class NumericValidationRule implements CleaningRule {
    @Override
    public boolean apply(Booking booking) {
        if (booking.getAge() == null || booking.getSeats() == null || booking.getAmount() == null) {
            return false;
        }
        return booking.getAge() >= 5 && booking.getAge() <= 100
                && booking.getSeats() >= 1 && booking.getSeats() <= 6
                && booking.getAmount() > 0;
    }
}