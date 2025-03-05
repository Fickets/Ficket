package com.example.ficketevent.global.utils;

import com.example.ficketevent.domain.event.dto.response.EventIndexingInfo;
import com.example.ficketevent.domain.event.repository.EventRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CSVGenerator {

    private final EventRepository eventRepository;

    public File generateCsv(List<Long> eventIds, String filePath) throws IOException {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(filePath));
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // 헤더 작성
            csvWriter.writeNext(new String[]{"EventId", "Title", "Poster_Url", "Stage", "Location", "Genres", "Schedules", "Ticketing"});

            // 데이터 작성
            for (Long eventId : eventIds) {

                EventIndexingInfo event = convertToDto(eventRepository.findEventIndexingInfoRaw(eventId));

                String genres = String.join(", ", event.getGenreList());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                String schedules = event.getEventDateList().stream()
                        .map(eventSchedule -> eventSchedule.format(formatter))
                        .collect(Collectors.joining(", "));

                csvWriter.writeNext(new String[]{
                        String.valueOf(event.getEventId()),
                        event.getTitle(),
                        event.getPosterUrl(),
                        event.getStageName(),
                        event.getSido(),
                        genres,
                        schedules,
                        event.getTicketingTime().format(formatter)
                });

            }
        }

        return new File(filePath);
    }

    private EventIndexingInfo convertToDto(Map<String, Object> map) {
        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return new EventIndexingInfo(
                ((Long) map.get("eventId")),
                (String) map.get("title"),
                (String) map.get("stageName"),
                (String) map.get("sido"),
                (String) map.get("posterUrl"),
                map.get("ticketingTime") != null ?
                        ((Timestamp) map.get("ticketingTime")).toLocalDateTime() : null,
                Arrays.asList(((String) map.get("genreList")).split(",")),
                Arrays.stream(((String) map.get("eventDateList")).split(","))
                        .map(date -> LocalDateTime.parse(date, FORMATTER))
                        .collect(Collectors.toList())
        );
    }
}
