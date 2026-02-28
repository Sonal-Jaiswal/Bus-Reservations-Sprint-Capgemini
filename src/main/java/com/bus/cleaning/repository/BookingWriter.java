package com.bus.cleaning.repository;

import com.bus.cleaning.model.Booking;

import java.io.IOException;
import java.util.List;

public interface BookingWriter {
    void writeCleaned(String path, List<Booking> bookings) throws IOException;

    void writeInvalid(String path, List<String> invalidReasons) throws IOException;
}