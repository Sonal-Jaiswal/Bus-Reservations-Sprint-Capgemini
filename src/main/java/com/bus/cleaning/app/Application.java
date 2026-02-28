package com.bus.cleaning.app;

import com.bus.cleaning.config.AppConfig;
import com.bus.cleaning.service.CleaningPipeline;

public class Application {
    public static void main(String[] args) {
        String rawPath = "data/raw_bookings.csv";
        String cleanedPath = "output/cleaned_bookings.csv";
        String invalidPath = "output/invalid_bookings.csv";
        String aggregatePath = "output/aggregated_report.csv";

        CleaningPipeline pipeline = new AppConfig().cleaningPipeline();
        try {
            pipeline.run(rawPath, cleanedPath, invalidPath, aggregatePath);
            System.out.println("Cleaning completed successfully.");
            System.out.println("Cleaned output: " + cleanedPath);
            System.out.println("Invalid records: " + invalidPath);
            System.out.println("Aggregation report: " + aggregatePath);
        } catch (Exception exception) {
            System.err.println("Cleaning failed: " + exception.getMessage());
        }
    }
}