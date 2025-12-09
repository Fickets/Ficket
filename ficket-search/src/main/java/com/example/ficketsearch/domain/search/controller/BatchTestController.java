package com.example.ficketsearch.domain.search.controller;

import com.example.ficketsearch.domain.search.service.FullIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test/batch")
@RequiredArgsConstructor
public class BatchTestController {

    private final JobLauncher jobLauncher;
    private final Job fullIndexingJob;
    private final FullIndexingService fullIndexingService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 전체 배치 작업 실행 (모든 단계)
     * POST /api/test/batch/run-full
     */
    @PostMapping("/run-full")
    public ResponseEntity<Map<String, Object>> runFullBatch() {
        log.info("=== TEST: 전체 배치 작업 실행 시작 ===");
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("executionTime", now.format(FORMATTER))
                    .addString("triggeredBy", "test-api")
                    .toJobParameters();
            
            jobLauncher.run(fullIndexingJob, jobParameters);
            
            response.put("success", true);
            response.put("message", "전체 배치 작업이 실행되었습니다.");
            response.put("executionTime", now.format(FORMATTER));
            log.info("=== TEST: 전체 배치 작업 완료 ===");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("TEST: 전체 배치 작업 실패", e);
            response.put("success", false);
            response.put("message", "배치 작업 실패: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 인덱싱 초기화만 실행 (스냅샷 백업 → 데이터 삭제 → 인덱스 생성)
     * POST /api/test/batch/initialize-indexing
     */
    @PostMapping("/initialize-indexing")
    public ResponseEntity<Map<String, Object>> initializeIndexing() {
        log.info("=== TEST: 인덱싱 초기화 실행 ===");
        Map<String, Object> response = new HashMap<>();
        
        try {
            fullIndexingService.initializeIndexing();
            
            response.put("success", true);
            response.put("message", "인덱싱 초기화가 완료되었습니다.");
            log.info("=== TEST: 인덱싱 초기화 완료 ===");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("TEST: 인덱싱 초기화 실패", e);
            response.put("success", false);
            response.put("message", "초기화 실패: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * CSV 다운로드 및 색인만 실행
     * POST /api/test/batch/handle-indexing
     * Body: { "s3Urls": "s3://bucket/path/file1.csv,s3://bucket/path/file2.csv" }
     */
    @PostMapping("/handle-indexing")
    public ResponseEntity<Map<String, Object>> handleIndexing(@RequestBody Map<String, String> request) {
        String s3Urls = request.get("s3Urls");
        log.info("=== TEST: CSV 색인 실행 - {} ===", s3Urls);
        Map<String, Object> response = new HashMap<>();
        
        if (s3Urls == null || s3Urls.isEmpty()) {
            response.put("success", false);
            response.put("message", "s3Urls 파라미터가 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            long startTime = System.currentTimeMillis();
            fullIndexingService.handleFullIndexing(s3Urls);
            long duration = (System.currentTimeMillis() - startTime) / 1000;
            
            response.put("success", true);
            response.put("message", "CSV 색인이 완료되었습니다.");
            response.put("durationSeconds", duration);
            response.put("processedUrls", s3Urls.split(",").length);
            log.info("=== TEST: CSV 색인 완료 ({}초) ===", duration);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("TEST: CSV 색인 실패", e);
            response.put("success", false);
            response.put("message", "색인 실패: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 스냅샷 복원
     * POST /api/test/batch/restore-snapshot
     */
    @PostMapping("/restore-snapshot")
    public ResponseEntity<Map<String, Object>> restoreSnapshot() {
        log.info("=== TEST: 스냅샷 복원 실행 ===");
        Map<String, Object> response = new HashMap<>();
        
        try {
            fullIndexingService.restoreSnapshot();
            
            response.put("success", true);
            response.put("message", "스냅샷 복원이 완료되었습니다.");
            log.info("=== TEST: 스냅샷 복원 완료 ===");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("TEST: 스냅샷 복원 실패", e);
            response.put("success", false);
            response.put("message", "복원 실패: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 오늘 날짜 경로의 CSV 파일로 색인 (자동 경로 생성)
     * POST /api/test/batch/index-today
     */
    @PostMapping("/index-today")
    public ResponseEntity<Map<String, Object>> indexTodayData() {
        log.info("=== TEST: 오늘 날짜 데이터 색인 실행 ===");
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 오늘 날짜 경로 생성 (예: 2025/12/09)
            LocalDateTime now = LocalDateTime.now();
            String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String s3Path = String.format("s3://ficket-event-content/index/full/%s/", datePath);
            
            log.info("생성된 S3 경로: {}", s3Path);
            
            // 실제로는 S3에서 파일 목록을 조회해야 하지만, 테스트용으로 단순화
            response.put("success", false);
            response.put("message", "이 API는 s3Urls를 직접 제공하는 /handle-indexing을 사용하세요.");
            response.put("suggestedPath", s3Path);
            response.put("example", s3Path + "events_part1.csv," + s3Path + "events_part2.csv");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("TEST: 오늘 날짜 데이터 색인 실패", e);
            response.put("success", false);
            response.put("message", "색인 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 헬스 체크 및 상태 정보
     * GET /api/test/batch/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("=== TEST: 헬스 체크 ===");
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("status", "UP");
            response.put("timestamp", LocalDateTime.now().format(FORMATTER));
            response.put("availableEndpoints", Map.of(
                "runFull", "POST /api/test/batch/run-full",
                "initializeIndexing", "POST /api/test/batch/initialize-indexing",
                "handleIndexing", "POST /api/test/batch/handle-indexing",
                "restoreSnapshot", "POST /api/test/batch/restore-snapshot",
                "indexToday", "POST /api/test/batch/index-today"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "DOWN");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 샘플 S3 URL 생성 헬퍼
     * GET /api/test/batch/generate-sample-urls?date=2025-12-09&count=3
     */
    @GetMapping("/generate-sample-urls")
    public ResponseEntity<Map<String, Object>> generateSampleUrls(
            @RequestParam(defaultValue = "") String date,
            @RequestParam(defaultValue = "1") int count) {
        
        log.info("=== TEST: 샘플 URL 생성 - date: {}, count: {} ===", date, count);
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 날짜가 없으면 오늘 날짜 사용
            if (date == null || date.isEmpty()) {
                date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            
            // yyyy-MM-dd를 yyyy/MM/dd로 변환
            String[] parts = date.split("-");
            String datePath = String.join("/", parts);
            
            // 샘플 URL 생성
            StringBuilder urls = new StringBuilder();
            for (int i = 1; i <= count; i++) {
                if (i > 1) urls.append(",");
                urls.append(String.format("s3://ficket-event-content/index/full/%s/events_part%d.csv", 
                        datePath, i));
            }
            
            response.put("success", true);
            response.put("s3Urls", urls.toString());
            response.put("date", date);
            response.put("count", count);
            response.put("usage", "POST /api/test/batch/handle-indexing 에 s3Urls로 전달하세요.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("TEST: 샘플 URL 생성 실패", e);
            response.put("success", false);
            response.put("message", "URL 생성 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}