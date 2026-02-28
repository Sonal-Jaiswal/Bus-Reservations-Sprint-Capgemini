package com.bus.cleaning.rules;

import com.bus.cleaning.model.Booking;

public class DerivedFieldsRule implements CleaningRule {
    @Override
    public boolean apply(Booking booking) {
        if (booking.getAmount() == null || booking.getSeats() == null || booking.getAge() == null || booking.getSeats() == 0) {
            return false;
        }
        booking.setAmountPerSeat(booking.getAmount() / booking.getSeats());
        int age = booking.getAge();
        String category = age <= 17 ? "MINOR" : age <= 59 ? "ADULT" : "SENIOR";
        booking.setAgeCategory(category);
        return true;
    }
}