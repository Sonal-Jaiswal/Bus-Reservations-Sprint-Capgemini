package com.bus.cleaning.repository;

import com.bus.cleaning.model.Booking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvBookingWriter implements BookingWriter {
    @Override
    public void writeCleaned(String path, List<Booking> bookings) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("booking_id,passenger_name,age,seats,amount,travel_date,bus_code,bus_type,status,route,amount_per_seat,age_category");
            writer.newLine();
            for (Booking booking : bookings) {
                writer.write(String.join(",",
                        booking.getBookingId(),
                        booking.getPassengerName(),
                        String.valueOf(booking.getAge()),
                        String.valueOf(booking.getSeats()),
                        String.format("%.2f", booking.getAmount()),
                        booking.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        booking.getBusCode(),
                        booking.getBusType(),
                        booking.getStatus(),
                        booking.getRoute(),
                        String.format("%.2f", booking.getAmountPerSeat()),
                        booking.getAgeCategory()
                ));
                writer.newLine();
            }
        }
    }

    @Override
    public void writeInvalid(String path, List<String> invalidReasons) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("raw_booking_id,reason");
            writer.newLine();
            for (String reason : invalidReasons) {
                writer.write(reason);
                writer.newLine();
            }
        }
    }
}