package com.ramonlimas.domain.dto;
import org.apache.commons.csv.CSVRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public record HistoryCSV(
        String userId,
        String title,
        Date date
) {
    public static HistoryCSV fromCsv(CSVRecord record, String userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
        LocalDate localDate = LocalDate.parse(record.get("Date"), formatter);
        Date date = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());

        return new HistoryCSV(
                userId,
                record.get("Title"),
                date
        );
    }
}
