package com.officebuddy.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlexibleLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.isBlank()) return null;
        text = text.trim();
        // Handle "2026-07-01T00:00:00.000" or "2026-07-01T00:00:00" or "2026-07-01"
        try {
            if (text.contains("T")) {
                // Try ISO_LOCAL_DATE_TIME first, then fallback to substring
                try {
                    return LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
                } catch (Exception e) {
                    return LocalDate.parse(text.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
                }
            }
            return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            // Last resort: take first 10 chars
            if (text.length() >= 10) {
                return LocalDate.parse(text.substring(0, 10));
            }
            throw new IOException("Unable to parse LocalDate from: " + text, e);
        }
    }
}
