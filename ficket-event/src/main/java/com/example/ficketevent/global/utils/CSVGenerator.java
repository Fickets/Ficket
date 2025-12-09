package com.example.ficketevent.global.utils;

import com.example.ficketevent.domain.event.dto.response.EventIndexingInfo;
import com.example.ficketevent.domain.event.repository.EventRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    private static final DateTimeFormatter FORMATTER_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_DB = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_FILENAME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String[] CSV_HEADER = {
            "EventId", "Title", "Poster_Url", "Stage", "Location",
            "Genres", "Schedules", "Ticketing"
    };

    /**
     * 이벤트 ID 리스트를 받아 CSV를 InputStream으로 생성
     * 파일 시스템을 사용하지 않고 메모리에서 직접 생성
     */
    public InputStream generateCsvStream(List<Long> eventIds) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // 헤더 작성
            csvWriter.writeNext(CSV_HEADER);

            // 이벤트 데이터 조회 및 작성
            List<Map<String, Object>> rawList = eventRepository.findEventIndexingInfoRawBulk(eventIds);
            log.info("CSV 생성 시작: {} 건", rawList.size());

            for (Map<String, Object> raw : rawList) {
                EventIndexingInfo event = convertToDto(raw);
                csvWriter.writeNext(convertToCsvRow(event));
            }

            csvWriter.flush();
            writer.flush();

            log.info("CSV 생성 완료: {} 건", rawList.size());
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (IOException e) {
            log.error("CSV 생성 실패", e);
            throw e;
        }
    }

    /**
     * CSV 파일명 생성 (timestamp_uuid.csv 형식)
     */
    public String generateFileName(String uuid) {
        String timestamp = LocalDateTime.now().format(FORMATTER_FILENAME);
        return String.format("%s_%s.csv", timestamp, uuid);
    }

    /**
     * EventIndexingInfo를 CSV 행으로 변환
     */
    private String[] convertToCsvRow(EventIndexingInfo event) {
        return new String[]{
                String.valueOf(event.getEventId()),
                event.getTitle(),
                event.getPosterUrl(),
                event.getStageName(),
                event.getSido(),
                formatGenres(event.getGenreList()),
                formatSchedules(event.getEventDateList()),
                formatTicketingTime(event.getTicketingTime())
        };
    }

    /**
     * 장르 리스트를 문자열로 포맷
     */
    private String formatGenres(List<String> genres) {
        return String.join(", ", genres);
    }

    /**
     * 일정 리스트를 문자열로 포맷
     */
    private String formatSchedules(List<LocalDateTime> schedules) {
        return schedules.stream()
                .map(date -> date.format(FORMATTER_TIMESTAMP))
                .collect(Collectors.joining(", "));
    }

    /**
     * 티켓팅 시간을 문자열로 포맷
     */
    private String formatTicketingTime(LocalDateTime ticketingTime) {
        return ticketingTime != null ? ticketingTime.format(FORMATTER_TIMESTAMP) : "";
    }

    /**
     * DB 조회 결과를 DTO로 변환
     */
    private EventIndexingInfo convertToDto(Map<String, Object> map) {
        return new EventIndexingInfo(
                ((Number) map.get("eventId")).longValue(),
                (String) map.get("title"),
                (String) map.get("stageName"),
                (String) map.get("sido"),
                (String) map.get("posterUrl"),
                parseTicketingTime(map.get("ticketingTime")),
                parseGenreList((String) map.get("genreList")),
                parseEventDateList((String) map.get("eventDateList"))
        );
    }

    /**
     * 티켓팅 시간 파싱
     */
    private LocalDateTime parseTicketingTime(Object ticketingTime) {
        if (ticketingTime == null) {
            return null;
        }
        return ((Timestamp) ticketingTime).toLocalDateTime();
    }

    /**
     * 장르 리스트 파싱
     */
    private List<String> parseGenreList(String genreList) {
        return Arrays.asList(genreList.split(","));
    }

    /**
     * 일정 리스트 파싱
     */
    private List<LocalDateTime> parseEventDateList(String eventDateList) {
        return Arrays.stream(eventDateList.split(","))
                .map(date -> LocalDateTime.parse(date.trim(), FORMATTER_DB))
                .collect(Collectors.toList());
    }
}