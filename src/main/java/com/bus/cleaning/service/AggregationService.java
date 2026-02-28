package com.bus.cleaning.service;

import com.bus.cleaning.model.Booking;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AggregationService {
    public Map<String, Long> countByRoute(List<Booking> bookings) {
        return bookings.stream().collect(Collectors.groupingBy(Booking::getRoute, Collectors.counting()));
    }

    public Map<String, Long> countByStatus(List<Booking> bookings) {
        return bookings.stream().collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
    }
}