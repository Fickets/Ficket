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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CSVGenerator {

    private final EventRepository eventRepository;

    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD_T_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String[] CSV_HEADER = {"EventId", "Title", "Poster_Url", "Stage", "Location", "Genres", "Schedules", "Ticketing"};

    public File generateCsv(List<Long> eventIds, String filePath) throws IOException {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(filePath));
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            csvWriter.writeNext(CSV_HEADER); // 헤더 작성

            List<Map<String, Object>> rawList = eventRepository.findEventIndexingInfoRawBulk(eventIds);

            for (Map<String, Object> raw : rawList) {
                EventIndexingInfo event = convertToDto(raw);

                String genres = String.join(", ", event.getGenreList());
                String schedules = event.getEventDateList().stream()
                        .map(d -> d.format(FORMATTER_YYYY_MM_DD_T_HH_MM_SS))
                        .collect(Collectors.joining(", "));

                csvWriter.writeNext(new String[]{
                        String.valueOf(event.getEventId()),
                        event.getTitle(),
                        event.getPosterUrl(),
                        event.getStageName(),
                        event.getSido(),
                        genres,
                        schedules,
                        event.getTicketingTime().format(FORMATTER_YYYY_MM_DD_T_HH_MM_SS)
                });
            }

            log.info("CSV 생성 완료: {}", filePath);
        } catch (IOException e) {
            log.error("CSV 생성 실패 - 파일 경로: {}", filePath, e);
            throw e;
        }

        return new File(filePath);
    }

    private EventIndexingInfo convertToDto(Map<String, Object> map) {
        return new EventIndexingInfo(
                ((Number) map.get("eventId")).longValue(),
                (String) map.get("title"),
                (String) map.get("stageName"),
                (String) map.get("sido"),
                (String) map.get("posterUrl"),
                map.get("ticketingTime") != null ?
                        ((Timestamp) map.get("ticketingTime")).toLocalDateTime() : null,
                Arrays.asList(((String) map.get("genreList")).split(",")),
                Arrays.stream(((String) map.get("eventDateList")).split(","))
                        .map(date -> LocalDateTime.parse(date, FORMATTER_YYYY_MM_DD_HH_MM_SS))
                        .collect(Collectors.toList())
        );
    }
}
