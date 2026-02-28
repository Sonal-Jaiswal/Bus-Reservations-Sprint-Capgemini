package com.bus.cleaning.config;

import com.bus.cleaning.repository.BookingReader;
import com.bus.cleaning.repository.BookingWriter;
import com.bus.cleaning.repository.CsvBookingReader;
import com.bus.cleaning.repository.CsvBookingWriter;
import com.bus.cleaning.service.AggregationService;
import com.bus.cleaning.service.CleaningPipeline;
import com.bus.cleaning.service.DuplicateService;

public class AppConfig {
    public BookingReader bookingReader() {
        return new CsvBookingReader();
    }

    public BookingWriter bookingWriter() {
        return new CsvBookingWriter();
    }

    public DuplicateService duplicateService() {
        return new DuplicateService();
    }

    public AggregationService aggregationService() {
        return new AggregationService();
    }

    public CleaningPipeline cleaningPipeline() {
        return new CleaningPipeline(bookingReader(), bookingWriter(), duplicateService(), aggregationService());
    }
}