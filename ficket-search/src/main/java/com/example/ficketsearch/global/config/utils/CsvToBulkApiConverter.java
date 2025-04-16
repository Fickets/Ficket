package com.example.ficketsearch.global.config.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvToBulkApiConverter {

    /**
     * CSV를 JSON 형태의 Map으로 변환한 스트림을 반환합니다.
     */
    public Stream<Map<String, Object>> convertCsvToJsonStream(String csvFilePath) {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.Builder.create()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build())
        ) {
            List<Map<String, Object>> records = csvParser.getRecords().stream()
                    .filter(record -> record.size() > 0)
                    .map(this::convertRecordToMap)
                    .toList();

            return records.stream();  // 파일은 이미 닫혔고 메모리에서 스트림 제공
        } catch (Exception e) {
            throw new RuntimeException("CSV 처리 실패: " + csvFilePath, e);
        }
    }

    /**
     * CSVRecord를 Map 형태로 변환합니다.
     */
    private Map<String, Object> convertRecordToMap(CSVRecord record) {
        Map<String, Object> resultMap = new HashMap<>(record.toMap());

        resultMap.computeIfPresent("Genres", (k, v) -> convertToListMap("Genre", (String) v));
        resultMap.computeIfPresent("Schedules", (k, v) -> convertToListMap("Schedule", (String) v));

        return resultMap;
    }

    /**
     * 문자열을 List<Map<String, String>> 형태로 변환합니다.
     */
    private List<Map<String, String>> convertToListMap(String key, String input) {
        return Arrays.stream(input.split(","))
                .map(item -> Map.of(key, item.trim()))
                .collect(Collectors.toList());
    }
}
