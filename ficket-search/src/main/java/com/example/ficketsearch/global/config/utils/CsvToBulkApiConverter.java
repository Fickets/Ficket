package com.example.ficketsearch.global.config.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvToBulkApiConverter {

    private final ObjectMapper objectMapper;

    public Stream<String> convertCsvToBulkJsonStream(String csvFilePath, String indexName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            return csvParser.getRecords().stream()
                    .filter(record -> record.size() > 0)  // 빈 줄 필터링
                    .map(record -> convertRecordToJson(record, indexName));
        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV file at: " + csvFilePath, e);
        }
    }

    private String convertRecordToJson(CSVRecord record, String indexName) {
        try {
            String metadata = "{\"index\": {\"_index\": \"" + indexName + "\"}}";

            Map<String, String> recordMap = record.toMap();

            if (recordMap.containsKey("Genres")) {
                recordMap.computeIfPresent("Genres", (k, genres) -> convertStringToNestedJsonArray("Genre", genres));
            }

            if (recordMap.containsKey("Schedules")) {
                recordMap.computeIfPresent("Schedules", (k, schedules) -> convertStringToNestedJsonArray("Schedule", schedules));
            }

            String jsonData = objectMapper.writeValueAsString(recordMap);
            log.info("Converted JSON: {}", jsonData);
            return metadata + "\n" + jsonData;
        } catch (Exception e) {
            throw new RuntimeException("Error converting CSV record to JSON: " + record.toString(), e);
        }
    }

    private String convertStringToNestedJsonArray(String key, String input) {
        String[] info = input.split(",");

        return "[" + Arrays.stream(info)
                .map(item -> "{\"" + key + "\": \"" + item.trim() + "\"}")  // key 값을 사용하여 객체 생성
                .collect(Collectors.joining(",")) + "]";
    }
}
