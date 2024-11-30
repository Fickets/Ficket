package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.dto.request.SelectSeat;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
class PreoccupyServiceTest {

    @Autowired
    private PreoccupyService preoccupyService;

    @Autowired
    private RedissonClient redissonClient;

    private static final Long EVENT_SCHEDULE_ID = 1L; // 이벤트 스케줄 ID
    private static final Integer RESERVATION_LIMIT = 1; // 예약 제한
    private static final Set<Long> SEAT_MAPPING_IDS = Set.of(34L); // 좌석 매핑 ID

    @BeforeEach
    void setUp() {
        try {
            // Redis 전체 데이터 삭제
            redissonClient.getKeys().flushdb();
            System.out.println("Redis 초기화 완료");
        } catch (Exception e) {
            throw new IllegalStateException("Redis 초기화 실패", e);
        }
    }

    @AfterEach
    void tearDown() {
        try {
            // Redis 전체 데이터 삭제
            redissonClient.getKeys().flushdb();
            System.out.println("Redis 정리 완료");
        } catch (Exception e) {
            throw new IllegalStateException("Redis 정리 실패", e);
        }
    }

    @Test
    @DisplayName("1000명의 사용자가 동일한 하나의 좌석 예약 - 동시성 테스트")
    void testMultiReserve() throws InterruptedException {
        int threadNum = 1000; // 동시 요청 스레드 수
        CountDownLatch latch = new CountDownLatch(threadNum); // 동시성 테스트 완료 대기
        AtomicInteger successCounter = new AtomicInteger(0); // 성공 카운터
        AtomicInteger failureCounter = new AtomicInteger(0); // 실패 카운터

        ExecutorService executorService = Executors.newFixedThreadPool(threadNum); // 스레드 풀 생성

        for (long i = 1L; i <= threadNum; i++) {
            Long userId = i; // 각 사용자 고유 ID
            executorService.submit(() -> {
                try {
                    // SelectSeat 객체 생성
                    SelectSeat request = new SelectSeat(EVENT_SCHEDULE_ID, RESERVATION_LIMIT, SEAT_MAPPING_IDS);

                    // 좌석 예약 시도
                    preoccupyService.preoccupySeat(request, userId);
                    successCounter.incrementAndGet(); // 성공 시 카운터 증가
                } catch (Exception e) {
                    failureCounter.incrementAndGet(); // 실패 시 카운터 증가
                } finally {
                    latch.countDown(); // 작업 완료
                }
            });
        }

        latch.await(); // 모든 스레드가 완료될 때까지 대기
        executorService.shutdown(); // 스레드 풀 종료

        log.info("예약에 성공한 사용자 수 = {}", successCounter.get());
        log.info("예약에 실패한 사용자 수 = {}", failureCounter.get());

        assertEquals(1, successCounter.get(), "하나의 사용자만 좌석 예약에 성공해야 합니다.");
        assertEquals(threadNum - 1, failureCounter.get(), "한 명을 제외한 사용자는 좌석 예약에 실패해야 합니다.");
    }

    @Test
    @DisplayName("예약 제한 초과 테스트")
    void testExceedReservationLimit() {
        SelectSeat request = new SelectSeat(EVENT_SCHEDULE_ID, 1, Set.of(34L, 35L)); // 예약 제한 초과 요청
        Long userId = 1L;

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            preoccupyService.preoccupySeat(request, userId);
        });

        log.info("예약 제한 초과 테스트 실패 메시지: {}", exception.getMessage());
    }

    @Test
    @DisplayName("이미 예약된 좌석 테스트")
    void testAlreadyReservedSeat() {
        Long userId = 1L;
        SelectSeat request = new SelectSeat(EVENT_SCHEDULE_ID, RESERVATION_LIMIT, SEAT_MAPPING_IDS);

        // 첫 번째 사용자가 좌석 예약
        preoccupyService.preoccupySeat(request, userId);

        // 두 번째 사용자가 동일한 좌석 예약 시도
        Long anotherUserId = 2L;
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            preoccupyService.preoccupySeat(request, anotherUserId);
        });

        log.info("이미 예약된 좌석에 대한 실패 메시지: {}", exception.getMessage());
    }

    @Test
    @DisplayName("Redis 락 동작 확인")
    void testRedisLock() {
        String key = "TestLock";
        RBucket<String> bucket = redissonClient.getBucket(key);

        // 첫 번째 락 시도
        boolean locked = bucket.trySet("locked", 1, TimeUnit.SECONDS);
        assertTrue(locked, "첫 번째 락은 성공해야 합니다.");

        // 두 번째 락 시도
        boolean lockedAgain = bucket.trySet("locked", 1, TimeUnit.SECONDS);
        assertFalse(lockedAgain, "두 번째 락은 실패해야 합니다.");

        // 락 해제 후 재시도
        bucket.delete();
        boolean lockedAfterDelete = bucket.trySet("locked", 1, TimeUnit.SECONDS);
        assertTrue(lockedAfterDelete, "락 해제 후에는 다시 락이 가능해야 합니다.");
    }

//    @Test
//    @DisplayName("사용자가 1~4개의 좌석 랜덤 선택 - 모든 좌석이 다 팔릴 때까지 진행 - 동시성 테스트")
//    void testUntilAllSeatsSoldWithRandomSeatsPerUser() throws InterruptedException {
//        int threadNum = 1000; // 동시 요청 스레드 수
//        int totalSeats = 100; // 시스템에 존재하는 좌석 수
//        AtomicInteger successCounter = new AtomicInteger(0); // 성공 카운터
//        AtomicInteger failureCounter = new AtomicInteger(0); // 실패 카운터
//
//        // 사용 가능한 좌석 목록
//        Set<Long> allSeatIds = Collections.synchronizedSet(new HashSet<>());
//        for (long i = 1L; i <= totalSeats; i++) {
//            allSeatIds.add(i);
//        }
//
//        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
//        java.util.Random random = new java.util.Random();
//
//        while (successCounter.get() < totalSeats) {
//            CountDownLatch latch = new CountDownLatch(threadNum); // 각 반복에서 새로운 CountDownLatch 생성
//
//            for (long i = 1L; i <= threadNum; i++) {
//                Long userId = i; // 각 사용자 고유 ID
//                executorService.submit(() -> {
//                    Set<Long> selectedSeats = new HashSet<>();
//
//                    try {
//                        int seatsToSelect;
//
//                        synchronized (allSeatIds) {
//                            if (allSeatIds.isEmpty()) {
//                                return; // 모든 좌석이 팔린 경우
//                            }
//
//                            // 랜덤하게 1~4개의 좌석 선택
//                            seatsToSelect = random.nextInt(4) + 1;
//
//                            Iterator<Long> iterator = allSeatIds.iterator();
//                            while (iterator.hasNext() && selectedSeats.size() < seatsToSelect) {
//                                selectedSeats.add(iterator.next());
//                                iterator.remove(); // 선택된 좌석 제거
//                            }
//                        }
//
//                        if (selectedSeats.isEmpty()) {
//                            return; // 선택된 좌석이 없으면 종료
//                        }
//
//                        // SelectSeat 객체 생성
//                        SelectSeat request = new SelectSeat(EVENT_SCHEDULE_ID, seatsToSelect, selectedSeats);
//
//                        // 좌석 예약 시도
//                        preoccupyService.preoccupySeat(request, userId);
//                        successCounter.addAndGet(selectedSeats.size());
//
//                    } catch (Exception e) {
//                        failureCounter.incrementAndGet(); // 실패 시 카운터 증가
//                        synchronized (allSeatIds) {
//                            allSeatIds.addAll(selectedSeats); // 실패한 좌석 다시 복구
//                        }
//                    } finally {
//                        latch.countDown(); // latch 감소
//                    }
//                });
//            }
//
//            latch.await(); // 모든 스레드가 완료될 때까지 대기
//        }
//
//        executorService.shutdown();
//
//        log.info("총 성공 예약된 좌석 수 = {}", successCounter.get());
//        log.info("예약에 실패한 사용자 수 = {}", failureCounter.get());
//
//        assertEquals(totalSeats, successCounter.get(), "총 성공 예약된 좌석 수는 시스템 좌석 수와 같아야 합니다.");
//    }


    @Test
    @DisplayName("사용자가 1~4개의 좌석 랜덤 선택 - 모든 좌석이 다 팔릴 때까지 진행 - 동시성 테스트")
    void testUntilAllSeatsSoldWithRandomSeatsPerUser() throws InterruptedException {
        int threadNum = 500; // 동시 요청 스레드 수
        int totalSeats = 10; // 시스템에 존재하는 좌석 수
        AtomicInteger successCounter = new AtomicInteger(0); // 성공 카운터
        AtomicInteger failureCounter = new AtomicInteger(0); // 실패 카운터

        // 사용 가능한 좌석 목록 (BlockingQueue로 변경)
        BlockingQueue<Long> allSeatIds = new LinkedBlockingQueue<>();
        for (long i = 1L; i <= totalSeats; i++) {
            allSeatIds.add(i);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        java.util.Random random = new java.util.Random();

        while (successCounter.get() < totalSeats) {
            CountDownLatch latch = new CountDownLatch(threadNum); // 각 반복에서 새로운 CountDownLatch 생성

            for (long i = 1L; i <= threadNum; i++) {
                Long userId = i; // 각 사용자 고유 ID
                executorService.submit(() -> {
                    List<Long> selectedSeats = new ArrayList<>();

                    try {
                        int seatsToSelect = random.nextInt(4) + 1; // 랜덤하게 1~4개의 좌석 선택

                        // 좌석 선택
                        for (int j = 0; j < seatsToSelect; j++) {
                            Long seat = allSeatIds.poll(); // 큐에서 좌석 하나 가져오기
                            if (seat != null) {
                                selectedSeats.add(seat);
                            } else {
                                break; // 좌석이 다 팔린 경우
                            }
                        }

                        if (selectedSeats.isEmpty()) {
                            return; // 선택된 좌석이 없으면 종료
                        }

                        // SelectSeat 객체 생성
                        SelectSeat request = new SelectSeat(EVENT_SCHEDULE_ID, selectedSeats.size(), new HashSet<>(selectedSeats));

                        // 좌석 예약 시도
                        preoccupyService.preoccupySeat(request, userId);
                        successCounter.addAndGet(selectedSeats.size());

                    } catch (Exception e) {
                        failureCounter.incrementAndGet(); // 실패 시 카운터 증가
                        // 실패한 좌석 다시 복구
                        allSeatIds.addAll(selectedSeats);
                    } finally {
                        latch.countDown(); // latch 감소
                    }
                });
            }

            latch.await(); // 모든 스레드가 완료될 때까지 대기
        }

        executorService.shutdown();

        log.info("총 성공 예약된 좌석 수 = {}", successCounter.get());
        log.info("예약에 실패한 사용자 수 = {}", failureCounter.get());

        assertEquals(totalSeats, successCounter.get(), "총 성공 예약된 좌석 수는 시스템 좌석 수와 같아야 합니다.");
    }

}
