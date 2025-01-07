package com.example.ficketevent.global.utils;

import com.example.ficketevent.domain.event.entity.Event;
import com.example.ficketevent.domain.event.enums.Genre;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CSVGenerator {

    public File generateCsv(List<Event> events, String filePath) throws IOException {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(filePath));
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // 헤더 작성
            csvWriter.writeNext(new String[]{"EventId", "Title", "Poster_Url", "Stage", "Location", "Genres", "Schedules", "Ticketing"});

            // 데이터 작성
            for (Event event : events) {
                String genres = event.getGenre().stream()
                        .map(Genre::toString)
                        .collect(Collectors.joining(", "));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                String schedules = event.getEventSchedules().stream()
                        .map(eventSchedule -> eventSchedule.getEventDate().format(formatter))
                        .collect(Collectors.joining(", "));

                csvWriter.writeNext(new String[]{
                        String.valueOf(event.getEventId()),
                        event.getTitle(),
                        event.getEventImage().getPosterPcUrl(),
                        event.getEventStage().getStageName(),
                        event.getEventStage().getSido(),
                        genres,
                        schedules,
                        event.getTicketingTime().format(formatter)
                });

            }
        }

        return new File(filePath);
    }
}
