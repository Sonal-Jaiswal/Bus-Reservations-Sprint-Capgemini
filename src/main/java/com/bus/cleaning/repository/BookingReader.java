package com.bus.cleaning.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BookingReader {
    List<Map<String, String>> readAll(String path) throws IOException;
}