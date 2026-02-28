package com.bus.cleaning.repository;

import com.bus.cleaning.config.DbConfig;
import com.bus.cleaning.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DbBookingRepository {
    private final DbConfig dbConfig;

    public DbBookingRepository(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void saveAll(List<Booking> bookings) {
        String sql = "INSERT INTO cleaned_bookings (booking_id, passenger_name, age, seats, amount, travel_date, bus_code, bus_type, status, route, amount_per_seat, age_category) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dbConfig.getConnection()) {
            for (Booking booking : bookings) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, booking.getBookingId());
                    statement.setString(2, booking.getPassengerName());
                    statement.setInt(3, booking.getAge());
                    statement.setInt(4, booking.getSeats());
                    statement.setDouble(5, booking.getAmount());
                    statement.setDate(6, java.sql.Date.valueOf(booking.getTravelDate()));
                    statement.setString(7, booking.getBusCode());
                    statement.setString(8, booking.getBusType());
                    statement.setString(9, booking.getStatus());
                    statement.setString(10, booking.getRoute());
                    statement.setDouble(11, booking.getAmountPerSeat());
                    statement.setString(12, booking.getAgeCategory());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            System.err.println("DB save failed: " + exception.getMessage());
        }
    }
}